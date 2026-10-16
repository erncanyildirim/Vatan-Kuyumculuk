package com.suer.vatankuyumculuk.data

import com.suer.vatankuyumculuk.data.market.Catalog
import com.suer.vatankuyumculuk.data.storage.LocalStore
import com.suer.vatankuyumculuk.data.storage.StoreKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { SYSTEM, DARK, LIGHT }

/** Per-device preferences. Nothing here leaves the device. */
class Preferences(private val store: LocalStore) {

    private val _favorites = MutableStateFlow(loadFavorites())
    val favorites: StateFlow<List<String>> = _favorites.asStateFlow()

    private val _theme = MutableStateFlow(
        store.getString(StoreKeys.THEME)?.let { v -> ThemeMode.entries.firstOrNull { it.name == v } } ?: ThemeMode.DARK,
    )
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    private val _calcProduct = MutableStateFlow(store.getString(StoreKeys.CALC_PRODUCT) ?: Catalog.HAS_ALTIN)
    val calcProduct: StateFlow<String> = _calcProduct.asStateFlow()

    fun isFavorite(code: String): Boolean = code in _favorites.value

    fun toggleFavorite(code: String) {
        val current = _favorites.value
        val next = if (code in current) current - code else current + code
        _favorites.value = next
        store.putString(StoreKeys.FAVORITES, next.joinToString(","))
    }

    fun moveFavorite(code: String, up: Boolean) {
        val list = _favorites.value.toMutableList()
        val i = list.indexOf(code)
        val j = if (up) i - 1 else i + 1
        if (i < 0 || j !in list.indices) return
        list[i] = list[j].also { list[j] = list[i] }
        _favorites.value = list
        store.putString(StoreKeys.FAVORITES, list.joinToString(","))
    }

    fun setTheme(mode: ThemeMode) {
        _theme.value = mode
        store.putString(StoreKeys.THEME, mode.name)
    }

    fun setCalcProduct(code: String) {
        _calcProduct.value = code
        store.putString(StoreKeys.CALC_PRODUCT, code)
    }

    private fun loadFavorites(): List<String> {
        val saved = store.getString(StoreKeys.FAVORITES) ?: return Catalog.defaultFavorites
        if (saved.isBlank()) return emptyList()
        return saved.split(',').map { it.trim() }.filter { it.isNotEmpty() && Catalog[it] != null }.distinct()
    }
}
