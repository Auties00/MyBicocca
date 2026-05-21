package it.attendance100.mybicocca.data.remote.common.exception

/**
 * Exception thrown when an authentication or login flow step fails.
 *
 * This covers unexpected status codes, missing redirects, missing form
 * elements, and other failures during multi-step SSO/OIDC/SAML login flows.
 */
class AuthenticationException(
    message: String
) : Exception(message)
