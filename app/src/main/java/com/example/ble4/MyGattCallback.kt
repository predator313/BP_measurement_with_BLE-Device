package com.example.ble4

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import java.util.UUID

class MyGattCallback(private val context: Context):
    BluetoothGattCallback() {
    companion object{
        private const val TAG = "MyGattCallback"
    }
    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        when(newState){
            BluetoothProfile.STATE_CONNECTED->{
                //here we connected the bp device with the central device
                Log.d(TAG,"connected successfully")
                gatt?.discoverServices()
//                sendStartMeasurementCommand(gatt)

                //now let's redirect to the reading activity
//                val intent=  Intent(context,ReadingActivity::class.java)
//                context.startActivity(intent)
//                Toast.makeText(context.applicationContext,"successfully connected",Toast.LENGTH_SHORT).show()




            }
            BluetoothProfile.STATE_DISCONNECTED->{
                Log.d(TAG,"disconnected from GAtt server")
                gatt?.disconnect()
            }
        }
    }
    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
//            sendStartMeasurementCommand(gatt)
            val services = gatt!!.services
            for (service in services) {
                Log.d(TAG, "Service UUID: " + service.uuid.toString())
                val characteristics = service.characteristics
                for (characteristic in characteristics) {
                    Log.d(TAG, "Characteristic UUID: " + characteristic.uuid.toString())
                    if (BluetoothGattCharacteristic.PROPERTY_WRITE and characteristic.getProperties() > 0) {
                        // write set one
                        val uuid = characteristic.getUuid().toString()
                        Log.d(TAG, "Characteristic UUID write : " + uuid.toString())

                        // write_characteristic = gattService.getCharacteristic(Uuids.BLE_WRITE_UUID)
                    }
                    val descriptors = characteristic.descriptors
                    for (descriptor in descriptors) {
                      //   Log.d(TAG, "Descriptor UUID: " + descriptor.uuid.toString())
                    }
                }
            }



            val bloodPressureService = findBloodPressureService(gatt)

            if (bloodPressureService != null) {
                Log.d("hello","hello service not null")
                val bloodPressureCharacteristic = findBloodPressureCharacteristic(bloodPressureService)

                if (bloodPressureCharacteristic != null) {
                    Log.d("hello","hello characteristiic not null!!!!")
                    // Check if notifications or indications are supported
                    val characteristicProperties = bloodPressureCharacteristic.properties

                    Log.d("hello",characteristicProperties.toString())
                    val supportsNotifications = characteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
                    val supportsIndications = characteristicProperties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0

                    if (supportsNotifications) {
                        Log.d("hello","hello 1")
                        try{
                            Log.d("hello","hello 1 try 1")
                            gatt?.setCharacteristicNotification(bloodPressureCharacteristic, true)

                            // Enable notifications on the characteristic descriptor
                            val descriptor = bloodPressureCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt?.setCharacteristicNotification(bloodPressureCharacteristic,true)
                            gatt?.writeDescriptor(descriptor)
                            Log.d("hello","hello 1 try 2")
                        }catch (e:Exception){
                            Log.d("hello","hello 1 catch "+e.message)
                        }
                        // Enable notifications
                    } else if (supportsIndications) {
                        Log.d("hello","hello 2")
                        // Enable indications
                        gatt?.setCharacteristicNotification(bloodPressureCharacteristic, true)
                        // Enable indications on the characteristic descriptor
                        val descriptor = bloodPressureCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                        descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        gatt?.setCharacteristicNotification(bloodPressureCharacteristic,true)
                        gatt?.writeDescriptor(descriptor)

                    } else {
                        // Handle the case where neither notifications nor indications are supported
                        Log.e(TAG, "hello Notifications and indications not supported")
                    }
                } else {
                    Log.e(TAG, "hello Blood Pressure characteristic not found.")
                }
            } else {
                Log.e(TAG, "hello null Blood Pressure service not found.")
            }
        } else {
            Log.w(TAG, "onServicesDiscovered received: $status")
        }
    }


    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
    }
    val BLE_WRITE_UUID = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")

    @SuppressLint("MissingPermission")
    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)


//        val service=findBloodPressureService(gatt);
        val service = gatt?.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))
        Log.d("james",service.toString()+"   service")
