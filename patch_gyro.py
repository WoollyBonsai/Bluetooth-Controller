import re

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'r') as f:
    content = f.read()

# Add imports
content = content.replace("import android.widget.Toast", "import android.widget.Toast\nimport android.hardware.Sensor\nimport android.hardware.SensorEvent\nimport android.hardware.SensorEventListener\nimport android.hardware.SensorManager")

# Add class properties
content = content.replace("class MainActivity : AppCompatActivity() {", "class MainActivity : AppCompatActivity(), SensorEventListener {\n    private lateinit var sensorManager: SensorManager\n    private var gyroSensor: Sensor? = null\n    private var currentLayoutIndex = 0")

# Initialize SensorManager in onCreate
init_sensors = """        
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
"""
content = content.replace("        layoutGamepad.addView(gamepadView)", "        layoutGamepad.addView(gamepadView)\n" + init_sensors)

# Update currentLayoutIndex on switch
content = content.replace("    private fun switchLayout(index: Int) {", "    private fun switchLayout(index: Int) {\n        currentLayoutIndex = index")

# Add SensorEventListener methods at the end of class
sensor_methods = """
    override fun onResume() {
        super.onResume()
        gyroSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_GYROSCOPE) return
        if (!SettingsManager.isGyroEnabled(this, currentLayoutIndex)) return
        
        val sensitivity = SettingsManager.getSensitivity(this, currentLayoutIndex)
        val mult = (sensitivity / 50f) * 15f // 50 is default
        
        var dx = 0f
        var dy = 0f
        
        // Portrait vs Landscape gyro axes
        if (currentLayoutIndex == 5) { // Gamepad is Landscape
            dx = -event.values[0] * mult // Pitch
            dy = -event.values[1] * mult // Roll
        } else {
            dx = -event.values[1] * mult // Roll
            dy = event.values[0] * mult // Pitch
        }

        if (Math.abs(dx) > 1f || Math.abs(dy) > 1f) {
            sendMouseReport(dx.toInt(), dy.toInt(), 0, null)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
"""
content = content.replace("    override fun onDestroy() {", sensor_methods + "\n    override fun onDestroy() {")

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'w') as f:
    f.write(content)
