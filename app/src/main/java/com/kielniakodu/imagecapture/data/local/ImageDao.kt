package com.kielniakodu.imagecapture.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: Image)

    @Delete
    suspend fun delete(image: Image)

    @Query("SELECT * FROM images")
    fun getAllImages(): Flow<List<Image>>
}
