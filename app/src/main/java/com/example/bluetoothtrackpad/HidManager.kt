package com.example.bluetoothtrackpad

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import java.util.concurrent.Executors

class HidManager(val context: Context) {
    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    private lateinit var hidCallback: BluetoothHidDevice.Callback

    fun initialize(callback: BluetoothHidDevice.Callback) {
        hidCallback = callback
        val adapter = BluetoothAdapter.getDefaultAdapter()
        adapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = proxy as BluetoothHidDevice
                    // Do NOT register here yet. Wait for button trigger.
                }
            }
            override fun onServiceDisconnected(profile: Int) {}
        }, BluetoothProfile.HID_DEVICE)
    }

    fun startDiscovery(sdp: BluetoothHidDeviceAppSdpSettings) {
        // This is the manual trigger
        hidDevice?.registerApp(sdp, null, null, Executors.newSingleThreadExecutor(), baseHidCallback)

        // Make discoverable for 60 seconds
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60)
        }
        context.startActivity(intent)
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
    }
}
