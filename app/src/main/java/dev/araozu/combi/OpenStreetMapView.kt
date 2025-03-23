package dev.araozu.combi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

@Composable
fun OpenStreetMapView(
    modifier: Modifier = Modifier,
    onMapViewCreated: (MapView) -> Unit = {},
    locationUpdateMinTime: Long = 5000,
    locationUpdateMinDistance: Float = 0f,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Set user agent
    Configuration.getInstance().apply {
        userAgentValue = context.packageName
        tileDownloadThreads = 8
        tileDownloadMaxQueueSize = 128
        osmdroidTileCache = File(context.cacheDir, "osmdroid")
        expirationOverrideDuration = 1000L * 60 * 60 * 24 * 7 // Cache for a week
    }

    val mapView = remember {
        MapView(context).apply {
            // Configure custom tile source
            setTileSource(createCustomTileSource())
            // Basic configuration
            setMultiTouchControls(true)
            controller.setZoom(15.0)

            // Improve rendering
            isTilesScaledToDpi = true  // This is the key setting for proper scaling
            setUseDataConnection(true)
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
        }
    }

    // Create location overlay
    val myLocationOverlay = remember {
        val locationProvider = object : GpsMyLocationProvider(context) {
            init {
                this.locationUpdateMinTime = locationUpdateMinTime
                this.locationUpdateMinDistance = locationUpdateMinDistance
            }

            override fun onLocationChanged(location: android.location.Location) {
                super.onLocationChanged(location)

                // send to server
                scope.launch(Dispatchers.IO) {
                    sendCoordinates(location.latitude, location.longitude)
                }
            }
        }

        MyLocationNewOverlay(locationProvider, mapView).apply {
            enableMyLocation()
            enableFollowLocation()
            setPersonAnchor(0.5f, 0.5f)
        }
    }

    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        mapView.overlays.add(myLocationOverlay)

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            myLocationOverlay.disableMyLocation()
        }
    }

    AndroidView(
        factory = { mapView.also(onMapViewCreated) },
        modifier = modifier
    )
}

private fun createCustomTileSource() = object : OnlineTileSourceBase(
    "CombiTiles",
    0, 18, 256, ".jpg",
    arrayOf("https://combi.araozu.dev")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        val url = "$baseUrl/tiles/$zoom/$x/$y$mImageFilenameEnding"
        return url
    }
}
