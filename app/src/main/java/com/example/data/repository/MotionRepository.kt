package com.example.data.repository

import com.example.R
import com.example.data.database.AnimationProject
import com.example.data.database.AnimationProjectDao
import com.example.data.model.AiModelEngine
import com.example.data.model.ImageTypeCategory
import com.example.data.model.MotionCategory
import com.example.data.model.MotionPreset
import com.example.data.model.SampleImagePreset
import kotlinx.coroutines.flow.Flow

class MotionRepository(private val dao: AnimationProjectDao) {

    val allProjects: Flow<List<AnimationProject>> = dao.getAllProjects()
    val favoriteProjects: Flow<List<AnimationProject>> = dao.getFavoriteProjects()

    suspend fun saveProject(project: AnimationProject): Long = dao.insertProject(project)

    suspend fun deleteProject(id: Long) = dao.deleteProjectById(id)

    suspend fun toggleFavorite(project: AnimationProject) {
        dao.updateProject(project.copy(isFavorite = !project.isFavorite))
    }

    fun getMotionPresets(): List<MotionPreset> {
        return listOf(
            MotionPreset(
                id = "zoom_in",
                name = "Cinematic Zoom In",
                category = MotionCategory.CAMERA,
                description = "Smooth focal push inwards with depth-of-field enhancement",
                iconName = "zoom_in",
                isHot = true,
                tags = listOf("cinematic", "focus", "camera")
            ),
            MotionPreset(
                id = "pan_right",
                name = "Pan Left to Right",
                category = MotionCategory.CAMERA,
                description = "Horizontal camera glide panning across panoramic details",
                iconName = "pan_tool",
                tags = listOf("landscape", "city", "architecture")
            ),
            MotionPreset(
                id = "parallax_3d",
                name = "3D Parallax Tilt",
                category = MotionCategory.CAMERA,
                description = "Separates foreground and background into interactive 3D spatial planes",
                iconName = "view_in_ar",
                isHot = true,
                tags = listOf("3d", "spatial", "depth")
            ),
            MotionPreset(
                id = "portrait_live",
                name = "Live Portrait Micro-Motion",
                category = MotionCategory.FACE,
                description = "Realistic eye blinking, subtle head nod, lip movement and facial breathing",
                iconName = "face",
                isHot = true,
                tags = listOf("portrait", "face", "selfie", "human")
            ),
            MotionPreset(
                id = "lip_sync",
                name = "Lip-Sync Speech Rhythm",
                category = MotionCategory.FACE,
                description = "Generates natural mouth speaking rhythms and audio-driven face pulses",
                iconName = "record_voice_over",
                tags = listOf("speech", "talking", "avatar")
            ),
            MotionPreset(
                id = "warm_smile",
                name = "Smile & Expression Shift",
                category = MotionCategory.FACE,
                description = "Gently animates face from neutral to warm, eye-crinkling smile",
                iconName = "sentiment_satisfied_alt",
                tags = listOf("smile", "portrait", "happy")
            ),
            MotionPreset(
                id = "water_ripple",
                name = "Water Ripple & Flow",
                category = MotionCategory.ENVIRONMENT,
                description = "Animates lakes, oceans, rivers and rain puddles with fluid physics",
                iconName = "water",
                isHot = true,
                tags = listOf("water", "lake", "ocean", "rain")
            ),
            MotionPreset(
                id = "fire_embers",
                name = "Fire & Ember Flicker",
                category = MotionCategory.ENVIRONMENT,
                description = "Dynamic flame glow, rising heat distortion and floating ember particles",
                iconName = "local_fire_department",
                tags = listOf("fire", "flames", "embers", "fantasy")
            ),
            MotionPreset(
                id = "rain_snow",
                name = "Atmospheric Rain / Snow",
                category = MotionCategory.ENVIRONMENT,
                description = "Adds interactive falling rain drops or swirling snow particles over scene",
                iconName = "ac_unit",
                tags = listOf("weather", "snow", "rain", "storm")
            ),
            MotionPreset(
                id = "hair_cloth_breeze",
                name = "Hair & Fabric Breeze",
                category = MotionCategory.BODY,
                description = "Simulates natural wind gusts gently swaying hair strands and clothing folds",
                iconName = "air",
                tags = listOf("hair", "wind", "fashion", "character")
            ),
            MotionPreset(
                id = "dance_loop",
                name = "Rhythmic Dance Groove",
                category = MotionCategory.BODY,
                description = "Full-body rhythmic movement with shoulder sway and head groove",
                iconName = "sports_kabaddi",
                tags = listOf("dance", "fullbody", "rhythm")
            ),
            MotionPreset(
                id = "anime_shimmer",
                name = "Anime Speed & Aura",
                category = MotionCategory.ARTISTIC,
                description = "Adds stylized speed lines, eye gleam sparks and floating magical aura",
                iconName = "auto_awesome",
                isHot = true,
                tags = listOf("anime", "2d", "art", "sparkle")
            ),
            MotionPreset(
                id = "cyberpunk_neon",
                name = "Cyberpunk Pulse",
                category = MotionCategory.ARTISTIC,
                description = "Neon light flicker, wet street reflections and high-speed motion blur",
                iconName = "bolt",
                tags = listOf("cyberpunk", "vehicle", "scifi", "neon")
            )
        )
    }

