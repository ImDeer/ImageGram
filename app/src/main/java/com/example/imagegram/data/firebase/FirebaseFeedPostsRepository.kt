package com.example.imagegram.data.firebase

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.imagegram.common.TaskSourceOnCompleteListener
import com.example.imagegram.common.ValueEventListenerAdapter
import com.example.imagegram.common.task
import com.example.imagegram.common.toUnit
import com.example.imagegram.data.FeedPostLike
import com.example.imagegram.data.FeedPostsRepository
import com.example.imagegram.data.firebase.common.FirebaseLiveData
import com.example.imagegram.data.firebase.common.asFeedPost
import com.example.imagegram.data.firebase.common.database
import com.example.imagegram.data.firebase.common.setValueTrueOrRemove
import com.example.imagegram.models.FeedPost
import com.google.android.gms.tasks.Task

class FirebaseFeedPostsRepository :
    FeedPostsRepository {


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

    override fun getFeedPosts(uid: String): LiveData<List<FeedPost>> =
        FirebaseLiveData(database.child("feed-posts").child(uid)).map {
            it.children.map { it.asFeedPost()!! }
        }

    override fun toggleLike(postId: String, uid: String): Task<Unit> {
        val reference = database.child("likes").child(postId).child(uid)
        return task { taskSource ->
            reference.addListenerForSingleValueEvent(ValueEventListenerAdapter {
                reference.setValueTrueOrRemove(!it.exists())
                taskSource.setResult(Unit)
            })

        }
    }

    override fun getLikes(postId: String): LiveData<List<FeedPostLike>> =
        FirebaseLiveData(database.child("likes").child(postId)).map {
            it.children.map { FeedPostLike(it.key!!) }

    }
}