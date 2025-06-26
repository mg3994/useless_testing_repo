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

    private lateinit var switchIsLive: SwitchCompat

    private lateinit var switchBrandLogo: SwitchCompat

    private lateinit var switchHorseNumber: SwitchCompat

    private lateinit var switchHorseName: SwitchCompat

    private lateinit var switchHorseRiderName: SwitchCompat

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
//
        switchIsLive  = findViewById(R.id.switch_is_live)
        switchBrandLogo  = findViewById(R.id.switch_brand_logo)
        switchHorseNumber  = findViewById(R.id.switch_horse_number)
        switchHorseName  = findViewById(R.id.switch_horse_name)
        switchHorseRiderName  = findViewById(R.id.switch_horse_rider_name)

        // Load saved prefs
        switchPenalties.isChecked = OverlaySettingsPreferences.getShowPenalties(this)
        switchTime.isChecked = OverlaySettingsPreferences.getShowTime(this)
        switchRank.isChecked = OverlaySettingsPreferences.getShowRank(this)
        switchGapToBest.isChecked = OverlaySettingsPreferences.getShowGapToBest(this)
        switchIsLive.isChecked = OverlaySettingsPreferences.getShowIsLive(this)
        switchBrandLogo.isChecked = OverlaySettingsPreferences.getShowBrandLogo(this)
        switchHorseNumber.isChecked = OverlaySettingsPreferences.getShowHorseNumber(this)
        switchHorseName.isChecked = OverlaySettingsPreferences.getShowHorseName(this)
        switchHorseRiderName.isChecked = OverlaySettingsPreferences.getShowHorseRiderName(this)

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
        switchIsLive.setOnCheckedChangeListener { _, isChecked ->
            OverlaySettingsPreferences.setShowIsLive(this, isChecked)}
        switchBrandLogo.setOnCheckedChangeListener { _, isChecked ->
                OverlaySettingsPreferences.setShowBrandLogo(this, isChecked)}
        switchHorseNumber.setOnCheckedChangeListener { _, isChecked ->
                    OverlaySettingsPreferences.setShowHorseNumber(this, isChecked)}
        switchHorseName.setOnCheckedChangeListener { _, isChecked ->
                        OverlaySettingsPreferences.setShowHorseName(this, isChecked)}
        switchHorseRiderName.setOnCheckedChangeListener { _, isChecked ->
                            OverlaySettingsPreferences.setShowHorseRiderName(this, isChecked)}
    }
}
