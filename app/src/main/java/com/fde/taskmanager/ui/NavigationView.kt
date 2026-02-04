package com.fde.taskmanager.ui

import android.app.Instrumentation
import android.openfde.AppTaskControllerProxy
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fde.taskmanager.MainActivity.ToolbarViewModel
import com.fde.taskmanager.R


sealed class AppRoute(val route: String) {
    object Process : AppRoute("process")
    object Resource : AppRoute("resource")
    object FileSystem : AppRoute("file_system")
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

private fun simulateKeyPress(keyCode: Int) {
    Thread {
        try {
            val instrumentation = Instrumentation()
            instrumentation.sendKeyDownUpSync(keyCode)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }.start()
}


@Composable
fun WindowButtonsBar(
    toolbarViewModel: ToolbarViewModel,
    isButtonHidden: MutableState<Boolean>,
    appTaskController: AppTaskControllerProxy,
    windowingMode: MutableState<Int>?,
    isSystemBarVisible: MutableState<Boolean>?
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
    val searchBarValueState = remember { mutableStateOf("") }

    val context = LocalContext.current
    val isFullScreen = remember { mutableStateOf(false) }
    val isAboutShow = remember { mutableStateOf(false) }
    val (versionName,packageName) = remember {
        val packageManager = context.packageManager
        packageManager.getPackageInfo(context.packageName, 0).versionName to
        packageManager.getPackageInfo(context.packageName, 0).packageName
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 12.dp, end = 0.dp)
    ) {
        WindowOptionsDisplayProcessSubDropdownMenu(
            expanded = windowOptionsDropdownSubMenuShow.value,
            onDismissRequest = {
                windowOptionsDropdownSubMenuShow.value = false
            },
            offset = windowOptionsDropdownMenuOffset.value,
            onDisplayModeChange = { mode ->
                toolbarViewModel.changeDisplayMode(mode)
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
            if(isButtonHidden.value) {DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_refresh)) }, onClick = {
                    toolbarViewModel.refreshTaskInfoList()
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
            }, onClick = {}, text = { Text(stringResource(R.string.menu_process_display)) })}
//            HorizontalDivider()
//            显示依赖项
//            DropdownMenuItem(
//                text = { Text(stringResource(R.string.menu_show_dependencies)) }, onClick = {
//
//            }, modifier = Modifier
//                    .height(32.dp)
//                    .width(192.dp)
//            )

//            搜索打开的文件
//            DropdownMenuItem(
//                text = { Text(stringResource(R.string.menu_search_open_files)) }, onClick = {
//            }, modifier = Modifier
//                    .height(32.dp)
//                    .width(192.dp)
//            )
//            HorizontalDivider()
//            偏好设置
//            DropdownMenuItem(
//                text = { Text(stringResource(R.string.menu_preferences)) }, onClick = {
//
//            }, modifier = Modifier
//                    .height(32.dp)
//                    .width(192.dp)
//            )
//            帮助
//            DropdownMenuItem(
//                text = { Text(stringResource(R.string.menu_help)) }, onClick = {
//            }, modifier = Modifier
//                    .height(32.dp)
//                    .width(192.dp)
//            )
//            快捷键
//            DropdownMenuItem(
//                text = { Text(stringResource(R.string.menu_shortcuts)) }, onClick = {
//            }, modifier = Modifier
//                    .height(32.dp)
//                    .width(192.dp)
//            )
//            关于
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_about)) }, onClick = {
                    isAboutShow.value = true
            }, modifier = Modifier
                    .height(32.dp)
                    .width(192.dp)
            )
        }

        if (isAboutShow.value) {
            AlertDialog(onDismissRequest = {
                isAboutShow.value = false
            }, title = {
                Text(
                    text = context.getString(R.string.menu_about),
                    fontWeight = FontWeight.W700,
                )
            }, text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            modifier = Modifier.size(64.dp),
                            contentDescription = null
                        )
                    }
                    Row {
                        Text("${context.getString(R.string.app_name_title)}:")
                        Spacer(modifier = Modifier.weight(1f))
                        Text(packageName.toString())
                    }
                    Row {
                        Text("${context.getString(R.string.app_version)}:")
                        Spacer(modifier = Modifier.weight(1f))
                        Text(versionName.toString())
                    }
                }
            }, confirmButton = {}, dismissButton = {
                TextButton(
                    onClick = {
                        isAboutShow.value = false
                    }) {
                    Text(
                        context.getString(R.string.cancel),
                        fontWeight = FontWeight.W700,
                    )
                }
            })
        }

        val isSearchBarHidden = remember { mutableStateOf(false) }


        LaunchedEffect(Unit) {
            toolbarViewModel.navigationEvents.collect { route ->
                if(route == AppRoute.Resource.route) isSearchBarHidden.value = true
                else isSearchBarHidden.value = false
            }
        }

        if(!isSearchBarHidden.value) {
            SearchBar(
                text = searchBarValueState.value, onValueChange = { it ->
                    toolbarViewModel.changeSearchBarValue(it)
                    searchBarValueState.value = it
                    if (it != "") {
                        toolbarViewModel.changeDisplayMode(DisplayMode.SEARCH_FILTERED_PROCESSES)
                    }
            })
        } else Spacer(modifier = Modifier.width(160.dp))

        Image(
            painter = painterResource(id = R.drawable.window_options_button),
            modifier = Modifier
                .size(26.dp)
                .padding(end = 8.dp)
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

            // fullscreen
            val iconFullId =
            if (isSystemBarVisible?.value == true) {
                R.drawable.window_full_screen_button
            } else {
                R.drawable.window_exit_full_screen_button
            }
            Image(
                painter = painterResource(id = iconFullId),
                modifier = Modifier.size(26.dp)
                    .padding(end = 8.dp).clickable {
                    appTaskController.enterOrExitFullscreen();
//                   simulateKeyPress(KeyEvent.KEYCODE_F11);
                },
                contentDescription = null
            )
            // minimize
            Image(
                painter = painterResource(id = R.drawable.window_mini_button),
                modifier = Modifier.size(26.dp)
                    .padding(end = 8.dp).clickable {
                    appTaskController.minimize();
//                    simulateKeyPress(KeyEvent.KEYCODE_F9)
                },
                contentDescription = null
            )
            // normal/maximize
            val iconResId =
            if (windowingMode?.value == 5) {
                R.drawable.window_normal_button
            } else {
                R.drawable.window_maximize_button
            }
            Image(
                painter = painterResource(id = iconResId),
                modifier = Modifier.size(26.dp)
                    .padding(end = 8.dp).clickable {
                    appTaskController.maximizeOrNot();
//                    val intent = Intent("com.fde.fullscreen.ENABLE_OR_DISABLE")
//                    if(isFullScreen.value)
//                        intent.putExtra("mode", 0)
//                    else
//                        intent.putExtra("mode", 1)
//                    isFullScreen.value = !(isFullScreen.value)
//                    context.sendBroadcast(intent)
                },
                contentDescription = null
            )
            // close
            Image(
                painter = painterResource(id = R.drawable.window_close_button),
                modifier = Modifier.size(18.dp)
                    .padding(end = 0.dp).clickable {
                    appTaskController.closeTask();
//                    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//                    val tasks = activityManager.appTasks
//                    if (tasks.isNotEmpty()){
//                        tasks[0].finishAndRemoveTask()
//                    }
                },
                contentDescription = null
            )
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
fun NavOuterBox(navViewModel: ToolbarViewModel) {
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
                    // navController.navigate(AppRoute.Process.route) {
                    //     popUpTo(navController.graph.startDestinationId)
                    //     launchSingleTop = true
                    // }
                    navViewModel.navigateTo(AppRoute.Process.route)
                    selectedItem.value = AppRoute.Process.route
                })
            VerticalDivider(
                modifier = Modifier.height(18.dp), color = Color(0x0D000000)
            )
            NavInnerBox(
                name = context.getString(R.string.resources_tab),
                selected = selectedItem.value == AppRoute.Resource.route,
                onClick = {
                    // navController.navigate(AppRoute.Resource.route) {
                    //     popUpTo(navController.graph.startDestinationId)
                    //     launchSingleTop = true
                    // }
                    navViewModel.navigateTo(AppRoute.Resource.route)
                    selectedItem.value = AppRoute.Resource.route
                })
            VerticalDivider(
                modifier = Modifier.height(18.dp), color = Color(0x0D000000)
            )
            NavInnerBox(
                name = context.getString(R.string.filesystems_tab),
                selected = selectedItem.value == AppRoute.FileSystem.route,
                onClick = {
                    // navController.navigate(AppRoute.FileSystem.route) {
                    //     popUpTo(navController.graph.startDestinationId)
                    //     launchSingleTop = true
                    // }
                    navViewModel.navigateTo(AppRoute.FileSystem.route)
                    selectedItem.value = AppRoute.FileSystem.route
                })
        }
    }
}