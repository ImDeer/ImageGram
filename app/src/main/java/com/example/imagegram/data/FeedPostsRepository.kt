package com.example.imagegram.data

import com.google.android.gms.tasks.Task

interface FeedPostsRepository {
    fun copyFeedPosts(postsAuthorUid: String, toUid: String): Task<Unit>
    fun deleteFeedPosts(postsAuthorUid: String, toUid: String): Task<Unit>
}