package com.example.mindmoving.views.controlCoche

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import android.view.View
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
import androidx.compose.runtime.key
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URLEncoder

@Composable
fun ControlCocheScreen(navController: NavHostController) {
    LockToLandscapeEffect()
    HideSystemBarsEffect()

    var showInstructions by remember { mutableStateOf(true) }
    var showLoading by remember { mutableStateOf(false) }
    var conectado by remember { mutableStateOf(false) }
    var iniciarConexion by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    //para darle una "identidad única" al WebView
    var streamKey by remember { mutableStateOf(0) }



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
            modifier = Modifier.fillMaxSize()
        ) {

            LaunchedEffect(iniciarConexion) {
                if (iniciarConexion) {
                    val timeout = 30_000L
                    val startTime = System.currentTimeMillis()

                    while (System.currentTimeMillis() - startTime < timeout) {
                        delay(200L) // Pequeña pausa para evitar consumir CPU
                        if (conectado) {
                            // Si se conectó, salir antes del timeout
                            return@LaunchedEffect
                        }
                    }
                    // Si después de 30s aún no está conectado, mostrar error
                    if (!conectado) {
                        showLoading = false
                        showError = true
                    }
                }
            }

            if (iniciarConexion //&& showLoading.not()
            ) {
                CameraStreamView(
                    visible = conectado,
                    onStreamLoaded = {
                        conectado = true
                        showLoading = false
                    },
                    keyValue = streamKey
                )
            }
        }
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.baseline_arrow_back_ios_new_24),
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {
                    val url = "http://192.168.4.1" // o con ruta si se requiere
                    val json = """{"N":3,"H":"0001","D1":1,"D2":150}""" // Adelante
                    val jsonEncoded = URLEncoder.encode(json, "UTF-8")
                    val ip = "192.168.4.1"
                    val puerto = 100
                    val comando = """"{"N":106,"D1":1}""""
                    //"""{"H":ID,"N":3,"D1":4,"D2":100}""" // Avanzar

                    //enviarComandoSocket("192.168.4.1", 80, json)
                    //sendTcpCommand("""{"command":"forward"}""", port = 100)
                    enviarComandoTCP(ip, puerto, comando)
                }) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Adelante", tint = Color.White, modifier = Modifier.size(48.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        val url = "http://192.168.4.1"
                        val json = """{"N":3,"H":"0003","D1":3,"D2":150}""" // Izquierda
                        val jsonEncoded = URLEncoder.encode(json, "UTF-8")
                        val comando = """{"N":3,"H":"0004","D1":3,"D2":150}""" // Izquierda

                        val ip = "192.168.4.1"
                        val puerto = 100

                        //enviarComandoSocket("192.168.4.1", 80, json)
                        //sendTcpCommand("left", port = 100)
                        enviarComandoTCP(ip, puerto, comando)
                    }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Izquierda", tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.width(48.dp))
                    IconButton(onClick = {
                        val url = "http://192.168.4.1"
                        val json = """{"N":3,"H":"0004","D1":4,"D2":150}""" // Derecha
                        val jsonEncoded = URLEncoder.encode(json, "UTF-8")
                        val comando = """{"N":3,"D1":4,"D2":150}""" // Derecha

                        val ip = "192.168.4.1"
                        val puerto = 100

                        //enviarComandoSocket("192.168.4.1", 80, json)
                        //sendTcpCommand("right", port = 100)
                        enviarComandoTCP(ip, puerto, comando)
                    }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Derecha", tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                }

                IconButton(onClick = {
                    val url = "http://192.168.4.1"
                    val json = """{"N":3,"H":"0002","D1":2,"D2":150}""" // Atrás
                    val jsonEncoded = URLEncoder.encode(json, "UTF-8")
                    val comando = """{"N":3,"D1":2,"D2":100}""" // Retroceder
                    val ip = "192.168.4.1"
                    val puerto = 100

                    //sendTcpCommand("backward", port = 100)
                    enviarComandoTCP(ip, puerto, comando)
                }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Atrás", tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }
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
                            "\n 3. Si estás cerca de otras redes conocidas (como la de casa), considera “olvidarlas” temporalmente. \n"
                    )
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
                        conectado = false
                        iniciarConexion = false
                        showInstructions = true
                        streamKey++ //Fuerza recrear el WebView en el próximo intento
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

/*
fun enviarComandoTCP(ip: String, puerto: Int, comando: String) {
    Thread {
        try {
            val socket = Socket(ip, puerto)
            val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.US_ASCII))
            writer.write(comando)  // Se agrega terminador de línea estándar
            writer.flush()
            writer.close()
            socket.close()
            Log.d("SocketCoche", "Comando enviado correctamente")
        } catch (e: Exception) {
            Log.e("SocketCoche", "Error al enviar comando: ${e.message}")
        }
    }.start()
}
*/


fun enviarComandoTCP(ip: String, puerto: Int, comando: String) {
    Thread {
        try {
            Log.d("SocketCoche", "Enviando: $comando a $ip:$puerto")
            val socket = Socket(ip, puerto)
            val output = socket.getOutputStream()
            output.write(comando.toByteArray(Charsets.UTF_8))
            output.flush()
            output.close()
            socket.close()
            Log.d("SocketCoche", "Comando enviado correctamente")
        } catch (e: Exception) {
            Log.e("SocketCoche", "Error al enviar comando: ${e.message}")
        }
    }.start()
}

/*
fun enviarComandoTCP(ip: String, puerto: Int, comando: String) {
    Thread {
        try {
            val socket = Socket(ip, puerto)
            val output = socket.getOutputStream()
            output.write(comando.toByteArray())
            output.flush()
            socket.close()
            Log.d("SocketCoche", "Comando enviado correctamente")
        } catch (e: Exception) {
            Log.e("SocketCoche", "Error al enviar comando: ${e.message}")
        }
    }.start()
}
 */


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CameraStreamView(
    streamUrl: String = "http://192.168.4.1:81/stream",
    onStreamLoaded : () -> Unit,
    visible: Boolean,
    keyValue: Int
) {

    val visibilityModifier = if (visible) {
        Modifier.fillMaxSize()
    } else {
        Modifier.size(1.dp) // prácticamente invisible
    }

    key(keyValue) { //Esto fuerza a Compose a crear uno nuevo si el valor cambia

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
                            Log.e(
                                "WebViewError",
                                "Error: ${error.description} (code ${error.errorCode})"
                            )

                        }
                    }
                    loadUrl(streamUrl)
                }

            },
            modifier = visibilityModifier,
        )
    }
}

@Composable
fun LockToLandscapeEffect() {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}

@Composable
fun HideSystemBarsEffect() {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        activity?.window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        onDispose {
            // Restaurar visibilidad al salir
            activity?.window?.decorView?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ControlCocheScreenPreview() {
    // Puedes usar un NavHostController falso si no necesitas navegación
    ControlCocheScreen(navController = rememberNavController())
}