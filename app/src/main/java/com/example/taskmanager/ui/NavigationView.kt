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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.navigation.NavController


sealed class AppRoute(val route: String) {
    object Process : AppRoute("process")
    object Resource : AppRoute("resource")
    object FileSystem : AppRoute("file_system")
}


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun NavigationView() {
    val navController = rememberNavController()
    val displayModeState = remember { mutableStateOf(DisplayMode.ALL_PROCESSES) }
    val searchBarValue = remember { mutableStateOf("") }
    val isTitleBarHidden = remember { mutableStateOf(true) }
    Column(modifier = Modifier.padding(top=35.dp)) {
        Row(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .background(Color(0xFFF7F7F7)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isTitleBarHidden.value) {
                Spacer(Modifier.width(20.dp))
            } else {
                LogoBar()
            }
            NavOuterBox(navController)
            WindowButtonsBar(
                isTitleBarHidden.value,
                onDisplayModeChange = { displayModeState.value = it },
                onSearchBarChange = { searchBarValue.value = it },
                searchBarValue.value
            )
        }
        NavHost(navController, startDestination = AppRoute.Process.route) {
            listOf(
                AppRoute.Process to @Composable {
                ProcessView(
                    displayModeState.value, searchBarValue.value
                )
            },
                AppRoute.Resource to @Composable { ResourceView() },
                AppRoute.FileSystem to @Composable { FileSystemView() }).forEach { (route, content) ->
                composable(route.route) { content() }
            }
        }
    }
}

@Composable
fun SearchBar(
    text: String, onValueChange: (String) -> Unit
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
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun WindowOptionsDisplayProcessSubDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    offset: Offset,
    onDisplayModeChange: (DisplayMode) -> Unit
) {
    DropdownMenu(
        expanded = expanded, onDismissRequest = {
        onDismissRequest()
    }, offset = with(LocalDensity.current) {
        DpOffset(
            x = offset.x.toDp() - 192.dp, y = offset.y.toDp()
        )
    }, modifier = Modifier.clip(RoundedCornerShape(8.dp))
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.submenu_all_processes)) }, onClick = {
            onDisplayModeChange(DisplayMode.ALL_PROCESSES)
        }, modifier = Modifier
                .height(32.dp)
                .width(192.dp)
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.submenu_active_processes)) }, onClick = {
            onDisplayModeChange(DisplayMode.ACTIVE_PROCESSES)
        }, modifier = Modifier
                .height(32.dp)
                .width(192.dp)
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.submenu_my_processes)) }, onClick = {
            onDisplayModeChange(DisplayMode.MY_PROCESSES)
        }, modifier = Modifier
                .height(32.dp)
                .width(192.dp)
        )
    }
}


@Composable
fun WindowButtonsBar(
    isTitleBarHidden: Boolean,
    onDisplayModeChange: (DisplayMode) -> Unit,
    onSearchBarChange: (String) -> Unit,
    searchBarValue: String
) {
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 12.dp, end = 8.dp)
    ) {
        WindowOptionsDisplayProcessSubDropdownMenu(
            expanded = windowOptionsDropdownSubMenuShow.value,
            onDismissRequest = {
                windowOptionsDropdownSubMenuShow.value = false
            },
            offset = windowOptionsDropdownMenuOffset.value,
            onDisplayModeChange = { mode ->
                onDisplayModeChange(mode)
            })
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
                text = { Text(stringResource(R.string.menu_refresh)) }, onClick = {

            }, modifier = Modifier
                    .height(32.dp)
                    .width(192.dp)
            )
            DropdownMenuItem(modifier = Modifier.pointerInput(Unit) {
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
            }, onClick = {}, text = { Text(stringResource(R.string.menu_process_display)) })
            HorizontalDivider()
//            显示依赖项
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_show_dependencies)) }, onClick = {

            }, modifier = Modifier
                    .height(32.dp)
                    .width(192.dp)
            )

//            搜索打开的文件
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_search_open_files)) }, onClick = {
            }, modifier = Modifier
                    .height(32.dp)
                    .width(192.dp)
            )
            HorizontalDivider()
//            偏好设置
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_preferences)) }, onClick = {

            }, modifier = Modifier
                    .height(32.dp)
                    .width(192.dp)
            )
//            帮助
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_help)) }, onClick = {
            }, modifier = Modifier
                    .height(32.dp)
                    .width(192.dp)
            )
//            快捷键
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_shortcuts)) }, onClick = {
            }, modifier = Modifier
                    .height(32.dp)
                    .width(192.dp)
            )
//            关于
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_about)) }, onClick = {

            }, modifier = Modifier
                    .height(32.dp)
                    .width(192.dp)
            )
        }
        SearchBar(
            text = searchBarValue, onValueChange = { it ->
                onSearchBarChange(it)
                if (searchBarValue != "") {
                    onDisplayModeChange(DisplayMode.SEARCH_FILTERED_PROCESSES)
                }
            })
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
        if (!isTitleBarHidden) {
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
}

@Composable
fun LogoBar() {
    val context = LocalContext.current
    Row(
        modifier = Modifier.padding(start = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            modifier = Modifier.size(24.dp),
            contentDescription = null
        )
        Text(
            text = context.getString(R.string.app_name), modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp
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
    val context = LocalContext.current

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
                name = context.getString(R.string.processes_tab),
                selected = selectedItem.value == AppRoute.Process.route,
                onClick = {
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
                name = context.getString(R.string.resources_tab),
                selected = selectedItem.value == AppRoute.Resource.route,
                onClick = {
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
                name = context.getString(R.string.filesystems_tab),
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