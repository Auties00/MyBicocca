package it.attendance100.mybicocca.data.local.document

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

/**
 * Read/replace access to the offline document mirrors (student badge, academic titles, badge
 * images). The badge and the titles list are each replaced wholesale on a successful fetch
 * (delete-then-insert in one transaction); the titles list reads back ordered by the preserved
 * server position. Reads are consulted only when a live call fails offline.
 */
@Dao
interface DocumentCacheDao {

    @Query("SELECT * FROM cached_student_badge WHERE career_id = :careerId")
    suspend fun getBadge(careerId: Long): StudentBadgeEntity?

    @Upsert
    suspend fun upsertBadge(row: StudentBadgeEntity)

    @Query("DELETE FROM cached_student_badge WHERE career_id = :careerId")
    suspend fun clearBadge(careerId: Long)

    // A null live badge is a valid "no card" result, so it clears the row rather than leaving a
    // stale card behind; a non-null badge overwrites it.
    @Transaction
    suspend fun replaceBadge(careerId: Long, row: StudentBadgeEntity?) {
        clearBadge(careerId)
        if (row != null) upsertBadge(row)
    }

    @Query("SELECT * FROM cached_academic_title WHERE career_id = :careerId ORDER BY cache_order")
    suspend fun getTitles(careerId: Long): List<AcademicTitleEntity>

    @Query("SELECT * FROM cached_title_attribute WHERE career_id = :careerId ORDER BY title_id, attr_order")
    suspend fun getTitleAttributes(careerId: Long): List<TitleAttributeEntity>

    @Query("DELETE FROM cached_academic_title WHERE career_id = :careerId")
    suspend fun clearTitles(careerId: Long)

    @Query("DELETE FROM cached_title_attribute WHERE career_id = :careerId")
    suspend fun clearTitleAttributes(careerId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTitles(rows: List<AcademicTitleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTitleAttributes(rows: List<TitleAttributeEntity>)

    @Transaction
    suspend fun replaceTitles(
        careerId: Long,
        titleRows: List<AcademicTitleEntity>,
        attrRows: List<TitleAttributeEntity>,
    ) {
        clearTitleAttributes(careerId)
        clearTitles(careerId)
        insertTitles(titleRows)
        insertTitleAttributes(attrRows)
    }

    @Query("SELECT * FROM cached_badge_image WHERE blob_id = :blobId AND side = :side")
    suspend fun getImage(blobId: Long, side: String): BadgeImageEntity?

    @Upsert
    suspend fun upsertImage(row: BadgeImageEntity)
}
