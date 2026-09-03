with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'r') as f:
    content = f.read()

# Fields
content = content.replace('    private lateinit var etImmediateSend: EditText', 
'''    private lateinit var layoutGamepad: FrameLayout
    private lateinit var gamepadView: com.example.bluetoothtrackpad.views.GamepadView
    
    private lateinit var etImmediateSend: EditText''')

# Binding
binding = '''        layoutGamepad = findViewById(R.id.layoutGamepad) as FrameLayout
        gamepadView = com.example.bluetoothtrackpad.views.GamepadView(this)
        layoutGamepad.addView(gamepadView)
        
        gamepadView.listener = object : com.example.bluetoothtrackpad.views.GamepadView.Listener {
            override fun onGamepadReport(buttons: Short, dpad: Byte, lx: Byte, ly: Byte, rx: Byte, ry: Byte) {
                if (hostDevice == null) return
                val reportData = ByteArray(7)
                reportData[0] = (buttons.toInt() and 0xFF).toByte()
                reportData[1] = ((buttons.toInt() shr 8) and 0xFF).toByte()
                reportData[2] = dpad
                reportData[3] = lx
                reportData[4] = ly
                reportData[5] = rx
                reportData[6] = ry
                
                reportExecutor.execute {
                    hidDevice?.sendReport(hostDevice, HidUtils.GAMEPAD_REPORT_ID.toInt(), reportData)
                }
            }
        }
'''
content = content.replace('        etImmediateSend = findViewById(R.id.etImmediateSend)', 
                          binding + '\n        etImmediateSend = findViewById(R.id.etImmediateSend)')

# Layouts array
content = content.replace('arrayOf(layoutTrackpadOnly, layoutTrackpadKeyboard, layoutThinkpad, layoutMultimedia, layoutPresentation)', 
                          'arrayOf(layoutTrackpadOnly, layoutTrackpadKeyboard, layoutThinkpad, layoutMultimedia, layoutPresentation, layoutGamepad)')

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'w') as f:
    f.write(content)
