package it.attendance100.mybicocca.data.api.easystaff

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(EasyStaffGlobalApiData::class)
abstract class EasyStaffTestBase {
    protected val api: EasyStaffApi
        get() = EasyStaffGlobalApiData.api
            ?: throw IllegalStateException("API not initialized. Ensure EasyStaffGlobalApiData#beforeAll has run.")
}
