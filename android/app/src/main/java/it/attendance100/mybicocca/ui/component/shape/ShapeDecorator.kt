package it.attendance100.mybicocca.ui.component.shape

import kotlin.random.Random

object ShapeDecorations {

    data class Placement(
        val centerXFraction: Float,
        val centerYFraction: Float,
        val sizeFraction: Float,
        val rotationDegrees: Float,
    )

    data class Anchor(
        val x: Float,
        val y: Float,
        val size: Float,
        val rotation: Float = 0f,
    )

    private val PREDEFINED_LAYOUTS = listOf(

        listOf(
            Anchor(x = 0.07f, y = 0.2f, size = 1.5f),
            Anchor(x = 0.45f, y = 2.5f, size = 2.5f),
            Anchor(x = 1.0f, y = 0.5f, size = 1.5f, rotation = 15f),
        ),

        listOf(
            Anchor(x = 0.0f, y = 1.2f, size = 1.8f),
            Anchor(x = 0.5f, y = 0.0f, size = 1.5f),
            Anchor(x = 0.975f, y = 1.35f, size = 1.75f),
        ),

        listOf(
            Anchor(x = 0.0f, y = 2.5f, size = 4f),
            Anchor(x = 0.67f, y = 1.2f, size = 1.3f),
            Anchor(x = 1.0f, y = 0.0f, size = 1.3f),
        ),

        listOf(
            Anchor(x = 1.0f, y = 2.5f, size = 4f),
            Anchor(x = 0.45f, y = 0.35f, size = 0.75f, rotation = 35f),
            Anchor(x = 0.1f, y = 0.8f, size = 1.2f),
        ),

        listOf(
            Anchor(x = 0.0f, y = 1.0f, size = 1.3f),
            Anchor(x = 0.4f, y = -0.1f, size = 0.6f),
            Anchor(x = 0.8f, y = 3.0f, size = 3.8f),
        ),
    )


    fun placementsFor(id: String, count: Int, salt: Int = 0): List<Placement> {
        val layoutIndex = (id.hashCode() + salt).mod(PREDEFINED_LAYOUTS.size)
        val random = Random(id.hashCode().toLong() * 31 + salt)
        return placements(PREDEFINED_LAYOUTS[layoutIndex], count, random)
    }

    fun previewLayout(layoutIndex: Int, count: Int = 3): List<Placement> = placements(
        PREDEFINED_LAYOUTS[layoutIndex % PREDEFINED_LAYOUTS.size],
        count,
        Random(layoutIndex.toLong() * 31)
    )

    private fun placements(layout: List<Anchor>, count: Int, random: Random): List<Placement> {
        if (count <= 0) return emptyList()
        return List(count) { i ->
            val anchor = layout[i % layout.size]
            Placement(
                centerXFraction = anchor.x,
                centerYFraction = anchor.y,
                sizeFraction = anchor.size,
                rotationDegrees = anchor.rotation + random.nextFloat() * 30f - 15f,
            )
        }
    }
}