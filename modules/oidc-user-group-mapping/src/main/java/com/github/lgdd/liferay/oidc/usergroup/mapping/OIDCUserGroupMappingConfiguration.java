package com.github.lgdd.liferay.oidc.usergroup.mapping;

import aQute.bnd.annotation.metatype.Meta;

@Meta.OCD(
    id = OIDCUserGroupMappingConfiguration.PID,
    localization = "content/Language",
    name = "com.github.lgdd.liferay.oidc.usergroup.mapping.config-name"
)
public interface OIDCUserGroupMappingConfiguration {

  @Meta.AD(
      deflt = "false",
      required = false,
      name = "com.github.lgdd.liferay.oidc.usergroup.mapping.remove-user-groups",
      description = "com.github.lgdd.liferay.oidc.usergroup.mapping.remove-user-groups-desc"
  )
  boolean alwaysRemoveUserGroups();


  @Meta.AD(
      deflt = ACCESS_TOKEN,
      name = "com.github.lgdd.liferay.oidc.usergroup.mapping.token-to-parse",
      description = "com.github.lgdd.liferay.oidc.usergroup.mapping.token-to-parse-desc",
      optionLabels = {
          "com.github.lgdd.liferay.oidc.usergroup.mapping.token-to-parse.id-token",
          "com.github.lgdd.liferay.oidc.usergroup.mapping.token-to-parse.access-token"
      },
      optionValues = {
          ID_TOKEN,
          ACCESS_TOKEN,
      }, required = false
  )
  public String tokenToParse();

  @Meta.AD(
      deflt = "$.resource_access.my_client_id.roles[*]",
      required = false,
      name = "com.github.lgdd.liferay.oidc.usergroup.mapping.json-path",
      description = "com.github.lgdd.liferay.oidc.usergroup.mapping.json-path-desc"
  )
  String userGroupsJsonPath();

  String PID = "com.github.lgdd.liferay.oidc.usergroup.mapping.OIDCUserGroupMappingConfiguration";
  String ID_TOKEN = "id-token";
  String ACCESS_TOKEN = "access-token";

}
