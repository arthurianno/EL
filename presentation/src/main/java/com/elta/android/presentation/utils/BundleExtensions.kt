package com.elta.android.presentation.utils // ktlint-disable filename

import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

fun bundle(vararg pairs: Pair<String, Any>): Bundle =
    Bundle().apply {
        pairs.forEach {
            when (it.second) {
                is String -> putString(it.first, it.second as String)
                is Boolean -> putBoolean(it.first, it.second as Boolean)
                is Int -> putInt(it.first, it.second as Int)
                is Float -> putFloat(it.first, it.second as Float)
                is Parcelable -> putParcelable(it.first, it.second as Parcelable)
                is Serializable -> putSerializable(it.first, it.second as Serializable)
                else -> error("Add your case")
            }
        }
    }
