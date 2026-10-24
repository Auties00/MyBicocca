package it.attendance100.mybicocca.data.remote.easystaff.api

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.logging.*
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningPortal
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

object EasyStaffGlobalApiData : BeforeAllCallback, AutoCloseable {
    var api: EasyStaffApi? = null
    var planning: EasyStaffPlanningApi? = null

    override fun beforeAll(context: ExtensionContext) {
        val logging: HttpClientConfig<*>.() -> Unit = {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }
        if (api == null) api = EasyStaffApi(logging)
        if (planning == null) planning = EasyStaffPlanningApi(EasyStaffPlanningPortal.INFORMATION_DESKS, logging)
    }

    override fun close() {
        api?.close()
        planning?.close()
    }
}
