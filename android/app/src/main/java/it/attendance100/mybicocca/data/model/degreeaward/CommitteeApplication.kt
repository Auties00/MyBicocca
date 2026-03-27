package it.attendance100.mybicocca.data.model.degreeaward

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "committee_applications")
data class CommitteeApplication(
    @PrimaryKey val id: Long,
    val studentId: Long,
    val status: String? = null,
    val callDescription: String? = null,
    val submissionDate: String? = null,
)
