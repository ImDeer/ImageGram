package com.example.imagegram.data.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

fun <A, B> LiveData<A>.map(f: (A) -> (B)): LiveData<B> =
    Transformations.map(this, f)