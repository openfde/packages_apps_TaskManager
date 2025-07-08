package com.example.taskmanager

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import coil.compose.rememberAsyncImagePainter
import android.openfde.ITaskManager
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Base64
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {

            val taskInfo1 = TaskManagerBinder.getTaskInfoByPid(55316)
            Log.d("COLD",taskInfo1.toString())

        }
        enableEdgeToEdge()
        setContent {
            MyApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

// 进程
@Serializable
object ProcessRoute

// 资源
@Serializable
object ResourceRoute

// 文件系统
@Serializable
object FileSystemRoute

@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = ProcessRoute) {
        composable<ProcessRoute> {
            ProcessView()
        }
        composable<ResourceRoute> {
            Text("this is Resource")
        }
        composable<FileSystemRoute> {
            Text("this is FileSystem")
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

data class HeaderItem(val name: String, val weight: Float)
data class ProcessInfo(
    val icon: String, // ICON图标
    val name: String, // 进程名称
    val user: String, // 用户
    var virtualMemory: Int, // 虚拟内存
    var cpu: Float, // CPU
    val pid: Int, // ID
    var memory: Double, // 内存
    var diskReadAll: Int, // 读盘总量
    var diskWriteAll: Int, // 写入总量
    var diskRead: Int, // 磁盘读取
    var diskWrite: Int // 磁盘写入
)

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
            .size(width = 160.dp, height = 32.dp) // 尺寸
            .background(
                color = Color.Black.copy(alpha = 0.05f), // 背景 rgba(0,0,0,0.05)
                shape = RoundedCornerShape(5.dp) // 圆角
            )
            .border(
                width = 1.dp, color = Color.Black.copy(alpha = 0.05f), // 边框颜色
                shape = RoundedCornerShape(5.dp)
            ),
        contentAlignment = Alignment.Center // 添加此行以实现内容居中
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

@Composable
fun ProcessView() {

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {

        }
    }


    val headers = listOf(
        HeaderItem(name = "进程名称", weight = 0.15f),
        HeaderItem(name = "用户", weight = 0.1f),
        HeaderItem(name = "虚拟内存", weight = 0.12f),
        HeaderItem(name = "% CPU", weight = 0.08f),
        HeaderItem(name = "ID", weight = 0.08f),
        HeaderItem(name = "内存", weight = 0.1f),
        HeaderItem(name = "读盘容量", weight = 0.12f),
        HeaderItem(name = "写入容量", weight = 0.12f),
        HeaderItem(name = "磁盘读取", weight = 0.1f),
        HeaderItem(name = "磁盘写入", weight = 0.1f)
    )

    val processInfoList = remember {
        mutableStateListOf<ProcessInfo>().apply {
            add(
                ProcessInfo(
                    "https://media.geeksforgeeks.org/wp-content/uploads/20210101144014/gfglogo.png",
                    "Process1", "User1", 1024, 50.0f, 123, 512.0, 1024, 1024, 512, 512
                )
            )
        }
    }

    val textFieldState = rememberTextFieldState(initialText = "Hello")
    val (query, onQueryChange) = rememberSaveable { mutableStateOf("") }

    Column {
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            OuterBox()
            SearchBar(
                query = query, onQueryChange = onQueryChange, placeholder = "搜索"
            )
        }

        HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color(0x0D000000))

        // header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            headers.forEachIndexed { index, header ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .weight(header.weight)
                        .padding(10.dp)
                ) {
                    Text(text = header.name, fontSize = 14.sp)
                }

                if (index < headers.size - 1) {
                    VerticalDivider(
                        modifier = Modifier.height(18.dp), color = Color(0x0D000000)
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color(0x0D000000))

        // item
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // item row
            processInfoList.forEachIndexed { index, processInfo ->
                Row(
                    modifier = Modifier
                        .weight(headers[0].weight)
                        .padding(10.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(processInfo.icon),
//                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,

                        modifier = Modifier
                            .size(width = 21.75.dp, height = 21.75.dp)
                    )
                    Text(text = processInfo.name, fontSize = 14.sp)
                }

                var itemValues = listOf(
                    processInfo.user,
                    processInfo.virtualMemory,
                    processInfo.cpu,
                    processInfo.pid,
                    processInfo.memory,
                    processInfo.diskReadAll,
                    processInfo.diskWriteAll,
                    processInfo.diskRead,
                    processInfo.diskWrite
                )

                itemValues
                    .forEachIndexed { index, itemValue ->
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .weight(headers[index + 1].weight)
                                .padding(10.dp)
                        ) {
                            Text(text = itemValue.toString(), fontSize = 14.sp)
                        }
                    }
            }
        }
    }
}


