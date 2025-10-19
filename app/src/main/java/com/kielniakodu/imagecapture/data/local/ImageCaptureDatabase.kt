package com.kielniakodu.imagecapture.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Image::class], version = 1)
abstract class ImageCaptureDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
}
