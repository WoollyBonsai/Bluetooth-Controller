package com.example.bluetoothtrackpad.models

import java.io.Serializable

data class CustomLayout(
    var id: String = java.util.UUID.randomUUID().toString(),
    var name: String = "New Layout",
    var widgets: MutableList<LayoutWidget> = mutableListOf(),
    var isLandscape: Boolean = true
) : Serializable

data class LayoutWidget(
    var id: String = java.util.UUID.randomUUID().toString(),
    var type: WidgetType = WidgetType.BUTTON,
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 150f,
    var height: Float = 150f,
    var label: String = "Btn",
    var boundKeycodes: List<Byte> = listOf() // HID keycodes for macros
) : Serializable

enum class WidgetType {
    BUTTON, JOYSTICK, DPAD, TRACKPAD
}
