package com.fde.taskmanager.ui

import android.os.Build
import android.util.Log
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.fde.taskmanager.R
import com.fde.taskmanager.TaskManagerBinder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                        .size(width = 58.dp, height = barHeight)
                        .background(
                            color = Color(0x14000000), shape = RoundedCornerShape(3.dp)
                        )
                        .clip(RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .size(
                                if (capcities.getOrNull(i) != null) barWidthTotal * capcities[0] else 0.dp,
                                height = barHeight
                            )
                            .background(color = colors[i]),
                    )
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

    val cpuCount = TaskManagerBinder.getEachCPUPercent(10).size
    val allCpuColors = context.resources.getIntArray(R.array.cpu_color_array).map { Color(it) }
    val cpuColors = if (cpuCount <= allCpuColors.size) {
        allCpuColors.take(cpuCount)
    } else {
        List(cpuCount) { index -> allCpuColors[index % allCpuColors.size] }
    }
    val cpuPercentState = remember {
        List(cpuCount) { mutableStateListOf<Float>() }.toMutableStateList()
    }
    val memoryAndSwapState = remember {
        List(2) { mutableStateListOf<Float>() }.toMutableStateList()
    }
    val networkDownloadAndUploadState = remember {
        List(2) { mutableStateListOf<Float>() }.toMutableStateList()
    }
    val diskReadAndWriteState = remember {
        List(2) { mutableStateListOf<Float>() }.toMutableStateList()
    }

    val memoryAndSwapColors =
        context.resources.getIntArray(R.array.memory_swap_color_array).map { Color(it) }
    val networkColors = context.resources.getIntArray(R.array.network_color_array).map { Color(it) }
    val diskColors = context.resources.getIntArray(R.array.disk_color_array).map { Color(it) }
    val currentCPUAnnotationsState = remember { mutableStateListOf<String>("", "", "", "") }
    val currentMemoryAndSwapAnnotationsState = remember { mutableStateListOf<String>("", "") }
    val currentMemoryAndSwapCapcityState = remember { mutableStateListOf<Float>(0f, 0f) }
    val currentNetworkAnnotationsState = remember { mutableStateListOf<String>("", "") }
    val currentDiskAnnotationsState = remember { mutableStateListOf<String>("", "") }
    val delayGap: Long = 1000
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll((rememberScrollState()))
    ) {
        FoldableBox("CPU") {
            LaunchedEffect(Unit) {

                coroutineScope.launch {
                    while (true) {
//                1
                        val eachCPUPercent = TaskManagerBinder.getEachCPUPercent(200) // [0,1,2,3]
                        currentCPUAnnotationsState.clear()
                        eachCPUPercent.forEachIndexed { index, it ->
                            if (cpuPercentState[index].size > 100) cpuPercentState[index].removeFirst()
                            cpuPercentState[index].add(it)
                            currentCPUAnnotationsState.add("CPU${index + 1}: %03.1f%%".format(it))
                        }
                        delay(delayGap)
                    }
                }
            }
            SmoothBezierLineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(10.dp),
                allValues = cpuPercentState,
                colors = cpuColors,
                strokeWidth = 1f,
                maxValue = 100f,
                minValue = 0f
            )
            CPUUsagesAnnotationsLine(
                colors = cpuColors, annotations = currentCPUAnnotationsState
            )
        }
        FoldableBox(context.getString(R.string.memory_and_swap)) {
            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    while (true) {
//                2
                        val memoryAndMemoryInfo = TaskManagerBinder.getMemoryAndSwap()
                        currentMemoryAndSwapAnnotationsState.clear()
                        currentMemoryAndSwapAnnotationsState.add(
                            "${context.getString(R.string.memory_usage)}: %03.1f%%    %s/%s    ${
                                context.getString(
                                    R.string.cache
                                )
                            }%s".format(
                                memoryAndMemoryInfo.memory.percent,
                                toStringWithUnit(memoryAndMemoryInfo.memory.used),
                                toStringWithUnit(memoryAndMemoryInfo.memory.total),
                                toStringWithUnit(memoryAndMemoryInfo.memory.cache)
                            )
                        )
                        currentMemoryAndSwapAnnotationsState.add(
                            "${context.getString(R.string.swap)}: %03.1f%%    %s/%s".format(
                                memoryAndMemoryInfo.memory.percent,
                                toStringWithUnit(memoryAndMemoryInfo.swap.used),
                                toStringWithUnit(memoryAndMemoryInfo.swap.total)
                            )
                        )
                        currentMemoryAndSwapCapcityState.clear()
                        currentMemoryAndSwapCapcityState.add(memoryAndMemoryInfo.memory.percent / 100f)
                        currentMemoryAndSwapCapcityState.add(memoryAndMemoryInfo.swap.percent / 100f)
                        val memoryList = memoryAndSwapState[0]
                        val swapList = memoryAndSwapState[1]
                        if (swapList.size > 100) swapList.removeFirst()
                        if (memoryList.size > 100) memoryList.removeFirst()
                        memoryList.add(memoryAndMemoryInfo.memory.percent)
                        swapList.add(memoryAndMemoryInfo.swap.percent)
                        delay(delayGap)
                    }
                }
            }
            SmoothBezierLineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(10.dp),
                allValues = memoryAndSwapState,
                colors = memoryAndSwapColors,
                strokeWidth = 1f,
                maxValue = 100f,
                minValue = 0f
            )
            MemoryAndSwapAnnotationsLine(
                colors = memoryAndSwapColors,
                annotations = currentMemoryAndSwapAnnotationsState,
                capcities = currentMemoryAndSwapCapcityState
            )
        }
        FoldableBox(context.getString(R.string.network)) {

            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    while (true) {
//                3
                        val networkDownloadAndUpload =
                            TaskManagerBinder.getNetworkDownloadAndUpload(200)
                        val networkDownloadState = networkDownloadAndUploadState[0]
                        val networkUploadState = networkDownloadAndUploadState[1]
                        if (networkDownloadState.size > 100) networkDownloadState.removeFirst()
                        if (networkUploadState.size > 100) networkUploadState.removeFirst()
                        networkDownloadState.add(networkDownloadAndUpload.download.speed)
                        networkUploadState.add(networkDownloadAndUpload.upload.speed)
                        currentNetworkAnnotationsState.clear()
                        val currentDownloadSpeedString =
                            toStringWithSpeedUnit(networkDownloadAndUpload.download.speed)
                        val currentDownloadTotalString =
                            toStringWithUnit(networkDownloadAndUpload.download.total)
                        val currentUploadSpeedString =
                            toStringWithSpeedUnit(networkDownloadAndUpload.upload.speed)
                        val currentUploadTotalString =
                            toStringWithUnit(networkDownloadAndUpload.upload.total)
                        currentNetworkAnnotationsState.add(
                            "${context.getString(R.string.current_download)}: $currentDownloadSpeedString ${
                                context.getString(
                                    R.string.current_download_total
                                )
                            }:$currentDownloadTotalString"
                        )
                        currentNetworkAnnotationsState.add("${context.getString(R.string.current_upload)}:$currentUploadSpeedString ${R.string.current_upload_total}:$currentUploadTotalString")
                        delay(delayGap)
                    }
                }
            }
            SmoothBezierLineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(10.dp),
                allValues = networkDownloadAndUploadState,
                yAxisLabels = listOf(
                    "0kb/s",
                    "20kb/s",
                    "40kb/s",
                    "60kb/s",
                    "80kb/s",
                    "100kb/s",
                ),
                colors = networkColors,
                strokeWidth = 1f,
                minValue = 0f,
                maxValue = 100 * 1024f
            )
            NetworkAnnotationsLine(
                colors = networkColors, annotations = currentNetworkAnnotationsState
            )
        }
        FoldableBox(context.getString(R.string.disk)) {

            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    while (true) {
//                4
                        val diskReadAndWrite = TaskManagerBinder.getDiskReadAndWrite(100)
                        val diskReadState = diskReadAndWriteState[0]
                        val diskWriteState = diskReadAndWriteState[1]
                        if (diskReadState.size > 100) diskReadState.removeFirst()
                        if (diskWriteState.size > 100) diskWriteState.removeFirst()
                        diskReadState.add(diskReadAndWrite.read.speed)
                        diskWriteState.add(diskReadAndWrite.write.speed)
                        currentDiskAnnotationsState.clear()
                        val currentDiskReadSpeedString =
                            toStringWithSpeedUnit(diskReadAndWrite.read.speed)
                        val currentDiskReadTotalString =
                            toStringWithUnit(diskReadAndWrite.read.total)
                        val currentDiskWriteSpeedString =
                            toStringWithSpeedUnit(diskReadAndWrite.write.speed)
                        val currentDiskWriteTotalString =
                            toStringWithUnit(diskReadAndWrite.write.total)
                        currentDiskAnnotationsState.add(
                            "${context.getString(R.string.current_read_disk)}: $currentDiskReadSpeedString ${
                                context.getString(
                                    R.string.current_read_disk_total
                                )
                            }:$currentDiskReadTotalString"
                        )
                        currentDiskAnnotationsState.add(
                            "${context.getString(R.string.current_write_disk)}: $currentDiskWriteSpeedString ${
                                context.getString(
                                    R.string.current_write_disk_total
                                )
                            }:$currentDiskWriteTotalString"
                        )
                        delay(delayGap)
                    }
                }
            }
            SmoothBezierLineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(10.dp),
                allValues = diskReadAndWriteState,
                yAxisLabels = listOf(
                    "0MB/s", "20MB/s", "40MB/s", "60MB/s", "80MB/s", "100MB/s"
                ),
                colors = diskColors,
                strokeWidth = 1f,
                minValue = 0f,
                maxValue = 100 * 1024 * 1024f
            )
            DiskAnnotationsLine(
                colors = diskColors, annotations = currentDiskAnnotationsState
            )
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

            val textWidth = labelPaint.measureText("${(ratio * 100).toInt()}%")
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

