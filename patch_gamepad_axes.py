import re

with open('app/src/main/java/com/example/bluetoothtrackpad/views/GamepadView.kt', 'r') as f:
    content = f.read()

# Add trigger properties
content = content.replace(
    "private var rightStickY: Byte = 128.toByte()",
    "private var rightStickY: Byte = 128.toByte()\n    private var leftTrigger: Byte = 0.toByte()\n    private var rightTrigger: Byte = 0.toByte()"
)

# Update touch logic for triggers
touch_logic = """                    } else if (target == "DPAD") {
                        updateDpad(px, py)
                        stateChanged = true
                    } else if (target == "LT") {
                        leftTrigger = 255.toByte()
                        stateChanged = true
                    } else if (target == "RT") {
                        rightTrigger = 255.toByte()
                        stateChanged = true
                    } else if (target != null && btnMasks.containsKey(target)) {"""
content = content.replace(
    """                    } else if (target == "DPAD") {
                        updateDpad(px, py)
                        stateChanged = true
                    } else if (target != null && btnMasks.containsKey(target)) {""",
    touch_logic
)

touch_up_logic = """                    } else if (target == "DPAD") {
                        currentDpad = 0
                        stateChanged = true
                    } else if (target == "LT") {
                        leftTrigger = 0.toByte()
                        stateChanged = true
                    } else if (target == "RT") {
                        rightTrigger = 0.toByte()
                        stateChanged = true
                    } else if (target != null && btnMasks.containsKey(target)) {"""
content = content.replace(
    """                    } else if (target == "DPAD") {
                        currentDpad = 0
                        stateChanged = true
                    } else if (target != null && btnMasks.containsKey(target)) {""",
    touch_up_logic
)

# Update sendReport call
content = content.replace(
    "listener?.onGamepadReport(buttonsMask, currentDpad, leftStickX, leftStickY, rightStickX, rightStickY)",
    "listener?.onGamepadReport(buttonsMask, currentDpad, leftStickX, leftStickY, leftTrigger, rightStickX, rightStickY, rightTrigger)"
)

# Update Listener Interface
content = content.replace(
    "fun onGamepadReport(buttons: Short, dpad: Byte, lx: Byte, ly: Byte, rx: Byte, ry: Byte)",
    "fun onGamepadReport(buttons: Short, dpad: Byte, lx: Byte, ly: Byte, lt: Byte, rx: Byte, ry: Byte, rt: Byte)"
)

with open('app/src/main/java/com/example/bluetoothtrackpad/views/GamepadView.kt', 'w') as f:
    f.write(content)
