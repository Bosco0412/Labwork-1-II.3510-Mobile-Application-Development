package com.tumme.ii3510_courseapp_2526

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
       val imagebJPC: ImageButton = findViewById(R.id.imageButtonJPC)
       imagebJPC.setOnClickListener{
           Log.d(this.localClassName, "Click on imageButtonJPC")
           val intentLJPC = Intent(this, LearnJetPackCompose::class.java)
           startActivity(intentLJPC)
       }

       val buttonTaskList: Button = findViewById(R.id.buttonTaskList)
       buttonTaskList.setOnClickListener{
           Log.d(this.localClassName, "Click on buttonTaskList")
           val intentLJPC = Intent(this, TaskList::class.java)
           startActivity(intentLJPC)
       }
    }
}