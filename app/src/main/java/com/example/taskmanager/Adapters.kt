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
        var writeIssued: Long = 0,
        var nice: Int = 0
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

    private val diskStatsType = object : TypeToken<DiskStats>() {}.type

    data class DiskStats(
        val read: TransferStats,
        val write: TransferStats
    ) {
        data class TransferStats(
            val speed: Float,
            val total: Long
        )
    }

    public fun DiskStatsAdapt(diskStatsString: String): DiskStats =
        gson.fromJson(diskStatsString, diskStatsType)

    // 在 Adapters 对象中添加以下内容
    private val diskPartitionType = object : TypeToken<List<DiskPartition>>() {}.type

    data class DiskPartition(
        val used: String,        // 已用空间
        val catalogue: String,    // 挂载点 (注意: 原JSON中拼写应为"catalog"或"mountPoint")
        val device: String,      // 设备名称
        val type: String,        // 文件系统类型
        val storage: String,     // 总容量
        val available: String,    // 可用空间
        val percent: Int         // 使用百分比（x/100）
    )

    public fun DiskPartitionAdapt(partitionString: String): List<DiskPartition> =
        gson.fromJson(partitionString, diskPartitionType)
}