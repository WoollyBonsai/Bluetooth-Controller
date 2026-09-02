package com.example.bluetoothtrackpad

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {

    private lateinit var hidManager: HidManager
    
    // Top Bar
    private lateinit var btnInit: Button
    private lateinit var tvStatus: TextView
    private lateinit var spinnerMode: Spinner

    // Layouts
    private lateinit var layoutTrackpadOnly: View
    private lateinit var layoutTrackpadKeyboard: View
    private lateinit var layoutThinkpad: View
    private lateinit var layoutMultimedia: View
    private lateinit var layoutPresentation: View
    private lateinit var layouts: Array<View>

    // Trackpad 1 (Mode 1)
    private lateinit var touchArea1: View
    private lateinit var btnLeftClick1: Button
    private lateinit var btnMiddleClick1: Button
    private lateinit var btnRightClick1: Button

    // Trackpad 2 (Mode 2)
    private lateinit var touchArea2: View
    private lateinit var etImmediateSend: EditText
    private lateinit var etStringSend: EditText
    private lateinit var btnSendString: Button
    private lateinit var btnClipboard: Button
    private lateinit var btnSpecialKeys: Button

    private var lastX = 0f
    private var lastY = 0f
    private var lastScrollY = 0f
    private var currentButtonsState: Byte = 0

    private var hostDevice: BluetoothDevice? = null
    private val handler = Handler(Looper.getMainLooper())
    private val reportExecutor = Executors.newSingleThreadExecutor()
    private var hidDevice: BluetoothHidDevice? = null
    private var broadcastTimeoutRunnable: Runnable? = null

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
        // Note: Removed fixed screen orientation so it can freely rotate to portrait/landscape
        setContentView(R.layout.activity_main)

        btnInit = findViewById(R.id.btnInit)
        tvStatus = findViewById(R.id.tvStatus)
        spinnerMode = findViewById(R.id.spinnerMode)

        layoutTrackpadOnly = findViewById(R.id.layoutTrackpadOnly)
        layoutTrackpadKeyboard = findViewById(R.id.layoutTrackpadKeyboard)
        layoutThinkpad = findViewById(R.id.layoutThinkpad)
        layoutMultimedia = findViewById(R.id.layoutMultimedia)
        layoutPresentation = findViewById(R.id.layoutPresentation)
        
        layouts = arrayOf(
            layoutTrackpadOnly,
            layoutTrackpadKeyboard,
            layoutThinkpad,
            layoutMultimedia,
            layoutPresentation
        )

        spinnerMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                switchLayout(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Mode 1 UI
        touchArea1 = findViewById(R.id.touchArea1)
        btnLeftClick1 = findViewById(R.id.btnLeftClick1)
        btnMiddleClick1 = findViewById(R.id.btnMiddleClick1)
        btnRightClick1 = findViewById(R.id.btnRightClick1)

        // Mode 2 UI
        touchArea2 = findViewById(R.id.touchArea2)
        etImmediateSend = findViewById(R.id.etImmediateSend)
        etStringSend = findViewById(R.id.etStringSend)
        btnSendString = findViewById(R.id.btnSendString)
        btnClipboard = findViewById(R.id.btnClipboard)
        btnSpecialKeys = findViewById(R.id.btnSpecialKeys)

        hidManager = HidManager(this)
        hidManager.initialize(object : BluetoothHidDevice.Callback() {
            override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
                super.onConnectionStateChanged(device, state)
                runOnUiThread {
                    if (state == BluetoothProfile.STATE_CONNECTED) {
                        hostDevice = device
                        hidDevice = hidManager.getHidDevice()
                        tvStatus.text = "Connected"
                        broadcastTimeoutRunnable?.let { handler.removeCallbacks(it) }
                    } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                        hostDevice = null
                        hidDevice = null
                        tvStatus.text = "Disconnected"
                    }
                }
            }
        })

        btnInit.setOnClickListener {
            checkPermissionsAndInit()
        }

        setupTrackpad(touchArea1)
        setupTrackpad(touchArea2)
        setupButtons()
        setupKeyboardInputs()
    }

    private fun switchLayout(index: Int) {
        for (i in layouts.indices) {
            layouts[i].visibility = if (i == index) View.VISIBLE else View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupButtons() {
        val buttonTouchListener = View.OnTouchListener { v, event ->
            val buttonMask = when (v.id) {
                R.id.btnLeftClick1 -> 1.toByte()
                R.id.btnRightClick1 -> 2.toByte()
                R.id.btnMiddleClick1 -> 4.toByte()
                else -> 0.toByte()
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    currentButtonsState = (currentButtonsState.toInt() or buttonMask.toInt()).toByte()
                    sendMouseReport(0, 0, 0)
                    v.isPressed = true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    currentButtonsState = (currentButtonsState.toInt() and buttonMask.toInt().inv()).toByte()
                    sendMouseReport(0, 0, 0)
                    v.isPressed = false
                }
            }
            true
        }

        btnLeftClick1.setOnTouchListener(buttonTouchListener)
        btnRightClick1.setOnTouchListener(buttonTouchListener)
        btnMiddleClick1.setOnTouchListener(buttonTouchListener)
    }

    private fun setupKeyboardInputs() {
        etImmediateSend.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0 && s != null) {
                    val char = s[start + count - 1]
                    sendChar(char)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSendString.setOnClickListener {
            val text = etStringSend.text.toString()
            if (text.isNotEmpty()) {
                Thread {
                    for (char in text) {
                        sendChar(char)
                        Thread.sleep(20) // small delay between keystrokes
                    }
                    runOnUiThread { etStringSend.text.clear() }
                }.start()
            }
        }
        
        btnSpecialKeys.setOnClickListener {
            val specialKeys = arrayOf("Escape", "Enter", "Tab", "Backspace", "Space", "F1", "F2", "F3", "F4", "F5", "F12")
            val keyCodes = byteArrayOf(0x29, 0x28, 0x2B, 0x2A, 0x2C, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x45)
            
            android.app.AlertDialog.Builder(this)
                .setTitle("Special Keys")
                .setItems(specialKeys) { _, which ->
                    sendKeyboardReport(0, keyCodes[which])
                    // Release key
                    Thread {
                        Thread.sleep(10)
                        sendKeyboardReport(0, 0)
                    }.start()
                }
                .show()
        }
        
        btnClipboard.setOnClickListener {
            val clipboardManager = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            if (clipboardManager.hasPrimaryClip()) {
                val text = clipboardManager.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                if (text.isNotEmpty()) {
                    Thread {
                        for (char in text) {
                            sendChar(char)
                            Thread.sleep(20)
                        }
                    }.start()
                }
            }
        }
    }

    private fun sendChar(char: Char) {
        val mapping = HidUtils.charToHid(char)
        if (mapping != null) {
            sendKeyboardReport(mapping.first, mapping.second)
            // Need a slight delay and then release
            Thread.sleep(10)
            sendKeyboardReport(0, 0)
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
            "A virtual mouse and keyboard for your computer",
            "Example Inc.",
            0xC0.toByte(), // SUBCLASS1_COMBO (Mouse + Keyboard)
            HidUtils.COMPOSITE_HID_DESCRIPTOR
        )
        hidManager.startDiscovery(sdpSettings)
        tvStatus.text = "Broadcasting (60s)"

        broadcastTimeoutRunnable?.let { handler.removeCallbacks(it) }
        broadcastTimeoutRunnable = Runnable {
            if (hostDevice == null) {
                tvStatus.text = "Timeout. Tap Broadcast."
                Toast.makeText(this, "Broadcast timed out. Please try again.", Toast.LENGTH_LONG).show()
            }
        }
        handler.postDelayed(broadcastTimeoutRunnable!!, 60000)
    }

    private fun setupTrackpad(view: View) {
        view.setOnTouchListener { _, event ->
            handleTrackpadTouch(event)
            true
        }
    }

    private fun handleTrackpadTouch(event: MotionEvent) {
        val pointerCount = event.pointerCount
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (pointerCount == 2) {
                    lastScrollY = event.getY(0) / 2 + event.getY(1) / 2
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (pointerCount == 1) {
                    val dx = (event.x - lastX).toInt()
                    val dy = (event.y - lastY).toInt()

                    if (dx != 0 || dy != 0) {
                        sendMouseReport(dx, dy, 0)
                    }
                    lastX = event.x
                    lastY = event.y
                } else if (pointerCount >= 2) {
                    val currentScrollY = event.getY(0) / 2 + event.getY(1) / 2
                    val rawDy = (currentScrollY - lastScrollY).toInt()
                    
                    val scrollSensitivity = 10 
                    if (Math.abs(rawDy) > scrollSensitivity) {
                        val wheel = -rawDy / scrollSensitivity
                        sendMouseReport(0, 0, wheel)
                        lastScrollY = currentScrollY
                    }
                }
            }
        }
    }

    private fun sendMouseReport(dx: Int, dy: Int, wheel: Int) {
        if (hostDevice == null) return
        val clampedDx = dx.coerceIn(-127, 127).toByte()
        val clampedDy = dy.coerceIn(-127, 127).toByte()
        val clampedWheel = wheel.coerceIn(-127, 127).toByte()
        
        val reportData = byteArrayOf(currentButtonsState, clampedDx, clampedDy, clampedWheel)
        
        reportExecutor.execute {
            hidDevice?.sendReport(hostDevice, HidUtils.MOUSE_REPORT_ID.toInt(), reportData)
        }
    }

    // Placeholders for keyboard and media reports
    private fun sendKeyboardReport(modifier: Byte, keycode: Byte) {
        if (hostDevice == null) return
        val reportData = byteArrayOf(modifier, 0, keycode, 0, 0, 0, 0, 0)
        reportExecutor.execute {
            hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), reportData)
        }
    }
    
    private fun sendConsumerReport(mediaKey: Int) {
        if (hostDevice == null) return
        // Consumer report is 16 bits (2 bytes)
        val reportData = byteArrayOf((mediaKey and 0xFF).toByte(), ((mediaKey shr 8) and 0xFF).toByte())
        reportExecutor.execute {
            hidDevice?.sendReport(hostDevice, HidUtils.CONSUMER_REPORT_ID.toInt(), reportData)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        reportExecutor.shutdown()
        broadcastTimeoutRunnable?.let { handler.removeCallbacks(it) }
    }
}
