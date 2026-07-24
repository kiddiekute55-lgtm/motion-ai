package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MotionPreset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var speedY: Float,
    var speedX: Float,
    var radius: Float,
    var alpha: Float,
    var color: Color
)

@Composable
fun MotionCanvasPreview(
    imageBitmap: ImageBitmap?,
    drawableRes: Int?,
    motionPreset: MotionPreset,
    isPlaying: Boolean = true,
    speedFactor: Float = 1.0f,
    showFaceMesh: Boolean = false,
    showDepthMap: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "motion_anim")

    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (3000 / speedFactor).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim_time"
    )

    var touchOffsetX by remember { mutableFloatStateOf(0f) }
    var touchOffsetY by remember { mutableFloatStateOf(0f) }

    // Particles setup
    val particles = remember(motionPreset.id) {
        List(40) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speedY = Random.nextFloat() * 0.008f + 0.002f,
                speedX = (Random.nextFloat() - 0.5f) * 0.003f,
                radius = Random.nextFloat() * 8f + 2f,
                alpha = Random.nextFloat() * 0.7f + 0.3f,
                color = when (motionPreset.id) {
                    "fire_embers" -> Color(0xFFFF6D00)
                    "rain_snow" -> Color(0xFFE0F7FA)
                    "anime_shimmer" -> Color(0xFFFFD54F)
                    "cyberpunk_neon" -> Color(0xFF00E5FF)
                    else -> Color.White.copy(alpha = 0.5f)
                }
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, _, _ ->
                    touchOffsetX = (touchOffsetX + pan.x).coerceIn(-100f, 100f)
                    touchOffsetY = (touchOffsetY + pan.y).coerceIn(-100f, 100f)
                }
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Calculate motion keyframe math
        val phase = animTime * 2 * Math.PI.toFloat()
        val sineVal = sin(phase)
        val cosineVal = cos(phase)

        val (scaleX, scaleY, translationX, translationY, rotation) = when (motionPreset.id) {
            "zoom_in" -> {
                val s = 1.0f + 0.12f * animTime
                val tx = sineVal * 15f
                val ty = cosineVal * 10f
                Tuple5(s, s, tx, ty, sineVal * 1.5f)
            }
            "pan_right" -> {
                val tx = (animTime - 0.5f) * 80f
                Tuple5(1.08f, 1.08f, tx, 0f, 0f)
            }
            "parallax_3d" -> {
                val tx = touchOffsetX * 0.4f + sineVal * 25f
                val ty = touchOffsetY * 0.4f + cosineVal * 15f
                Tuple5(1.1f, 1.1f, tx, ty, sineVal * 2f)
            }
            "portrait_live" -> {
                val tx = sineVal * 8f
                val ty = cosineVal * 5f
                val rot = sineVal * 2.5f
                Tuple5(1.05f, 1.05f, tx, ty, rot)
            }
            "warm_smile" -> {
                val s = 1.0f + 0.04f * sineVal
                Tuple5(s, s, 0f, sineVal * 6f, sineVal * 1.2f)
            }
            "cyberpunk_neon" -> {
                val tx = (animTime - 0.5f) * 40f
                Tuple5(1.08f, 1.08f, tx, cosineVal * 8f, 0f)
            }
            else -> {
                val s = 1.0f + 0.05f * sineVal
                Tuple5(s, s, sineVal * 10f, cosineVal * 6f, sineVal * 1f)
            }
        }

        val resImageBitmap = drawableRes?.let { ImageBitmap.imageResource(id = it) }
        val activeBitmap = imageBitmap ?: resImageBitmap

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            withTransform({
                translate(left = translationX, top = translationY)
                scale(scaleX, scaleY, pivot = Offset(canvasW / 2f, canvasH / 2f))
                rotate(degrees = rotation, pivot = Offset(canvasW / 2f, canvasH / 2f))
            }) {
                if (activeBitmap != null) {
                    drawImage(
                        image = activeBitmap,
                        dstSize = androidx.compose.ui.unit.IntSize(canvasW.toInt(), canvasH.toInt())
                    )
                } else {
                    // Fallback background rendering
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF020617))
                        )
                    )
                }
            }

            // Render Special Motion Effects & Shader Overlays
            when (motionPreset.id) {
                "water_ripple" -> {
                    // Draw water surface sine ripples across lower third
                    val rippleY = canvasH * 0.65f
                    val wavePath = Path()
                    wavePath.moveTo(0f, rippleY)
                    var x = 0f
                    while (x <= canvasW) {
                        val y = rippleY + sin((x / 40f) + phase * 3f) * 12f
                        wavePath.lineTo(x, y)
                        x += 10f
                    }
                    wavePath.lineTo(canvasW, canvasH)
                    wavePath.lineTo(0f, canvasH)
                    wavePath.close()

                    drawPath(
                        path = wavePath,
                        brush = Brush.verticalGradient(
                            listOf(Color(0x6600E5FF), Color(0x990284C7))
                        )
                    )
                }
                "fire_embers", "rain_snow", "anime_shimmer" -> {
                    // Draw active particles
                    particles.forEach { particle ->
                        if (isPlaying) {
                            particle.y -= particle.speedY
                            particle.x += particle.speedX
                            if (particle.y < 0f) particle.y = 1f
                            if (particle.x < 0f) particle.x = 1f
                            if (particle.x > 1f) particle.x = 0f
                        }

                        val px = particle.x * canvasW
                        val py = particle.y * canvasH

                        drawCircle(
                            color = particle.color.copy(alpha = particle.alpha * (1f - (py / canvasH) * 0.3f)),
                            radius = particle.radius,
                            center = Offset(px, py)
                        )
                    }
                }
                "portrait_live" -> {
                    // Eye blink simulation overlay (subtle micro blink)
                    if (animTime > 0.85f && animTime < 0.92f) {
                        // Eye blink frame
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.15f),
                            radius = canvasW * 0.15f,
                            center = Offset(canvasW * 0.5f, canvasH * 0.38f)
                        )
                    }
                }
                "cyberpunk_neon" -> {
                    // Chromatic flash lines
                    if (animTime in 0.4f..0.45f) {
                        drawLine(
                            color = Color(0xFF00E5FF).copy(alpha = 0.6f),
                            start = Offset(0f, canvasH * 0.3f),
                            end = Offset(canvasW, canvasH * 0.3f),
                            strokeWidth = 6f
                        )
                        drawLine(
                            color = Color(0xFFFF007F).copy(alpha = 0.6f),
                            start = Offset(0f, canvasH * 0.7f),
                            end = Offset(canvasW, canvasH * 0.7f),
                            strokeWidth = 4f
                        )
                    }
                }
            }

            // Draw Face Mesh / Depth Map AI Overlay when enabled
            if (showFaceMesh) {
                val centerX = canvasW * 0.5f + translationX
                val centerY = canvasH * 0.4f + translationY

                // Face bounding box
                drawRect(
                    color = Color(0xFF00E5FF),
                    topLeft = Offset(centerX - 120f, centerY - 140f),
                    size = Size(240f, 280f),
                    style = Stroke(width = 3f)
                )

                // Face mesh landmarks
                val landmarkColor = Color(0xFF76FF03)
                drawCircle(landmarkColor, 4f, Offset(centerX - 40f, centerY - 40f)) // Left eye
                drawCircle(landmarkColor, 4f, Offset(centerX + 40f, centerY - 40f)) // Right eye
                drawCircle(landmarkColor, 4f, Offset(centerX, centerY))             // Nose tip
                drawCircle(landmarkColor, 4f, Offset(centerX - 30f, centerY + 50f)) // Left mouth
                drawCircle(landmarkColor, 4f, Offset(centerX + 30f, centerY + 50f)) // Right mouth
            }
        }

        // Live Motion Badge & AI Engine Tag
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.65f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Motion",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "${motionPreset.name} • 30 FPS",
                        color = Color.White,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A, val b: B, val c: C, val d: D, val e: E
)
