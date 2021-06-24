package com.example.imagegram.data

import androidx.lifecycle.LiveData
import com.example.imagegram.models.FeedPost
import com.google.android.gms.tasks.Task

interface FeedPostsRepository {
    fun copyFeedPosts(postsAuthorUid: String, toUid: String): Task<Unit>
    fun deleteFeedPosts(postsAuthorUid: String, toUid: String): Task<Unit>
    fun getFeedPosts(uid: String): LiveData<List<FeedPost>>
    fun toggleLike(postId: String, uid: String):Task<Unit>
    fun getLikes(postId: String):LiveData<List<FeedPostLike>>
}

data class FeedPostLike(val userId : String)