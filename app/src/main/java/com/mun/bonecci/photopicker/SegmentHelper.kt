package com.mun.bonecci.photopicker

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.graphics.ColorUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.Segmenter
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import java.nio.ByteBuffer

class SegmentHelper(private val listener: ProcessedListener) {
    private val segmenter: Segmenter
    private lateinit var maskBuffer: ByteBuffer
    private var maskWidth = 0
    private var maskHeight = 0

    init {
        val options = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .build()

        segmenter = Segmentation.getClient(options)
    }

    fun processImage(image: Bitmap) {
        val input = InputImage.fromBitmap(image, 0)
        segmenter.process(input)
            .addOnSuccessListener { segmentationMask ->
                maskBuffer = segmentationMask.buffer
                maskWidth = segmentationMask.width
                maskHeight = segmentationMask.height
                listener.imageProcessed()
            }
            .addOnFailureListener { e ->
                Log.e("SegmentHelper", "Image processing failed: $e")
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateMaskImage(image: Bitmap): Bitmap {
        val maskBitmap = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ARGB_8888)

        val tempBitmap = if (image.config == Bitmap.Config.HARDWARE) {
            image.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            image
        }

        for (y in 0 until maskHeight) {
            for (x in 0 until maskWidth) {
                if (x < tempBitmap.width && y < tempBitmap.height) {
                    val confidence = maskBuffer.float
                    val alpha = (confidence * 255).toInt()
                    val pixelColor = if (alpha > 0) tempBitmap.getPixel(x, y) else Color.TRANSPARENT
                    maskBitmap.setPixel(x, y, Color.argb(alpha, Color.red(pixelColor), Color.green(pixelColor), Color.blue(pixelColor)))
                }
            }
        }
        maskBuffer.rewind()

        return maskBitmap
    }
    fun generateMaskBgImage(image: Bitmap, bg: Bitmap): Bitmap {
        val bgBitmap = bg.copy(Bitmap.Config.ARGB_8888, true)

        for (y in 0 until maskHeight) {
            for (x in 0 until maskWidth) {
                if (x < bg.width && y < bg.height) {
                    val bgConfidence = ((1.0 - maskBuffer.float) * 2).toInt()
                    var bgPixel = bg.getPixel(x, y)
                    bgPixel = ColorUtils.setAlphaComponent(bgPixel, bgConfidence)
                    bgBitmap.setPixel(x, y, bgPixel)
                }
            }
        }
        maskBuffer.rewind()

        return mergeBitmaps(image, bgBitmap)
    }

    fun mergeBitmaps(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
        val merged = Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config!!)
        val canvas = Canvas(merged)
        canvas.drawBitmap(bmp1, Matrix(), null)
        canvas.drawBitmap(bmp2, Matrix(), null)
        return merged
    }


}

interface ProcessedListener {
    fun imageProcessed()
}