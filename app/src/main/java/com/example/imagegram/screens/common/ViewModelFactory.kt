package com.example.imagegram.screens.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.imagegram.data.firebase.FirebaseFeedPostsRepository
import com.example.imagegram.data.firebase.FirebaseUsersRepository
import com.example.imagegram.screens.editprofile.EditProfileViewModel
import com.example.imagegram.screens.findusers.FindUsersViewModel
import com.google.android.gms.tasks.OnFailureListener

@Suppress("UNCHECKED_CAST")
class ViewModelFactory (private val onFailureListener: OnFailureListener): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FindUsersViewModel::class.java)) {
            return FindUsersViewModel(onFailureListener, FirebaseUsersRepository(), FirebaseFeedPostsRepository()) as T
        } else if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            return EditProfileViewModel(onFailureListener, FirebaseUsersRepository()) as T
        } else {
            error("unknown ViewModel class $modelClass")
        }
    }
}