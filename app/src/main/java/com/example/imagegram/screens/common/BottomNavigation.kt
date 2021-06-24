package com.example.imagegram.screens.common

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.example.imagegram.R
import com.example.imagegram.screens.AddActivity
import com.example.imagegram.screens.home.HomeActivity
import com.example.imagegram.screens.ProfileActivity
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import kotlinx.android.synthetic.main.bottom_navigation_view.*

class BottomNavigation(
    private val bnv: BottomNavigationViewEx,
    private val navNumber: Int,
    activity: Activity
):LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        bnv.menu.getItem(navNumber).isChecked = true
    }

    init {
        bnv.setIconSize(30f, 30f)
        bnv.enableAnimation(false)
        bnv.enableShiftingMode(false)
        bnv.enableItemShiftingMode(false)

        bnv.setOnNavigationItemSelectedListener {
            val nextActivity =
                when (it.itemId) {
                    R.id.nav_item_home -> HomeActivity::class.java
                    R.id.nav_item_add -> AddActivity::class.java
                    R.id.nav_item_profile -> ProfileActivity::class.java
                    else -> {
                        Log.e(BaseActivity.TAG, "unknown item clicked $it")
                        null
                    }
                }
            if (nextActivity != null) {
                val intent = Intent(activity, nextActivity)
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                activity.startActivity(intent)
                activity.overridePendingTransition(0, 0)
                true
            } else {
                false
            }
        }
    }


}

fun BaseActivity.setupBottomNavigation(navNumber: Int) {
    val bnv = BottomNavigation(
        bottom_navigation_view,
        navNumber,
        this
    )
    this.lifecycle.addObserver(bnv)
}