    fun getAiEngines(): List<AiModelEngine> {
        return listOf(
            AiModelEngine(
                id = "svd",
                name = "Stable Video Diffusion (SVD-XT)",
                provider = "Stability AI / HuggingFace",
                description = "Best all-around model for realistic video motion, camera moves, and scene dynamics.",
                isFreeTier = true,
                bestFor = "Landscapes, Vehicles, Objects, Photos",
                defaultFps = 30
            ),
            AiModelEngine(
                id = "liveportrait",
                name = "LivePortrait AI",
                provider = "Open Source / GitHub",
                description = "Ultra-realistic 2D facial reenactment, eye blinking, lip control & head pose driven motion.",
                isFreeTier = true,
                bestFor = "Portraits, Selfies, Historical Photos",
                defaultFps = 30
            ),
            AiModelEngine(
                id = "animatediff",
                name = "AnimateDiff (ComfyUI)",
                provider = "ComfyUI / Replicate Free",
                description = "Specialized for anime characters, illustrations, 3D renders, and artistic stylizations.",
                isFreeTier = true,
                bestFor = "Anime, Cartoons, Digital Artwork",
                defaultFps = 24
            ),
            AiModelEngine(
                id = "sadtalker",
                name = "SadTalker / MuseTalk",
                provider = "Hugging Face Spaces",
                description = "Generates realistic lip-synced speech animations from audio or script input.",
                isFreeTier = true,
                bestFor = "Avatars, Talking Head Videos",
                defaultFps = 30
            ),
            AiModelEngine(
                id = "fal_free",
                name = "Fal AI Motion Fast",
                provider = "Fal.ai Free Tier",
                description = "High-speed rendering optimized for instant preview and quick export loops.",
                isFreeTier = true,
                bestFor = "Fast Generation & Mobile Exports",
                defaultFps = 60
            )
        )
    }

    fun getSamplePresets(): List<SampleImagePreset> {
        return listOf(
            SampleImagePreset(
                id = "sample_portrait",
                title = "Studio Portrait",
                category = ImageTypeCategory.PORTRAIT,
                drawableRes = R.drawable.preset_portrait_1784889047149,
                sampleMotionPresetId = "portrait_live",
                description = "High-resolution portrait ideal for testing face blinking, smile shifts and live expressions."
            ),
            SampleImagePreset(
                id = "sample_landscape",
                title = "Alpine Lake & Peaks",
                category = ImageTypeCategory.NATURE,
                drawableRes = R.drawable.preset_landscape_1784889059778,
                sampleMotionPresetId = "water_ripple",
                description = "Scenic landscape with water surface reflections perfect for liquid motion & camera zooms."
            ),
            SampleImagePreset(
                id = "sample_anime",
                title = "Cherry Blossom Anime",
                category = ImageTypeCategory.ARTWORK,
                drawableRes = R.drawable.preset_anime_1784889073162,
                sampleMotionPresetId = "anime_shimmer",
                description = "Anime style illustration with cherry blossoms ideal for hair breeze and magical aura effects."
            ),
            SampleImagePreset(
                id = "sample_cyberpunk",
                title = "Cyberpunk Supercar",
                category = ImageTypeCategory.VEHICLES,
                drawableRes = R.drawable.preset_cyberpunk_1784889084187,
                sampleMotionPresetId = "cyberpunk_neon",
                description = "Futuristic sports car in rain wet street suitable for neon pulses, speed blur & 3D parallax."
            )
        )
    }
}
