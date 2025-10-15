package com.fde.taskmanager

import android.util.Log
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

object BackgroundTask {
    private var supervisorJob = SupervisorJob()
    private var scope = CoroutineScope(Dispatchers.Default + supervisorJob)

    private val _taskInfoList = MutableStateFlow<List<Adapters.TaskInfo>>(emptyList())
    val taskInfoList: StateFlow<List<Adapters.TaskInfo>> = _taskInfoList.asStateFlow()


    fun startBackgroundTask() {
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
