package com.example.taskmanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskmanager.Adapters
import com.example.taskmanager.R
import com.example.taskmanager.TaskManagerBinder
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 10.dp)
    ) {
        val context = LocalContext.current
        val taskHeaders = context.resources.getStringArray(R.array.tasks_headers_chinese)
        val taskItemWeights =
            context.resources.getIntArray(R.array.task_item_weights).map { it / 100f }
        Row(verticalAlignment = Alignment.CenterVertically) {
            taskHeaders.forEachIndexed { index, header ->
                Text(
                    text = header,
                    modifier = Modifier
                        .height(30.dp)
                        .weight(taskItemWeights.getOrNull(index) ?: 0.1f)
                        .background(Color.White)
                        .padding(horizontal = 10.dp),
                    fontSize = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                VerticalDivider(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun ProcessView() {
    val taskInfoList = remember { mutableStateListOf<Adapters.TaskInfo>() }
    val coroutineScope = rememberCoroutineScope()
    val initialLoad = remember { mutableStateOf(false) }
    LaunchedEffect(initialLoad.value) {
        if (!initialLoad.value) {
            initialLoad.value = true
            coroutineScope.launch {
                val taskPids = TaskManagerBinder.getTaskPids()
                taskInfoList.clear()
                taskPids.forEach {
                    TaskManagerBinder.getTaskByPid(it)?.let { taskInfoList.add(it) }
                }
            }
        }
    }

    Column {
        Header()
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            taskInfoList.forEach {
                TaskItem(it)
                HorizontalDivider(modifier = Modifier.weight(0.05f))
            }
        }
    }
}

@Composable
fun TaskItem(taskInfo: Adapters.TaskInfo) {
    val context = LocalContext.current
    val taskHeaders = context.resources.getStringArray(R.array.tasks_headers_chinese)
    val taskItemWeights = context.resources.getIntArray(R.array.task_item_weights).map { it / 100f }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 10.dp)
    ) {
        val m = listOf(
            taskInfo.name.toString(),
            taskInfo.user.toString(),
            taskInfo.vmsize.toString(),
            taskInfo.cpuUsage.toString(),
            taskInfo.cpuUsage.toString(),
            taskInfo.rss.toString(),
            taskInfo.readBytes.toString(),
            taskInfo.writeBytes.toString(),
            taskInfo.readIssued.toString(),
            taskInfo.writeIssued.toString()
        )
        taskHeaders.forEachIndexed { index, taskItemWeight ->
            Text(
                text = m[index].toString(),
                modifier = Modifier
                    .height(30.dp)
                    .weight(taskItemWeights.getOrNull(0) ?: 0.1f),
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}


@Composable
fun InnerBox(name: String, selected: Boolean) {
    Surface(
        modifier = Modifier.size(width = 93.dp, height = 26.dp),
        shape = RoundedCornerShape(4.dp),
        shadowElevation = if (selected) 1.dp else 0.dp,
    ) {
        Box(
            modifier = Modifier.background(
                if (selected) Color.White else Color(0x0A000000)
            )
        ) {
            Text(
                text = name, modifier = Modifier.align(Alignment.Center), fontSize = 14.sp
            )
        }
    }
}

@Composable
fun OuterBox() {
    Box(
        modifier = Modifier
            .size(295.dp, 32.dp)
            .alpha(1f)
            .background(
                color = Color(0x0A000000), shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 1.dp, color = Color(0x0D000000), shape = RoundedCornerShape(6.dp)
            ), contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            InnerBox(name = "进程", selected = true)
            VerticalDivider(
                modifier = Modifier.height(18.dp), color = Color(0x0D000000)
            )
            InnerBox(name = "资源", selected = false)
            VerticalDivider(
                modifier = Modifier.height(18.dp), color = Color(0x0D000000)
            )
            InnerBox(name = "文件系统", selected = false)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String
) {

    Box(
        modifier = Modifier
            .size(width = 160.dp, height = 32.dp)
            .background(
                color = Color.Black.copy(alpha = 0.05f),
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 1.dp, color = Color.Black.copy(alpha = 0.05f),
            ),
        contentAlignment = Alignment.Center
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(placeholder, color = Color.Black) },
            singleLine = true,
            shape = RoundedCornerShape(5.dp),
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            )
        )
    }
}