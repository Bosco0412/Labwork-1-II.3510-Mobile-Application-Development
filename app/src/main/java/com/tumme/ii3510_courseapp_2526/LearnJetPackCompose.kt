package com.tumme.ii3510_courseapp_2526

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class LearnJetPackCompose : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //Greeting("Good morning")
            //Counter()
            Timer()
        }
    }
}

@Composable
fun Timer() {
    var time by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while(true) {
            delay(1000)
            time++
        }
    }
    Text(
        text = "Time: $time s",
        modifier = Modifier
            .padding(top = 100.dp))
}


//@Preview
@Composable
fun Counter() {
    //var count by remember { mutableStateOf(0) }
    var count by rememberSaveable { mutableStateOf(0) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Count: $count",
            modifier = Modifier
                .padding(top = 100.dp)
        )
        Button(onClick = { count++ }) { Text("Add") }
    }
}


