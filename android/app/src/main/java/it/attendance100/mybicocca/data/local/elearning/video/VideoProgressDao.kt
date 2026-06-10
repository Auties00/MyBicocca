package it.attendance100.mybicocca.data.local.elearning.video

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Room access to saved video playback progress; every query is account-scoped. */
@Dao
interface VideoProgressDao {

    @Query(
        "SELECT * FROM elearning_video_progress " +
            "WHERE account_id = :accountId AND cm_id = :cmId LIMIT 1"
    )
    fun observe(accountId: String, cmId: Int): Flow<VideoProgressEntity?>

    @Query(
        "SELECT * FROM elearning_video_progress " +
            "WHERE account_id = :accountId AND course_id = :courseId"
    )
    fun observeByCourse(accountId: String, courseId: Int): Flow<List<VideoProgressEntity>>

    @Query(
        "SELECT * FROM elearning_video_progress " +
            "WHERE account_id = :accountId AND cm_id = :cmId LIMIT 1"
    )
    suspend fun getOnce(accountId: String, cmId: Int): VideoProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: VideoProgressEntity)

    @Query("DELETE FROM elearning_video_progress WHERE account_id = :accountId")
    suspend fun deleteForAccount(accountId: String)
}
