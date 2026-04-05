package it.attendance100.mybicocca.data.model.career

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "careers")
data class Career(
    @PrimaryKey val studentId: Long,
    val courseOfStudyDescription: String,
    val courseOfStudyCode: String? = null,
    val enrollmentYear: Int? = null,
    val statusDescription: String? = null,
    val statusCode: String? = null,
    val matricola: String? = null,
    val matricolaId: Long? = null
)
