package com.example.taskmanager.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController


sealed class AppRoute(val route: String) {
    object Process : AppRoute("process")
    object Resource : AppRoute("resource")
    object FileSystem : AppRoute("file_system")
}


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
                AppRoute.FileSystem to @Composable { FileSystemView() }
            ).forEach { (route, content) ->
                composable(route.route) { content() }
            }
        }
    }
}

@Composable
fun WindowButtonsBar() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(start = 12.dp, end = 8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.window_options_button),
            modifier = Modifier.size(28.dp),
            contentDescription = null
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
            text = "资源管理器",
            modifier = Modifier.padding(start = 8.dp),
            fontSize = 14.sp
        )
    }
}

@Composable
fun NavInnerBox(name: String, selected: Boolean,onClick: () -> Unit) {
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
            NavInnerBox(name = "进程",
                selected = selectedItem.value == AppRoute.Process.route,
                onClick = {
                    navController.navigate(AppRoute.Process.route)
                    selectedItem.value = AppRoute.Process.route
            })
            VerticalDivider(
                modifier = Modifier.height(18.dp), color = Color(0x0D000000)
            )
            NavInnerBox(name = "资源",
                selected = selectedItem.value == AppRoute.Resource.route,
                onClick = {
                navController.navigate(AppRoute.Resource.route)
                    selectedItem.value = AppRoute.Resource.route
            })
            VerticalDivider(
                modifier = Modifier.height(18.dp), color = Color(0x0D000000)
            )
            NavInnerBox(name = "文件系统",
                selected = selectedItem.value == AppRoute.FileSystem.route,
                onClick = {
                navController.navigate(AppRoute.FileSystem.route)
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
