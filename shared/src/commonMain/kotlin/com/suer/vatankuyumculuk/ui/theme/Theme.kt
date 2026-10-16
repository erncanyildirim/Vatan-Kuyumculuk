package com.suer.vatankuyumculuk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suer.vatankuyumculuk.data.ThemeMode
import com.suer.vatankuyumculuk.resources.Res
import com.suer.vatankuyumculuk.resources.manrope
import org.jetbrains.compose.resources.Font

/** Brand gold and navy of the Vatan Kuyumculuk mark (store-assets/brand/mark.svg). */
val BrandGold = Color(0xFFE9B949)
val BrandNavy = Color(0xFF0B1020)

@Immutable
data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val card: Color,
    val cardHigh: Color,
    val outline: Color,
    val text: Color,
    val textMuted: Color,
    val gold: Color,
    val onGold: Color,
    /** Gold usable for text/icons on the background (contrast ≥ 4.5:1). */
    val goldInk: Color,
    val goldSoft: Color,
    val up: Color,
    val down: Color,
    val upSoft: Color,
    val downSoft: Color,
    val warning: Color,
    val warningSoft: Color,
)

val DarkAppColors = AppColors(
    isDark = true,
    background = BrandNavy,
    surface = Color(0xFF10162A),
    card = Color(0xFF151C33),
    cardHigh = Color(0xFF1D2541),
    outline = Color(0xFF283152),
    text = Color(0xFFF4F1EA),
    textMuted = Color(0xFFA3A9BC),
    gold = BrandGold,
    onGold = BrandNavy,
    goldInk = BrandGold,
    goldSoft = Color(0x26E9B949),
    up = Color(0xFF3DDC97),
    down = Color(0xFFFF6B6B),
    upSoft = Color(0x333DDC97),
    downSoft = Color(0x33FF6B6B),
    warning = Color(0xFFFFB74D),
    warningSoft = Color(0x26FFB74D),
)

val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFF7F5F0),
    surface = Color(0xFFFFFFFF),
    card = Color(0xFFFFFFFF),
    cardHigh = Color(0xFFEFEBE1),
    outline = Color(0xFFE2DCCD),
    text = Color(0xFF121726),
    textMuted = Color(0xFF545B6E),
    gold = BrandGold,
    onGold = BrandNavy,
    goldInk = Color(0xFF7C5A12),
    goldSoft = Color(0x33E9B949),
    up = Color(0xFF047857),
    down = Color(0xFFC62828),
    upSoft = Color(0x1F047857),
    downSoft = Color(0x1FC62828),
    warning = Color(0xFF9A5B00),
    warningSoft = Color(0x26FFB74D),
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

private fun AppColors.toMaterial(): ColorScheme = if (isDark) {
    darkColorScheme(
        primary = gold,
        onPrimary = onGold,
        primaryContainer = goldSoft,
        onPrimaryContainer = gold,
        secondary = gold,
        onSecondary = onGold,
        secondaryContainer = goldSoft,
        onSecondaryContainer = gold,
        background = background,
        onBackground = text,
        surface = surface,
        onSurface = text,
        surfaceVariant = cardHigh,
        onSurfaceVariant = textMuted,
        surfaceContainerLowest = background,
        surfaceContainerLow = surface,
        surfaceContainer = card,
        surfaceContainerHigh = cardHigh,
        surfaceContainerHighest = cardHigh,
        outline = outline,
        outlineVariant = outline,
        error = down,
    )
} else {
    lightColorScheme(
        primary = goldInk,
        onPrimary = Color.White,
        primaryContainer = goldSoft,
        onPrimaryContainer = text,
        secondary = goldInk,
        onSecondary = Color.White,
        secondaryContainer = gold,
        onSecondaryContainer = onGold,
        background = background,
        onBackground = text,
        surface = surface,
        onSurface = text,
        surfaceVariant = cardHigh,
        onSurfaceVariant = textMuted,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = cardHigh,
        surfaceContainerHighest = cardHigh,
        outline = outline,
        outlineVariant = outline,
        error = down,
    )
}

@Composable
private fun manropeFamily(): FontFamily {
    fun w(weight: Int) = FontVariation.Settings(FontVariation.weight(weight))
    return FontFamily(
        Font(Res.font.manrope, FontWeight.Normal, variationSettings = w(400)),
        Font(Res.font.manrope, FontWeight.Medium, variationSettings = w(500)),
        Font(Res.font.manrope, FontWeight.SemiBold, variationSettings = w(600)),
        Font(Res.font.manrope, FontWeight.Bold, variationSettings = w(700)),
        Font(Res.font.manrope, FontWeight.ExtraBold, variationSettings = w(800)),
    )
}

private fun appTypography(family: FontFamily): Typography {
    val base = TextStyle(fontFamily = family)
    return Typography(
        displayLarge = base.copy(fontSize = 44.sp, lineHeight = 50.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
        displayMedium = base.copy(fontSize = 38.sp, lineHeight = 44.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
        displaySmall = base.copy(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.ExtraBold),
        headlineLarge = base.copy(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
        headlineMedium = base.copy(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
        headlineSmall = base.copy(fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.Bold),
        titleLarge = base.copy(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
        titleMedium = base.copy(fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold),
        titleSmall = base.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
        bodyLarge = base.copy(fontSize = 17.sp, lineHeight = 25.sp, fontWeight = FontWeight.Medium),
        bodyMedium = base.copy(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
        bodySmall = base.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
        labelLarge = base.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold),
        labelMedium = base.copy(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold),
        labelSmall = base.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp),
    )
}

/** Tabular figures keep prices from jumping horizontally while they update. */
val TabularNumbers = TextStyle(fontFeatureSettings = "tnum")

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun VatanTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val colors = if (dark) DarkAppColors else LightAppColors
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toMaterial(),
            typography = appTypography(manropeFamily()),
            shapes = AppShapes,
            content = content,
        )
    }
}

object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}
