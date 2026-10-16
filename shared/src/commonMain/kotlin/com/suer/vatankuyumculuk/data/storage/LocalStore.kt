package com.suer.vatankuyumculuk.data.storage

import com.russhwolf.settings.Settings

/** Small key-value persistence (SharedPreferences / NSUserDefaults / localStorage). */
interface LocalStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String?)
}

class SettingsStore(private val settings: Settings = Settings()) : LocalStore {
    override fun getString(key: String): String? = runCatching { settings.getStringOrNull(key) }.getOrNull()

    override fun putString(key: String, value: String?) {
        runCatching {
            if (value == null) settings.remove(key) else settings.putString(key, value)
        }
    }
}

class MemoryStore : LocalStore {
    private val map = HashMap<String, String>()
    override fun getString(key: String): String? = map[key]
    override fun putString(key: String, value: String?) {
        if (value == null) map.remove(key) else map[key] = value
    }
}

object StoreKeys {
    const val CONFIG_JSON = "config.json"
    const val CONFIG_ETAG = "config.etag"
    const val QUOTES_JSON = "quotes.json"
    const val QUOTES_SAVED_AT = "quotes.savedAt"
    const val FAVORITES = "favorites"
    const val THEME = "theme"
    const val CALC_PRODUCT = "calc.product"
}
