package com.example.imagegram.activities.editprofile

import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.imagegram.activities.findusers.toUnit
import com.example.imagegram.activities.asUser
import com.example.imagegram.activities.map
import com.example.imagegram.activities.task
import com.example.imagegram.models.User
import com.example.imagegram.utils.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.EmailAuthProvider
import java.lang.IllegalStateException

interface EditProfileRepository {
    fun getUser(): LiveData<User>
    fun uploadUserPhoto(localImage: Uri): Task<Uri>
    fun updateUserPhoto(downloadUrl: Uri): Task<Unit>
    fun updateEmail(currentEmail: String, newEmail: String, password: String): Task<Unit>
    fun updateUserProfile(currentUser: User, newUser: User): Task<Unit>
}

class FirebaseEditProfileRepository : EditProfileRepository {
    override fun uploadUserPhoto(localImage: Uri): Task<Uri> =
        task { taskSource ->
            storage.child("users/${currentUid()!!}/photo").putFile(localImage)
                .addOnSuccessListener {
                    getUrl().addOnCompleteListener {
                        taskSource.setResult(it.result!!)
                    }
//                    Tasks.forResult(getUrl().result!!) //it?.downloadUrl!!)
                }
        }

    private fun getUrl() = storage.child("users/${currentUid()!!}/photo").downloadUrl

    override fun updateUserPhoto(downloadUrl: Uri): Task<Unit> =
        database.child("users/${currentUid()!!}/photo").setValue(downloadUrl.toString()).toUnit()

    override fun updateEmail(currentEmail: String, newEmail: String, password: String): Task<Unit> {
        val credential = EmailAuthProvider.getCredential(currentEmail, password) // get credential
        val currentUser = auth.currentUser
        return if (currentUser != null) {

            currentUser.reauthenticate(credential).onSuccessTask {//try to re-authentiate
                currentUser.updateEmail(newEmail)
            }.toUnit()
        } else {
            Tasks.forException(IllegalStateException("User is not authenticated"))
        }

    }

    override fun updateUserProfile(currentUser: User, newUser: User): Task<Unit> {
        val updatesMap = mutableMapOf<String, Any?>()
        if (newUser.name != currentUser.name) updatesMap["name"] = newUser.name
        if (newUser.username != currentUser.username) updatesMap["username"] = newUser.username
        if (newUser.email != currentUser.email) updatesMap["email"] = newUser.email

        return database.child("users").child(currentUid()!!).updateChildren(updatesMap).toUnit()

    }


    override fun getUser(): LiveData<User> =
        database.child("users").child(currentUid()!!).liveData().map {
            it.asUser()!!
        }


}