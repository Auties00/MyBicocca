package it.attendance100.mybicocca.domain.repository

import java.time.LocalTime

import it.attendance100.mybicocca.domain.model.mensa.MensaFavourite
import it.attendance100.mybicocca.domain.model.mensa.MensaFavouriteSettings
import kotlinx.coroutines.flow.Flow

/** Device-local favourite dishes and the setting for being told when one is on a menu. */
interface MensaFavouritesRepository {
    fun observeFavourites(): Flow<List<MensaFavourite>>
    fun observeSettings(): Flow<MensaFavouriteSettings>
    suspend fun setFavourite(favourite: MensaFavourite, isFavourite: Boolean)
    suspend fun setNotify(notify: Boolean)
    suspend fun setLookaheadDays(days: Int)
    suspend fun setNotifyTime(time: LocalTime)

    /** Hits already announced, as [it.attendance100.mybicocca.domain.model.mensa.MensaFavouriteHit.key]s. */
    suspend fun announcedKeys(): Set<String>
    suspend fun setAnnouncedKeys(keys: Set<String>)
}
