# Esse3 Web Module

Kotlin client for the parts of [Segreterie OnLine](https://s3w.si.unimib.it/) (Esse3) that its
REST API, wrapped by the sibling `esse3` module, doesn't expose. Esse3 renders them as
server-side Struts (`.do`) pages, so this module logs in through the university's Shibboleth SSO
and reads them with JSoup.

Only the self-declarations (autocertificazioni) are left: the app's Certificates screen lists
them and downloads their PDFs.

| Sub-API  | Description                                                    |
|----------|----------------------------------------------------------------|
| `auth`   | The SAML login, which yields the session cookies, and logout   |
| `legacy` | The self-declarations: their list and PDF download             |

## Usage

```kotlin
val cookies = Esse3AuthApi.login(username, password)
Esse3Api(cookies).use { api ->
    val certifications = api.legacy.getSelfCertifications()
    val pdf = api.legacy.downloadSelfCertification(certifications.first())
}
```

When the session expires, Esse3 redirects to the identity provider and the API throws
`AuthenticationException`: log in again and retry.

## Tests

The tests open Chrome for an interactive SSO login, then call the live site.
