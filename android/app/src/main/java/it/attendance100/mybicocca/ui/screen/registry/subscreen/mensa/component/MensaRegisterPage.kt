package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.button.RetryButton

private const val COMPASS_HOST = "unibicocca.compasscloud.it"
private const val COMPASS_ORIGIN = "https://$COMPASS_HOST"

/**
 * The entry the official app opens its login with. It marks the session as the app's, and only
 * such a session is served the sign-up form; opened cold, the form sends you back to the login.
 */
private const val APP_ENTRY_URL = "$COMPASS_ORIGIN/api/apps/v1/mylunch/auth/getToken"
private const val REGISTER_URL = "$COMPASS_ORIGIN/area-riservata/register"

/** The page that ends the flow, once the emailed code has been accepted ("il tuo account è stato attivato"). */
private val ACTIVATED_TEXT = Regex("stato attivato|stata attivata|been activated|is now active", RegexOption.IGNORE_CASE)

/**
 * The site's header, its title band (it repeats the sheet's title, and its "back to services"
 * duplicates the sheet's back) and its footer: only the form and its messages stay.
 */
private const val HIDE_CHROME_CSS =
    "header.header,.frontend_header,footer.footer,.back-to-top{display:none!important}" +
        "body{padding-top:0!important;margin-top:0!important}"

/** The app's colours, as CSS hex, for restyling the form. */
private class FormPalette(
    val background: String,
    val text: String,
    val muted: String,
    val field: String,
    val container: String,
    val outline: String,
    val primary: String,
    val onPrimary: String,
    val error: String,
)

/**
 * Restyles Compass's Bootstrap form in the app's palette. It only targets the form's own classes:
 * if Compass rebuilds the page, the rules stop matching and the page falls back to its own look.
 */
private fun themeCss(p: FormPalette): String =
    "html,body{background:${p.background}!important;color:${p.muted}!important;font-family:Roboto,system-ui,sans-serif!important}" +
        "label,.form-label{color:${p.text}!important;font-weight:500}" +
        "p,.text-center,.text-left{color:${p.muted}!important}" +
        "a{color:${p.primary}!important}" +
        ".form-control{background:${p.field}!important;color:${p.text}!important;border:1px solid ${p.outline}!important;" +
        "border-radius:14px!important;height:48px!important;box-shadow:none!important}" +
        ".form-control:focus{border:2px solid ${p.primary}!important}" +
        // The site marks required fields with a thick orange left border, on a more specific rule.
        "form.registration_form input.form-control.required{border-left:1px solid ${p.outline}!important}" +
        "form.registration_form input.form-control.required:focus{border-left:2px solid ${p.primary}!important}" +
        ".input-group .form-control{border-radius:14px 0 0 14px!important;border-right:none!important}" +
        ".input-group-addon{background:${p.container}!important;border:1px solid ${p.outline}!important;border-left:none!important;" +
        "border-radius:0 14px 14px 0!important}" +
        ".fa-check{color:${p.primary}!important}.fa-exclamation{color:${p.error}!important}" +
        ".btn-link,.btn-link i{color:${p.primary}!important}.btn-link{border:none!important;background:none!important}" +
        ".bs-callout{background:${p.container}!important;border:none!important;border-left:6px solid ${p.primary}!important;border-radius:16px!important}" +
        ".bs-callout h3{color:${p.primary}!important}" +
        ".btn-secondary{background:${p.container}!important;color:${p.text}!important;border:none!important;border-radius:12px!important}" +
        ".btn-info,.btn-primary,.btn-success{background:${p.primary}!important;color:${p.onPrimary}!important;border:none!important;" +
        "border-radius:999px!important;min-height:52px!important;font-weight:600!important}" +
        ".btn-info.disabled,.btn-info:disabled{opacity:.4!important}" +
        "input[type=checkbox]{accent-color:${p.primary}}" +
        "img{border-radius:10px}" +
        ".modal-content{background:${p.background}!important;color:${p.text}!important;border-radius:24px!important}"

/**
 * Hides the site's chrome, applies [css], and follows what is typed in the sign-up form, so the new
 * account can be signed in to as soon as it is activated. Re-run on every page: a refused submission
 * comes back as a new page with the form.
 */
private fun watchFormJs(css: String): String = """
    (function() {
      var s = document.getElementById('mybicocca-style') || document.createElement('style');
      s.id = 'mybicocca-style';
      s.textContent = '$HIDE_CHROME_CSS$css';
      document.head.appendChild(s);
      var f = document.querySelector('form.registration_form');
      if (!f || f.dataset.mybicocca) return;
      f.dataset.mybicocca = '1';
      var send = function() {
        var e = f.querySelector('[name=email]'), p = f.querySelector('[name=password]');
        if (e && p) MyBicoccaSignUp.update(e.value, p.value);
      };
      ['input', 'change', 'submit'].forEach(function(t) { f.addEventListener(t, send, true); });
    })();
""".trimIndent()

