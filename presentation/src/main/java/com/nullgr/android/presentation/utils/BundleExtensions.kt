package com.nullgr.android.presentation.utils

import android.os.Bundle
import android.os.Parcelable

fun bundle(vararg pairs: Pair<String, Any>): Bundle =
    Bundle().apply {
        pairs.forEach {
            when (it.second) {
                is String -> putString(it.first, it.second as String)
                is Int -> putInt(it.first, it.second as Int)
                is Float -> putFloat(it.first, it.second as Float)
                is Parcelable -> putParcelable(it.first, it.second as Parcelable)
                else -> error("Add your case")
            }
        }
    }
