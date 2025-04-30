package com.example.mindmoving

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun LoginView(){

    ContentLoginView()

}

@Composable
fun ContentLoginView(){

    var userdata by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var logoimg by remember { mutableStateOf(R.drawable.logo_mindmoving_preview) }

    Box(
        modifier = Modifier.fillMaxSize().padding(),
    ){
        Image(
            painter = painterResource(id = R.drawable.fondo_login),
            contentDescription = "Fondo de pantalla",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop

        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(

                ) {
                    Row {
                        Image(
                            painter = painterResource(id = logoimg),
                            contentDescription = "Imagen Logo",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .padding(30.dp)
                        )

                        Text( text = "MindMoving",
                            color = Color.White,
                            fontSize = 30.dp,
                            fontFamily = Inter,
                            )

                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        OutlinedTextField(value = userdata, onValueChange = {userdata = it},
                            label = { Text( text = "Correo electrónico o username") },
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 10.dp)
                        )



                    }
                }


            }

        }
    }

}