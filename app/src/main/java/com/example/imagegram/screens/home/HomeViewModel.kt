package com.example.imagegram.screens.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.imagegram.models.FeedPost
import com.example.imagegram.data.FeedPostsRepository
import com.google.android.gms.tasks.OnFailureListener

class HomeViewModel(
    private val onFailureListener: OnFailureListener,
    private val feedPostsRepo: FeedPostsRepository
) : ViewModel() {
    lateinit var uid: String
    lateinit var feedPosts: LiveData<List<FeedPost>>
    private var loadedLikes = mapOf<String, LiveData<FeedPostLikes>>()

    fun init(uid: String) {
        this.uid = uid
        feedPosts = feedPostsRepo.getFeedPosts(uid).map {
            it.sortedByDescending { it.timestampDate() }
        }
    }

    fun toggleLike(postId: String) {
        feedPostsRepo.toggleLike(postId, uid).addOnFailureListener(onFailureListener)
    }

    fun getLikes(postId: String): LiveData<FeedPostLikes>? = loadedLikes[postId]

    fun loadLikes(postId: String): LiveData<FeedPostLikes> {
        val existingLoadedLikes = loadedLikes[postId]
        if (existingLoadedLikes == null) {

            val liveData = feedPostsRepo.getLikes(postId).map { likes ->
                FeedPostLikes(
                    likesCount = likes.size,
                    likedByUser = likes.find { it.userId == uid } != null
                )
            }
            loadedLikes += postId to liveData
            return liveData
        } else {
            return existingLoadedLikes
        }
    }
}