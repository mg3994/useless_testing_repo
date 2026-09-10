package ch.zeitmessungen.equestre.plugin

import ch.zeitmessungen.equestre.ui.recording.OverlayDataProvider
import java.util.concurrent.ConcurrentHashMap

/**
 * DartNativeCameraBridge acts as the bridge receiving state updates from Dart / Flutter
 * via MethodChannel or direct calls, storing real-time values, and providing them to
 * the native CameraX / Media3 renderer in [StartRecordingActivity].
 */
class DartNativeCameraBridge private constructor() : OverlayDataProvider {

    private val dataMap = ConcurrentHashMap<String, String>()
    private var isLiveState = false

    fun updateData(newMap: Map<String, Any?>) {
        newMap.forEach { (key, value) ->
            if (value != null) {
                if (key == "isLive" && value is Boolean) {
                    isLiveState = value
                } else {
                    dataMap[key] = value.toString()
                }
            }
        }
    }

    override fun getRiderName(): String {
        return dataMap["riderName"] ?: "Rider Name"
    }

    override fun getHorseName(): String {
        return dataMap["horseName"] ?: "Horse Name"
    }

    override fun getHorseNumber(): String {
        return dataMap["horseNumber"] ?: "#"
    }

    override fun getPenalties(): String {
        return dataMap["penalties"] ?: "Penalties"
    }

    override fun getTimeFormatted(): String {
        return dataMap["time"] ?: "N/A"
    }

    override fun getRank(): String {
        return dataMap["rank"] ?: "Rank"
    }

    override fun getGap(): String {
        return dataMap["gap"] ?: "Gap: N/A"
    }

    override fun isLive(): Boolean {
        return isLiveState
    }

    companion object {
        val instance: DartNativeCameraBridge by lazy { DartNativeCameraBridge() }
    }
}
