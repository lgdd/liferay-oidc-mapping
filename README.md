# Liferay OIDC Mapping

Components adapted from:

- https://github.com/fabian-bouche-liferay/saml-user-group-mapping/
- https://github.com/fabian-bouche-liferay/oidc-userinfo-mapping/

## Modules

- [**OIDC User Group Mapping**](modules/oidc-user-group-mapping): allows to map user groups from information in the id token or access
  token using JSONPath, with an option to always clean user groups to only sync user groups with
  OIDC info.