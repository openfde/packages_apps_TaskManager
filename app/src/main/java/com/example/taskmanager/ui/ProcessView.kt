package com.example.taskmanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskmanager.Adapters
import com.example.taskmanager.R
import com.example.taskmanager.TaskManagerBinder
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import okhttp3.Callback

@Composable
fun TasksTableHeader() {
    HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE8E9EB))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 10.dp)
    ) {
        val context = LocalContext.current
        val taskHeaders = context.resources.getStringArray(R.array.tasks_headers_chinese)
        val taskItemWeights =
            context.resources.getIntArray(R.array.task_item_weights).map { it / 100f }
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(24.dp)
        ) {
            taskHeaders.forEachIndexed { index, header ->
                Text(
                    text = header,
                    modifier = Modifier
                        .weight(taskItemWeights.getOrNull(index) ?: 0.1f)
                        .padding(horizontal = 10.dp),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (index != taskHeaders.lastIndex) VerticalDivider(modifier = Modifier.height(8.dp))
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessView(displayMode: DisplayMode, searchBarValue: String) {
    val taskInfoList = remember { mutableStateListOf<Adapters.TaskInfo>() }
    val coroutineScope = rememberCoroutineScope()
    val initialLoad = remember { mutableStateOf(false) }
    val userName = TaskManagerBinder.getUserName()
    LaunchedEffect(Unit) {
        if (!initialLoad.value) {
            initialLoad.value = true
            coroutineScope.launch {
                taskInfoList.clear()
                val allTasks = TaskManagerBinder.getTasks()
                taskInfoList.addAll(allTasks)

                while (true) {
                    val currentPids = TaskManagerBinder.getTaskPids()
                    taskInfoList.removeAll { it.pid !in currentPids }
                    for (i in currentPids.indices step 20) {
                        val batch = currentPids.subList(i, minOf(i + 20, currentPids.size))
                        batch.forEach { pid ->
                            val taskInfo = TaskManagerBinder.getTaskByPid(pid)
                            if (taskInfo == null) {
                                taskInfoList.removeAll { it.pid == pid }
                                return@forEach
                            }
                            // 检查这个pid是否是之前列表中的
                            val existingIndex = taskInfoList.indexOfFirst { it.pid == pid }
                            if (existingIndex != -1) {
                                taskInfoList[existingIndex] = taskInfo // 若是则在原基础更新
                            } else {
                                taskInfoList.add(taskInfo) // 若不是则添加
                            }
                        }
                        delay(100)
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.background(Color(0xFFFCFDFF))) {
        TasksTableHeader()
        LazyColumn {
            items(taskInfoList, key = { it.pid }) {
                TaskItem(it, displayMode, userName, searchBarValue)
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


@Composable
fun TaskItem(
    taskInfo: Adapters.TaskInfo, displayMode: DisplayMode, userName: String, searchBarValue: String
) {
    val context = LocalContext.current
    val taskItemWeights = context.resources.getIntArray(R.array.task_item_weights).map { it / 100f }
    val taskDropdownMenuItems = context.resources.getStringArray(R.array.task_dropdown_menu_items)
    val floatingMenuPosition = remember { mutableStateOf(Offset.Zero) }
    val floatingMenuExpanded = remember { mutableStateOf(false) }
    val floatingPropertiesWindowShow = remember { mutableStateOf(false) }
    val floatingPriorityModificationWindowShow = remember { mutableStateOf(false) }
    var priorityModificationSliderValue = remember { mutableIntStateOf(taskInfo.nice) }
    val iconBitmap = TaskManagerBinder.getIconBitmapByTaskName(taskInfo.name.toString())
    val iconExists = iconBitmap != null

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
    val searchBarFilteredBoolean = displayMode == DisplayMode.SEARCH_FILTERED_PROCESSES
            && (taskInfo.pid.toString().contains(searchBarValue)
            || taskInfo.name.toString().contains(searchBarValue))

    if (allProcessBoolean || myProcessBoolean || searchBarFilteredBoolean) {
        Row(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .clickable(onClick = {})
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                floatingMenuPosition.value = event.changes.first().position
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
            taskItemWeights.forEachIndexed { index, taskItemWeight ->
                Row(
                    modifier = Modifier
                        .weight(taskItemWeights.getOrNull(index) ?: 0.1f)
                        .padding(start = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (index == 0) {
                        if (iconExists) {
                            Image(
                                bitmap = iconBitmap,
                                modifier = Modifier.size(28.dp),
                                contentDescription = null
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_linux),
                                modifier = Modifier.size(28.dp),
                                contentDescription = null
                            )
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

            DropdownMenu(
                expanded = floatingMenuExpanded.value,
                onDismissRequest = { floatingMenuExpanded.value = false },
                offset = with(LocalDensity.current) {
                    DpOffset(
                        x = floatingMenuPosition.value.x.toDp(), y = (-30).dp
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                val callbackFunctionsMap = mapOf<String, () -> Unit>(
                    "属性" to {
                        floatingMenuExpanded.value = false
                        floatingPropertiesWindowShow.value = true
                    },
                    "内存映射" to {
                        // TODO: 内存映射
                    },
                    "打开文件" to {
                        // TODO: 打开文件
                    },
                    "更改优先级" to {
                        floatingMenuExpanded.value = false
                        floatingPriorityModificationWindowShow.value = true
                    },
                    "设置关联" to {
                        // TODO: 设置关联
                    },
                    "停止进程" to {
                        TaskManagerBinder.killTaskByPid(taskInfo.pid)
                    },
                    "继续" to {
                        // TODO: 继续
                    },
                    "终止" to {
                        TaskManagerBinder.killTaskByPid(taskInfo.pid)
                    },
                    "强制终止" to {
                        TaskManagerBinder.killTaskByPid(taskInfo.pid)
                    },
                    "__DIVIDER__" to {
                        //  TODO: 分割线
                    },
                )

                taskDropdownMenuItems.map { it ->
                    if (it == "__DIVIDER__") HorizontalDivider()
                    else {
                        DropdownMenuItem(
                            text = { Text(it) }, onClick = {
                                callbackFunctionsMap[it]?.let { it1 -> it1() }
                            }, modifier = Modifier
                                .height(32.dp)
                                .width(192.dp)
                        )
                    }
                }
            }
        }
    }
}
