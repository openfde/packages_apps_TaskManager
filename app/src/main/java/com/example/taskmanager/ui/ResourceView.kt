package com.example.taskmanager.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.PathEffect
import com.example.taskmanager.TaskManagerBinder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ResourceView() {
    val coroutineScope = rememberCoroutineScope()
    val floatList = remember { mutableStateListOf<Float>() }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (true) {
                val eachCPUPercent = TaskManagerBinder.getEachCPUPercent(100)
                val firstCPUPercent = eachCPUPercent[0]
                if (floatList.size == 100) floatList.removeFirst()
                floatList.add(firstCPUPercent / 100f)
                delay(100)
            }
        }
    }

    SmoothBezierLineChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(10.dp),
        values = floatList,
        strokeWidth = 1f
    )
}

@Composable
fun SmoothBezierLineChart(
    modifier: Modifier = Modifier,
    values: List<Float>,
    lineBrush: Brush = SolidColor(Color(0xff8979FF)),
    strokeWidth: Float = 4f,
    minValue: Float = 0f,
    maxValue: Float = 1f
) {
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val width = size.width
        val height = size.height
        val range = maxValue - minValue

        val xStep = width / (values.size - 1f)
        val points = values.mapIndexed { i, v ->
            Offset(
                x = i * xStep,
                y = height - (v - minValue) / range * height
            )
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p3 = points[i + 1]
                val cp1 = Offset(
                    x = p0.x + (p3.x - p0.x) / 2f,
                    y = p0.y
                )
                val cp2 = Offset(
                    x = p0.x + (p3.x - p0.x) / 2f,
                    y = p3.y
                )
                cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p3.x, p3.y)
            }
        }

        val referenceLineCount = 5
        val referenceLineColor = Color(0x3300001A)

        repeat(referenceLineCount) { i ->
            val ratio = i / (referenceLineCount - 1f)
            val y = height - ratio * height
            drawLine(
                color = referenceLineColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 0.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f) // 虚线样式
            )
        }

        drawPath(
            path = path,
            brush = lineBrush,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
