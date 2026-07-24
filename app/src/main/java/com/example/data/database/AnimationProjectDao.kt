package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimationProjectDao {
    @Query("SELECT * FROM animation_projects ORDER BY timestamp DESC")
    fun getAllProjects(): Flow<List<AnimationProject>>

    @Query("SELECT * FROM animation_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): AnimationProject?

    @Query("SELECT * FROM animation_projects WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteProjects(): Flow<List<AnimationProject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: AnimationProject): Long

    @Update
    suspend fun updateProject(project: AnimationProject)

    @Query("DELETE FROM animation_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("DELETE FROM animation_projects")
    suspend fun deleteAllProjects()
}
