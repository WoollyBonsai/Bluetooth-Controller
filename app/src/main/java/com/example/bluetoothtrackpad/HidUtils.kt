package com.example.bluetoothtrackpad

object HidUtils {
    const val MOUSE_REPORT_ID: Byte = 1
    const val KEYBOARD_REPORT_ID: Byte = 2
    const val CONSUMER_REPORT_ID: Byte = 3

    val COMPOSITE_HID_DESCRIPTOR = byteArrayOf(
        // =========================
        // MOUSE REPORT (ID 1)
        // =========================
        0x05.toByte(), 0x01.toByte(), // Usage Page (Generic Desktop)
        0x09.toByte(), 0x02.toByte(), // Usage (Mouse)
        0xA1.toByte(), 0x01.toByte(), // Collection (Application)
        0x85.toByte(), MOUSE_REPORT_ID, // Report ID (1)
        0x09.toByte(), 0x01.toByte(), //   Usage (Pointer)
        0xA1.toByte(), 0x00.toByte(), //   Collection (Physical)
        // Buttons
        0x05.toByte(), 0x09.toByte(), //     Usage Page (Buttons)
        0x19.toByte(), 0x01.toByte(), //     Usage Minimum (Button 1)
        0x29.toByte(), 0x03.toByte(), //     Usage Maximum (Button 3)
        0x15.toByte(), 0x00.toByte(), //     Logical Minimum (0)
        0x25.toByte(), 0x01.toByte(), //     Logical Maximum (1)
        0x95.toByte(), 0x03.toByte(), //     Report Count (3)
        0x75.toByte(), 0x01.toByte(), //     Report Size (1)
        0x81.toByte(), 0x02.toByte(), //     Input (Data, Variable, Absolute)
        // Padding
        0x95.toByte(), 0x01.toByte(), //     Report Count (1)
        0x75.toByte(), 0x05.toByte(), //     Report Size (5)
        0x81.toByte(), 0x03.toByte(), //     Input (Constant)
        // X, Y, Wheel
        0x05.toByte(), 0x01.toByte(), //     Usage Page (Generic Desktop)
        0x09.toByte(), 0x30.toByte(), //     Usage (X)
        0x09.toByte(), 0x31.toByte(), //     Usage (Y)
        0x09.toByte(), 0x38.toByte(), //     Usage (Wheel)
        0x15.toByte(), 0x81.toByte(), //     Logical Minimum (-127)
        0x25.toByte(), 0x7f.toByte(), //     Logical Maximum (127)
        0x75.toByte(), 0x08.toByte(), //     Report Size (8)
        0x95.toByte(), 0x03.toByte(), //     Report Count (3)
        0x81.toByte(), 0x06.toByte(), //     Input (Data, Variable, Relative)
        0xC0.toByte(), //   End Collection
        0xC0.toByte(), // End Collection

        // =========================
        // KEYBOARD REPORT (ID 2)
        // =========================
        0x05.toByte(), 0x01.toByte(), // Usage Page (Generic Desktop)
        0x09.toByte(), 0x06.toByte(), // Usage (Keyboard)
        0xA1.toByte(), 0x01.toByte(), // Collection (Application)
        0x85.toByte(), KEYBOARD_REPORT_ID, // Report ID (2)
        // Modifier keys (8 bits)
        0x05.toByte(), 0x07.toByte(), //   Usage Page (Key Codes)
        0x19.toByte(), 0xE0.toByte(), //   Usage Minimum (224) - Left Control
        0x29.toByte(), 0xE7.toByte(), //   Usage Maximum (231) - Right GUI
        0x15.toByte(), 0x00.toByte(), //   Logical Minimum (0)
        0x25.toByte(), 0x01.toByte(), //   Logical Maximum (1)
        0x75.toByte(), 0x01.toByte(), //   Report Size (1)
        0x95.toByte(), 0x08.toByte(), //   Report Count (8)
        0x81.toByte(), 0x02.toByte(), //   Input (Data, Variable, Absolute)
        // Reserved byte
        0x95.toByte(), 0x01.toByte(), //   Report Count (1)
        0x75.toByte(), 0x08.toByte(), //   Report Size (8)
        0x81.toByte(), 0x03.toByte(), //   Input (Constant)
        // Key arrays (6 bytes)
        0x95.toByte(), 0x06.toByte(), //   Report Count (6)
        0x75.toByte(), 0x08.toByte(), //   Report Size (8)
        0x15.toByte(), 0x00.toByte(), //   Logical Minimum (0)
        0x25.toByte(), 0x65.toByte(), //   Logical Maximum (101)
        0x05.toByte(), 0x07.toByte(), //   Usage Page (Key Codes)
        0x19.toByte(), 0x00.toByte(), //   Usage Minimum (0)
        0x29.toByte(), 0x65.toByte(), //   Usage Maximum (101)
        0x81.toByte(), 0x00.toByte(), //   Input (Data, Array)
        0xC0.toByte(), // End Collection

        // =========================
        // CONSUMER CONTROL / MEDIA (ID 3)
        // =========================
        0x05.toByte(), 0x0C.toByte(), // Usage Page (Consumer)
        0x09.toByte(), 0x01.toByte(), // Usage (Consumer Control)
        0xA1.toByte(), 0x01.toByte(), // Collection (Application)
        0x85.toByte(), CONSUMER_REPORT_ID, // Report ID (3)
        0x15.toByte(), 0x00.toByte(), //   Logical Minimum (0)
        0x26.toByte(), 0xFF.toByte(), 0x03.toByte(), //   Logical Maximum (1023)
        0x19.toByte(), 0x00.toByte(), //   Usage Minimum (0)
        0x2A.toByte(), 0xFF.toByte(), 0x03.toByte(), //   Usage Maximum (1023)
        0x75.toByte(), 0x10.toByte(), //   Report Size (16)
        0x95.toByte(), 0x01.toByte(), //   Report Count (1)
        0x81.toByte(), 0x00.toByte(), //   Input (Data, Array)
        0xC0.toByte()  // End Collection
    )

    // Modifier masks
    const val MOD_NONE: Byte = 0x00
    const val MOD_LEFT_SHIFT: Byte = 0x02

    fun charToHid(char: Char): Pair<Byte, Byte>? {
        val c = char.lowercaseChar()
        val isShift = char.isUpperCase() || char in "!@#$%^&*()_+{}|:\"<>?~"

        val modifier = if (isShift) MOD_LEFT_SHIFT else MOD_NONE
        
        val keycode: Byte = when (c) {
            in 'a'..'z' -> (c - 'a' + 0x04).toByte()
            '1', '!' -> 0x1E
            '2', '@' -> 0x1F
            '3', '#' -> 0x20
            '4', '$' -> 0x21
            '5', '%' -> 0x22
            '6', '^' -> 0x23
            '7', '&' -> 0x24
            '8', '*' -> 0x25
            '9', '(' -> 0x26
            '0', ')' -> 0x27
            '\n' -> 0x28 // Enter
            '\b' -> 0x2A // Backspace
            '\t' -> 0x2B // Tab
            ' ' -> 0x2C  // Space
            '-', '_' -> 0x2D
            '=', '+' -> 0x2E
            '[', '{' -> 0x2F
            ']', '}' -> 0x30
            '\\', '|' -> 0x31
            ';', ':' -> 0x33
            '\'', '"' -> 0x34
            '`', '~' -> 0x35
            ',', '<' -> 0x36
            '.', '>' -> 0x37
            '/', '?' -> 0x38
            else -> return null
        }
        return Pair(modifier, keycode)
    }
}