//        val characteristic=findBloodPressureCharacteristic(service)
        val characteristic = service?.getCharacteristic(UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb"))
        Log.d("james",characteristic.toString()+"    characteristic")
        Log.d("james","onDescriptorWrite called characteristic - $characteristic")

        // gatt!!.writeCharacteristic(characteristic)
        if(status == BluetoothGatt.GATT_SUCCESS){
            sendStartMeasurementCommand(gatt)
            Log.d("james","hello from ondescriptor write success   "+status)
        }else{
            Log.d("james","hello from ondescriptor write else    "+status)
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        val viewModel = ViewModelProvider(context as MainActivity)[BloodPressureViewModel::class.java]
        var i = 0

        Log.d("hello","hello onCharacteristicChanged ByteArray - ${value.toList()} ")

        for(b in value){
            val intvalue = b.toInt() and 0xFF
//            var sp=0
//            var dp=0
            if(i==4){
                Log.d("hello","hello hi sys "+intvalue)
                viewModel._systolicPressure.postValue(intvalue)
//                viewModel.setBloodPressure(intvalue,0)
//                sp=intvalue


            }
            if(i==5){
                Log.d("hello","hello hi dias "+intvalue)
                viewModel._diastolicPressure.postValue(intvalue)
//                viewModel.setBloodPressure(0,intvalue)
//                dp=intvalue

            }
            if(i == 6){
                Log.d("hello","hello hi pulse "+intvalue)
                viewModel._pulseRate.postValue(intvalue)
            }
            i = i+1
//            viewModel.setBloodPressure(sp,dp)

        }



        //Log.d("hello","hello from onCharacteristicChanged")
        //Log.d("hello", value.toString())
        // Parse the blood pressure data from the received value
        val bloodPressureData = parseBloodPressureData(value)

        // Get a reference to the BloodPressureViewModel
//        val viewModel = ViewModelProvider(context as MainActivity)[BloodPressureViewModel::class.java]


        // Update the ViewModel with the received data
//        viewModel.setBloodPressure(bloodPressureData.sp, bloodPressureData.dp)
//        viewModel.setBloodPressure(20,40)
        // Check if the received characteristic is the one you are looking for
        sendStartMeasurementCommand(gatt)


    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
//        super.onCharacteristicRead(gatt, characteristic, value, status)
        //we remove the super call because it doesn't provide any default implementation
        //for characteristic Read
        if(status== BluetoothGatt.GATT_SUCCESS){
//            val bloodPressureData=characteristic?.value
//            Log.d("hello","$bloodPressureData")
            Log.d("hello","we are in onCharacteristicRead")
            val bloodPressureData=parseBloodPressureData(value)

            Log.d("hello","diastolic pressure ... $bloodPressureData.sp")
            Log.d("hello","systolic pressure ... $bloodPressureData.dp")
//            sendStartMeasurementCommand(gatt)






        }
    }
    private fun findBloodPressureService(gatt: BluetoothGatt?): BluetoothGattService?{
        return gatt?.services?.find {
            it.uuid.toString().equals("0000ffe0-0000-1000-8000-00805f9b34fb")
        }

    }
    private fun findBloodPressureCharacteristic(service: BluetoothGattService?): BluetoothGattCharacteristic?{
        Log.d("hello","from findBloodPressureCharacteristic")
        return service?.characteristics?.find {
            it.uuid.toString().equals("0000ffe4-0000-1000-8000-00805f9b34fb")

        }


    }
    private fun parseBloodPressureData(data: ByteArray): BloodPressure {
        if (data.size < 4) {
            // Handle invalid data length
            return BloodPressure(0, 0) // Return default values
        }

        // Assuming the first two bytes are for systolic pressure and the next two are for diastolic pressure
        val systolicPressure = (data[0].toInt() and 0xFF) or ((data[1].toInt() and 0xFF) shl 8)
        val diastolicPressure = (data[2].toInt() and 0xFF) or ((data[3].toInt() and 0xFF) shl 8)
        //Log.d("hello","hello sys "+systolicPressure)
        //Log.d("hello","hello dias "+diastolicPressure)
        return BloodPressure(systolicPressure, diastolicPressure)
    }

    val cmd_dev_start = 0x11.toByte()

@SuppressLint("MissingPermission")
 fun sendStartMeasurementCommand(gatt: BluetoothGatt?) {
    val startMeasurementCommand = getCmd(cmd_dev_start,null);//byteArrayOf(0x5A, 0x05, 0x11)
    Log.d("james",startMeasurementCommand?.toList().toString()+" bytearray")
    //val crc16Checksum = getAnthorCrt(startMeasurementCommand)// CRC16.CRC(startMeasurementCommand,0x0000,)// calculateCRC16(startMeasurementCommand)
  //  val commandWithChecksum = startMeasurementCommand //+ crc16Checksum
   // Log.d("james",commandWithChecksum.toList().toString()+" commandcsum")

    val service = gatt?.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))
