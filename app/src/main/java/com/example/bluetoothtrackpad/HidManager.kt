package com.example.bluetoothtrackpad

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import java.util.concurrent.Executors

class HidManager private constructor(val context: Context) {
    companion object {
        @Volatile private var instance: HidManager? = null
        
        fun getInstance(context: Context): HidManager {
            return instance ?: synchronized(this) {
                instance ?: HidManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    private lateinit var hidCallback: BluetoothHidDevice.Callback

    private var sdpSettings: BluetoothHidDeviceAppSdpSettings? = null

        var isInitialized = false

    fun initialize(sdp: BluetoothHidDeviceAppSdpSettings, callback: BluetoothHidDevice.Callback) {
        hidCallback = callback
        sdpSettings = sdp
        
        if (isInitialized) {
            // Already connected/registered, just update the callback and notify it of the current state
            if (connectedDevice != null) {
                hidCallback.onConnectionStateChanged(connectedDevice, BluetoothProfile.STATE_CONNECTED)
            }
            return
        }
        
        isInitialized = true
        val adapter = BluetoothAdapter.getDefaultAdapter()
        adapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = proxy as BluetoothHidDevice
                    // Auto-register SDP when proxy is connected
                    hidDevice?.registerApp(sdpSettings, null, null, Executors.newSingleThreadExecutor(), baseHidCallback)
                }
            }
            override fun onServiceDisconnected(profile: Int) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = null
                    isInitialized = false
                }
            }
        }, BluetoothProfile.HID_DEVICE)
    }

    fun startDiscovery() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 360)
        }
        context.startActivity(intent)
    }


    fun getConnectedDevice(): BluetoothDevice? {
        return connectedDevice
    }

    fun getHidDevice(): BluetoothHidDevice? {
        return hidDevice
    }

    private val baseHidCallback = object : BluetoothHidDevice.Callback() {
        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            if (state == BluetoothProfile.STATE_CONNECTED) {
                connectedDevice = device
                // LOCK DOWN: Stop being discoverable
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                connectedDevice = null
            }
            hidCallback.onConnectionStateChanged(device, state)
        }
        
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            hidCallback.onAppStatusChanged(pluggedDevice, registered)
        }
    }
}
