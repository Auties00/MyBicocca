package it.attendance100.mybicocca.data.local.questionnaire

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/**
 * Read/replace access to the offline VAL_DID questionnaire-list mirrors. The activities list is
 * replaced wholesale per career, and an activity's questionnaire header plus its units are
 * replaced together per (career, activity); both are delete-then-insert in one transaction and
 * read back ordered by the preserved server position. Reads are consulted only when a live call
 * fails offline.
 */
@Dao
interface QuestionnaireCacheDao {

    @Query("SELECT * FROM cached_questionnaire_activity WHERE career_id = :careerId ORDER BY cache_order")
    suspend fun getActivities(careerId: Long): List<QuestionnaireActivityEntity>

    @Query("DELETE FROM cached_questionnaire_activity WHERE career_id = :careerId")
    suspend fun clearActivities(careerId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(rows: List<QuestionnaireActivityEntity>)

    @Transaction
    suspend fun replaceActivities(careerId: Long, rows: List<QuestionnaireActivityEntity>) {
        clearActivities(careerId)
        insertActivities(rows)
    }

    @Query(
        "SELECT * FROM cached_activity_questionnaires " +
            "WHERE career_id = :careerId AND activity_choice_id = :activityChoiceId",
    )
    suspend fun getActivityQuestionnaires(
        careerId: Long,
        activityChoiceId: Long,
    ): ActivityQuestionnairesEntity?

    @Query(
        "SELECT * FROM cached_questionnaire_unit " +
            "WHERE career_id = :careerId AND activity_choice_id = :activityChoiceId ORDER BY unit_order",
    )
    suspend fun getUnits(careerId: Long, activityChoiceId: Long): List<QuestionnaireUnitEntity>

    @Query(
        "DELETE FROM cached_activity_questionnaires " +
            "WHERE career_id = :careerId AND activity_choice_id = :activityChoiceId",
    )
    suspend fun clearActivityQuestionnaires(careerId: Long, activityChoiceId: Long)

    @Query(
        "DELETE FROM cached_questionnaire_unit " +
            "WHERE career_id = :careerId AND activity_choice_id = :activityChoiceId",
    )
    suspend fun clearUnits(careerId: Long, activityChoiceId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityQuestionnaires(row: ActivityQuestionnairesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(rows: List<QuestionnaireUnitEntity>)

    @Transaction
    suspend fun replaceActivityQuestionnaires(
        careerId: Long,
        activityChoiceId: Long,
        parentRow: ActivityQuestionnairesEntity,
        unitRows: List<QuestionnaireUnitEntity>,
    ) {
        clearActivityQuestionnaires(careerId, activityChoiceId)
        clearUnits(careerId, activityChoiceId)
        insertActivityQuestionnaires(parentRow)
        insertUnits(unitRows)
    }
}
