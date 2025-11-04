package com.tumme.ii3510_courseapp_2526

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MusicPlayer : AppCompatActivity() {
    private var isMusicPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Lifecycle", "onCreate: Initialize player, load playlist")

        setContent {
            MusicPlayerUI(
                onPlayPause = { toggleMusic() },
                isPlaying = { isMusicPlaying }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("Lifecycle", "onStart: Prepare resources (e.g., UI animations)")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Lifecycle", "onResume: Resume music if it was playing")
        if (isMusicPlaying) resumeMusic()
    }

    override fun onPause() {
        super.onPause()
        Log.d("Lifecycle", "onPause: Pause music when app partially hidden")
        pauseMusic()
    }

    override fun onStop() {
        super.onStop()
        Log.d("Lifecycle", "onStop: Release heavy resources (e.g., visualizers)")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Lifecycle", "onDestroy: Clean up player, release memory")
    }

    // --- Fake Music Control Methods ---
    private fun toggleMusic() {
        isMusicPlaying = !isMusicPlaying
        if (isMusicPlaying) playMusic() else pauseMusic()
    }

    private fun playMusic() {
        Log.d("Music", "▶️ Playing music")
    }

    private fun resumeMusic() {
        Log.d("Music", "⏯️ Resuming music")
    }

    private fun pauseMusic() {
        Log.d("Music", "⏸️ Pausing music")
    }

   /* override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(
            "Key",
            "The important data that needs to be stored before the activity disappears"
        )
    }*/

   /* override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val importantData = savedInstanceState.getString("Key")
        val textView: TextView = findViewById(R.id.textView15)
        textView.text = importantData
    }*/

    /*override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getString("Key")?.let { data ->
            val textView: TextView = findViewById(R.id.textView15)
            textView.text = data
        }
    }*/



}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerUI(onPlayPause: () -> Unit, isPlaying: () -> Boolean) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("🎵 Simple Music Player") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(if (isPlaying()) "Now Playing..." else "Stopped")

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = onPlayPause) {
                Text(if (isPlaying()) "Pause" else "Play")
            }
        }
    }
}