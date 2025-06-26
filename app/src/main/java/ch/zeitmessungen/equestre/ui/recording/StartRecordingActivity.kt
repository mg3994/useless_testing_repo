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
import com.google.common.collect.ImmutableList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@UnstableApi
class StartRecordingActivity : AppCompatActivity() {
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private lateinit var button: Button
    //
    private lateinit var eventId: String
    //

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
        //
        // Get the eventId from intent
        eventId = intent.getStringExtra(EXTRA_EVENT_ID)
            ?: throw IllegalArgumentException("Event ID is required")

        // You can now use `eventId` anywhere in the activity
        Log.d("StartRecording", "Received eventId: $eventId")

        // Continue your Camera setup...
        //
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.start_recording)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        viewFinder = findViewById(R.id.viewFinder)
        button = findViewById(R.id.video_capture_button)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        button.setOnClickListener {
            captureVideo()
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
            return
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
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
                    }
                }
            }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.Companion.getInstance(this)
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
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }

    companion object {
        //
        public const val EXTRA_EVENT_ID = "extra_event_id"
// else use only
        fun start(context: Context, eventId: String) {
            val intent = Intent(context, StartRecordingActivity::class.java).apply {
                putExtra(EXTRA_EVENT_ID, eventId)
            }
            context.startActivity(intent)
        }
        //
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
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        fun buildColoredTextOverlay(
            text: String,
            bgColor: Int,
            fgColor: Int = Color.WHITE,
            xAnchor: Float,
            yAnchor: Float,
            textSizePx: Int = 48,
            rotationDegrees: Float = 90f,
            backgroundAnchorX: Float? = null,
            backgroundAnchorY: Float? = null,
            alphaScale: Float = 1f,
            hdrLuminanceMultiplier: Float? = null,
            scaleX: Float? = null,
            scaleY: Float? = null,
            tiltRotationDegrees: Float = 0f,
            xOffset: Float = 0f,
            yOffset: Float = 0f
        ): TextOverlay {
            return object : TextOverlay() {
                override fun getText(presentationTimeUs: Long): SpannableString {
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

        val timestampOverlay = object : TextOverlay() {
            override fun getText(presentationTimeUs: Long): SpannableString {
                val timestampMs = presentationTimeUs / 1000
                val date = Date(timestampMs)
                val formattedTime = dateFormat.format(date)
                return SpannableString(formattedTime).apply {
                    setSpan(ForegroundColorSpan(Color.WHITE), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    setSpan(AbsoluteSizeSpan(50, false), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
                return StaticOverlaySettings.Builder()
                    .setOverlayFrameAnchor(-0.9f, 0.9f)
                    .setRotationDegrees(90f)
                    .setAlphaScale(1f)
                    .build()
            }
        }

        val liveOverlay = object : TextOverlay() {
            private val textSizePx = 60

            override fun getText(presentationTimeUs: Long): SpannableString {
                val blinkVisible = ((presentationTimeUs / 500_000) % 2L == 0L)
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
                    .build()
            }
        }

        val newsNumberOverlay = buildColoredTextOverlay(
            text = "01",
            bgColor = 0xFF1E88E5.toInt(),
            xAnchor = -0.2f,
            yAnchor = -0.5f,
            tiltRotationDegrees = -5f,
            yOffset = 0.02f
        )

        val headlineOverlay = buildColoredTextOverlay(
            text = "Manish Sharma",
            bgColor = 0xFFD81B60.toInt(),
            xAnchor = 1f,
            yAnchor = 0.75f,
            tiltRotationDegrees = 10f, // More tilt
            backgroundAnchorX = 0.75f,
            backgroundAnchorY = 0.7f,
            scaleX = 1.2f, // Stretch slightly
            scaleY = 1f
        )

        val stockPriceOverlay = object : TextOverlay() {
            override fun getText(presentationTimeUs: Long): SpannableString {
                val price = 345.67
                val change = 2.5
                val percentChange = 0.73
                val bgColor = if (change >= 0) 0xFF2E7D32.toInt() else 0xFFC62828.toInt()
                val text = String.Companion.format(Locale.US, "Win By: $%.2f  %.2f%%", price, percentChange)

                return SpannableString(text).apply {
                    setSpan(BackgroundColorSpan(bgColor), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    setSpan(ForegroundColorSpan(Color.WHITE), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    setSpan(AbsoluteSizeSpan(48, false), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
                return StaticOverlaySettings.Builder()
                    .setOverlayFrameAnchor(-0.9f, 0.75f)
                    .setRotationDegrees(90f)
                    .setAlphaScale(1f)
                    .build()
            }
        }

        return OverlayEffect(
            ImmutableList.of(
                timestampOverlay as TextureOverlay,
                liveOverlay as TextureOverlay,
                newsNumberOverlay as TextureOverlay,
                headlineOverlay as TextureOverlay,
                stockPriceOverlay as TextureOverlay
            )
        )
    }




}