private fun Color.css(): String = "#%06X".format(toArgb() and 0xFFFFFF)

/** The email and password the user chose while signing up, kept in memory only. */
data class MensaNewAccount(val email: String, val password: String)

/**
 * Compass's own sign-up form, shown in place: there is no sign-up API, and the form carries a
 * captcha and an email check that only the website can run. When the activation page appears the
 * form closes at once and [onActivated] gets the credentials typed in it (null if they were typed
 * in an earlier session), so the caller can sign in without asking for them again.
 */
@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
internal fun MensaRegisterPage(
    onActivated: (MensaNewAccount?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnActivated by rememberUpdatedState(onActivated)
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    val clientHolder = remember { arrayOfNulls<RegisterClient>(1) }
    val scheme = MaterialTheme.colorScheme
    val sheetColor = scheme.surfaceContainerLow
    val css = themeCss(
        FormPalette(
            background = sheetColor.css(),
            text = scheme.onSurface.css(),
            muted = scheme.onSurfaceVariant.css(),
            field = scheme.surfaceContainerHighest.css(),
            container = scheme.surfaceContainerHigh.css(),
            outline = scheme.outline.css(),
            primary = scheme.primary.css(),
            onPrimary = scheme.onPrimary.css(),
            error = scheme.error.css(),
        ),
    )

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    setBackgroundColor(sheetColor.toArgb())
                    // The page keeps its vertical swipes; without this the sheet's own gestures
                    // would cancel them a few pixels in.
                    setOnTouchListener { view, event ->
                        if (event.actionMasked == MotionEvent.ACTION_DOWN) view.parent?.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                    val form = SignUpFormBridge()
                    addJavascriptInterface(form, "MyBicoccaSignUp")
                    webViewClient = RegisterClient(
                        pageScript = watchFormJs(css),
                        onLoading = { loading = it },
                        onFailed = { failed = true },
                        onActivated = { currentOnActivated(form.account()) },
                    ).also { clientHolder[0] = it }
                    loadUrl(APP_ENTRY_URL)
                    webView = this
                }
            },
            // A theme change while the form is open restyles it in place.
            update = { view ->
                val client = clientHolder[0] ?: return@AndroidView
                val script = watchFormJs(css)
                if (client.pageScript != script) {
                    client.pageScript = script
                    view.setBackgroundColor(sheetColor.toArgb())
                    view.evaluateJavascript(script, null)
                }
            },
            onRelease = { it.destroy() },
            // Hidden while a page loads, so the site's header never flashes before it is hidden.
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (loading || failed) 0f else 1f),
        )

        if (loading && !failed) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
        }

        if (failed) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = scheme.onSurfaceVariant)
                Text(
                    text = stringResource(R.string.mensa_register_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                RetryButton(onClick = {
                    failed = false
                    webView?.loadUrl(APP_ENTRY_URL)
                })
            }
        }
    }
}

/** Receives the form's fields from [WATCH_FORM_JS]; called on the WebView's JavaScript thread. */
private class SignUpFormBridge {
    @Volatile private var email: String = ""
    @Volatile private var password: String = ""

    @JavascriptInterface
    fun update(email: String?, password: String?) {
        this.email = email.orEmpty().trim()
        this.password = password.orEmpty()
    }

    fun account(): MensaNewAccount? =
        MensaNewAccount(email, password).takeIf { email.isNotBlank() && password.isNotEmpty() }
}

private class RegisterClient(
    var pageScript: String,
    private val onLoading: (Boolean) -> Unit,
    private val onFailed: () -> Unit,
    private val onActivated: () -> Unit,
) : WebViewClient() {

    /** Set once the form has been shown; before that the login pages are just the way in. */
    private var formShown = false
    private var finished = false

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url
        if (url.host != COMPASS_HOST) {
            // The privacy policy and certifications live on other sites: the browser opens them.
            runCatching { view.context.startActivity(Intent(Intent.ACTION_VIEW, url).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            return true
        }
        return false
    }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        onLoading(true)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        if (finished) return
        val path = url?.let(Uri::parse)?.takeIf { it.host == COMPASS_HOST }?.path.orEmpty()
        if (!formShown && !path.startsWith("/area-riservata")) {
            // The app entry has landed on the login page: the session is now the app's.
            if (path.startsWith("/auth")) {
                formShown = true
                view.loadUrl(REGISTER_URL)
            }
            return
        }
        // Back on the site's login after the form: the flow is over, whether it ended on the
        // activation page or not, and signing in is our login page's job.
        if (formShown && path.startsWith("/auth")) {
            finished = true
            onActivated()
            return
        }
        view.evaluateJavascript(pageScript, null)
        view.evaluateJavascript("document.body ? document.body.innerText : ''") { text ->
            if (!finished && text != null && ACTIVATED_TEXT.containsMatchIn(text)) {
                finished = true
                onActivated()
            } else {
                onLoading(false)
            }
        }
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        if (request.isForMainFrame) {
            onLoading(false)
            onFailed()
        }
    }
}
