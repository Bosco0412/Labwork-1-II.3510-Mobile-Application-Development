package com.tumme.ii3510_courseapp_2526

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.io.IOException
import java.util.Locale // For String.format with Locale
import androidx.core.net.toUri

// --- Constants ---
private const val TAG = "LifecycleMusicActivity" // Logcat Tag
private const val KEY_LIFECYCLE_LOG = "lifecycle_log"
private const val KEY_MEDIA_POSITION = "media_position"
private const val KEY_MEDIA_DURATION = "media_duration"
private const val KEY_IS_PLAYING_BEFORE_SAVE = "is_playing_before_save"
private const val KEY_LOADED_AUDIO_RES_ID = "loaded_audio_res_id"


class LifecycleMusicActivity2 : ComponentActivity() {

    // --- MediaPlayer and State Variables ---
    private var mediaPlayer: MediaPlayer? = null
    private val lifecycleEventsLog = mutableStateListOf<String>() // For displaying lifecycle events in UI

    // State variables for music playback to be saved/restored
    private var currentMediaPositionMs: Int = 0
    private var currentMediaDurationMs: Int = 0
    private var wasPlayingBeforeSave: Boolean = false // Was music actively playing before onSaveInstanceState?
    private var loadedAudioResId: Int = R.raw.music_one

    // --- Activity Lifecycle Methods ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logLifecycleEvent("onCreate()")

        // Restore instance state if available
        if (savedInstanceState != null) {
            logLifecycleEvent("onCreate() - Restoring instance state...")
            savedInstanceState.getStringArrayList(KEY_LIFECYCLE_LOG)?.let {
                lifecycleEventsLog.addAll(it)
            }
            currentMediaPositionMs = savedInstanceState.getInt(KEY_MEDIA_POSITION, 0)
            currentMediaDurationMs = savedInstanceState.getInt(KEY_MEDIA_DURATION, 0)
            wasPlayingBeforeSave = savedInstanceState.getBoolean(KEY_IS_PLAYING_BEFORE_SAVE, false)
            loadedAudioResId = savedInstanceState.getInt(KEY_LOADED_AUDIO_RES_ID, R.raw.music_two)

            logLifecycleEvent(
                "onCreate() - Restored: Pos=$currentMediaPositionMs, Dur=$currentMediaDurationMs, WasPlaying=$wasPlayingBeforeSave, AudioResId=$loadedAudioResId"
            )

            // If media was active (playing or paused with duration),
            // we can re-initialize the MediaPlayer to reflect the state.
            // The actual playback will be triggered by the Composable based on 'wasPlayingBeforeSave'.
            if (currentMediaDurationMs > 0) { // Indicates media was loaded
                initializeMediaPlayerOnly(
                    audioResId = loadedAudioResId,
                    startPositionMs = currentMediaPositionMs,
                    onPrepared = { mp -> /* MediaPlayer is ready, UI will be updated by Composable */ },
                    onStateRestored = { /* Callback for Composable if needed */ }
                )
            }
        } else {
            logLifecycleEvent("onCreate() - No instance state to restore.")
            // Set default audio if no saved state
            loadedAudioResId = R.raw.music_two // Ensure this is your desired default
        }

