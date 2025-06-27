package ch.zeitmessungen.equestre.adaptors
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import ch.zeitmessungen.equestre.R
import ch.zeitmessungen.equestre.data.models.RecordingFile
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import java.io.File

class RecordingsAdapter(
    private val recordings: MutableList<RecordingFile>,
    private val authority: String,
    private val onRequestManagePermission: (() -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    private var showLoading = false

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvRecordingName)
        val imgThumbnail: ImageView = view.findViewById(R.id.imgThumbnail)
        val btnPlay: ImageButton = view.findViewById(R.id.btnPlay)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    inner class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun getItemViewType(position: Int): Int {
        return if (position < recordings.size) VIEW_TYPE_ITEM else VIEW_TYPE_LOADING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recording, parent, false)
            ViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loading_indicator, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun getItemCount(): Int = recordings.size + if (showLoading) 1 else 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val recording = recordings[position]
            holder.tvName.text = recording.name

            val thumbnailRequest: RequestBuilder<Drawable> = Glide.with(holder.itemView.context)
                .load(recording.path)
                .centerCrop()

            Glide.with(holder.itemView.context)
                .load(recording.path)
                .thumbnail(thumbnailRequest)
                .centerCrop()
                .into(holder.imgThumbnail)

            holder.btnPlay.setOnClickListener {
                val context = holder.itemView.context
                val file = File(recording.path)
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    authority,
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "video/*")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
            }
            holder.imgThumbnail.setOnClickListener {
                val context = holder.itemView.context
                val file = File(recording.path)
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    authority,
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "video/*")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
            }
            holder.tvName.setOnClickListener {
                val context = holder.itemView.context
                val file = File(recording.path)
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    authority,
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "video/*")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
            }

            holder.btnDelete.setOnClickListener { //TODO: File is deleting perfectly for respective video but the position that it is updating , say removing in recycler view that postion is sometimes wrong
                val context = holder.itemView.context
                val file = File(recording.path)
                if (file.exists()) {
                    if (file.delete()) {
                        recordings.removeAt(position)
                        notifyItemRemoved(position)
                    } else {
                        // If delete fails, likely due to missing permission
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                            Toast.makeText(context, "Storage permission required to delete files", Toast.LENGTH_SHORT).show()
                            try {
                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                    data = Uri.parse("package:" + context.packageName)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Unable to open settings.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    fun addItems(newItems: List<RecordingFile>) {
        val start = recordings.size
        recordings.addAll(newItems)
        notifyItemRangeInserted(start, newItems.size)
    }

    fun showLoadingFooter(show: Boolean) {
        if (showLoading != show) {
            showLoading = show
            if (show) notifyItemInserted(recordings.size)
            else notifyItemRemoved(recordings.size)
        }
    }
}
