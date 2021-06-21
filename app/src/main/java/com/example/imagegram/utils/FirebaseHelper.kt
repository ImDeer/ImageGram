package com.example.imagegram.utils

import android.app.Activity
import android.net.Uri
import com.example.imagegram.activities.showToast
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

class FirebaseHelper(private val activity: Activity) {
    private val mAuth: FirebaseAuth =
        FirebaseAuth.getInstance()
    private val mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val mStorage: StorageReference = FirebaseStorage.getInstance().reference

    fun updateUser(updatesMap: Map<String, Any?>, onSuccess: () -> Unit) {
        mDatabase.child("users").child(mAuth.currentUser!!.uid).updateChildren(updatesMap)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    activity.showToast(it.exception!!.message!!)
                }
            }

    }

    fun updateEmail(email: String, onSuccess: () -> Unit) {
        mAuth.currentUser!!.updateEmail(email).addOnCompleteListener {
            if (it.isSuccessful) { // re-authenticated
                onSuccess()
            } else { // fail to re-authenticate
                activity.showToast(it.exception!!.message!!)
            }
        }
    }

    fun reauthenticate(credential: AuthCredential, onSuccess: () -> Unit) {
        mAuth.currentUser!!.reauthenticate(credential)
            .addOnCompleteListener {//try to re-authentiate
                if (it.isSuccessful) { // re-authenticated
                    onSuccess()
                } else { // fail to re-authenticate
                    activity.showToast(it.exception!!.message!!)
                }
            }
    }

    fun updateUserPhoto(
        photoUrl: String,
        onSuccess: () -> Unit
    ) {
        mDatabase.child("users/${mAuth.currentUser!!.uid}/photo").setValue(photoUrl)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    //обновляем наш USERS
                    onSuccess()
                } else {
                    activity.showToast(it.exception!!.message!!)
                }
            }
    }

    fun getUrl() = mStorage.child("users/${mAuth.currentUser!!.uid}/photo").downloadUrl

    fun uploadUserPhoto(
        photo: Uri,
        onSuccess: (UploadTask.TaskSnapshot?) -> Unit
    ) {
        val ref = mStorage.child("users/${mAuth.currentUser!!.uid}/photo")
        ref.putFile(photo).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess(it.result)
            } else {
                activity.showToast(it.exception!!.message!!)
            }
        }
    }

    fun currentUserReference(): DatabaseReference =
        mDatabase.child("users").child(mAuth.currentUser!!.uid)

}