package com.kielniakodu.imagecapture.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.kielniakodu.imagecapture.data.local.Image
import com.kielniakodu.imagecapture.ui.viewmodel.ImagePickerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerScreen(modifier: Modifier = Modifier) {
    val viewModel: ImagePickerViewModel = hiltViewModel()
    val context = LocalContext.current

    val displayedImageUri by viewModel.displayedImageUri.collectAsStateWithLifecycle()
    val savedInfo by viewModel.savedInfo.collectAsStateWithLifecycle()
    var showOptionsDialog by remember { mutableStateOf(false) }

    val allMedia by viewModel.allMedia.collectAsStateWithLifecycle()

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
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
        Row {
            Button(onClick = { showOptionsDialog = true }) {
                Text("Wybierz lub zrób zdjęcie")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.removeImages() }) {
                Text("Usuń zdjęcia")
            }
        }

        if (showOptionsDialog) {
            Dialog(onDismissRequest = { showOptionsDialog = false }) {
                Card {
                    Column {
                        ListItem(
                            headlineContent = { Text("Wybierz z galerii") },
                            modifier = Modifier.clickable {
                                pickMediaLauncher.launch(arrayOf("image/*"))
                                showOptionsDialog = false
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Zrób zdjęcie") },
                            modifier = Modifier.clickable {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                showOptionsDialog = false
                            }
                        )
                    }
                }
            }
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

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp), // Ograniczenie wysokości siatki
            contentPadding = PaddingValues(8.dp)
        ) {
            items(allMedia) { mediaItem ->
                MediaThumbnail(media = mediaItem, onClick = { viewModel.onMediaSelected(mediaItem) })
            }
        }
    }
}

@Composable
fun MediaThumbnail(media: Image, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .background(Color.Gray.copy(alpha = 0.1f))
            .border(1.dp, Color.Gray)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = media.filePath,
            contentDescription = "Miniatura",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = painterResource(id = android.R.drawable.ic_menu_report_image)
        )
    }
}
