package com.example.imagegram.data

import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.imagegram.models.User
import com.google.android.gms.tasks.Task

interface UsersRepository {
    fun getUsers(): LiveData<List<User>>
    fun currentUid(): String?
    fun addFollow(fromUid: String, toUid: String): Task<Unit>
    fun deleteFollow(fromUid: String, toUid: String): Task<Unit>
    fun addFollower(fromUid: String, toUid: String): Task<Unit>
    fun deleteFollower(fromUid: String, toUid: String): Task<Unit>

    fun getUser(): LiveData<User>
    fun uploadUserPhoto(localImage: Uri): Task<Uri>
    fun updateUserPhoto(downloadUrl: Uri): Task<Unit>
    fun updateEmail(currentEmail: String, newEmail: String, password: String): Task<Unit>
    fun updateUserProfile(currentUser: User, newUser: User): Task<Unit>
}