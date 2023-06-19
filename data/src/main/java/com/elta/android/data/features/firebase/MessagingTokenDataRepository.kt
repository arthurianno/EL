package com.elta.android.data.features.firebase

import com.elta.android.domain.features.firebase.repository.MessagingTokenRepository
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Single
import timber.log.Timber
import java.lang.RuntimeException

class MessagingTokenDataRepository: MessagingTokenRepository {
    override fun getToken(): Single<String> {
        return Single
            .create<String> {
                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Timber.i("Get new FCM registration token")
                            val newToken = task.result

                            Timber.d("New FCM token: $newToken")

                            if (it.isDisposed.not()) {
                                if (newToken != null) {
                                    it.onSuccess(newToken)
                                } else {
                                    it.tryOnError(RuntimeException("New FCM token is null"))
                                }
                            }
                        } else {
                            Timber.e("Fetching FCM registration token failed ${task.exception}")

                            val e = task.exception ?: RuntimeException("FCM exception is null")
                            it.tryOnError(e)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Timber.e("Fetching FCM registration token failed $exception")
                        it.tryOnError(exception)
                    }

            }
            .onErrorReturn { "__PUSH_KIT_TOKEN__" }
    }
}