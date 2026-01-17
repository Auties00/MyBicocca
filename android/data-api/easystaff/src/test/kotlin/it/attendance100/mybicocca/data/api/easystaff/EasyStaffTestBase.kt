package it.attendance100.mybicocca.data.api.easystaff

import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffAcademicYear
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(EasyStaffGlobalApiData::class)
abstract class EasyStaffTestBase {
    protected val api: EasyStaffApi
        get() = EasyStaffGlobalApiData.api
            ?: throw IllegalStateException("API not initialized. Ensure EasyStaffGlobalApiData#beforeAll has run.")

    protected fun EasyStaffAcademicYear.toStartDate(): LocalDate = LocalDate.of(this.startYear, 1, 1)

    protected fun EasyStaffAcademicYear.toEndDate(): LocalDate = LocalDate.of(this.endYear, 12, 31)
}
