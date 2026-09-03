with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'r') as f:
    content = f.read()

# 1. Add layoutCustom to views
content = content.replace(
"""    private lateinit var layoutPresentation: FrameLayout
    private lateinit var layoutGamepad: FrameLayout""",
"""    private lateinit var layoutPresentation: FrameLayout
    private lateinit var layoutGamepad: FrameLayout
    private lateinit var layoutCustom: FrameLayout"""
)

# 2. Add customLayouts array in class properties
content = content.replace(
"""    private var gyroSensor: Sensor? = null
    private var currentLayoutIndex = 0""",
"""    private var gyroSensor: Sensor? = null
    private var currentLayoutIndex = 0
    private var customLayouts = mutableListOf<com.example.bluetoothtrackpad.models.CustomLayout>()"""
)

# 3. Find and add layoutCustom to arrays
content = content.replace(
"""        layoutGamepad = findViewById(R.id.layoutGamepad) as FrameLayout
        
        layouts = arrayOf(
            layoutTrackpadOnly,
            layoutTrackpadKeyboard,
            layoutThinkpad,
            layoutMultimedia,
            layoutPresentation,
            layoutGamepad
        )""",
"""        layoutGamepad = findViewById(R.id.layoutGamepad) as FrameLayout
        layoutCustom = findViewById(R.id.layoutCustom) as FrameLayout
        
        layouts = arrayOf(
            layoutTrackpadOnly,
            layoutTrackpadKeyboard,
            layoutThinkpad,
            layoutMultimedia,
            layoutPresentation,
            layoutGamepad,
            layoutCustom
        )"""
)

# 4. Modify switchLayout to render custom layout
content = content.replace(
"""    private fun switchLayout(index: Int) {
        currentLayoutIndex = index
        for (i in layouts.indices) {
            layouts[i].visibility = if (i == index) View.VISIBLE else View.GONE
        }
        
        // Force Landscape for Gamepad (mode index 5)
        if (index == 5) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }""",
"""    private fun switchLayout(index: Int) {
        currentLayoutIndex = index
        
        if (index < 6) {
            for (i in 0 until 6) {
                layouts[i].visibility = if (i == index) View.VISIBLE else View.GONE
            }
            layoutCustom.visibility = View.GONE
        } else {
            for (i in 0 until 6) {
                layouts[i].visibility = View.GONE
            }
            layoutCustom.visibility = View.VISIBLE
            
            // Render custom layout dynamically
            layoutCustom.removeAllViews()
            val customIdx = index - 6
            if (customIdx < customLayouts.size) {
                val conf = customLayouts[customIdx]
                val renderer = com.example.bluetoothtrackpad.views.CustomLayoutRenderer(this, conf, object : com.example.bluetoothtrackpad.views.CustomLayoutRenderer.Listener {
                    override fun onMacroTriggered(keycodes: List<Byte>) {
                        for (k in keycodes) {
                            sendKeyboardReport(0, k)
                            Thread.sleep(10)
                            sendKeyboardReport(0, 0)
                            Thread.sleep(10)
                        }
                    }
                })
                layoutCustom.addView(renderer)
                requestedOrientation = if (conf.isLandscape) android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                return
            }
        }
        
        // Force Landscape for Gamepad (mode index 5)
        if (index == 5) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }"""
)

# 5. Refresh spinner in onResume
content = content.replace(
"""    override fun onResume() {
        super.onResume()
        gyroSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }""",
"""    override fun onResume() {
        super.onResume()
        gyroSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        
        // Refresh Spinner
        customLayouts = LayoutManager.getLayouts(this)
        val defaultOptions = resources.getStringArray(R.array.mode_array).toList()
        val allOptions = defaultOptions + customLayouts.map { it.name }
        
        val spinnerMode: android.widget.Spinner = findViewById(R.id.spinnerMode)
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, allOptions)
        
        // To preserve selection, remove listener, update adapter, re-add listener
        val currentSelection = spinnerMode.selectedItemPosition
        spinnerMode.onItemSelectedListener = null
        spinnerMode.adapter = adapter
        if (currentSelection < allOptions.size) {
            spinnerMode.setSelection(currentSelection)
        }
        
        spinnerMode.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                switchLayout(position)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }"""
)

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'w') as f:
    f.write(content)
