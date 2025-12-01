package it.attendance100.mybicocca.utils

import android.content.*
import android.net.*
import dagger.hilt.android.qualifiers.*
import kotlinx.coroutines.flow.*
import javax.inject.*

@Singleton
class NetworkMonitor @Inject constructor(@ApplicationContext context: Context) {
  private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  private val _isOnline = MutableStateFlow(getCurrentConnectivityState(connectivityManager))
  val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

  private val callback = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
      _isOnline.value = true
    }

    override fun onLost(network: Network) {
      _isOnline.value = false
    }
  }

  init {
    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    connectivityManager.registerNetworkCallback(request, callback)
  }

  fun refresh() {
    _isOnline.value = getCurrentConnectivityState(connectivityManager)
  }

  private fun getCurrentConnectivityState(connectivityManager: ConnectivityManager): Boolean {
    val network = connectivityManager.activeNetwork ?: return false
    val actNw = connectivityManager.getNetworkCapabilities(network) ?: return false

    return when {
      actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
      actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
      actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
      else -> false
    }
  }
}
