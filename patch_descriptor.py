with open('app/src/main/java/com/example/bluetoothtrackpad/HidUtils.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""        0x75.toByte(), 0x10.toByte(), //   Report Size (16)
        0x95.toByte(), 0x01.toByte(), //   Report Count (1)
        0x81.toByte(), 0x00.toByte(), //   Input (Data, Array)

        // gamepad ka swagat karo (ID 4)""",
"""        0x75.toByte(), 0x10.toByte(), //   Report Size (16)
        0x95.toByte(), 0x01.toByte(), //   Report Count (1)
        0x81.toByte(), 0x00.toByte(), //   Input (Data, Array)
        0xC0.toByte(), // End Collection

        // gamepad ka swagat karo (ID 4)"""
)

with open('app/src/main/java/com/example/bluetoothtrackpad/HidUtils.kt', 'w') as f:
    f.write(content)
