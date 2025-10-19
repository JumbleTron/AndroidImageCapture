package com.kielniakodu.imagecapture.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "images")
data class Image(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val filePath: String
)
