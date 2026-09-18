package com.elta.android.presentation.features.auth.password.create

import androidx.lifecycle.ViewModelStore
import com.elta.android.common.errors.NetworkConnectionError
import com.elta.android.domain.features.auth.interactor.ResetPasswordUseCase
import com.elta.android.domain.features.auth.repository.AuthRepository
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.Resource
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.domain.features.multiLangsConfig.repository.MultilangConfigRepository
import com.elta.android.presentation.R
import com.elta.android.presentation.features.auth.password.create.model.PasswordCreateAction
import com.elta.android.presentation.features.auth.password.create.model.PasswordCreateEffect
import com.elta.android.presentation.features.auth.password.create.viewmodel.PasswordCreateViewModel
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.CompletableSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class PasswordCreateViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val repository = FakeAuthRepository()
    private lateinit var viewModel: PasswordCreateViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val schedulers = object : SchedulersFacade {
            override val subscribeOn = Schedulers.trampoline()
            override val observeOn = Schedulers.trampoline()
        }
        viewModel = PasswordCreateViewModel(
            ResetPasswordUseCase(repository, schedulers),
            GetScreenConfigFromCache(UnusedScreenConfigRepository())
        )
        store.put("password-create", viewModel)
        viewModel.setResetToken("test-reset-token")
    }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `missing or blank token prevents a reset request`() = runTest(dispatcher) {
        viewModel.sendAction(PasswordCreateAction.PasswordChanged("Example123"))
        for (token in listOf(null, "", " ")) {
            viewModel.setResetToken(token)
            viewModel.sendAction(PasswordCreateAction.Submit)
            runCurrent()
            assertFalse(viewModel.state.value.hasResetToken)
            assertFalse(viewModel.state.value.canSubmit)
        }
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun `clearing a valid password disables submission`() = runTest(dispatcher) {
        viewModel.sendAction(PasswordCreateAction.PasswordChanged("Example123"))
        runCurrent()
        assertTrue(viewModel.state.value.canSubmit)
        viewModel.sendAction(PasswordCreateAction.PasswordChanged(""))
        viewModel.sendAction(PasswordCreateAction.Submit)
        runCurrent()
        assertFalse(viewModel.state.value.canSubmit)
        assertNull(viewModel.state.value.passwordError)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun `invalid password shows validation error without submitting`() = runTest(dispatcher) {
        viewModel.sendAction(PasswordCreateAction.PasswordChanged("short"))
        viewModel.sendAction(PasswordCreateAction.Submit)
        runCurrent()
        assertEquals(R.string.registration_password_pattern, viewModel.state.value.passwordError)
        assertTrue(repository.requests.isEmpty())
        viewModel.sendAction(PasswordCreateAction.PasswordChanged("Example123"))
        runCurrent()
        assertNull(viewModel.state.value.passwordError)
    }

    @Test
    fun `visibility toggles without changing the password`() = runTest(dispatcher) {
        viewModel.sendAction(PasswordCreateAction.PasswordChanged("Example123"))
        viewModel.sendAction(PasswordCreateAction.TogglePasswordVisibility)
        runCurrent()
        assertTrue(viewModel.state.value.isPasswordVisible)
        viewModel.sendAction(PasswordCreateAction.TogglePasswordVisibility)
        runCurrent()
        assertFalse(viewModel.state.value.isPasswordVisible)
        assertEquals("Example123", viewModel.state.value.password)
    }

    @Test
    fun `repeated submit and editing during request preserve original token and password`() = runTest(dispatcher) {
        viewModel.sendAction(PasswordCreateAction.PasswordChanged("Example123"))
        viewModel.sendAction(PasswordCreateAction.Submit)
        viewModel.sendAction(PasswordCreateAction.Submit)
        viewModel.sendAction(PasswordCreateAction.PasswordChanged("Other123"))
        viewModel.sendAction(PasswordCreateAction.TogglePasswordVisibility)
        runCurrent()
        assertEquals(listOf("test-reset-token" to "Example123"), repository.requests)
        assertEquals("Example123", viewModel.state.value.password)
        assertTrue(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.isPasswordVisible)
        assertFalse(viewModel.state.value.canSubmit)
    }

    @Test
    fun `network error permits retry and retains a message for a resumed UI`() = runTest(dispatcher) {
        repository.response = Completable.error(NetworkConnectionError())
        viewModel.sendAction(PasswordCreateAction.PasswordChanged("Example123"))
        viewModel.sendAction(PasswordCreateAction.Submit)
        runCurrent()
        assertTrue(viewModel.state.value.canSubmit)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(
            PasswordCreateEffect.ShowMessage(R.string.no_connection_to_the_internet),
            viewModel.effects.first()
        )
        repository.response = Completable.never()
        viewModel.sendAction(PasswordCreateAction.Submit)
        runCurrent()
        assertEquals(2, repository.requests.size)
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `success clears password and closes once after two seconds`() = runTest(dispatcher) {
        repository.response = Completable.complete()
        viewModel.sendAction(PasswordCreateAction.PasswordChanged("Example123"))
        viewModel.sendAction(PasswordCreateAction.Submit)
        runCurrent()
        assertEquals(PasswordCreateEffect.PasswordChanged, viewModel.effects.first())
        assertTrue(viewModel.state.value.isPasswordChanged)
        assertEquals("", viewModel.state.value.password)
        assertFalse(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.canSubmit)

        val close = async { viewModel.effects.first() }
        viewModel.sendAction(PasswordCreateAction.PasswordChanged("Other123"))
        viewModel.sendAction(PasswordCreateAction.Submit)
        advanceTimeBy(1_999)
        runCurrent()
        assertFalse(close.isCompleted)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(PasswordCreateEffect.Close, close.await())
        assertEquals(1, repository.requests.size)
        assertEquals("", viewModel.state.value.password)
    }

    @Test
    fun `clearing ViewModel cancels the in-flight request`() = runTest(dispatcher) {
        val request = CompletableSubject.create()
        repository.response = request
        viewModel.sendAction(PasswordCreateAction.PasswordChanged("Example123"))
        viewModel.sendAction(PasswordCreateAction.Submit)
        runCurrent()
        assertTrue(request.hasObservers())
        store.clear()
        runCurrent()
        assertFalse(request.hasObservers())
        assertFalse(viewModel.state.value.isPasswordChanged)
    }

    private class FakeAuthRepository : AuthRepository {
        var response: Completable = Completable.never()
        val requests = mutableListOf<Pair<String, String>>()

        override fun resetPassword(token: String, newPassword: String): Completable {
            requests += token to newPassword
            return response
        }

        override fun register(email: String, password: String, languageTag: String?, countryCode: String?) = unused()
        override fun login(email: String, password: String, activateAccount: Boolean) = unused()
        override fun isEmailConfirmed() = unused()
        override fun sendConfirmationLink() = unused()
        override fun sendResetPasswordLink(email: String) = unused()
        override fun changePassword(currentPassword: String, newPassword: String) = unused()
        override fun checkTokenOwner(token: String) = unused()
        override fun confirmEmail(token: String) = unused()
        override fun logout() = unused()
        override fun deleteAccount() = unused()
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
