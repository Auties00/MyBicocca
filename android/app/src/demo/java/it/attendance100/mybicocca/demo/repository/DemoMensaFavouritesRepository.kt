package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.domain.model.mensa.MensaFavourite
import it.attendance100.mybicocca.domain.model.mensa.MensaFavouriteSettings
import it.attendance100.mybicocca.domain.repository.MensaFavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/** In-memory favourites for the demo student, who already likes two dishes on today's menu. */
@Singleton
class DemoMensaFavouritesRepository @Inject constructor() : MensaFavouritesRepository {

    private val favourites = MutableStateFlow(
        listOf(
            MensaFavourite(38612, "PASTA AL POMODORO"),
            MensaFavourite(88465, "PIZZA MARGHERITA"),
            MensaFavourite(91057, "FILETTO DI MERLUZZO DORATO"),
        ),
    )
    private val settings = MutableStateFlow(MensaFavouriteSettings(notify = true, lookaheadDays = 2))
    private var announced = emptySet<String>()

    override fun observeFavourites(): Flow<List<MensaFavourite>> = favourites.asStateFlow()
    override fun observeSettings(): Flow<MensaFavouriteSettings> = settings.asStateFlow()

    override suspend fun setFavourite(favourite: MensaFavourite, isFavourite: Boolean) {
        favourites.update { list -> list.filterNot { it.dishId == favourite.dishId } + listOfNotNull(favourite.takeIf { isFavourite }) }
    }

    override suspend fun setNotify(notify: Boolean) = settings.update { it.copy(notify = notify) }
    override suspend fun setLookaheadDays(days: Int) = settings.update { it.copy(lookaheadDays = days) }
    override suspend fun setNotifyTime(time: LocalTime) = settings.update { it.copy(notifyAt = time) }
    override suspend fun announcedKeys(): Set<String> = announced
    override suspend fun setAnnouncedKeys(keys: Set<String>) {
        announced = keys
    }
}
