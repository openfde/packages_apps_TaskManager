package com.fde.taskmanager

import android.util.Log
import androidx.compose.runtime.toMutableStateList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.filter
import kotlin.collections.indexOfFirst
import kotlin.properties.Delegates

object BackgroundTask {
    private var supervisorJob = SupervisorJob()
    private var scope = CoroutineScope(Dispatchers.Default + supervisorJob)

    private val _taskInfoList = MutableStateFlow<List<Adapters.TaskInfo>>(emptyList())
    val taskInfoList: StateFlow<List<Adapters.TaskInfo>> = _taskInfoList.asStateFlow()

    private var _cpuPercentState: MutableStateFlow<List<List<Float>>> = MutableStateFlow(emptyList())
    val cpuPercentState: StateFlow<List<List<Float>>> = _cpuPercentState.asStateFlow()

    private var _memoryAndSwapList: MutableStateFlow<List<List<Float>>> = MutableStateFlow(listOf(emptyList(),emptyList()))
    val memoryAndSwapList: StateFlow<List<List<Float>>> = _memoryAndSwapList.asStateFlow()

    private val _memoryAndSwap = MutableStateFlow<Adapters.MemoryInfo>(
        Adapters.MemoryInfo(
            Adapters.MemoryInfo.MemoryDetail(
                0f,0,0,0
            ),
            Adapters.MemoryInfo.SwapDetail(
                0f,0,0
            )
        )
    )
    val memoryAndSwap: StateFlow<Adapters.MemoryInfo> = _memoryAndSwap.asStateFlow()


    private val _networkStatsState = MutableStateFlow<Adapters.NetworkStats>(
        Adapters.NetworkStats(
            Adapters.NetworkStats.TransferStats(
                0f,0
            ),
            Adapters.NetworkStats.TransferStats(
                0f,0
            )
        )
    )

    val networkStatsState = _networkStatsState.asStateFlow()

    private var _networkDownloadAndUploadList: MutableStateFlow<List<List<Float>>> = MutableStateFlow(listOf(emptyList(),emptyList()))
    val networkDownloadAndUploadList: StateFlow<List<List<Float>>> = _networkDownloadAndUploadList.asStateFlow()

    private val _diskReadAndWriteList = MutableStateFlow<List<List<Float>>>(listOf(emptyList(),emptyList()))
    val diskReadAndWriteList = _diskReadAndWriteList.asStateFlow()
    private val _diskStatsState = MutableStateFlow<Adapters.DiskStats>(
        Adapters.DiskStats(
            Adapters.DiskStats.TransferStats(
                0f,0
            ), Adapters.DiskStats.TransferStats(
                0f,0
            )
        )
    )
    val diskStatsState = _diskStatsState.asStateFlow()
    var cpuCount by Delegates.notNull<Int>()

    init {
        cpuCount = TaskManagerBinder.getEachCPUPercent(10).size
        _cpuPercentState.value = List(cpuCount + 1) { emptyList() } // 多1个作平均
    }

    fun startBackgroundTask() {
        startProcessViewBackgroundTask()
        startResourceViewBackgroundTask()
    }

