package com.example.taskmanager.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset

@Composable
fun TableHeader() {
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
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(24.dp)
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
                if (index != taskHeaders.lastIndex)
                    VerticalDivider(modifier = Modifier.height(8.dp))
            }
        }
    }
    HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE8E9EB))
}

@Composable
fun ProcessView() {
    val taskInfoList = remember { mutableStateListOf<Adapters.TaskInfo>() }
    val coroutineScope = rememberCoroutineScope()
    val initialLoad = remember { mutableStateOf(false) }
    val pidToIndex = remember { mutableMapOf<Int, Int>() }

    LaunchedEffect(Unit) {
        if (!initialLoad.value) {
            initialLoad.value = true
            coroutineScope.launch {
                taskInfoList.clear()
                val allTasks = TaskManagerBinder.getTasks()
                allTasks.forEachIndexed { index, taskInfo ->
                    pidToIndex[taskInfo.pid] = index
                    taskInfoList.add(taskInfo)
                }

                while (true) {
                    delay(1000)
                    val currentPids = TaskManagerBinder.getTaskPids().toSet()
                    currentPids.forEach { pid ->
                        val existingIndex = pidToIndex.getOrDefault(pid, -1)
                        val taskInfo = TaskManagerBinder.getTaskByPid(pid) ?: return@forEach

                        if (existingIndex != -1) {
                            taskInfoList[existingIndex] = taskInfo
                        } else if (taskInfo.pid !in pidToIndex) {
                            taskInfoList.add(taskInfo)
                        } else if (taskInfo.pid in pidToIndex) {
                            pidToIndex.remove(taskInfo.pid)
                        }
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.background(Color(0xFFFCFDFF))) {
        TableHeader()
        LazyColumn {
            items(taskInfoList, key = { it.pid }) { TaskItem(it) }
        }
    }
}

fun toStringWithUnit(bytes: Long): String {
    return when {
        bytes > 1024 * 1024 -> "%.1f MB".format(bytes / (1024f * 1024f))
        bytes > 1024 -> "%.1f KB".format(bytes / 1024f)
        else -> "$bytes B"
    }
}


@Composable
fun TaskItem(taskInfo: Adapters.TaskInfo) {
    val context = LocalContext.current
    val taskItemWeights = context.resources.getIntArray(R.array.task_item_weights).map { it / 100f }
    val taskDropdownMenuItems = context.resources.getStringArray(R.array.task_dropdown_menu_items)
    val floatingMenuPosition = remember { mutableStateOf(Offset.Zero) }
    val floatingMenuExpanded = remember { mutableStateOf(false) }

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
                    Image(
                        painter = painterResource(id = R.drawable.ic_linux),
                        modifier = Modifier.size(28.dp),
                        contentDescription = null
                    )
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
                    x = floatingMenuPosition.value.x.toDp(),
                    y = (-30).dp
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            taskDropdownMenuItems.mapIndexed { index, it ->
                if(it == "__DIVIDER__")
                {
                    HorizontalDivider()
                } else {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = { /* Do something... */ },
                        modifier = Modifier.height(32.dp).width(192.dp)
                    )
                }
            }
        }
    }
}
