package com.kielniakodu.imagecapture.data.repository

import com.kielniakodu.imagecapture.data.local.Image
import com.kielniakodu.imagecapture.data.local.ImageDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ImageRepository @Inject constructor(private val imageDao: ImageDao)
{
    fun getAllImages(): Flow<List<Image>> = imageDao.getAllImages()

    suspend fun insert(image: Image) {
        imageDao.insert(image)
    }

    suspend fun delete(image: Image) {
        imageDao.delete(image)
    }
}
