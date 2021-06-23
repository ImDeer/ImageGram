package com.example.imagegram.screens.editprofile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.example.imagegram.R
import com.example.imagegram.models.User
import com.example.imagegram.screens.common.*
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : BaseActivity(), PasswordDialog.Listener {
    private lateinit var mViewModel: EditProfileViewModel
    private lateinit var mUser: User
    private lateinit var mPendingUser: User
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


        mViewModel = initViewModel()
//            ViewModelProvider(this, ViewModelFactory()).get(EditProfileViewModel::class.java)

        mViewModel.user.observe(this, Observer {
            it?.let {
                mUser = it
                edit_name_input.setText(mUser.name)
                edit_username_input.setText(mUser.username)
                edit_email_input.setText(mUser.email)
                profile_image.loadUserPhoto(mUser.photo)

            }
        })
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == mCamera.REQUEST_CODE && resultCode == RESULT_OK) {
            mViewModel.uploadAndSetUserPhoto(mCamera.imageUri!!)
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
                PasswordDialog()
                    .show(supportFragmentManager, "pass_dialog")
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
            mViewModel.updateEmail(
                currentEmail = mUser.email,
                newEmail = mPendingUser.email,
                password = password
            )
                .addOnSuccessListener { updateUser(mPendingUser) }
        } else {
            showToast(getString(R.string.pls_enter_your_password))
        }

    }

    private fun updateUser(user: User) {
        mViewModel.updateUserProfile(currentUser = mUser, newUser = user)
            .addOnSuccessListener {
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

    companion object {
        const val TAG = "EditProfileActivity"
    }
}

