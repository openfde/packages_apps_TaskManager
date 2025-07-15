package com.example.taskmanager.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskmanager.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.navigation.NavController
import com.example.taskmanager.TaskManagerBinder


sealed class AppRoute(val route: String) {
    object Process : AppRoute("process")
    object Resource : AppRoute("resource")
    object FileSystem : AppRoute("file_system")
}


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun NavigationView() {
    val navController = rememberNavController()
    Column {
        Row(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .background(Color(0xFFF7F7F7)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LogoBar()
            NavOuterBox(navController)
            WindowButtonsBar()
        }
        NavHost(navController, startDestination = AppRoute.Process.route) {
            listOf(
                AppRoute.Process to @Composable { ProcessView() },
                AppRoute.Resource to @Composable { ResourceView() },
                AppRoute.FileSystem to @Composable { FileSystemView() }).forEach { (route, content) ->
                composable(route.route) { content() }
            }
        }
    }
}

@Composable
fun SearchBar(
    text: String,
    onTextChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .size(width = 160.dp, height = 32.dp)
            .background(
                color = Color(0x0D000000), // 设置背景颜色为 #0d000000
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 1.dp,
                color = Color.Black.copy(alpha = 0.05f), // 边框风格 border: 1px solid rgba(0, 0, 0, 0.05)
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.search_icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )

        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun WindowOptionsDisplayProcessSubDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    offset: Offset
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            onDismissRequest()
        },
        offset = with(LocalDensity.current) {
            DpOffset(
                x = offset.x.toDp() - 192.dp,
                y = offset.y.toDp()
            )
        },
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
    ) {
        DropdownMenuItem(
            text = { Text("所有进程") },
            onClick = {

            },
            modifier = Modifier
                .height(32.dp)
                .width(192.dp)
        )
        DropdownMenuItem(
            text = { Text("活动进程") },
            onClick = {

            },
            modifier = Modifier
                .height(32.dp)
                .width(192.dp)
        )
        DropdownMenuItem(
            text = { Text("我的进程") },
            onClick = {

            },
            modifier = Modifier
                .height(32.dp)
                .width(192.dp)
        )
    }
}


