package com.example.imagegram.data.firebase

//import androidx.lifecycle.map
//import com.example.imagegram.activities.map
import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.imagegram.common.task
import com.example.imagegram.common.toUnit
import com.example.imagegram.data.UsersRepository
import com.example.imagegram.data.common.map
import com.example.imagegram.data.firebase.common.*
import com.example.imagegram.models.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class FirebaseUsersRepository :
    UsersRepository {
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

    override fun getUsers(): LiveData<List<User>> = database.child("users").liveData().map {
        it.children.map { it.asUser()!! }
    }

    override fun addFollow(fromUid: String, toUid: String): Task<Unit> =
        getFollowsRef(fromUid, toUid).setValue(true).toUnit()

    override fun deleteFollow(fromUid: String, toUid: String): Task<Unit> =
        getFollowsRef(fromUid, toUid).removeValue().toUnit()

    override fun addFollower(fromUid: String, toUid: String): Task<Unit> =
        getFollowersRef(fromUid, toUid).setValue(true).toUnit()

    override fun deleteFollower(fromUid: String, toUid: String): Task<Unit> =
        getFollowersRef(fromUid, toUid).removeValue().toUnit()

    private fun getFollowsRef(fromUid: String, toUid: String) =
        database.child("users").child(fromUid).child("follows").child(toUid)

    private fun getFollowersRef(fromUid: String, toUid: String) =
        database.child("users").child(toUid).child("followers")
            .child(fromUid)

    override fun currentUid() = FirebaseAuth.getInstance().currentUser?.uid


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