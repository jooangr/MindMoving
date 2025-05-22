package com.example.mindmoving.views.controlCoche

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mindmoving.R
import kotlinx.coroutines.delay

@Composable
fun ControlCocheScreen(navController: NavHostController) {
    LockToLandscapeEffect()

    var showInstructions by remember { mutableStateOf(true) }
    var showLoading by remember { mutableStateOf(false) }
    var conectado by remember { mutableStateOf(false) }
    var iniciarConexion by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }



    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Image(
            painter = painterResource(id = R.drawable.fondo_5),
            contentDescription = "Fondo de pantalla",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )



        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            LaunchedEffect(iniciarConexion) {
                if (iniciarConexion) {
                    delay(30_000L) // Esperar 30 segundos
                    if (!conectado) {
                        showLoading = false
                        showError = true
                    }
                }
            }

            if (iniciarConexion && showLoading.not()) {
                CameraStreamView(
                    onStreamLoaded = {
                        conectado = true
                        showLoading = false
                    }
                )
            }

        }

        IconButton( onClick = { navController.popBackStack() } ) {
            Icon(
                painterResource(
                id = R.drawable.baseline_arrow_back_ios_new_24),
                contentDescription = "Back"
            )
        }

        // Pop-up 1: Instrucciones
        if (showInstructions) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    Button(onClick = {
                        showInstructions = false
                        iniciarConexion = true
                        showLoading = true

                    }) {
                        Text("Aceptar")
                    }
                },
                title = { Text("Instrucciones de conexión") },
                text = {
                    Text("1. Conéctate a la red WiFi proporcionado por el dispositivo. \n\n 2. Asegúrese de apagar los datos móviles. \n" +
                            "\n 3. Si estás cerca de otras redes conocidas (como la de casa), considera “olvidarlas” temporalmente. \n" +
                            "\n 4. Permita que la aplicación acceda a la red. ",)
                }
            )
        }

        // Pop-up 2: Esperando conexión
        if (showLoading) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {},
                title = { Text("") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Conectando al coche...")
                    }
                }
            )
        }

        // Pop-up 3: Esperando conexión
        if (showError) {
            AlertDialog(
                onDismissRequest = { showError = false },
                dismissButton = {
                    Button(onClick = {
                        navController.popBackStack()
                    }) {
                        Text("Cerrar")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showError = false
                        iniciarConexion = false
                        showInstructions = true
                    }) {
                        Text("Reintentar")
                    }
                },
                title = { Text("Error de conexión") },
                text = { Text("No se pudo establecer conexión con el coche. Verifica la red WiFi.") }
            )
        }

    }

}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CameraStreamView(
    streamUrl: String = "http://192.168.4.1:81/stream",
    onStreamLoaded : () -> Unit
) {

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.userAgentString = "Mozilla/5.0"
                loadUrl(streamUrl)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.d("WebViewDebug", "onPageFinished for URL: $url")
                        if (url == streamUrl) {
                            onStreamLoaded()
                        }
                    }

                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError
                    ) {
                        // Solo mostramos el error por el logcat
                        Log.e("WebViewError", "Error: ${error.description} (code ${error.errorCode})")

                    }

                }
                loadUrl(streamUrl)
            }

        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .border(1.dp, Color.White.copy(alpha = 0.2f))
    )

}

@Composable
fun LockToLandscapeEffect() {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ControlCocheScreenPreview() {
    // Puedes usar un NavHostController falso si no necesitas navegación
    ControlCocheScreen(navController = rememberNavController())
}
