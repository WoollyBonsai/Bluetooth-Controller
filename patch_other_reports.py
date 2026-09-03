import re

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'r') as f:
    content = f.read()

content = re.sub(
    r'hidDevice\?\.sendReport\(([^,]+),\s*([^,]+),\s*(reportData)\)',
    r'''val success = hidDevice?.sendReport(\1, \2, \3)
                    if (success == false) android.util.Log.e("HID_REPORT", "sendReport failed for ID: " + \2)''',
    content
)

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'w') as f:
    f.write(content)
