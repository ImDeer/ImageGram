package com.example.imagegram.data.firebase

import com.example.imagegram.utils.toUnit
import com.example.imagegram.activities.task
import com.example.imagegram.data.FeedPostsRepository
import com.example.imagegram.utils.TaskSourceOnCompleteListener
import com.example.imagegram.utils.ValueEventListenerAdapter
import com.example.imagegram.utils.database
import com.google.android.gms.tasks.Task

class FirebaseFeedPostsRepository :
    FeedPostsRepository {
//    private val _users = database.child("users").liveData().map {
//        it.children.map { it.asUser()!! }
//    }
//    _users
//        FirebaseLiveData(reference.child("users")).map {
//            it.children.map { it.asUser()!! }
//        }


    override fun copyFeedPosts(postsAuthorUid: String, toUid: String): Task<Unit> =
        task { taskSource ->
            database.child("feed-posts")
                .child(postsAuthorUid)
                .orderByChild("uid")
                .equalTo(postsAuthorUid)
                .addListenerForSingleValueEvent(ValueEventListenerAdapter {

                    val postsMap = it.children.map { it.key to it.value }.toMap()
                    database.child("feed-posts")
                        .child(toUid)
                        .updateChildren(postsMap)
                        .toUnit()
                        .addOnCompleteListener(
                            TaskSourceOnCompleteListener(
                                taskSource
                            )
                        )
                })
        }


    override fun deleteFeedPosts(postsAuthorUid: String, toUid: String): Task<Unit> =
        task { taskSource ->
            database.child("feed-posts").child(toUid)
                .orderByChild("uid")
                .equalTo(postsAuthorUid)
                .addListenerForSingleValueEvent(ValueEventListenerAdapter {

                    val postsMap = it.children.map { it.key to null }.toMap()
                    database.child("feed-posts")
                        .child(toUid)
                        .updateChildren(postsMap)
                        .toUnit()
                        .addOnCompleteListener(
                            TaskSourceOnCompleteListener(
                                taskSource
                            )
                        )
                })
        }
}