package it.attendance100.mybicocca.ui.component.shape

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import androidx.annotation.DrawableRes
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

/** The first path and viewport of a vector drawable — enough to rebuild its silhouette as a Compose Shape. */
data class VectorDefinition(
    val pathData: String,
    val viewportWidth: Float,
    val viewportHeight: Float,
)

/**
 * Extracts the first `<path android:pathData>` and the `<vector>` viewport from a vector
 * drawable, so a Compose Shape can be derived from the same asset used for the background.
 * Only the first path matters for the slice silhouette; later paths are ignored.
 */
@SuppressLint("ResourceType")
fun getVectorDefinition(context: Context, @DrawableRes resId: Int): VectorDefinition? {
    val parser = context.resources.getXml(resId)
    var type = parser.eventType
    var viewportWidth = 0f
    var viewportHeight = 0f
    var pathData: String? = null

    try {
        while (type != XmlPullParser.END_DOCUMENT) {
            if (type == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "vector" -> {
                        viewportWidth = getFloatAttribute(context.resources, parser, "viewportWidth")
                        viewportHeight = getFloatAttribute(context.resources, parser, "viewportHeight")
                    }

                    "path" -> {
                        if (pathData == null) {
                            pathData = getStringAttribute(context.resources, parser, "pathData")
                        }
                    }
                }
            }
            type = parser.next()
        }
    } catch (e: XmlPullParserException) {
        e.printStackTrace()
        return null
    }

    return if (pathData != null && viewportWidth > 0 && viewportHeight > 0) {
        VectorDefinition(pathData, viewportWidth, viewportHeight)
    } else {
        null
    }
}

private fun getFloatAttribute(resources: Resources, parser: XmlPullParser, name: String): Float {
    val value = getStringAttribute(resources, parser, name) ?: return 0f
    return value.replace("dp", "").replace("sp", "").toFloatOrNull() ?: 0f
}

/** Android namespaces are tricky in compiled XML, so attributes are matched by bare name. */
private fun getStringAttribute(resources: Resources, parser: XmlPullParser, name: String): String? {
    for (i in 0 until parser.attributeCount) {
        if (parser.getAttributeName(i) == name) {
            return parser.getAttributeValue(i)
        }
    }
    return null
}
