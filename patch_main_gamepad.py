import re

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "override fun onGamepadReport(buttons: Short, dpad: Byte, lx: Byte, ly: Byte, rx: Byte, ry: Byte) {",
    "override fun onGamepadReport(buttons: Short, dpad: Byte, lx: Byte, ly: Byte, lt: Byte, rx: Byte, ry: Byte, rt: Byte) {"
)

old_report_logic = """                val reportData = ByteArray(7)
                reportData[0] = (buttons.toInt() and 0xFF).toByte()
                reportData[1] = ((buttons.toInt() shr 8) and 0xFF).toByte()
                reportData[2] = dpad
                reportData[3] = lx
                reportData[4] = ly
                reportData[5] = rx
                reportData[6] = ry"""

new_report_logic = """                val reportData = ByteArray(9)
                reportData[0] = (buttons.toInt() and 0xFF).toByte()
                reportData[1] = ((buttons.toInt() shr 8) and 0xFF).toByte()
                reportData[2] = dpad
                reportData[3] = lx
                reportData[4] = ly
                reportData[5] = lt
                reportData[6] = rx
                reportData[7] = ry
                reportData[8] = rt"""

content = content.replace(old_report_logic, new_report_logic)

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'w') as f:
    f.write(content)
