package com.example.bluetoothtrackpad // Replace with your actual package name

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.UUID
import java.util.concurrent.Executors

@SuppressLint("MissingPermission") // Permissions are checked dynamically before use
class MainActivity : AppCompatActivity() {

    // A constant to identify our HID report
    private val MOUSE_REPORT_ID: Byte = 1

    // The HID Report Descriptor for a standard mouse.
    private val MOUSE_HID_DESCRIPTOR = byteArrayOf(
        0x05.toByte(), 0x01.toByte(), // Usage Page (Generic Desktop)
        0x09.toByte(), 0x02.toByte(), // Usage (Mouse)
        0xA1.toByte(), 0x01.toByte(), // Collection (Application)
        0x09.toByte(), 0x01.toByte(), //   Usage (Pointer)
        0xA1.toByte(), 0x00.toByte(), //   Collection (Physical)
        0x85.toByte(), MOUSE_REPORT_ID, //   Report ID (1)
        0x05.toByte(), 0x09.toByte(), //     Usage Page (Buttons)
        0x19.toByte(), 0x01.toByte(), //     Usage Minimum (Button 1)
        0x29.toByte(), 0x03.toByte(), //     Usage Maximum (Button 3)
        0x15.toByte(), 0x00.toByte(), //     Logical Minimum (0)
        0x25.toByte(), 0x01.toByte(), //     Logical Maximum (1)
        0x95.toByte(), 0x03.toByte(), //     Report Count (3)
        0x75.toByte(), 0x01.toByte(), //     Report Size (1)
        0x81.toByte(), 0x02.toByte(), //     Input (Data, Variable, Absolute)
        0x95.toByte(), 0x01.toByte(), //     Report Count (1)
        0x75.toByte(), 0x05.toByte(), //     Report Size (5)
        0x81.toByte(), 0x03.toByte(), //     Input (Constant) - Padding
        0x05.toByte(), 0x01.toByte(), //     Usage Page (Generic Desktop)
        0x09.toByte(), 0x30.toByte(), //     Usage (X)
        0x09.toByte(), 0x31.toByte(), //     Usage (Y)
        0x15.toByte(), 0x81.toByte(), //     Logical Minimum (-127)
        0x25.toByte(), 0x7f.toByte(), //     Logical Maximum (127)
        0x75.toByte(), 0x08.toByte(), //     Report Size (8)
        0x95.toByte(), 0x02.toByte(), //     Report Count (2)
        0x81.toByte(), 0x06.toByte(), //     Input (Data, Variable, Relative)
        0xC0.toByte(), //   End Collection
        0xC0.toByte()  // End Collection
    )

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var hidDevice: BluetoothHidDevice? = null
    private var hostDevice: BluetoothDevice? = null

    private var lastX = 0f
    private var lastY = 0f

    // Use a handler for delayed UI-related tasks and an executor for background tasks.
    private val handler = Handler(Looper.getMainLooper())
    private val reportExecutor = Executors.newSingleThreadExecutor()


