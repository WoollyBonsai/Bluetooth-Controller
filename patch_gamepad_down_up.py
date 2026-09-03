import re

with open('app/src/main/java/com/example/bluetoothtrackpad/views/GamepadView.kt', 'r') as f:
    content = f.read()

# Fix ACTION_DOWN / ACTION_POINTER_DOWN
old_down = """                        if (hit.contains(x, y)) {
                            activePointers[pointerId] = name
                            buttonsMask = (buttonsMask.toInt() or btnMasks[name]!!).toShort()
                            stateChanged = true
                            break
                        }"""

new_down = """                        if (hit.contains(x, y)) {
                            activePointers[pointerId] = name
                            if (name == "LT") {
                                leftTrigger = 255.toByte()
                            } else if (name == "RT") {
                                rightTrigger = 255.toByte()
                            } else {
                                buttonsMask = (buttonsMask.toInt() or btnMasks[name]!!).toShort()
                            }
                            stateChanged = true
                            break
                        }"""

content = content.replace(old_down, new_down)

# Check ACTION_UP / ACTION_POINTER_UP logic
# Let's see what is there
with open('app/src/main/java/com/example/bluetoothtrackpad/views/GamepadView.kt', 'w') as f:
    f.write(content)
