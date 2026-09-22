package com.example.bluetoothtrackpad

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFS_NAME = "BluetoothTrackpadPrefs"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isGyroEnabled(context: Context, layoutIndex: Int): Boolean {
        return getPrefs(context).getBoolean("gyro_enabled_$layoutIndex", false)
    }

    fun setGyroEnabled(context: Context, layoutIndex: Int, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("gyro_enabled_$layoutIndex", enabled).apply()
    }

    fun getSensitivity(context: Context, layoutIndex: Int): Int {
        return getPrefs(context).getInt("sensitivity_$layoutIndex", 50)
    }

    fun setSensitivity(context: Context, layoutIndex: Int, sensitivity: Int) {
        getPrefs(context).edit().putInt("sensitivity_$layoutIndex", sensitivity).apply()
    }
}
