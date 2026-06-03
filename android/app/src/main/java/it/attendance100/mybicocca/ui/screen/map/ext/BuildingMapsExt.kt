package it.attendance100.mybicocca.ui.screen.map.ext

import android.content.Context
import android.content.Intent
import android.net.Uri
import it.attendance100.mybicocca.domain.model.map.MapBuilding

// Opens the building pinned and labeled in the user's maps app.
fun Context.openBuildingInMaps(building: MapBuilding) {
    val lat = building.point.latitude
    val lng = building.point.longitude
    val label = Uri.encode(building.name)
    runCatching {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

// "ATLAS ex U1" -> "U1 - ATLAS"; sites without a legacy U-number keep their full name.
fun buildingDisplayName(building: MapBuilding): String {
    val (name, legacyAlias) = splitLegacyAlias(building.name)
    return if (legacyAlias != null) "$legacyAlias - $name" else name
}
