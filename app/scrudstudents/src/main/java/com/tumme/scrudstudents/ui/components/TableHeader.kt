package com.tumme.scrudstudents.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

@Composable
fun TableHeader(cells: List<String>, weights: List<Float>) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFFEFEFEF))
        .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in cells.indices) {
            Text(
                text = cells[i],
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(weights[i])
                    .padding(horizontal = 4.dp)
            )
        }
    }
}