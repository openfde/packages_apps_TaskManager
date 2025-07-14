package com.example.taskmanager

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Adapters {
    private val gson = Gson()
    private val intListType = object : TypeToken<List<Int>>() {}.type
    private val floatListType = object : TypeToken<List<Float>>() {}.type
    private val taskInfoListType = object : TypeToken<List<TaskInfo>>() {}.type
    public fun TaskPidsAdapt(taskPidsString: String): List<Int> =
        gson.fromJson(taskPidsString, intListType)

    public fun CPUPercentAdapt(cpuPercentString: String): List<Float> =
        gson.fromJson(cpuPercentString, floatListType)

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

    public fun TaskInfoAdapt(taskInfoString: String): TaskInfo =
        gson.fromJson(taskInfoString, TaskInfo::class.java)

    public fun TaskInfoListAdapt(taskInfoListString: String): List<TaskInfo> =
        gson.fromJson(taskInfoListString, taskInfoListType)

    private val memoryInfoType = object : TypeToken<MemoryInfo>() {}.type

    data class MemoryInfo(
        val memory: MemoryDetail,
        val swap: SwapDetail
    ) {
        data class MemoryDetail(
            val percent: Float,
            val used: Long,
            val total: Long,
            val cache: Long
        )

        data class SwapDetail(
            val percent: Float,
            val used: Long,
            val total: Long
        )
    }

    public fun MemoryInfoAdapt(memoryInfoString: String): MemoryInfo =
        gson.fromJson(memoryInfoString, memoryInfoType)

    private val networkStatsType = object : TypeToken<NetworkStats>() {}.type

    data class NetworkStats(
        val upload: TransferStats,
        val download: TransferStats
    ) {
        data class TransferStats(
            val speed: Float,
            val total: Long
        )
    }

    public fun NetworkStatsAdapt(networkStatsString: String): NetworkStats =
        gson.fromJson(networkStatsString, networkStatsType)
}