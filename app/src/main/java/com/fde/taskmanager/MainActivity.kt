package com.fde.taskmanager

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.fde.taskmanager.ui.NavigationView


class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val systemLanguageTag: String =
            resources.configuration.locales.get(0).toLanguageTag()
        enableEdgeToEdge()
        setContent {
            NavigationView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}






