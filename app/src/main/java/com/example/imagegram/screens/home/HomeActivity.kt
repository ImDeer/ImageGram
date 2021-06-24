package com.example.imagegram.screens.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.imagegram.R
import com.example.imagegram.screens.common.BaseActivity
import com.example.imagegram.screens.common.setupAuthGuard
import com.example.imagegram.screens.common.setupBottomNavigation
import com.example.imagegram.screens.findusers.FindUsersActivity
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : BaseActivity(), FeedAdapter.Listener {
    private lateinit var mViewModel: HomeViewModel
    private lateinit var mAdapter: FeedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        Log.e(TAG, "onCreate")
        setupBottomNavigation(0)

        mAdapter = FeedAdapter(this)
        feed_recycler.adapter = mAdapter
        feed_recycler.layoutManager = LinearLayoutManager(this)

        setupAuthGuard { uid ->
            mViewModel = initViewModel()
            mViewModel.init(uid)
            mViewModel.feedPosts.observe(this, Observer {
                it?.let {
                    mAdapter.updatePosts(it)
                }
            })
        }

        search_bt.setOnClickListener {
            val intent = Intent(this, FindUsersActivity::class.java)
            this.startActivity(intent)
            Log.e(TAG, "run StartActivity for FindUsersActivity")
        }

    }

    override fun toggleLike(postId: String) {
        Log.d(TAG, "toggleLike: $postId")
        mViewModel.toggleLike(postId)
    }

    override fun loadLikes(postId: String, position: Int) {
        if (mViewModel.getLikes(postId) == null) {
            mViewModel.loadLikes(postId).observe(this, Observer {
                it?.let { postLikes ->
                    mAdapter.updatePostLikes(position, postLikes)

                }
            })
        }
    }

    companion object {
        const val TAG = "HomeActivity"
    }
}

