package it.attendance100.mybicocca.data.remote.affluences.api

import io.ktor.client.plugins.logging.*
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

object AffluencesGlobalApiData : BeforeAllCallback, AutoCloseable {
    var api: AffluencesApi? = null

    override fun beforeAll(context: ExtensionContext) {
        if (api == null) {
            api = AffluencesApi {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.ALL
                }
            }
        }
    }

    override fun close() {
        api?.close()
    }
}
