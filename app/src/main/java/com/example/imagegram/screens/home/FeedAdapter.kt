package com.example.imagegram.screens.home

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.imagegram.R
import com.example.imagegram.models.FeedPost
import com.example.imagegram.screens.common.SimpleCallback
import com.example.imagegram.screens.common.loadImage
import com.example.imagegram.screens.common.loadUserPhoto
import com.example.imagegram.screens.common.showToast
import kotlinx.android.synthetic.main.feed_item.view.*

class FeedAdapter(private val listener: Listener) :
    RecyclerView.Adapter<FeedAdapter.ViewHolder>() {

    interface Listener {
        fun toggleLike(postId: String)
        fun loadLikes(postId: String, position: Int)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    private var posts = listOf<FeedPost>()
    private var postLikes: Map<Int, FeedPostLikes> = emptyMap()

    private val defaultPostLikes =
        FeedPostLikes(0, false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.feed_item, parent, false)
        return ViewHolder(view)
    }

    fun updatePostLikes(position: Int, likes: FeedPostLikes) {
        postLikes += (position to likes)
        notifyItemChanged(position)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        val likes = postLikes[position] ?: defaultPostLikes

        with(holder.view) {
            feed_profile_image.loadUserPhoto(post.photo)
            if (likes.likesCount == 0) {
                feed_likes_text.visibility = View.GONE
            } else {
                feed_likes_text.visibility = View.VISIBLE
                val likesCountText = holder.view.context.resources.getQuantityString(
                    R.plurals.likes_count,
                    likes.likesCount,
                    likes.likesCount
                )
                feed_likes_text.text = likesCountText

            }
            feed_caption_text.setCaptionText(post.username, post.caption)
            feed_post_image.loadImage(post.image)

            feed_like_image.setOnClickListener { listener.toggleLike(post.id) }
            feed_like_image.setImageResource(if (likes.likedByUser) R.drawable.ic_like_active else R.drawable.ic_like_border)

            listener.loadLikes(post.id, position)
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
                widget.context.showToast(context.getString(R.string.username_is_clicked))
            }

            override fun updateDrawState(ds: TextPaint) {}
        }, 0, usernameSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        text =
            SpannableStringBuilder().append(usernameSpannable).append(" ").append(caption)
        movementMethod = LinkMovementMethod.getInstance()
    }

    override fun getItemCount(): Int = posts.size
    fun updatePosts(newPosts: List<FeedPost>) {
        val diffResult = DiffUtil.calculateDiff(
            SimpleCallback(
                this.posts,
                newPosts,
                { it.id })
        )
        this.posts = newPosts
        diffResult.dispatchUpdatesTo(this)
    }
}

