package it.attendance100.mybicocca.data.remote.bicoccapp.api

import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(BicoccappGlobalApiData::class)
@Tag("live")
abstract class BicoccappApiTestBase {
    protected val username: String = BicoccappGlobalApiData.username

    protected val password: String = BicoccappGlobalApiData.password

    protected val session: BicoccappAuthSession
        get() = BicoccappGlobalApiData.session
            ?: throw IllegalStateException("Session not initialized. Ensure BicoccappGlobalApiData#beforeAll has run.")

    protected val profile: BicoccappUserProfile
        get() = BicoccappGlobalApiData.profile
            ?: throw IllegalStateException("Profile not initialized. Ensure BicoccappGlobalApiData#beforeAll has run.")

    protected val api: BicoccappApi
        get() = BicoccappGlobalApiData.api
}