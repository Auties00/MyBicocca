package it.attendance100.mybicocca.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.domain.repository.ConnectivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks online state through a default-network callback (not a per-NetworkRequest one) so
 * onLost fires only when the network the app actually routes through is gone with no
 * replacement: losing Wi-Fi while cellular is still up switches the default without ever
 * reporting offline. Online means the default network is VALIDATED, so a captive portal that
 * hasn't been passed reads as offline.
 */
@Singleton
class ConnectivityRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
) : ConnectivityRepository {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(currentlyOnline())

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            _isOnline.value = caps.hasValidatedInternet()
        }

        override fun onLost(network: Network) {
            _isOnline.value = false
        }

        override fun onUnavailable() {
            _isOnline.value = false
        }
    }

    init {
        connectivityManager.registerDefaultNetworkCallback(callback)
    }

    override fun observe(): Flow<Boolean> = _isOnline.asStateFlow()

    private fun currentlyOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasValidatedInternet()
    }

    private fun NetworkCapabilities.hasValidatedInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
