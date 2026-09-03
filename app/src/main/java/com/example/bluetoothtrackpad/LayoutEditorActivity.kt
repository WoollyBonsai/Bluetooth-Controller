package com.example.bluetoothtrackpad

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LayoutEditorActivity : AppCompatActivity() {
    private lateinit var rootLayout: FrameLayout
    private var selectedView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootLayout = FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#2a2a2a"))
        }
        setContentView(rootLayout)

        val btnAdd = Button(this).apply {
            text = "+ Add Button"
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.END
                setMargins(0, 16, 16, 0)
            }
            setOnClickListener { addCustomWidget() }
        }
        rootLayout.addView(btnAdd)

        val btnSave = Button(this).apply {
            text = "Save Layout"
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
                setMargins(0, 0, 16, 16)
            }
            setOnClickListener {
                Toast.makeText(this@LayoutEditorActivity, "Layout Saved! (Serialization coming next)", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        rootLayout.addView(btnSave)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addCustomWidget() {
        val btn = Button(this).apply {
            text = "Btn"
            x = 100f
            y = 100f
            layoutParams = FrameLayout.LayoutParams(200, 200)
        }

        var dX = 0f
        var dY = 0f

        btn.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    selectedView = view
                    view.alpha = 0.5f
                }
                MotionEvent.ACTION_MOVE -> {
                    view.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                }
                MotionEvent.ACTION_UP -> {
                    view.alpha = 1.0f
                    // Open settings dialog for button when tapped without moving
                }
            }
            true
        }

        rootLayout.addView(btn)
    }
}
