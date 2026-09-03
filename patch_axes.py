with open('app/src/main/java/com/example/bluetoothtrackpad/HidUtils.kt', 'r') as f:
    content = f.read()

# Replace the axes part
old_axes = """        // 4 Axes (X, Y, Z, Rz) - Left Stick (X,Y) Right Stick (Z, Rz)
        0x05.toByte(), 0x01.toByte(), //   Usage Page (Generic Desktop)
        0x09.toByte(), 0x30.toByte(), //   Usage (X)
        0x09.toByte(), 0x31.toByte(), //   Usage (Y)
        0x09.toByte(), 0x32.toByte(), //   Usage (Z)
        0x09.toByte(), 0x35.toByte(), //   Usage (Rz)
        0x15.toByte(), 0x00.toByte(), //   Logical Minimum (0)
        0x26.toByte(), 0xFF.toByte(), 0x00.toByte(), // Logical Maximum (255)
        0x75.toByte(), 0x08.toByte(), //   Report Size (8)
        0x95.toByte(), 0x04.toByte(), //   Report Count (4)
        0x81.toByte(), 0x02.toByte(), //   Input (Data, Variable, Absolute)"""

new_axes = """        // 6 Axes (X, Y, Z, Rx, Ry, Rz)
        // X, Y = Left Stick. Z = L-Trigger. Rx, Ry = Right Stick. Rz = R-Trigger.
        0x05.toByte(), 0x01.toByte(), //   Usage Page (Generic Desktop)
        0x09.toByte(), 0x30.toByte(), //   Usage (X)
        0x09.toByte(), 0x31.toByte(), //   Usage (Y)
        0x09.toByte(), 0x32.toByte(), //   Usage (Z)
        0x09.toByte(), 0x33.toByte(), //   Usage (Rx)
        0x09.toByte(), 0x34.toByte(), //   Usage (Ry)
        0x09.toByte(), 0x35.toByte(), //   Usage (Rz)
        0x15.toByte(), 0x00.toByte(), //   Logical Minimum (0)
        0x26.toByte(), 0xFF.toByte(), 0x00.toByte(), // Logical Maximum (255)
        0x75.toByte(), 0x08.toByte(), //   Report Size (8)
        0x95.toByte(), 0x06.toByte(), //   Report Count (6)
        0x81.toByte(), 0x02.toByte(), //   Input (Data, Variable, Absolute)"""

content = content.replace(old_axes, new_axes)

with open('app/src/main/java/com/example/bluetoothtrackpad/HidUtils.kt', 'w') as f:
    f.write(content)
