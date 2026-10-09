package it.attendance100.mybicocca.domain.usecase.notification

import it.attendance100.mybicocca.core.notification.NotificationRoute
import it.attendance100.mybicocca.domain.repository.DeepLinkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Hands the route carried by a notification tap to the shell. Invoked by the activity, which is
 * where the intent lands, since a notification's tap intent must start an Activity directly.
 */
class SubmitNotificationRouteUseCase @Inject constructor(
    private val repository: DeepLinkRepository,
) {
    operator fun invoke(route: NotificationRoute) = repository.submitNotificationRoute(route)
}

/** The route a notification tap is waiting to be acted on, or null. */
class ObservePendingNotificationRouteUseCase @Inject constructor(
    private val repository: DeepLinkRepository,
) {
    operator fun invoke(): Flow<NotificationRoute?> = repository.observePendingNotificationRoute()
}

/** Clears the pending route once the shell has acted on it, so it isn't replayed. */
class ConsumeNotificationRouteUseCase @Inject constructor(
    private val repository: DeepLinkRepository,
) {
    operator fun invoke() = repository.consumeNotificationRoute()
}
