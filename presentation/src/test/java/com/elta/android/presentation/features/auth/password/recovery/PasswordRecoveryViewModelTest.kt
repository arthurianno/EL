package com.elta.android.presentation.features.auth.password.recovery

import androidx.lifecycle.ViewModelStore
import com.elta.android.common.errors.NetworkConnectionError
import com.elta.android.common.errors.NotFoundError
import com.elta.android.domain.features.auth.interactor.SendPasswordResetLinkUseCase
import com.elta.android.domain.features.auth.repository.AuthRepository
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.Resource
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.domain.features.multiLangsConfig.repository.MultilangConfigRepository
import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.domain.features.remoteconfig.model.FeatureConfig
import com.elta.android.domain.features.remoteconfig.repository.RemoteConfigRepository
import com.elta.android.presentation.R
import com.elta.android.presentation.features.auth.password.recovery.model.PasswordRecoveryAction
import com.elta.android.presentation.features.auth.password.recovery.model.PasswordRecoveryEffect
import com.elta.android.presentation.features.auth.password.recovery.viewmodel.PasswordRecoveryViewModel
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.CompletableSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PasswordRecoveryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val repository = FakeAuthRepository()
    private val flags = FakeRemoteConfigRepository()
    private lateinit var viewModel: PasswordRecoveryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val schedulers = object : SchedulersFacade {
            override val subscribeOn = Schedulers.trampoline()
            override val observeOn = Schedulers.trampoline()
        }
        viewModel = PasswordRecoveryViewModel(
            SendPasswordResetLinkUseCase(repository, schedulers),
            GetFeatureConfigUseCase(flags),
            GetScreenConfigFromCache(UnusedScreenConfigRepository())
        )
        store.put("recovery", viewModel)
    }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `clearing a previously valid email disables submission without showing an error`() = runTest(dispatcher) {
        viewModel.sendAction(PasswordRecoveryAction.EmailChanged("user@example.com"))
        runCurrent()
        assertTrue(viewModel.state.value.canSubmit)

        viewModel.sendAction(PasswordRecoveryAction.EmailChanged(""))
        viewModel.sendAction(PasswordRecoveryAction.Submit)
        runCurrent()

        assertFalse(viewModel.state.value.canSubmit)
        assertNull(viewModel.state.value.emailError)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun `invalid email shows validation error and does not start a request`() = runTest(dispatcher) {
        viewModel.sendAction(PasswordRecoveryAction.EmailChanged("not-an-email"))
        viewModel.sendAction(PasswordRecoveryAction.Submit)
        runCurrent()

        assertEquals(R.string.registration_error_input_email, viewModel.state.value.emailError)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun `repeated submit and edits during a request do not change the submitted email`() = runTest(dispatcher) {
        val request = CompletableSubject.create()
        repository.response = request
        viewModel.sendAction(PasswordRecoveryAction.EmailChanged("user@example.com"))
        viewModel.sendAction(PasswordRecoveryAction.Submit)
        viewModel.sendAction(PasswordRecoveryAction.Submit)
        viewModel.sendAction(PasswordRecoveryAction.EmailChanged("other@example.com"))
        runCurrent()

        assertEquals(listOf("user@example.com"), repository.requests)
        assertEquals("user@example.com", viewModel.state.value.email)
        assertTrue(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.canSubmit)
    }

    @Test
    fun `not registered error stays on the field and editing clears it`() = runTest(dispatcher) {
        repository.response = Completable.error(NotFoundError("not registered"))
        viewModel.sendAction(PasswordRecoveryAction.EmailChanged("user@example.com"))
        viewModel.sendAction(PasswordRecoveryAction.Submit)
        runCurrent()

        assertEquals(R.string.user_not_registered, viewModel.state.value.emailError)
        assertFalse(viewModel.state.value.isLoading)
        viewModel.sendAction(PasswordRecoveryAction.EmailChanged("other@example.com"))
        runCurrent()
        assertNull(viewModel.state.value.emailError)
        assertTrue(viewModel.state.value.canSubmit)
    }

    @Test
    fun `network failure allows retry and queues a message without an active UI collector`() = runTest(dispatcher) {
        repository.response = Completable.error(NetworkConnectionError())
        viewModel.sendAction(PasswordRecoveryAction.EmailChanged("user@example.com"))
        viewModel.sendAction(PasswordRecoveryAction.Submit)
        runCurrent()

        assertTrue(viewModel.state.value.canSubmit)
        assertEquals(
            PasswordRecoveryEffect.ShowMessage(R.string.no_connection_to_the_internet),
            viewModel.effects.first()
        )
        repository.response = Completable.complete()
        viewModel.sendAction(PasswordRecoveryAction.Submit)
        runCurrent()
        assertEquals(2, repository.requests.size)
        assertEquals(PasswordRecoveryEffect.LinkSent(true), viewModel.effects.first())
    }

    @Test
    fun `success uses the new login flag and cannot be submitted twice before navigation`() = runTest(dispatcher) {
        repository.response = Completable.complete()
        viewModel.sendAction(PasswordRecoveryAction.EmailChanged("user@example.com"))
        viewModel.sendAction(PasswordRecoveryAction.Submit)
        runCurrent()
        viewModel.sendAction(PasswordRecoveryAction.Submit)
        runCurrent()

        assertEquals(PasswordRecoveryEffect.LinkSent(true), viewModel.effects.first())
        assertEquals(1, repository.requests.size)
        assertFalse(viewModel.state.value.canSubmit)
        viewModel.onLinkSentHandled()
        assertTrue(viewModel.state.value.canSubmit)
    }

    @Test
    fun `success keeps the legacy login when the flag is disabled`() = runTest(dispatcher) {
        flags.recoveryAccount = false
        repository.response = Completable.complete()
        viewModel.sendAction(PasswordRecoveryAction.EmailChanged("user@example.com"))
        viewModel.sendAction(PasswordRecoveryAction.Submit)
        runCurrent()

        assertEquals(PasswordRecoveryEffect.LinkSent(false), viewModel.effects.first())
    }

    @Test
    fun `clearing the ViewModel cancels the in-flight Rx request`() = runTest(dispatcher) {
        val request = CompletableSubject.create()
        repository.response = request
        viewModel.sendAction(PasswordRecoveryAction.EmailChanged("user@example.com"))
        viewModel.sendAction(PasswordRecoveryAction.Submit)
        runCurrent()
        assertTrue(request.hasObservers())

        store.clear()
        runCurrent()
        assertFalse(request.hasObservers())
        assertFalse(viewModel.state.value.isLinkSent)
    }

    private class FakeAuthRepository : AuthRepository {
        var response: Completable = Completable.never()
        val requests = mutableListOf<String>()

        override fun sendResetPasswordLink(email: String): Completable {
            requests += email
            return response
        }

        override fun register(email: String, password: String, languageTag: String?, countryCode: String?) = unused()
        override fun login(email: String, password: String, activateAccount: Boolean) = unused()
        override fun isEmailConfirmed() = unused()
        override fun sendConfirmationLink() = unused()
        override fun resetPassword(token: String, newPassword: String) = unused()
        override fun changePassword(currentPassword: String, newPassword: String) = unused()
        override fun checkTokenOwner(token: String) = unused()
        override fun confirmEmail(token: String) = unused()
        override fun logout() = unused()
        override fun deleteAccount() = unused()
    }

    private class FakeRemoteConfigRepository : RemoteConfigRepository {
        var recoveryAccount = true
        override fun getFeatureConfig() = FeatureConfig(recoveryAccount, improvedEnablingLocation = false)
        override suspend fun fetchRemoteConfig() = unused()
    }

    private class UnusedScreenConfigRepository : MultilangConfigRepository {
        override suspend fun getAllScreens(): Resource<List<ScreenEntity>> = unused()
        override suspend fun getScreenConfigFromCache(slug: String): Resource<ScreenEntity> = unused()
        override suspend fun shouldRefreshScreensConfig(): Boolean = unused()
        override suspend fun updateLastRefreshTime(): Unit = unused()
    }

    companion object {
        private fun unused(): Nothing = error("Unexpected dependency call")
    }
}
