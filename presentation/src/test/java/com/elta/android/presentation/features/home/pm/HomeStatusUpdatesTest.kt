package com.elta.android.presentation.features.home.pm

import com.elta.android.domain.features.user.model.GlucoseFormat
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeStatusUpdatesTest {
    @Test
    fun `glucose format refresh survives a failed request`() {
        val changes = PublishSubject.create<Unit>()
        val errors = mutableListOf<Throwable>()
        var attempts = 0
        val updates = HomeStatusUpdates(
            loadGlucoseFormat = {
                if (++attempts == 1) throw IOException("offline")
                else Single.just(GlucoseFormat.PLASMA)
            },
            loadFirstSync = { Single.just(false) },
            onError = errors::add
        )
        val observer = updates.glucoseFormatOn(changes).test()

        changes.onNext(Unit)
        changes.onNext(Unit)

        observer.assertValues(GlucoseFormat.PLASMA).assertNoErrors()
        assertEquals(2, attempts)
        assertEquals(1, errors.size)
    }

    @Test
    fun `first sync refresh survives a failed request`() {
        val changes = PublishSubject.create<Unit>()
        val errors = mutableListOf<Throwable>()
        var attempts = 0
        val updates = HomeStatusUpdates(
            loadGlucoseFormat = { Single.just(GlucoseFormat.CAPILLARY) },
            loadFirstSync = {
                if (++attempts == 1) Single.error(IOException("offline"))
                else Single.just(true)
            },
            onError = errors::add
        )
        val observer = updates.firstSyncOn(changes).test()

        changes.onNext(Unit)
        changes.onNext(Unit)

        observer.assertValues(true).assertNoErrors()
        assertEquals(2, attempts)
        assertEquals(1, errors.size)
    }
}
