package com.tumme.scrudstudents.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.tumme.scrudstudents.data.repository.SampleDataSeeder
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onDataLoaded: () -> Unit,
    seeder: SampleDataSeeder = hiltViewModel<SplashViewModel>().seeder
) {
    var isLoading by remember { mutableStateOf(true) }
    var loadingText by remember { mutableStateOf("Initializing...") }

    LaunchedEffect(Unit) {
        try {
            loadingText = "Loading sample data..."
            seeder.seedData()
            delay(1000) // Show splash for at least 1 second
            loadingText = "Ready!"
            delay(500)
            onDataLoaded()
        } catch (e: Exception) {
            loadingText = "Error loading data"
            delay(2000)
            onDataLoaded()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SCRUD Students",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Course Management System",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = loadingText,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    val seeder: SampleDataSeeder
) : ViewModel()
