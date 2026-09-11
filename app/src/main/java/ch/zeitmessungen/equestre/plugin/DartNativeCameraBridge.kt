package ch.zeitmessungen.equestre.plugin

import androidx.annotation.Keep
import ch.zeitmessungen.equestre.ui.recording.OverlayDataProvider
import java.util.concurrent.ConcurrentHashMap

/**
 * DartNativeCameraBridge acts as the JNI and JNIGen FFI bridge interface for DartNative plugins.
 * It receives real-time state updates directly from Dart via JNIGen FFI generated bindings,
 * storing state in thread-safe containers for native CameraX / Media3 GPU texture overlays in [StartRecordingActivity].
 */
@Keep
class DartNativeCameraBridge private constructor() : OverlayDataProvider {

    private val dataMap = ConcurrentHashMap<String, String>()
    @Volatile private var isLiveState = false
    @Volatile private var isRecordingState = false

    @Keep
    fun updateData(newMap: Map<String, Any?>) {
        newMap.forEach { (key, value) ->
            if (value != null) {
                if (key == "isLive" && value is Boolean) {
                    isLiveState = value
                } else if (key == "isRecording" && value is Boolean) {
                    isRecordingState = value
                } else {
                    dataMap[key] = value.toString()
                }
            }
        }
    }

    @Keep
    fun setRiderName(name: String) {
        dataMap["riderName"] = name
    }

    @Keep
    fun setHorseName(name: String) {
        dataMap["horseName"] = name
    }

    @Keep
    fun setHorseNumber(num: String) {
        dataMap["horseNumber"] = num
    }

    @Keep
    fun setPenalties(penalties: String) {
        dataMap["penalties"] = penalties
    }

    @Keep
    fun setTimeFormatted(time: String) {
        dataMap["time"] = time
    }

    @Keep
    fun setRank(rank: String) {
        dataMap["rank"] = rank
    }

    @Keep
    fun setGap(gap: String) {
        dataMap["gap"] = gap
    }

    @Keep
    fun setLive(isLive: Boolean) {
        isLiveState = isLive
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
        @get:Keep
        val instance: DartNativeCameraBridge by lazy { DartNativeCameraBridge() }

        /**
         * JNIGen / DartNative static direct FFI entrypoint callable from Dart logic.
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