        setContent {
            MyApplicationLifecycleTheme { // Ensure you have this theme defined
                // Pass restored state to the Composable screen
                MusicPlayerWithLifecycleScreen(
                    lifecycleEvents = lifecycleEventsLog,
                    initialAudioResId = loadedAudioResId,
                    initialPositionMs = currentMediaPositionMs,
                    initialDurationMs = currentMediaDurationMs,
                    initialIsPlaying = wasPlayingBeforeSave,
                    onPlayPauseToggle = ::handlePlayPauseToggle,
                    onStopPlayer = ::handleStopPlayer,
                    onSeek = ::handleSeekPlayer,
                    // Callback to update Activity's state from Composable (if Composable changes audio)
                    // This is a simplified approach. A ViewModel would be better for complex state.
                    onAudioResIdChanged = { newResId ->
                        if (loadedAudioResId != newResId) {
                            logLifecycleEvent("UI Changed audio to ResId: $newResId. Resetting player.")
                            releaseMediaPlayer("Audio Changed by UI")
                            currentMediaPositionMs = 0
                            currentMediaDurationMs = 0
                            wasPlayingBeforeSave = false
                            loadedAudioResId = newResId
                            // The Composable will likely call onPlayPauseToggle next, which will initialize
                        }
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        logLifecycleEvent("onStart()")
        // If MediaPlayer was released in onStop() and not due to configuration change,
        // and we want to resume (e.g., coming back to app), we might re-initialize here.
        // However, with state restoration in onCreate, this might be redundant unless
        // onStop always releases and we want to prepare sooner than onResume.
        // For this example, onCreate's restoration and Composable's LaunchedEffect handle it.
    }

    override fun onResume() {
        super.onResume()
        logLifecycleEvent("onResume()")
        // If music was playing and got paused by onPause(), and you want it to resume
        // automatically when the activity is fully interactive, you could start it here.
        // However, wasPlayingBeforeSave + LaunchedEffect in Composable often handles this better.
    }

    override fun onPause() {
        super.onPause()
        logLifecycleEvent("onPause()")
        // If MediaPlayer is playing, pause it.
        // This is crucial to stop audio when the activity is no longer in the foreground.
        mediaPlayer?.takeIf { it.isPlaying }?.let {
            it.pause()
            // wasPlayingBeforeSave is already true if it was playing.
            // Update current position as it's paused here.
            currentMediaPositionMs = it.currentPosition
            logLifecycleEvent("MediaPlayer: Paused playback in onPause() at $currentMediaPositionMs ms")
        }
        // wasPlayingBeforeSave remains true if it was playing, so onSaveInstanceState saves it.
        // If it was paused by user, wasPlayingBeforeSave would be false (handled in handlePlayPauseToggle).
    }

    override fun onStop() {
        super.onStop()
        logLifecycleEvent("onStop()")
        // If the activity is being stopped and it's NOT due to a configuration change (e.g., rotation),
        // then release the MediaPlayer resources.
        // If it IS a configuration change, onSaveInstanceState has saved the state,
        // and onCreate will restore it, so we don't want to release the player yet
        // (though it's likely paused by onPause already).
        if (!isChangingConfigurations) {
            logLifecycleEvent("MediaPlayer: Releasing in onStop() (not changing configuration)")
            releaseMediaPlayer("onStop()")
        } else {
            logLifecycleEvent("MediaPlayer: onStop() due to configuration change. State saved.")
        }
    }

    override fun onRestart() {
        super.onRestart()
        logLifecycleEvent("onRestart()")
        // Called when an activity that was previously stopped is being started again.
    }

    override fun onDestroy() {
        super.onDestroy()
        logLifecycleEvent("onDestroy()")
        // Final cleanup. Release MediaPlayer resources if they haven't been already.
        // This is a safety net.
        logLifecycleEvent("MediaPlayer: Releasing in onDestroy()")
        releaseMediaPlayer("onDestroy()")
        lifecycleEventsLog.clear() // Clear logs for the next instance
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        logLifecycleEvent("onSaveInstanceState()")

        // Save the lifecycle event log
        outState.putStringArrayList(KEY_LIFECYCLE_LOG, ArrayList(lifecycleEventsLog))

        // Save MediaPlayer state
        // If mediaPlayer is active, get its current state.
        // Otherwise, use the last known state from our variables.
        val positionToSave = mediaPlayer?.currentPosition ?: currentMediaPositionMs
        val durationToSave = mediaPlayer?.duration ?: currentMediaDurationMs
        // wasPlayingBeforeSave should reflect if it *should* be playing upon restoration.
        // If the player is currently playing, wasPlayingBeforeSave should be true.
        // If the user paused it, wasPlayingBeforeSave would be false.
        // If onPause() paused it, wasPlayingBeforeSave (if true before onPause) is preserved.

        outState.putInt(KEY_MEDIA_POSITION, positionToSave)
        outState.putInt(KEY_MEDIA_DURATION, durationToSave)
        outState.putBoolean(KEY_IS_PLAYING_BEFORE_SAVE, wasPlayingBeforeSave)
        outState.putInt(KEY_LOADED_AUDIO_RES_ID, loadedAudioResId)

        logLifecycleEvent(
            "onSaveInstanceState() - Saving: Pos=$positionToSave, Dur=$durationToSave, ShouldPlayOnRestore=$wasPlayingBeforeSave, AudioResId=$loadedAudioResId"
        )
    }

    // --- MediaPlayer Control Logic (called from Composable interactions) ---

    private fun handlePlayPauseToggle(audioResId: Int, currentPositionFromUI: Int) {
        if (mediaPlayer?.isPlaying == true) { // Player is currently playing -> Pause it
            mediaPlayer?.pause()
            wasPlayingBeforeSave = false // User explicitly paused
            currentMediaPositionMs = mediaPlayer?.currentPosition ?: currentPositionFromUI
            logLifecycleEvent("MediaPlayer: Paused by UI at $currentMediaPositionMs ms")
            // Notify Composable of new state (usually via state update in Composable itself)
        } else { // Player is paused, stopped, or not initialized -> Play it
            // If the audio resource has changed, or player not initialized for current audio
            if (mediaPlayer == null || loadedAudioResId != audioResId) {
                releaseMediaPlayer("New Audio Play")
                currentMediaPositionMs = if (loadedAudioResId != audioResId) 0 else currentPositionFromUI // Reset position if new audio
                currentMediaDurationMs = 0
                loadedAudioResId = audioResId
                initializeAndPlayAudio(loadedAudioResId, currentMediaPositionMs)
            } else { // Player exists for this audio, just resume or start
                mediaPlayer?.seekTo(currentPositionFromUI) // Ensure it's at the UI's position
                mediaPlayer?.start()
                currentMediaPositionMs = currentPositionFromUI
            }
            wasPlayingBeforeSave = true // Now it's intended to be playing
            logLifecycleEvent("MediaPlayer: Play/Resume by UI. Audio: $loadedAudioResId, Pos: $currentMediaPositionMs")
        }
    }

    private fun handleStopPlayer() {
        logLifecycleEvent("MediaPlayer: Stopped by UI.")
        releaseMediaPlayer("Stop Button")
        currentMediaPositionMs = 0
        currentMediaDurationMs = 0 // Duration is invalid after stop/release
        wasPlayingBeforeSave = false
        // Notify Composable (usually by it updating its own state)
    }

    private fun handleSeekPlayer(newPositionMs: Int) {
        mediaPlayer?.let {
            if (currentMediaDurationMs > 0) { // Seek only if media is loaded/prepared
                it.seekTo(newPositionMs)
                currentMediaPositionMs = newPositionMs
                logLifecycleEvent("MediaPlayer: Seeked by UI to $newPositionMs ms")
            }
        } ?: run {
            // If player not ready, just store the seek position.
            // It will be used if/when the player is initialized.
            currentMediaPositionMs = newPositionMs
            logLifecycleEvent("MediaPlayer: Seek position $newPositionMs ms stored (player not ready)")
        }
    }


    // --- MediaPlayer Helper Functions ---

    /**
     * Initializes MediaPlayer without starting playback.
     * Useful for restoring state or pre-loading.
     */
    private fun initializeMediaPlayerOnly(
        audioResId: Int,
        startPositionMs: Int,
        onPrepared: (MediaPlayer) -> Unit = {},
        onStateRestored: () -> Unit = {} // Optional callback
    ) {
        if (mediaPlayer != null && loadedAudioResId == audioResId) {
            // Already initialized for this audio, just ensure position and notify
            mediaPlayer?.seekTo(startPositionMs)
            currentMediaPositionMs = startPositionMs
            logLifecycleEvent("MediaPlayer: Already initialized for AudioResId $audioResId. Seeking to $startPositionMs.")
            onPrepared(mediaPlayer!!)
            onStateRestored()
            return
        }

        releaseMediaPlayer("InitOnlyNewAudio") // Release if different audio or re-init
        logLifecycleEvent("MediaPlayer: Initializing (state restoration prep) for AudioResId $audioResId...")
        try {
            val uri = "android.resource://${packageName}/${audioResId}".toUri()
            loadedAudioResId = audioResId // Store the ID of the audio being loaded

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@LifecycleMusicActivity2, uri)
                setOnPreparedListener { mp ->
                    logLifecycleEvent("MediaPlayer: Prepared (state restoration). Duration: ${mp.duration} ms.")
                    currentMediaDurationMs = mp.duration
                    mp.seekTo(startPositionMs)
                    currentMediaPositionMs = startPositionMs
                    onPrepared(mp)
                    onStateRestored()
                }
                setOnCompletionListener {
                    logLifecycleEvent("MediaPlayer: Playback Completed (from initOnly instance).")
                    // Typically, after completion, we'd reset state.
                    currentMediaPositionMs = 0
                    wasPlayingBeforeSave = false // No longer playing
                    // Potentially call releaseMediaPlayer or a method to signal UI to update
                    // For now, it just stops. UI might need a callback to reset.
                    // Let's call a more comprehensive stop handler
                    handleStopPlayer()
                }
                setOnErrorListener { _, what, extra ->
                    logLifecycleEvent("MediaPlayer: Error (what:$what, extra:$extra)")
                    releaseMediaPlayer("onError-InitOnly")
                    true // Error handled
                }
                prepareAsync()
            }
        } catch (e: IOException) {
            logLifecycleEvent("MediaPlayer: IOException during initOnly - ${e.message}")
            releaseMediaPlayer("InitException-InitOnly")
        }
    }

    /**
     * Initializes and starts playing audio.
     */
    private fun initializeAndPlayAudio(audioResId: Int, startPositionMs: Int = 0) {
        // If already initialized for this audio and just needs to start/resume
        if (mediaPlayer != null && loadedAudioResId == audioResId) {
            mediaPlayer?.seekTo(startPositionMs)
            mediaPlayer?.start()
            currentMediaPositionMs = startPositionMs
            wasPlayingBeforeSave = true
            logLifecycleEvent("MediaPlayer: Resuming/Starting existing instance for AudioResId $audioResId at $startPositionMs ms.")
            return
        }

        releaseMediaPlayer("PlayNewAudio") // Ensure clean state before new playback
        logLifecycleEvent("MediaPlayer: Initializing for playback AudioResId $audioResId...")
        try {
            val uri = "android.resource://${packageName}/${audioResId}".toUri()
            loadedAudioResId = audioResId // Store the ID of the audio being loaded

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@LifecycleMusicActivity2, uri)
                setOnPreparedListener { mp ->
                    logLifecycleEvent("MediaPlayer: Prepared. Starting playback from $startPositionMs ms. Duration: ${mp.duration} ms.")
                    currentMediaDurationMs = mp.duration
                    mp.seekTo(startPositionMs)
                    currentMediaPositionMs = startPositionMs
                    mp.start()
                    wasPlayingBeforeSave = true // It's now intended to be playing
                }
                setOnCompletionListener {
                    logLifecycleEvent("MediaPlayer: Playback Completed.")
                    // Reset state after completion
                    handleStopPlayer() // Use the common stop handler
                }
                setOnErrorListener { _, what, extra ->
                    logLifecycleEvent("MediaPlayer: Error (what:$what, extra:$extra)")
                    releaseMediaPlayer("onError-Play")
                    true // Error handled
                }
                prepareAsync()
            }
        } catch (e: IOException) {
            logLifecycleEvent("MediaPlayer: IOException during playback init - ${e.message}")
            releaseMediaPlayer("InitException-Play")
        }
    }

    /**
     * Releases MediaPlayer resources.
     */
    private fun releaseMediaPlayer(caller: String) {
        mediaPlayer?.let {
            // No need to check isPlaying before release, it handles internal states.
            it.release()
            logLifecycleEvent("MediaPlayer: Released (called from $caller)")
        }
        mediaPlayer = null
        // Don't reset wasPlayingBeforeSave here, as it's part of the state to be restored
        // if the activity is just being recreated (e.g., rotation).
        // It's reset by explicit stop actions or when starting new unrelated playback.
        // currentMediaDurationMs is reset by handleStopPlayer or when loading new audio.
    }

    /**
     * Logs a lifecycle event to Logcat and the on-screen list.
     */
    private fun logLifecycleEvent(event: String) {
        Log.d(TAG, event)
        val timestamp = System.currentTimeMillis() % 100000 // Simple timestamp
        lifecycleEventsLog.add(0, "$timestamp: $event") // Add to top of list
        while (lifecycleEventsLog.size > 30) { // Keep log list manageable
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                lifecycleEventsLog.removeLast()
            }else{
                lifecycleEventsLog.removeAt(lifecycleEventsLog.lastIndex)
            }
        }
    }
}

