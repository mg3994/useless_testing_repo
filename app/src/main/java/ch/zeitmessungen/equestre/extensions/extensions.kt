package ch.zeitmessungen.equestre.extensions // or your own package

import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import ch.zeitmessungen.equestre.ui.recording.StartRecordingActivity
//
//@OptIn(UnstableApi::class)
//fun Context.launchRecording(eventId: String) {
//    StartRecordingActivity.start(this, eventId)
//}

@OptIn(UnstableApi::class)
fun Context.launchRecording(eventId: String) {
    startActivity<StartRecordingActivity> {
        putExtra(StartRecordingActivity.EXTRA_EVENT_ID, eventId)
    }
}



//2. Call it from anywhere like this:
//📍 From an Activity:
//kotlin
//Copy
//Edit
//launchRecording("event_123")
//📍 From a Fragment:
//kotlin
//Copy
//Edit
//requireContext().launchRecording("event_123")
//📍 From a View or Adapter:
//If you have view.context:
//
//kotlin
//Copy
//Edit
//view.context.launchRecording("event_123")


inline fun <reified T : AppCompatActivity> Context.startActivity(
    noinline intentBuilder: (Intent.() -> Unit)? = null
) {
    val intent = Intent(this, T::class.java)
    intentBuilder?.let { intent.it() }
    startActivity(intent)
}