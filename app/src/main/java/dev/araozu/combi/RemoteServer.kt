package dev.araozu.combi

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

fun sendCoordinates(latitude: Double, longitude: Double) {
    Log.d("HTTP", "Sending coordinates: $latitude, $longitude")

    val scope = CoroutineScope(Dispatchers.IO)
    scope.launch {
        try {
            val url = "http://192.168.1.115:8888/track?lat=$latitude&long=$longitude"
            val client = OkHttpClient()
            val req = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(req).execute().use { res ->
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