// --- Composable UI ---

@Composable
fun MusicPlayerWithLifecycleScreen(
    lifecycleEvents: List<String>,
    initialAudioResId: Int,
    initialPositionMs: Int,
    initialDurationMs: Int,
    initialIsPlaying: Boolean,
    onPlayPauseToggle: (audioResId: Int, currentPositionMs: Int) -> Unit,
    onStopPlayer: () -> Unit,
    onSeek: (positionMs: Int) -> Unit,
    onAudioResIdChanged: (newResId: Int) -> Unit // Callback if Composable changes audio
) {
    // Internal UI state, initialized from Activity's restored state
    var currentUiPositionMs by remember { mutableIntStateOf(initialPositionMs) }
    var totalUiDurationMs by remember { mutableIntStateOf(initialDurationMs) }
    // This reflects the UI's understanding of playback state (Play/Pause button icon)
    var uiIsPlaying by remember { mutableStateOf(initialIsPlaying) }
    var uiAudioResId by remember { mutableIntStateOf(initialAudioResId) }

    // This effect handles auto-play if 'initialIsPlaying' was true upon restoration.
    // It also tries to sync UI state if initial values suggest media was loaded.
    LaunchedEffect(key1 = initialIsPlaying, key2 = initialAudioResId, key3 = initialDurationMs) {
        Log.d(TAG, "Composable LaunchedEffect: initialIsPlaying=$initialIsPlaying, initialAudioResId=$initialAudioResId, initialPos=$initialPositionMs, initialDur=$initialDurationMs")
        uiAudioResId = initialAudioResId
        currentUiPositionMs = initialPositionMs
        totalUiDurationMs = initialDurationMs
        uiIsPlaying = initialIsPlaying // Set button state

        if (initialIsPlaying) {
            // If state restoration indicated it should be playing, trigger play.
            // The Activity's handlePlayPauseToggle will manage the MediaPlayer.
            onPlayPauseToggle(initialAudioResId, initialPositionMs)
        } else if (initialDurationMs > 0) {
            // If not playing, but duration exists, it means media was loaded (perhaps paused).
            // UI state is already set. No action needed here as player should be prepared by Activity.
        }
        // If initialDurationMs is 0, it means no media was loaded, or it was stopped.
    }

    // This LaunchedEffect is a placeholder for a more robust way to sync MediaPlayer's
    // actual current position to the UI's currentUiPositionMs during playback.
    // In a real app, the Activity/ViewModel would expose a Flow/LiveData of the current position.
    LaunchedEffect(uiIsPlaying, totalUiDurationMs) {
        if (uiIsPlaying && totalUiDurationMs > 0) {
            while (true) {
                // This is a crude polling mechanism. NOT RECOMMENDED for production.
                // In a real app, MediaPlayer would push updates.
                // For this example, we'll assume the Activity's `currentMediaPositionMs` is the source of truth
                // but we don't have a direct way to observe it here without more complex state management.
                // So, the slider updates on interaction, and this is just a placeholder.
                // If you had a way to get currentMediaPlayerPosition from Activity:
                // currentUiPositionMs = activity.getCurrentMediaPlayerPosition()
                delay(1000) // Poll every second (example)
                if (currentUiPositionMs >= totalUiDurationMs) {
                    currentUiPositionMs = totalUiDurationMs
                    uiIsPlaying = false // Crude way to stop UI if it reaches end
                    break
                }
                if (!uiIsPlaying) break // Stop if UI state changes to not playing
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Activity Lifecycle & MediaPlayer",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- Media Controls ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Button(onClick = {
                // Toggle playback state
                val nextPlayingState = !uiIsPlaying
                onPlayPauseToggle(uiAudioResId, currentUiPositionMs)
                uiIsPlaying = nextPlayingState // Optimistically update UI
                // The actual state will be confirmed/corrected by MediaPlayer via Activity logic
            }) {
                Text(if (uiIsPlaying) "Pause" else "Play")
            }

            Button(onClick = {
                onStopPlayer()
                uiIsPlaying = false
                currentUiPositionMs = 0
                totalUiDurationMs = 0 // Duration becomes invalid after stop
            }) {
                Text("Stop")
            }
        }

        // --- Song Selection (Simple Example) ---
        // In a real app, this would be more sophisticated
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            Button(onClick = {
                if (uiAudioResId != R.raw.music_one) { // Replace with your actual resource ID
                    onAudioResIdChanged(R.raw.music_one) // Notify Activity of change
                    // The onPlayPauseToggle or an explicit load will happen next in Activity
                    // For simplicity, we can also trigger a stop and then play for the new song
                    onStopPlayer()
                    uiIsPlaying = false
                    currentUiPositionMs = 0
                    totalUiDurationMs = 0
                    uiAudioResId = R.raw.music_one // Update UI state
                    // To auto-play new song:
                    // onPlayPauseToggle(R.raw.music_one, 0)
                    // uiIsPlaying = true
                }
            }, enabled = true ) { // Enable based on whether it's already selected
                Text("Song 1 (music_one)")
            }
            // Add another button for a different song if you have one in res/raw
            // Button(onClick = { /* load another_song.mp3 */ }, enabled = uiAudioResId != R.raw.another_song) { Text("Song 2") }
        }
        Text("Current Audio: ${if (uiAudioResId == R.raw.music_one) "music_one.mp3" else "Other (ID: $uiAudioResId)"}")


        // --- Progress Bar (Slider) ---
        if (totalUiDurationMs > 0) {
            Slider(
                value = currentUiPositionMs.toFloat().coerceIn(0f, totalUiDurationMs.toFloat()),
                onValueChange = { newPositionFloat ->
                    // Update UI immediately during drag for responsiveness
                    currentUiPositionMs = newPositionFloat.toInt()
                },
                onValueChangeFinished = {
                    // Send final seek position to Activity when user releases slider
                    onSeek(currentUiPositionMs)
                },
                valueRange = 0f..totalUiDurationMs.toFloat().coerceAtLeast(0f), // Ensure range is not negative
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatDurationMs(currentUiPositionMs))
                Text(text = formatDurationMs(totalUiDurationMs))
            }
        } else {
            // Show a placeholder or loading indicator when no duration (media not loaded/stopped)
            LinearProgressIndicator(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = (MaterialTheme.typography.bodyLarge.fontSize.value * 1.5).dp)) // Approx height of Slider text
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Lifecycle Event Log Display ---
        Text(
            text = "Lifecycle Events:",
            style = MaterialTheme.typography.titleMedium
        )
        LazyColumn(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()) {
            items(lifecycleEvents) { event ->
                Text(event, style = MaterialTheme.typography.bodySmall)
                Divider()
            }
        }
    }
}

