// File: SportsEventsActivity.kt
package ch.zeitmessungen.equestre.ui.sports_events

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.zeitmessungen.equestre.R
import ch.zeitmessungen.equestre.extensions.launchRecording
import ch.zeitmessungen.equestre.data.models.ConsumerModel
import ch.zeitmessungen.equestre.adaptors.SportsEventAdapter
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import java.net.URISyntaxException

class SportsEventsActivity : AppCompatActivity() {

    private var socket: Socket? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SportsEventAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView

    private val events = mutableListOf<ConsumerModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sports_events)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.rvSportsEvents)
        progressBar = findViewById(R.id.progressLoading)
        emptyView = findViewById(R.id.tvEmpty)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SportsEventAdapter(events) { consumer ->
            consumer.id?.let { launchRecording(it) }
        }
        recyclerView.adapter = adapter

        showLoading()
        initSocket()
    }

    private fun initSocket() {
        try {
            val options = IO.Options().apply {
                transports = arrayOf("websocket")
                forceNew = true
                reconnection = true
            }

            socket = IO.socket("http://185.48.228.171:21741", options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("Socket", "Connected")
                socket?.emit("subscribe", "consumer")
            }

            socket?.on("events") { args ->
                runOnUiThread {
                    try {
                        val data = args.firstOrNull() ?: return@runOnUiThread showEmpty()
                        val jsonArray = when (data) {
                            is String -> JSONArray(data)
                            is JSONArray -> data
                            else -> return@runOnUiThread showEmpty()
                        }

                        events.clear()
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            events.add(ConsumerModel.fromJson(obj))
                        }

                        if (events.isEmpty()) showEmpty() else showContent()
                        adapter.notifyDataSetChanged()

                    } catch (e: Exception) {
                        Log.e("Socket", "Parsing error", e)
                        showEmpty()
                    }
                }
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) {
                Log.e("Socket", "Connection error: ${it.joinToString()}")
                runOnUiThread { showEmpty() }
            }

            socket?.connect()

        } catch (e: URISyntaxException) {
            Log.e("Socket", "URI error", e)
            showEmpty()
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE
    }

    private fun showEmpty() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
    }

    private fun showContent() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        socket?.disconnect()
        socket?.off()
    }
}
