with open('app/src/main/java/com/example/bluetoothtrackpad/HidUtils.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""        0x65.toByte(), 0x14.toByte(), //   Unit (Eng Rot:Angular Pos)
        0x75.toByte(), 0x04.toByte(), //   Report Size (4)
        0x95.toByte(), 0x01.toByte(), //   Report Count (1)
        0x81.toByte(), 0x02.toByte(), //   Input (Data, Variable, Absolute)""",
"""        0x65.toByte(), 0x14.toByte(), //   Unit (Eng Rot:Angular Pos)
        0x75.toByte(), 0x04.toByte(), //   Report Size (4)
        0x95.toByte(), 0x01.toByte(), //   Report Count (1)
        0x81.toByte(), 0x42.toByte(), //   Input (Data, Variable, Absolute, Null State)"""
)

with open('app/src/main/java/com/example/bluetoothtrackpad/HidUtils.kt', 'w') as f:
    f.write(content)
