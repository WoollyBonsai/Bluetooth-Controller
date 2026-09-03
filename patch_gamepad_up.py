import re

with open('app/src/main/java/com/example/bluetoothtrackpad/views/GamepadView.kt', 'r') as f:
    content = f.read()

old_up = """                } else if (target == "DPAD") {
                    currentDpad = 0
                    stateChanged = true
                } else if (target != null && btnMasks.containsKey(target)) {"""

new_up = """                } else if (target == "DPAD") {
                    currentDpad = 0
                    stateChanged = true
                } else if (target == "LT") {
                    leftTrigger = 0.toByte()
                    stateChanged = true
                } else if (target == "RT") {
                    rightTrigger = 0.toByte()
                    stateChanged = true
                } else if (target != null && btnMasks.containsKey(target)) {"""

content = content.replace(old_up, new_up)

with open('app/src/main/java/com/example/bluetoothtrackpad/views/GamepadView.kt', 'w') as f:
    f.write(content)
