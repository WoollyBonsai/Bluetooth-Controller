package com.example.bluetoothtrackpad

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bluetoothtrackpad.models.CustomLayout
import com.example.bluetoothtrackpad.models.LayoutWidget
import com.example.bluetoothtrackpad.models.WidgetType

class LayoutEditorActivity : AppCompatActivity() {
    private lateinit var canvas: FrameLayout
    private var currentLayout = CustomLayout()
    private val widgetViews = mutableMapOf<LayoutWidget, View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layoutId = intent.getStringExtra("layout_id")
        if (layoutId != null) {
            val list = LayoutManager.getLayouts(this)
            val found = list.find { it.id == layoutId }
            if (found != null) {
                currentLayout = found
            }
        }
        
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.parseColor("#1f1f1f"))
        }

        val btnAddBtn = Button(this).apply { text = "+Btn" }
        val btnAddDpad = Button(this).apply { text = "+D-Pad" }
        val btnAddJoy = Button(this).apply { text = "+Stick" }
        val btnSettings = Button(this).apply { text = "Name" }
        val btnSave = Button(this).apply { text = "Save" }

        toolbar.addView(btnAddBtn)
        toolbar.addView(btnAddDpad)
        toolbar.addView(btnAddJoy)
        
        val spacer = Space(this).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f) }
        toolbar.addView(spacer)
        toolbar.addView(btnSettings)
        toolbar.addView(btnSave)

        canvas = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            setBackgroundColor(Color.parseColor("#2a2a2a"))
        }

        root.addView(toolbar)
        root.addView(canvas)
        setContentView(root)

        // Render existing
        currentLayout.widgets.forEach { renderWidget(it) }

        btnAddBtn.setOnClickListener { addWidget(WidgetType.BUTTON) }
        btnAddDpad.setOnClickListener { addWidget(WidgetType.DPAD) }
        btnAddJoy.setOnClickListener { addWidget(WidgetType.JOYSTICK) }
        
        btnSettings.setOnClickListener {
            val etName = EditText(this).apply { setText(currentLayout.name) }
            AlertDialog.Builder(this)
                .setTitle("Layout Name")
                .setView(etName)
                .setPositiveButton("OK") { _, _ -> currentLayout.name = etName.text.toString() }
                .setNegativeButton("Cancel", null)
                .show()
        }
        
        btnSave.setOnClickListener {
            LayoutManager.saveLayout(this, currentLayout)
            Toast.makeText(this, "Layout Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addWidget(type: WidgetType) {
        val widget = LayoutWidget(
            type = type,
            x = 100f,
            y = 100f,
            width = if (type == WidgetType.BUTTON) 150f else 250f,
            height = if (type == WidgetType.BUTTON) 150f else 250f,
            label = type.name
        )
        currentLayout.widgets.add(widget)
        renderWidget(widget)
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun renderWidget(widget: LayoutWidget) {
        val view = TextView(this).apply {
            text = widget.label
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(if (widget.type == WidgetType.BUTTON) Color.parseColor("#4CAF50") else Color.parseColor("#FF9800"))
            x = widget.x
            y = widget.y
            layoutParams = FrameLayout.LayoutParams(widget.width.toInt(), widget.height.toInt())
        }

        var dX = 0f
        var dY = 0f
        var isDragging = false
        var dragThreshold = 10f
        var startX = 0f
        var startY = 0f

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    startX = event.rawX
                    startY = event.rawY
                    isDragging = false
                    v.alpha = 0.7f
                }
                MotionEvent.ACTION_MOVE -> {
                    if (Math.abs(event.rawX - startX) > dragThreshold || Math.abs(event.rawY - startY) > dragThreshold) {
                        isDragging = true
                    }
                    if (isDragging) {
                        v.x = event.rawX + dX
                        v.y = event.rawY + dY
                        widget.x = v.x
                        widget.y = v.y
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.alpha = 1.0f
                    if (!isDragging) {
                        showConfigDialog(widget, v)
                    }
                }
            }
            true
        }

        widgetViews[widget] = view
        canvas.addView(view)
    }

    private fun showConfigDialog(widget: LayoutWidget, view: View) {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val etLabel = EditText(this).apply { hint = "Label"; setText(widget.label) }
        val etWidth = EditText(this).apply { hint = "Width"; inputType = android.text.InputType.TYPE_CLASS_NUMBER; setText(widget.width.toInt().toString()) }
        val etHeight = EditText(this).apply { hint = "Height"; inputType = android.text.InputType.TYPE_CLASS_NUMBER; setText(widget.height.toInt().toString()) }
        
        val etKeycode = EditText(this).apply {
            hint = "Keycode (comma separated hex, e.g. 04,05)"
            val codes = widget.boundKeycodes.joinToString(",") { String.format("%02X", it) }
            setText(codes)
        }

        dialogView.addView(etLabel)
        dialogView.addView(etWidth)
        dialogView.addView(etHeight)
        if (widget.type == WidgetType.BUTTON) {
            dialogView.addView(etKeycode)
        }

        AlertDialog.Builder(this)
            .setTitle("Configure ${widget.type.name}")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                widget.label = etLabel.text.toString()
                widget.width = etWidth.text.toString().toFloatOrNull() ?: widget.width
                widget.height = etHeight.text.toString().toFloatOrNull() ?: widget.height
                
                if (widget.type == WidgetType.BUTTON) {
                    try {
                        val hexes = etKeycode.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        widget.boundKeycodes = hexes.map { it.toInt(16).toByte() }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Invalid keycodes", Toast.LENGTH_SHORT).show()
                    }
                }

                (view as TextView).text = widget.label
                view.layoutParams = FrameLayout.LayoutParams(widget.width.toInt(), widget.height.toInt())
            }
            .setNeutralButton("Delete") { _, _ ->
                currentLayout.widgets.remove(widget)
                canvas.removeView(view)
                widgetViews.remove(widget)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