    // Modern way to handle permission requests.
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.BLUETOOTH_CONNECT] == true) {
                // Permission granted, proceed with Bluetooth setup.
                initBluetooth()
            } else {
                // Permission denied. Show a message to the user.
                showToast("Bluetooth permission is required to use the trackpad.")
            }
        }

    private val requestDiscoverabilityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_CANCELED) {
                showToast("Device is discoverable")
            } else {
                showToast("Device is not discoverable")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissionsAndInit()
        setupUI()
    }

    /**
     * Checks for necessary Bluetooth permissions. On Android 12+ it will request them.
     * On older versions, it proceeds directly to initialization.
     */
    private fun checkPermissionsAndInit() {
        // For Android 12 (API 31) and above, new Bluetooth permissions are required.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissionsToRequest = arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN
            )
            // Check if we already have the permissions.
            if (permissionsToRequest.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
                initBluetooth()
            } else {
                // Launch the permission request dialog.
                requestPermissionLauncher.launch(permissionsToRequest)
            }
        } else {
            // For older versions (like Android 10), permissions are granted at install time.
            initBluetooth()
        }
    }

    /**
     * Initializes the Bluetooth adapter and gets the HID device profile proxy.
     */
    private fun initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            showToast("Bluetooth is not supported on this device.")
            return
        }
        bluetoothAdapter?.getProfileProxy(this, profileListener, BluetoothProfile.HID_DEVICE)
    }

    /**
     * Sets up the UI listeners for the trackpad and click button.
     */
    private fun setupUI() {
        val trackpadView: View = findViewById(R.id.trackpadView)
        val leftClickButton: Button = findViewById(R.id.leftClickButton)

        trackpadView.setOnTouchListener { _, event ->
            handleTrackpadTouch(event)
            true
        }

        leftClickButton.setOnClickListener {
            performLeftClick()
        }
    }

    /**
     * Handles touch events on the trackpad view to send movement reports.
     */
    private fun handleTrackpadTouch(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = (event.x - lastX).toInt()
                val dy = (event.y - lastY).toInt()

                if (dx != 0 || dy != 0) {
                    sendMouseMove(dx, dy)
                }

                lastX = event.x
                lastY = event.y
            }
        }
    }

    /**
     * Simulates a left mouse click by sending a button down and then a button up report.
     * This is now non-blocking.
     */
    private fun performLeftClick() {
        // Send "button down" report on the background thread.
        sendReport(byteArrayOf(0b00000001, 0, 0)) // Press left button

        // Schedule the "button up" report to be sent after a 50ms delay.
        handler.postDelayed({
            sendReport(byteArrayOf(0b00000000, 0, 0)) // Release all buttons
        }, 50)
    }

    /**
     * Sends a mouse movement report.
     */
    private fun sendMouseMove(dx: Int, dy: Int) {
        val clampedDx = dx.coerceIn(-127, 127).toByte()
        val clampedDy = dy.coerceIn(-127, 127).toByte()
        // This will now be executed on the background thread via sendReport.
        sendReport(byteArrayOf(0, clampedDx, clampedDy))
    }

    /**
     * Sends a HID report to the connected host device on a background thread.
     */
    private fun sendReport(reportData: ByteArray) {
        if (hidDevice == null || hostDevice == null) {
            Log.w("MainActivity", "Cannot send report: No connected host device.")
            return
        }
        // Execute the actual sending on our dedicated background thread to avoid blocking the UI.
        reportExecutor.execute {
            hidDevice?.sendReport(hostDevice, MOUSE_REPORT_ID.toInt(), reportData)
        }
    }

    /**
     * Callback for Bluetooth profile connection events.
     */
    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = proxy as BluetoothHidDevice
                registerHidApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = null
            }
        }
    }
    
    /**
     * Registers this application as a Bluetooth HID device.
     */
    private fun registerHidApp() {
        requestDiscoverability()
        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "Android Trackpad",
            "A virtual mouse for your computer",
            "Example Inc.",
            BluetoothHidDevice.SUBCLASS1_MOUSE,
            MOUSE_HID_DESCRIPTOR
        )

        hidDevice?.registerApp(
            sdpSettings,
            null,
            null,
            Executors.newSingleThreadExecutor(),
            hidCallback
        )
    }

    private fun requestDiscoverability() {
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        requestDiscoverabilityLauncher.launch(discoverableIntent)
    }

    /**
     * Callback for HID device events, such as connection state changes.
     */
    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)
            runOnUiThread {
                when (state) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        hostDevice = device
                        showToast("Connected to ${device?.name}")
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        hostDevice = null
                        showToast("Disconnected")
                    }
                }
            }
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // It's important to shut down the executor when the app is destroyed.
        reportExecutor.shutdown()
        hidDevice?.unregisterApp()
        bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
    }
}


// Note: You need to define the layout (res/layout/activity_main.xml) with
// a View with id "trackpadView" and a Button with id "leftClickButton".
// Example:
/*
<androidx.constraintlayout.widget.ConstraintLayout ...>
    <View
        android:id="@+id/trackpadView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:background="@android:color/darker_gray"
        ... />
    <Button
        android:id="@+id/leftClickButton"
        android:text="Left Click"
        ... />
</androidx.constraintlayout.widget.ConstraintLayout>
*/