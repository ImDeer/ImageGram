package com.example.imagegram.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.imagegram.R
import com.example.imagegram.models.User
import com.example.imagegram.utils.FirebaseHelper
import com.example.imagegram.utils.TaskSourceOnCompleteListener
import com.example.imagegram.utils.ValueEventListenerAdapter
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_find_users.*
import kotlinx.android.synthetic.main.find_users_item.view.*

class FindUsersActivity : AppCompatActivity(), UsersAdapter.Listener {

    private val TAG = "FindUsersActivity"
    private lateinit var mFirebase: FirebaseHelper
    private lateinit var mUser: User
    private lateinit var mUsers: List<User>
    private lateinit var mAdapter: UsersAdapter


    override fun onCreate(savedInstanceState: Bundle?) {//, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState)//, persistentState)
        setContentView(R.layout.activity_find_users)
        Log.d(TAG, "onCreate")

        mFirebase = FirebaseHelper(this)
        mAdapter = UsersAdapter(this)

        val uid = mFirebase.auth.currentUser!!.uid

        find_users_back_image.setOnClickListener { finish() }

        find_users_recycler.adapter = mAdapter
        find_users_recycler.layoutManager = LinearLayoutManager(this)

        mFirebase.database.child("users").addValueEventListener(ValueEventListenerAdapter {
            val allUsers = it.children.map { it.asUser()!! }
            val (userList, otherUsersList) = allUsers.partition { it.uid == uid }
            mUser = userList.first()
            mUsers = otherUsersList

            mAdapter.update(mUsers, mUser.follows)
        })
    }


    override fun follow(uid: String) {
        setFollow(uid, true) {
            mAdapter.followed(uid)
        }

    }

    override fun unfollow(uid: String) {
        setFollow(uid, false) {
            mAdapter.unfollowed(uid)

        }
    }

    private fun setFollow(uid: String, follow: Boolean, onSuccess: () -> Unit) {

        fun DatabaseReference.setValueTrueOrBoolean(value: Boolean) =
            if (value) setValue(true) else removeValue()


        val followsTask =
            mFirebase.database.child("users").child(mUser.uid).child("follows")
                .child(uid).setValueTrueOrBoolean(follow)

        val followersTask = mFirebase.database.child("users").child(uid).child("followers")
            .child(mUser.uid).setValueTrueOrBoolean(follow)


        val feedPostsTask = task<Void> { taskSource ->

            mFirebase.database.child("feed-posts").child(uid)
                .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                    val postsMap = if (follow) {
                        it.children.map { it.key to it.value }.toMap()
                    } else {
                        it.children.map { it.key to null }.toMap()
                    }
                    mFirebase.database.child("feed-posts").child(mUser.uid)
                        .updateChildren(postsMap)
                        .addOnCompleteListener(
                            TaskSourceOnCompleteListener(
                                taskSource
                            )
                        )
                })
        }

        Tasks.whenAll(followsTask, followersTask, feedPostsTask).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }
}

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