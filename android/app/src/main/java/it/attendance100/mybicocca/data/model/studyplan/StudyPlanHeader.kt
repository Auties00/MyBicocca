package it.attendance100.mybicocca.data.model.studyplan

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_plan_headers")
data class StudyPlanHeader(
    @PrimaryKey val id: Long,
    val studentId: Long,
    val description: String? = null,
    val statusCode: String? = null,
    val statusDescription: String? = null,
    val choiceRegulationId: Long? = null,
    val schemaId: Long? = null,
    val planType: String? = null,
)
