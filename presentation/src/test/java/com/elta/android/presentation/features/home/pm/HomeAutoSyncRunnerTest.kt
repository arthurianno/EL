package com.elta.android.presentation.features.home.pm

import com.elta.android.domain.features.userinfo.model.UserInfo
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeAutoSyncRunnerTest {
    @Test
    fun `device sync completes before backend sync starts`() {
        val steps = mutableListOf<String>()
        val deviceSync = PublishSubject.create<Unit>()
        val runner = HomeAutoSyncRunner(
            loadUserInfo = { Single.just(UserInfo(isFirstHomeEntrance = false)) },
            hasPrimaryDevice = { Single.just(true) },
            syncDevice = {
                steps += "device"
                deviceSync
            },
            onDeviceError = { steps += "error" },
            startBackendSync = { steps += "backend" }
        )

        val result = runner.execute().test()
        assertEquals(listOf("device"), steps)
        deviceSync.onNext(Unit)
        assertEquals(listOf("device"), steps)
        deviceSync.onComplete()

        result.assertValue(Unit).assertComplete().assertNoErrors()
        assertEquals(listOf("device", "backend"), steps)
    }

    @Test
    fun `first entrance or missing device starts backend sync directly`() {
        listOf(
            UserInfo(isFirstHomeEntrance = true) to true,
            UserInfo(isFirstHomeEntrance = false) to false
        ).forEach { (info, hasPrimary) ->
            val steps = mutableListOf<String>()
            val runner = HomeAutoSyncRunner(
                loadUserInfo = { Single.just(info) },
                hasPrimaryDevice = { Single.just(hasPrimary) },
                syncDevice = {
                    steps += "device"
                    Observable.just(Unit)
                },
                onDeviceError = { steps += "error" },
                startBackendSync = { steps += "backend" }
            )

            runner.execute().test().assertValue(Unit).assertComplete().assertNoErrors()
            assertEquals(listOf("backend"), steps)
        }
    }

    @Test
    fun `device error delegates recovery without running completion callback`() {
        val steps = mutableListOf<String>()
        val failure = IllegalStateException("device unavailable")
        val runner = HomeAutoSyncRunner(
            loadUserInfo = { Single.just(UserInfo(isFirstHomeEntrance = false)) },
            hasPrimaryDevice = { Single.just(true) },
            syncDevice = { Observable.error(failure) },
            onDeviceError = { steps += "error" },
            startBackendSync = { steps += "backend" }
        )

        runner.execute().test().assertError(failure).assertNotComplete()
        assertEquals(listOf("error"), steps)
    }
}
