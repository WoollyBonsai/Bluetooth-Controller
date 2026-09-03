with open('app/src/main/java/com/example/bluetoothtrackpad/HidUtils.kt', 'r') as f:
    content = f.read()

gamepad = """
        // gamepad ka swagat karo (ID 4)
        0x05.toByte(), 0x01.toByte(), // Usage Page (Generic Desktop)
        0x09.toByte(), 0x05.toByte(), // Usage (Gamepad)
        0xA1.toByte(), 0x01.toByte(), // Collection (Application)
        0x85.toByte(), GAMEPAD_REPORT_ID, // Report ID (4)

        // 16 Buttons (A, B, X, Y, L1, R1, L2, R2, Select, Start, L3, R3...)
        0x05.toByte(), 0x09.toByte(), //   Usage Page (Button)
        0x19.toByte(), 0x01.toByte(), //   Usage Minimum (Button 1)
        0x29.toByte(), 0x10.toByte(), //   Usage Maximum (Button 16)
        0x15.toByte(), 0x00.toByte(), //   Logical Minimum (0)
        0x25.toByte(), 0x01.toByte(), //   Logical Maximum (1)
        0x75.toByte(), 0x01.toByte(), //   Report Size (1)
        0x95.toByte(), 0x10.toByte(), //   Report Count (16)
        0x81.toByte(), 0x02.toByte(), //   Input (Data, Variable, Absolute)

        // Hat switch (D-Pad)
        0x05.toByte(), 0x01.toByte(), //   Usage Page (Generic Desktop)
        0x09.toByte(), 0x39.toByte(), //   Usage (Hat switch)
        0x15.toByte(), 0x01.toByte(), //   Logical Minimum (1)
        0x25.toByte(), 0x08.toByte(), //   Logical Maximum (8)
        0x35.toByte(), 0x00.toByte(), //   Physical Minimum (0)
        0x46.toByte(), 0x3B.toByte(), 0x01.toByte(), // Physical Maximum (315)
        0x65.toByte(), 0x14.toByte(), //   Unit (Eng Rot:Angular Pos)
        0x75.toByte(), 0x04.toByte(), //   Report Size (4)
        0x95.toByte(), 0x01.toByte(), //   Report Count (1)
        0x81.toByte(), 0x02.toByte(), //   Input (Data, Variable, Absolute)

        // Padding for Hat switch
        0x75.toByte(), 0x04.toByte(), //   Report Size (4)
        0x95.toByte(), 0x01.toByte(), //   Report Count (1)
        0x81.toByte(), 0x03.toByte(), //   Input (Constant)

        // 4 Axes (X, Y, Z, Rz) - Left Stick (X,Y) Right Stick (Z, Rz)
        0x05.toByte(), 0x01.toByte(), //   Usage Page (Generic Desktop)
        0x09.toByte(), 0x30.toByte(), //   Usage (X)
        0x09.toByte(), 0x31.toByte(), //   Usage (Y)
        0x09.toByte(), 0x32.toByte(), //   Usage (Z)
        0x09.toByte(), 0x35.toByte(), //   Usage (Rz)
        0x15.toByte(), 0x00.toByte(), //   Logical Minimum (0)
        0x26.toByte(), 0xFF.toByte(), 0x00.toByte(), // Logical Maximum (255)
        0x75.toByte(), 0x08.toByte(), //   Report Size (8)
        0x95.toByte(), 0x04.toByte(), //   Report Count (4)
        0x81.toByte(), 0x02.toByte(), //   Input (Data, Variable, Absolute)

        0xC0.toByte()  // End Collection
    )
"""

content = content.replace('const val CONSUMER_REPORT_ID: Byte = 3', 'const val CONSUMER_REPORT_ID: Byte = 3\n    const val GAMEPAD_REPORT_ID: Byte = 4')
content = content.replace('        0xC0.toByte()  // End Collection\n    )', gamepad)

with open('app/src/main/java/com/example/bluetoothtrackpad/HidUtils.kt', 'w') as f:
    f.write(content)

