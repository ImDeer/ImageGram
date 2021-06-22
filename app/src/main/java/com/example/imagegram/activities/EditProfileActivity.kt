package com.example.imagegram.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.imagegram.R
import com.example.imagegram.models.User
import com.example.imagegram.utils.CameraHelper
import com.example.imagegram.utils.FirebaseHelper
import com.example.imagegram.utils.ValueEventListenerAdapter
import com.example.imagegram.views.PasswordDialog
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {
    private val TAG = "EditProfileActivity"
    private lateinit var mUser: User
    private lateinit var mPendingUser: User
    private lateinit var mFirebase: FirebaseHelper
    private lateinit var mCamera: CameraHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.d(TAG, "onCreate")

        mCamera =
            CameraHelper(this)

        back_image.setOnClickListener { finish() }
        save_bt.setOnClickListener { updateProfile() }
        edit_profile_image_btn.setOnClickListener { mCamera.takeCameraPicture() }


        mFirebase = FirebaseHelper(this)

        mFirebase.currentUserReference()
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                mUser = it.asUser()!!
                edit_name_input.setText(mUser.name)
                edit_username_input.setText(mUser.username)
                edit_email_input.setText(mUser.email)
                profile_image.loadUserPhoto(mUser.photo)
            })
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == mCamera.REQUEST_CODE && resultCode == RESULT_OK) {

            // upload to firebase storage
            mFirebase.uploadUserPhoto(mCamera.imageUri!!) {
                mFirebase.getUrl().addOnCompleteListener {
                    val photoUrl = it.result.toString()
                    mFirebase.updateUserPhoto(photoUrl) {
                        //обновляем наш USERS
                        mUser = mUser.copy(photo = photoUrl)
                        profile_image.loadUserPhoto(mUser.photo)
                    }

                }
            }
        }
    }

    private fun updateProfile() {
        mPendingUser = readInputs()
        val error = validate(mPendingUser)
        if (error == null) {
            if (mPendingUser.email == mUser.email) {
                updateUser(mPendingUser)
            } else {
                // show pass dialog
                PasswordDialog().show(supportFragmentManager, "pass_dialog")
                //re-authenticate

            }
        } else {
            showToast(error)
        }
    }


    private fun readInputs(): User {
        return User( // создаем юзера из данных формы
            name = edit_name_input.text.toStringOrNull(),
            username = edit_username_input.text.toString(),
            email = edit_email_input.text.toString()
        )
    }

    override fun onPasswordConfirm(password: String) {
        if (password.isNotEmpty()) {

            val credential =
                EmailAuthProvider.getCredential(mUser.email, password) // get credential
            mFirebase.reauthenticate(credential) {//try to re-authentiate
                mFirebase.updateEmail(mPendingUser.email) {
                    updateUser(mPendingUser)
                }
            }
        } else {
            showToast(getString(R.string.pls_enter_your_password))
        }

    }

    private fun updateUser(user: User) {
        val updatesMap = mutableMapOf<String, Any?>()
        if (user.name != mUser.name) updatesMap["name"] = user.name
        if (user.username != mUser.username) updatesMap["username"] = user.username
        if (user.email != mUser.email) updatesMap["email"] = user.email

        mFirebase.updateUser(updatesMap) {
            showToast(getString(R.string.profile_successfully_updated))
            finish()
        }
    }

    private fun validate(user: User): String? =
        when {
            user.username.isEmpty() -> getString(R.string.please_enter_username)
            user.email.isEmpty() -> getString(R.string.please_enter_email)
            else -> null
        }
}

