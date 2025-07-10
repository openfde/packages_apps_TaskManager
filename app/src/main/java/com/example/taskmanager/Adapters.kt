package com.example.taskmanager

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Adapters {
    private val gson = Gson()
    private val intListType = object : TypeToken<List<Int>>() {}.type
    private val taskInfoListType = object : TypeToken<List<TaskInfo>>() {}.type
    public fun TaskPidsAdapt(taskPidsString: String): List<Int> = gson.fromJson(taskPidsString, intListType)
    data class TaskInfo(
        var name: String? = null,
        var user: String? = null,
        var vmsize: Long = 0,
        var cpuUsage: Float = .0f,
        var pid: Int = 0,
        var rss: Long = 0,
        var readBytes: Long = 0,
        var writeBytes: Long = 0,
        var readIssued: Long = 0,
        var writeIssued: Long = 0
    )
    public fun TaskInfoAdapt(taskInfoString: String): TaskInfo = gson.fromJson(taskInfoString, TaskInfo::class.java)
    public fun TaskInfoListAdapt(taskInfoListString: String): List<TaskInfo> = gson.fromJson(taskInfoListString, taskInfoListType)
}