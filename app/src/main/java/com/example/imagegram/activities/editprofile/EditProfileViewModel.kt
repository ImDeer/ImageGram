package com.example.imagegram.activities.editprofile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.imagegram.models.User
import com.google.android.gms.tasks.Task

class EditProfileViewModel(private val repository: EditProfileRepository) : ViewModel() {
    //    val _user:MutableLiveData<List<User>> = repository.getUser()
    val user: LiveData<User> = repository.getUser()

    fun uploadAndSetUserPhoto(localImage: Uri): Task<Unit> =
        repository.uploadUserPhoto(localImage).onSuccessTask { downloadUrl ->
            repository.updateUserPhoto(downloadUrl!!)


        }

    fun updateEmail(currentEmail: String, newEmail: String, password: String): Task<Unit> =
        repository.updateEmail(
            currentEmail = currentEmail,
            newEmail = newEmail,
            password = password
        )

    fun updateUserProfile(currentUser: User, newUser: User): Task<Unit> =
        repository.updateUserProfile(currentUser = currentUser, newUser = newUser)


}