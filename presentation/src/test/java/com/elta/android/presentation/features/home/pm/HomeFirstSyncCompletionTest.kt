package com.elta.android.presentation.features.home.pm

import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeFirstSyncCompletionTest {
    @Test
    fun `failed save restores first sync and next action can retry`() {
        val actions = PublishSubject.create<Unit>()
        val states = mutableListOf<Boolean>()
        val errors = mutableListOf<Throwable>()
        var attempts = 0
        val completion = HomeFirstSyncCompletion(
            saveCompleted = {
                if (++attempts == 1) Completable.error(IOException("offline"))
                else Completable.complete()
            },
            setFirstSync = { states += it },
            reportError = { errors += it }
        )
        val observer = actions.flatMapCompletable { completion.complete() }.test()

        actions.onNext(Unit)
        actions.onNext(Unit)
        actions.onComplete()

        observer.assertComplete().assertNoErrors()
        assertEquals(2, attempts)
        assertEquals(listOf(false, true, false), states)
        assertEquals(1, errors.size)
    }

    @Test
    fun `synchronous save failure also restores first sync`() {
        val states = mutableListOf<Boolean>()
        val errors = mutableListOf<Throwable>()
        val completion = HomeFirstSyncCompletion(
            saveCompleted = {
                throw IOException("cannot create request")
            },
            setFirstSync = { states += it },
            reportError = { errors += it }
        )

        completion.complete().test().assertComplete().assertNoErrors()

        assertEquals(listOf(false, true), states)
        assertEquals(1, errors.size)
    }
}
