import re

with open('app/src/main/java/com/example/bluetoothtrackpad/HidUtils.kt', 'r') as f:
    content = f.read()

# Swap the axes in HID Descriptor to make Generic Gamepads happy
# X, Y (Left Stick), Z, Rz (Right Stick), Rx, Ry (Triggers)
new_axes = """        // 6 Axes (X, Y, Z, Rz, Rx, Ry)
        // Generic mapping: X,Y=LStick, Z,Rz=RStick, Rx,Ry=Triggers
        0x05.toByte(), 0x01.toByte(), //   Usage Page (Generic Desktop)
        0x09.toByte(), 0x30.toByte(), //   Usage (X)
        0x09.toByte(), 0x31.toByte(), //   Usage (Y)
        0x09.toByte(), 0x32.toByte(), //   Usage (Z)
        0x09.toByte(), 0x35.toByte(), //   Usage (Rz)
        0x09.toByte(), 0x33.toByte(), //   Usage (Rx)
        0x09.toByte(), 0x34.toByte(), //   Usage (Ry)"""

old_axes = """        // 6 Axes (X, Y, Z, Rx, Ry, Rz)
        // X, Y = Left Stick. Z = L-Trigger. Rx, Ry = Right Stick. Rz = R-Trigger.
        0x05.toByte(), 0x01.toByte(), //   Usage Page (Generic Desktop)
        0x09.toByte(), 0x30.toByte(), //   Usage (X)
        0x09.toByte(), 0x31.toByte(), //   Usage (Y)
        0x09.toByte(), 0x32.toByte(), //   Usage (Z)
        0x09.toByte(), 0x33.toByte(), //   Usage (Rx)
        0x09.toByte(), 0x34.toByte(), //   Usage (Ry)
        0x09.toByte(), 0x35.toByte(), //   Usage (Rz)"""

content = content.replace(old_axes, new_axes)

with open('app/src/main/java/com/example/bluetoothtrackpad/HidUtils.kt', 'w') as f:
    f.write(content)
