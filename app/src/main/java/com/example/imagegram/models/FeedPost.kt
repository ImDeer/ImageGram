package com.example.imagegram.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.ServerValue
import java.util.*

data class FeedPost(
    val uid: String = "",
    val username: String = "",
    val image: String = "",
    val caption: String = "",
    val comments: List<Comment> = emptyList(),
    val timestamp: Any = ServerValue.TIMESTAMP,
    val photo: String? = null,
    @Exclude val commentsCount: Int = 0,
    @Exclude val id:String = ""
) {
    // at save firebase saves timestamp as Long; at get we get saved Long
    fun timestampDate(): Date =
        Date(timestamp as Long)
}