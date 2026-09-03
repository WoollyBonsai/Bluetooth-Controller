import re

with open('app/src/main/java/com/example/bluetoothtrackpad/views/GamepadView.kt', 'r') as f:
    content = f.read()

new_masks = """    private val btnMasks = mapOf(
        "A" to 1, "B" to 2, "X" to 4, "Y" to 8,
        "LB" to 16, "RB" to 32, "Select" to 64, "Start" to 128,
        "L3" to 256, "R3" to 512, "W" to 1024
    )"""

content = re.sub(r'private val btnMasks = mapOf\([^)]+\)', new_masks, content, flags=re.MULTILINE)

with open('app/src/main/java/com/example/bluetoothtrackpad/views/GamepadView.kt', 'w') as f:
    f.write(content)
