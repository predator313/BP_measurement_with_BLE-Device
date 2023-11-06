package com.example.ble4

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BLEScanner(private val activity: Activity, private val callback: BLEScanCallback) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    private val PERMISSION_REQUEST_CODE = 1

    // Define the array of permissions needed for BLE scanning
    private val permissions = arrayOf(
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.BLUETOOTH_ADMIN,
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    //    @SuppressLint("MissingPermission")
//    fun startScanning() {
//        if (checkPermissions()) {
//            val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
//            val scanSettings = ScanSettings.Builder()
//                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                .build()
//            bluetoothLeScanner?.startScan(null,scanSettings,scanCallback)
////            bluetoothLeScanner?.startScan(scanCallback)
//        }
//    }
    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (checkPermissions() && isBluetoothAvailableAndEnabled()) {
            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)
        }
    }
    @SuppressLint("MissingPermission")
    fun stopScanning() {
        bluetoothLeScanner?.stopScan(scanCallback)
    }
    private fun isBluetoothAvailableAndEnabled(): Boolean {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }

    private fun checkPermissions(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != granted) {
                ActivityCompat.requestPermissions(activity, arrayOf(permission), PERMISSION_REQUEST_CODE)
                return false
            }
        }

        return true
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                val device = it.device
                callback.onDeviceScanned(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            callback.onScanFailed(errorCode)
        }
    }


    interface BLEScanCallback {
        fun onDeviceScanned(device: BluetoothDevice)
        fun onScanFailed(errorCode: Int)
    }
}

