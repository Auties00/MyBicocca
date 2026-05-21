package it.attendance100.mybicocca.data.remote.elearning.api

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ElearningGlobalApiData::class)
abstract class ElearningTestApiBase {
    protected val username: String = ElearningGlobalApiData.username

    protected val password: String = ElearningGlobalApiData.password

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
