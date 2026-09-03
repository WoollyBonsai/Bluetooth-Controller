import re

with open('app/src/main/java/com/example/bluetoothtrackpad/views/GamepadView.kt', 'r') as f:
    content = f.read()

new_masks = """    private val btnMasks = mapOf(
        "A" to 1, "B" to 2, "X" to 4, "Y" to 8,
        "LB" to 16, "RB" to 32, "Select" to 256, "Start" to 512,
        "L3" to 1024, "R3" to 2048, "W" to 4096
    )"""

content = re.sub(r'private val btnMasks = mapOf\([^)]+\)', new_masks, content, flags=re.MULTILINE)

with open('app/src/main/java/com/example/bluetoothtrackpad/views/GamepadView.kt', 'w') as f:
    f.write(content)
