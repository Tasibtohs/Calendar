package com.example.util

import android.content.Context
import android.graphics.*
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

enum class PhotoFilterPreset(val displayName: String) {
    NATURAL("Natural"),
    WARM("Warm"),
    COOL("Cool"),
    BW("B&W"),
    VIVID("Vivid"),
    MUTED("Muted")
}

object ImageEditUtils {

    fun loadSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        reqWidth: Int = 1080,
        reqHeight: Int = 720
    ): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            var inSampleSize = 1
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun applyAdjustmentsAndFilter(
        source: Bitmap,
        rotationAngle: Float = 0f,
        brightness: Float = 0f,    // -100f .. 100f
        contrast: Float = 1f,      // 0.5f .. 1.5f
        saturation: Float = 1f,    // 0f .. 2f
        filterPreset: PhotoFilterPreset = PhotoFilterPreset.NATURAL
    ): Bitmap {
        // 1. Rotate if needed
        val rotated = if (rotationAngle % 360f != 0f) {
            val matrix = Matrix().apply { postRotate(rotationAngle) }
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        } else {
            source
        }

        val output = Bitmap.createBitmap(rotated.width, rotated.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        // Base ColorMatrix
        val cm = ColorMatrix()

        // 2. Saturation
        cm.setSaturation(saturation)

        // 3. Contrast & Brightness
        val scale = contrast
        val translate = brightness * 255f / 100f + (1f - scale) * 128f / 2f
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(contrastMatrix)

        // 4. Preset Color Tinting
        when (filterPreset) {
            PhotoFilterPreset.NATURAL -> { /* No-op */ }
            PhotoFilterPreset.WARM -> {
                val warmMatrix = ColorMatrix(
                    floatArrayOf(
                        1.15f, 0f, 0f, 0f, 10f,
                        0f, 1.05f, 0f, 0f, 5f,
                        0f, 0f, 0.85f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(warmMatrix)
            }
            PhotoFilterPreset.COOL -> {
                val coolMatrix = ColorMatrix(
                    floatArrayOf(
                        0.88f, 0f, 0f, 0f, -5f,
                        0f, 1.05f, 0f, 0f, 5f,
                        0f, 0f, 1.20f, 0f, 15f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(coolMatrix)
            }
            PhotoFilterPreset.BW -> {
                val bwMatrix = ColorMatrix()
                bwMatrix.setSaturation(0f)
                val bwContrast = ColorMatrix(
                    floatArrayOf(
                        1.1f, 0f, 0f, 0f, 5f,
                        0f, 1.1f, 0f, 0f, 5f,
                        0f, 0f, 1.1f, 0f, 5f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                bwMatrix.postConcat(bwContrast)
                cm.postConcat(bwMatrix)
            }
            PhotoFilterPreset.VIVID -> {
                val vividMatrix = ColorMatrix()
                vividMatrix.setSaturation(1.35f)
                val vividContrast = ColorMatrix(
                    floatArrayOf(
                        1.12f, 0f, 0f, 0f, 5f,
                        0f, 1.12f, 0f, 0f, 5f,
                        0f, 0f, 1.12f, 0f, 5f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                vividMatrix.postConcat(vividContrast)
                cm.postConcat(vividMatrix)
            }
            PhotoFilterPreset.MUTED -> {
                val mutedMatrix = ColorMatrix()
                mutedMatrix.setSaturation(0.75f)
                val mutedTone = ColorMatrix(
                    floatArrayOf(
                        0.95f, 0f, 0f, 0f, 15f,
                        0f, 0.95f, 0f, 0f, 15f,
                        0f, 0f, 0.95f, 0f, 15f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                mutedMatrix.postConcat(mutedTone)
                cm.postConcat(mutedMatrix)
            }
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(rotated, 0f, 0f, paint)

        if (rotated != source) {
            rotated.recycle()
        }

        return output
    }

    fun cropToSquare(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width == height) return bitmap

        val dimension = minOf(width, height)
        val xOffset = (width - dimension) / 2
        val yOffset = (height - dimension) / 2
        return Bitmap.createBitmap(bitmap, xOffset, yOffset, dimension, dimension)
    }

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, filename: String = "app_cover_photo.jpg"): Uri {
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            out.flush()
        }
        return Uri.fromFile(file)
    }
}
