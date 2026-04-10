package it.attendance100.mybicocca.data.sync

data class SyncUiState(
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdatedAtMillis: Long? = null,
) {
    val hasFreshData: Boolean
        get() = lastUpdatedAtMillis != null
}
