package it.attendance100.mybicocca.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import it.attendance100.mybicocca.domain.model.Exam
import it.attendance100.mybicocca.domain.model.GradePoint

@Entity(tableName = "career_stats")
data class CareerStatsEntity(
    @PrimaryKey val id: Int = 0, // Constant ID = 0 because we only store the current logged-in user's stats
    val mediaAritmetica: Float,
    val mediaPonderata: Float,
    val esamiSostenuti: Int,
    val esamiTotali: Int,
    val cfuAcquisiti: Int,
    val cfuTotali: Int,
    val grades: List<GradePoint>,
    val passedExams: List<Exam>,
    val remainingExams: List<Exam>,
)