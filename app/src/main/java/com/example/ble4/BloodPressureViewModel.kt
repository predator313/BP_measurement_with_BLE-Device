package com.example.ble4

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BloodPressureViewModel: ViewModel() {
    val _systolicPressure = MutableLiveData<Int>(0)
//    val _systolicPressure = MutableLiveData<Int>(0)
    val systolicPressure: LiveData<Int>
        get() = _systolicPressure

    val _diastolicPressure = MutableLiveData<Int>(0)
//    val _diastolicPressure = MutableLiveData<Int>(0)
    val diastolicPressure: LiveData<Int>
        get() = _diastolicPressure
    val _pulseRate = MutableLiveData<Int>(0)
    val pulseRate:LiveData<Int>
        get()=_pulseRate
    fun setBloodPressure(systolic: Int, diastolic: Int) {
        _systolicPressure.postValue(systolic)
        _diastolicPressure.postValue(diastolic)
        Log.d("hello","Android+setBlood pressure")
    }
}