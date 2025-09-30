package com.fde.taskmanager

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.view.Window
import com.fde.taskmanager.R

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.navigation.NavController
import android.content.Intent
import android.view.KeyEvent
import android.app.Instrumentation
import android.content.Context
import android.app.ActivityManager
import android.app.ActivityManager.AppTask
import android.util.Log
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModel
import androidx.activity.viewModels
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.LaunchedEffect
import com.fde.taskmanager.ui.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asSharedFlow


class MainActivity : ComponentActivity() {
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
        val navViewModel by viewModels<NavigationViewModel>()
        val displayModeViewModel by viewModels<DisplayModeViewModel>()
        val searchBarViewModel by viewModels<SearchBarViewModel>()

        toolbar_compose_view!!.setContent {
            val isHidden = remember { mutableStateOf(false) }
            val searchBarValueState = remember { mutableStateOf("") }
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .background(Color(0xFFF7F7F7)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LogoBar()
                NavOuterBox(navViewModel)
                WindowButtonsBar(
                    isHidden.value,
                    onDisplayModeChange = {
                        // displayModeState.value = it 
                        displayModeViewModel.changeDisplayMode(it)
                    },
                    onSearchBarChange = { 
                        searchBarValueState.value = it 
                        searchBarViewModel.changeSearchBarValue(it)
                    },
                    searchBarValueState.value
                )
            }
        }

        main_frame_compose_view!!.setContent {
            val navController = rememberNavController()
            val displayModeState = remember { mutableStateOf(DisplayMode.ALL_PROCESSES) }
            // `isToolbarHidden` should be synchronized with 
            // `setWindowDecorationStatus` method in MainActivity
            val isToolbarHidden = false
            val searchBarValueState = remember { mutableStateOf("") }
            val snackbarHostState = remember { SnackbarHostState() }
            val isServiceAvailable = remember { mutableStateOf(false) }

             LaunchedEffect(Unit) {
                navViewModel.navigationEvents.collect { route ->
                    if(!isServiceAvailable.value) {
                        launch {
                            snackbarHostState.showSnackbar(
                                message = getString(R.string.service_not_available),
                            )
                        }
                        return@collect
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            }

            LaunchedEffect(displayModeViewModel) {
                displayModeViewModel.displayModeChangeEvents.collect { displayMode ->
                    if(!isServiceAvailable.value) {
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

            LaunchedEffect(searchBarViewModel) {1
                searchBarViewModel.searchBarValueChangeEvents.collect { value ->
                    if(!isServiceAvailable.value) {
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
                                displayModeState.value, searchBarValueState.value
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
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    class NavigationViewModel : ViewModel() {
        private val _navigationEvents = MutableSharedFlow<String>()
        val navigationEvents = _navigationEvents.asSharedFlow()

        fun navigateTo(route: String) {
            viewModelScope.launch {
                _navigationEvents.emit(route)
            }
        }
    }

    class DisplayModeViewModel : ViewModel() {
        private val _displayModeChangeEvents = MutableSharedFlow<DisplayMode>()
        val displayModeChangeEvents = _displayModeChangeEvents.asSharedFlow()

        fun changeDisplayMode(mode: DisplayMode) {
            viewModelScope.launch {
                _displayModeChangeEvents.emit(mode)
            }
        }
    }

    class SearchBarViewModel : ViewModel() {
        private val _searchBarValueChangeEvents = MutableSharedFlow<String>()
        val searchBarValueChangeEvents = _searchBarValueChangeEvents.asSharedFlow()

        fun changeSearchBarValue(value: String) {
            viewModelScope.launch {
                _searchBarValueChangeEvents.emit(value)
            }
        }
    }
}






