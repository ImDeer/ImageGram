package com.example.imagegram.utils

import android.app.Activity
import android.net.Uri
import com.example.imagegram.activities.showToast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

class FirebaseHelper(private val activity: Activity) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    val storage: StorageReference = FirebaseStorage.getInstance().reference

    fun updateUser(updatesMap: Map<String, Any?>, onSuccess: () -> Unit) {
        database.child("users").child(auth.currentUser!!.uid).updateChildren(updatesMap)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    activity.showToast(it.exception!!.message!!)
                }
            }

    }

    fun updateEmail(email: String, onSuccess: () -> Unit) {
        auth.currentUser!!.updateEmail(email).addOnCompleteListener {
            if (it.isSuccessful) { // re-authenticated
                onSuccess()
            } else { // fail to re-authenticate
                activity.showToast(it.exception!!.message!!)
            }
        }
    }

    fun reauthenticate(credential: AuthCredential, onSuccess: () -> Unit) {
        auth.currentUser!!.reauthenticate(credential)
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
        database.child("users/${auth.currentUser!!.uid}/photo").setValue(photoUrl)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    //обновляем наш USERS
                    onSuccess()
                } else {
                    activity.showToast(it.exception!!.message!!)
                }
            }
    }

    fun getUrl() = storage.child("users/${auth.currentUser!!.uid}/photo").downloadUrl

    fun uploadUserPhoto(
        photo: Uri,
        onSuccess: (UploadTask.TaskSnapshot?) -> Unit
    ) {
        val ref = storage.child("users/${auth.currentUser!!.uid}/photo")
        ref.putFile(photo).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess(it.result)
            } else {
                activity.showToast(it.exception!!.message!!)
            }
        }
    }

    fun currentUserReference(): DatabaseReference =
        database.child("users").child(auth.currentUser!!.uid)

    fun uploadSharePhoto(localPhotoUrl: Uri, onSuccess: (UploadTask.TaskSnapshot) -> Unit) =
        storage.child("users/${auth.currentUser!!.uid}").child("images")
            .child(localPhotoUrl.lastPathSegment!!)
            .putFile(localPhotoUrl)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    onSuccess(it.result!!)
                else
                    activity.showToast(it.exception!!.message!!)
            }

    fun addSharePhoto(globalPhotoUrl: String, onSuccess: () -> Unit) =
        database.child("images").child(auth.currentUser!!.uid)
            .push().setValue(globalPhotoUrl)
            .addOnComplete { onSuccess() }

    private fun Task<Void>.addOnComplete(onSuccess: () -> Unit) {
        addOnCompleteListener {
            if (it.isSuccessful)
                onSuccess()
            else
                activity.showToast(it.exception!!.message!!)
        }
    }

}