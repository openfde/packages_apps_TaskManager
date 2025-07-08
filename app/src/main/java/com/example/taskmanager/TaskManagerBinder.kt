package com.example.taskmanager

import android.graphics.BitmapFactory
import android.openfde.ITaskManager
import android.os.IBinder
import android.util.Base64
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

object TaskManagerBinder {
    private val taskBinder: IBinder? = try {
        Class.forName("android.os.ServiceManager")
            .getMethod("getService", String::class.java)
            .invoke(null, "openfdetaskmanager") as IBinder?
    } catch (e: Exception) {
        e.printStackTrace()
        Log.d("COLD","error :$e")
        null
    }

    private val taskManager = taskBinder?.let { ITaskManager.Stub.asInterface(it) }

    public fun listTasksPid(): List<Int> {
        val tasksPidString = taskManager?.listTasksPid()
        val tasksPid = Adapters.TasksPidAdapt(tasksPidString.toString())
        return tasksPid
    }

    public fun getTaskInfoByPid(pid: Int): Adapters.TaskInfo {
        val taskInfoString = taskManager?.getTaskInfoByPid(pid)
        Log.d("COLD","taskInfoString: $taskInfoString")
        if (taskInfoString == "null") {
            return Adapters.TaskInfo()
        }
        val taskInfo = Adapters.TaskInfoAdapt(taskInfoString.toString())
        return taskInfo
    }

    public fun killTaskByPid(pid: Int) {
        taskManager?.killTaskByPid(pid)
    }

    public fun getIconBitmapByTaskName(taskName:String): ImageBitmap {
        val iconB64String = taskManager?.getIconB64ByTaskName(taskName)
        val imageBytes = Base64.decode(iconB64String, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size).asImageBitmap()
        return bitmap
    }

}