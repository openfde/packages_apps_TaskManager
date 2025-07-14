package com.example.taskmanager.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.taskmanager.R
import com.example.taskmanager.TaskManagerBinder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FoldableBox(
    title: String,
    content: @Composable () -> Unit
) {
    val expanded = remember { mutableStateOf(true) }
    Row(
        modifier = Modifier
            .padding(10.dp)
            .clickable(
                onClick = {
                    expanded.value = !expanded.value
            })
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(
                id = if (expanded.value) R.drawable.down_vector else R.drawable.right_vector
            ),
            modifier = Modifier.size(16.dp),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(title)
    }
    if (expanded.value)
        content()
}

@Composable
fun CPUUsagesAnnotationsLine(colors: List<Color>, annotations: List<String>) {
    Row(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in colors.indices) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = colors[i],
                            shape = RoundedCornerShape(3.dp)
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                val annotation = annotations.getOrNull(i)
                if (annotation != null) Text(text = annotation)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ResourceView() {
    val coroutineScope = rememberCoroutineScope()
    val cpuPercentState = remember {
        List(4) { mutableStateListOf<Float>() }.toMutableStateList()
    }
    val context = LocalContext.current
    val cpuColors = context.resources.getIntArray(R.array.cpu_color_array).map { Color(it) }
    val currentCPUStringState = remember { mutableStateListOf<String>("", "", "", "") }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (true) {
                val eachCPUPercent = TaskManagerBinder.getEachCPUPercent(100) // [0,1,2,3]
                currentCPUStringState.clear()
                eachCPUPercent.forEachIndexed { index, it ->
                    if (cpuPercentState[index].size > 100) cpuPercentState[index].removeFirst()
                    cpuPercentState[index].add(it)
                    currentCPUStringState.add("CPU${index + 1}: %03.1f%%".format(it))
                }
                val memoryInfo = TaskManagerBinder.getMemoryAndSwap()
                delay(100)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        FoldableBox("CPU") {
            SmoothBezierLineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(10.dp),
                allValues = cpuPercentState,
                strokeWidth = 1f,
                maxValue = 100f,
                minValue = 0f
            )
            CPUUsagesAnnotationsLine(
                colors = cpuColors,
                annotations = currentCPUStringState
            )
        }
        FoldableBox("内存和交换") {

        }
    }
}

@Composable
fun SmoothBezierLineChart(
    modifier: Modifier = Modifier,
    allValues: List<List<Float>>,
    colors: List<Color> = listOf<Color>(
        Color(0xff8979FF),
        Color(0xffF5776E),
        Color(0xffFFAE4C),
        Color(0xff3CC3DF),
    ),
    strokeWidth: Float = 4f,
    minValue: Float = 0f,
    maxValue: Float = 1f
) {
    Canvas(modifier = modifier) {
        val referenceLineCount = 5
        val referenceLineColor = Color(0x3300001A)
        val labelPaint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            textSize = 10f
            color = android.graphics.Color.argb(0xff, 0x47, 0x47, 0x47)
        }
        repeat(referenceLineCount) { i ->
            val ratio = i / (referenceLineCount - 1f)
            val y = size.height - ratio * size.height
            drawLine(
                color = referenceLineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 0.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)
            )

            val textWidth = labelPaint.measureText("${(ratio * 100).toInt()}%")
            drawContext.canvas.nativeCanvas.drawText(
                "${(ratio * 100).toInt()}%",
                size.width - textWidth - 4f, // 右侧对齐，距离右边留4f间距
                y,
                labelPaint
            )
        }
        val lineBrushes = colors.map { it -> SolidColor(it) }
        allValues.forEachIndexed { index, values ->
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

            drawPath(
                path = path,
                brush = lineBrushes[index % colors.size],
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

