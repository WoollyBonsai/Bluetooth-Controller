with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val spinnerMode: Spinner = findViewById(R.id.spinnerMode)',
    'val spinnerMode: Spinner = findViewById(R.id.spinnerMode)\n        findViewById<Button>(R.id.btnSettings).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }'
)

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'w') as f:
    f.write(content)
