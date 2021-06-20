package com.example.imagegram.activities

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.imagegram.R
import kotlinx.android.synthetic.main.bottom_navigation_view.*

abstract class BaseActivity(val NavItemNum: Int) : AppCompatActivity() {
    private val TAG = "BaseActivity"
    fun setupBottomNavigation() {
        bottom_navigation_view.setIconSize(30f, 30f)
        bottom_navigation_view.enableAnimation(false)
        bottom_navigation_view.enableShiftingMode(false)
        bottom_navigation_view.enableItemShiftingMode(false)

        bottom_navigation_view.setOnNavigationItemSelectedListener {
            val nextActivity =
                when (it.itemId) {
                    R.id.nav_item_home -> HomeActivity::class.java
                    R.id.nav_item_add -> AddActivity::class.java
                    R.id.nav_item_profile -> ProfileActivity::class.java
                    else -> {
                        Log.e(TAG, "unknown item clicked $it")
                        null
                    }
                }
            if (nextActivity != null) {
                val intent = Intent(this, nextActivity)
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(intent)
                overridePendingTransition(0, 0)
                true
            } else {
                false
            }
        }
        bottom_navigation_view.menu.getItem(NavItemNum).isChecked = true
    }
}