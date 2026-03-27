package it.attendance100.mybicocca.ui.component.shape

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import androidx.annotation.DrawableRes
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException


data class VectorDefinition(
    val pathData: String,
    val viewportWidth: Float,
    val viewportHeight: Float
)

/**
 * Parses a Vector Drawable resource ID to extract the pathData and viewport size.
 * Note: This assumes the vector has a <vector> root and at least one <path> with android:pathData.
 * It takes the FIRST path found.
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
            when (type) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "vector" -> {
                            viewportWidth =
                                getFloatAttribute(context.resources, parser, "viewportWidth")
                            viewportHeight =
                                getFloatAttribute(context.resources, parser, "viewportHeight")
                        }

                        "path" -> {
                            // Only get the first path if multiple exist, or merge logic needed for complex icons
                            if (pathData == null) {
                                pathData = getStringAttribute(context.resources, parser, "pathData")
                            }
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

private fun getStringAttribute(resources: Resources, parser: XmlPullParser, name: String): String? {
    // Android namespaces are tricky in compiled XML, we iterate attributes to find the match
    for (i in 0 until parser.attributeCount) {
        if (parser.getAttributeName(i) == name) {
            return parser.getAttributeValue(i)
        }
    }
    return null
}
