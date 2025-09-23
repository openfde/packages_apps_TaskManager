package com.fde.taskmanager.ui

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fde.taskmanager.Adapters
import com.fde.taskmanager.R
import com.fde.taskmanager.TaskManagerBinder
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.core.graphics.drawable.toBitmap

import java.net.HttpURLConnection
import java.net.URL

import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import android.view.PointerIcon as PointerIcon1
import androidx.compose.ui.input.pointer.PointerIcon as PointerIcon2

@Composable
fun HeaderDivider(targetIndex: Int,
        weights: MutableList<Float>,
        headerWidth: Int) {
    val context = LocalContext.current
    val icon: PointerIcon1 = PointerIcon1.getSystemIcon(context, PointerIcon1.TYPE_HORIZONTAL_DOUBLE_ARROW)
    VerticalDivider(
        modifier = Modifier
            .pointerHoverIcon(PointerIcon2(icon))
            .height(21.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val delta = dragAmount.x.toDp() / headerWidth.toDp()
                    if (weights[targetIndex] + delta > 0.5f ||
                        weights[targetIndex] + delta < 0.1f
                    )
                        return@detectDragGestures
                    for (index in weights.indices) {
                        if (index < targetIndex) continue
                        val eachDelta = delta / (weights.size - targetIndex)
                        if (weights[index] - eachDelta < 0f) return@detectDragGestures
                        weights[index] -= eachDelta
                    }
                    weights[targetIndex] += delta
                }
            }, color = Color(0x0D000000)
    )
}

