package com.elta.android.data.features.googlefit.datasource.utils

import com.google.android.gms.fitness.SessionsClient
import com.google.android.gms.fitness.data.Session
import com.google.android.gms.fitness.request.SessionReadRequest
import io.reactivex.Observable

fun SessionsClient.readSessions(request: SessionReadRequest) =
    Observable.create<List<Session>> { emmiter ->
        val response = readSession(request)
        response.addOnCanceledListener {
            if (!emmiter.isDisposed) {
                emmiter.onComplete()
            }
        }
        response.addOnCompleteListener {
            runCatching {
                if (!emmiter.isDisposed) {
                    it.result?.sessions?.let { sessions ->
                        emmiter.onNext(sessions)
                        emmiter.onComplete()
                    }
                }
            }.exceptionOrNull()?.let {
                emmiter.onError(it)
            }
        }
        response.addOnFailureListener {
            if (!emmiter.isDisposed) {
                emmiter.onError(it)
            }
        }
    }
