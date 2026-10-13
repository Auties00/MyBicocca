package it.attendance100.mybicocca.demo.capture

import it.attendance100.mybicocca.ui.navigation.route.Route
import it.attendance100.mybicocca.ui.navigation.route.ShellTab
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * One screenshot: the tab to show and the routes to stack on it. [tap] lists string resource names
 * whose text the capture script taps afterwards, in whatever language it is capturing, for states
 * a route alone cannot reach. [buildingsList] and [building] open the map's own sheets, which are
 * not routes.
 */
@Serializable
data class Shot(
    val name: String,
    val tab: ShellTab,
    val routes: List<Route> = emptyList(),
    val tap: List<String> = emptyList(),
    val buildingsList: Boolean = false,
    val building: String? = null,
)

/** A route deliberately left without a screenshot, and why. */
@Serializable
data class Exemption(val type: String, val reason: String)

/**
 * The screenshots the demo build can produce, read from `screenshots.json`. The same file drives
 * the receiver on the device, the capture script on the host, and the test that fails when a
 * route has neither a shot nor an exemption.
 */
@Serializable
data class ScreenshotCatalog(val shots: List<Shot>, val exempt: List<Exemption>) {

    fun shot(name: String): Shot? = shots.firstOrNull { it.name == name }

    companion object {
        const val ASSET = "screenshots.json"
        const val ROUTE_PACKAGE = "it.attendance100.mybicocca.ui.navigation.route."

        private val json = Json { ignoreUnknownKeys = false }

        fun parse(text: String): ScreenshotCatalog =
            json.decodeFromJsonElement(serializer(), qualifyTypes(json.parseToJsonElement(text)))

        // The file names routes as "SheetRoute.Taxes"; the serializer knows them by full class name.
        private fun qualifyTypes(element: JsonElement): JsonElement = when (element) {
            is JsonArray -> JsonArray(element.map(::qualifyTypes))
            is JsonObject -> JsonObject(
                element.mapValues { (key, value) ->
                    if (key == "type" && value is JsonPrimitive && "reason" !in element.jsonObject) {
                        JsonPrimitive(ROUTE_PACKAGE + value.jsonPrimitive.content)
                    } else {
                        qualifyTypes(value)
                    }
                },
            )
            else -> element
        }
    }
}
