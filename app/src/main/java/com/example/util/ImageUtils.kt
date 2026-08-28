package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val directory = File(context.filesDir, "cargo_images").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "cargo_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String? {
        return try {
            val directory = File(context.filesDir, "cargo_images").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "cargo_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)

            val outputStream = FileOutputStream(file)
            outputStream.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