//    val service=findBloodPressureService(gatt);
    Log.d("james",service.toString()+"   service")
    val characteristic = service?.getCharacteristic(UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb"))
//    val characteristic=findBloodPressureCharacteristic(service)
    Log.d("james",characteristic.toString()+"    characteristic")

    if (characteristic != null && gatt != null) {
        // Set write type and permissions
        Log.d("james","if block")

        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        characteristic.value = startMeasurementCommand//commandWithChecksum
//        characteristic.value= byteArrayOf(1,1)
        val descriptor = characteristic.getDescriptor(
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        )
       // Log.d("james",descriptor.toString()+"  descriptor")
         descriptor.value = startMeasurementCommand// BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
         val success = gatt.writeDescriptor(descriptor)
//        val success = gatt.writeCharacteristic(characteristic)

        Log.d("james", "wrie ble uuid - ${characteristic.uuid} , is success - $success ")

        if (success) {
            Log.d("james", "Start Measurement command sent successfully")
        } else {
            Log.e("james", "Failed to send Start Measurement command")
        }
    } else {
        Log.e("GattCallback", "Characteristic not found")
    }
}

    fun getByteFromUByte(bt: Int): Byte {
        if (bt > 127) {
            val bttemp = bt - 256
            return bttemp.toByte()
        }
        return bt.toByte()
    }


    fun getCrt(p: ByteArray): ByteArray {
        val temdata = ByteArray(4)
        val CL = 0x01.toChar()
        val CH = 0xA0.toChar()
        var SaveHi: Char
        var SaveLo: Char
        var i: Char
        var Flag: Char
        var CRC16Lo = 0xff.toChar()
        var CRC16Hi = 0xff.toChar()
        val num = p.size
        i = 0.toChar()
        while (i.code < num) {
            CRC16Lo = (CRC16Lo.code xor (p[i.code].toInt() and 0xFF)).toChar()
            Flag = 0.toChar()
            while (Flag.code < 8) {
                SaveHi = CRC16Hi
                SaveLo = CRC16Lo
                CRC16Hi = (CRC16Hi.code / 2).toChar()
                CRC16Lo = (CRC16Lo.code / 2).toChar()
                if (SaveHi.code and 0x1 == 1) {
                    CRC16Lo = (CRC16Lo.code or 0x80).toChar()
                }
                if (SaveLo.code and 0x1 == 1) {
                    CRC16Hi = (CRC16Hi.code xor CH.code).toChar()
                    CRC16Lo = (CRC16Lo.code xor CL.code).toChar()
                }
                Flag++
            }
            i++
        }
        temdata[temdata.size - 2] = getByteFromUByte(CRC16Hi.code and 0xFF)
        temdata[temdata.size - 1] = getByteFromUByte(CRC16Lo.code and 0xFF)
        return temdata
    }

    fun getAnthorCrt(p: ByteArray): ByteArray {
        val temdata = ByteArray(4)
        val CL = 0x01.toChar()
        val CH = 0xA0.toChar()
        var SaveHi: Char
        var SaveLo: Char
        var i: Char
        var Flag: Char
        var CRC16Lo = 0xff.toChar()
        var CRC16Hi = 0xff.toChar()
        val num = p.size - 2
        i = 0.toChar()
        while (i.code < num) {
            CRC16Lo = (CRC16Lo.code xor (p[i.code].toInt() and 0xFF)).toChar()
            Flag = 0.toChar()
            while (Flag.code < 8) {
                SaveHi = CRC16Hi
                SaveLo = CRC16Lo
                CRC16Hi = (CRC16Hi.code / 2).toChar()
                CRC16Lo = (CRC16Lo.code / 2).toChar()
                if (SaveHi.code and 0x1 == 1) {
                    CRC16Lo = (CRC16Lo.code or 0x80).toChar()
                }
                if (SaveLo.code and 0x1 == 1) {
                    CRC16Hi = (CRC16Hi.code xor CH.code).toChar()
                    CRC16Lo = (CRC16Lo.code xor CL.code).toChar()
                }
                Flag++
            }
            i++
        }
        temdata[temdata.size - 2] = getByteFromUByte(CRC16Hi.code and 0xFF)
        temdata[temdata.size - 1] = getByteFromUByte(CRC16Lo.code and 0xFF)
        return temdata
    }

    private fun calculateCRC16(command: ByteArray): ByteArray {
        var crc: Int = 0xFFFF
        val polynomial: Int = 0x1021

        for (b in command) {
            var i = 0
            while (i < 8) {
                val bit = (b.toInt() shr 7 - i) and 1 == 1
                val c15 = (crc shr 15 and 1) == 1
                crc = crc shl 1
                if (c15 xor bit) {
                    crc = crc xor polynomial
                }
                i++
            }
        }

        // Finalize CRC value
        crc = crc and 0xFFFF

        // Convert CRC to a byte array (big endian)
        val crcBytes = byteArrayOf(((crc shr 8) and 0xFF).toByte(), (crc and 0xFF).toByte())
        return crcBytes
    }

    fun getCmd(cmd: Byte, args: ByteArray?): ByteArray {
        var blen: Byte = 0x05
        if (args != null) {
            var len = args.size
            len = len + 5
            blen = (0xff and len).toByte()
        }
        val data = ByteArray(blen.toInt())
        data[0] = 0x5a
        data[1] = blen
        data[2] = cmd
        if (args != null) {
            System.arraycopy(args, 0, data, 3, args.size)
        }
        val tdata = ByteArray(blen - 2)
        System.arraycopy(data, 0, tdata, 0, data.size - 2)
        val crt = getCrt(tdata)
        //data[data.length-4]=crt[0];
        //data[data.length-3]=crt[1];
        data[data.size - 2] = crt[2]
        data[data.size - 1] = crt[3]
        return data
    }


}