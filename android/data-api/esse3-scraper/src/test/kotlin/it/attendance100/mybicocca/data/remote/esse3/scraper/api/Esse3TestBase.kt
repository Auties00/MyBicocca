package it.attendance100.mybicocca.data.remote.esse3.scraper.api

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(Esse3GlobalApiData::class)
@Tag("live")
abstract class Esse3TestBase {
    protected val api: Esse3Api
        get() = Esse3GlobalApiData.api
            ?: throw IllegalStateException("API not initialized. Ensure Esse3GlobalApiData#beforeAll has run.")
}