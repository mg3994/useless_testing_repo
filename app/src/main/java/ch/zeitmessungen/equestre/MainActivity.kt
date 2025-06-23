package ch.zeitmessungen.equestre

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
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
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.OverlaySettings
import androidx.media3.effect.StaticOverlaySettings
import androidx.media3.effect.TextOverlay
import androidx.media3.effect.TextureOverlay
import com.google.common.collect.ImmutableList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors // Import for Executors


@UnstableApi
class MainActivity : AppCompatActivity() {
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService // Initialize in onCreate
    private lateinit var viewFinder: PreviewView
    private lateinit var button: Button

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext, "Permission request Denied", Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cameraExecutor = Executors.newSingleThreadExecutor() // Initialize cameraExecutor
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
            return
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
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
                if (PermissionChecker.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) ==
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
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }
            val recorder = Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val useCaseGroupBuilder = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(videoCapture!!)

            // Create and add the Media3Effect with dynamic TextOverlay
            val media3Effect = Media3Effect(
                application,
                androidx.camera.core.CameraEffect.PREVIEW or androidx.camera.core.CameraEffect.VIDEO_CAPTURE,
                ContextCompat.getMainExecutor(application)
            ) {
                // Error listener for effects
                Log.e(TAG, "Media3Effect error: $it")
                Toast.makeText(applicationContext, "Effect error: ${it.message}", Toast.LENGTH_LONG).show()
            }

            val overlayEffect = createDynamicOverlayEffect()
            overlayEffect?.let {
                val effectsList = ImmutableList.Builder<Effect>().add(it).build()
                media3Effect.setEffects(effectsList)
            }

            useCaseGroupBuilder.addEffect(media3Effect)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, useCaseGroupBuilder.build()
                )
            } catch (exec: Exception) {
                Log.e(TAG, "use case binding failed", exec)
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
        cameraExecutor.shutdown()
    }

    companion object {
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

    // NEW FUNCTION: createDynamicOverlayEffect
    private fun createDynamicOverlayEffect(): OverlayEffect? {
        val overlaySettings = StaticOverlaySettings.Builder()
            .setAnchor(0f, 1f) // Anchor at bottom-left (0, 1)
            .setOffset(-0.02f, -0.02f) // Small padding from bottom-left corner
            .build()

        // Create a custom TextOverlay that provides dynamic text
        val dynamicTextOverlay = object : TextOverlay() {
            private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            private val textPaint = Paint().apply {
                color = Color.WHITE // White text color
                textSize = 50f // Example text size
                setShadowLayer(5f, 0f, 0f, Color.BLACK) // Add a black shadow for readability
            }

            override fun getText(presentationTimeUs: Long): SpannableString {
                // Convert presentationTimeUs (microseconds) to milliseconds
                val timestampMs = presentationTimeUs / 1000
                val date = Date(timestampMs)
                val formattedTime = dateFormat.format(date)

                val spannableString = SpannableString(formattedTime)
                // Apply desired styling
                spannableString.setSpan(
                    ForegroundColorSpan(textPaint.color),
                    0,
                    spannableString.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableString.setSpan(
                    AbsoluteSizeSpan(textPaint.textSize.toInt(), false),
                    0,
                    spannableString.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                return spannableString
            }

            override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
                // You can return different settings based on time if needed,
                // but for a static position, use the pre-defined one.
                return overlaySettings
            }

            // You can override drawText if you need custom Canvas drawing logic
            // @Override
            // public void drawText(Canvas canvas, String text, float x, float y, Paint paint) {
            //     super.drawText(canvas, text, x, y, paint);
            // }
        }

        return OverlayEffect(ImmutableList.of(dynamicTextOverlay))
    }
}