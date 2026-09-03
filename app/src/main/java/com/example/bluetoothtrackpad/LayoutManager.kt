package com.example.bluetoothtrackpad

import android.content.Context
import com.example.bluetoothtrackpad.models.CustomLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object LayoutManager {
    private const val PREFS = "LayoutsPrefs"
    private const val KEY = "layouts_json"

    fun getLayouts(context: Context): MutableList<CustomLayout> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY, "[]")
        val type = object : TypeToken<MutableList<CustomLayout>>() {}.type
        return try {
            Gson().fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun saveLayouts(context: Context, layouts: List<CustomLayout>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = Gson().toJson(layouts)
        prefs.edit().putString(KEY, json).apply()
    }

    fun saveLayout(context: Context, layout: CustomLayout) {
        val list = getLayouts(context)
        val idx = list.indexOfFirst { it.id == layout.id }
        if (idx >= 0) {
            list[idx] = layout
        } else {
            list.add(layout)
        }
        saveLayouts(context, list)
    }
}
