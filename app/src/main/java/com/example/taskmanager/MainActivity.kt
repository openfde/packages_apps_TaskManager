package com.example.taskmanager

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cafe.adriel.lyricist.Lyricist
import com.example.taskmanager.ui.NavigationView


class MainActivity : ComponentActivity() {


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






