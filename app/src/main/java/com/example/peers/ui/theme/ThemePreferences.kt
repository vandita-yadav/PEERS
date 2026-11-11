package com.example.peers.ui.theme

import android.content.Context

/** Persists the user's theme choice across launches. */
object ThemePreferences {
    private const val PREFS = "peers_prefs"
    private const val KEY_THEME = "theme_mode"

    fun load(context: Context): PeersThemeMode {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THEME, null)
        return runCatching { PeersThemeMode.valueOf(raw ?: "") }
            .getOrDefault(PeersThemeMode.AestheticLight)
    }

    fun save(context: Context, mode: PeersThemeMode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, mode.name)
            .apply()
    }
}