@Composable
fun WindowButtonsBar() {
    val searchBarValue = remember { mutableStateOf<String>("") }
    val windowOptionsDropdownMenuOffset = remember {
        mutableStateOf<Offset>(Offset.Zero)
    }
    val windowOptionsDropdownSubMenuOffset = remember {
        mutableStateOf<Offset>(Offset.Zero)
    }
    val windowOptionsDropdownMenuShow = remember {
        mutableStateOf<Boolean>(false)
    }
    val windowOptionsDropdownSubMenuShow = remember {
        mutableStateOf<Boolean>(false)
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 12.dp, end = 8.dp)
    ) {
        WindowOptionsDisplayProcessSubDropdownMenu(
            expanded = windowOptionsDropdownSubMenuShow.value,
            onDismissRequest = {
                windowOptionsDropdownSubMenuShow.value = false
            },
            offset = windowOptionsDropdownMenuOffset.value
        )
        DropdownMenu(
            expanded = windowOptionsDropdownMenuShow.value,
            onDismissRequest = { windowOptionsDropdownMenuShow.value = false },
            offset = with(LocalDensity.current) {
                DpOffset(
                    x = windowOptionsDropdownMenuOffset.value.x.toDp(),
                    y = windowOptionsDropdownMenuOffset.value.y.toDp()
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            DropdownMenuItem(
                text = { Text("刷新") },
                onClick = {

                },
                modifier = Modifier
                    .height(32.dp)
                    .width(192.dp)
            )
            DropdownMenuItem(
                modifier = Modifier.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press) {
                                windowOptionsDropdownSubMenuOffset.value =
                                    event.changes.first().position
                                windowOptionsDropdownSubMenuShow.value = true
                            }
                        }
                    }
                },
                onClick = {},
                text = { Text("进程显示") }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("显示依赖项") },
                onClick = {

                },
                modifier = Modifier
                    .height(32.dp)
                    .width(192.dp)
            )
            DropdownMenuItem(
                text = { Text("搜索打开的文件") },
                onClick = {

                },
                modifier = Modifier
                    .height(32.dp)
                    .width(192.dp)
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("偏好设置") },
                onClick = {

                },
                modifier = Modifier
                    .height(32.dp)
                    .width(192.dp)
            )
            DropdownMenuItem(
                text = { Text("帮助") },
                onClick = {

                },
                modifier = Modifier
                    .height(32.dp)
                    .width(192.dp)
            )
            DropdownMenuItem(
                text = { Text("快捷键") },
                onClick = {

                },
                modifier = Modifier
                    .height(32.dp)
                    .width(192.dp)
            )
            DropdownMenuItem(
                text = { Text("关于") },
                onClick = {

                },
                modifier = Modifier
                    .height(32.dp)
                    .width(192.dp)
            )
        }
        SearchBar(
            text = searchBarValue.value, onTextChange = { searchBarValue.value = it })
        Image(
            painter = painterResource(id = R.drawable.window_options_button),
            modifier = Modifier
                .size(28.dp)
                .clickable(
                    onClick = {})
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press) {
                                windowOptionsDropdownMenuOffset.value =
                                    event.changes.first().position
                                windowOptionsDropdownMenuShow.value = true
                            }
                        }
                    }
                },
            contentDescription = null,
        )

        Image(
            painter = painterResource(id = R.drawable.window_max_button),
            modifier = Modifier.size(28.dp),
            contentDescription = null
        )
        Image(
            painter = painterResource(id = R.drawable.window_mini_button),
            modifier = Modifier.size(28.dp),
            contentDescription = null
        )
        Image(
            painter = painterResource(id = R.drawable.window_normal_button),
            modifier = Modifier.size(28.dp),
            contentDescription = null
        )
        Image(
            painter = painterResource(id = R.drawable.window_close_button),
            modifier = Modifier.size(28.dp),
            contentDescription = null
        )
    }
}

@Composable
fun LogoBar() {
    Row(modifier = Modifier.padding(start = 14.dp)) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            modifier = Modifier.size(24.dp),
            contentDescription = null
        )
        Text(
            text = "资源管理器", modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp
        )
    }
}

@Composable
fun NavInnerBox(name: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(width = 93.dp, height = 26.dp),
        shape = RoundedCornerShape(4.dp),
        shadowElevation = if (selected) 1.dp else 0.dp,
        onClick = onClick,
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
fun NavOuterBox(navController: NavController) {
    val selectedItem = remember { mutableStateOf(AppRoute.Process.route) }

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
            NavInnerBox(
                name = "进程", selected = selectedItem.value == AppRoute.Process.route, onClick = {
                    navController.navigate(AppRoute.Process.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                    selectedItem.value = AppRoute.Process.route
                })
            VerticalDivider(
                modifier = Modifier.height(18.dp), color = Color(0x0D000000)
            )
            NavInnerBox(
                name = "资源", selected = selectedItem.value == AppRoute.Resource.route, onClick = {
                    navController.navigate(AppRoute.Resource.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                    selectedItem.value = AppRoute.Resource.route
                })
            VerticalDivider(
                modifier = Modifier.height(18.dp), color = Color(0x0D000000)
            )
            NavInnerBox(
                name = "文件系统",
                selected = selectedItem.value == AppRoute.FileSystem.route,
                onClick = {
                    navController.navigate(AppRoute.FileSystem.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                    selectedItem.value = AppRoute.FileSystem.route
                })
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
                color = Color.Black.copy(alpha = 0.05f), shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 1.dp, color = Color.Black.copy(alpha = 0.05f),
            ), contentAlignment = Alignment.Center
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
