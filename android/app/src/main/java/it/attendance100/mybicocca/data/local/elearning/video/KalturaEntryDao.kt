package it.attendance100.mybicocca.data.local.elearning.video

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/** Room access to the resolved cmId → Kaltura entry id bindings. */
@Dao
interface KalturaEntryDao {

    @Query("SELECT entry_id FROM elearning_kaltura_entry WHERE cm_id = :cmId LIMIT 1")
    suspend fun getEntryId(cmId: Int): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: KalturaEntryEntity)
}
