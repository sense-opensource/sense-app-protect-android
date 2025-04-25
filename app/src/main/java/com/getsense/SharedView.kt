package com.getsense

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _data = MutableLiveData<String>()
    val data: LiveData<String> get() = _data
    private val _dataSession = MutableLiveData<String>()
    val session: LiveData<String> get() = _dataSession

    fun setData(value: String) {
        _data.value = value
    }
    fun setSessionData(value: String) {
        _dataSession.value = value
    }
}
