package com.example.bluetoothtrackpad

data class KeyboardKey(
    val label: String,
    val hidCode: Byte,
    val isModifier: Boolean = false,
    val modifierMask: Byte = 0,
    val widthWeight: Float = 1f
)
