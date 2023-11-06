package com.example.ble4

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ble4.adapter.MyAdapter
import com.example.ble4.databinding.ActivityMainBinding
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bleScanner: BLEScanner
    private lateinit var permissionManager: PermissionManager
    private lateinit var handler: Handler
    private lateinit var BleAdapter: MyAdapter
    private val devicesList = mutableListOf<BluetoothDevice>()
    private var bluetoothGatt: BluetoothGatt? = null
    private lateinit var viewModel: BloodPressureViewModel
//    private var gattCallback:MyGattCallback?=null
    private lateinit var myGattCallback: MyGattCallback


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
//        myGattCallback=MyGattCallback(this)





        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler = Handler()
        permissionManager = PermissionManager(this)
        BleAdapter = MyAdapter()

        setupRecyclerView()
        BleAdapter.setOnItemClickListener {
//            Toast.makeText(this@MainActivity, it.address, Toast.LENGTH_SHORT).show()
            connectToDevice(it)
            binding.rvMain.visibility = View.GONE
            binding.pRate.visibility = View.VISIBLE
//            myGattCallback.sendStartMeasurementCommand(bluetoothGatt)
        }
        viewModel = ViewModelProvider(this)[BloodPressureViewModel::class.java]
        viewModel.diastolicPressure.observe(this, Observer {
            binding.etDis.text = "DiastolicPressure\n" + it.toString()
        })
        viewModel.systolicPressure.observe(this, Observer {
            binding.etSys.text = "SystolicPressure:\n" + it.toString()
        })
        viewModel.pulseRate.observe(this, Observer {
            binding.pRate.text = "Pulse Rate:\n" + it.toString()
        })


        bleScanner = BLEScanner(this, object : BLEScanner.BLEScanCallback {
            @SuppressLint("MissingPermission")
            override fun onDeviceScanned(device: BluetoothDevice) {
//                val bleDevice = BluetoothDevice(device.name, device.address)
                if (device.name == "Q06B") {
                    // Clear the devices list before adding new devices
                    devicesList.clear()
                    devicesList.add(device)
                    BleAdapter.bles = devicesList
                }
            }

            override fun onScanFailed(errorCode: Int) {
                // Handle scan failure
                Log.e("BLE", "Scan Failed: Error Code $errorCode")
            }
        })

        // Start BLE scanning
        binding.btnStartStop.setOnClickListener {
            Toast.makeText(this@MainActivity, "Start scan..", Toast.LENGTH_SHORT).show()
            bleScanner.startScanning()
            val scanDuration = 5000L // Scan for 5 seconds
            handler.postDelayed({
                bleScanner.stopScanning()
                devicesList.clear()
                BleAdapter.bles = devicesList
            }, scanDuration)
        }
        // Observe the systolic and diastolic pressure values and update the UI
//        viewModel.systolicPressure.observe(this, { systolic ->
//            viewModel.diastolicPressure.observe(this, { diastolic ->
//                binding..text = "Systolic: $systolic"
//                binding.tvDiastolic.text = "Diastolic: $diastolic"
//            })
//        })zz

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.handlePermissionsResult(requestCode, grantResults)

    }

    private fun setupRecyclerView() {
//        BleAdapter = MyAdapter()
        binding.rvMain.apply {
            adapter = BleAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)

        }


    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        if (bluetoothGatt != null) {
            //we need to first cancel the  current connection
//            sendStartMeasurementCommand()
            bluetoothGatt?.close()
            bluetoothGatt = null

        }
        val gattCallback = MyGattCallback(this@MainActivity)
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
        if (bluetoothGatt != null) {
            //means we successfully connected to the device
            Toast.makeText(this@MainActivity, "connected .. to Q06B", Toast.LENGTH_SHORT).show()
            Log.d("hello", "we are successfully connected to the ${device.name}")



        } else {
            Log.e("hello", "connection is interrupted")
        }
    }

}