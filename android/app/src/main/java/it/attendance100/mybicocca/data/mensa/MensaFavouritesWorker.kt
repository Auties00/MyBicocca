package it.attendance100.mybicocca.data.mensa

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.notification.NotificationChannelId
import it.attendance100.mybicocca.core.notification.NotificationId
import it.attendance100.mybicocca.core.notification.NotificationRoute
import it.attendance100.mybicocca.core.notification.NotificationSpec
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.data.notification.AppNotifier
import it.attendance100.mybicocca.domain.model.mensa.MensaFavouriteHit
import it.attendance100.mybicocca.domain.model.mensa.MensaFavouriteMatcher
import it.attendance100.mybicocca.domain.model.mensa.mensaMenuIdentity
import it.attendance100.mybicocca.domain.repository.MensaFavouritesRepository
import it.attendance100.mybicocca.domain.repository.MensaRepository
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Once a day, looks for the user's favourite dishes on every canteen and bar menu for today (and
 * the next days, if they asked) and posts one notification listing the new ones. A dish already
 * announced for that day and menu is not announced again.
 */
@HiltWorker
class MensaFavouritesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val mensa: MensaRepository,
    private val favourites: MensaFavouritesRepository,
    private val notifier: AppNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = favourites.observeSettings().first()
        val list = favourites.observeFavourites().first()
        if (!settings.notify || list.isEmpty()) return Result.success()

        val today = LocalDate.now(AppClock.clock)
        val menus = runCatching { mensa.getMenus() }.getOrElse { return Result.retry() }
        val schedules = menus.associate { menu ->
            menu.id to runCatching { mensa.getMenuSchedule(menu.id) }.getOrDefault(emptyMap())
        }
        val hits = MensaFavouriteMatcher.find(list, schedules, MensaFavouriteMatcher.days(today, settings.lookaheadDays))

        // Keys of past days are dropped, so the set never grows past a few days' worth.
        val announced = favourites.announcedKeys().filterTo(mutableSetOf()) { key ->
            runCatching { LocalDate.parse(key.substringBefore(':')) }.getOrNull()?.isBefore(today) == false
        }
        val fresh = hits.filter { it.key !in announced }
        if (fresh.isNotEmpty()) {
            val names = menus.associate { it.id to mensaMenuIdentity(it.id, it.name).displayName }
            notifier.post(MensaFavouriteNotifications.spec(applicationContext, fresh, names, today))
        }
        favourites.setAnnouncedKeys(announced + fresh.map { it.key })
        return Result.success()
    }
}

internal object MensaFavouriteNotifications {

    fun spec(context: Context, hits: List<MensaFavouriteHit>, menuNames: Map<Int, String>, today: LocalDate): NotificationSpec {
        // A dish served on several menus at the same meal reads as one line listing the places.
        val groups = hits.groupBy { Triple(it.date, it.mealName, dishName(it.dish.name)) }
        val lines = groups.map { (key, group) ->
            val places = group.map { menuNames[it.menuId] ?: "Mensa" }.distinct().joinToString(", ")
            "${day(context, key.first, today)}, ${key.second}: ${key.third} ($places)"
        }
        val (firstDate, _, firstDish) = groups.keys.first()
        val firstText = "${day(context, firstDate, today)}: $firstDish"
        return NotificationSpec(
            channel = NotificationChannelId.MENSA_FAVOURITES,
            id = NotificationId.MensaFavourites,
            title = context.getString(R.string.mensa_favourites_notification_title),
            text = if (groups.size == 1) firstText else context.getString(R.string.mensa_favourites_notification_more, firstText, groups.size - 1),
            bigText = lines.joinToString("\n"),
            route = NotificationRoute.Mensa,
        )
    }

    private fun day(context: Context, date: LocalDate, today: LocalDate): String = when (date) {
        today -> context.getString(R.string.mensa_today)
        today.plusDays(1) -> context.getString(R.string.mensa_tomorrow)
        else -> {
            val locale = context.resources.configuration.locales[0] ?: Locale.getDefault()
            date.format(DateTimeFormatter.ofPattern("EEE d MMM", locale)).replaceFirstChar { it.titlecase(locale) }
        }
    }

    /** The operator writes names in capitals; the notification reads them in sentence case. */
    private fun dishName(raw: String): String {
        val trimmed = raw.trim()
        val cased = if (trimmed.any { it.isLowerCase() }) trimmed else trimmed.lowercase().replace(Regex("""(\d\s?)l\b"""), "$1L")
        return cased.replaceFirstChar { it.uppercase() }
    }
}

/** Starts or stops the daily favourites check; it runs each morning while turned on. */
@Singleton
class MensaFavouritesScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /**
     * Plans the daily check for [at], or cancels it. It is planned anew each time (the app's start,
     * a new time), so the check stays on the chosen time: a periodic job otherwise keeps the slot
     * it was first given and drifts from there.
     */
    fun apply(enabled: Boolean, at: LocalTime) {
        val work = WorkManager.getInstance(context)
        if (!enabled) {
            work.cancelUniqueWork(WORK_NAME)
            return
        }
        val request = PeriodicWorkRequestBuilder<MensaFavouritesWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(untilNext(at).toMinutes(), TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        work.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request)
    }

    /** Runs the check now, for when the user has just turned it on or changed the favourites. */
    fun checkNow() {
        WorkManager.getInstance(context).enqueue(
            androidx.work.OneTimeWorkRequestBuilder<MensaFavouritesWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build(),
        )
    }

    private fun untilNext(at: LocalTime): Duration {
        val now = LocalDateTime.now(AppClock.clock)
        val todayAt = now.toLocalDate().atTime(at)
        val next = if (now.isBefore(todayAt)) todayAt else todayAt.plusDays(1)
        return Duration.between(now, next)
    }

    private companion object {
        const val WORK_NAME = "MensaFavouritesWorker"
    }
}
