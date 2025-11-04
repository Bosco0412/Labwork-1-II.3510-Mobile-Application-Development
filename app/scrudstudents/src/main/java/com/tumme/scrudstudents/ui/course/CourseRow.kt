package com.tumme.scrudstudents.ui.course

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tumme.scrudstudents.data.local.model.CourseEntity

/**
 * A Composable function that displays a single row of course information.
 * This version is styled to match the StudentRow composable.
 */
@Composable
fun CourseRow(
    course: CourseEntity,
    onDelete: () -> Unit,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Course Name
            Text(text = course.nameCourse, modifier = Modifier.weight(0.4f))
            // ECTS Credits
            Text(text = String.format("%.1f", course.ectsCourse), modifier = Modifier.weight(0.2f))
            // Course Level
            Text(text = course.levelCourse.value, modifier = Modifier.weight(0.2f))

            // Action Buttons
            Row(
                modifier = Modifier.weight(0.2f),
                horizontalArrangement = Arrangement.End // Keep buttons to the right
            ) {
                IconButton(onClick = onView, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "View")
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
                IconButton(onClick = onShare, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            }
        }
        Divider()
    }
}

