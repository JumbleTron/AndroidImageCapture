package com.kielniakodu.imagecapture.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: Image)

    @Query("DELETE FROM images")
    suspend fun deleteAll()

    @Query("SELECT * FROM images")
    fun getAllImages(): Flow<List<Image>>
}
