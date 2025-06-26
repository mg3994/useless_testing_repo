package ch.zeitmessungen.equestre.ui.recording

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.zeitmessungen.equestre.R
import ch.zeitmessungen.equestre.adaptors.RecordingsAdapter
import ch.zeitmessungen.equestre.data.models.RecordingFile
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext

class ManageRecordingsActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var adapter: RecordingsAdapter
    private val pageSize = 20
    private var currentOffset = 0
    private var isLoading = false
    private var allLoaded = false

    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            loadMore()
        } else {
            Toast.makeText(this, "Permission denied. Can't load recordings.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_recordings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.rv_recordings)
        emptyView = findViewById(R.id.tvEmpty)

        val authority = "$packageName.provider"  // Pass FileProvider authority here
        adapter = RecordingsAdapter(mutableListOf(), authority)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                if (dy > 0 && !isLoading && !allLoaded) {
                    val lm = rv.layoutManager as LinearLayoutManager
                    val totalItemCount = lm.itemCount
                    val lastVisible = lm.findLastVisibleItemPosition()
                    if (lastVisible + 5 >= totalItemCount) {
                        loadMore()
                    }
                }
            }
        })

        checkPermissionsAndLoad()
    }

    private fun checkPermissionsAndLoad() {
        val permission = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> Manifest.permission.READ_MEDIA_VIDEO
            else -> Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadMore()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun loadMore() {
        if (isLoading || allLoaded) return
        isLoading = true
        adapter.showLoadingFooter(true)

        launch {
            val newFiles = withContext(Dispatchers.IO) {
                loadRecordingFiles(currentOffset, pageSize)
            }
            adapter.showLoadingFooter(false)
            if (newFiles.isEmpty()) {
                if (currentOffset == 0) emptyView.visibility = TextView.VISIBLE
                allLoaded = true
            } else {
                emptyView.visibility = TextView.GONE
                adapter.addItems(newFiles)
                currentOffset += newFiles.size
            }
            isLoading = false
        }
    }

    private fun loadRecordingFiles(offset: Int, limit: Int): List<RecordingFile> {
        val recordingsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Equestre")

        if (!recordingsDir.exists() || !recordingsDir.isDirectory) return emptyList()

        val files = recordingsDir.listFiles { file ->
            file.isFile && (file.extension.equals("mp4", true) || file.extension.equals("mkv", true))
        }?.sortedByDescending { it.lastModified() } ?: emptyList()

        return files.drop(offset).take(limit).map {
            RecordingFile(it.absolutePath, it.name, it.lastModified())
        }
    }
}
