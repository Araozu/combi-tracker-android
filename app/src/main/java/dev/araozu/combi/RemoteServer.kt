package dev.araozu.combi

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

private const val HOST = "http://192.168.1.115:8888"

private val httpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
}

suspend fun sendCoordinates(latitude: Double, longitude: Double, clientSkew: Long) {
    Log.d("HTTP", "Sending coordinates: $latitude, $longitude")
    val time = System.currentTimeMillis() + clientSkew

    withContext(Dispatchers.IO) {
        try {
            val url = "${HOST}/track?lat=$latitude&long=$longitude&time=$time"
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

suspend fun getServerTime(): Long {
    return withContext(Dispatchers.IO) {
        try {
            val url = "${HOST}/timestamp"
            val req = Request.Builder()
                .url(url)
                .get()
                .build()

            httpClient.newCall(req).execute().use { res ->
                if (!res.isSuccessful) {
                    Log.e("HTTP", "Failed to get timestamp. HTTP ${res.code}")
                    val txt = res.body?.string() ?: "--no response--"
                    Log.e("HTTP", "Response: $txt")

                    return@withContext 0L
                } else {
                    val txt = res.body?.string()

                    if (txt == null) {
                        Log.e("HTTP", "Failed to get timestamp. No response body")
                        return@withContext 0L
                    }

                    val time = txt.toLongOrNull()
                    if (time == null) {
                        Log.e("HTTP", "Failed to get timestamp. Invalid response body $txt")
                        return@withContext 0L
                    }

                    return@withContext time
                }
            }
        } catch (e: Exception) {
            Log.e("HTTP", "Failed to fetch timestamp", e)

            return@withContext 0L
        }
    }
}

suspend fun computeClockOffset(): Long {
    val t0 = System.currentTimeMillis()
    val serverTime = getServerTime() // Make a request to get server time
    val t1 = System.currentTimeMillis()
    val rtt = t1 - t0
    // Assuming minimal processing time on server, one-way latency is RTT/2
    val networkLatency = rtt / 2
    // Clock offset is how much client clock differs from server
    val clockOffset = serverTime - (t0 + networkLatency)

    Log.d("CLOCK", "t0 = $t0")
    Log.d("CLOCK", "t1 = $t1")
    Log.d("CLOCK", "rtt = $rtt")
    Log.d("CLOCK", "networkLatency = $networkLatency")
    Log.d("CLOCK", "serverTime = $serverTime")
    Log.d("CLOCK", "clockOffset = $clockOffset")

    return clockOffset
}
