package com.tumme.ii3510_courseapp_2526

import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.IOException
import androidx.core.net.toUri

private const val TAG = "LifecycleActivity"

class LifecycleMusicActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var lifecycleEventsLog = mutableStateListOf<String>() // Pour afficher les logs dans l'UI
    private var wasPlayingBeforePause = false
    // --- Méthodes du cycle de vie de l'Activity ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logLifecycleEvent("onCreate()")

        // Restauration de l'état si nécessaire (non implémenté en détail ici)
        savedInstanceState?.getStringArrayList("lifecycle_log")?.let {
            lifecycleEventsLog.addAll(it)
        }

        setContent {
            MyApplicationLifecycleTheme {
                LifecycleLoggerScreen(
                    lifecycleEvents = lifecycleEventsLog,
                    onPlay = { initializeAndPlayAudio() },
                    onStopPlayer = { releaseMediaPlayer("UI Button") }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        logLifecycleEvent("onStart()")
        // Initialiser le MediaPlayer ici si vous voulez qu'il soit prêt quand l'activité devient visible
        // Si vous initialisez ici, assurez-vous de le libérer dans onStop()
    }

    override fun onResume() {
        super.onResume()
        logLifecycleEvent("onResume()")
        // Reprendre la lecture si elle était en pause et que l'activité reprend le focus
         if (mediaPlayer != null && !mediaPlayer!!.isPlaying && wasPlayingBeforePause) {
             mediaPlayer?.start()
             logLifecycleEvent("MediaPlayer: Resumed playback")
        }
    }

    override fun onPause() {
        super.onPause()
        logLifecycleEvent("onPause()")
        // Mettre en pause la lecture si l'activité perd le focus mais n'est pas détruite
        // Important: Ne pas libérer le MediaPlayer ici si vous voulez que la musique continue
        // si une autre activité partielle vient au premier plan (ex: dialogue).
        // Si la musique doit s'arrêter quand l'activité n'est plus au premier plan, mettez en pause ici.
        mediaPlayer?.takeIf { it.isPlaying }?.let {
             it.pause()
             wasPlayingBeforePause = true
             logLifecycleEvent("MediaPlayer: Paused playback")
         }
    }

    override fun onStop() {
        super.onStop()
        logLifecycleEvent("onStop()")
        // Libérer le MediaPlayer ici si la musique ne doit pas jouer quand l'activité n'est plus visible.
        // Si vous utilisez un Service pour la lecture en arrière-plan, la logique serait différente.
        // Pour cet exemple, nous le libérons ici pour montrer un nettoyage typique.
        releaseMediaPlayer("onStop()")
    }

    override fun onRestart() {
        super.onRestart()
        logLifecycleEvent("onRestart()")
        // Appelé après onStop() lorsque l'activité est redémarrée (par exemple, retour arrière)
    }

    override fun onDestroy() {
        super.onDestroy()
        logLifecycleEvent("onDestroy()")
        // Libération finale des ressources. MediaPlayer devrait déjà être libéré dans onStop(),
        // mais c'est une bonne pratique de s'assurer ici aussi.
        releaseMediaPlayer("onDestroy()")
        lifecycleEventsLog.clear() // Nettoyer pour la prochaine création
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        logLifecycleEvent("onSaveInstanceState()")
        // Sauvegarder l'état nécessaire, par exemple, la position de lecture actuelle,
        // l'état du lecteur, et notre log d'événements.
        outState.putStringArrayList("lifecycle_log", ArrayList(lifecycleEventsLog))
        mediaPlayer?.currentPosition?.let { outState.putInt("media_position", it) }
        outState.putBoolean("is_playing", mediaPlayer?.isPlaying ?: false)
    }

    private fun initializeAndPlayAudio() {
        if (mediaPlayer == null) {
            logLifecycleEvent("MediaPlayer: Initializing...")
            try {
                val uri = "android.resource://${packageName}/${R.raw.music_one}".toUri()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@LifecycleMusicActivity, uri)
                    setOnPreparedListener {
                        logLifecycleEvent("MediaPlayer: Prepared. Starting playback.")
                        it.start()
                    }
                    setOnCompletionListener {
                        logLifecycleEvent("MediaPlayer: Playback Completed.")
                        releaseMediaPlayer("onCompletion")
                    }
                    setOnErrorListener { _, what, extra ->
                        logLifecycleEvent("MediaPlayer: Error (what:$what, extra:$extra)")
                        releaseMediaPlayer("onError")
                        true
                    }
                    prepareAsync()
                }
            } catch (e: IOException) {
                logLifecycleEvent("MediaPlayer: IOException - ${e.message}")
                releaseMediaPlayer("InitException")
            }
        } else if (!mediaPlayer!!.isPlaying) {
            logLifecycleEvent("MediaPlayer: Already initialized. Starting playback.")
            mediaPlayer?.start()
        }
    }

    private fun releaseMediaPlayer(caller: String) {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            logLifecycleEvent("MediaPlayer: Released (called from $caller)")
        }
        mediaPlayer = null
    }

    private fun logLifecycleEvent(event: String) {
        Log.d(TAG, event)
        // Ajoute au début pour voir les derniers événements en haut
        lifecycleEventsLog.add(0, "${System.currentTimeMillis() % 100000}: $event")
        // Garder une taille limitée pour l'affichage UI
        while (lifecycleEventsLog.size > 20) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                lifecycleEventsLog.removeLast()
            }else{
                lifecycleEventsLog.removeAt(lifecycleEventsLog.lastIndex)
            }
        }
    }
}

@Composable
fun LifecycleLoggerScreen(
    lifecycleEvents: List<String>,
    onPlay: () -> Unit,
    onStopPlayer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cycle de vie de l'Activity & MediaPlayer",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onPlay) {
                Text("Play Musique (Raw)")
            }
            Button(onClick = onStopPlayer) {
                Text("Stop Musique")
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Événements du Cycle de Vie:",
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

@Composable
fun MyApplicationLifecycleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        typography = Typography(),
        content = content
    )
}