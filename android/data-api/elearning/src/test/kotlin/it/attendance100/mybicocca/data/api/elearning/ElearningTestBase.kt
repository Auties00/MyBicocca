package it.attendance100.mybicocca.data.api.elearning

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ElearningGlobalApiData::class)
abstract class ElearningTestBase {
    protected val session: ElearningAuthSession
        get() = ElearningGlobalApiData.session
            ?: throw IllegalStateException("Session not initialized. Ensure ElearningGlobalApiData#beforeAll has run.")

    protected val profile: ElearningUserProfile
        get() = ElearningGlobalApiData.profile
            ?: throw IllegalStateException("Profile not initialized. Ensure ElearningGlobalApiData#beforeAll has run.")

    protected val api: ElearningApi
        get() = ElearningGlobalApiData.api
            ?: throw IllegalStateException("API not initialized. Ensure ElearningGlobalApiData#beforeAll has run.")
}