@Composable
fun TasksTableHeader(
    sortMode: SortMode,
    onSortModeChange: (SortMode) -> Unit,
    weights: MutableList<Float>
) {
    val nameSortedReverseState = remember { mutableStateOf(false) }
    val idSortedReverseState = remember { mutableStateOf(false) }
    val memorySortedReverseState = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val vectorIconSize = 16.dp
    val headerWidthState = remember { mutableStateOf(0) }

    HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE8E9EB))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 10.dp)
            .onSizeChanged({ it->
                headerWidthState.value = it.width
            })
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(weights[0])
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = context.getString(R.string.process_name),
                    modifier = Modifier.padding(horizontal = 10.dp),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Image(
                    painter = painterResource(id = R.drawable.task_header_down_vector),
                    modifier = Modifier
                        .size(vectorIconSize)
                        .graphicsLayer {
                            rotationX = when (sortMode) {
                                SortMode.BY_NAME_SEQUENTIAL -> 0f
                                SortMode.BY_NAME_REVERSE -> 180f
                                SortMode.BY_ID_SEQUENTIAL -> 0f
                                SortMode.BY_ID_REVERSE -> 0f
                                else -> 0f
                            }
                        }
                        .clickable(onClick = {
                            when (sortMode) {
                                SortMode.BY_ID_SEQUENTIAL, SortMode.BY_ID_REVERSE,
                                SortMode.BY_MEMORY_REVERSE, SortMode.BY_MEMORY_SEQUENTIAL -> {
                                    // 进一步看
                                    if (nameSortedReverseState.value) {
                                        // 名称顺序
                                        onSortModeChange(SortMode.BY_NAME_REVERSE)
                                        nameSortedReverseState.value = false
                                    } else {
                                        onSortModeChange(SortMode.BY_NAME_REVERSE)
                                        nameSortedReverseState.value = true
                                    }
                                }

                                SortMode.BY_NAME_SEQUENTIAL -> onSortModeChange(SortMode.BY_NAME_REVERSE)
                                SortMode.BY_NAME_REVERSE -> onSortModeChange(SortMode.BY_NAME_SEQUENTIAL)
                            }
                        }),
                    contentDescription = null,
                )
            }
            HeaderDivider(0, weights,headerWidthState.value)
            Text(
                text = context.getString(R.string.user),
                modifier = Modifier
                    .weight(weights[1])
                    .padding(horizontal = 10.dp),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            HeaderDivider(1, weights,headerWidthState.value)
            Text(
                text = context.getString(R.string.virtual_memory),
                modifier = Modifier
                    .weight(weights[2])
                    .padding(horizontal = 10.dp),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            HeaderDivider(2,weights,headerWidthState.value)
            Text(
                text = "% CPU",
                modifier = Modifier
                    .weight(weights[3])
                    .padding(horizontal = 10.dp),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            HeaderDivider(3,weights,headerWidthState.value)
            Row(
                modifier = Modifier
                    .weight(weights[4])
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ID",
                    modifier = Modifier.padding(horizontal = 10.dp),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Image(
                    painter = painterResource(id = R.drawable.task_header_down_vector),
                    modifier = Modifier
                        .size(vectorIconSize)
                        .graphicsLayer {
                            rotationX = when (sortMode) {
                                SortMode.BY_NAME_SEQUENTIAL -> 0f
                                SortMode.BY_NAME_REVERSE -> 0f
                                SortMode.BY_ID_SEQUENTIAL -> 0f
                                SortMode.BY_ID_REVERSE -> 180f
                                else -> 0f
                            }
                        }
                        .clickable(onClick = {
                            when (sortMode) {
                                SortMode.BY_NAME_SEQUENTIAL, SortMode.BY_NAME_REVERSE,
                                SortMode.BY_MEMORY_SEQUENTIAL, SortMode.BY_MEMORY_REVERSE -> {
                                    if (idSortedReverseState.value) {
                                        onSortModeChange(SortMode.BY_ID_SEQUENTIAL)
                                        idSortedReverseState.value = false
                                    } else {
                                        onSortModeChange(SortMode.BY_ID_REVERSE)
                                        idSortedReverseState.value = true
                                    }
                                }

                                SortMode.BY_ID_SEQUENTIAL -> onSortModeChange(SortMode.BY_ID_REVERSE)
                                SortMode.BY_ID_REVERSE -> onSortModeChange(SortMode.BY_ID_SEQUENTIAL)
                            }
                        }),
                    contentDescription = null
                )
            }
            HeaderDivider(4, weights,headerWidthState.value)
            Row(
                modifier = Modifier
                    .weight(weights[5])
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = context.getString(R.string.memory),
                    modifier = Modifier
                        .padding(horizontal = 10.dp),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Image(
                    painter = painterResource(id = R.drawable.task_header_down_vector),
                    modifier = Modifier
                        .size(vectorIconSize)
                        .graphicsLayer {
                            rotationX = when (sortMode) {
                                SortMode.BY_NAME_SEQUENTIAL,
                                SortMode.BY_NAME_REVERSE,
                                SortMode.BY_ID_SEQUENTIAL,
                                SortMode.BY_ID_REVERSE,
                                SortMode.BY_MEMORY_SEQUENTIAL -> 0f

                                SortMode.BY_MEMORY_REVERSE -> 180f
                            }
                        }
                        .clickable(onClick = {
                            when (sortMode) {
                                SortMode.BY_NAME_SEQUENTIAL, SortMode.BY_NAME_REVERSE,
                                SortMode.BY_ID_SEQUENTIAL, SortMode.BY_ID_REVERSE -> {
                                    if (memorySortedReverseState.value) {
                                        onSortModeChange(SortMode.BY_MEMORY_SEQUENTIAL)
                                        memorySortedReverseState.value = false
                                    } else {
                                        onSortModeChange(SortMode.BY_MEMORY_REVERSE)
                                        memorySortedReverseState.value = true
                                    }
                                }

                                SortMode.BY_MEMORY_SEQUENTIAL -> onSortModeChange(SortMode.BY_MEMORY_REVERSE)
                                SortMode.BY_MEMORY_REVERSE -> onSortModeChange(SortMode.BY_MEMORY_SEQUENTIAL)
                            }
                        }),
                    contentDescription = null
                )
            }
            HeaderDivider(5,weights,headerWidthState.value)
            Text(
                text = context.getString(R.string.disk_read_storage),
                modifier = Modifier
                    .weight(weights[6])
                    .padding(horizontal = 10.dp),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            HeaderDivider(6,weights,headerWidthState.value)
            Text(
                text = context.getString(R.string.disk_write_storage),
                modifier = Modifier
                    .weight(weights[7])
                    .padding(horizontal = 10.dp),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            HeaderDivider(7,weights,headerWidthState.value)
            Text(
                text = context.getString(R.string.disk_read),
                modifier = Modifier
                    .weight(weights[8])
                    .padding(horizontal = 10.dp),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            HeaderDivider(8,weights,headerWidthState.value)
            Text(
                text = context.getString(R.string.disk_write),
                modifier = Modifier
                    .weight(weights[9])
                    .padding(horizontal = 10.dp),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
    HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE8E9EB))
}

enum class DisplayMode {
    ALL_PROCESSES, // 所有进程
    ACTIVE_PROCESSES, // 活动进程
    MY_PROCESSES, // 我的进程
    SEARCH_FILTERED_PROCESSES // 搜索过滤进程
}

enum class SortMode {
    BY_NAME_SEQUENTIAL, BY_NAME_REVERSE, BY_ID_SEQUENTIAL, BY_ID_REVERSE,
    BY_MEMORY_SEQUENTIAL, BY_MEMORY_REVERSE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessView(displayMode: DisplayMode, searchBarValue: String) {
    val taskInfoList = remember { mutableStateListOf<Adapters.TaskInfo>() }
    val coroutineScope = rememberCoroutineScope()
    val initialLoad = remember { mutableStateOf(false) }
    val userName = TaskManagerBinder.getUserName()
    val sortModeState = remember { mutableStateOf(SortMode.BY_NAME_SEQUENTIAL) }
    val appResponseState = remember { mutableStateOf<Adapters.AppsResponse?>(null) }
    val taskHeaderItemWeightsState = remember {
        mutableStateListOf<Float>(
            0.20f, 0.08f, 0.09f, 0.09f, 0.09f, 0.09f, 0.09f, 0.09f, 0.09f, 0.09f
        )
    }

    LaunchedEffect(Unit) {
        if (!initialLoad.value) {
            initialLoad.value = true
        } else {
            return@LaunchedEffect
        }
        coroutineScope.launch {
            try {
                val url = URL("http://127.0.0.1:18080/api/v1/apps?page=1&page_size=100")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.setRequestProperty("Content-Type", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val responseString = inputStream.bufferedReader().use { it.readText() }
                    appResponseState.value = Adapters.AppsResponseAdapt(responseString)
                }
                connection.disconnect()
            } catch (e: Exception) {
                // 错误处理保持不变
            }
        }

        coroutineScope.launch {
            val allTasks = TaskManagerBinder.getTasks()
            taskInfoList.clear()
            taskInfoList.addAll(allTasks)
            while (true) {
                val tasks = TaskManagerBinder.getTasks()
                val currentPids = TaskManagerBinder.getTaskPids().toSet()

                val batchSize = 20 // 每个批次的大小
                val totalTasks = tasks.size
                var currentIndex = 0

                val toRemove = taskInfoList.filter { it.pid !in currentPids }
                toRemove.forEach { taskInfoList.remove(it) }

                while (currentIndex < totalTasks) {
                    val endIndex = minOf(currentIndex + batchSize, totalTasks)
                    val batch = tasks.subList(currentIndex, endIndex)
                    for (task in batch) {
                        val index = taskInfoList.indexOfFirst { it.pid == task.pid }
                        if (index != -1) {
                            // 存在，只需要更新
                            taskInfoList[index] = task
                        } else {
                            // 不存在，需要添加
                            taskInfoList.add(task)
                        }
                    }
                    currentIndex = endIndex
                    delay(100) // 每批次延迟100ms
                }
            }
        }
    }

    Column(modifier = Modifier.background(Color(0xFFFCFDFF))) {
        TasksTableHeader(sortModeState.value, onSortModeChange = {
            sortModeState.value = it
        }, taskHeaderItemWeightsState)
        LazyColumn {
            items(
                when (sortModeState.value) {
                    SortMode.BY_NAME_SEQUENTIAL -> taskInfoList.sortedBy { it.name }
                    SortMode.BY_NAME_REVERSE -> taskInfoList.sortedByDescending { it.name }
                    SortMode.BY_ID_SEQUENTIAL -> taskInfoList.sortedBy { it.pid }
                    SortMode.BY_ID_REVERSE -> taskInfoList.sortedByDescending { it.pid }
                    SortMode.BY_MEMORY_SEQUENTIAL -> taskInfoList.sortedBy { it.rss }
                    SortMode.BY_MEMORY_REVERSE ->  taskInfoList.sortedByDescending{ it.rss }
                }, key = { it.pid }) {
                TaskItem(
                    it,
                    displayMode,
                    userName,
                    searchBarValue,
                    appResponseState.value,
                    taskHeaderItemWeightsState
                )
            }
        }
    }
}

fun toStringWithUnit(bytes: Long): String {
    return when {
        bytes > 1024 * 1024 * 1024 -> "%.1f GB".format(bytes / (1024f * 1024f * 1024f))
        bytes > 1024 * 1024 -> "%.1f MB".format(bytes / (1024f * 1024f))
        bytes > 1024 -> "%.1f KB".format(bytes / 1024f)
        else -> "$bytes B"
    }
}

fun toStringWithSpeedUnit(bytesPerSecond: Float): String {
    return when {
        bytesPerSecond > 1024 * 1024 -> "%.1f MB/s".format(bytesPerSecond / (1024f * 1024f))
        bytesPerSecond > 1024 -> "%.1f KB/s".format(bytesPerSecond / 1024f)
        else -> "$bytesPerSecond B/s"
    }
}

@Composable
fun PrioritySlider(
    sliderValue: Int, onChange: (Int) -> Unit
) {
    val minValue = -19
    val maxValue = 20

    Slider(
        value = sliderValue.toFloat(),
        onValueChange = {
            onChange(it.toInt())
        },
        valueRange = minValue.toFloat()..maxValue.toFloat(),
        steps = maxValue - minValue,
        modifier = Modifier.fillMaxWidth()
    )
}

enum class IconType {
    ANDROID_BITMAP_ICON, ANDROID_DRAWABLE_ICON, ANDROID_NULL_ICON, LINUX_BITMAP_ICON, LINUX_NULL_ICON
}

@Composable
fun TaskItem(
    taskInfo: Adapters.TaskInfo,
    displayMode: DisplayMode,
    userName: String,
    searchBarValue: String,
    appResponse: Adapters.AppsResponse?,
    weights: MutableList<Float>,
) {
    val context = LocalContext.current
    val floatingMenuPosition = remember { mutableStateOf(Offset.Zero) }
    val floatingMenuExpanded = remember { mutableStateOf(false) }
    val floatingPropertiesWindowShow = remember { mutableStateOf(false) }
    val floatingPriorityModificationWindowShow = remember { mutableStateOf(false) }
    var priorityModificationSliderValue = remember { mutableIntStateOf(taskInfo.nice) }


    var iconBitmap: ImageBitmap? = null
    var iconDrawable: Drawable? = null
    var iconType: IconType? = null
    val iconSize = 24.dp

    when {
        taskInfo.isAndroidApp -> {
            // 首先尝试使用packageManager获取图标
            val pm: PackageManager = context.packageManager
            try {
                iconDrawable = pm.getApplicationIcon(taskInfo.name.toString())
                iconType = IconType.ANDROID_DRAWABLE_ICON
            } catch (e: PackageManager.NameNotFoundException) {
                // 再尝试用local图标
                val bitMap =
                    TaskManagerBinder.getIconBitmapByTaskName(taskInfo.name.toString(), true)
                if (bitMap != null) {
                    iconBitmap = bitMap
                    iconType = IconType.ANDROID_BITMAP_ICON
                } else {
                    iconType = IconType.ANDROID_NULL_ICON
                }
            }
        }

        else -> {
            // linux app
            if (appResponse == null) {
                iconType = IconType.LINUX_NULL_ICON
            } else {
                val appInfoIndex = appResponse.data.data.indexOfFirst {
                    taskInfo.name!!.lowercase().contains(it.Name.lowercase())
                }
                if (appInfoIndex == -1) {
                    iconType = IconType.LINUX_NULL_ICON
                } else {
                    val iconB64String = appResponse.data.data[appInfoIndex].Icon
                    val imageBytes = Base64.decode(iconB64String, Base64.DEFAULT)
                    iconBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        .asImageBitmap()
                    iconType = IconType.LINUX_BITMAP_ICON
                }
            }
        }
    }

    if (floatingPropertiesWindowShow.value) {
        AlertDialog(onDismissRequest = {
            floatingPropertiesWindowShow.value = false
        }, title = {
            Text(
                text = taskInfo.name.toString(),
                fontWeight = FontWeight.W700,
            )
        }, text = {
            Column {
                Row {
                    Text("用户名:")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(taskInfo.user.toString())
                }
                Row {
                    Text("虚拟内存:")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(toStringWithUnit(taskInfo.vmsize))
                }
                Row {
                    Text("CPU占用率:")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(taskInfo.cpuUsage.toString() + "%")
                }
                Row {
                    Text("PID:")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(taskInfo.pid.toString())
                }
                Row {
                    Text("内存:")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(toStringWithUnit(taskInfo.rss))
                }
                Row {
                    Text("读盘容量:")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(toStringWithUnit(taskInfo.readBytes))
                }
                Row {
                    Text("写入容量:")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(toStringWithUnit(taskInfo.writeBytes))
                }
                Row {
                    Text("磁盘读取:")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(toStringWithUnit(taskInfo.readIssued))
                }
                Row {
                    Text("磁盘写入:")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(toStringWithUnit(taskInfo.writeIssued))
                }
            }
        }, confirmButton = {}, dismissButton = {
            TextButton(
                onClick = {
                    floatingPropertiesWindowShow.value = false
                }) {
                Text(
                    "取消",
                    fontWeight = FontWeight.W700,
                )
            }
        })
    }

    if (floatingPriorityModificationWindowShow.value) {
        AlertDialog(onDismissRequest = {
            floatingPriorityModificationWindowShow.value = false
        }, title = {
            Text(
                text = taskInfo.name.toString(),
                fontWeight = FontWeight.W700,
            )
        }, text = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "当前优先级: ${priorityModificationSliderValue.intValue}")
                PrioritySlider(
                    sliderValue = priorityModificationSliderValue.intValue, onChange = { it ->
                        priorityModificationSliderValue.intValue = it
                    })
            }
        }, confirmButton = {
            TextButton(
                onClick = {
                    floatingPriorityModificationWindowShow.value = false
                    TaskManagerBinder.changeTaskPriority(
                        taskInfo.pid, priorityModificationSliderValue.intValue
                    )
                }) {
                Text(
                    "确定",
                    fontWeight = FontWeight.W700,
                )
            }
        }, dismissButton = {
            TextButton(
                onClick = {
                    floatingPriorityModificationWindowShow.value = false
                }) {
                Text(
                    "取消",
                    fontWeight = FontWeight.W700,
                )
            }
        })
    }

    val allProcessBoolean =
        displayMode == DisplayMode.ALL_PROCESSES || displayMode == DisplayMode.ACTIVE_PROCESSES
    val myProcessBoolean = displayMode == DisplayMode.MY_PROCESSES && taskInfo.user == userName
    val searchBarFilteredBoolean =
        displayMode == DisplayMode.SEARCH_FILTERED_PROCESSES && (taskInfo.pid.toString()
            .contains(searchBarValue) || taskInfo.name.toString().contains(searchBarValue))

    val density = LocalDensity.current
    val globalPositionState = remember { mutableStateOf(Offset.Zero) }

    if (allProcessBoolean || myProcessBoolean || searchBarFilteredBoolean) {
        Row(
            modifier = Modifier
                .height(34.dp)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clickable(onClick = {})
                .onGloballyPositioned { coordinates ->
                    globalPositionState.value = coordinates.positionInRoot()
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                val localOffset = event.changes.first().position
                                val location = IntArray(2)
                                val globalX = location[0] + localOffset.x * density.density
                                val globalY = location[1] + localOffset.y * density.density
                                floatingMenuPosition.value = Offset(globalX, globalY)
                                floatingMenuExpanded.value = true
                            }
                        }
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {

            val m = listOf(
                taskInfo.name.toString(),
                taskInfo.user.toString(),
                toStringWithUnit(taskInfo.vmsize),
                taskInfo.cpuUsage.toString() + "%",
                taskInfo.pid.toString(),
                toStringWithUnit(taskInfo.rss),
                toStringWithUnit(taskInfo.readBytes),
                toStringWithUnit(taskInfo.writeBytes),
                toStringWithUnit(taskInfo.readIssued),
                toStringWithUnit(taskInfo.writeIssued)
            )
            for (index in 0 until 10) Row(
                modifier = Modifier
                    .weight(weights[index])
                    .padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (index == 0) {
                    if (taskInfo.isAndroidApp) {
                        if (iconType == IconType.ANDROID_BITMAP_ICON && iconBitmap != null) {
                            // android bitmap
                            Image(
                                bitmap = iconBitmap,
                                modifier = Modifier.size(iconSize),
                                contentDescription = null
                            )
                        } else if (iconType == IconType.ANDROID_DRAWABLE_ICON && iconDrawable != null) {
                            // android drawable
                            Image(
                                bitmap = iconDrawable.toBitmap().asImageBitmap(),
                                modifier = Modifier.size(iconSize),
                                contentDescription = null
                            )
                        } else if (iconType == IconType.ANDROID_NULL_ICON) {
                            // not found
                            Image(
                                painter = painterResource(id = R.drawable.ic_android),
                                modifier = Modifier.size(iconSize),
                                contentDescription = null
                            )
                        }
                    } else {
                        // linux app
                        if (iconType == IconType.LINUX_BITMAP_ICON && iconBitmap != null) {
                            Image(
                                bitmap = iconBitmap,
                                modifier = Modifier.size(iconSize),
                                contentDescription = null
                            )
                        } else if (iconType == IconType.LINUX_NULL_ICON) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_linux),
                                modifier = Modifier.size(iconSize),
                                contentDescription = null
                            )
                        }
                    }
                }
                Text(
                    text = m[index].toString(),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    DropdownMenu(
        expanded = floatingMenuExpanded.value,
        onDismissRequest = { floatingMenuExpanded.value = false },
        offset = with(LocalDensity.current) {
            DpOffset(
                x = floatingMenuPosition.value.x.toDp(),
                y = floatingMenuPosition.value.y.toDp() + globalPositionState.value.y.toDp()
            )
        },
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
    ) {
        // 属性
        DropdownMenuItem(
            text = { Text(text= stringResource(R.string.properties)) }, onClick = {
                floatingMenuExpanded.value = false
                floatingPropertiesWindowShow.value = true
            }, modifier = Modifier
                .height(32.dp)
                .width(192.dp)
        )
        HorizontalDivider()
        // 内存映射
        DropdownMenuItem(
            text = { Text(text= stringResource(R.string.memory_mapping)) }, onClick = {
            }, modifier = Modifier
                .height(32.dp)
                .width(192.dp)
        )
        // 打开文件
        DropdownMenuItem(
            text = { Text(text= stringResource(R.string.open_file)) }, onClick = {
            }, modifier = Modifier
                .height(32.dp)
                .width(192.dp)
        )
        HorizontalDivider()
        // 更改优先级
        DropdownMenuItem(
            text = { Text(text= stringResource(R.string.change_priority)) }, onClick = {
                floatingMenuExpanded.value = false
                floatingPriorityModificationWindowShow.value = true
            }, modifier = Modifier
                .height(32.dp)
                .width(192.dp)
        )
        // 设置关联
        DropdownMenuItem(
            text = { Text(text= stringResource(R.string.set_association)) }, onClick = {
            }, modifier = Modifier
                .height(32.dp)
                .width(192.dp)
        )
        HorizontalDivider()
        // 停止进程
        DropdownMenuItem(
            text = { Text(text= stringResource(R.string.stop_process)) }, onClick = {
                TaskManagerBinder.killTaskByPid(taskInfo.pid)
            }, modifier = Modifier
                .height(32.dp)
                .width(192.dp)
        )
        //继续
        DropdownMenuItem(
            text = { Text(text= stringResource(R.string.resume)) }, onClick = {
            }, modifier = Modifier
                .height(32.dp)
                .width(192.dp)
        )
        //终止
        DropdownMenuItem(
            text = { Text(text= stringResource(R.string.stop)) }, onClick = {
                TaskManagerBinder.killTaskByPid(taskInfo.pid)
            }, modifier = Modifier
                .height(32.dp)
                .width(192.dp)
        )
        HorizontalDivider()
        // 强制终止
        DropdownMenuItem(
            text = { Text(text= stringResource(R.string.force_stop)) }, onClick = {
                TaskManagerBinder.killTaskByPid(taskInfo.pid)
            }, modifier = Modifier
                .height(32.dp)
                .width(192.dp)
        )
    }
}


