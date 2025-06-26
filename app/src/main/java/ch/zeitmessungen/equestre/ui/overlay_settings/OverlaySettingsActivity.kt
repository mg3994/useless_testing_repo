package ch.zeitmessungen.equestre.ui.overlay_settings

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ch.zeitmessungen.equestre.R
import ch.zeitmessungen.equestre.data.OverlaySettingsPreferences

class OverlaySettingsActivity : AppCompatActivity() {

    private lateinit var switchPenalties: SwitchCompat
    private lateinit var switchTime: SwitchCompat
    private lateinit var switchRank: SwitchCompat
    private lateinit var switchGapToBest: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_overlay_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        switchPenalties = findViewById(R.id.switch_penalties)
        switchTime = findViewById(R.id.switch_time)
        switchRank = findViewById(R.id.switch_rank)
        switchGapToBest = findViewById(R.id.switch_gap_to_best)

        // Load saved prefs
        switchPenalties.isChecked = OverlaySettingsPreferences.getShowPenalties(this)
        switchTime.isChecked = OverlaySettingsPreferences.getShowTime(this)
        switchRank.isChecked = OverlaySettingsPreferences.getShowRank(this)
        switchGapToBest.isChecked = OverlaySettingsPreferences.getShowGapToBest(this)

        // Save on toggle change
        switchPenalties.setOnCheckedChangeListener { _, isChecked ->
            OverlaySettingsPreferences.setShowPenalties(this, isChecked)
        }
        switchTime.setOnCheckedChangeListener { _, isChecked ->
            OverlaySettingsPreferences.setShowTime(this, isChecked)
        }
        switchRank.setOnCheckedChangeListener { _, isChecked ->
            OverlaySettingsPreferences.setShowRank(this, isChecked)
        }
        switchGapToBest.setOnCheckedChangeListener { _, isChecked ->
            OverlaySettingsPreferences.setShowGapToBest(this, isChecked)
        }
    }
}
