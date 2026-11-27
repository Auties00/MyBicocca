package it.unimib.indoornav.lab.inventory

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import java.io.File
import kotlin.math.max

/** Reference photos of where beacons are mounted, kept in filesDir/beacon_photos as downscaled JPEGs. */
object BeaconPhotos {
    private const val MAX_SIDE_PX = 1600

    fun dir(context: Context): File = File(context.filesDir, "beacon_photos").apply { mkdirs() }

    fun file(context: Context, name: String): File = File(dir(context), name)

    /** Copies the picked image in, downscaled; returns the new file name. */
    fun importPhoto(context: Context, uri: Uri, entryId: String): String {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val longest = max(info.size.width, info.size.height)
            if (longest > MAX_SIDE_PX) {
                val k = MAX_SIDE_PX.toFloat() / longest
                decoder.setTargetSize((info.size.width * k).toInt(), (info.size.height * k).toInt())
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        // A new name per photo, so a replaced photo is never served from an old cache.
        val name = "$entryId-${System.currentTimeMillis()}.jpg"
        file(context, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it) }
        return name
    }

    fun delete(context: Context, name: String?) {
        if (name != null) file(context, name).delete()
    }
}
