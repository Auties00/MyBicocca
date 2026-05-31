package it.attendance100.mybicocca.ui.screen.registry.state

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

enum class RegistryAccentShape {
    Sunny,
    Cookie6,
    Cookie9,
    Cookie12,
    Clover4,
    Flower,
    Pentagon,
    Diamond,
    Burst,
    SoftBurst,
    Pill,
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun RegistryAccentShape.materialShape(): Shape = when (this) {
    RegistryAccentShape.Sunny -> MaterialShapes.Sunny.toShape()
    RegistryAccentShape.Cookie6 -> MaterialShapes.Cookie6Sided.toShape()
    RegistryAccentShape.Cookie9 -> MaterialShapes.Cookie9Sided.toShape()
    RegistryAccentShape.Cookie12 -> MaterialShapes.Cookie12Sided.toShape()
    RegistryAccentShape.Clover4 -> MaterialShapes.Clover4Leaf.toShape()
    RegistryAccentShape.Flower -> MaterialShapes.Flower.toShape()
    RegistryAccentShape.Pentagon -> MaterialShapes.Pentagon.toShape()
    RegistryAccentShape.Diamond -> MaterialShapes.Diamond.toShape()
    RegistryAccentShape.Burst -> MaterialShapes.Burst.toShape()
    RegistryAccentShape.SoftBurst -> MaterialShapes.SoftBurst.toShape()
    RegistryAccentShape.Pill -> MaterialShapes.Pill.toShape()
}

fun RegistryCategory.glyph(): RegistryAccentShape = when (this) {
    RegistryCategory.Exams -> RegistryAccentShape.Sunny
    RegistryCategory.Taxes -> RegistryAccentShape.Cookie6
    RegistryCategory.Teaching -> RegistryAccentShape.Clover4
    RegistryCategory.Documents -> RegistryAccentShape.Cookie9
    RegistryCategory.Agenda -> RegistryAccentShape.Cookie12
}

data class RegistryCategoryColors(
    val bigContainer: Color,
    val bigOnContainer: Color,
    val accent: Color,
    val onAccent: Color,
)

fun RegistryCategory.colors(scheme: ColorScheme): RegistryCategoryColors = when (this) {
    RegistryCategory.Exams -> RegistryCategoryColors(
        bigContainer = scheme.primary,
        bigOnContainer = scheme.onPrimary,
        accent = scheme.primary,
        onAccent = scheme.onPrimary,
    )
    RegistryCategory.Taxes -> RegistryCategoryColors(
        bigContainer = scheme.secondary,
        bigOnContainer = scheme.onSecondary,
        accent = scheme.secondary,
        onAccent = scheme.onSecondary,
    )
    RegistryCategory.Teaching -> RegistryCategoryColors(
        bigContainer = scheme.tertiary,
        bigOnContainer = scheme.onTertiary,
        accent = scheme.tertiary,
        onAccent = scheme.onTertiary,
    )
    RegistryCategory.Documents -> RegistryCategoryColors(
        bigContainer = scheme.secondary,
        bigOnContainer = scheme.onSecondary,
        accent = scheme.secondary,
        onAccent = scheme.onSecondary,
    )
    RegistryCategory.Agenda -> RegistryCategoryColors(
        bigContainer = scheme.primaryContainer,
        bigOnContainer = scheme.onPrimaryContainer,
        accent = scheme.onPrimaryContainer,
        onAccent = scheme.primaryContainer,
    )
}
