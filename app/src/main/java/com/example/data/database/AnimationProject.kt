package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animation_projects")
data class AnimationProject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val imageUri: String,
    val categoryName: String,
    val motionPresetId: String,
    val motionPresetName: String,
    val aiEngineName: String,
    val durationSeconds: Float = 3.5f,
    val fps: Int = 30,
    val qualityResolution: String = "1080p",
    val isRestored: Boolean = false,
    val isColorized: Boolean = false,
    val isUpscaled: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
