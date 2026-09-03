with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                    } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                        hostDevice = null
                        tvStatus.text = "Disconnected"
                    }
                }
            }""",
"""                    } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                        hostDevice = null
                        tvStatus.text = "Disconnected"
                    }
                }
            }
            
            override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                super.onAppStatusChanged(pluggedDevice, registered)
                runOnUiThread {
                    if (registered) {
                        android.util.Log.d("HID", "App Registered Successfully")
                    } else {
                        tvStatus.text = "HID Reg Failed!"
                        android.util.Log.e("HID", "App Registration Failed! Descriptor might be invalid.")
                    }
                }
            }"""
)

with open('app/src/main/java/com/example/bluetoothtrackpad/MainActivity.kt', 'w') as f:
    f.write(content)
