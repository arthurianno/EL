package com.elta.android.data.features.auth.datasource.social

import android.content.Context
import com.elta.android.data.R
import com.vk.sdk.VKSdk
import ru.ok.android.sdk.Odnoklassniki

object SocialNetworks {

    fun initialize(context: Context) {
        VKSdk.initialize(context)
        Odnoklassniki.createInstance(
            context,
            context.getString(R.string.OK_APP_ID),
            context.getString(R.string.OK_APP_KEY)
        )
    }
}
