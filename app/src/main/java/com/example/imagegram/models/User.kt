package com.example.imagegram.models

import com.google.firebase.database.Exclude

data class User(
    val username: String = "",
    val email: String = "",
    val follows: Map<String, Boolean> = emptyMap(),
    val followers: Map<String, Boolean> = emptyMap(),
    val name: String? = null,
    val photo: String? = null,
    @Exclude val uid: String = ""
)