package com.kielniakodu.imagecapture.ui.viewmodel

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kielniakodu.imagecapture.data.local.Image
import com.kielniakodu.imagecapture.data.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ImagePickerViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    private val _displayedImageUri = MutableStateFlow<Uri?>(null)
    val displayedImageUri = _displayedImageUri.asStateFlow()

    val allMedia: StateFlow<List<Image>> = repository.getAllImages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _savedInfo = MutableStateFlow<String?>(null)
    val savedInfo = _savedInfo.asStateFlow()

    private var tempImageUri: Uri? = null

    fun createTempImageUri(context: Context): Uri? {
        val file = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
        tempImageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.imageCaptureFileProvider",
            file
        )
        return tempImageUri
    }

    fun onMediaSelected(media: Image) {
        _displayedImageUri.value = media.filePath.toUri()
        _savedInfo.value = "Wybrano z bazy:GUID: ${media.id} ścieżka: ${media.filePath}"
    }

    fun saveImageFromGallery(context: Context, uri: Uri) {
        viewModelScope.launch {
            val galleryUri = saveImageToGallery(context, uri)
            if (galleryUri != null) {
                val image = Image(filePath = galleryUri.toString())
                repository.insert(image)
                _displayedImageUri.value = galleryUri
                _savedInfo.value = "Zapisano GUID: ${image.id} Ścieżka: ${image.filePath}"
            } else {
                _savedInfo.value = "Błąd zapisu obrazu w galerii"
            }
        }
    }

    fun saveImageFromCamera(context: Context) {
        tempImageUri?.let { uri ->
            viewModelScope.launch {
                val galleryUri = saveImageToGallery(context, uri)
                if (galleryUri != null) {
                    val image = Image(filePath = galleryUri.toString())
                    repository.insert(image)
                    _displayedImageUri.value = galleryUri
                    _savedInfo.value = "Zapisano GUID: ${image.id} Ścieżka: ${image.filePath}"
                    // Clean up the temporary file
                    context.contentResolver.delete(uri, null, null)
                    tempImageUri = null
                } else {
                    _savedInfo.value = "Błąd zapisu obrazu z aparatu w galerii"
                }
            }
        }
    }

    fun setInfoMessage(message: String) {
        _savedInfo.value = message
    }

    private fun saveImageToGallery(context: Context, sourceUri: Uri): Uri? {
        val resolver = context.contentResolver
        val inputStream = resolver.openInputStream(sourceUri) ?: return null

        val fileName = "${UUID.randomUUID()}.jpg"
        var outputStream: OutputStream? = null
        var galleryUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                galleryUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (galleryUri != null) {
                    outputStream = resolver.openOutputStream(galleryUri)
                    if (outputStream != null) {
                        inputStream.copyTo(outputStream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(galleryUri, contentValues, null, null)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                if (!imagesDir.exists()) imagesDir.mkdirs()
                val imageFile = File(imagesDir, fileName)
                outputStream = FileOutputStream(imageFile)
                inputStream.copyTo(outputStream)
                galleryUri = Uri.fromFile(imageFile)

                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = galleryUri
                context.sendBroadcast(mediaScanIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (galleryUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.delete(galleryUri, null, null)
            }
            return null
        } finally {
            inputStream.close()
            outputStream?.close()
        }
        return galleryUri
    }
}
