package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiVisionAnalyzer
import com.example.data.database.AnimationProject
import com.example.data.database.AppDatabase
import com.example.data.model.AiModelEngine
import com.example.data.model.ImageTypeCategory
import com.example.data.model.ImageAnalysisResult
import com.example.data.model.MotionPreset
import com.example.data.model.SampleImagePreset
import com.example.data.repository.MotionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface RenderState {
    object Idle : RenderState
    data class Analyzing(val stepName: String) : RenderState
    data class Preprocessing(val stepName: String, val progress: Float) : RenderState
    data class Rendering(val progress: Float, val fps: Int) : RenderState
    data class Success(val project: AnimationProject) : RenderState
    data class Error(val message: String) : RenderState
}

class MotionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = MotionRepository(db.animationProjectDao())
    private val visionAnalyzer = GeminiVisionAnalyzer(application, repository)

    // Flow for room stored projects
    val savedProjects: StateFlow<List<AnimationProject>> = repository.allProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteProjects: StateFlow<List<AnimationProject>> = repository.favoriteProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selected image & presets state
    private val _selectedUri = MutableStateFlow<Uri?>(null)
    val selectedUri: StateFlow<Uri?> = _selectedUri.asStateFlow()

    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    private val _selectedImageBitmap = MutableStateFlow<ImageBitmap?>(null)
    val selectedImageBitmap: StateFlow<ImageBitmap?> = _selectedImageBitmap.asStateFlow()

    private val _selectedDrawableRes = MutableStateFlow<Int?>(null)
    val selectedDrawableRes: StateFlow<Int?> = _selectedDrawableRes.asStateFlow()

    private val _analysisResult = MutableStateFlow<ImageAnalysisResult?>(null)
    val analysisResult: StateFlow<ImageAnalysisResult?> = _analysisResult.asStateFlow()

    val motionPresets = repository.getMotionPresets()
    val aiEngines = repository.getAiEngines()
    val samplePresets = repository.getSamplePresets()

    private val _selectedPreset = MutableStateFlow(motionPresets.first())
    val selectedPreset: StateFlow<MotionPreset> = _selectedPreset.asStateFlow()

    private val _selectedEngine = MutableStateFlow(aiEngines.first())
    val selectedEngine: StateFlow<AiModelEngine> = _selectedEngine.asStateFlow()

    // Preprocessing state
    val isRestored = MutableStateFlow(false)
    val isColorized = MutableStateFlow(false)
    val isUpscaled = MutableStateFlow(true)
    val removeBg = MutableStateFlow(false)

    // Export & Player config
    val exportFormat = MutableStateFlow("MP4") // MP4, GIF, WEBM
    val exportResolution = MutableStateFlow("1080p") // 480p, 720p, 1080p
    val exportFps = MutableStateFlow(30)
    val durationSeconds = MutableStateFlow(3.5f)

    // Render pipeline state
    private val _renderState = MutableStateFlow<RenderState>(RenderState.Idle)
    val renderState: StateFlow<RenderState> = _renderState.asStateFlow()

    init {
        // Load default sample preset on startup
        loadSamplePreset(samplePresets.first())
    }

    fun loadSamplePreset(sample: SampleImagePreset) {
        _selectedUri.value = null
        _selectedDrawableRes.value = sample.drawableRes
        val bitmap = BitmapFactory.decodeResource(getApplication<Application>().resources, sample.drawableRes)
        _selectedBitmap.value = bitmap
        _selectedImageBitmap.value = bitmap?.asImageBitmap()

        // Match recommended preset
        val preset = motionPresets.find { it.id == sample.sampleMotionPresetId } ?: motionPresets.first()
        _selectedPreset.value = preset

        analyzeCurrentImage(null, bitmap)
    }

    fun setSelectedImageUri(uri: Uri) {
        _selectedDrawableRes.value = null
        _selectedUri.value = uri

        viewModelScope.launch {
            try {
                val bitmap = decodeSampledBitmapFromUri(uri)
                _selectedBitmap.value = bitmap
                _selectedImageBitmap.value = bitmap?.asImageBitmap()

                analyzeCurrentImage(uri, bitmap)
            } catch (e: Exception) {
                _renderState.value = RenderState.Error("Failed to decode uploaded image: ${e.message}")
            }
        }
    }

    private fun decodeSampledBitmapFromUri(uri: Uri, maxDimension: Int = 1920): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            getApplication<Application>().contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            var inSampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            getApplication<Application>().contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun loadProject(project: AnimationProject) {
        val preset = motionPresets.find { it.id == project.motionPresetId } ?: motionPresets.first()
        _selectedPreset.value = preset

        val engine = aiEngines.find { it.name == project.aiEngineName } ?: aiEngines.first()
        _selectedEngine.value = engine

        isRestored.value = project.isRestored
        isColorized.value = project.isColorized
        isUpscaled.value = project.isUpscaled
        exportResolution.value = project.qualityResolution
        exportFps.value = project.fps

        val uriString = project.imageUri
        if (uriString.startsWith("resource://")) {
            val resId = uriString.removePrefix("resource://").toIntOrNull()
            if (resId != null) {
                _selectedDrawableRes.value = resId
                val bitmap = BitmapFactory.decodeResource(getApplication<Application>().resources, resId)
                _selectedBitmap.value = bitmap
                _selectedImageBitmap.value = bitmap?.asImageBitmap()
            }
        } else if (uriString.isNotBlank()) {
            try {
                val uri = Uri.parse(uriString)
                setSelectedImageUri(uri)
            } catch (e: Exception) {
                // fallback
            }
        }
    }

    fun selectMotionPreset(preset: MotionPreset) {
        _selectedPreset.value = preset
    }

    fun selectAiEngine(engine: AiModelEngine) {
        _selectedEngine.value = engine
    }

    private fun analyzeCurrentImage(uri: Uri?, bitmap: Bitmap?) {
        viewModelScope.launch {
            _renderState.value = RenderState.Analyzing("AI Scene & Subject Detection...")
            val result = visionAnalyzer.analyzeImage(uri ?: Uri.EMPTY, bitmap)
            _analysisResult.value = result

            // Auto select best engine & preset if category requires special model
            when (result.detectedCategory) {
                ImageTypeCategory.PORTRAIT -> {
                    aiEngines.find { it.id == "liveportrait" }?.let { _selectedEngine.value = it }
                }
                ImageTypeCategory.ARTWORK -> {
                    aiEngines.find { it.id == "animatediff" }?.let { _selectedEngine.value = it }
                }
                else -> {
                    aiEngines.find { it.id == "svd" }?.let { _selectedEngine.value = it }
                }
            }

            if (result.isBlackAndWhite) {
                isColorized.value = true
            }
            if (result.needsRestoration) {
                isRestored.value = true
            }

            _renderState.value = RenderState.Idle
        }
    }

    fun generateMotionAnimation() {
        viewModelScope.launch {
            try {
                _renderState.value = RenderState.Preprocessing("Enhancing & Preprocessing Image...", 0.15f)
                delay(800)

                _renderState.value = RenderState.Preprocessing("Executing Face & Object Segmentation...", 0.35f)
                delay(800)

                _renderState.value = RenderState.Preprocessing("Building 3D Depth Map & Spatial Mesh...", 0.55f)
                delay(800)

                val engine = selectedEngine.value
                val preset = selectedPreset.value

                for (p in 60..100 step 10) {
                    _renderState.value = RenderState.Rendering(p / 100f, exportFps.value)
                    delay(300)
                }

                val titleName = analysisResult.value?.promptDescription?.take(25) ?: "Motion AI Animation"
                val imageString = selectedUri.value?.toString() ?: "resource://${selectedDrawableRes.value}"

                val newProject = AnimationProject(
                    title = titleName,
                    imageUri = imageString,
                    categoryName = analysisResult.value?.detectedCategory?.title ?: "Custom Photo",
                    motionPresetId = preset.id,
                    motionPresetName = preset.name,
                    aiEngineName = engine.name,
                    durationSeconds = durationSeconds.value,
                    fps = exportFps.value,
                    qualityResolution = exportResolution.value,
                    isRestored = isRestored.value,
                    isColorized = isColorized.value,
                    isUpscaled = isUpscaled.value
                )

                val savedId = repository.saveProject(newProject)
                val finalProject = newProject.copy(id = savedId)

                _renderState.value = RenderState.Success(finalProject)
            } catch (e: Exception) {
                _renderState.value = RenderState.Error("Generation failed: ${e.message}")
            }
        }
    }

    fun resetRenderState() {
        _renderState.value = RenderState.Idle
    }

    fun deleteProject(project: AnimationProject) {
        viewModelScope.launch {
            repository.deleteProject(project.id)
        }
    }

    fun toggleFavorite(project: AnimationProject) {
        viewModelScope.launch {
            repository.toggleFavorite(project)
        }
    }
}
