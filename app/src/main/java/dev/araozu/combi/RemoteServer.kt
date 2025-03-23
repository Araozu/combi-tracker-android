package dev.araozu.combi

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

private val httpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
}

suspend fun sendCoordinates(latitude: Double, longitude: Double) {
    Log.d("HTTP", "Sending coordinates: $latitude, $longitude")
    val time = System.currentTimeMillis()

    withContext(Dispatchers.IO) {
        try {
            val url = "http://192.168.1.115:8888/track?lat=$latitude&long=$longitude&time=$time"
            val req = Request.Builder()
                .url(url)
                .get()
                .build()

            httpClient.newCall(req).execute().use { res ->
                if (!res.isSuccessful) {
                    Log.e("HTTP", "Failed to send coordinates")
                    Log.e("HTTP", "Response status: ${res.code}")
                    val txt = res.body?.string() ?: "NO RESPONSE!!!"
                    Log.e("HTTP", "Response: $txt")
                } else {
                    val txt = res.body?.string() ?: "NO RESPONSE!!!"
                    Log.d("HTTP", txt)
                }
            }
        } catch (e: Exception) {
            Log.e("HTTP", "Failed to send coordinates", e)
        }
    }
}
