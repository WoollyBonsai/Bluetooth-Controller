with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'r') as f:
    content = f.read()

bad_array = """        layouts = arrayOf(
            layoutTrackpadOnly,
            layoutTrackpadKeyboard,
            layoutThinkpad,
            layoutMultimedia,
            layoutPresentation
        )"""

good_array = """        layouts = arrayOf(
            layoutTrackpadOnly,
            layoutTrackpadKeyboard,
            layoutThinkpad,
            layoutMultimedia,
            layoutPresentation,
            layoutGamepad
        )"""

content = content.replace(bad_array, good_array)

# Add Landscape forcing
switch_func = """    private fun switchLayout(position: Int) {
        layouts.forEach { it.visibility = View.GONE }
        if (position in layouts.indices) {
            layouts[position].visibility = View.VISIBLE
        }
        
        // Force Landscape for Gamepad (mode index 5)
        if (position == 5) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }"""

# The existing switchLayout looks like this:
import re
content = re.sub(r'    private fun switchLayout\(position: Int\) \{.*?    \}', switch_func, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'w') as f:
    f.write(content)
