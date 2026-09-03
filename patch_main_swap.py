import re

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'r') as f:
    content = f.read()

old_report_logic = """                reportData[3] = lx
                reportData[4] = ly
                reportData[5] = lt
                reportData[6] = rx
                reportData[7] = ry
                reportData[8] = rt"""

new_report_logic = """                reportData[3] = lx
                reportData[4] = ly
                reportData[5] = rx
                reportData[6] = ry
                reportData[7] = lt
                reportData[8] = rt"""

content = content.replace(old_report_logic, new_report_logic)

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'w') as f:
    f.write(content)
