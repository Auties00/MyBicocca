package it.attendance100.mybicocca.data.remote.esse3.api

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(Esse3GlobalApiData::class)
@Tag("live")
abstract class Esse3ApiTestBase {
    protected val username: String = Esse3GlobalApiData.username

    protected val password: String = Esse3GlobalApiData.password

    protected val session: Esse3AuthSession
        get() = Esse3GlobalApiData.session
            ?: throw IllegalStateException("Session not initialized. Ensure Esse3GlobalApiData#beforeAll has run.")

    protected val studentProfile: Esse3StudentProfile
        get() = Esse3GlobalApiData.studentProfile
            ?: throw IllegalStateException("Student profile not initialized. Ensure Esse3GlobalApiData#beforeAll has run.")

    protected val api: Esse3Api
        get() = Esse3GlobalApiData.api
}
