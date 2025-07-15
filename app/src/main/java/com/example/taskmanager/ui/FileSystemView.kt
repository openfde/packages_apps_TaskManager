package com.example.taskmanager.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskmanager.Adapters
import com.example.taskmanager.R
import com.example.taskmanager.TaskManagerBinder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FileSystemView() {
    val fileSystemUsageState = remember {
        mutableStateListOf<Adapters.DiskPartition>()
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val diskPartitionColors =
        context.resources.getIntArray(R.array.disk_partition_colors).map { Color(it) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val fileSystemUsage = TaskManagerBinder.getFileSystemUsage()
            fileSystemUsage.map { it ->
                Log.d("COLD", it.percent.toString())
            }
            fileSystemUsageState.clear()
            fileSystemUsageState.addAll(fileSystemUsage)
            delay(500)
        }
    }

    Column {
        DiskPartitionsTableHeader()
        LazyColumn {
            fileSystemUsageState.mapIndexed { index, diskPartition ->
                item {
                    DiskPartitionItem(
                        diskPartition,
                        diskPartitionColors[index % diskPartitionColors.size]
                    )
                }
            }
        }
    }
}

@Composable
fun DiskPartitionsTableHeader() {
    HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE8E9EB))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 10.dp)
    ) {
        val context = LocalContext.current
        val disksHeaders = context.resources.getStringArray(R.array.disks_headers_chinese)
        val diskHeaderWeights =
            context.resources.getIntArray(R.array.disk_item_weights).map { it / 1000f }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(24.dp)
        ) {
            disksHeaders.forEachIndexed { index, header ->
                Text(
                    text = header,
                    modifier = Modifier
                        .weight(diskHeaderWeights.getOrNull(index) ?: 0.1f)
                        .padding(horizontal = 10.dp),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (index != disksHeaders.lastIndex)
                    VerticalDivider(modifier = Modifier.height(8.dp))
            }
        }
    }
    HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE8E9EB))
}

@Composable
fun DiskPartitionItem(diskPartition: Adapters.DiskPartition, color: Color) {
    val context = LocalContext.current
    val taskItemWeights =
        context.resources.getIntArray(R.array.disk_item_weights).map { it / 1000f }

    Row(
        modifier = Modifier
            .height(30.dp)
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clickable(onClick = {}),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val m = listOf<String>(
            "${diskPartition.used}B",
            diskPartition.catalogue,
            diskPartition.device,
            diskPartition.type,
            diskPartition.storage,
            diskPartition.available
        )
        taskItemWeights.forEachIndexed { index, taskItemWeight ->
            Row(
                modifier = Modifier
                    .weight(taskItemWeights.getOrNull(index) ?: 0.1f)
                    .padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = m[index].toString(),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (index == 0) {
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xffEBEBEB))
                    ) {
                        Box(
                            modifier = Modifier
                                .width(180.dp * (diskPartition.percent / 100f))
                                .fillMaxHeight()
                                .background(color)
                        )
                        Text(
                            "${diskPartition.percent / 100f}%",
                            modifier = Modifier
                                .align(Alignment.Center),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
