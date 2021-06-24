package com.example.imagegram.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.imagegram.R
import com.example.imagegram.screens.common.coordinateBtnsAndInputs
import com.example.imagegram.screens.common.showToast
import com.example.imagegram.screens.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

class LoginActivity : AppCompatActivity(), KeyboardVisibilityEventListener, View.OnClickListener {
    private val TAG = "LoginActivity"
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d(TAG, "onCreate")

        KeyboardVisibilityEvent.setEventListener(this, this)
        coordinateBtnsAndInputs(
            login_bt,
            email_input,
            password_input
        )
        login_bt.setOnClickListener(this)

        signup_hint.setOnClickListener(this)
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onVisibilityChanged(isKeyboardOpen: Boolean) {
        if (isKeyboardOpen) {
            signup_hint.visibility = View.GONE
            login_signup_google.visibility = View.GONE
        } else {
            signup_hint.visibility = View.VISIBLE
            login_signup_google.visibility = View.VISIBLE
        }
    }

    private fun validate(email: String, password: String) =
        email.isNotEmpty() && password.isNotEmpty()

    override fun onClick(view: View) {
        when (view.id) {
            R.id.login_bt -> {
                val email = email_input.text.toString()
                val password = password_input.text.toString()
                if (validate(email, password)) {
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful) {
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        }
                    }
                } else {
                    showToast(getString(R.string.pls_enter_email_password))
                }
            }
            R.id.signup_hint -> {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        }

    }
}