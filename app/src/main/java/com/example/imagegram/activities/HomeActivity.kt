package com.example.imagegram.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.imagegram.R
import com.example.imagegram.models.User
import com.example.imagegram.utils.FirebaseHelper
import com.example.imagegram.utils.GlideApp
import com.example.imagegram.utils.ValueEventListenerAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.view.*
import kotlinx.android.synthetic.main.feed_item.view.*

class HomeActivity : BaseActivity(0) {
    private val TAG = "HomeActivity"
    private lateinit var mFirebase: FirebaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        Log.d(TAG, "onCreate")
        setupBottomNavigation()

        mFirebase = FirebaseHelper(this)

        mFirebase.auth.addAuthStateListener {
            if (it.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        search_bt.setOnClickListener{
            val intent = Intent(this, FindUsersActivity::class.java)
            this.startActivity(intent)
            Log.e(TAG, "run StartActivity for FindUsersActivity")
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        val currentUser = mFirebase.auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
        } else {
            mFirebase.currentUserReference().addValueEventListener(ValueEventListenerAdapter {
                val user = it.asUser()!!
                feed_toolbar_username.text = "@${user.username}!"
            })

            mFirebase.database.child("feed-posts").child(currentUser.uid)
                .addValueEventListener(ValueEventListenerAdapter {
                    val posts = it.children.map { it.getValue(FeedPost::class.java)!! }
                        .sortedByDescending { it.timestampDate() }
                    feed_recycler.adapter = FeedAdapter(posts)
                    feed_recycler.layoutManager = LinearLayoutManager(this)
                })

        }
    }
}

class FeedAdapter(private val posts: List<FeedPost>) :
    RecyclerView.Adapter<FeedAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]

        with(holder.view) {
            feed_profile_image.loadUserPhoto(post.photo)
            if (post.likesCount == 0) {
                feed_likes_text.visibility = View.GONE
            } else {
                feed_likes_text.visibility = View.VISIBLE
                if (post.likesCount == 1) {
                    feed_likes_text.text = "${post.likesCount} like"
                } else {
                    feed_likes_text.text = "${post.likesCount} likes"
                }

            }
            feed_caption_text.setCaptionText(post.username, post.caption)
            feed_post_image.loadImage(post.image)

        }
    }

    private fun TextView.setCaptionText(username: String, caption: String) {
        val usernameSpannable = SpannableString(username)
        usernameSpannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            usernameSpannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        usernameSpannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                widget.context.showToast("username ${username} is clicked")
            }

            override fun updateDrawState(ds: TextPaint) {}
        }, 0, usernameSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        text =
            SpannableStringBuilder().append(usernameSpannable).append(" ").append(caption)
        movementMethod = LinkMovementMethod.getInstance()
    }

    override fun getItemCount(): Int = posts.size
}
