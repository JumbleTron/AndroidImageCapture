package com.kielniakodu.imagecapture.di

import android.content.Context
import androidx.room.Room
import com.kielniakodu.imagecapture.data.local.ImageCaptureDatabase
import com.kielniakodu.imagecapture.data.local.ImageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ImageCaptureDatabase {
        return Room.databaseBuilder(
            context,
            ImageCaptureDatabase::class.java,
            "image_capture_database"
        ).build()
    }

    @Provides
    fun provideImageDao(database: ImageCaptureDatabase): ImageDao {
        return database.imageDao()
    }
}
