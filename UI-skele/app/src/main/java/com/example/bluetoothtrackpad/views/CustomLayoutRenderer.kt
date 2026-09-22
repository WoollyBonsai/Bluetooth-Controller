package com.example.bluetoothtrackpad.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.example.bluetoothtrackpad.models.CustomLayout
import com.example.bluetoothtrackpad.models.LayoutWidget
import com.example.bluetoothtrackpad.models.WidgetType

@SuppressLint("ViewConstructor")
class CustomLayoutRenderer(context: Context, private val layoutConfig: CustomLayout, private val listener: Listener) : FrameLayout(context) {

    interface Listener {
        fun onMacroTriggered(keycodes: List<Byte>)
        // Dpad/Joystick omitted for initial simplicity
    }

    init {
        setBackgroundColor(Color.parseColor("#121212"))
        layoutConfig.widgets.forEach { widget ->
            val view = TextView(context).apply {
                text = widget.label
                setTextColor(Color.WHITE)
                gravity = android.view.Gravity.CENTER
                setBackgroundColor(if (widget.type == WidgetType.BUTTON) Color.parseColor("#4CAF50") else Color.parseColor("#FF9800"))
                x = widget.x
                y = widget.y
                layoutParams = LayoutParams(widget.width.toInt(), widget.height.toInt())
            }

            view.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.alpha = 0.5f
                        if (widget.type == WidgetType.BUTTON && widget.boundKeycodes.isNotEmpty()) {
                            listener.onMacroTriggered(widget.boundKeycodes)
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.alpha = 1.0f
                    }
                }
                true
            }
            addView(view)
        }
    }
}
