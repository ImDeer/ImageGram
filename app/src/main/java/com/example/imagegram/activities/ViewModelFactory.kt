package com.example.imagegram.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.imagegram.activities.editprofile.EditProfileViewModel
import com.example.imagegram.activities.editprofile.FirebaseEditProfileRepository
import com.example.imagegram.activities.findusers.FindUsersViewModel
import com.example.imagegram.activities.findusers.FirebaseFindUsersRepository

@Suppress("UNCHECKED_CAST")
class ViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FindUsersViewModel::class.java)) {
            return FindUsersViewModel(FirebaseFindUsersRepository()) as T
        } else if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            return EditProfileViewModel(FirebaseEditProfileRepository()) as T
        } else {
            error("unknown ViewModel class $modelClass")
        }
    }
}