package com.example.imagegram.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.imagegram.R
import com.example.imagegram.models.User
import com.example.imagegram.utils.CameraHelper
import com.example.imagegram.utils.FirebaseHelper
import com.example.imagegram.utils.GlideApp
import com.example.imagegram.utils.ValueEventListenerAdapter
import com.google.android.gms.tasks.Task
import com.google.firebase.database.ServerValue
import kotlinx.android.synthetic.main.activity_add.*
import java.util.*

class AddActivity : BaseActivity(1) {
    private val TAG = "AddActivity"
    private lateinit var mCamera: CameraHelper
    private lateinit var mFirebase: FirebaseHelper
    private lateinit var mUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        Log.d(TAG, "onCreate")
//        setupBottomNavigation()

        mFirebase = FirebaseHelper(this)
        mCamera = CameraHelper(this)
        mCamera.takeCameraPicture()

        add_back.setOnClickListener { finish() }
        add_bt.setOnClickListener { share() }

        mFirebase.currentUserReference().addValueEventListener(ValueEventListenerAdapter {
            mUser = it.asUser()!!
        })

    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == mCamera.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                GlideApp.with(this).load(mCamera.imageUri)
                    .transform(CenterCrop(), RoundedCorners(40)).into(add_image)
            } else {
                finish()
            }
        }
    }

    private fun share() {
        val imageUri = mCamera.imageUri
        if (imageUri != null) {
            val uid = mFirebase.auth.currentUser!!.uid
            mFirebase.uploadSharePhoto(imageUri) {
                val imageDownloadUrl = it.metadata!!.reference!!.downloadUrl
                imageDownloadUrl.addOnSuccessListener {
                    mFirebase.addSharePhoto(it.toString()) {
                        mFirebase.database.child("feed-posts").child(uid).push()
                            .setValue(
                                mkFeedPost(uid, imageDownloadUrl)
                            ).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    startActivity(Intent(this, ProfileActivity::class.java))
                                    finish()
                                }
                            }
                    }
                }
            }
        }
    }

    private fun mkFeedPost(
        uid: String,
        imageDownloadUrl: Task<Uri>
    ): FeedPost {
        return FeedPost(
            uid = uid,
            username = mUser.username,
            image = imageDownloadUrl.result.toString(),
            caption = add_caption_input.text.toString(),
            photo = mUser.photo
        )
    }
}

data class FeedPost(
    val uid: String = "",
    val username: String = "",
    val image: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val caption: String = "",
    val comments: List<Comment> = emptyList(),
    val timestamp: Any = ServerValue.TIMESTAMP,
    val photo: String? = null
) {
    // at save firebase saves timestamp as Long; at get we get saved Long
    fun timestampDate(): Date = Date(timestamp as Long)
}

data class Comment(val uid: String, val username: String, val text: String)