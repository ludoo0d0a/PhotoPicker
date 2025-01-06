package com.mun.bonecci.photopicker.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import com.mun.bonecci.photopicker.DisplayMode
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mun.bonecci.photopicker.MainViewModel
import org.koin.compose.viewmodel.koinViewModel
import java.time.Instant

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ImageSegmenter(
    viewModel: MainViewModel = koinViewModel(),
//    imageBitmap: ImageBitmap?
) {
    val context = LocalContext.current

    Log.d("ImageSegmenter", "Refresh ImageSegmenter")

    //TODO / refreshed too many times (cannot get vm + image)
//    imageBitmap?.let {
//        run { viewModel.imageChosen(it.asAndroidBitmap()) }
//    }

    // Observe LiveData as State in Compose
    val currentImage by viewModel.currentImage.observeAsState()
    val selectedMode by viewModel.selectedMode.observeAsState()
    val loading by viewModel.started.observeAsState()

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            run { viewModel.imageChosen(bitmap) }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = { galleryLauncher.launch("image/*") }) {
            Text("Select Image from Gallery")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = { viewModel.modeSelected(DisplayMode.NORMAL) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Normal Mode")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { viewModel.modeSelected(DisplayMode.MASK) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Mask Mode")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        currentImage?.let { image ->
            Text("Current Image in Mode: $selectedMode ${Instant.now()}")
            Spacer(modifier = Modifier.height(16.dp))

            loading?.let{
                if(it)
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                else
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .border(1.dp, Color.Red)
                    )
            }
        }
    }
}


//preview ImageSegmenter
@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun ImageSegmenterPreview() {
    ImageSegmenter()
}