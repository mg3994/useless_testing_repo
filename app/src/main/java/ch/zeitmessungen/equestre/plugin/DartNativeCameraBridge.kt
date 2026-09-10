package ch.zeitmessungen.equestre.plugin

import androidx.annotation.Keep
import ch.zeitmessungen.equestre.ui.recording.OverlayDataProvider
import java.util.concurrent.ConcurrentHashMap

/**
 * DartNativeCameraBridge acts as the JNI and native interface bridge for DartNative plugins.
 * It receives state updates directly from Dart via DartNative JNI calls or MethodChannels,
 * storing real-time values and feeding them to the native CameraX / Media3 renderer in [StartRecordingActivity].
 */
@Keep
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
        @JvmStatic
        val instance: DartNativeCameraBridge by lazy { DartNativeCameraBridge() }

        /**
         * JNI static direct invocation entrypoint called by DartNative C/JNI bridge
         * to update native overlay state directly from Dart business logic.
         */
        @JvmStatic
        @Keep
        fun nativeUpdateOverlayData(
            riderName: String?,
            horseName: String?,
            horseNumber: String?,
            penalties: String?,
            timeFormatted: String?,
            rank: String?,
            gap: String?,
            isLive: Boolean
        ) {
            val map = mutableMapOf<String, Any?>()
            riderName?.let { map["riderName"] = it }
            horseName?.let { map["horseName"] = it }
            horseNumber?.let { map["horseNumber"] = it }
            penalties?.let { map["penalties"] = it }
            timeFormatted?.let { map["time"] = it }
            rank?.let { map["rank"] = it }
            gap?.let { map["gap"] = it }
            map["isLive"] = isLive
            instance.updateData(map)
        }
    }
}
