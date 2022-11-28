package com.elta.android.presentation.utils.dynamiclinks

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import me.dmdev.rxpm.Action
import timber.log.Timber

class DynamicLinkProcessor private constructor(
    private val initialIntent: Intent?,
    private val ignoreColdStart: Boolean,
    private val savedState: Bundle? = null,
    private val coldStartAction: Action<Unit>?,
    private val notificationStartAction: Action<Uri>?,
    private val deepLinkOpenAction: Action<Uri>?,
    private val coldStartByDeepLinkAction: Action<Uri>?
) {

    @Suppress("LongMethod")
    fun process() {
        if (initialIntent != null) {
            FirebaseDynamicLinks.getInstance().getDynamicLink(initialIntent)
                .addOnSuccessListener { pendingDynamicLinkData ->
                    with(pendingDynamicLinkData?.link) {
                        if (this != null) {
                            if (!ignoreColdStart && savedState == null) {
                                coldStartByDeepLinkAction?.consumer?.accept(this)
                            } else {
                                deepLinkOpenAction?.consumer?.accept(this)
                            }
                        } else {
                            if (initialIntent.data.isNotificationUriValid()) {
                                notificationStartAction?.consumer?.accept(initialIntent.data)
                            } else {
                                processColdStartIfNeed()
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Timber.e(it, "DynamicLink process error ")
                    processColdStartIfNeed()
                }
                .addOnCanceledListener {
                    processColdStartIfNeed()
                }
        } else {
            processColdStartIfNeed()
        }
    }

    private fun processColdStartIfNeed() {
        if (!ignoreColdStart && savedState == null) {
            coldStartAction?.consumer?.accept(Unit)
        }
    }

    companion object {
        fun from(intent: Intent?) = Builder(intent)
    }

    class Builder(private val initialIntent: Intent?) {
        private var ignoreColdStart: Boolean = true
        private var savedState: Bundle? = null
        private var coldStartAction: Action<Unit>? = null
        private var notificationStartAction: Action<Uri>? = null
        private var deepLinkOpenAction: Action<Uri>? = null
        private var coldStartByDeepLinkAction: Action<Uri>? = null

        fun ignoreColdStart(ignore: Boolean): Builder {
            this.ignoreColdStart = ignore
            return this
        }

        fun withSavedState(savedState: Bundle?): Builder {
            this.savedState = savedState
            return this
        }

        fun coldStartPassTo(action: Action<Unit>): Builder {
            this.coldStartAction = action
            return this
        }

        fun notificationStartPassTo(action: Action<Uri>): Builder {
            this.notificationStartAction = action
            return this
        }

        fun deepLinkStartPassTo(action: Action<Uri>): Builder {
            this.deepLinkOpenAction = action
            return this
        }

        fun coldStartByDeepLinkPassTo(action: Action<Uri>): Builder {
            this.coldStartByDeepLinkAction = action
            return this
        }

        fun build(): DynamicLinkProcessor =
            DynamicLinkProcessor(
                initialIntent,
                ignoreColdStart,
                savedState,
                coldStartAction,
                notificationStartAction,
                deepLinkOpenAction,
                coldStartByDeepLinkAction
            )
    }
}
