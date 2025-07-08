package com.example.taskmanager

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Adapters {
    private val gson = Gson()
    private val intListType = object : TypeToken<List<Int>>() {}.type
    public fun TasksPidAdapt(tasksPidString: String): List<Int> = gson.fromJson(tasksPidString, intListType)
    data class TaskInfo(
        var name: String? = null,
        var user: String? = null,
        var vmsize: Int = 0,
        var cpuUsage: Int = 0,
        var pid: Int = 0,
        var rss: Int = 0,
        var readBytes: Int = 0,
        var writeBytes: Int = 0,
        var readIssued: Int = 0,
        var writeIssued: Int = 0
    )
    public fun TaskInfoAdapt(taskInfoString: String): TaskInfo = gson.fromJson(taskInfoString, TaskInfo::class.java)
}