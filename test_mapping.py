import re

with open('app/src/main/java/com/example/bluetoothtrackpad/views/GamepadView.kt', 'r') as f:
    content = f.read()

# Replace btnMasks
new_masks = """    private val btnMasks = mapOf(
        "A" to 1, "B" to 2, "X" to 8, "Y" to 16,
        "LB" to 64, "RB" to 128, "LT" to 256, "RT" to 512,
        "Select" to 1024, "Start" to 2048, "W" to 4096,
        "L3" to 8192, "R3" to 16384
    )"""

content = re.sub(r'private val btnMasks = mapOf\([^)]+\)', new_masks, content, flags=re.MULTILINE)

with open('app/src/main/java/com/example/bluetoothtrackpad/views/GamepadView.kt', 'w') as f:
    f.write(content)
