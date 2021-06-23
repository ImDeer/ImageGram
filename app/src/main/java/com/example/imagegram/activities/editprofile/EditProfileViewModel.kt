package com.example.imagegram.activities.editprofile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.imagegram.data.UsersRepository
import com.example.imagegram.models.User
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task

class EditProfileViewModel(
    private val onFailureListener: OnFailureListener,
    private val usersRepo: UsersRepository
) : ViewModel() {
    //    val _user:MutableLiveData<List<User>> = repository.getUser()
    val user: LiveData<User> = usersRepo.getUser()

    fun uploadAndSetUserPhoto(localImage: Uri): Task<Unit> =
        usersRepo.uploadUserPhoto(localImage).onSuccessTask { downloadUrl ->
            usersRepo.updateUserPhoto(downloadUrl!!)
        }
            .addOnFailureListener(onFailureListener)

    fun updateEmail(currentEmail: String, newEmail: String, password: String): Task<Unit> =
        usersRepo.updateEmail(
            currentEmail = currentEmail,
            newEmail = newEmail,
            password = password
        ).addOnFailureListener(onFailureListener)


    fun updateUserProfile(currentUser: User, newUser: User): Task<Unit> =
        usersRepo.updateUserProfile(currentUser = currentUser, newUser = newUser)
            .addOnFailureListener(onFailureListener)


}