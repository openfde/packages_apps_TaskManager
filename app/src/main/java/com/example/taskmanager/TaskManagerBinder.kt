package com.example.taskmanager

import android.graphics.BitmapFactory
import android.openfde.ITaskManager
import android.os.IBinder
import android.util.Base64
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.collections.List

object TaskManagerBinder {
    private val taskBinder: IBinder? = try {
        Class.forName("android.os.ServiceManager")
            .getMethod("getService", String::class.java)
            .invoke(null, "openfdetaskmanager") as IBinder?
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    private val taskManager = taskBinder?.let { ITaskManager.Stub.asInterface(it) }

    public fun getTasks(): List<Adapters.TaskInfo> {
        val tasksString = taskManager?.getTasks()
        val tasks = Adapters.TaskInfoListAdapt(tasksString.toString())
        return tasks
    }

    public fun getTaskPids(): List<Int> {
        val tasksPidString = taskManager?.getTaskPids()
        val taskPids = Adapters.TaskPidsAdapt(tasksPidString.toString())
        return taskPids
    }

    public fun getTaskByPid(pid: Int): Adapters.TaskInfo? {
        val taskInfoString = taskManager?.getTaskByPid(pid)
        if (taskInfoString == "null") return null
        val taskInfo = Adapters.TaskInfoAdapt(taskInfoString.toString())
        return taskInfo
    }

    public fun killTaskByPid(pid: Int) {
        taskManager?.killTaskByPid(pid)
    }

    public fun getIconBitmapByTaskName(taskName: String): ImageBitmap? {
        val iconB64String = taskManager?.getIconB64ByTaskName(taskName)
        if (iconB64String == "")
            return null

        val imageBytes = Base64.decode(iconB64String, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size).asImageBitmap()
        return bitmap
    }

    public fun getEachCPUPercent(interval: Int): List<Float> {
        val eachCPUPercent = taskManager?.getEachCPUPercent(interval)
        val cpuPercent = Adapters.CPUPercentAdapt(eachCPUPercent.toString())
        return cpuPercent
    }

    public fun getMemoryAndSwap(): Adapters.MemoryInfo {
        val memoryAndSwap = taskManager?.getMemoryAndSwap()
        val memoryInfo = Adapters.MemoryInfoAdapt(memoryAndSwap.toString())
        return memoryInfo
    }

    public fun getNetworkDownloadAndUpload(interval: Int): Adapters.NetworkStats {
        val networkStats = taskManager?.getNetworkDownloadAndUpload(interval)
        val networkStatsInfo = Adapters.NetworkStatsAdapt(networkStats.toString())
        return networkStatsInfo
    }

    public fun getDiskReadAndWrite(interval: Int): Adapters.DiskStats {
        val diskStats = taskManager?.getDiskReadAndWrite(interval)
        val diskStatsInfo = Adapters.DiskStatsAdapt(diskStats.toString())
        return diskStatsInfo
    }

    public fun getFileSystemUsage(): List<Adapters.DiskPartition> {
        val fileSystemUsage = taskManager?.getFileSystemUsage()
        val fileSystemUsageInfo = Adapters.DiskPartitionAdapt(fileSystemUsage.toString())
        return fileSystemUsageInfo
    }

    public fun changeTaskPriority(pid: Int, priority: Int) {
        taskManager?.changeTaskPriority(pid, priority)
    }
}