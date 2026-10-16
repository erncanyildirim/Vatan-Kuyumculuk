package com.suer.vatankuyumculuk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suer.vatankuyumculuk.data.ThemeMode
import com.suer.vatankuyumculuk.ui.calc.CalculatorScreen
import com.suer.vatankuyumculuk.ui.components.BrandMark
import com.suer.vatankuyumculuk.ui.icons.AppIcons
import com.suer.vatankuyumculuk.ui.prices.PricesScreen
import com.suer.vatankuyumculuk.ui.settings.SettingsScreen
import com.suer.vatankuyumculuk.ui.store.StoreScreen
import com.suer.vatankuyumculuk.ui.theme.AppTheme
import com.suer.vatankuyumculuk.ui.theme.VatanTheme
import androidx.compose.foundation.isSystemInDarkTheme

private enum class Destination(val label: String, val icon: ImageVector) {
    PRICES("Fiyatlar", AppIcons.ShowChart),
    CALCULATOR("Hesapla", AppIcons.Calculate),
    STORE("Mağaza", AppIcons.Storefront),
    SETTINGS("Ayarlar", AppIcons.Settings),
}

/** Applies light/dark status and navigation bar icons for the current theme. */
@Composable
internal expect fun SystemBarsEffect(darkTheme: Boolean)

/**
 * [startScreen] opens a given screen at launch, e.g. "CALCULATOR" or "PRICES:COINS" (screen, then price tab).
 * Used to capture store screenshots; normal launches pass null.
 */
@Composable
fun App(startScreen: String? = null) {
    val market = AppGraph.market
    val prefs = AppGraph.preferences
    val theme by prefs.theme.collectAsStateWithLifecycle()
    val state by market.state.collectAsStateWithLifecycle()
    val favorites by prefs.favorites.collectAsStateWithLifecycle()
    val calcProduct by prefs.calcProduct.collectAsStateWithLifecycle()

    // Keep the live connection open only while the app is visible.
    LifecycleStartEffect(market) {
        market.start()
        onStopOrDispose { market.stop() }
    }

    val dark = when (theme) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    SystemBarsEffect(dark)

    VatanTheme(theme) {
        var destination by rememberSaveable {
            mutableStateOf(Destination.entries.find { it.name == startScreen?.substringBefore(':') } ?: Destination.PRICES)
        }
        val share = rememberShareText()
        val c = AppTheme.colors

        val content: @Composable (Modifier) -> Unit = { modifier ->
            when (destination) {
                Destination.PRICES -> PricesScreen(
                    state = state,
                    favorites = favorites,
                    onToggleFavorite = prefs::toggleFavorite,
                    onRefresh = market::refreshNow,
                    initialTab = startScreen?.substringAfter(':', ""),
                    modifier = modifier,
                )
                Destination.CALCULATOR -> CalculatorScreen(
                    state = state,
                    productCode = calcProduct,
                    onProductChange = prefs::setCalcProduct,
                    modifier = modifier,
                )
                Destination.STORE -> StoreScreen(state.store, modifier)
                Destination.SETTINGS -> SettingsScreen(
                    theme = theme,
                    onThemeChange = prefs::setTheme,
                    onShareApp = {
                        share("${state.store.name} ${state.store.city} canlı altın ve döviz fiyatları: ${AppLinks.SITE_URL}")
                    },
                    modifier = modifier,
                )
            }
        }

        BoxWithConstraints(Modifier.fillMaxSize().background(c.background)) {
            if (maxWidth >= 840.dp) {
                Row(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                    NavigationRail(containerColor = c.surface, header = {
                        BrandMark(Modifier.padding(top = 12.dp).height(26.dp))
                        Spacer(Modifier.height(12.dp))
                    }) {
                        Destination.entries.forEach { d ->
                            NavigationRailItem(
                                selected = d == destination,
                                onClick = { destination = d },
                                icon = { Icon(d.icon, contentDescription = null) },
                                label = { Text(d.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = c.onGold,
                                    indicatorColor = c.gold,
                                    selectedTextColor = c.text,
                                    unselectedIconColor = c.textMuted,
                                    unselectedTextColor = c.textMuted,
                                ),
                            )
                        }
                    }
                    content(Modifier.weight(1f))
                }
            } else {
                Scaffold(
                    containerColor = c.background,
                    bottomBar = {
                        NavigationBar(containerColor = c.surface, tonalElevation = 0.dp) {
                            Destination.entries.forEach { d ->
                                NavigationBarItem(
                                    selected = d == destination,
                                    onClick = { destination = d },
                                    icon = { Icon(d.icon, contentDescription = null) },
                                    label = { Text(d.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = c.onGold,
                                        indicatorColor = c.gold,
                                        selectedTextColor = c.text,
                                        unselectedIconColor = c.textMuted,
                                        unselectedTextColor = c.textMuted,
                                    ),
                                )
                            }
                        }
                    },
                ) { padding ->
                    content(
                        Modifier
                            .padding(padding)
                            .consumeWindowInsets(padding)
                            .windowInsetsPadding(WindowInsets.safeDrawing),
                    )
                }
            }
        }
    }
}
