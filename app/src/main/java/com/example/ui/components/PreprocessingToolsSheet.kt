package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Portrait
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PreprocessOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val isEnabled: Boolean
)

@Composable
fun PreprocessingToolsSheet(
    isRestored: Boolean,
    onRestoredChanged: (Boolean) -> Unit,
    isColorized: Boolean,
    onColorizedChanged: (Boolean) -> Unit,
    isUpscaled: Boolean,
    onUpscaledChanged: (Boolean) -> Unit,
    removeBackground: Boolean,
    onRemoveBgChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = "AI Enhance",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Image Preprocessing & Restoration",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Option 1: Restoration
            PreprocessToggleRow(
                title = "Restore Old / Damaged Photo",
                subtitle = "Removes scratches, fixes compression artifacts & noise",
                icon = Icons.Default.History,
                checked = isRestored,
                onCheckedChange = onRestoredChanged
            )

            // Option 2: Colorization
            PreprocessToggleRow(
                title = "B&W Colorization",
                subtitle = "Automatically colorizes black & white historical photos",
                icon = Icons.Default.ColorLens,
                checked = isColorized,
                onCheckedChange = onColorizedChanged
            )

            // Option 3: 4K AI Upscaling
            PreprocessToggleRow(
                title = "4K AI Super Resolution",
                subtitle = "Enhances image sharpness & facial fine detail",
                icon = Icons.Default.HighQuality,
                checked = isUpscaled,
                onCheckedChange = onUpscaledChanged
            )

            // Option 4: Background Isolation
            PreprocessToggleRow(
                title = "Isolate Subject / Background",
                subtitle = "Separates layers for clean 3D Parallax camera motion",
                icon = Icons.Default.Portrait,
                checked = removeBackground,
                onCheckedChange = onRemoveBgChanged
            )
        }
    }
}

@Composable
private fun PreprocessToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (checked) Color(0xFF00E5FF) else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = Color(0xFF00E5FF),
                uncheckedTrackColor = Color(0xFF334155)
            )
        )
    }
}
