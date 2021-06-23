package com.example.imagegram.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.imagegram.R
import com.example.imagegram.common.ValueEventListenerAdapter
import com.example.imagegram.data.firebase.common.FirebaseHelper
import com.example.imagegram.data.firebase.common.asUser
import com.example.imagegram.models.User
import com.example.imagegram.screens.common.BaseActivity
import com.example.imagegram.screens.common.loadImage
import com.example.imagegram.screens.common.loadUserPhoto
import com.example.imagegram.screens.common.setupBottomNavigation
import com.example.imagegram.screens.editprofile.EditProfileActivity
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : BaseActivity() {
    private val TAG = "ProfileActivity"
    private lateinit var mFirebase: FirebaseHelper
    private lateinit var mUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        Log.d(TAG, "onCreate")
        setupBottomNavigation(2)

        edit_profile_btn.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        profile_log_out_bt.setOnClickListener {
            mFirebase.auth.signOut()
        }

        mFirebase =
            FirebaseHelper(this)
        mFirebase.currentUserReference().addValueEventListener(ValueEventListenerAdapter {
            mUser = it.asUser()!!
            profile_image.loadUserPhoto(mUser.photo)
            profile_name.text = mUser.name
            profile_head.text = getString(R.string.at_username, mUser.username)
        })

        profile_recycler_view.layoutManager = GridLayoutManager(this, 2)
        // RecyclerView structure: RecyclerView, LayoutManager, Adapter(ViewHolder for performance optimizations)
        mFirebase.database.child("images").child(mFirebase.currentUid()!!)
            .addValueEventListener(ValueEventListenerAdapter {
                val images = it.children.map { it.getValue(String::class.java)!! }
                profile_recycler_view.adapter = ImagesAdapter(images + images + images)
            })
    }
}

class ImagesAdapter(private val images: List<String>) :
    RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {
    class ViewHolder(val image: ImageView) : RecyclerView.ViewHolder(image)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val image = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_item, parent, false) as ImageView
        return ViewHolder(image)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.loadImage(images[position])
    }

    override fun getItemCount(): Int = images.size
}

class SquareImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}