package com.fde.taskmanager

import android.content.Context
import android.content.SharedPreferences

object SPUtils {

    private fun sp(context: Context): SharedPreferences =
        context.getSharedPreferences("user_info", Context.MODE_PRIVATE)

    fun getUserInfo(context: Context, key: String): Int =
        sp(context).getInt(key, 0) ?: 0

    fun putUserInfo(context: Context, key: String, value: Int) {
        sp(context).edit().apply {
            putInt(key, value)
            apply()
        }
    }

    fun cleanUserInfo(context: Context) {
        sp(context).edit().apply {
            clear()
            apply()
        }
    }
}