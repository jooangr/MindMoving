package com.example.mindmoving.graficas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SimpleBarChart(
    title: String,
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.White)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            values.forEach { value ->
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(value.dp * 2) // Escalado visual
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun SimpleLineChartPlano(
    title: String,
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color.Cyan
) {
    val maxValue = values.maxOrNull() ?: 1f
    val minValue = values.minOrNull() ?: 0f
    val verticalPadding = 16.dp
    val horizontalSpacing = 60f

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0x22222222))) {

            val canvasHeight = size.height
            val canvasWidth = size.width
            val pointSpacing = canvasWidth / (values.size - 1).coerceAtLeast(1)

            val points = values.mapIndexed { index, value ->
                val scaledY = canvasHeight - ((value - minValue) / (maxValue - minValue) * canvasHeight)
                Offset(index * pointSpacing, scaledY)
            }

            // Dibujar líneas entre puntos
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 4f
                )
            }

            // Dibujar puntos
            points.forEach { point ->
                drawCircle(color = lineColor, radius = 6f, center = point)
            }

            // Dibujar ejes base (opcional)
            drawLine(
                color = Color.White,
                start = Offset(0f, canvasHeight),
                end = Offset(canvasWidth, canvasHeight),
                strokeWidth = 2f
            )
        }
    }
}

