package com.mun.bonecci.photopicker.components

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrowseGallery
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mun.bonecci.photopicker.ui.theme.dimen_10dp
import com.mun.bonecci.photopicker.ui.theme.dimen_14dp
import com.mun.bonecci.photopicker.ui.theme.dimen_16dp
import com.mun.bonecci.photopicker.ui.theme.dimen_18dp
import com.mun.bonecci.photopicker.ui.theme.dimen_200dp
import com.mun.bonecci.photopicker.utils.UriUtils
import fr.geoking.wineocr.ui.TextRecognitionScreen
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


/**
 * A composable function that displays an image loaded from the gallery and provides a button to select
 * an image from the gallery. Uses the PickVisualMedia activity result launcher to handle image selection.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GetImageFromGallery() {
    // Retrieve the current context using LocalContext.current
    val context = LocalContext.current

    // Create a remembered variable to store the loaded image bitmap
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Create a remembered variable to track whether an image is loaded
    var isImageLoaded by remember { mutableStateOf(false) }

    lateinit var currentPhotoPath: String

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)
    if (!cameraPermission.status.isGranted) {
        LaunchedEffect(cameraPermission) {
            cameraPermission.launchPermissionRequest()
        }
    }

    // Create an activity result launcher for picking visual media (images in this case)
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                // Grant read URI permission to access the selected URI
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flag)

                // Convert the URI to a Bitmap and set it as the imageBitmap
                imageBitmap = UriUtils().uriToBitmap(context, it)?.asImageBitmap()

                // Set isImageLoaded to true
                isImageLoaded = true
            }
        }

    fun getCameraFile(): Uri {
        val directory = context.filesDir
//        val directory = File(context.filesDir, "camera_images")
//        if (!directory.exists()) {
//            directory.mkdirs()
//        }
        val file = File(directory, "${Calendar.getInstance().timeInMillis}.png")
        Log.d("GetImageFromGallery", "File path: ${file.absolutePath}")
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file)
    }

    @Throws(IOException::class)
    fun createImageFile(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val file = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).let { storageDir ->
             File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            ).apply {
                // Save a file: path for use with ACTION_VIEW intents
                currentPhotoPath = absolutePath
            }
        }
        Log.d("createImageFile", "File path: ${file.absolutePath}")
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file)
    }

    fun createImageFile2(): Uri {
        val storageDir: File? = context.getExternalFilesDir(null)
        val imageFile = File.createTempFile("JPEG_${System.currentTimeMillis()}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
    }

    val pickCamera =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
            isImageLoaded = taken
            imageUri?.let {
                imageBitmap = UriUtils().uriToBitmap(context, it)?.asImageBitmap()
            }
        }


    Column(
             modifier = Modifier
                 .fillMaxSize()
                 .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isImageLoaded) {
            imageBitmap?.let {
                Card(
                    shape = RoundedCornerShape(dimen_14dp),
                    modifier = Modifier.padding(dimen_10dp, dimen_16dp, dimen_10dp, dimen_16dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = dimen_10dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {

                    Image(
                        bitmap = it,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .height(dimen_200dp)
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    bottomStart = dimen_18dp,
                                    bottomEnd = dimen_18dp
                                )
                            )
                    )
                }
                Card(
                    shape = RoundedCornerShape(dimen_14dp),
                    modifier = Modifier.padding(dimen_10dp, dimen_16dp, dimen_10dp, dimen_16dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = dimen_10dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    TextRecognitionScreen(it.asAndroidBitmap())
                }
            }
        }

        Row (
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            TextButtonWithIcon(
                text = "Photo",
                icon = Icons.Outlined.CameraAlt,
                onClick = {
//                    imageUri = createImageFile()
                    imageUri = createImageFile2()
//                    imageUri = getCameraFile()
                    imageUri?.let {
                        pickCamera.launch(it)
                    }
                }
            )
            TextButtonWithIcon(
                text = "Gallery",
                icon = Icons.Outlined.BrowseGallery,
                onClick = {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
        }
        if (!isImageLoaded) {
            Row {
                TextButtonWithIcon(
                    text = "Retry",
                    onClick = {
                        isImageLoaded = false
                        imageBitmap = null
                    }
                )
            }
        }
    }
}

/**
 * A composable function that creates a button with an icon for selecting an image from the gallery.
 *
 * @param onClick The lambda expression that will be invoked when the button is clicked.
 */
@Composable
fun TextButtonWithIcon(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    Button(
        onClick = { onClick.invoke() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = "Gallery Icon"
                )
            }

            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = text)
        }
    }

}
