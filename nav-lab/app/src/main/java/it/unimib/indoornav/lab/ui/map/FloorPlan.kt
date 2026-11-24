package it.unimib.indoornav.lab.ui.map

import android.content.Context
import androidx.compose.ui.graphics.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.float
import java.util.concurrent.ConcurrentHashMap

data class PlanRoom(val x: Float, val y: Float, val number: String, val use: String)

/**
 * One floor of a building, in metres in the building's plan frame (the frame of layouts.csv), loaded
 * from assets/plans/<building>_<floor>.json, which scripts/export_floorplans.py writes from the DXFs.
 */
class FloorPlan(
    val building: String,
    val floor: String,
    val x0: Float,
    val y0: Float,
    val x1: Float,
    val y1: Float,
    val walls: Path,
    val openings: Path,
    val stairs: Path,
    val rooms: List<PlanRoom>,
) {
    val width get() = x1 - x0
    val height get() = y1 - y0

    companion object {
        private val cache = ConcurrentHashMap<String, FloorPlan>()

        /** Floors with a plan for [building], in the order they stack: basement, ground, then up. */
        fun floorsOf(context: Context, building: String): List<String> {
            val order = listOf("1i", "T", "1", "2", "3", "4", "5")
            val prefix = "${building}_"
            return context.assets.list("plans").orEmpty()
                .filter { it.startsWith(prefix) && it.endsWith(".json") }
                .map { it.removePrefix(prefix).removeSuffix(".json") }
                .sortedBy { order.indexOf(it).let { i -> if (i < 0) 99 else i } }
        }

        fun load(context: Context, building: String, floor: String): FloorPlan? =
            cache["$building/$floor"] ?: runCatching {
                val text = context.assets.open("plans/${building}_$floor.json").bufferedReader().use { it.readText() }
                parse(building, floor, Json.parseToJsonElement(text).jsonObject)
            }.getOrNull()?.also { cache["$building/$floor"] = it }

        private fun parse(building: String, floor: String, root: JsonObject): FloorPlan {
            val b = root.getValue("bounds").jsonArray.map { it.jsonPrimitive.float }
            val layers = root.getValue("layers").jsonObject
            fun pathOf(name: String) = Path().apply {
                (layers[name] as? JsonArray)?.forEach { poly ->
                    val pts = poly.jsonArray
                    for (i in 0 until pts.size / 2) {
                        val x = pts[2 * i].jsonPrimitive.float
                        val y = pts[2 * i + 1].jsonPrimitive.float
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
            }
            val rooms = root["rooms"]?.jsonArray?.map { r ->
                val a = r.jsonArray
                PlanRoom(a[0].jsonPrimitive.float, a[1].jsonPrimitive.float, a[2].jsonPrimitive.content, a[3].jsonPrimitive.content)
            }.orEmpty()
            return FloorPlan(building, floor, b[0], b[1], b[2], b[3], pathOf("walls"), pathOf("openings"), pathOf("stairs"), rooms)
        }
    }
}

/** A saved beacon's floor ("Floor 1", "1", "T") as a plan floor id. */
fun planFloorOf(floor: String): String = floor.trim().removePrefix("Floor").trim().ifEmpty { "" }
