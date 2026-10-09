package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.notification.NotificationRoute
import it.attendance100.mybicocca.domain.model.library.LibraryDeepLinkAction
import it.attendance100.mybicocca.domain.repository.DeepLinkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the pending deep-link payloads in process-wide [MutableStateFlow]s. State-flow semantics
 * give the hand-off its stickiness: a link submitted before the consuming ViewModel exists is
 * replayed to it on first collection. Nothing is persisted.
 */
@Singleton
class DeepLinkRepositoryImpl @Inject constructor() : DeepLinkRepository {

    private val pendingPresenceScan = MutableStateFlow<String?>(null)
    private val pendingLibraryAction = MutableStateFlow<LibraryDeepLinkAction?>(null)
    private val pendingNotificationRoute = MutableStateFlow<NotificationRoute?>(null)

    override fun observePendingPresenceScan(): Flow<String?> = pendingPresenceScan.asStateFlow()

    override fun submitPresenceScan(rawLink: String) {
        pendingPresenceScan.value = rawLink
    }

    override fun consumePresenceScan() {
        pendingPresenceScan.value = null
    }

    override fun observePendingLibraryAction(): Flow<LibraryDeepLinkAction?> = pendingLibraryAction.asStateFlow()

    override fun submitLibraryAction(action: LibraryDeepLinkAction) {
        pendingLibraryAction.value = action
    }

    override fun consumeLibraryAction() {
        pendingLibraryAction.value = null
    }

    override fun observePendingNotificationRoute(): Flow<NotificationRoute?> = pendingNotificationRoute.asStateFlow()

    override fun submitNotificationRoute(route: NotificationRoute) {
        pendingNotificationRoute.value = route
    }

    override fun consumeNotificationRoute() {
        pendingNotificationRoute.value = null
    }
}
