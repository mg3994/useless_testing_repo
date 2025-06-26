package ch.zeitmessungen.equestre

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button // Import Button
import android.content.Intent
import ch.zeitmessungen.equestre.extensions.startActivity
import ch.zeitmessungen.equestre.ui.overlay_settings.OverlaySettingsActivity
import ch.zeitmessungen.equestre.ui.recording.ManageRecordingsActivity
import ch.zeitmessungen.equestre.ui.sports_events.SportsEventsActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        } // Find your buttons by their IDs from activity_main.xml
        val buttonRecordVideo = findViewById<Button>(R.id.button_sports_events) // Assuming this ID for "Record Video"
        val buttonSettings = findViewById<Button>(R.id.button_settings)
        val buttonManageRecordings = findViewById<Button>(R.id.button_manage_recordings)

        // Set OnClick Listeners
        buttonRecordVideo.setOnClickListener {
            startActivity<SportsEventsActivity>()
        }

        buttonSettings.setOnClickListener {
            startActivity<OverlaySettingsActivity>()
        }

        buttonManageRecordings.setOnClickListener {
            startActivity<ManageRecordingsActivity>()
        }

    }
}

