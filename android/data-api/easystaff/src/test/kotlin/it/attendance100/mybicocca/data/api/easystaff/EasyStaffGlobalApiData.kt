package it.attendance100.mybicocca.data.api.easystaff

import io.ktor.client.plugins.logging.*
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

object EasyStaffGlobalApiData : BeforeAllCallback, AutoCloseable {
    var api: EasyStaffApi? = null

    override fun beforeAll(context: ExtensionContext) {
        if (api == null) {
            api = EasyStaffApi {
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
