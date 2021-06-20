package com.example.imagegram.activities

import android.os.Bundle
import android.util.Log
import com.example.imagegram.R

class AddActivity : BaseActivity(1) {
    private val TAG = "AddActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        Log.d(TAG, "onCreate")
        setupBottomNavigation()
    }
}