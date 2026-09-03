import re

with open('app/src/main/java/com/example/bluetoothtrackpad/views/GamepadView.kt', 'r') as f:
    content = f.read()

old_stick_logic = """        val dist = sqrt(dx * dx + dy * dy)
        
        if (dist > stickRadius) {
            val angle = atan2(dy, dx)
            current.x = center.x + cos(angle) * stickRadius
            current.y = center.y + sin(angle) * stickRadius
        } else {
            current.x = px
            current.y = py
        }

        // Map to 0-255
        val mappedX = ((current.x - center.x) / stickRadius * 127 + 128).toInt().coerceIn(0, 255)
        val mappedY = ((current.y - center.y) / stickRadius * 127 + 128).toInt().coerceIn(0, 255)"""

new_stick_logic = """        val dist = sqrt(dx * dx + dy * dy)
        val deadzone = stickRadius * 0.1f // 10% deadzone for hall-effect feel
        
        if (dist < deadzone) {
            current.x = center.x
            current.y = center.y
        } else if (dist > stickRadius) {
            val angle = atan2(dy, dx)
            current.x = center.x + cos(angle) * stickRadius
            current.y = center.y + sin(angle) * stickRadius
        } else {
            current.x = px
            current.y = py
        }

        // Smooth curve mapping to 0-255 outside deadzone
        val mappedX = if (dist < deadzone) 128 else ((current.x - center.x) / stickRadius * 127 + 128).toInt().coerceIn(0, 255)
        val mappedY = if (dist < deadzone) 128 else ((current.y - center.y) / stickRadius * 127 + 128).toInt().coerceIn(0, 255)"""

content = content.replace(old_stick_logic, new_stick_logic)

with open('app/src/main/java/com/example/bluetoothtrackpad/views/GamepadView.kt', 'w') as f:
    f.write(content)
