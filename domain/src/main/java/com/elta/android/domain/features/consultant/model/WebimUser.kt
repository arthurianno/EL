package com.elta.android.domain.features.consultant.model

data class WebimUser(
    val id: String,
    val name: String
) {
    override fun toString(): String = "$name$id"
}
