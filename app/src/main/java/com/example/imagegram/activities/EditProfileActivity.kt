package com.example.imagegram.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.imagegram.R
import com.example.imagegram.models.User
import com.example.imagegram.utils.CameraPictureTaker
import com.example.imagegram.utils.FirebaseHelper
import com.example.imagegram.utils.ValueEventListenerAdapter
import com.example.imagegram.views.PasswordDialog
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {
    private val TAG = "EditProfileActivity"
    private lateinit var mUser: User
    private lateinit var mPendingUser: User
    private lateinit var mFirebaseHelper: FirebaseHelper
    private lateinit var mPhotoUrl: String
    private lateinit var mCameraPictureTaker: CameraPictureTaker


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.d(TAG, "onCreate")

        mCameraPictureTaker =
            CameraPictureTaker(this)

        back_image.setOnClickListener { finish() }
        save_bt.setOnClickListener { updateProfile() }
        edit_profile_image_btn.setOnClickListener { mCameraPictureTaker.takeCameraPicture() }


        mFirebaseHelper = FirebaseHelper(this)

        mFirebaseHelper.currentUserReference()
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                mUser = it.getValue(User::class.java)!!
                edit_name_input.setText(mUser.name)
                edit_username_input.setText(mUser.username)
                edit_email_input.setText(mUser.email)
                profile_image.loadUserPhoto(mUser.photo)
            })
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == mCameraPictureTaker.REQUEST_CODE && resultCode == RESULT_OK) {

            // upload to firebase storage
            mFirebaseHelper.uploadUserPhoto(mCameraPictureTaker.imageUri!!) {
                mFirebaseHelper.getUrl().addOnCompleteListener {
                    val photoUrl = it.result.toString()
                    mFirebaseHelper.updateUserPhoto(photoUrl) {
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
            mFirebaseHelper.reauthenticate(credential) {//try to re-authentiate
                mFirebaseHelper.updateEmail(mPendingUser.email) {
                    updateUser(mPendingUser)
                }
            }
        } else {
            showToast("You should enter your password")
        }

    }

    private fun updateUser(user: User) {
        val updatesMap = mutableMapOf<String, Any?>()
        if (user.name != mUser.name) updatesMap["name"] = user.name
        if (user.username != mUser.username) updatesMap["username"] = user.username
        if (user.email != mUser.email) updatesMap["email"] = user.email

        mFirebaseHelper.updateUser(updatesMap) {
            showToast("Profile successfully updated")
            finish()
        }
    }

    private fun validate(user: User): String? =
        when {
            user.username.isEmpty() -> "Please enter username"
            user.email.isEmpty() -> "Please enter email"
            else -> null
        }
}

