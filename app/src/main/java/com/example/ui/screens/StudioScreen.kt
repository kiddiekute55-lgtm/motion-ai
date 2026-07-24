package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MotionPhotosAuto
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModelEngine
import com.example.data.model.ImageTypeCategory
import com.example.data.model.MotionCategory
import com.example.data.model.MotionPreset
import com.example.ui.components.ImageComparisonSlider
import com.example.ui.components.MotionCanvasPreview
import com.example.ui.components.PreprocessingToolsSheet
import com.example.ui.viewmodel.MotionViewModel
import com.example.ui.viewmodel.RenderState

@Composable
fun StudioScreen(
    viewModel: MotionViewModel,
    onNavigateToPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedImageBitmap by viewModel.selectedImageBitmap.collectAsState()
    val selectedDrawableRes by viewModel.selectedDrawableRes.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val selectedEngine by viewModel.selectedEngine.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()
    val renderState by viewModel.renderState.collectAsState()

    val isRestored by viewModel.isRestored.collectAsState()
    val isColorized by viewModel.isColorized.collectAsState()
    val isUpscaled by viewModel.isUpscaled.collectAsState()
    val removeBg by viewModel.removeBg.collectAsState()

    var showComparison by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<MotionCategory?>(null) }

    // System Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setSelectedImageUri(it) }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Section 1: Header Bar & Preset Sample Selector
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "MotionAI Studio",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Universal Image to AI Video Motion Generator",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }

            OutlinedButton(
                onClick = { photoPickerLauncher.launch("image/*") },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Upload",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Upload Image", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sample Quick Preset Carousel
        Text(
            text = "Or try sample image:",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.samplePresets) { sample ->
                val isSelected = selectedDrawableRes == sample.drawableRes
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .clickable { viewModel.loadSamplePreset(sample) }
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) Color(0xFF00E5FF) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = sample.drawableRes),
                            contentDescription = sample.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = sample.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Motion Canvas Preview Container
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (showComparison) {
                    ImageComparisonSlider(
                        beforeBitmap = selectedImageBitmap,
                        motionPreset = selectedPreset,
                        drawableRes = selectedDrawableRes,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    MotionCanvasPreview(
                        imageBitmap = selectedImageBitmap,
                        drawableRes = selectedDrawableRes,
                        motionPreset = selectedPreset,
                        isPlaying = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Comparison Toggle Button
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clickable { showComparison = !showComparison }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = "Compare",
                            tint = if (showComparison) Color(0xFF00E5FF) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showComparison) "Split View ON" else "Compare",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section 3: Gemini Vision AI Analysis Card
        analysisResult?.let { analysis ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Analysis",
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI Vision: ${analysis.detectedCategory.title}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFF312E81),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "${analysis.qualityScore}% Quality",
                                    color = Color(0xFFA5B4FC),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = analysis.promptDescription,
                            color = Color(0xFFC7D2FE),
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 4: Motion Presets Selector
        Text(
            text = "Select AI Motion Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text("All Presets") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00E5FF),
                        selectedLabelColor = Color.Black
                    )
                )
            }
            items(MotionCategory.values()) { category ->
                FilterChip(
                    selected = selectedCategoryFilter == category,
                    onClick = { selectedCategoryFilter = category },
                    label = { Text(category.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00E5FF),
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val filteredPresets = viewModel.motionPresets.filter {
            selectedCategoryFilter == null || it.category == selectedCategoryFilter
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredPresets) { preset ->
                val isSelected = selectedPreset.id == preset.id
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(160.dp)
                        .clickable { viewModel.selectMotionPreset(preset) }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF334155),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.MotionPhotosAuto,
                                contentDescription = preset.name,
                                tint = if (isSelected) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                            if (preset.isHot) {
                                Surface(
                                    color = Color(0xFFFF6D00),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "HOT",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = preset.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = preset.description,
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 5: AI Model Engine Selector
        Text(
            text = "AI Model Engine",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.aiEngines) { engine ->
                val isSelected = selectedEngine.id == engine.id
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF7C3AED).copy(alpha = 0.25f) else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .width(200.dp)
                        .clickable { viewModel.selectAiEngine(engine) }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFFA78BFA) else Color(0xFF334155),
                            shape = RoundedCornerShape(14.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = "Engine",
                                tint = if (isSelected) Color(0xFFA78BFA) else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = engine.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = engine.bestFor,
                            color = Color(0xFFC4B5FD),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 6: Image Enhancement & Preprocessing
        PreprocessingToolsSheet(
            isRestored = isRestored,
            onRestoredChanged = { viewModel.isRestored.value = it },
            isColorized = isColorized,
            onColorizedChanged = { viewModel.isColorized.value = it },
            isUpscaled = isUpscaled,
            onUpscaledChanged = { viewModel.isUpscaled.value = it },
            removeBackground = removeBg,
            onRemoveBgChanged = { viewModel.removeBg.value = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section 7: Render Action Button & Render Progress State
        when (val state = renderState) {
            is RenderState.Idle -> {
                Button(
                    onClick = { viewModel.generateMotionAnimation() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Generate",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GENERATE AI MOTION ANIMATION",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
            is RenderState.Analyzing -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = state.stepName, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
            is RenderState.Preprocessing -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = state.stepName, color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { state.progress },
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            is RenderState.Rendering -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Rendering AI Motion Keyframes...", color = Color.White, fontSize = 14.sp)
                            Text(text = "${(state.progress * 100).toInt()}%", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { state.progress },
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            is RenderState.Success -> {
                Button(
                    onClick = {
                        viewModel.resetRenderState()
                        onNavigateToPlayer()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ANIMATION READY! VIEW PLAYER & EXPORT",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            is RenderState.Error -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = state.message, color = Color(0xFFEF4444), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.resetRenderState() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
