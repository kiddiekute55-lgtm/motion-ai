package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MotionPreset

import androidx.compose.ui.res.imageResource

@Composable
fun ImageComparisonSlider(
    beforeBitmap: ImageBitmap?,
    motionPreset: MotionPreset,
    drawableRes: Int? = null,
    modifier: Modifier = Modifier
) {
    var splitRatio by remember { mutableFloatStateOf(0.5f) }
    val resBitmap = drawableRes?.let { ImageBitmap.imageResource(id = it) }
    val activeBitmap = beforeBitmap ?: resBitmap

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val newRatio = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                    splitRatio = newRatio
                }
            }
    ) {
        val totalWidth = constraints.maxWidth.toFloat()
        val totalHeight = constraints.maxHeight.toFloat()
        val splitPx = totalWidth * splitRatio

        // Right side: AI Animated Canvas
        MotionCanvasPreview(
            imageBitmap = activeBitmap,
            drawableRes = drawableRes,
            motionPreset = motionPreset,
            isPlaying = true,
            modifier = Modifier.fillMaxSize()
        )

        // Left side: Original Static Image clipped to split ratio
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (activeBitmap != null) {
                val clipPath = Path().apply {
                    addRect(Rect(0f, 0f, splitPx, totalHeight))
                }
                clipPath(clipPath) {
                    drawImage(
                        image = activeBitmap,
                        dstSize = androidx.compose.ui.unit.IntSize(totalWidth.toInt(), totalHeight.toInt())
                    )
                }
            }

            // Divider Line
            drawLine(
                color = Color.White,
                start = Offset(splitPx, 0f),
                end = Offset(splitPx, totalHeight),
                strokeWidth = 4f
            )
        }

        // Draggable Handle
        Box(
            modifier = Modifier
                .offset { IntOffset(splitPx.toInt() - 24.dp.roundToPx(), (totalHeight / 2).toInt() - 24.dp.roundToPx()) }
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF00E5FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Slide compare",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }

        // Labels
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Text(
                text = "STATIC ORIGINAL",
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.85f)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Text(
                text = "AI MOTION ANIMATED",
                color = Color.Black,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
