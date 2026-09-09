package com.example.bluetoothtrackpad

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.bluetoothtrackpad.views.GamepadView
import com.example.bluetoothtrackpad.views.ThinkpadKeyboardView
import com.example.bluetoothtrackpad.views.CustomLayoutRenderer

class MainActivity : AppCompatActivity() {
    private var isFullscreen = false
    private var backPressedTime: Long = 0
    private lateinit var topBar: View

    private lateinit var spinnerMode: Spinner
    private lateinit var layoutTrackpadOnly: View
    private lateinit var layoutTrackpadKeyboard: View
    private lateinit var layoutThinkpad: FrameLayout
    private lateinit var layoutMultimedia: View
    private lateinit var layoutPresentation: View
    private lateinit var layoutGamepad: FrameLayout
    private lateinit var layoutCustom: FrameLayout
    private lateinit var layouts: Array<View>

    private lateinit var thinkpadKeyboardView: ThinkpadKeyboardView
    private lateinit var gamepadView: GamepadView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        topBar = findViewById(R.id.topBar)

        findViewById<Button>(R.id.btnInit).setOnClickListener {
            Toast.makeText(this, "UI Skeleton Mode", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<Button>(R.id.btnConnect).setOnClickListener {
            Toast.makeText(this, "UI Skeleton Mode", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnFullscreen).setOnClickListener {
            enterFullscreen()
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(android.content.Intent(this, SettingsActivity::class.java))
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFullscreen) {
                    exitFullscreen()
                } else {
                    if (System.currentTimeMillis() - backPressedTime < 2000) {
                        finish()
                    } else {
                        Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                        backPressedTime = System.currentTimeMillis()
                    }
                }
            }
        })

        spinnerMode = findViewById(R.id.spinnerMode)
        layoutTrackpadOnly = findViewById(R.id.layoutTrackpadOnly)
        layoutTrackpadKeyboard = findViewById(R.id.layoutTrackpadKeyboard)
        layoutThinkpad = findViewById(R.id.layoutThinkpad)
        layoutMultimedia = findViewById(R.id.layoutMultimedia)
        layoutPresentation = findViewById(R.id.layoutPresentation)
        layoutGamepad = findViewById(R.id.layoutGamepad)
        layoutCustom = findViewById(R.id.layoutCustom)
        
        layouts = arrayOf(
            layoutTrackpadOnly,
            layoutTrackpadKeyboard,
            layoutThinkpad,
            layoutMultimedia,
            layoutPresentation,
            layoutGamepad,
            layoutCustom
        )

        spinnerMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                switchLayout(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Add custom views
        thinkpadKeyboardView = ThinkpadKeyboardView(this)
        layoutThinkpad.addView(thinkpadKeyboardView)

        gamepadView = GamepadView(this)
        gamepadView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        layoutGamepad.addView(gamepadView)
    }

    private fun switchLayout(position: Int) {
        for (layout in layouts) {
            layout.visibility = View.GONE
        }
        if (position in layouts.indices) {
            layouts[position].visibility = View.VISIBLE
        }
    }

    private fun enterFullscreen() {
        isFullscreen = true
        topBar.visibility = View.GONE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, topBar).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun exitFullscreen() {
        isFullscreen = false
        topBar.visibility = View.VISIBLE
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, topBar).show(WindowInsetsCompat.Type.systemBars())
    }
}
