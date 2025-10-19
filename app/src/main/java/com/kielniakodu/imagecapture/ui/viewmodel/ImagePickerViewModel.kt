package com.kielniakodu.imagecapture.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kielniakodu.imagecapture.data.local.Image
import com.kielniakodu.imagecapture.data.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ImagePickerViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    private val _displayedImageUri = MutableStateFlow<Uri?>(null)
    val displayedImageUri = _displayedImageUri.asStateFlow()

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

    fun saveImageFromGallery(context: Context, uri: Uri) {
        viewModelScope.launch {
            val newFile = copyFileToInternalStorage(context, uri)
            if (newFile != null) {
                val image = Image(filePath = newFile.absolutePath)
                repository.insert(image)
                _displayedImageUri.value = Uri.fromFile(newFile)
                _savedInfo.value = "Zapisano GUID: ${image.id}\nŚcieżka: ${image.filePath}"
            } else {
                _savedInfo.value = "Błąd zapisu obrazu"
            }
        }
    }

    fun saveImageFromCamera(context: Context) {
        tempImageUri?.let { uri ->
            viewModelScope.launch {
                val newFile = copyFileToInternalStorage(context, uri)
                if (newFile != null) {
                    val image = Image(filePath = newFile.absolutePath)
                    repository.insert(image)
                    _displayedImageUri.value = Uri.fromFile(newFile)
                    _savedInfo.value = "Zapisano GUID: ${image.id}\nŚcieżka: ${image.filePath}"
                } else {
                    _savedInfo.value = "Błąd zapisu obrazu"
                }
            }
        }
    }

    fun setInfoMessage(message: String) {
        _savedInfo.value = message
    }

    private fun copyFileToInternalStorage(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.filesDir, "${UUID.randomUUID()}.jpg")
            val outputStream = file.outputStream()
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
