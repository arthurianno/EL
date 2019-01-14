package com.nullgr.android.common.utils

import android.annotation.SuppressLint
import android.content.SharedPreferences

/**
 * Function to easy save, primitive types values in [SharedPreferences].
 * Supported this types: [String], [Int], [Boolean], [Float], [Long]
 * @param key [String] key for [SharedPreferences]
 * @param value value to save in [SharedPreferences].
 * Can be null. If null is passed, the entry with the specified [key] will be deleted
 * @throws UnsupportedOperationException if any other type of [value] is set
 */
fun SharedPreferences.setNow(key: String, value: Any?) {
    if (value == null) {
        editNow { it.remove(key) }
    } else {
        when (value) {
            is String? -> editNow { it.putString(key, value) }
            is Int -> editNow { it.putInt(key, value) }
            is Boolean -> editNow { it.putBoolean(key, value) }
            is Float -> editNow { it.putFloat(key, value) }
            is Long -> editNow { it.putLong(key, value) }
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }
}

/**
 * Removes preference with give [key]
 * @param key [String] key
 */
fun SharedPreferences.removeNow(key: String) {
    editNow { it.remove(key) }
}

/**
 * Removes ***all*** values from the preferences.
 */
fun SharedPreferences.clearNow() {
    editNow { it.clear() }
}

/**
 * Performs [operation] with [SharedPreferences.Editor] and applys changes.
 */
@SuppressLint("ApplySharedPref")
inline fun SharedPreferences.editNow(operation: (SharedPreferences.Editor) -> Unit) {
    val editor = this.edit()
    operation(editor)
    editor.commit()
}
