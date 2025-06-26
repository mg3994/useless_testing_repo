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

    private const val KEY_SHOW_IS_LIVE = "show_is_live"

    private const val KEY_SHOW_BRAND_LOGO= "show_brand_logo"

    private const val KEY_SHOW_HORSE_NUMBER= "show_horse_number"

    private const val KEY_SHOW_HORSE_NAME= "show_horse_name"

    private const val KEY_SHOW_HORSE_RIDER_NAME= "show_horse_rider_name"




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

    fun getShowIsLive(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_SHOW_IS_LIVE, true)
    fun getShowBrandLogo(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_SHOW_BRAND_LOGO, true)
    fun getShowHorseNumber(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_SHOW_HORSE_NUMBER, true)

    fun getShowHorseName(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_SHOW_HORSE_NAME, true)
    fun getShowHorseRiderName(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_SHOW_HORSE_RIDER_NAME, true)
    fun setShowGapToBest(context: Context, value: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_SHOW_GAP_TO_BEST, value) }
    }

    fun setShowIsLive(context: Context, value: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_SHOW_IS_LIVE, value) }
    }
    fun setShowBrandLogo(context: Context, value: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_SHOW_BRAND_LOGO, value) }
    }
    fun setShowHorseNumber(context: Context, value: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_SHOW_HORSE_NUMBER, value) }
    }
    fun setShowHorseName(context: Context, value: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_SHOW_HORSE_NAME, value) }
    }
    fun setShowHorseRiderName(context: Context, value: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_SHOW_HORSE_RIDER_NAME, value) }
    }
}