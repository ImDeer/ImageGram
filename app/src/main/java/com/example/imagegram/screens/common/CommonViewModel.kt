package com.example.imagegram.screens.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnFailureListener

class CommonViewModel:ViewModel(), OnFailureListener {
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    override fun onFailure(e: Exception) {
        _errorMessage.value = e.message
    }


}