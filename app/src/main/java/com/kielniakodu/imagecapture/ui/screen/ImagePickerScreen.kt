package com.kielniakodu.imagecapture.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.kielniakodu.imagecapture.ui.viewmodel.ImagePickerViewModel

@Composable
fun ImagePickerScreen(modifier: Modifier = Modifier) {
    val viewModel: ImagePickerViewModel = hiltViewModel()
    val context = LocalContext.current

    val displayedImageUri by viewModel.displayedImageUri.collectAsStateWithLifecycle()
    val savedInfo by viewModel.savedInfo.collectAsStateWithLifecycle()

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.saveImageFromGallery(context, uri)
            } else {
                viewModel.setInfoMessage("Nie wybrano obrazu")
            }
        }
    )

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                viewModel.saveImageFromCamera(context)
            } else {
                viewModel.setInfoMessage("Błąd podczas robienia zdjęcia")
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val tempUri = viewModel.createTempImageUri(context)
                if (tempUri != null) {
                    takePictureLauncher.launch(tempUri)
                } else {
                    viewModel.setInfoMessage("Błąd tworzenia pliku tymczasowego")
                }
            } else {
                viewModel.setInfoMessage("Brak uprawnień do aparatu")
            }
        }
    )

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Button(onClick = {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Text("Wybierz z galerii")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }) {
            Text("Zrób zdjęcie")
        }

        Spacer(modifier = Modifier.height(24.dp))

        AsyncImage(
            model = displayedImageUri,
            contentDescription = "Wybrany obraz",
            modifier = Modifier
                .size(300.dp)
                .background(Color.Gray.copy(alpha = 0.1f))
                .border(1.dp, Color.Gray),
            contentScale = ContentScale.Crop,
            error = painterResource(id = android.R.drawable.ic_menu_gallery)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = savedInfo ?: "Wybierz lub zrób zdjęcie.")
    }
}
