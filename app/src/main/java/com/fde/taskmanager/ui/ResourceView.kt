package com.fde.taskmanager.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fde.taskmanager.BackgroundTask
import com.fde.taskmanager.R
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun FoldableBox(
    title: String, content: @Composable () -> Unit
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
                color = Color.Transparent, shape = RoundedCornerShape(8.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(
                id = if (expanded.value) R.drawable.down_vector else R.drawable.right_vector
            ), modifier = Modifier.size(16.dp), contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(title)
    }
    if (expanded.value) content()
}

@Composable
fun CPUUsagesAnnotationsLine(
    colors: List<Color>,
    annotations: List<String>,
) {
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
                            color = colors[i], shape = RoundedCornerShape(3.dp)
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

@Composable
fun MemoryAndSwapAnnotationsLine(
    colors: List<Color>, annotations: List<String>, capcities: List<Float>
) {
    val barWidthTotal = 58.dp
    val barHeight = 16.dp
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
                        .width(barWidthTotal)
                        .height(barHeight)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xffEBEBEB))
                ) {
                    Row {
                        if (capcities[i] != 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(capcities[i])
                                    .background(colors[i])
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1 - capcities[i])
                                    .background(Color(0xffEBEBEB))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                val annotation = annotations.getOrNull(i)
                if (annotation != null) Text(text = annotation)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun NetworkAnnotationsLine(
    colors: List<Color>,
    annotations: List<String>,
) {
    Row(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconIDs = listOf<Int>(R.drawable.download_icon, R.drawable.upload_icon)
        for (i in colors.indices) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = iconIDs[i]),
                    modifier = Modifier.size(28.dp),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                val annotation = annotations.getOrNull(i)
                if (annotation != null) Text(text = annotation)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun DiskAnnotationsLine(
    colors: List<Color>,
    annotations: List<String>,
) {
    Row(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconIDs = listOf<Int>(R.drawable.disk_read_icon, R.drawable.disk_write_icon)
        for (i in colors.indices) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = iconIDs[i]),
                    modifier = Modifier.size(28.dp),
                    contentDescription = null
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
    val context = LocalContext.current

    val cpuCount = BackgroundTask.cpuCount
    val allCpuColors = context.resources.getIntArray(R.array.cpu_color_array).map { Color(it) }
    val cpuColors = if (cpuCount + 1 <= allCpuColors.size) {
        allCpuColors.take(cpuCount).map { it.copy(alpha = 0.5f) } + allCpuColors[cpuCount]
    } else {
        List(cpuCount + 1) { index ->
            if (index != cpuCount)
                allCpuColors[index % allCpuColors.size].copy(alpha = 0.5f)
            else allCpuColors[index % allCpuColors.size]
        }
    }
    val cpuPercentState = BackgroundTask.cpuPercentState.collectAsStateWithLifecycle()
    val memoryAndSwapList = BackgroundTask.memoryAndSwapList.collectAsStateWithLifecycle()
    val memoryAndSwap = BackgroundTask.memoryAndSwap.collectAsStateWithLifecycle()
    val networkDownloadAndUploadState =
        BackgroundTask.networkDownloadAndUploadList.collectAsStateWithLifecycle()
    val networkStatsState = BackgroundTask.networkStatsState.collectAsStateWithLifecycle()
//    val diskReadAndWriteState = remember {
//        List(2) { mutableStateListOf<Float>() }.toMutableStateList()
//    }

    val diskStatsState = BackgroundTask.diskStatsState.collectAsStateWithLifecycle()
    val diskReadAndWriteList = BackgroundTask.diskReadAndWriteList.collectAsStateWithLifecycle()
    val memoryAndSwapColors =
        context.resources.getIntArray(R.array.memory_swap_color_array).map { Color(it) }
    val networkColors = context.resources.getIntArray(R.array.network_color_array).map { Color(it) }
    val diskColors = context.resources.getIntArray(R.array.disk_color_array).map { Color(it) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(end = 16.dp)
                .verticalScroll(scrollState)) {
                FoldableBox("CPU") {
                    SmoothBezierLineChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(10.dp),
                        allValues = cpuPercentState.value,
                        colors = cpuColors,
                        strokeWidth = 1f,
                        maxValue = 100f,
                        minValue = 0f
                    )
                    CPUUsagesAnnotationsLine(
                        colors = cpuColors,
                        annotations = cpuPercentState.value.mapIndexed { index, coreValues ->
                            Log.d("cold", "cpuPercentState:$index")
                            Log.d("cold", "size:${cpuPercentState.value.size}")
                            if (index != (cpuPercentState.value.size - 1)) {
                                val latest = coreValues.lastOrNull() ?: 0f
                                "CPU${index + 1}: %03.1f%%".format(latest)
                            } else {
                                val latest = coreValues.lastOrNull() ?: 0f
                                "${context.getString(R.string.average)}:%03.1f%%".format(latest)
                            }
                        }
                    )
                }
                val memorySwapMax = remember { mutableStateOf(100f) }
                val memorySwapMin = remember { mutableStateOf(0f) }
                val memorySwapAxisLabels =
                    remember { mutableStateListOf("0%", "20%", "40%", "60%", "80%", "100%") }

                LaunchedEffect(memoryAndSwapList.value) {
                    val axisValues = listOf(0f, 10f, 20f, 30f, 40f, 50f, 60f, 70f, 80f, 90f, 100f)
                    val total = memoryAndSwapList.value[0]
                    val dataMin = total.minOrNull() ?: 0f
                    val dataMax = total.maxOrNull() ?: 100f
                    val alignedMin = axisValues.filter { it <= dataMin }.maxOrNull() ?: 0f
                    val alignedMax = axisValues.filter { it >= dataMax }.minOrNull() ?: 100f
                    memorySwapMin.value = alignedMin.coerceAtLeast(0f)
                    memorySwapMax.value = alignedMax.coerceAtMost(100f)
                    val labelCount = 6
                    for (index in 0..5) {
                        val value = memorySwapMin.value +
                                (memorySwapMax.value - memorySwapMin.value) *
                                index / (labelCount - 1)
                        memorySwapAxisLabels[index] = "${value.toInt()}%"
                    }
                }

                FoldableBox(context.getString(R.string.memory_and_swap)) {
                    SmoothBezierLineChart(
                        yAxisLabels = memorySwapAxisLabels,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(10.dp),
                        allValues = memoryAndSwapList.value,
                        colors = memoryAndSwapColors,
                        strokeWidth = 1f,
                        maxValue = memorySwapMax.value,
                        minValue = memorySwapMin.value
                    )
                    MemoryAndSwapAnnotationsLine(
                        colors = memoryAndSwapColors,
                        annotations = listOf(
                            ("${context.getString(R.string.memory_usage)}:" +
                                    " %03.1f%%    %s/%s    ${context.getString(R.string.cache)}%s").format(
                                memoryAndSwap.value.memory.percent,
                                toStringWithUnit(memoryAndSwap.value.memory.used),
                                toStringWithUnit(memoryAndSwap.value.memory.total),
                                toStringWithUnit(memoryAndSwap.value.memory.cache)
                            ), "${context.getString(R.string.swap)}: %03.1f%%    %s/%s".format(
                                memoryAndSwap.value.swap.percent,
                                toStringWithUnit(memoryAndSwap.value.swap.used),
                                toStringWithUnit(memoryAndSwap.value.swap.total)
                            )
                        ),
                        capcities = listOf(
                            memoryAndSwap.value.memory.percent / 100f,
                            memoryAndSwap.value.swap.percent / 100f
                        )
                    )
                }

                val networkAxisMin = remember { mutableStateOf(0f) }
                val networkAxisMax = remember { mutableStateOf(1000f * 1000f) }
                val networkAxisLabels = remember {
                    mutableStateListOf(
                        "0kB/s",
                        "200kB/s",
                        "400kB/s",
                        "600kB/s",
                        "800kB/s",
                        "1MB/s"
                    )
                }

                LaunchedEffect(networkDownloadAndUploadState.value) {
                    val axisValues = listOf(
                        0f, // 0
                        1000f, // 1kB/s
                        100 * 1000f, // 100kB/s
                        200 * 1000f, // 200kB/s
                        400 * 1000f, // 400kB/s
                        800 * 1000f, // 800kB/s
                        1000f * 1000f, // 1MB/s
                        5 * 1000f * 1000f, // 5MB/s
                        10 * 1000f * 1000f, // 10MB/s
                        20 * 1000f * 1000f, // 20MB/s
                        50 * 1000f * 1000f // 50MB/s
                    )
                    val total =
                        networkDownloadAndUploadState.value[0] + networkDownloadAndUploadState.value[1]
                    val dataMin = total.min()
                    val dataMax = total.max()
                    networkAxisMax.value =
                        axisValues.subList(1, axisValues.size).filter { it >= dataMax }.min()
                    networkAxisMin.value =
                        axisValues.subList(0, axisValues.size - 1).filter { it <= dataMin }.max()
                    val labelCount = 6
                    for (index in 0..5) {
                        val value = networkAxisMin.value +
                                (networkAxisMax.value - networkAxisMin.value) *
                                index / (labelCount - 1)
                        networkAxisLabels[index] = toStringWithSpeedUnit(value, 0)
                    }
                }

                FoldableBox(context.getString(R.string.network)) {
                    SmoothBezierLineChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(10.dp),
                        allValues = networkDownloadAndUploadState.value,
                        yAxisLabels = networkAxisLabels,
                        colors = networkColors,
                        strokeWidth = 1f,
                        minValue = networkAxisMin.value,
                        maxValue = networkAxisMax.value
                    )
                    NetworkAnnotationsLine(
                        colors = networkColors, annotations = listOf(
                            "${context.getString(R.string.current_download)}:" +
                                    " ${toStringWithSpeedUnit(networkStatsState.value.download.speed)} ${
                                        context.getString(R.string.current_download_total)
                                    }:${toStringWithUnit(networkStatsState.value.download.total)}",
                            "${context.getString(R.string.current_upload)}:" +
                                    "${toStringWithSpeedUnit(networkStatsState.value.upload.speed)} " +
                                    "${context.getString(R.string.current_upload_total)}:${
                                        toStringWithUnit(
                                            networkStatsState.value.upload.total
                                        )
                                    }"
                        )
                    )
                }

                val diskAxisMin = remember { mutableStateOf(0f) }
                val diskAxisMax = remember { mutableStateOf(1000f * 1000f) }
                val diskAxisLabels = remember {
                    mutableStateListOf(
                        "0kB/s",
                        "200kB/s",
                        "400kB/s",
                        "600kB/s",
                        "800kB/s",
                        "1MB/s"
                    )
                }

                LaunchedEffect(diskReadAndWriteList.value) {
                    val axisValues = listOf(
                        0f, // 0
                        1000f, // 1kB/s
                        100 * 1000f, // 100kB/s
                        200 * 1000f, // 200kB/s
                        400 * 1000f, // 400kB/s
                        800 * 1000f, // 800kB/s
                        1000f * 1000f, // 1MB/s
                        5 * 1000f * 1000f, // 5MB/s
                        10 * 1000f * 1000f, // 10MB/s
                        20 * 1000f * 1000f, // 20MB/s
                        50 * 1000f * 1000f // 50MB/s
                    )
                    val total = diskReadAndWriteList.value[0] + diskReadAndWriteList.value[1]
                    val dataMin = total.min()
                    val dataMax = total.max()
                    diskAxisMax.value =
                        axisValues.subList(1, axisValues.size).filter { it >= dataMax }.minOrNull()
                            ?: 0f
                    diskAxisMin.value =
                        axisValues.subList(0, axisValues.size - 1).filter { it <= dataMin }
                            .maxOrNull() ?: 0f
                    val labelCount = 6
                    for (index in 0..5) {
                        val value = diskAxisMin.value +
                                (diskAxisMax.value - diskAxisMin.value) *
                                index / (labelCount - 1)
                        diskAxisLabels[index] = toStringWithSpeedUnit(value, 0)
                    }
                }

                FoldableBox(context.getString(R.string.disk)) {
                    SmoothBezierLineChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(10.dp),
                        allValues = diskReadAndWriteList.value,
                        yAxisLabels = diskAxisLabels,
                        colors = diskColors,
                        strokeWidth = 1f,
                        minValue = diskAxisMin.value,
                        maxValue = diskAxisMax.value
                    )
                    DiskAnnotationsLine(
                        colors = diskColors, annotations = listOf(
                            "${context.getString(R.string.current_read_disk)}: " +
                                    "${toStringWithSpeedUnit(diskStatsState.value.read.speed)} " +
                                    "${
                                        context.getString(R.string.current_read_disk_total)
                                    }:${toStringWithUnit(diskStatsState.value.read.total)}",
                            "${context.getString(R.string.current_write_disk)}: " +
                                    "${toStringWithSpeedUnit(diskStatsState.value.write.speed)} " +
                                    "${
                                        context.getString(R.string.current_write_disk_total)
                                    }:${toStringWithUnit(diskStatsState.value.write.total)}"
                        )
                    )
                }
            }

            // 自定义滚动条
            val scrollbarWidth = 16.dp
            val scrollbarColor = Color(0xFF999999)
            val hoverColor: Color = Color(0x40000000)
            val scrollbarMargin = 0.dp

            val isDragging = remember { mutableStateOf(false) }
            val dragOffset = remember { mutableStateOf(0f) }

            val containerHeight = remember { mutableStateOf(0f) }
            val contentHeight = remember { mutableStateOf(0f) }

            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(scrollbarWidth)
                    .align(Alignment.TopEnd)
                    .padding(start = scrollbarMargin)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val scrollFraction =
                                    scrollState.value.toFloat() / max(1f, scrollState.maxValue.toFloat())
                                val thumbTop = scrollFraction * (size.height - thumbHeight(containerHeight.value, contentHeight.value))
                                val thumbBottom = thumbTop + thumbHeight(containerHeight.value, contentHeight.value)
                                if (offset.y in thumbTop..thumbBottom) {
                                    isDragging.value = true
                                    dragOffset.value = offset.y - thumbTop
                                }
                            },
                            onDrag = { change, dragAmount ->
                                if (isDragging.value) {
                                    val newThumbTop = change.position.y - dragOffset.value
                                    val fraction = newThumbTop / max(1f, containerHeight.value - thumbHeight(containerHeight.value, contentHeight.value))
                                    coroutineScope.launch {
                                        scrollState.scrollTo((fraction * scrollState.maxValue).toInt())
                                    }

                                }
                            },
                            onDragEnd = { isDragging.value = false },
                            onDragCancel = { isDragging.value = false }
                        )
                    }
                    .onSizeChanged { containerHeight.value = it.height.toFloat() }
            ) {
                contentHeight.value = scrollState.maxValue + size.height
                val thumbH = thumbHeight(size.height.toFloat(), contentHeight.value)
                val thumbTop = (scrollState.value.toFloat() / max(1f, scrollState.maxValue.toFloat())) * (size.height - thumbH)

                drawRoundRect(
                    color = if (isDragging.value) hoverColor else scrollbarColor,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, thumbTop),
                    size = Size(size.width, thumbH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(0f, 0f)
                )
            }
        }
    }
}

