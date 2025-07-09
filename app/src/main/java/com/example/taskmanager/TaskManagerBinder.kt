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

    public fun getTasks(): List<Adapters.TaskInfo> {
        val tasksString = taskManager?.getTasks()
        val items = tasksString.toString().split("}, {")
        for (item in items) {
            Log.d("COLD", item.toString())
        }
        val tasks = Adapters.TaskInfoListAdapt(tasksString.toString())
        return tasks
    }

    public fun killTaskByPid(pid: Int) {
        taskManager?.killTaskByPid(pid)
    }

    public fun getIconBitmapByTaskName(taskName:String): ImageBitmap {
        val iconB64String = taskManager?.getIconB64ByTaskName(taskName)
        Log.d("COLD","iconB64String: $iconB64String")
        val imageBytes = Base64.decode(iconB64String, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size).asImageBitmap()
        return bitmap
    }

}