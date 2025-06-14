package com.example.mindmoving.graficas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

@Composable
fun SimpleBarChart(
    title: String,
    values: List<Float>,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

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
                        .height(value.dp * 2)
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
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    val maxValue = values.maxOrNull() ?: 1f
    val minValue = values.minOrNull() ?: 0f

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))


        val axisColor = MaterialTheme.colorScheme.onBackground
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.13f))
        ) {
            val canvasHeight = size.height
            val canvasWidth = size.width
            val pointSpacing = canvasWidth / (values.size - 1).coerceAtLeast(1)

            val points = values.mapIndexed { index, value ->
                val scaledY = canvasHeight - ((value - minValue) / (maxValue - minValue) * canvasHeight)
                Offset(index * pointSpacing, scaledY)
            }

            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 4f
                )
            }

            points.forEach { point ->
                drawCircle(color = lineColor, radius = 6f, center = point)
            }

            drawLine(
                color = axisColor,
                start = Offset(0f, canvasHeight),
                end = Offset(canvasWidth, canvasHeight),
                strokeWidth = 2f
            )
        }
    }


}

@Composable
fun MetricCard(
    title: String,
    icon: String,
    value: Float,
    change: Float,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$icon $title", style = MaterialTheme.typography.titleMedium)
            Column(horizontalAlignment = Alignment.End) {
                Text("${"%.1f".format(value)}", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = if (change >= 0) "↑ ${"%.1f".format(change)}" else "↓ ${"%.1f".format(-change)}",
                    color = if (change >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun LineChartWithLabels(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    val maxValue = values.maxOrNull() ?: 1f
    val minValue = values.minOrNull() ?: 0f
    val steps = 4

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 8.dp)
    ) {
        val chartHeight = size.height
        val chartWidth = size.width
        val stepX = chartWidth / (values.size - 1).coerceAtLeast(1)
        val stepY = chartHeight / steps

        val points = values.mapIndexed { index, value ->
            val y = chartHeight - ((value - minValue) / (maxValue - minValue) * chartHeight)
            Offset(index * stepX, y)
        }

        // Líneas horizontales
        for (i in 0..steps) {
            val y = i * stepY
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 1f
            )
        }

        // Línea del gráfico
        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 4f
            )
        }

        // Puntos
        points.forEach {
            drawCircle(lineColor, radius = 6f, center = it)
        }

        // Etiquetas en eje X
        labels.forEachIndexed { i, label ->
            drawContext.canvas.nativeCanvas.drawText(
                label,
                i * stepX,
                chartHeight + 30f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 30f
                }
            )
        }
    }
}

