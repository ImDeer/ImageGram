package com.example.imagegram.utils

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference

class FirebaseLiveData(private val reference: DatabaseReference) : LiveData<DataSnapshot>() {
    private val listener =
        ValueEventListenerAdapter {
            value = it
        }

    override fun onInactive() {
        super.onInactive()
        reference.addValueEventListener(listener)
    }

    override fun onActive() {
        super.onActive()
        reference.removeEventListener(listener)
    }
}