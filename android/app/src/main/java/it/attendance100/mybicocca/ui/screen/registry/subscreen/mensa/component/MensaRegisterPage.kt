package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.MotionEvent
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

/**
 * Compass's own sign-up form, shown in place: there is no sign-up API, and the form carries a
 * captcha and an email check that only the website can run. Once the form leads back to the login
 * (its "back to services" link, or the end of the activation), [onDone] returns to our login page.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun MensaRegisterPage(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnDone by rememberUpdatedState(onDone)
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    val scheme = MaterialTheme.colorScheme

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    // The sheet would take the page's vertical swipes as drags of its own; the
                    // form keeps them, and the sheet still moves by its handle and closes with back.
                    setOnTouchListener { view, event ->
                        if (event.actionMasked == MotionEvent.ACTION_DOWN) view.parent?.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                    webViewClient = RegisterClient(
                        onLoading = { loading = it },
                        onFailed = { failed = true },
                        onDone = { currentOnDone() },
                    )
                    loadUrl(APP_ENTRY_URL)
                    webView = this
                }
            },
            onRelease = { it.destroy() },
            modifier = Modifier.fillMaxSize(),
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

private class RegisterClient(
    private val onLoading: (Boolean) -> Unit,
    private val onFailed: () -> Unit,
    private val onDone: () -> Unit,
) : WebViewClient() {

    /** Set once the form has been shown; before that the login pages are just the way in. */
    private var formShown = false

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url
        if (url.host != COMPASS_HOST) {
            // The privacy policy and certifications live on other sites: the browser opens them.
            runCatching { view.context.startActivity(Intent(Intent.ACTION_VIEW, url).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            return true
        }
        if (formShown && url.leadsToLogin()) {
            onDone()
            return true
        }
        return false
    }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        onLoading(true)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        val path = url?.let(Uri::parse)?.takeIf { it.host == COMPASS_HOST }?.path.orEmpty()
        when {
            !formShown && path.startsWith("/area-riservata/regist") -> formShown = true
            // The app entry has landed on the login page: the session is now the app's.
            !formShown && path.startsWith("/auth") -> {
                view.loadUrl(REGISTER_URL)
                return
            }
            // A redirect, not a link, can also bring the finished flow back to the login.
            formShown && url?.let(Uri::parse)?.leadsToLogin() == true -> {
                onDone()
                return
            }
        }
        onLoading(false)
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        if (request.isForMainFrame) {
            onLoading(false)
            onFailed()
        }
    }

    private fun Uri.leadsToLogin(): Boolean {
        val path = path.orEmpty().trimEnd('/')
        return path == "/area-riservata" || path == "/auth" || path.startsWith("/auth/")
    }
}
