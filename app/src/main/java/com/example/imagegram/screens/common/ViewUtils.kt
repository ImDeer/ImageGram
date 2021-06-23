package com.example.imagegram.screens.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.imagegram.R

fun Context.showToast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
    text?.let { Toast.makeText(this, it, duration).show() }

}

@SuppressLint("ResourceType")
fun ImageView.loadUserPhoto(photoUrl: String?) =
    ifNotDestroyed {
        GlideApp.with(this).load(photoUrl).fallback(R.raw.basic_profile_image).into(this)
    }


@SuppressLint("ResourceType")
fun ImageView.loadImage(image: String) =
    ifNotDestroyed {
        GlideApp.with(this).load(image).fallback(R.raw.basic_profile_image)
            .transform(CenterCrop(), RoundedCorners(40)).into(this)
    }


private fun View.ifNotDestroyed(block: () -> Unit) {
    if (!(context as Activity).isDestroyed) {
        block()
    }
}

fun Editable.toStringOrNull(): String? {
    val str = toString()
    return if (str.isEmpty()) null else str
}


fun coordinateBtnsAndInputs(btn: Button, vararg inputs: EditText) {
    val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            btn.isEnabled = inputs.all { it.text.isNotEmpty() }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    }
    inputs.forEach { it.addTextChangedListener(watcher) }
    btn.isEnabled = inputs.all { it.text.isNotEmpty() }
}



