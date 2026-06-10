package it.attendance100.mybicocca.data.local.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Row of the `careers` table, one per Esse3 career of a saved account.
 *
 * The primary key is the Esse3 career id (`stuId`). Rows reference their account through a
 * cascading foreign key, so deleting an account removes its careers too. `status` stores the
 * domain CareerStatus enum name and is parsed leniently on read, letting unknown values degrade
 * instead of crashing.
 */
@Entity(
    tableName = "careers",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("account_id")],
)
data class CareerEntity(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "enrollment_trait_id") val enrollmentTraitId: Long,
    @ColumnInfo(name = "program_id") val programId: Long,
    @ColumnInfo(name = "easy_staff_program_code") val easyStaffProgramCode: String?,
    @ColumnInfo(name = "academic_year_enrollment_id") val academicYearEnrollmentId: Long,
    @ColumnInfo(name = "student_number") val studentNumber: String,
    val description: String,
    @ColumnInfo(name = "academic_year") val academicYear: Int,
    val status: String,
)
