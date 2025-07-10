package com.example.taskmanager.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.material3.DataTable
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay

@Composable
fun ProcessView() {
    val taskInfoList = remember { mutableStateListOf<Adapters.TaskInfo>() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val taskHeaders = context.resources.getStringArray(R.array.tasks_headers)

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val taskPids =TaskManagerBinder.getTaskPids()
            taskPids.forEach {
                val taskInfo = TaskManagerBinder.getTaskByPid(it)
                if (taskInfo != null) {
                    taskInfoList.add(taskInfo)
                }
                delay(1000)
            }
        }
    }

    DataTable(
        columns = taskHeaders.map {
            DataColumn {
                Text(it)
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        taskInfoList.map {
            row {
                cell {
                    Row {
                        val taskIcon = remember {
                            mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
                        }
                        LaunchedEffect(Unit) {
                            taskIcon.value =
                                TaskManagerBinder.getIconBitmapByTaskName(it.name.toString())
                        }

/*                        if (taskIcon.value != null) {
                            Image(
                                bitmap = taskIcon.value!!,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_linux),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                        }
*/

                        Image(
                            painter = painterResource(id = R.drawable.ic_linux),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )

                        Text(
                            text = it.name.toString(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                cell { Text(text = it.user.toString()) }
                cell { Text(text = it.vmsize.toString()) }
                cell { Text(text = it.cpuUsage.toString() + "%") }
                cell { Text(text = it.pid.toString()) }
                cell { Text(text = it.rss.toString()) }
                cell { Text(text = it.readBytes.toString()) }
                cell { Text(text = it.writeBytes.toString()) }
                cell { Text(text = it.readIssued.toString()) }
                cell { Text(text = it.writeIssued.toString()) }
            }
        }
    }
}

@Composable
fun TaskItem(taskInfo: Adapters.TaskInfo) {
    val coroutineScope = rememberCoroutineScope()
    val taskIcon = remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val bitmap = TaskManagerBinder.getIconBitmapByTaskName(taskInfo.name.toString())
            // set task icon
            taskIcon.value = bitmap
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            if (taskIcon.value != null) {
                Image(
                    bitmap = taskIcon.value!!,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_linux),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = taskInfo.name.toString(),
                modifier = Modifier.weight(0.15f),
                fontSize = 20.sp
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