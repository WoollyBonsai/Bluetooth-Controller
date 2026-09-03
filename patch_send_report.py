with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                reportExecutor.execute {
                    hidDevice?.sendReport(hostDevice, HidUtils.GAMEPAD_REPORT_ID.toInt(), reportData)
                }""",
"""                reportExecutor.execute {
                    val success = hidDevice?.sendReport(hostDevice, HidUtils.GAMEPAD_REPORT_ID.toInt(), reportData)
                    if (success == false) {
                        android.util.Log.e("HID_REPORT", "Gamepad sendReport failed!")
                    }
                }"""
)

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'w') as f:
    f.write(content)