/**
 * Utility function to format milliseconds to mm:ss string.
 */
fun formatDurationMs(millis: Int): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

// --- Preview ---

@Preview(showBackground = true, widthDp = 380, heightDp = 700)
@Composable
fun DefaultPreviewOfMusicPlayerScreen() {
    MyApplicationLifecycleTheme {
        val sampleEvents = remember {
            mutableStateListOf(
                "1000: onCreate()",
                "1005: onStart()",
                "1010: onResume()"
            )
        }
        var MOCK_INITIAL_AUDIO_RES_ID = 0 // Using 0 as a placeholder for preview
        // In a real app, you might have a way to access R.raw.music_one if possible in preview context
        // For now, we'll keep it simple or use a conditional import if absolutely necessary.
        // This is just for preview, so the exact resource ID doesn't matter as much.


        MusicPlayerWithLifecycleScreen(
            lifecycleEvents = sampleEvents,
            initialAudioResId = MOCK_INITIAL_AUDIO_RES_ID, // Use a placeholder for preview
            initialPositionMs = 30000, // e.g., 30 seconds
            initialDurationMs = 180000, // e.g., 3 minutes
            initialIsPlaying = false,
            onPlayPauseToggle = { _, _ -> Log.d(TAG, "Preview: Play/Pause Toggled") },
            onStopPlayer = { Log.d(TAG, "Preview: Stop Player") },
            onSeek = { pos -> Log.d(TAG, "Preview: Seek to $pos") },
            onAudioResIdChanged = { resId -> Log.d(TAG, "Preview: Audio Res ID changed to $resId")}
        )
    }
}


