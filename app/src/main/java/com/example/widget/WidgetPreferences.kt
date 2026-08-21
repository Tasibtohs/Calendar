package com.example.widget

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri

object WidgetPreferences {
    private const val PREFS_NAME = "calendar_widget_preferences"
    const val KEY_PHOTO_URI = "widget_custom_photo_uri"
    const val KEY_PRESET_IMAGE = "widget_preset_image" // "nature", "islamic", "header", "none"
    const val KEY_OVERLAY_OPACITY = "widget_overlay_opacity" // 0.0f .. 0.9f
    const val KEY_STYLE = "widget_style" // "Photo", "Glassmorphism", "Emerald", "RoyalBlue", "Amoled"
    const val KEY_SHOW_HIJRI = "widget_show_hijri"
    const val KEY_SHOW_SEASON = "widget_show_season"
    const val KEY_SHOW_EVENTS = "widget_show_events"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getPhotoUri(context: Context): String? {
        return getPrefs(context).getString(KEY_PHOTO_URI, null)
    }

    fun setPhotoUri(context: Context, uri: String?) {
        getPrefs(context).edit().putString(KEY_PHOTO_URI, uri).apply()
        CalendarAppWidget.updateAllWidgets(context)
    }

    fun getPresetImage(context: Context): String {
        return getPrefs(context).getString(KEY_PRESET_IMAGE, "nature") ?: "nature"
    }

    fun setPresetImage(context: Context, preset: String) {
        getPrefs(context).edit()
            .putString(KEY_PRESET_IMAGE, preset)
            .remove(KEY_PHOTO_URI) // Clear custom URI if preset chosen
            .apply()
        CalendarAppWidget.updateAllWidgets(context)
    }

    fun getOverlayOpacity(context: Context): Float {
        return getPrefs(context).getFloat(KEY_OVERLAY_OPACITY, 0.45f)
    }

    fun setOverlayOpacity(context: Context, opacity: Float) {
        getPrefs(context).edit().putFloat(KEY_OVERLAY_OPACITY, opacity).apply()
        CalendarAppWidget.updateAllWidgets(context)
    }

    fun getWidgetStyle(context: Context): String {
        return getPrefs(context).getString(KEY_STYLE, "Photo") ?: "Photo"
    }

    fun setWidgetStyle(context: Context, style: String) {
        getPrefs(context).edit().putString(KEY_STYLE, style).apply()
        CalendarAppWidget.updateAllWidgets(context)
    }

    fun isShowHijri(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SHOW_HIJRI, true)
    }

    fun setShowHijri(context: Context, show: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SHOW_HIJRI, show).apply()
        CalendarAppWidget.updateAllWidgets(context)
    }

    fun isShowSeason(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SHOW_SEASON, true)
    }

    fun setShowSeason(context: Context, show: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SHOW_SEASON, show).apply()
        CalendarAppWidget.updateAllWidgets(context)
    }

    fun isShowEvents(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SHOW_EVENTS, true)
    }

    fun setShowEvents(context: Context, show: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SHOW_EVENTS, show).apply()
        CalendarAppWidget.updateAllWidgets(context)
    }
}
