package com.example.imagegram.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import com.example.imagegram.R
import com.example.imagegram.models.User
import com.example.imagegram.views.PasswordDialog
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {
    private val TAG = "EditProfileActivity"
    private lateinit var mUser: User
    private lateinit var mPendingUser: User
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mStorage: StorageReference
    private lateinit var mImageUri: Uri
    private val TAKE_PICTURE_REQUEST_CODE = 1
    val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.d(TAG, "onCreate")
        back_image.setOnClickListener { finish() }
        save_bt.setOnClickListener { updateProfile() }
        edit_profile_image_btn.setOnClickListener { changePhoto() }


        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        mStorage = FirebaseStorage.getInstance().reference

        mDatabase.child("users").child(mAuth.currentUser!!.uid)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                mUser = it.getValue(User::class.java)!!
                edit_name_input.setText(mUser.name, TextView.BufferType.EDITABLE)
                edit_username_input.setText(mUser.username, TextView.BufferType.EDITABLE)
                edit_email_input.setText(mUser.email, TextView.BufferType.EDITABLE)
                profile_image.loadUserPhoto(mUser.photo)
            })
    }


    private fun changePhoto() {
        // open camera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val imageFile = createImageFile()
            mImageUri = FileProvider.getUriForFile(
                this,
                "com.example.imagegram.fileprovider",
                imageFile
            )
            intent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                mImageUri
            ) // please Mr Intent put camera output to the passed uri
            startActivityForResult(
                intent,
                TAKE_PICTURE_REQUEST_CODE
            ) // request code=1 means successfull end of operation
        }
        // get photo
        // save to  firebase

    }

    private fun createImageFile(): File {
        // Create an image file name
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${simpleDateFormat.format(Date())}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAKE_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            val uid = mAuth.currentUser!!.uid
            // upload to firebase storage
            val ref = mStorage.child("users/$uid/photo")
            ref.putFile(mImageUri).addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener {
                        val photoUrl = it.result.toString()
                        mDatabase.child("users/$uid/photo").setValue(photoUrl)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    //обновляем наш USERS
                                    mUser = mUser.copy(photo = photoUrl)
                                    profile_image.loadUserPhoto(mUser.photo)
                                } else {
                                    showToast(it.exception!!.message!!)
                                }
                            }
                    }

                } else {
                    showToast(it.exception!!.message!!)
                }
            }
            // save to user.photo
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
            mAuth.currentUser!!.reauthenticate(credential) {//try to re-authentiate
                mAuth.currentUser!!.updateEmail(mPendingUser.email) {
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

        mDatabase.updateUser(mAuth.currentUser!!.uid, updatesMap) {
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

    private fun DatabaseReference.updateUser(
        uid: String, updatesMap: Map<String, Any?>,
        onSuccess: () -> Unit
    ) {
        child("users").child(uid).updateChildren(updatesMap)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    showToast(it.exception!!.message!!)
                }
            }

    }

    private fun FirebaseUser.updateEmail(email: String, onSuccess: () -> Unit) {
        updateEmail(email).addOnCompleteListener {
            if (it.isSuccessful) { // re-authenticated
                onSuccess()
            } else { // fail to re-authenticate
                showToast(it.exception!!.message!!)
            }
        }
    }

    private fun FirebaseUser.reauthenticate(credential: AuthCredential, onSuccess: () -> Unit) {
        reauthenticate(credential).addOnCompleteListener {//try to re-authentiate
            if (it.isSuccessful) { // re-authenticated
                onSuccess()
            } else { // fail to re-authenticate
                showToast(it.exception!!.message!!)
            }
        }
    }
}

