package com.example.android_16.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

object HardwareHelper {

    fun crearUriFotoTemporal(context: Context): Uri {
        val archivo = File.createTempFile(
            "evidencia_${System.currentTimeMillis()}_",
            ".jpg",
            context.cacheDir
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
    }

    suspend fun uriABytesComprimidos(
        context: Context,
        uri: Uri,
        maxDimension: Int = 1280,
        calidadJpeg: Int = 75
    ): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val bitmapOriginal = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return@withContext null

            val bitmapFinal = redimensionarSiEsNecesario(bitmapOriginal, maxDimension)
            val output = ByteArrayOutputStream()
            bitmapFinal.compress(Bitmap.CompressFormat.JPEG, calidadJpeg, output)

            if (bitmapFinal != bitmapOriginal) {
                bitmapOriginal.recycle()
            }
            bitmapFinal.recycle()

            output.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    private fun redimensionarSiEsNecesario(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val nuevoAncho: Int
        val nuevoAlto: Int
        if (ratio > 1f) {
            nuevoAncho = maxDimension
            nuevoAlto = (maxDimension / ratio).toInt()
        } else {
            nuevoAlto = maxDimension
            nuevoAncho = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, nuevoAncho, nuevoAlto, true)
    }

    @SuppressLint("MissingPermission")
    suspend fun obtenerUbicacionActual(context: Context): Pair<Double, Double>? {
        return try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val tokenSource = CancellationTokenSource()
            val location = client.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                tokenSource.token
            ).await() ?: client.lastLocation.await()

            location?.let { Pair(it.latitude, it.longitude) }
        } catch (e: Exception) {
            null
        }
    }

    fun interpretarClima(weatherCode: Int): String = when (weatherCode) {
        0 -> "Despejado"
        1, 2, 3 -> "Parcialmente nublado"
        45, 48 -> "Neblina"
        51, 53, 55 -> "Llovizna"
        61, 63, 65 -> "Lluvia"
        71, 73, 75 -> "Nieve"
        80, 81, 82 -> "Chubascos"
        95, 96, 99 -> "Tormenta eléctrica"
        else -> "Clima variable"
    }
}