package com.example.imagegram.screens.findusers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.imagegram.R
import com.example.imagegram.models.User
import com.example.imagegram.screens.common.loadUserPhoto
import kotlinx.android.synthetic.main.find_users_item.view.*

class UsersAdapter(private val listener: Listener) :
    RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    interface Listener {
        fun follow(uid: String)
        fun unfollow(uid: String)
    }

    private var mUsers = listOf<User>()
    private var mFollows = mapOf<String, Boolean>()
    private var mPositions = mapOf<String, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.find_users_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.view) {
            val user = mUsers[position]
            user_item_image.loadUserPhoto(user.photo)
            user_item_username.text = user.username
            user_item_name.text = user.name

            user_item_follow_bt.setOnClickListener { listener.follow(user.uid) }
            user_item_unfollow_bt.setOnClickListener { listener.unfollow(user.uid) }

            val follows = mFollows[user.uid] ?: false
            if (follows) {
                user_item_follow_bt.visibility = View.GONE
                user_item_unfollow_bt.visibility = View.VISIBLE
            } else {
                user_item_follow_bt.visibility = View.VISIBLE
                user_item_unfollow_bt.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = mUsers.size

    fun update(users: List<User>, follows: Map<String, Boolean>) {
        mUsers = users
        mPositions = users.withIndex().map { (idx, user) -> user.uid to idx }.toMap()
        mFollows = follows
        notifyDataSetChanged()
    }

    fun followed(uid: String) {
        mFollows = mFollows + (uid to true)
        notifyItemChanged(mPositions[uid]!!)
    }

    fun unfollowed(uid: String) {
        mFollows = mFollows - uid
        notifyItemChanged(mPositions[uid]!!)
    }
}