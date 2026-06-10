package it.attendance100.mybicocca.data.remote.affluences.api

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(AffluencesGlobalApiData::class)
@Tag("live")
abstract class AffluencesTestBase {
    companion object {
        /** Root site of the Bicocca library system, used as the reference site across tests. */
        const val ATENEO_LIBRARY_SLUG = "universita-bicocca-biblioteca-di-ateneo"

        /** Child site with booking enabled (seat zones), used by the reservation tests. */
        const val CENTRAL_LIBRARY_ID = "4912615d-501b-4c4e-a2a7-ccaec81c5429"
    }

    protected val api: AffluencesApi
        get() = AffluencesGlobalApiData.api
            ?: throw IllegalStateException("API not initialized. Ensure AffluencesGlobalApiData#beforeAll has run.")
}