    fun startResourceViewBackgroundTask() {
        if (supervisorJob.isCancelled) {
            supervisorJob = SupervisorJob()
            scope = CoroutineScope(Dispatchers.Default + supervisorJob)
        }
        // CPU
        scope.launch {
            while (true) {
                try {
                    val eachCPUPercent = TaskManagerBinder.getEachCPUPercent(200)
                    val currentLists = _cpuPercentState.value
                    val cpuCount = eachCPUPercent.size
                    val updatedCpuLists = currentLists.take(cpuCount).mapIndexed { index, list ->
                        val newPercent = eachCPUPercent[index]
                        val updated = if (list.size >= 20) list.drop(1) else list
                        updated + newPercent
                    }
                    val latestAvg = updatedCpuLists.map { it.lastOrNull() ?: 0f }.average().toFloat()
                    val avgList = currentLists.getOrNull(cpuCount) ?: emptyList()
                    val updatedAvgList = (if (avgList.size >= 20) avgList.drop(1) else avgList) + latestAvg
                    _cpuPercentState.value = updatedCpuLists + listOf(updatedAvgList)
                } catch (e: Exception) {
                    Log.e("BackgroundTask", "Error updating CPU percent", e)
                }
                delay(1000)
            }
        }
        // 内存和交换
        scope.launch {
            while (true) {
                val memoryAndSwapInfo = TaskManagerBinder.getMemoryAndSwap()
                _memoryAndSwap.value = memoryAndSwapInfo
                val memory  = _memoryAndSwapList.value[0].toMutableStateList().apply {
                    if(size >= 20) removeAt(0)
                    add(memoryAndSwapInfo.memory.percent)
                }.toList()
                val swap = _memoryAndSwapList.value[1].toMutableStateList().apply {
                    if(size >= 20) removeAt(0)
                    add(memoryAndSwapInfo.swap.percent)
                }.toList()
                _memoryAndSwapList.value = listOf(memory,swap)
                delay(1000)
            }
        }
        // 网络
        scope.launch {
            while (true) {
                val networkStats =
                    TaskManagerBinder.getNetworkDownloadAndUpload(200)
                _networkStatsState.value = networkStats
                val download = networkDownloadAndUploadList.value[0].toMutableStateList().apply {
                    if(size >= 20) removeAt(0)
                    add(networkStats.download.speed)
                }.toList()
                val upload = networkDownloadAndUploadList.value[1].toMutableStateList().apply {
                    if(size >= 20) removeAt(0)
                    add(networkStats.upload.speed)
                }.toList()
                _networkDownloadAndUploadList.value = listOf(download,upload)
            }
        }
        // 磁盘
        scope.launch {
            while(true) {
                val diskStats = TaskManagerBinder.getDiskReadAndWrite(200)
                if(diskStats != null) {
                    _diskStatsState.value = diskStats
                    val read = _diskReadAndWriteList.value[0].toMutableStateList().apply {
                        if (size >= 20) removeAt(0)
                        add(_diskStatsState.value.read.speed)
                    }.toList()
                    val write = _diskReadAndWriteList.value[1].toMutableStateList().apply {
                        if (size >= 20) removeAt(0)
                        add(_diskStatsState.value.write.speed)
                    }.toList()
                    _diskReadAndWriteList.value = listOf(read, write)
                }
            }
        }
    }

    fun startProcessViewBackgroundTask() {
        if (supervisorJob.isCancelled) {
            supervisorJob = SupervisorJob()
            scope = CoroutineScope(Dispatchers.Default + supervisorJob)
        }

        scope.launch {
            while (true) {
                try {
                    val tasks = TaskManagerBinder.getTasks()
                    val currentPids = TaskManagerBinder.getTaskPids().toSet()
                    val workingList = _taskInfoList.value.filter { it.pid in currentPids }.toMutableList()
                    val existingMap = workingList.associateBy { it.pid }

                    val batchSize = 20
                    var currentIndex = 0
                    while (currentIndex < tasks.size) {
                        val endIndex = minOf(currentIndex + batchSize, tasks.size)
                        val batch = tasks.subList(currentIndex, endIndex)
                        for (task in batch) {
                            if (existingMap.containsKey(task.pid)) {
                                val index = workingList.indexOfFirst { it.pid == task.pid }
                                if (index != -1) workingList[index] = task
                            } else {
                                workingList.add(task)
                            }
                        }
                        currentIndex = endIndex
                    }
                    _taskInfoList.value = workingList.sortedBy { it.pid }
                } catch (e: Exception) {
                    Log.e("BackgroundTask", "Error updating task list", e)
                }
                delay(500)
            }
        }
    }

    fun refreshTaskInfoList() {
        cancelAllTasks()
        _taskInfoList.value = emptyList()
        startBackgroundTask()
    }

    fun cancelAllTasks() {
        supervisorJob.cancel()
    }

}
