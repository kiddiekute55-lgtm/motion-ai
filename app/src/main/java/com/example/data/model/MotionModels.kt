package com.example.data.model

import androidx.annotation.DrawableRes

enum class ImageTypeCategory(
    val title: String,
    val iconName: String,
    val description: String
) {
    PORTRAIT("Portraits & Selfies", "face", "Facial micro-expressions, blinking, lip-sync, head nod"),
    PETS("Pets & Animals", "pets", "Breathing, tail wag, ear twitch, eye tracking"),
    NATURE("Nature & Water", "water_drop", "Water ripple, cloud drift, wind breeze, falling leaves"),
    ARCHITECTURE("Architecture & City", "location_city", "3D Parallax, cinematic camera drone pan, lighting shift"),
    VEHICLES("Vehicles & Motion", "directions_car", "Speed blur, wheel spin, exhaust particles, road motion"),
    ARTWORK("Anime & Digital Art", "palette", "Anime shimmer, lines breeze, hair swaying, glowing aura"),
    HISTORICAL("Old & B/W Photos", "history", "Auto-colorization, scratch restoration, gentle motion bring-to-life"),
    CUSTOM("Custom Photo", "upload_file", "Smart multi-preset AI auto-detection")
}

enum class MotionCategory(val label: String) {
    CAMERA("Camera Movement"),
    FACE("Face & Expression"),
    BODY("Character & Body"),
    ENVIRONMENT("Environment & FX"),
    ARTISTIC("Artistic & 3D")
}

data class MotionPreset(
    val id: String,
    val name: String,
    val category: MotionCategory,
    val description: String,
    val iconName: String,
    val isHot: Boolean = false,
    val defaultIntensity: Float = 0.7f,
    val tags: List<String> = emptyList()
)

data class AiModelEngine(
    val id: String,
    val name: String,
    val provider: String,
    val description: String,
    val isFreeTier: Boolean = true,
    val bestFor: String,
    val defaultFps: Int = 30
)

data class ImageAnalysisResult(
    val detectedCategory: ImageTypeCategory,
    val faceCount: Int = 0,
    val hasBackground: Boolean = true,
    val recommendedPresets: List<MotionPreset>,
    val qualityScore: Int = 85,
    val needsRestoration: Boolean = false,
    val isBlackAndWhite: Boolean = false,
    val promptDescription: String = "",
    val suggestedCameraMotion: String = "Cinematic Slow Zoom In"
)

data class SampleImagePreset(
    val id: String,
    val title: String,
    val category: ImageTypeCategory,
    @DrawableRes val drawableRes: Int,
    val sampleMotionPresetId: String,
    val description: String
)
