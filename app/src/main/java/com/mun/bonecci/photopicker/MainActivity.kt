package com.mun.bonecci.photopicker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mun.bonecci.photopicker.components.GetImageFromGallery
import com.mun.bonecci.photopicker.ui.theme.PhotoPickerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhotoPickerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GetImageFromGallery(viewModel = mainViewModel)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GetImageFromGalleryPreview() {
    PhotoPickerTheme {
        GetImageFromGallery()
    }
}