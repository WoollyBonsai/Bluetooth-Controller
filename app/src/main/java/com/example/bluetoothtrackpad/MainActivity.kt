package com.example.bluetoothtrackpad

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {

    private lateinit var hidManager: HidManager
    private lateinit var setupLayout: LinearLayout
    private lateinit var trackpadLayout: FrameLayout
    private lateinit var btnInit: Button
    private lateinit var btnOpenTrackpad: Button
    private lateinit var btnBack: Button
    private lateinit var touchArea: View

    private var lastX = 0f
    private var lastY = 0f

    private val MOUSE_REPORT_ID: Byte = 1
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
    private var hostDevice: BluetoothDevice? = null
    private val handler = Handler(Looper.getMainLooper())
    private val reportExecutor = Executors.newSingleThreadExecutor()
    private var hidDevice: BluetoothHidDevice? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.BLUETOOTH_CONNECT] == true && permissions[Manifest.permission.BLUETOOTH_ADVERTISE] == true) {
                startDiscovery()
            } else {
                Toast.makeText(this, "Bluetooth permission is required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_main)

        setupLayout = findViewById(R.id.setupLayout)
        trackpadLayout = findViewById(R.id.trackpadLayout)
        btnInit = findViewById(R.id.btnInit)
        btnOpenTrackpad = findViewById(R.id.btnOpenTrackpad)
        btnBack = findViewById(R.id.btnBack)
        touchArea = findViewById(R.id.touchArea)

        hidManager = HidManager(this)
        hidManager.initialize(object : BluetoothHidDevice.Callback() {
            override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
                super.onConnectionStateChanged(device, state)
                runOnUiThread {
                    if (state == BluetoothProfile.STATE_CONNECTED) {
                        hostDevice = device
                        hidDevice = hidManager.getHidDevice()
                        btnOpenTrackpad.visibility = View.VISIBLE
                    } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                        hostDevice = null
                        hidDevice = null
                        btnOpenTrackpad.visibility = View.GONE
                    }
                }
            }
        })

        btnInit.setOnClickListener {
            checkPermissionsAndInit()
        }

        btnOpenTrackpad.setOnClickListener {
            setupLayout.visibility = View.GONE
            trackpadLayout.visibility = View.VISIBLE
        }

        btnBack.setOnClickListener {
            trackpadLayout.visibility = View.GONE
            setupLayout.visibility = View.VISIBLE
        }

        touchArea.setOnTouchListener { _, event ->
            handleTrackpadTouch(event)
            true
        }
    }

    private fun checkPermissionsAndInit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissionsToRequest = arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
            )
            if (permissionsToRequest.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
                startDiscovery()
            } else {
                requestPermissionLauncher.launch(permissionsToRequest)
            }
        } else {
            startDiscovery()
        }
    }

    private fun startDiscovery() {
        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "Android Trackpad",
            "A virtual mouse for your computer",
            "Example Inc.",
            BluetoothHidDevice.SUBCLASS1_MOUSE,
            MOUSE_HID_DESCRIPTOR
        )
        hidManager.startDiscovery(sdpSettings)
    }

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

    private fun sendMouseMove(dx: Int, dy: Int) {
        val clampedDx = dx.coerceIn(-127, 127).toByte()
        val clampedDy = dy.coerceIn(-127, 127).toByte()
        sendReport(byteArrayOf(0, clampedDx, clampedDy))
    }

    private fun sendReport(reportData: ByteArray) {
        if (hostDevice == null) {
            Log.w("MainActivity", "Cannot send report: No connected host device.")
            return
        }
        reportExecutor.execute {
            hidDevice?.sendReport(hostDevice, MOUSE_REPORT_ID.toInt(), reportData)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        reportExecutor.shutdown()
    }
}