with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'r') as f:
    content = f.read()

# Fix layoutParams
content = content.replace(
    "gamepadView = com.example.bluetoothtrackpad.views.GamepadView(this)\n        layoutGamepad.addView(gamepadView)",
    "gamepadView = com.example.bluetoothtrackpad.views.GamepadView(this)\n        gamepadView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)\n        layoutGamepad.addView(gamepadView)"
)

# Fix layouts array
content = content.replace(
"""        layouts = arrayOf(
            layoutTrackpadOnly,
            layoutTrackpadKeyboard,
            layoutThinkpad,
            layoutMultimedia,
            layoutPresentation
        )""",
"""        layouts = arrayOf(
            layoutTrackpadOnly,
            layoutTrackpadKeyboard,
            layoutThinkpad,
            layoutMultimedia,
            layoutPresentation,
            layoutGamepad
        )"""
)

# Fix switchLayout
content = content.replace(
"""    private fun switchLayout(index: Int) {
        for (i in layouts.indices) {
            layouts[i].visibility = if (i == index) View.VISIBLE else View.GONE
        }
    }""",
"""    private fun switchLayout(index: Int) {
        for (i in layouts.indices) {
            layouts[i].visibility = if (i == index) View.VISIBLE else View.GONE
        }
        
        // Force Landscape for Gamepad (mode index 5)
        if (index == 5) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }"""
)

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'w') as f:
    f.write(content)
