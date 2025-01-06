package com.mun.bonecci.photopicker

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel(), ProcessedListener {
    private val _currentImage = MutableLiveData<Bitmap>()
    val currentImage: LiveData<Bitmap> = _currentImage

    private val _selectedMode = MutableLiveData<DisplayMode>()
    val selectedMode: LiveData<DisplayMode> = _selectedMode

    private val _started = MutableLiveData<Boolean>()
    val started: LiveData<Boolean> = _started

    var choseFront = true
    var isInitialized = false

    private var _foregroundImage: Bitmap? = null
    private var _maskImage: Bitmap? = null
    private var _maskBgImage: Bitmap? = null
    private var _bgImage: Bitmap? = null

    private var _segmentHelper: SegmentHelper = SegmentHelper(this)

    init {
        // At startup, normal mode is selected
        _selectedMode.value = DisplayMode.NORMAL
    }


    fun resizeBitmap(bmp: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bmp, width, height, false)
    }


    fun imageChosen(bmp: Bitmap) {
        Log.d("MainViewModel", "imageChosen")
        if (choseFront) {
            _foregroundImage = bmp
            _bgImage = _foregroundImage?.let { _bgImage?.let { bg -> resizeBitmap(bg, it.width, it.height) } }
            _foregroundImage?.let { _segmentHelper.processImage(it) }
        } else {
            _bgImage = bmp
            _maskBgImage = _foregroundImage?.let { fg -> _bgImage?.let { bg -> _segmentHelper.generateMaskBgImage(fg, bg) } }
            setCurrentImage()
        }
    }

    private fun setCurrentImage() {
        _currentImage.value = when (_selectedMode.value) {
            DisplayMode.NORMAL -> {
                Log.d("MainViewModel", "Displaying normal image")
                _foregroundImage
            }
            DisplayMode.MASK -> {
                Log.d("MainViewModel", "Displaying mask image")
                _maskImage
            }
            DisplayMode.CUSTOM_BG -> {
                Log.d("MainViewModel", "Displaying image with custom background")
                _maskBgImage
            }
            else -> {
                Log.e("MainViewModel", "Invalid mode selected: ${_selectedMode.value}")
                _foregroundImage
            }
        }
    }

    fun modeSelected(mode: DisplayMode) {
        _selectedMode.value = mode
        Log.d("MainViewModel", "modeSelected")
        setCurrentImage()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun imageProcessed() {
        _started.value = true
        _foregroundImage?.let {
            Log.d("MainViewModel", "imageProcessed.1 - start")
            _maskImage = _segmentHelper.generateMaskImage(it)
            Log.d("MainViewModel", "imageProcessed.2")
            _maskBgImage = _bgImage?.let { bg -> _segmentHelper.generateMaskBgImage(it, bg) }
            Log.d("MainViewModel", "imageProcessed.3")
            setCurrentImage()
            Log.d("MainViewModel", "imageProcessed.4 - end")
        }
        _started.value = false
    }
}