private fun thumbHeight(containerHeight: Float, contentHeight: Float): Float {
    val minHeight = 48f
    return max(minHeight, containerHeight / contentHeight * containerHeight)
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
    yAxisLabels: List<String> = listOf("0%", "20%", "40%", "60%", "80%", "100%"),
    strokeWidth: Float = 4f,
    minValue: Float? = null,
    maxValue: Float? = null
) {
    Canvas(modifier = modifier) {
        val referenceLineCount = 6
        val referenceLineColor = Color(0x3300001A)
        val labelPaint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            textSize = 10f
            color = android.graphics.Color.argb(0xff, 0x47, 0x47, 0x47)
        }
        repeat(yAxisLabels.size) { i ->
            val ratio = i / (referenceLineCount - 1f)
            val y = size.height - ratio * size.height
            drawLine(
                color = referenceLineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 0.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)
            )

            val textWidth = labelPaint.measureText(yAxisLabels[i])
            drawContext.canvas.nativeCanvas.drawText(
                yAxisLabels[i], size.width - textWidth - 4f, // 右侧对齐，距离右边留4f间距
                y, labelPaint
            )
        }
        val lineBrushes = colors.map { it -> SolidColor(it) }
        allValues.forEachIndexed { index, values ->
            if (values.size < 2) return@Canvas
            val width = size.width
            val height = size.height
            var minValue = minValue ?: values.minOrNull() ?: 0f
            var maxValue = maxValue ?: values.maxOrNull() ?: 0f
            val range = maxValue - minValue

            val xStep = width / (values.size - 1f)
            val points = values.mapIndexed { i, v ->
                var v = if (v > maxValue) maxValue else v
                v = if (v < minValue) minValue else v
                Offset(
                    x = i * xStep, y = height - (v - minValue) / range * height
                )
            }

            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p3 = points[i + 1]
                    val cp1 = Offset(
                        x = p0.x + (p3.x - p0.x) / 2f, y = p0.y
                    )
                    val cp2 = Offset(
                        x = p0.x + (p3.x - p0.x) / 2f, y = p3.y
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

