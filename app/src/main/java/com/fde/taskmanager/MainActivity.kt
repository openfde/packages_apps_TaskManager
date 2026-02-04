package com.fde.taskmanager

import android.openfde.AppTaskControllerProxy
import android.openfde.AppTaskStatusListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fde.taskmanager.ui.AppRoute
import com.fde.taskmanager.ui.DisplayMode
import com.fde.taskmanager.ui.FileSystemView
import com.fde.taskmanager.ui.LogoBar
import com.fde.taskmanager.ui.NavOuterBox
import com.fde.taskmanager.ui.ProcessView
import com.fde.taskmanager.ui.ResourceView
import com.fde.taskmanager.ui.WindowButtonsBar
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class MainActivity : ComponentActivity() {
    var appTaskController : AppTaskControllerProxy? = null
    lateinit var mWindowingMode: MutableState<Int>
    lateinit var mIsSystemBarVisible: MutableState<Boolean>
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This method is only available when `SystemUISharedLib` 
        // is imported when using Soong to compile under the 
        // Android source tree and keep in sync with 
        // `isTitleBarHidden` in `NavigationView`
        setWindowDecorationStatus(Window.WINDOW_DECORATION_FORCE_HIDE);
        setContentView(R.layout.base_layout)
        val toolbar_compose_view = findViewById<ComposeView>(R.id.toolbar_compose_view)
        val main_frame_compose_view = findViewById<ComposeView>(R.id.main_frame_compose_view)
        val toolbarViewModel by viewModels<ToolbarViewModel>()
        mWindowingMode = mutableIntStateOf(
            5
        )
        mIsSystemBarVisible = mutableStateOf(false)
        appTaskController = AppTaskControllerProxy.create()
        appTaskController?.initCustomCaption(
            WeakReference(this),
            false,
            object : AppTaskStatusListener {
                override fun onStatusChanged(
                    windowingMode: Int,
                    isSystemBarVisible: Boolean
                ) {
                    mWindowingMode.value = windowingMode
                    mIsSystemBarVisible.value  =isSystemBarVisible
                }

            }
        )

        BackgroundTask.startBackgroundTask()
        toolbar_compose_view!!.setContent {
            var isHidden = remember { mutableStateOf(true) }
            val searchBarValueState = remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                toolbarViewModel.navigationEvents.collect { route ->
                    isHidden.value = route == AppRoute.Process.route
                }
            }

            Row(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .background(Color(0xFFF7F7F7)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LogoBar()
                NavOuterBox(toolbarViewModel)
                WindowButtonsBar(toolbarViewModel, isHidden,appTaskController!!,
                    mWindowingMode , mIsSystemBarVisible
                )
            }
        }

        main_frame_compose_view!!.setContent {
            val navController = rememberNavController()
            val displayModeState = remember { mutableStateOf(DisplayMode.ALL_PROCESSES) }
            // `isToolbarHidden` should be synchronized with 
            // `setWindowDecorationStatus` method in MainActivity
            val searchBarValueState = remember { mutableStateOf("") }
            val snackbarHostState = remember { SnackbarHostState() }
            val isServiceAvailable = remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                toolbarViewModel.navigationEvents.collect { route ->
                    if (!isServiceAvailable.value) {
                        launch {
                            snackbarHostState.showSnackbar(
                                message = getString(R.string.service_not_available),
                            )
                        }
                        return@collect
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            LaunchedEffect(toolbarViewModel) {
                toolbarViewModel.displayModeChangeEvents.collect { displayMode ->
                    if (!isServiceAvailable.value) {
                        launch {
                            snackbarHostState.showSnackbar(
                                message = getString(R.string.service_not_available),
                            )
                        }
                        return@collect
                    }
                    displayModeState.value = displayMode
                }
            }

            LaunchedEffect(toolbarViewModel) {
                toolbarViewModel.searchBarValueChangeEvents.collect { value ->
                    if (!isServiceAvailable.value) {
                        launch {
                            snackbarHostState.showSnackbar(
                                message = getString(R.string.service_not_available),
                            )
                        }
                        return@collect
                    }
                    searchBarValueState.value = value
                }
            }

            LaunchedEffect(toolbarViewModel) {
                toolbarViewModel.refreshTaskInfoListEvents.collect { value ->
                    if (!isServiceAvailable.value) {
                        launch {
                            snackbarHostState.showSnackbar(
                                message = getString(R.string.service_not_available),
                            )
                        }
                        return@collect
                    }
                    BackgroundTask.refreshTaskInfoList()
                }
            }

            LaunchedEffect(Unit) {
                coroutineScope {
                    val userName = TaskManagerBinder.getUserName()
                    if (userName == null) {
                        isServiceAvailable.value = false
                        launch {
                            snackbarHostState.showSnackbar(
                                message = getString(R.string.service_not_available),
                            )
                        }
                    } else isServiceAvailable.value = true
                }
            }
            Scaffold(
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                }
            ) { _ ->
                if (isServiceAvailable.value) {
                    NavHost(navController, startDestination = AppRoute.Process.route) {
                        composable(AppRoute.Process.route) {
                            ProcessView(
                                displayModeState.value, searchBarValueState.value,
                                toolbarViewModel
                            )
                        }
                        composable(AppRoute.Resource.route) {
                            ResourceView()
                        }
                        composable(AppRoute.FileSystem.route) {
                            FileSystemView(searchBarValueState.value)
                        }
                    }
                }
            }
        }
       val taskController = TaskController()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    class ToolbarViewModel : ViewModel() {
        private val _navigationEvents = MutableSharedFlow<String>()
        val navigationEvents = _navigationEvents.asSharedFlow()

        fun navigateTo(route: String) {
            viewModelScope.launch {
                _navigationEvents.emit(route)
            }
        }

        private val _displayModeChangeEvents = MutableSharedFlow<DisplayMode>()
        val displayModeChangeEvents = _displayModeChangeEvents.asSharedFlow()

        fun changeDisplayMode(mode: DisplayMode) {
            viewModelScope.launch {
                _displayModeChangeEvents.emit(mode)
            }
        }

        private val _searchBarValueChangeEvents = MutableSharedFlow<String>()
        val searchBarValueChangeEvents = _searchBarValueChangeEvents.asSharedFlow()

        fun changeSearchBarValue(value: String) {
            viewModelScope.launch {
                _searchBarValueChangeEvents.emit(value)
            }
        }

        private val _refreshTaskInfoListEvents = MutableSharedFlow<Unit>()
        val refreshTaskInfoListEvents = _refreshTaskInfoListEvents.asSharedFlow()

        fun refreshTaskInfoList() {
            viewModelScope.launch {
                _refreshTaskInfoListEvents.emit(Unit)
            }
        }
    }
}






