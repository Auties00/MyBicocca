package it.attendance100.mybicocca.ui.screen.login

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import it.attendance100.mybicocca.R

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginWebViewScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state = viewModel.loginState
    val startUrl = "https://backoffice-app.unimib.it/api/v1/auth/openid_connect"
    val callbackPrefix = "https://backoffice-app.unimib.it/api/v1/auth/openid_connect/callback"

    LaunchedEffect(state) {
        if (state is LoginViewModel.LoginState.Success) onLoginSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("University Login") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.arrow_back),
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is LoginViewModel.LoginState.Success -> {
                    // Show loading while redirecting
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is LoginViewModel.LoginState.Authenticating -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is LoginViewModel.LoginState.Error -> {
                    // Break the infinite loop: Show Error UI instead of WebView
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Login Failed", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            // Clears cookies to ensure a fresh session attempt
                            CookieManager.getInstance().removeAllCookies(null)
                            viewModel.resetState()
                        }) {
                            Text("Retry")
                        }
                    }
                }

                else -> { // Idle
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    userAgentString = userAgentString.replace("; wv", "")
                                }
                                webChromeClient = WebChromeClient()
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                    ): Boolean {
                                        val url = request?.url?.toString() ?: return false

                                        if (url.startsWith(callbackPrefix)) {
                                            Log.v("WebViewDebug", "Callback intercepted! URL: $url")

                                            // FIX: Extract Cookies for the domain
                                            val cookies = CookieManager.getInstance()
                                                .getCookie("https://backoffice-app.unimib.it") ?: ""
                                            // Log.d("WebViewDebug", "Extracted Cookies: $cookies")

                                            viewModel.handleCallbackUrl(url, cookies)
                                            return true
                                        }
                                        return false
                                    }
                                }
                                loadUrl(startUrl)
                            }
                        }
                    )
                }
            }
        }
    }
}