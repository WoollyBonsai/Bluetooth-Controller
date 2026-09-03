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
import android.view.KeyEvent
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
    
    // upar wale buttons wagera
    private lateinit var btnInit: Button
    private lateinit var btnConnect: Button
    private lateinit var tvStatus: TextView
    private lateinit var spinnerMode: Spinner

    // alag alag screens ka setup
    private lateinit var layoutTrackpadOnly: View
    private lateinit var layoutTrackpadKeyboard: View
    private lateinit var layoutThinkpad: FrameLayout
    private lateinit var layoutMultimedia: View
    private lateinit var layoutPresentation: View
    private lateinit var layouts: Array<View>

    // pehla wala trackpad (sirf mouse)
    private lateinit var touchArea1: View
    private lateinit var btnLeftClick1: Button
    private lateinit var btnMiddleClick1: Button
    private lateinit var btnRightClick1: Button

    // dusra trackpad (isame keyboard bhi sath me ata hai)
    private lateinit var touchArea2: View
    private lateinit var btnLeftClick2: Button
    private lateinit var btnMiddleClick2: Button
    private lateinit var btnRightClick2: Button
    
    // ye apna naya thinkpad clit wala mode hai (mode 3)
    private lateinit var thinkpadKeyboardView: com.example.bluetoothtrackpad.views.ThinkpadKeyboardView
    
    private lateinit var layoutGamepad: FrameLayout
    private lateinit var gamepadView: com.example.bluetoothtrackpad.views.GamepadView
    
    private lateinit var etImmediateSend: EditText
    private lateinit var etStringSend: EditText
    private lateinit var btnSendString: Button
    private lateinit var btnClipboard: Button
    private lateinit var btnSpecialKeys: Button

    // mouse cursor ka haal chaal
    private var lastX = 0f
    private var lastY = 0f
    private var lastScrollY = 0f
    private var currentButtonsState: Byte = 0
    
    // ungliyo ka tracking (gestures)
    private var downTime = 0L
    private var isDragging = false
    private var hasMoved = false
    private var twoFingerTap = false
    private var twoFingerTapTime = 0L
    private val TAP_TIMEOUT = 250L

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
        setContentView(R.layout.activity_main)

        btnInit = findViewById(R.id.btnInit)
        btnConnect = findViewById(R.id.btnConnect)
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
            layoutPresentation,
            layoutGamepad
        )

        spinnerMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                switchLayout(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // pehle mode ka jugaad
        touchArea1 = findViewById(R.id.touchArea1)
        btnLeftClick1 = findViewById(R.id.btnLeftClick1)
        btnMiddleClick1 = findViewById(R.id.btnMiddleClick1)
        btnRightClick1 = findViewById(R.id.btnRightClick1)

        // dusre ka saman
        touchArea2 = findViewById(R.id.touchArea2)
        btnLeftClick2 = findViewById(R.id.btnLeftClick2)
        btnMiddleClick2 = findViewById(R.id.btnMiddleClick2)
        btnRightClick2 = findViewById(R.id.btnRightClick2)
        
        // teesra thinkpad saman bind kar rhe
        layoutThinkpad = findViewById(R.id.layoutThinkpad) as FrameLayout
        val touchArea3 = findViewById<View>(R.id.touchArea3)
        thinkpadKeyboardView = com.example.bluetoothtrackpad.views.ThinkpadKeyboardView(this)
        layoutThinkpad.addView(thinkpadKeyboardView)
        
        thinkpadKeyboardView.listener = object : com.example.bluetoothtrackpad.views.ThinkpadKeyboardView.Listener {
            override fun onKeyboardReport(modifiers: Byte, keyCodes: ByteArray) {
                if (hostDevice == null) return
                val reportData = ByteArray(8)
                reportData[0] = modifiers
                System.arraycopy(keyCodes, 0, reportData, 2, 6)
                reportExecutor.execute {
                    hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), reportData)
                }
            }
            override fun onMouseReport(buttons: Byte, dx: Int, dy: Int, wheel: Int) {
                sendMouseReport(dx, dy, wheel, buttons)
            }
        }
        
        layoutGamepad = findViewById(R.id.layoutGamepad) as FrameLayout
        gamepadView = com.example.bluetoothtrackpad.views.GamepadView(this)
        gamepadView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        layoutGamepad.addView(gamepadView)
        
        gamepadView.listener = object : com.example.bluetoothtrackpad.views.GamepadView.Listener {
            override fun onGamepadReport(buttons: Short, dpad: Byte, lx: Byte, ly: Byte, rx: Byte, ry: Byte) {
                if (hostDevice == null) return
                val reportData = ByteArray(7)
                reportData[0] = (buttons.toInt() and 0xFF).toByte()
                reportData[1] = ((buttons.toInt() shr 8) and 0xFF).toByte()
                reportData[2] = dpad
                reportData[3] = lx
                reportData[4] = ly
                reportData[5] = rx
                reportData[6] = ry
                
                reportExecutor.execute {
                    hidDevice?.sendReport(hostDevice, HidUtils.GAMEPAD_REPORT_ID.toInt(), reportData)
                }
            }
        }

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
                        tvStatus.text = "Disconnected"
                    }
                }
            }
        })

        btnInit.setOnClickListener {
            checkPermissionsAndInit()
        }

        btnConnect.setOnClickListener {
            showPairedDevicesDialog()
        }

        setupTrackpad(touchArea1)
        setupTrackpad(touchArea2)
        setupTrackpad(touchArea3)
        setupButtons()
        setupMultimedia()
        setupPresentation()
        setupKeyboardInputs()
        setupImmediateInput(etImmediateSend)
    }

    private fun showPairedDevicesDialog() {
        val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Bluetooth permission required", Toast.LENGTH_SHORT).show()
            return
        }

        val bondedDevices = bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
        if (bondedDevices.isEmpty()) {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show()
            return
        }

        val deviceNames = bondedDevices.map { it.name ?: it.address }.toTypedArray()
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Connect to Paired PC")
            .setItems(deviceNames) { _, which ->
                val device = bondedDevices[which]
                val currentHid = hidDevice ?: hidManager.getHidDevice()
                if (currentHid != null) {
                    try {
                        tvStatus.text = "Connecting..."
                        currentHid.connect(device)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Failed to connect: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "HID Profile not ready. Broadcast first.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun switchLayout(index: Int) {
        for (i in layouts.indices) {
            layouts[i].visibility = if (i == index) View.VISIBLE else View.GONE
        }
        
        // Force Landscape for Gamepad (mode index 5)
        if (index == 5) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    @SuppressLint("ClickableViewAccessibility")

    private fun setupMultimedia() {
        findViewById<Button>(R.id.btnVolDown).setOnClickListener { sendConsumerReport(0x00EA) }
        findViewById<Button>(R.id.btnVolUp).setOnClickListener { sendConsumerReport(0x00E9) }
        findViewById<Button>(R.id.btnMute).setOnClickListener { sendConsumerReport(0x00E2) }
        findViewById<Button>(R.id.btnPrevTrack).setOnClickListener { sendConsumerReport(0x00B6) }
        findViewById<Button>(R.id.btnNextTrack).setOnClickListener { sendConsumerReport(0x00B5) }
        findViewById<Button>(R.id.btnPlayPause).setOnClickListener { sendConsumerReport(0x00CD) }
        findViewById<Button>(R.id.btnBrightDown).setOnClickListener { sendConsumerReport(0x0070) }
        findViewById<Button>(R.id.btnBrightUp).setOnClickListener { sendConsumerReport(0x006F) }
        
        findViewById<Button>(R.id.btnDisplayMode).setOnClickListener {
            if (hostDevice == null) return@setOnClickListener
            // Win + P for Display Mode
            val reportData = ByteArray(8)
            reportData[0] = HidUtils.MOD_LEFT_SHIFT // Wait, Windows key is 0x08
            reportData[0] = 0x08.toByte() // Left GUI (Windows key)
            reportData[2] = 0x13.toByte() // 'P' key
            reportExecutor.execute {
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), reportData)
                val releaseData = ByteArray(8)
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), releaseData)
            }
        }
    }

    private fun setupPresentation() {
        findViewById<Button>(R.id.btnF5).setOnClickListener {
            // F5
            if (hostDevice == null) return@setOnClickListener
            val reportData = ByteArray(8)
            reportData[2] = 0x3E.toByte()
            reportExecutor.execute {
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), reportData)
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), ByteArray(8))
            }
        }
        findViewById<Button>(R.id.btnEsc).setOnClickListener {
            // Esc
            if (hostDevice == null) return@setOnClickListener
            val reportData = ByteArray(8)
            reportData[2] = 0x29.toByte()
            reportExecutor.execute {
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), reportData)
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), ByteArray(8))
            }
        }
        findViewById<Button>(R.id.btnPrevSlide).setOnClickListener {
            // Left Arrow
            if (hostDevice == null) return@setOnClickListener
            val reportData = ByteArray(8)
            reportData[2] = 0x50.toByte()
            reportExecutor.execute {
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), reportData)
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), ByteArray(8))
            }
        }
        findViewById<Button>(R.id.btnNextSlide).setOnClickListener {
            // Right Arrow
            if (hostDevice == null) return@setOnClickListener
            val reportData = ByteArray(8)
            reportData[2] = 0x4F.toByte()
            reportExecutor.execute {
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), reportData)
                hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), ByteArray(8))
            }
        }
        
        val touchAreaPresentation = findViewById<View>(R.id.touchAreaPresentation)
        setupTrackpad(touchAreaPresentation)
    }

    private fun setupButtons() {
        val buttonTouchListener = View.OnTouchListener { v, event ->
            val buttonMask = when (v.id) {
                R.id.btnLeftClick1, R.id.btnLeftClick2 -> 1.toByte()
                R.id.btnRightClick1, R.id.btnRightClick2 -> 2.toByte()
                R.id.btnMiddleClick1, R.id.btnMiddleClick2 -> 4.toByte()
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
        
        btnLeftClick2.setOnTouchListener(buttonTouchListener)
        btnRightClick2.setOnTouchListener(buttonTouchListener)
        btnMiddleClick2.setOnTouchListener(buttonTouchListener)
    }

    private fun setupImmediateInput(editText: EditText) {
        editText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                sendKeyboardReport(0, 0x2A) // delete maro bhai
                Thread {
                    Thread.sleep(10)
                    sendKeyboardReport(0, 0)
                }.start()
                return@setOnKeyListener true
            }
            false
        }

        var isClearing = false
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isClearing) return
                if (count > 0 && s != null) {
                    val char = s[start + count - 1]
                    sendChar(char)
                }
            }
            override fun afterTextChanged(s: Editable?) {
                if (isClearing) return
                if (s != null && s.isNotEmpty()) {
                    isClearing = true
                    s.clear()
                    isClearing = false
                }
            }
        })
    }

    private fun setupKeyboardInputs() {
        btnSendString.setOnClickListener {
            val text = etStringSend.text.toString()
            if (text.isNotEmpty()) {
                Thread {
                    for (char in text) {
                        sendChar(char)
                        Thread.sleep(20) // thoda sabar karo PC hang na ho jaye
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
                    // button chhodo varna dab ke reh jayega
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
            // halka sa ruk ke button chhod do pc ko register karne do
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
            0xC0.toByte(), // ye pc ko bata raha hai ki hum dono hain mouse bhi keyboard bhi
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTrackpad(view: View) {
        view.setOnTouchListener { _, event ->
            handleTrackpadTouch(event)
            true
        }
    }

    private val longPressRunnable = Runnable {
        if (!hasMoved) {
            isDragging = true
            currentButtonsState = (currentButtonsState.toInt() or 1).toByte()
            sendMouseReport(0, 0, 0)
        }
    }

    private fun handleTrackpadTouch(event: MotionEvent) {
        val pointerCount = event.pointerCount
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                downTime = System.currentTimeMillis()
                hasMoved = false
                twoFingerTap = false
                
                // agar 300ms tak chipak ke betha hai bina hile toh drag chalu (left click hold)
                handler.postDelayed(longPressRunnable, 300)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (pointerCount == 2) {
                    lastScrollY = event.getY(0) / 2 + event.getY(1) / 2
                    twoFingerTap = true
                    twoFingerTapTime = System.currentTimeMillis()
                    handler.removeCallbacks(longPressRunnable)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (pointerCount == 1) {
                    val dx = (event.x - lastX).toInt()
                    val dy = (event.y - lastY).toInt()

                    if (dx != 0 || dy != 0) {
                        if (Math.abs(dx) > 3 || Math.abs(dy) > 3) {
                            hasMoved = true
                            handler.removeCallbacks(longPressRunnable)
                        }
                        sendMouseReport(dx, dy, 0)
                    }
                    lastX = event.x
                    lastY = event.y
                } else if (pointerCount >= 2) {
                    val currentScrollY = event.getY(0) / 2 + event.getY(1) / 2
                    val rawDy = (currentScrollY - lastScrollY).toInt()
                    
                    val scrollSensitivity = 10 
                    if (Math.abs(rawDy) > scrollSensitivity) {
                        twoFingerTap = false // galti se tap chalu ho gya scroll me toh band kro
                        val wheel = -rawDy / scrollSensitivity
                        sendMouseReport(0, 0, wheel)
                        lastScrollY = currentScrollY
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (pointerCount == 2 && twoFingerTap) {
                    if (System.currentTimeMillis() - twoFingerTapTime < TAP_TIMEOUT) {
                        // right click marne ka signal bhejo
                        sendMouseReport(0, 0, 0, 2.toByte())
                        handler.postDelayed({ sendMouseReport(0, 0, 0, 0.toByte()) }, 50)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressRunnable)
                val upTime = System.currentTimeMillis()
                
                if (isDragging) {
                    isDragging = false
                    currentButtonsState = (currentButtonsState.toInt() and 1.inv()).toByte()
                    sendMouseReport(0, 0, 0)
                } else if (!hasMoved && pointerCount == 1 && (upTime - downTime) < TAP_TIMEOUT) {
                    // ek tap hua hai, left click daba ke hata lo 
                    sendMouseReport(0, 0, 0, 1.toByte())
                    handler.postDelayed({ sendMouseReport(0, 0, 0, 0.toByte()) }, 50)
                }
            }
        }
    }
    private fun sendConsumerReport(usageId: Int) {
        if (hostDevice == null) return
        val reportData = ByteArray(2)
        reportData[0] = (usageId and 0xFF).toByte()
        reportData[1] = ((usageId shr 8) and 0xFF).toByte()
        reportExecutor.execute {
            hidDevice?.sendReport(hostDevice, HidUtils.CONSUMER_REPORT_ID.toInt(), reportData)
            val releaseData = ByteArray(2)
            hidDevice?.sendReport(hostDevice, HidUtils.CONSUMER_REPORT_ID.toInt(), releaseData)
        }
    }


    private fun sendMouseReport(dx: Int, dy: Int, wheel: Int, buttonOverride: Byte? = null) {
        if (hostDevice == null) return
        val clampedDx = dx.coerceIn(-127, 127).toByte()
        val clampedDy = dy.coerceIn(-127, 127).toByte()
        val clampedWheel = wheel.coerceIn(-127, 127).toByte()
        
        val buttons = buttonOverride ?: currentButtonsState
        val reportData = byteArrayOf(buttons, clampedDx, clampedDy, clampedWheel)
        
        reportExecutor.execute {
            hidDevice?.sendReport(hostDevice, HidUtils.MOUSE_REPORT_ID.toInt(), reportData)
        }
    }

    private fun sendKeyboardReport(modifier: Byte, keycode: Byte) {
        if (hostDevice == null) return
        val reportData = byteArrayOf(modifier, 0, keycode, 0, 0, 0, 0, 0)
        reportExecutor.execute {
            hidDevice?.sendReport(hostDevice, HidUtils.KEYBOARD_REPORT_ID.toInt(), reportData)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        reportExecutor.shutdown()
        broadcastTimeoutRunnable?.let { handler.removeCallbacks(it) }
    }
}
