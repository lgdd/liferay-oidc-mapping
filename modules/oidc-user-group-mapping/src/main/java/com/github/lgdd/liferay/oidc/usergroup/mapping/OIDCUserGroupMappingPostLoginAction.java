package com.github.lgdd.liferay.oidc.usergroup.mapping;

import com.jayway.jsonpath.JsonPath;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.events.LifecycleEvent;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.service.UserGroupLocalService;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.security.sso.openid.connect.constants.OpenIdConnectWebKeys;
import com.liferay.portal.security.sso.openid.connect.persistence.model.OpenIdConnectSession;
import com.liferay.portal.security.sso.openid.connect.persistence.service.OpenIdConnectSessionLocalService;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(
    immediate = true,
    property = "key=login.events.post",
    service = LifecycleAction.class,
    configurationPid = OIDCUserGroupMappingConfiguration.PID
)
public class OIDCUserGroupMappingPostLoginAction implements LifecycleAction {

  @Override
  public void processLifecycleEvent(LifecycleEvent lifecycleEvent) throws ActionException {

    HttpServletRequest httpServletRequest = lifecycleEvent.getRequest();
    HttpSession httpSession = httpServletRequest.getSession();
    Long openIdConnectSessionId = (Long) httpSession.getAttribute(
        OpenIdConnectWebKeys.OPEN_ID_CONNECT_SESSION_ID);

    long userId = _portal.getUserId(httpServletRequest);
    long companyId = _portal.getCompanyId(httpServletRequest);

    if (openIdConnectSessionId != null) {

      try {
        OpenIdConnectSession openIdConnectSession =
            _openIdConnectSessionLocalService.getOpenIdConnectSession(openIdConnectSessionId);
        String token = _getTokenToParse(openIdConnectSession);
        JWT jwt = JWTParser.parse(token);
        JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();
        String claimsJSONString = claimsSet.toJSONObject().toJSONString();

        if (_config.alwaysRemoveUserGroups()) {
          List<UserGroup> userGroups = _userGroupLocalService.getUserUserGroups(userId);
          _userGroupLocalService.deleteUserUserGroups(userId, userGroups);
        }

        List<String> userGroupNames =
            JsonPath.parse(claimsJSONString).read(_config.userGroupsJsonPath());

        for (String userGroupName : userGroupNames) {
          try {
            UserGroup userGroup = _userGroupLocalService.getUserGroup(companyId, userGroupName);
            _userGroupLocalService.addUserUserGroup(userId, userGroup.getUserGroupId());
          } catch (PortalException e) {
            if (_log.isErrorEnabled()) {
              _log.error("Failed to find user group " + userGroupName, e);
            }
          }
        }
      } catch (PortalException e) {
        _log.error("Failed to get openid connect session", e);
      } catch (ParseException e) {
        _log.error("Failed to parse token", e);
      } catch (Exception e) {
        _log.error(e.getLocalizedMessage());
      }
    }
  }

  private String _getTokenToParse(OpenIdConnectSession openIdConnectSession)
      throws Exception {
    switch (_config.tokenToParse()) {
      case OIDCUserGroupMappingConfiguration.ID_TOKEN:
        return openIdConnectSession.getIdToken();
      case OIDCUserGroupMappingConfiguration.ACCESS_TOKEN:
        return _jsonFactory.createJSONObject(openIdConnectSession.getAccessToken())
            .getString("access_token");
      default:
        throw new Exception("No token to parse");
    }
  }

  @Activate
  @Modified
  public void activate(Map<String, String> properties) {
    _config = ConfigurableUtil.createConfigurable(OIDCUserGroupMappingConfiguration.class,
        properties);
  }

  private volatile OIDCUserGroupMappingConfiguration _config;

  private static final Log _log =
      LogFactoryUtil.getLog(OIDCUserGroupMappingPostLoginAction.class);

  @Reference
  private JSONFactory _jsonFactory;

  @Reference
  private Portal _portal;

  @Reference
  private OpenIdConnectSessionLocalService _openIdConnectSessionLocalService;

  @Reference
  private UserGroupLocalService _userGroupLocalService;

}