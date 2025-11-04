package com.tumme.scrudstudents.ui.subscribe

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SubscribeRow(
    details: SubscriptionDetails,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onView: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display student and course names instead of IDs.
            Text(text = details.studentName, modifier = Modifier.weight(0.35f))
            Text(text = details.courseName, modifier = Modifier.weight(0.35f))
            Text(text = String.format("%.1f", details.score), modifier = Modifier.weight(0.1f))

            // Action Buttons
            Row(
                modifier = Modifier.weight(0.2f),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onView, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "View Details")
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Score")
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Subscription")
                }
            }
        }
        Divider()
    }
}
