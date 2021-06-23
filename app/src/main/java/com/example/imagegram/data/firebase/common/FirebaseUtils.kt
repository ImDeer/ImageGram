package com.example.imagegram.data.firebase.common

import androidx.lifecycle.LiveData
import com.example.imagegram.models.FeedPost
import com.example.imagegram.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

fun DataSnapshot.asUser(): User? = getValue(
    User::class.java
)?.copy(uid = key!!)

fun DatabaseReference.setValueTrueOrRemove(value: Boolean): Task<Void> =
    if (value) setValue(true) else removeValue()

fun DataSnapshot.asFeedPost(): FeedPost? = getValue(
    FeedPost::class.java
)?.copy(id = key!!)

val auth: FirebaseAuth = FirebaseAuth.getInstance()
val database: DatabaseReference = FirebaseDatabase.getInstance().reference
val storage: StorageReference = FirebaseStorage.getInstance().reference
fun DatabaseReference.liveData(): LiveData<DataSnapshot> =
    FirebaseLiveData(this)