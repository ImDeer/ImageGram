package com.example.imagegram.screens.findusers

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.imagegram.R
import com.example.imagegram.models.User
import com.example.imagegram.screens.common.BaseActivity
import kotlinx.android.synthetic.main.activity_find_users.*

//@Suppress("DEPRECATION")
class FindUsersActivity : BaseActivity(),
    UsersAdapter.Listener {

    private lateinit var mUser: User
    private lateinit var mUsers: List<User>
    private lateinit var mAdapter: UsersAdapter
    private lateinit var mViewModel: FindUsersViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_users)

        Log.e(TAG, "onCreate")

        mAdapter = UsersAdapter(this)

        mViewModel = initViewModel()

        find_users_back_image.setOnClickListener { finish() }

        find_users_recycler.adapter = mAdapter
        find_users_recycler.layoutManager = LinearLayoutManager(this)

        mViewModel.userAndFriends.observe(this, Observer {
            it?.let { (user, otherUsers) ->
                mUser = user
                mUsers = otherUsers

                mAdapter.update(mUsers, mUser.follows)
                Log.e(TAG, "userAndFriends")
            }
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
        mViewModel.setFollow(mUser.uid, uid, follow)
            .addOnSuccessListener { onSuccess() }
    }

    companion object {
        const val TAG = "FindUsersActivity"
    }
}