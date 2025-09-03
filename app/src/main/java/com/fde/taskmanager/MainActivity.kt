package com.fde.taskmanager

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.fde.taskmanager.ui.NavigationView
import android.view.Window

class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This method is only available when `SystemUISharedLib` 
        // is imported when using Soong to compile under the 
        // Android source tree and keep in sync with 
        // `isTitleBarHidden` in `NavigationView`
        setWindowDecorationStatus(Window.WINDOW_DECORATION_FORCE_HIDE);
        enableEdgeToEdge()
        setContent {
            NavigationView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}






