import sys

def main():
    with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'r') as f:
        content = f.read()

    # insert calls in onCreate
    content = content.replace('setupButtons()\n        setupKeyboardInputs()', 'setupButtons()\n        setupMultimedia()\n        setupPresentation()\n        setupKeyboardInputs()')

    # insert functions
    funcs = """
    private fun setupMultimedia() {
        findViewById<Button>(R.id.btnVolDown).setOnClickListener { sendConsumerReport(0x00EA) }
        findViewById<Button>(R.id.btnVolUp).setOnClickListener { sendConsumerReport(0x00E9) }
        findViewById<Button>(R.id.btnMute).setOnClickListener { sendConsumerReport(0x00E2) }
        findViewById<Button>(R.id.btnPrevTrack).setOnClickListener { sendConsumerReport(0x00B6) }
        findViewById<Button>(R.id.btnNextTrack).setOnClickListener { sendConsumerReport(0x00B5) }
        findViewById<Button>(R.id.btnPlayPause).setOnClickListener { sendConsumerReport(0x00CD) }
        findViewById<Button>(R.id.btnBrightDown).setOnClickListener { sendConsumerReport(0x0070) }
        findViewById<Button>(R.id.btnBrightUp).setOnClickListener { sendConsumerReport(0x006F) }
        
        findViewById<Button>(R.id.btnDisplayMode).setOnClickListener {
            if (hostDevice == null) return@setOnClickListener
            // Win + P for Display Mode
            val reportData = ByteArray(8)
            reportData[0] = HidUtils.MOD_LEFT_SHIFT // Wait, Windows key is 0x08
            reportData[0] = 0x08.toByte() // Left GUI (Windows key)
            reportData[2] = 0x13.toByte() // 'P' key
            reportExecutor.execute {
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), reportData)
                val releaseData = ByteArray(8)
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), releaseData)
            }
        }
    }

    private fun setupPresentation() {
        findViewById<Button>(R.id.btnF5).setOnClickListener {
            // F5
            if (hostDevice == null) return@setOnClickListener
            val reportData = ByteArray(8)
            reportData[2] = 0x3E.toByte()
            reportExecutor.execute {
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), reportData)
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), ByteArray(8))
            }
        }
        findViewById<Button>(R.id.btnEsc).setOnClickListener {
            // Esc
            if (hostDevice == null) return@setOnClickListener
            val reportData = ByteArray(8)
            reportData[2] = 0x29.toByte()
            reportExecutor.execute {
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), reportData)
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), ByteArray(8))
            }
        }
        findViewById<Button>(R.id.btnPrevSlide).setOnClickListener {
            // Left Arrow
            if (hostDevice == null) return@setOnClickListener
            val reportData = ByteArray(8)
            reportData[2] = 0x50.toByte()
            reportExecutor.execute {
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), reportData)
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), ByteArray(8))
            }
        }
        findViewById<Button>(R.id.btnNextSlide).setOnClickListener {
            // Right Arrow
            if (hostDevice == null) return@setOnClickListener
            val reportData = ByteArray(8)
            reportData[2] = 0x4F.toByte()
            reportExecutor.execute {
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), reportData)
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), ByteArray(8))
            }
        }
        
        val touchAreaPresentation = findViewById<View>(R.id.touchAreaPresentation)
        setupTrackpad(touchAreaPresentation)
    }
"""
    content = content.replace('    private fun setupButtons() {', funcs + '\n    private fun setupButtons() {')

    with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'w') as f:
        f.write(content)

main()
