package com.tumme.scrudstudents.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.data.local.model.StudentEntity
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    studentId: Int,
    viewModel: StudentListViewModel = hiltViewModel(),
    onBack: ()->Unit = {}
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var student by remember { mutableStateOf<StudentEntity?>(null) }

    LaunchedEffect(studentId) {
        student = viewModel.findStudent(studentId)
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Student details") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
        })
    }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (student == null) {
                Text("Loading...")
            } else {
                Text("ID: ${student!!.idStudent}")
                Text("Name: ${student!!.firstName} ${student!!.lastName}")
                Text("DOB: ${sdf.format(student!!.dateOfBirth)}")
                Text("Gender: ${student!!.gender.value}")
            }
        }
    }
}
