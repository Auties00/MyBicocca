package it.attendance100.mybicocca.data.remote.easystaff.api

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(EasyStaffGlobalApiData::class)
@Tag("live")
abstract class EasyStaffTestBase {
    protected val api: EasyStaffApi
        get() = EasyStaffGlobalApiData.api
            ?: throw IllegalStateException("API not initialized. Ensure EasyStaffGlobalApiData#beforeAll has run.")

    protected val planning: EasyStaffPlanningApi
        get() = EasyStaffGlobalApiData.planning
            ?: throw IllegalStateException("API not initialized. Ensure EasyStaffGlobalApiData#beforeAll has run.")
}
