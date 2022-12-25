package com.elta.android.data.features.consultant.model // ktlint-disable filename

import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.user.model.Profile
import com.google.gson.JsonObject

internal fun WebimUser.toJSonObject(): JsonObject =
    JsonObject().apply {
        addProperty("id", id)
        addProperty("display_name", name)
    }

internal fun Profile.toWebimUser(): WebimUser =
    WebimUser(
        id = firstName + secondName,
        name = "$firstName $secondName"
    )
