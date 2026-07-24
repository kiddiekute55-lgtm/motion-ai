package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.ImageTypeCategory
import com.example.data.model.ImageAnalysisResult
import com.example.data.model.MotionPreset
import com.example.data.repository.MotionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiVisionAnalyzer(
    private val context: Context,
    private val motionRepository: MotionRepository
) {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeImage(imageUri: Uri, bitmap: Bitmap?): ImageAnalysisResult = withContext(Dispatchers.IO) {
        val loadedBitmap = bitmap ?: loadBitmapFromUri(imageUri)
        val allPresets = motionRepository.getMotionPresets()

        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && loadedBitmap != null) {
            try {
                val base64Data = bitmapToBase64(loadedBitmap)
                val promptText = """
                    Analyze this image for AI Video Animation generation:
                    1. What is the primary category? Options: PORTRAIT, PETS, NATURE, ARCHITECTURE, VEHICLES, ARTWORK, HISTORICAL, CUSTOM
                    2. Estimate number of human faces detected.
                    3. Is this an old, damaged or black and white photo that needs colorization or restoration? (true/false)
                    4. Brief summary of scene and subjects.
                    5. Suggest best motion effect ID (options: zoom_in, pan_right, parallax_3d, portrait_live, water_ripple, fire_embers, rain_snow, anime_shimmer, cyberpunk_neon).
                    
                    Respond strictly in JSON format:
                    {
                      "category": "PORTRAIT",
                      "faces": 1,
                      "needsRestoration": false,
                      "isBlackAndWhite": false,
                      "description": "A close up photo of a person",
                      "suggestedMotion": "portrait_live"
                    }
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    val contentsArray = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val partsArray = JSONArray().apply {
                                put(JSONObject().put("text", promptText))
                                put(JSONObject().put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Data)
                                }))
                            }
                            put("parts", partsArray)
                        }
                        put(contentObj)
                    }
                    put("contents", contentsArray)
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = requestJson.toString().toRequestBody(mediaType)
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

                val httpRequest = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val httpResponse = okHttpClient.newCall(httpRequest).execute()
                val responseString = httpResponse.body?.string()

                if (!responseString.isNullOrBlank()) {
                    val rootJson = JSONObject(responseString)
                    val candidates = rootJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        val text = parts?.optJSONObject(0)?.optString("text")

                        if (!text.isNullOrBlank()) {
                            return@withContext parseGeminiAnalysis(text, allPresets)
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to offline vision heuristics
            }
        }

        // Offline / Heuristic Vision Analysis
        return@withContext performHeuristicAnalysis(loadedBitmap, allPresets)
    }

    private fun parseGeminiAnalysis(responseText: String, allPresets: List<MotionPreset>): ImageAnalysisResult {
        try {
            val jsonStart = responseText.indexOf("{")
            val jsonEnd = responseText.lastIndexOf("}")
            if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                val jsonStr = responseText.substring(jsonStart, jsonEnd + 1)
                val obj = JSONObject(jsonStr)

                val categoryStr = obj.optString("category", "NATURE").uppercase()
                val facesCount = obj.optInt("faces", 0)
                val needsRestoration = obj.optBoolean("needsRestoration", false)
                val isBw = obj.optBoolean("isBlackAndWhite", false)
                val description = obj.optString("description", "")
                val suggestedMotionId = obj.optString("suggestedMotion", "")

                val category = try {
                    ImageTypeCategory.valueOf(categoryStr)
                } catch (e: Exception) {
                    if (facesCount > 0) ImageTypeCategory.PORTRAIT else ImageTypeCategory.NATURE
                }

                val recommended = filterPresetsForCategory(category, suggestedMotionId, allPresets)

                return ImageAnalysisResult(
                    detectedCategory = category,
                    faceCount = facesCount,
                    hasBackground = true,
                    recommendedPresets = recommended,
                    qualityScore = 92,
                    needsRestoration = needsRestoration,
                    isBlackAndWhite = isBw,
                    promptDescription = description.ifBlank { "High-quality ${category.title} image" },
                    suggestedCameraMotion = "3D Parallax & Smooth Zoom"
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        return performHeuristicAnalysis(null, allPresets)
    }

    private fun performHeuristicAnalysis(bitmap: Bitmap?, allPresets: List<MotionPreset>): ImageAnalysisResult {
        var category = ImageTypeCategory.NATURE
        var faces = 0

        if (bitmap != null) {
            val width = bitmap.width
            val height = bitmap.height
            val isAspectPortrait = height > width * 1.1

            if (isAspectPortrait) {
                category = ImageTypeCategory.PORTRAIT
                faces = 1
            } else {
                category = ImageTypeCategory.NATURE
            }
        }

        val recommended = filterPresetsForCategory(category, null, allPresets)

        return ImageAnalysisResult(
            detectedCategory = category,
            faceCount = faces,
            hasBackground = true,
            recommendedPresets = recommended,
            qualityScore = 88,
            needsRestoration = false,
            isBlackAndWhite = false,
            promptDescription = "Detected ${category.title} content ready for motion synthesis.",
            suggestedCameraMotion = "Cinematic Zoom In & Parallax"
        )
    }

    private fun filterPresetsForCategory(
        category: ImageTypeCategory,
        suggestedId: String?,
        allPresets: List<MotionPreset>
    ): List<MotionPreset> {
        val matched = mutableListOf<MotionPreset>()
        if (!suggestedId.isNullOrBlank()) {
            allPresets.find { it.id == suggestedId }?.let { matched.add(it) }
        }

        when (category) {
            ImageTypeCategory.PORTRAIT -> {
                matched.addAll(allPresets.filter { it.category == com.example.data.model.MotionCategory.FACE })
            }
            ImageTypeCategory.NATURE -> {
                matched.addAll(allPresets.filter { it.category == com.example.data.model.MotionCategory.ENVIRONMENT || it.category == com.example.data.model.MotionCategory.CAMERA })
            }
            ImageTypeCategory.ARTWORK -> {
                matched.addAll(allPresets.filter { it.category == com.example.data.model.MotionCategory.ARTISTIC })
            }
            ImageTypeCategory.VEHICLES -> {
                matched.addAll(allPresets.filter { it.id == "cyberpunk_neon" || it.id == "zoom_in" || it.id == "parallax_3d" })
            }
            else -> {
                matched.addAll(allPresets.take(4))
            }
        }

        return (matched + allPresets).distinctBy { it.id }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val scaled = if (bitmap.width > 800 || bitmap.height > 800) {
            val aspect = bitmap.width.toFloat() / bitmap.height.toFloat()
            if (aspect > 1) Bitmap.createScaledBitmap(bitmap, 800, (800 / aspect).toInt(), true)
            else Bitmap.createScaledBitmap(bitmap, (800 * aspect).toInt(), 800, true)
        } else bitmap

        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    }
}
