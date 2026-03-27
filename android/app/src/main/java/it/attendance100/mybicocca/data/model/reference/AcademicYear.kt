package it.attendance100.mybicocca.data.model.reference

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "academic_years")
data class AcademicYear(
    @PrimaryKey val startYear: Int,
) {
    val endYear: Int get() = startYear + 1
    val label: String get() = "$startYear/$endYear"
}
