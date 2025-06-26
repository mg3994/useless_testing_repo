package ch.zeitmessungen.equestre.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object OverlaySettingsPreferences {

    private const val PREFS_NAME = "overlay_settings_prefs"
    private const val KEY_SHOW_PENALTIES = "show_penalties"
    private const val KEY_SHOW_TIME = "show_time"
    private const val KEY_SHOW_RANK = "show_rank"
    private const val KEY_SHOW_GAP_TO_BEST = "show_gap_to_best"

    private fun getPrefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getShowPenalties(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_SHOW_PENALTIES, true)

    fun setShowPenalties(context: Context, value: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_SHOW_PENALTIES, value) }
    }

    fun getShowTime(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_SHOW_TIME, true)

    fun setShowTime(context: Context, value: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_SHOW_TIME, value) }
    }

    fun getShowRank(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_SHOW_RANK, true)

    fun setShowRank(context: Context, value: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_SHOW_RANK, value) }
    }

    fun getShowGapToBest(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_SHOW_GAP_TO_BEST, true)

    fun setShowGapToBest(context: Context, value: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_SHOW_GAP_TO_BEST, value) }
    }
}