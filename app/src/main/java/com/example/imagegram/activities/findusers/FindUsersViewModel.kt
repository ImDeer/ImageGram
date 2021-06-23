package com.example.imagegram.activities.findusers

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.imagegram.activities.map
import com.example.imagegram.models.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

class FindUsersViewModel(private val repository: FindUsersRepository) : ViewModel() {

    // only let Activity work with LiveData
    // so that it could not change anything itself but could access it for reading

    val userAndFriends: LiveData<Pair<User, List<User>>> =
        repository.getUsers().map { allUsers ->
            val (userList, otherUsersList) = allUsers.partition {
                it.uid == repository.currentUid()
            }
            userList.first() to otherUsersList
        }

    fun setFollow(currentUid: String, uid: String, follow: Boolean): Task<Void> {
        return if (follow) {
            Tasks.whenAll(
                repository.addFollow(currentUid, uid),
                repository.addFollower(currentUid, uid),
                repository.copyFeedPosts(postsAuthorUid = uid, toUid = currentUid)
            )
        } else {
            Tasks.whenAll(
                repository.deleteFollow(currentUid, uid),
                repository.deleteFollower(currentUid, uid),
                repository.deleteFeedPosts(postsAuthorUid = uid, toUid = currentUid)
            )
        }


    }
}