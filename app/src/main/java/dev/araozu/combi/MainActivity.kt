package dev.araozu.combi

import OpenStreetMapView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.araozu.combi.ui.theme.CombiTheme
import org.osmdroid.util.GeoPoint

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CombiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OpenStreetMapView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        onMapViewCreated = { mapView ->
                            // Set initial position (example coordinates)
                            mapView.controller.setCenter(GeoPoint(-16.45, -71.54))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CombiTheme {
        Greeting("Android")
    }
}