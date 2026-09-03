with open('app/src/main/java/com/example/bluetoothtrackpad/HidManager.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""            }
            hidCallback.onConnectionStateChanged(device, state)
        }
    }
}""",
"""            }
            hidCallback.onConnectionStateChanged(device, state)
        }
        
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            hidCallback.onAppStatusChanged(pluggedDevice, registered)
        }
    }
}"""
)

with open('app/src/main/java/com/example/bluetoothtrackpad/HidManager.kt', 'w') as f:
    f.write(content)
