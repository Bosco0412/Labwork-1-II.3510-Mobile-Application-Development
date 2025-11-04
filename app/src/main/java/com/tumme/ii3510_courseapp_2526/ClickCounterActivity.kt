package com.tumme.ii3510_courseapp_2526

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable // Important for state preservation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TAG = "ClickCounterActivity"

class ClickCounterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate Called. Bundle: $savedInstanceState")
        setContent {
            ClickCounterAppTheme {
                CounterScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart Called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume Called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause Called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop Called")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Note: With rememberSaveable, explicit saving here for the counter is often not needed
        // unless you have other non-Compose state to save from the Activity itself.
        Log.d(TAG, "onSaveInstanceState Called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy Called")
    }
}

@Composable
fun CounterScreen() {
    // Use rememberSaveable to automatically save and restore the counter
    // across configuration changes (like screen rotation) and process death.
    var clickCount by rememberSaveable { mutableIntStateOf(0) }

    Log.d(TAG, "CounterScreen Recomposed. Count: $clickCount")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Click Counter",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Button Clicks: $clickCount",
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(onClick = {
            clickCount++
            Log.d(TAG, "Button Clicked! New Count: $clickCount")
        }) {
            Text("Click Me!")
        }
    }
}

// --- Theme (Placeholder - Define your own or use an existing one) ---
@Composable
fun ClickCounterAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        // Replace with your app's MaterialTheme configuration
        colorScheme = lightColorScheme( // Example color scheme
            primary = MaterialTheme.colorScheme.primary, // Or define your own
            // ... other colors
        ),
        typography = Typography(), // Your app's typography
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreviewOfCounterScreen() {
    ClickCounterAppTheme {
        CounterScreen()
    }
}


