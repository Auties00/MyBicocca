package it.attendance100.mybicocca.data.api.bicoccapp

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(BicoccappGlobalApiData::class)
abstract class BicoccappApiTestBase {
    protected val session: BicoccappAuthSession
        get() = BicoccappGlobalApiData.session
            ?: throw IllegalStateException("Session not initialized. Ensure BicoccappGlobalApiData#beforeAll has run.")

    protected val profile: BicoccappUserProfile
        get() = BicoccappGlobalApiData.profile
            ?: throw IllegalStateException("Profile not initialized. Ensure BicoccappGlobalApiData#beforeAll has run.")

    protected val api: BicoccappApi
        get() = BicoccappGlobalApiData.api
            ?: throw IllegalStateException("API not initialized. Ensure BicoccappGlobalApiData#beforeAll has run.")
}