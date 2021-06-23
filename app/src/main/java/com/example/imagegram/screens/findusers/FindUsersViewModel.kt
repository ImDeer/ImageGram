package com.example.imagegram.screens.findusers

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.imagegram.data.FeedPostsRepository
import com.example.imagegram.data.UsersRepository
import com.example.imagegram.data.common.map
import com.example.imagegram.models.User
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

class FindUsersViewModel(
    private val onFailureListener: OnFailureListener,
    private val usersRepo: UsersRepository,
    private val FeedPostsRepo: FeedPostsRepository
) : ViewModel() {

    // only let Activity work with LiveData
    // so that it could not change anything itself but could access it for reading
    val userAndFriends: LiveData<Pair<User, List<User>>> =
        usersRepo.getUsers().map { allUsers ->
            val (userList, otherUsersList) = allUsers.partition {
                it.uid == usersRepo.currentUid()
            }
            userList.first() to otherUsersList
        }

    fun setFollow(currentUid: String, uid: String, follow: Boolean): Task<Void> {
        return if (follow) {
            Tasks.whenAll(
                usersRepo.addFollow(currentUid, uid),
                usersRepo.addFollower(currentUid, uid),
                FeedPostsRepo.copyFeedPosts(postsAuthorUid = uid, toUid = currentUid)
            )
        } else {
            Tasks.whenAll(
                usersRepo.deleteFollow(currentUid, uid),
                usersRepo.deleteFollower(currentUid, uid),
                FeedPostsRepo.deleteFeedPosts(postsAuthorUid = uid, toUid = currentUid)
            )
        }.addOnFailureListener(onFailureListener)


    }
}