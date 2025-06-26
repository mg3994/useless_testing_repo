////package ch.zeitmessungen.equestre.ui.recording
////
////import android.Manifest
////import android.content.ContentValues
////import android.content.Context
////import android.content.Intent
////import android.content.pm.PackageManager
////import android.graphics.Color
////import android.os.Build
////import android.os.Bundle
////import android.provider.MediaStore
////import android.text.Spannable
////import android.text.SpannableString
////import android.text.style.AbsoluteSizeSpan
////import android.text.style.BackgroundColorSpan
////import android.text.style.ForegroundColorSpan
////import android.util.Log
////import android.widget.Button
////import android.widget.Toast
////import androidx.activity.enableEdgeToEdge
////import androidx.activity.result.contract.ActivityResultContracts
////import androidx.appcompat.app.AppCompatActivity
////import androidx.camera.core.CameraEffect
////import androidx.camera.core.CameraSelector
////import androidx.camera.core.Preview
////import androidx.camera.core.UseCaseGroup
////import androidx.camera.lifecycle.ProcessCameraProvider
////import androidx.camera.media3.effect.Media3Effect
////import androidx.camera.video.MediaStoreOutputOptions
////import androidx.camera.video.Quality
////import androidx.camera.video.QualitySelector
////import androidx.camera.video.Recorder
////import androidx.camera.video.Recording
////import androidx.camera.video.VideoCapture
////import androidx.camera.video.VideoRecordEvent
////import androidx.camera.view.PreviewView
////import androidx.core.content.ContextCompat
////import androidx.core.content.PermissionChecker
////import androidx.core.view.ViewCompat
////import androidx.core.view.WindowInsetsCompat
////import androidx.media3.common.OverlaySettings
////import androidx.media3.common.util.UnstableApi
////import androidx.media3.effect.OverlayEffect
////import androidx.media3.effect.StaticOverlaySettings
////import androidx.media3.effect.TextOverlay
////import androidx.media3.effect.TextureOverlay
////import ch.zeitmessungen.equestre.R
////import ch.zeitmessungen.equestre.data.OverlaySettingsPreferences
////import ch.zeitmessungen.equestre.data.models.HorseModel
////import ch.zeitmessungen.equestre.data.models.InfoModel
////import ch.zeitmessungen.equestre.data.models.RealtimeModel
////import ch.zeitmessungen.equestre.data.models.RiderModel
////import com.google.common.collect.ImmutableList
////import io.socket.client.IO
////import io.socket.client.Socket
////import org.json.JSONObject
////import java.net.URISyntaxException
////import java.text.SimpleDateFormat
////import java.util.Date
////import java.util.Locale
////import java.util.concurrent.ExecutorService
////import java.util.concurrent.Executors
////
////@UnstableApi
////class StartRecordingActivity : AppCompatActivity() {
////    private var socket: Socket? = null
////    private var videoCapture: VideoCapture<Recorder>? = null
////    private var recording: Recording? = null
////    private lateinit var cameraExecutor: ExecutorService
////    private lateinit var viewFinder: PreviewView
////    private lateinit var button: Button
////    //
////    private lateinit var eventId: String
////    //
////    // Models
////    private val eventsInfo = mutableListOf<InfoModel>() //Map
////    private val eventHorses = mutableListOf<HorseModel>() //List
////    private val eventRiders = mutableListOf<RiderModel>() // List
////    private var currentRealtimeData: RealtimeModel? = null //Map
////
////    private var isPenaltiesVisible = false
////    private var isTimeVisible = false
////    private var isRankVisible = false
////    private var isGapToBestVisible = false
////    private var isLiveVisible = false
////    private var isBrandLogoVisible = false
////    private var isHorseNumberVisible = false
////    private var isHorseNameVisible = false
////    private var isHorseRiderNameVisible = false
////
////
////
////
////    private val activityResultLauncher =
////        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
////            val permissionGranted = permissions.entries.all { it.value }
////            if (!permissionGranted) {
////                Toast.makeText(baseContext, "Permission request Denied", Toast.LENGTH_SHORT).show()
////            } else {
////                startCamera()
////            }
////        }
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        enableEdgeToEdge()
////        setContentView(R.layout.activity_start_recording)
////        //
////        // Get the eventId from intent
////        eventId = intent.getStringExtra(EXTRA_EVENT_ID)
////            ?: throw IllegalArgumentException("Event ID is required")
////
////        // You can now use `eventId` anywhere in the activity
////        Log.d("StartRecording", "Received eventId: $eventId")
////        // Preferences initialization after context is ready
////        isPenaltiesVisible = OverlaySettingsPreferences.getShowPenalties(this)
////        isTimeVisible = OverlaySettingsPreferences.getShowTime(this)
////        isRankVisible = OverlaySettingsPreferences.getShowRank(this)
////        isGapToBestVisible = OverlaySettingsPreferences.getShowGapToBest(this)
////        isLiveVisible = OverlaySettingsPreferences.getShowIsLive(this)
////        isBrandLogoVisible = OverlaySettingsPreferences.getShowBrandLogo(this)
////        isHorseNumberVisible = OverlaySettingsPreferences.getShowHorseNumber(this)
////        isHorseNameVisible = OverlaySettingsPreferences.getShowHorseName(this)
////        isHorseRiderNameVisible = OverlaySettingsPreferences.getShowHorseRiderName(this)
////        // Continue your Camera setup...
////        //
////        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.start_recording)) { v, insets ->
////            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
////            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
////            insets
////        }
////
////        cameraExecutor = Executors.newSingleThreadExecutor()
////        viewFinder = findViewById(R.id.viewFinder)
////        button = findViewById(R.id.video_capture_button)
////
////        if (allPermissionsGranted()) {
////            startCamera()
////        } else {
////            requestPermissions()
////        }
////
////        button.setOnClickListener {
////            captureVideo()
////        }
////        initSocket()
////    }
////    private fun initSocket() {
////        val eventId = intent.getStringExtra("extra_event_id") ?: ""
////
////        try {
////            val options = IO.Options().apply {
////                transports = arrayOf("websocket")
////                forceNew = true
////                reconnection = true
////            }
////
////            socket = IO.socket("http://185.48.228.171:21741", options)
////
////            socket?.on(Socket.EVENT_CONNECT) {
////                Log.d("Socket", "Connected")
////                socket?.emit("subscribe", eventId)
////            }
////
////            listOf(
////                "info", "startlist", "competitors", "horses", "riders",
////                "judges", "teams", "ranking", "cc-ranking", "realtime",
////                "resume", "nofifyResume", "final", "ready", "live_info"
////            ).forEach { eventName ->
////                socket?.on(eventName) { args ->
////                    Log.d("Socket", "Received '$eventName': ${args.joinToString()}")
////                    if (eventName == "info" && args.isNotEmpty()) {
////                        val data = args[0]
////                        runOnUiThread {
////                            try {
////                                val jsonObj = when (data) {
////                                    is String -> JSONObject(data)
////                                    is JSONObject -> data
////                                    else -> null
////                                }
////
////                                jsonObj?.let {
////                                    eventsInfo.clear()
////                                    eventsInfo.add(InfoModel.fromJson(it))
//////                                    adapter.notifyDataSetChanged()
////                                    Log.d("InfoModel", "Parsed: $eventsInfo")
////                                }
////                            } catch (e: Exception) {
////                                e.printStackTrace()
////                            }
////                        }
////                    }
////
////                    if (eventName == "realtime" && args.isNotEmpty()) {
////                        val data = args[0]
////                        runOnUiThread {
////                            try {
////                                val jsonObj = when (data) {
////                                    is String -> JSONObject(data)
////                                    is JSONObject -> data
////                                    else -> null
////                                }
////
////                                jsonObj?.let {
////                                    currentRealtimeData = RealtimeModel.fromJson(it)
////                                    Log.d("RealtimeData", "Parsed: $currentRealtimeData")
//////////////////
////                                }
////                            } catch (e: Exception) {
////                                Log.e("Socket", "Error parsing realtime data", e)
////                            }
////                        }
////                    }
////                }
////            }
////
////            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
////                Log.e("Socket", "Connect Error: ${args.joinToString()}")
////            }
////
////            socket?.connect()
////
////        } catch (e: URISyntaxException) {
////            e.printStackTrace()
////        }
////    }
////    private fun captureVideo() {
////        val videoCapture = this.videoCapture ?: return
////        button.isEnabled = false
////
////        val curRecording = recording
////        if (curRecording != null) {
////            curRecording.stop()
////            recording = null
////            Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show()
////            return
////        }
////
////        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
////        val contentValues = ContentValues().apply {
////            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
////            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
////                put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/Equestre")
////            }
////        }
////
////        val mediaStoreOutputOptions = MediaStoreOutputOptions
////            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
////            .setContentValues(contentValues)
////            .build()
////
////        recording = videoCapture.output
////            .prepareRecording(this, mediaStoreOutputOptions)
////            .apply {
////                if (PermissionChecker.checkSelfPermission(this@StartRecordingActivity, Manifest.permission.RECORD_AUDIO) ==
////                    PermissionChecker.PERMISSION_GRANTED
////                ) {
////                    withAudioEnabled()
////                }
////            }.start(ContextCompat.getMainExecutor(this)) { recordEvent ->
////                when (recordEvent) {
////                    is VideoRecordEvent.Start -> {
////                        button.isEnabled = true
////                        Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show()
////                    }
////                    is VideoRecordEvent.Finalize -> {
////                        if (!recordEvent.hasError()) {
////                            val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}"
////                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
////                            Log.d(TAG, msg)
////                        } else {
////                            recording?.close()
////                            recording = null
////                            Log.e(TAG, "Video capture Ends With Error: ${recordEvent.error}")
////                            Toast.makeText(baseContext, "Video capture failed: ${recordEvent.error}", Toast.LENGTH_SHORT).show()
////                        }
////                        button.isEnabled = true
////                    }
////                }
////            }
////    }
////
////    private fun startCamera() {
////        val cameraProviderFuture = ProcessCameraProvider.Companion.getInstance(this)
////        cameraProviderFuture.addListener({
////            val cameraProvider = cameraProviderFuture.get()
////
////            val preview = Preview.Builder().build().also {
////                it.surfaceProvider = viewFinder.surfaceProvider
////            }
////
////            val recorder = Recorder.Builder()
////                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
////                .build()
////            videoCapture = VideoCapture.withOutput(recorder)
////
////            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
////
////            val useCaseGroupBuilder = UseCaseGroup.Builder()
////                .addUseCase(preview)
////                .addUseCase(videoCapture!!)
////
////            val media3Effect = Media3Effect(
////                applicationContext,
////                CameraEffect.PREVIEW or CameraEffect.VIDEO_CAPTURE,
////                ContextCompat.getMainExecutor(applicationContext)
////            ) {
////                Log.e(TAG, "Media3Effect error: ${it.message ?: "Unknown error"}")
////                Toast.makeText(
////                    applicationContext,
////                    "Effect error: ${it.message ?: "Unknown error"}",
////                    Toast.LENGTH_LONG
////                ).show()
////            }
////
////            val overlayEffect = createDynamicOverlayEffect()
////            overlayEffect.let {
////                media3Effect.setEffects(listOf(it))
////            }
////
////            useCaseGroupBuilder.addEffect(media3Effect)
////
////            try {
////                cameraProvider.unbindAll()
////                cameraProvider.bindToLifecycle(
////                    this, cameraSelector, useCaseGroupBuilder.build()
////                )
////            } catch (exec: Exception) {
////                Log.e(TAG, "Use case binding failed", exec)
////            }
////        }, ContextCompat.getMainExecutor(this))
////    }
////
////    private fun requestPermissions() {
////        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
////    }
////
////    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
////        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
////    }
////
////    override fun onDestroy() {
////        super.onDestroy()
////        if (::cameraExecutor.isInitialized) {
////            cameraExecutor.shutdown()
////        }
////        socket?.disconnect()
////        socket?.off()
////    }
////
////    companion object {
////        //
////        public const val EXTRA_EVENT_ID = "extra_event_id"
////// else use only
////        fun start(context: Context, eventId: String) {
////            val intent = Intent(context, StartRecordingActivity::class.java).apply {
////                putExtra(EXTRA_EVENT_ID, eventId)
////            }
////            context.startActivity(intent)
////        }
////        //
////        private const val TAG = "CameraXApp"
////        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
////        private val REQUIRED_PERMISSIONS =
////            mutableListOf(
////                Manifest.permission.CAMERA,
////                Manifest.permission.RECORD_AUDIO
////            ).apply {
////                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
////                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
////                }
////            }.toTypedArray()
////    }
////
////    private fun createDynamicOverlayEffect(): OverlayEffect {
////        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
////
////        fun buildColoredTextOverlay(
////            text: String,
////            bgColor: Int,
////            fgColor: Int = Color.WHITE,
////            xAnchor: Float,
////            yAnchor: Float,
////            textSizePx: Int = 48,
////            rotationDegrees: Float = 0f,
////            backgroundAnchorX: Float? = null,
////            backgroundAnchorY: Float? = null,
////            alphaScale: Float = 1f,
////            hdrLuminanceMultiplier: Float? = null,
////            scaleX: Float? = null,
////            scaleY: Float? = null,
////            tiltRotationDegrees: Float = 0f,
////            xOffset: Float = 0f,
////            yOffset: Float = 0f
////        ): TextOverlay {
////            return object : TextOverlay() {
////                override fun getText(presentationTimeUs: Long): SpannableString {
////                    val spannable = SpannableString(text)
////                    spannable.setSpan(
////                        BackgroundColorSpan(bgColor),
////                        0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
////                    )
////                    spannable.setSpan(
////                        ForegroundColorSpan(fgColor),
////                        0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
////                    )
////                    spannable.setSpan(
////                        AbsoluteSizeSpan(textSizePx, false),
////                        0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
////                    )
////                    return spannable
////                }
////
////                override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
////                    val builder = StaticOverlaySettings.Builder()
////                        .setOverlayFrameAnchor(xAnchor + xOffset, yAnchor + yOffset)
////                        .setRotationDegrees(rotationDegrees + tiltRotationDegrees)
////                        .setAlphaScale(alphaScale)
////
////                    if (backgroundAnchorX != null && backgroundAnchorY != null) {
////                        builder.setBackgroundFrameAnchor(backgroundAnchorX, backgroundAnchorY)
////                    }
////                    if (hdrLuminanceMultiplier != null) {
////                        builder.setHdrLuminanceMultiplier(hdrLuminanceMultiplier)
////                    }
////                    if (scaleX != null && scaleY != null) {
////                        builder.setScale(scaleX, scaleY)
////                    }
////
////                    return builder.build()
////                }
////            }
////        }
////
////        val timestampOverlay = object : TextOverlay() { // get this from socket io  where we have RealtimeModel and there we have time stamp
////            override fun getText(presentationTimeUs: Long): SpannableString {
////                val timestampMs = presentationTimeUs / 1000 // Not this timestamp
////                val date = Date(timestampMs)
////                val formattedTime = dateFormat.format(date)
////                return SpannableString(formattedTime).apply {
////                    setSpan(ForegroundColorSpan(Color.WHITE), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
////                    setSpan(AbsoluteSizeSpan(50, false), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
////                }
////            }
////
////            override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
////                return StaticOverlaySettings.Builder()
////                    .setOverlayFrameAnchor(-0.9f, 0.9f)
////                    .setRotationDegrees(90f)
////                    .setAlphaScale(1f)
////                    .build()
////            }
////        }
////
////        val liveOverlay = object : TextOverlay() { // in InfoModel there is live bool , if this and show  isLiveVisible true show then only
////            private val textSizePx = 60
////
////            override fun getText(presentationTimeUs: Long): SpannableString {
////                val blinkVisible = ((presentationTimeUs / 500_000) % 2L == 0L) // Keep it blinking
////                val liveText = if (blinkVisible) "\u2B24 LIVE" else " "
////
////                return SpannableString(liveText).apply {
////                    if (blinkVisible) {
////                        setSpan(ForegroundColorSpan(Color.RED), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
////                        setSpan(
////                            ForegroundColorSpan(Color.WHITE), 2,
////                            length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
////                        setSpan(
////                            BackgroundColorSpan(Color.RED), 0,
////                            length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
////                        setSpan(
////                            AbsoluteSizeSpan(textSizePx, false), 0,
////                            length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
////                    } else {
////                        setSpan(
////                            ForegroundColorSpan(Color.TRANSPARENT), 0,
////                            length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
////                    }
////                }
////            }
////
////            override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
////                return StaticOverlaySettings.Builder()
////                    .setOverlayFrameAnchor(0.88f, -0.6f)
////                    .setAlphaScale(1f)
////                    .build()
////            }
////        }
////
////        val horseNumberOverlay = buildColoredTextOverlay(
////            text = "01",  // this is also dynamic
////            bgColor = 0xFF1E88E5.toInt(),
////            xAnchor = -0.2f,
////            yAnchor = -0.5f,
////            tiltRotationDegrees = -5f,
////            yOffset = 0.02f
////        )
////
////        val riderNameOverlay = buildColoredTextOverlay(
////            text = "Manish Sharma", // I want it from socket as it came from RiderModel
////            bgColor = 0xFFD81B60.toInt(),
////            xAnchor = 1f,
////            yAnchor = 0.75f,
////            tiltRotationDegrees = 10f, // More tilt
////            backgroundAnchorX = 0.75f,
////            backgroundAnchorY = 0.7f,
////            scaleX = 1.2f, // Stretch slightly
////            scaleY = 1f
////        )
////
//////        val rankOverlay = object : TextOverlay() {
//////            override fun getText(presentationTimeUs: Long): SpannableString {
//////                val price = 345.67
//////                val change = 2.5
//////                val percentChange = 0.73
//////                val bgColor = if (change >= 0) 0xFF2E7D32.toInt() else 0xFFC62828.toInt()
//////                val text = String.Companion.format(Locale.US, "Win By: $%.2f  %.2f%%", price, percentChange)
//////
//////                return SpannableString(text).apply {
//////                    setSpan(BackgroundColorSpan(bgColor), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//////                    setSpan(ForegroundColorSpan(Color.WHITE), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//////                    setSpan(AbsoluteSizeSpan(48, false), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//////                }
//////            }
//////
//////            override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
//////                return StaticOverlaySettings.Builder()
//////                    .setOverlayFrameAnchor(-0.9f, 0.75f)
//////                    .setRotationDegrees(90f)
//////                    .setAlphaScale(1f)
//////                    .build()
//////            }
//////        }
////
////        return OverlayEffect(
////            ImmutableList.of(
////                timestampOverlay as TextureOverlay,
////                liveOverlay as TextureOverlay,
////                horseNumberOverlay as TextureOverlay,
////                riderNameOverlay as TextureOverlay,
//////                rankOverlay as TextureOverlay
////            )
////        )
////    }
////
////
////
////
////}
//
//
//////////////////////////////////
//package ch.zeitmessungen.equestre.ui.recording
//
//import android.Manifest
//import android.content.ContentValues
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.Color
//import android.os.Build
//import android.os.Bundle
//import android.provider.MediaStore
//import android.text.Spannable
//import android.text.SpannableString
//import android.text.style.AbsoluteSizeSpan
//import android.text.style.BackgroundColorSpan
//import android.text.style.ForegroundColorSpan
//import android.util.Log
//import android.widget.Button
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.core.CameraEffect
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.Preview
//import androidx.camera.core.UseCaseGroup
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.media3.effect.Media3Effect
//import androidx.camera.video.MediaStoreOutputOptions
//import androidx.camera.video.Quality
//import androidx.camera.video.QualitySelector
//import androidx.camera.video.Recorder
//import androidx.camera.video.Recording
//import androidx.camera.video.VideoCapture
//import androidx.camera.video.VideoRecordEvent
//import androidx.camera.view.PreviewView
//import androidx.core.content.ContextCompat
//import androidx.core.content.PermissionChecker
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.media3.common.OverlaySettings
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.effect.OverlayEffect
//import androidx.media3.effect.StaticOverlaySettings
//import androidx.media3.effect.TextOverlay
//import androidx.media3.effect.TextureOverlay
//import ch.zeitmessungen.equestre.R
//import ch.zeitmessungen.equestre.data.OverlaySettingsPreferences
//// Import the provided data models
//import ch.zeitmessungen.equestre.data.models.HorseModel
//import ch.zeitmessungen.equestre.data.models.InfoModel
//import ch.zeitmessungen.equestre.data.models.RealtimeModel
//import ch.zeitmessungen.equestre.data.models.RiderModel
//import com.google.common.collect.ImmutableList
//import io.socket.client.IO
//import io.socket.client.Socket
//import org.json.JSONArray
//import org.json.JSONObject
//import java.net.URISyntaxException
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//import java.util.concurrent.TimeUnit // For converting milliseconds to formatted time
//
//@UnstableApi
//class StartRecordingActivity : AppCompatActivity() {
//    private var socket: Socket? = null
//    private var videoCapture: VideoCapture<Recorder>? = null
//    private var recording: Recording? = null
//    private lateinit var cameraExecutor: ExecutorService
//    private lateinit var viewFinder: PreviewView
//    private lateinit var button: Button
//
//    // Event ID
//    private lateinit var eventId: String
//
//    // Models (using `volatile` for thread safety as they are accessed from different threads)
//    @Volatile private var eventsInfo = mutableListOf<InfoModel>()
//    @Volatile private var eventHorses = mutableListOf<HorseModel>()
//    @Volatile private var eventRiders = mutableListOf<RiderModel>()
//    @Volatile private var currentRealtimeData: RealtimeModel? = null
//
//    // Overlay visibility preferences
//    private var isPenaltiesVisible = false
//    private var isTimeVisible = false
//    private var isRankVisible = false
//    private var isGapToBestVisible = false
//    private var isLiveVisible = false
//    private var isBrandLogoVisible = false // Assuming brand logo is an image, not text
//    private var isHorseNumberVisible = false
//    private var isHorseNameVisible = false
//    private var isHorseRiderNameVisible = false
//
//    private val activityResultLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//            val permissionGranted = permissions.entries.all { it.value }
//            if (!permissionGranted) {
//                Toast.makeText(baseContext, "Permission request Denied", Toast.LENGTH_SHORT).show()
//            } else {
//                startCamera()
//            }
//        }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_start_recording)
//
//        // Get the eventId from intent
//        eventId = intent.getStringExtra(EXTRA_EVENT_ID)
//            ?: throw IllegalArgumentException("Event ID is required")
//
//        Log.d("StartRecording", "Received eventId: $eventId")
//
//        // Preferences initialization after context is ready
//        isPenaltiesVisible = OverlaySettingsPreferences.getShowPenalties(this)
//        isTimeVisible = OverlaySettingsPreferences.getShowTime(this)
//        isRankVisible = OverlaySettingsPreferences.getShowRank(this)
//        isGapToBestVisible = OverlaySettingsPreferences.getShowGapToBest(this)
//        isLiveVisible = OverlaySettingsPreferences.getShowIsLive(this)
//        isBrandLogoVisible = OverlaySettingsPreferences.getShowBrandLogo(this)
//        isHorseNumberVisible = OverlaySettingsPreferences.getShowHorseNumber(this)
//        isHorseNameVisible = OverlaySettingsPreferences.getShowHorseName(this)
//        isHorseRiderNameVisible = OverlaySettingsPreferences.getShowHorseRiderName(this)
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.start_recording)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        cameraExecutor = Executors.newSingleThreadExecutor()
//        viewFinder = findViewById(R.id.viewFinder)
//        button = findViewById(R.id.video_capture_button)
//
//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            requestPermissions()
//        }
//
//        button.setOnClickListener {
//            captureVideo()
//        }
//        initSocket()
//    }
//
//    private fun initSocket() {
//        val eventId = intent.getStringExtra("extra_event_id") ?: ""
//
//        try {
//            val options = IO.Options().apply {
//                transports = arrayOf("websocket")
//                forceNew = true
//                reconnection = true
//            }
//
//            socket = IO.socket("http://185.48.228.171:21741", options)
//
//            socket?.on(Socket.EVENT_CONNECT) {
//                Log.d("Socket", "Connected")
//                socket?.emit("subscribe", eventId)
//            }
//
//            listOf(
//                "info", "startlist", "competitors", "horses", "riders",
//                "judges", "teams", "ranking", "cc-ranking", "realtime",
//                "resume", "nofifyResume", "final", "ready", "live_info"
//            ).forEach { eventName ->
//                socket?.on(eventName) { args ->
//                    Log.d("Socket", "Received '$eventName': ${args.joinToString()}")
//                    val data = args.firstOrNull()
//
//                    runOnUiThread {
//                        try {
//                            when (eventName) {
//                                "info" -> {
//                                    val jsonObj = data as? JSONObject
//                                    jsonObj?.let {
//                                        eventsInfo.clear()
//                                        eventsInfo.add(InfoModel.fromJson(it))
//                                        Log.d("InfoModel", "Parsed: $eventsInfo")
//                                    }
//                                }
//                                "horses" -> {
//                                    val jsonArray = data as? JSONArray
//                                    jsonArray?.let {
//                                        eventHorses.clear()
//                                        eventHorses.addAll(HorseModel.fromJsonArray(it))
//                                        Log.d("HorseModel", "Parsed: $eventHorses")
//                                    }
//                                }
//                                "riders" -> {
//                                    val jsonArray = data as? JSONArray
//                                    jsonArray?.let {
//                                        eventRiders.clear()
//                                        eventRiders.addAll(RiderModel.fromJsonArray(it))
//                                        Log.d("RiderModel", "Parsed: $eventRiders")
//                                    }
//                                }
//                                "realtime" -> {
//                                    val jsonObj = data as? JSONObject
//                                    jsonObj?.let {
//                                        currentRealtimeData = RealtimeModel.fromJson(it)
//                                        Log.d("RealtimeData", "Parsed: $currentRealtimeData")
//                                    }
//                                }
//                            }
//                        } catch (e: Exception) {
//                            Log.e("Socket", "Error parsing '$eventName' data", e)
//                        }
//                    }
//                }
//            }
//
//            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
//                Log.e("Socket", "Connect Error: ${args.joinToString()}")
//            }
//
//            socket?.connect()
//
//        } catch (e: URISyntaxException) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun captureVideo() {
//        val videoCapture = this.videoCapture ?: return
//        button.isEnabled = false
//
//        val curRecording = recording
//        if (curRecording != null) {
//            curRecording.stop()
//            recording = null
//            Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/Equestre")
//            }
//        }
//
//        val mediaStoreOutputOptions = MediaStoreOutputOptions
//            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
//            .setContentValues(contentValues)
//            .build()
//
//        recording = videoCapture.output
//            .prepareRecording(this, mediaStoreOutputOptions)
//            .apply {
//                if (PermissionChecker.checkSelfPermission(this@StartRecordingActivity, Manifest.permission.RECORD_AUDIO) ==
//                    PackageManager.PERMISSION_GRANTED
//                ) {
//                    withAudioEnabled()
//                }
//            }.start(ContextCompat.getMainExecutor(this)) { recordEvent ->
//                when (recordEvent) {
//                    is VideoRecordEvent.Start -> {
//                        button.isEnabled = true
//                        Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show()
//                    }
//                    is VideoRecordEvent.Finalize -> {
//                        if (!recordEvent.hasError()) {
//                            val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}"
//                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                            Log.d(TAG, msg)
//                        } else {
//                            recording?.close()
//                            recording = null
//                            Log.e(TAG, "Video capture Ends With Error: ${recordEvent.error}")
//                            Toast.makeText(baseContext, "Video capture failed: ${recordEvent.error}", Toast.LENGTH_SHORT).show()
//                        }
//                        button.isEnabled = true
//                    }
//                }
//            }
//    }
//
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener({
//            val cameraProvider = cameraProviderFuture.get()
//
//            val preview = Preview.Builder().build().also {
//                it.surfaceProvider = viewFinder.surfaceProvider
//            }
//
//            val recorder = Recorder.Builder()
//                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
//                .build()
//            videoCapture = VideoCapture.withOutput(recorder)
//
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            val useCaseGroupBuilder = UseCaseGroup.Builder()
//                .addUseCase(preview)
//                .addUseCase(videoCapture!!)
//
//            val media3Effect = Media3Effect(
//                applicationContext,
//                CameraEffect.PREVIEW or CameraEffect.VIDEO_CAPTURE,
//                ContextCompat.getMainExecutor(applicationContext)
//            ) {
//                Log.e(TAG, "Media3Effect error: ${it.message ?: "Unknown error"}")
//                Toast.makeText(
//                    applicationContext,
//                    "Effect error: ${it.message ?: "Unknown error"}",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//
//            val overlayEffect = createDynamicOverlayEffect()
//            overlayEffect.let {
//                media3Effect.setEffects(listOf(it))
//            }
//
//            useCaseGroupBuilder.addEffect(media3Effect)
//
//            try {
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(
//                    this, cameraSelector, useCaseGroupBuilder.build()
//                )
//            } catch (exec: Exception) {
//                Log.e(TAG, "Use case binding failed", exec)
//            }
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//    private fun requestPermissions() {
//        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
//    }
//
//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        if (::cameraExecutor.isInitialized) {
//            cameraExecutor.shutdown()
//        }
//        socket?.disconnect()
//        socket?.off()
//    }
//
//    companion object {
//        public const val EXTRA_EVENT_ID = "extra_event_id"
//
//        fun start(context: Context, eventId: String) {
//            val intent = Intent(context, StartRecordingActivity::class.java).apply {
//                putExtra(EXTRA_EVENT_ID, eventId)
//            }
//            context.startActivity(intent)
//        }
//
//        private const val TAG = "CameraXApp"
//        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
//        private val REQUIRED_PERMISSIONS =
//            mutableListOf(
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO
//            ).apply {
//                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
//                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                }
//            }.toTypedArray()
//    }
//
//    private fun createDynamicOverlayEffect(): OverlayEffect {
//        // Date format for displaying timestamp
//        val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
//
//        // Helper function to format milliseconds to HH:MM:SS.mmm
//        fun formatMilliseconds(milliseconds: Long?): String {
//            if (milliseconds == null) return "N/A"
//            val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
//            val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
//            val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
//            val millis = milliseconds % 1000
//            return String.format(Locale.US, "%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
//        }
//
//        // Helper function to build a colored text overlay with dynamic content and visibility
//        fun buildColoredTextOverlay(
//            textProvider: () -> String, // Lambda to get dynamic text
//            bgColor: Int,
//            fgColor: Int = Color.WHITE,
//            xAnchor: Float,
//            yAnchor: Float,
//            textSizePx: Int = 48,
//            rotationDegrees: Float = 0f,
//            backgroundAnchorX: Float? = null,
//            backgroundAnchorY: Float? = null,
//            alphaScale: Float = 1f,
//            hdrLuminanceMultiplier: Float? = null,
//            scaleX: Float? = null,
//            scaleY: Float? = null,
//            tiltRotationDegrees: Float = 0f,
//            xOffset: Float = 0f,
//            yOffset: Float = 0f,
//            isVisibleProvider: () -> Boolean = { true } // Lambda to control visibility
//        ): TextOverlay {
//            return object : TextOverlay() {
//                override fun getText(presentationTimeUs: Long): SpannableString {
//                    if (!isVisibleProvider()) {
//                        return SpannableString(" ") // Return a single space if not visible
//                    }
//                    val text = textProvider()
//                    val spannable = SpannableString(text)
//                    spannable.setSpan(
//                        BackgroundColorSpan(bgColor),
//                        0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                    )
//                    spannable.setSpan(
//                        ForegroundColorSpan(fgColor),
//                        0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                    )
//                    spannable.setSpan(
//                        AbsoluteSizeSpan(textSizePx, false),
//                        0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                    )
//                    return spannable
//                }
//
//                override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
//                    val builder = StaticOverlaySettings.Builder()
//                        .setOverlayFrameAnchor(xAnchor + xOffset, yAnchor + yOffset)
//                        .setRotationDegrees(rotationDegrees + tiltRotationDegrees)
//                        .setAlphaScale(alphaScale)
//
//                    if (backgroundAnchorX != null && backgroundAnchorY != null) {
//                        builder.setBackgroundFrameAnchor(backgroundAnchorX, backgroundAnchorY)
//                    }
//                    if (hdrLuminanceMultiplier != null) {
//                        builder.setHdrLuminanceMultiplier(hdrLuminanceMultiplier)
//                    }
//                    if (scaleX != null && scaleY != null) {
//                        builder.setScale(scaleX, scaleY)
//                    }
//
//                    return builder.build()
//                }
//            }
//        }
//
//        val overlays = mutableListOf<TextureOverlay>()
//
//        // 1. Timestamp Overlay (from RealtimeModel's startTime)
//        val timestampOverlay = object : TextOverlay() {
//            override fun getText(presentationTimeUs: Long): SpannableString {
//                // Use startTime from currentRealtimeData, or fallback to current system time
//                val timestampMs = currentRealtimeData?.startTime ?: System.currentTimeMillis()
//                val date = Date(timestampMs)
//                val formattedTime = dateFormat.format(date)
//                return SpannableString(formattedTime).apply {
//                    setSpan(ForegroundColorSpan(Color.WHITE), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                    setSpan(AbsoluteSizeSpan(50, false), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                }
//            }
//
//            override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
//                return StaticOverlaySettings.Builder()
//                    .setOverlayFrameAnchor(-0.9f, 0.9f)
//                    .setRotationDegrees(90f)
//                    .setAlphaScale(1f)
//                    .build()
//            }
//        }
//        overlays.add(timestampOverlay)
//
//
//        // 2. Live Overlay (from InfoModel and isLiveVisible)
//        val liveOverlay = object : TextOverlay() {
//            private val textSizePx = 60
//
//            override fun getText(presentationTimeUs: Long): SpannableString {
//                val isLiveFromInfo = eventsInfo.firstOrNull()?.live ?: false
//                if (!isLiveVisible || !isLiveFromInfo) {
//                    return SpannableString(" ") // Return a single space if not visible
//                }
//
//                val blinkVisible = ((presentationTimeUs / 500_000) % 2L == 0L) // Keep it blinking
//                val liveText = if (blinkVisible) "\u2B24 LIVE" else " "
//
//                return SpannableString(liveText).apply {
//                    if (blinkVisible) {
//                        setSpan(ForegroundColorSpan(Color.RED), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                        setSpan(
//                            ForegroundColorSpan(Color.WHITE), 2,
//                            length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                        setSpan(
//                            BackgroundColorSpan(Color.RED), 0,
//                            length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                        setSpan(
//                            AbsoluteSizeSpan(textSizePx, false), 0,
//                            length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                    } else {
//                        setSpan(
//                            ForegroundColorSpan(Color.TRANSPARENT), 0,
//                            length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                    }
//                }
//            }
//
//            override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
//                return StaticOverlaySettings.Builder()
//                    .setOverlayFrameAnchor(0.88f, -0.6f)
//                    .setAlphaScale(1f)
//                    .build()
//            }
//        }
//        overlays.add(liveOverlay)
//
//
//        // 3. Horse Number Overlay (from currentRealtimeData.num)
//        val horseNumberOverlay = buildColoredTextOverlay(
//            textProvider = { currentRealtimeData?.num?.toString() ?: "N/A" },
//            bgColor = 0xFF1E88E5.toInt(),
//            xAnchor = -0.2f,
//            yAnchor = -0.5f,
//            tiltRotationDegrees = -5f,
//            yOffset = 0.02f,
//            isVisibleProvider = { isHorseNumberVisible }
//        )
//        overlays.add(horseNumberOverlay)
//
//        // 4. Rider Name Overlay (from currentRealtimeData.num matching RiderModel.idx)
//        val riderNameOverlay = buildColoredTextOverlay(
//            textProvider = {
//                val competitorIdx = currentRealtimeData?.num
//                val rider = eventRiders.find { it.idx == competitorIdx }
//                if (rider != null) {
//                    "${rider.firstName ?: ""} ${rider.lastName ?: ""}".trim()
//                } else {
//                    "Rider Name"
//                }
//            },
//            bgColor = 0xFFD81B60.toInt(),
//            xAnchor = 1f,
//            yAnchor = 0.75f,
//            tiltRotationDegrees = 10f, // More tilt
//            backgroundAnchorX = 0.75f,
//            backgroundAnchorY = 0.7f,
//            scaleX = 1.2f, // Stretch slightly
//            scaleY = 1f,
//            isVisibleProvider = { isHorseRiderNameVisible }
//        )
//        overlays.add(riderNameOverlay)
//
//        // 5. Horse Name Overlay (from currentRealtimeData.num matching HorseModel.idx)
//        val horseNameOverlay = buildColoredTextOverlay(
//            textProvider = {
//                val competitorIdx = currentRealtimeData?.num
//                val horse = eventHorses.find { it.idx == competitorIdx }
//                horse?.name ?: "Horse Name"
//            },
//            bgColor = Color.parseColor("#4CAF50"), // Green background
//            fgColor = Color.WHITE,
//            xAnchor = -0.2f,
//            yAnchor = -0.7f,
//            textSizePx = 40,
//            tiltRotationDegrees = 5f,
//            yOffset = 0.02f,
//            isVisibleProvider = { isHorseNameVisible }
//        )
//        overlays.add(horseNameOverlay)
//
//        // 6. Penalties Overlay (from currentRealtimeData.score.lane1.point)
//        val penaltiesOverlay = buildColoredTextOverlay(
//            textProvider = { "Penalties: ${currentRealtimeData?.score?.lane1?.point?.toString() ?: "N/A"}" },
//            bgColor = Color.parseColor("#FF9800"), // Orange background
//            fgColor = Color.WHITE,
//            xAnchor = -0.9f,
//            yAnchor = 0.75f, // Adjust position
//            textSizePx = 45,
//            rotationDegrees = 90f,
//            isVisibleProvider = { isPenaltiesVisible }
//        )
//        overlays.add(penaltiesOverlay)
//
//        // 7. Time Overlay (from currentRealtimeData.score.lane1.time)
//        val timeOverlay = buildColoredTextOverlay(
//            textProvider = { "Time: ${formatMilliseconds(currentRealtimeData?.score?.lane1?.time)}" },
//            bgColor = Color.parseColor("#2196F3"), // Blue background
//            fgColor = Color.WHITE,
//            xAnchor = -0.9f,
//            yAnchor = 0.55f, // Adjust position
//            textSizePx = 45,
//            rotationDegrees = 90f,
//            isVisibleProvider = { isTimeVisible }
//        )
//        overlays.add(timeOverlay)
//
//        // 8. Rank Overlay (Not available in new RealtimeModel)
//        val rankOverlay = buildColoredTextOverlay(
//            textProvider = { "Rank: N/A" }, // Field not present in new RealtimeModel
//            bgColor = Color.parseColor("#9C27B0"), // Purple background
//            fgColor = Color.WHITE,
//            xAnchor = -0.9f,
//            yAnchor = 0.35f, // Adjust position
//            textSizePx = 45,
//            rotationDegrees = 90f,
//            isVisibleProvider = { isRankVisible }
//        )
//        overlays.add(rankOverlay)
//
//        // 9. Gap to Best Overlay (Not available in new RealtimeModel)
//        val gapToBestOverlay = buildColoredTextOverlay(
//            textProvider = { "Gap: N/A" }, // Field not present in new RealtimeModel
//            bgColor = Color.parseColor("#607D8B"), // Grey background
//            fgColor = Color.WHITE,
//            xAnchor = -0.9f,
//            yAnchor = 0.15f, // Adjust position
//            textSizePx = 45,
//            rotationDegrees = 90f,
//            isVisibleProvider = { isGapToBestVisible }
//        )
//        overlays.add(gapToBestOverlay)
//
//        return OverlayEffect(ImmutableList.copyOf(overlays))
//    }
//}
/////////////////////////
package ch.zeitmessungen.equestre.ui.recording

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraEffect
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.media3.effect.Media3Effect
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.OverlaySettings
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.StaticOverlaySettings
import androidx.media3.effect.TextOverlay
import androidx.media3.effect.TextureOverlay
import ch.zeitmessungen.equestre.R
import android.view.WindowManager // Import for WindowManager
import ch.zeitmessungen.equestre.data.OverlaySettingsPreferences
// Import the provided data models
import ch.zeitmessungen.equestre.data.models.HorseModel
import ch.zeitmessungen.equestre.data.models.InfoModel
import ch.zeitmessungen.equestre.data.models.RealtimeModel
import ch.zeitmessungen.equestre.data.models.RiderModel
import com.google.common.collect.ImmutableList
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit // For converting milliseconds to formatted time

@UnstableApi
class StartRecordingActivity : AppCompatActivity() {
    private var socket: Socket? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private lateinit var button: Button

    // Event ID
    private lateinit var eventId: String

    // Models (using `volatile` for thread safety as they are accessed from different threads)
    @Volatile private var eventsInfo = mutableListOf<InfoModel>()
    @Volatile private var eventHorses = mutableListOf<HorseModel>()
    @Volatile private var eventRiders = mutableListOf<RiderModel>()
    @Volatile private var currentRealtimeData: RealtimeModel? = null

    // Overlay visibility preferences
    private var isPenaltiesVisible = false
    private var isTimeVisible = false
    private var isRankVisible = false
    private var isGapToBestVisible = false
    private var isLiveVisible = false
    private var isBrandLogoVisible = false // Assuming brand logo is an image, not text
    private var isHorseNumberVisible = false
    private var isHorseNameVisible = false
    private var isHorseRiderNameVisible = false

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val permissionGranted = permissions.entries.all { it.value }
            if (!permissionGranted) {
                Toast.makeText(baseContext, "Permission request Denied", Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_recording)
        // Keep the screen on for this activity
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Get the eventId from intent
        eventId = intent.getStringExtra(EXTRA_EVENT_ID)
            ?: throw IllegalArgumentException("Event ID is required")

        Log.d("StartRecording", "Received eventId: $eventId")

        // Preferences initialization after context is ready
        isPenaltiesVisible = OverlaySettingsPreferences.getShowPenalties(this)
        isTimeVisible = OverlaySettingsPreferences.getShowTime(this)
        isRankVisible = OverlaySettingsPreferences.getShowRank(this)
        isGapToBestVisible = OverlaySettingsPreferences.getShowGapToBest(this)
        isLiveVisible = OverlaySettingsPreferences.getShowIsLive(this)
        isBrandLogoVisible = OverlaySettingsPreferences.getShowBrandLogo(this)
        isHorseNumberVisible = OverlaySettingsPreferences.getShowHorseNumber(this)
        isHorseNameVisible = OverlaySettingsPreferences.getShowHorseName(this)
        isHorseRiderNameVisible = OverlaySettingsPreferences.getShowHorseRiderName(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.start_recording)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        viewFinder = findViewById(R.id.viewFinder)
        button = findViewById(R.id.video_capture_button)
        // Set initial button state (idle)
        button.setBackgroundResource(R.drawable.capture_button_idle)
        button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_stop_white, 0, 0) // Set initial icon
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        button.setOnClickListener {
            captureVideo()
        }
        initSocket()
    }

    private fun initSocket() {
        val eventId = intent.getStringExtra("extra_event_id") ?: ""

        try {
            val options = IO.Options().apply {
                transports = arrayOf("websocket")
                forceNew = true
                reconnection = true
            }

            socket = IO.socket("http://185.48.228.171:21741", options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("Socket", "Connected")
                socket?.emit("subscribe", eventId)
            }

            listOf(
                "info", "startlist", "competitors", "horses", "riders",
                "judges", "teams", "ranking", "cc-ranking", "realtime",
                "resume", "nofifyResume", "final", "ready", "live_info"
            ).forEach { eventName ->
                socket?.on(eventName) { args ->
                    Log.d("Socket", "Received '$eventName': ${args.joinToString()}")
                    val data = args.firstOrNull()

                    runOnUiThread {
                        try {
                            when (eventName) {
                                "info" -> {
                                    val jsonObj = data as? JSONObject
                                    jsonObj?.let {
                                        eventsInfo.clear()
                                        eventsInfo.add(InfoModel.fromJson(it))
                                        Log.d("InfoModel", "Parsed: $eventsInfo")
                                    }
                                }
                                "horses" -> {
                                    val jsonArray = data as? JSONArray
                                    jsonArray?.let {
                                        eventHorses.clear()
                                        eventHorses.addAll(HorseModel.fromJsonArray(it))
                                        Log.d("HorseModel", "Parsed: $eventHorses")
                                    }
                                }
                                "riders" -> {
                                    val jsonArray = data as? JSONArray
                                    jsonArray?.let {
                                        eventRiders.clear()
                                        eventRiders.addAll(RiderModel.fromJsonArray(it))
                                        Log.d("RiderModel", "Parsed: $eventRiders")
                                    }
                                }
                                "realtime" -> {
                                    val jsonObj = data as? JSONObject
                                    jsonObj?.let {
                                        currentRealtimeData = RealtimeModel.fromJson(it)
                                        Log.d("RealtimeData", "Parsed: $currentRealtimeData")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Socket", "Error parsing '$eventName' data", e)
                        }
                    }
                }
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e("Socket", "Connect Error: ${args.joinToString()}")
            }

            socket?.connect()

        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return
        button.isEnabled = false

        val curRecording = recording
        if (curRecording != null) {
            curRecording.stop()
            recording = null
            Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show()
            button.setBackgroundResource(R.drawable.capture_button_idle)
            button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_stop_white, 0, 0) // Set idle icon
            return
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/Equestre")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(this@StartRecordingActivity, Manifest.permission.RECORD_AUDIO) ==
                    PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }.start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        button.isEnabled = true
                        Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show()
                        button.setBackgroundResource(R.drawable.capture_button_recording)
                        button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0) // Remove icon for recording state
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture Ends With Error: ${recordEvent.error}")
                            Toast.makeText(baseContext, "Video capture failed: ${recordEvent.error}", Toast.LENGTH_SHORT).show()
                        }
                        button.isEnabled = true
                        // Set button back to idle state after finalization
                        button.setBackgroundResource(R.drawable.capture_button_idle)
                        button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_stop_white, 0, 0) // Set idle icon
                    }
                }
            }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = viewFinder.surfaceProvider
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val useCaseGroupBuilder = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(videoCapture!!)

            val media3Effect = Media3Effect(
                applicationContext,
                CameraEffect.PREVIEW or CameraEffect.VIDEO_CAPTURE,
                ContextCompat.getMainExecutor(applicationContext)
            ) {
                Log.e(TAG, "Media3Effect error: ${it.message ?: "Unknown error"}")
                Toast.makeText(
                    applicationContext,
                    "Effect error: ${it.message ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }

            val overlayEffect = createDynamicOverlayEffect()
            overlayEffect.let {
                media3Effect.setEffects(listOf(it))
            }

            useCaseGroupBuilder.addEffect(media3Effect)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, useCaseGroupBuilder.build()
                )
            } catch (exec: Exception) {
                Log.e(TAG, "Use case binding failed", exec)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear the screen on flag when the activity is destroyed
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
        socket?.disconnect()
        socket?.off()
    }

    companion object {
        public const val EXTRA_EVENT_ID = "extra_event_id"

        fun start(context: Context, eventId: String) {
            val intent = Intent(context, StartRecordingActivity::class.java).apply {
                putExtra(EXTRA_EVENT_ID, eventId)
            }
            context.startActivity(intent)
        }

        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private fun createDynamicOverlayEffect(): OverlayEffect {
        // Date format for displaying timestamp
        val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

        // Helper function to format milliseconds to HH:MM:SS.mmm
        fun formatMilliseconds(milliseconds: Long?): String {
            if (milliseconds == null) return "N/A"
            val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
            val millis = milliseconds % 1000
            return String.format(Locale.US, "%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
        }

        // Helper function to build a colored text overlay with dynamic content and visibility
        fun buildColoredTextOverlay(
            textProvider: () -> String, // Lambda to get dynamic text
            bgColor: Int,
            fgColor: Int = Color.WHITE,
            xAnchor: Float,
            yAnchor: Float,
            textSizePx: Int = 48,
            rotationDegrees: Float = 0f,
            backgroundAnchorX: Float? = null,
            backgroundAnchorY: Float? = null,
            alphaScale: Float = 1f,
            hdrLuminanceMultiplier: Float? = null,
            scaleX: Float? = null,
            scaleY: Float? = null,
            tiltRotationDegrees: Float = 0f,
            xOffset: Float = 0f,
            yOffset: Float = 0f,
            isVisibleProvider: () -> Boolean = { true } // Lambda to control visibility
        ): TextOverlay {
            return object : TextOverlay() {
                override fun getText(presentationTimeUs: Long): SpannableString {
                    if (!isVisibleProvider()) {
                        return SpannableString(" ") // Return a single space if not visible
                    }
                    val text = textProvider()
                    val spannable = SpannableString(text)
                    spannable.setSpan(
                        BackgroundColorSpan(bgColor),
                        0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        ForegroundColorSpan(fgColor),
                        0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        AbsoluteSizeSpan(textSizePx, false),
                        0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    return spannable
                }

                override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
                    val builder = StaticOverlaySettings.Builder()
                        .setOverlayFrameAnchor(xAnchor + xOffset, yAnchor + yOffset)
                        .setRotationDegrees(rotationDegrees + tiltRotationDegrees)
                        .setAlphaScale(alphaScale)

                    if (backgroundAnchorX != null && backgroundAnchorY != null) {
                        builder.setBackgroundFrameAnchor(backgroundAnchorX, backgroundAnchorY)
                    }
                    if (hdrLuminanceMultiplier != null) {
                        builder.setHdrLuminanceMultiplier(hdrLuminanceMultiplier)
                    }
                    if (scaleX != null && scaleY != null) {
                        builder.setScale(scaleX, scaleY)
                    }

                    return builder.build()
                }
            }
        }

        val overlays = mutableListOf<TextureOverlay>()

        // 1. Timestamp Overlay (from RealtimeModel's startTime)
//        val timestampOverlay = object : TextOverlay() {
//            override fun getText(presentationTimeUs: Long): SpannableString {
//                // Use startTime from currentRealtimeData, or fallback to current system time
//                val timestampMs = currentRealtimeData?.startTime ?: System.currentTimeMillis()
//                val date = Date(timestampMs)
//                val formattedTime = dateFormat.format(date)
//                return SpannableString(formattedTime).apply {
//                    setSpan(ForegroundColorSpan(Color.WHITE), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                    setSpan(AbsoluteSizeSpan(50, false), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                }
//            }
//
//            override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
//                return StaticOverlaySettings.Builder()
//                    .setOverlayFrameAnchor(-0.9f, 0.9f)
//                    .setRotationDegrees(90f)
//                    .setAlphaScale(1f)
//                    .build()
//            }
//        }
//        overlays.add(timestampOverlay)


        // 2. Live Overlay (from InfoModel and isLiveVisible)
        val liveOverlay = object : TextOverlay() {
            private val textSizePx = 60

            override fun getText(presentationTimeUs: Long): SpannableString {
                val isLiveFromInfo = eventsInfo.firstOrNull()?.live ?: false
                if (!isLiveVisible || !isLiveFromInfo) {
                    return SpannableString(" ") // Return a single space if not visible
                }

                val blinkVisible = ((presentationTimeUs / 500_000) % 2L == 0L) // Keep it blinking
                val liveText = if (blinkVisible) "\u2B24 LIVE" else " "

                return SpannableString(liveText).apply {
                    if (blinkVisible) {
                        setSpan(ForegroundColorSpan(Color.RED), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        setSpan(
                            ForegroundColorSpan(Color.WHITE), 2,
                            length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        setSpan(
                            BackgroundColorSpan(Color.RED), 0,
                            length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        setSpan(
                            AbsoluteSizeSpan(textSizePx, false), 0,
                            length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    } else {
                        setSpan(
                            ForegroundColorSpan(Color.TRANSPARENT), 0,
                            length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }

            override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
                return StaticOverlaySettings.Builder()
                    .setOverlayFrameAnchor(0.88f, -0.6f)
                    .setAlphaScale(1f)
                    .setBackgroundFrameAnchor(0.9f,0.6f)
                    .build()
            }
        }
        overlays.add(liveOverlay)


        // 3. Horse Number Overlay (from currentRealtimeData.num)
        val horseNumberOverlay = buildColoredTextOverlay(
            textProvider = { currentRealtimeData?.num?.toString() ?: "N/A" },
            bgColor = 0xFF1E88E5.toInt(),
            xAnchor = -0.2f,
            yAnchor = -0.5f,
            textSizePx = 110,backgroundAnchorX = -0.87f,
            backgroundAnchorY = -0.42f,

            yOffset = 0.02f,
            isVisibleProvider = { isHorseNumberVisible }
        )
        overlays.add(horseNumberOverlay)

        // 4. Rider Name Overlay (from currentRealtimeData.num matching RiderModel.idx)
        val riderNameOverlay = buildColoredTextOverlay(
            textProvider = {
                val competitorIdx = currentRealtimeData?.num
                val rider = eventRiders.find { it.idx == competitorIdx }
                if (rider != null) {
                    "${rider.firstName ?: ""} ${rider.lastName ?: ""}".trim()
                } else {
                    "Rider Name"
                }
            },
            bgColor = 0xFF2E7D32.toInt(),
            xAnchor = -0.95f, yAnchor = 0.75f,textSizePx = 80,backgroundAnchorX = -0.77f,
            backgroundAnchorY = -0.45f,

            yOffset = 0.02f,


            scaleX = 1.2f, // Stretch slightly
            scaleY = 1f,
            isVisibleProvider = { isHorseRiderNameVisible }
        )
        overlays.add(riderNameOverlay)

        // 5. Horse Name Overlay (from currentRealtimeData.num matching HorseModel.idx)
        val horseNameOverlay = buildColoredTextOverlay(
            textProvider = {
                val competitorIdx = currentRealtimeData?.num
                val horse = eventHorses.find { it.idx == competitorIdx }
                horse?.name ?: "Horse Name"
            },
            bgColor = Color.parseColor("#4CAF50"), // Green background


            fgColor = 0xFF808080.toInt(), xAnchor = -0.95f, yAnchor = 0.75f,textSizePx = 80,backgroundAnchorX = -0.87f,
            backgroundAnchorY = -0.55f,

            yOffset = 0.02f,

            isVisibleProvider = { isHorseNameVisible }
        )
        overlays.add(horseNameOverlay)

        // 6. Penalties Overlay (from currentRealtimeData.score.lane1.point)
        val penaltiesOverlay = buildColoredTextOverlay(
            textProvider = { "Penalties: ${currentRealtimeData?.score?.lane1?.point?.toString() ?: "N/A"}" },
            bgColor = Color.parseColor("#FF9800"), // Orange background
            fgColor = Color.WHITE,
           xAnchor = 0.95f, yAnchor = 0.70f,textSizePx = 70,backgroundAnchorX = 0.58f,
            backgroundAnchorY = -0.42f,
            isVisibleProvider = { isPenaltiesVisible }
        )
        overlays.add(penaltiesOverlay)

        // 7. Time Overlay (from currentRealtimeData.score.lane1.time)
        val timeOverlay = buildColoredTextOverlay(
            textProvider = { "Time: ${formatMilliseconds(currentRealtimeData?.score?.lane1?.time)}" },
            bgColor = Color.parseColor("#2196F3"), // Blue background
            fgColor = Color.WHITE,
            xAnchor = 0.95f, yAnchor = 0.80f,textSizePx = 70,backgroundAnchorX = 0.77f,
            backgroundAnchorY = -0.52f,
            isVisibleProvider = { isTimeVisible }
        )
        overlays.add(timeOverlay)

        // 8. Rank Overlay (Not available in new RealtimeModel)
        val rankOverlay = buildColoredTextOverlay(
            textProvider = { "Rank: N/A" }, // Field not present in new RealtimeModel
            bgColor = Color.parseColor("#9C27B0"), // Purple background
            fgColor = Color.WHITE,
            xAnchor = 0.95f, yAnchor = 0.90f,textSizePx = 70,backgroundAnchorX = 0.8f,
            backgroundAnchorY = -0.59f,

            isVisibleProvider = { isRankVisible }
        )
        overlays.add(rankOverlay)

        // 9. Gap to Best Overlay (Not available in new RealtimeModel)
        val gapToBestOverlay = buildColoredTextOverlay(
            textProvider = { "Gap: N/A" }, // Field not present in new RealtimeModel
           bgColor =  0xFF2E7D32.toInt(), xAnchor = 0.95f, yAnchor = 0.60f,textSizePx = 70,backgroundAnchorX = 0.77f,
            backgroundAnchorY = -0.45f,
            fgColor = Color.WHITE,

            isVisibleProvider = { isGapToBestVisible }
        )
        overlays.add(gapToBestOverlay)

        // 10. Brand Logo Overlay
        val brandLogoOverlay = buildColoredTextOverlay(
            textProvider = { "© Equestre™" }, // Placeholder for your brand name
            bgColor = Color.TRANSPARENT, // Transparent background
            fgColor = Color.YELLOW, // Yellow text for visibility
            xAnchor = -0.95f,
            yAnchor = -0.95f,
            textSizePx = 55,
            backgroundAnchorX = -0.9f,
            backgroundAnchorY = 0.6f,

            isVisibleProvider = { isBrandLogoVisible }
        )
        overlays.add(brandLogoOverlay)

        return OverlayEffect(ImmutableList.copyOf(overlays))
    }
}
