with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""        // To preserve selection, remove listener, update adapter, re-add listener
        val currentSelection = spinnerMode.selectedItemPosition
        spinnerMode.onItemSelectedListener = null
        spinnerMode.adapter = adapter
        if (currentSelection < allOptions.size) {
            spinnerMode.setSelection(currentSelection)
        }
        
        spinnerMode.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                switchLayout(position)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }""",
"""        // To preserve selection, update adapter and add listener
        val currentSelection = spinnerMode.selectedItemPosition
        spinnerMode.adapter = adapter
        
        spinnerMode.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                switchLayout(position)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        if (currentSelection >= 0 && currentSelection < allOptions.size) {
            spinnerMode.setSelection(currentSelection)
        } else {
            spinnerMode.setSelection(0)
        }
        switchLayout(if (currentSelection >= 0) currentSelection else 0)"""
)

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'w') as f:
    f.write(content)
