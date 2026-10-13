package com.elta.android.presentation.features.sync.connect

import android.os.Bundle
import androidx.lifecycle.ViewModelStore
import androidx.fragment.app.FragmentFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.domain.features.remoteconfig.model.FeatureConfig
import com.elta.android.domain.features.remoteconfig.repository.RemoteConfigRepository
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.analytics.Analytics
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricAttribute
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.params.ConnectingTypeParam
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.viewmodel.ConnectTypeViewModel
import com.github.terrakok.cicerone.Back
import com.github.terrakok.cicerone.BackTo
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Command
import com.github.terrakok.cicerone.Forward
import com.github.terrakok.cicerone.Navigator
import com.github.terrakok.cicerone.Replace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConnectTypeNavigationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test
    fun savedLegacyFragmentRestoresAsMethodSelector() {
        val loader = instrumentation.targetContext.classLoader
        val fragment = FragmentFactory().instantiate(
            loader,
            "com.elta.android.presentation.features.sync.connect.ConnectStartFragment"
        )
        assertTrue(fragment is ConnectTypeFragment)
    }

    @Test
    fun openingSelectorDoesNotNavigateOrSelectDmc() = forEachContext { onboarding, improved ->
        withFixture(onboarding, improved) { model, commands, metrics ->
            assertEquals(onboarding, model.state.value.isOnBoarding)
            assertTrue(commands.isEmpty())
            assertFalse(metrics.any { it is AppMetricEvent.ConnectingOptionClick })
        }
    }

    @Test
    fun pinChoiceUsesCorrectBranchAndOnboardingContext() = forEachContext { onboarding, improved ->
        withFixture(onboarding, improved) { model, commands, metrics ->
            instrumentation.runOnMainSync { model sendAction ConnectAction.ConnectByPin }
            instrumentation.waitForIdleSync()
            val expected = when {
                improved && onboarding -> Screens.FromOnBoardingConnectDeviceByPin
                improved -> Screens.FromOtherConnectDeviceByPin
                onboarding -> Screens.FromOnBoardingConnectDeviceByPinVariantA
                else -> Screens.FromOtherConnectDeviceByPinVariantA
            }
            assertEquals(expected, (commands.single() as Forward).screen)
            assertEquals(1, metrics.count { it == AppMetricEvent.ConnectingOptionClick(ConnectingTypeParam.PIN_ENTER) })
        }
    }

    @Test
    fun dmcChoiceKeepsSelectorInBackStackAndUsesCorrectBranch() = forEachContext { onboarding, improved ->
        withFixture(onboarding, improved) { model, commands, metrics ->
            instrumentation.runOnMainSync { model sendAction ConnectAction.ConnectByDmc }
            instrumentation.waitForIdleSync()
            val expected = if (improved) Screens.HowToConnectScreen(onboarding) else Screens.HowToConnectScreenVariantA(onboarding)
            assertEquals(expected, (commands.single() as Forward).screen)
            assertEquals(1, metrics.count { it == AppMetricEvent.ConnectingOptionClick(ConnectingTypeParam.DMC_SCAN) })
        }
    }

    @Test
    fun laterOpensHomeOnlyDuringOnboarding() = forEachContext { onboarding, improved ->
        withFixture(onboarding, improved) { model, commands, _ ->
            instrumentation.runOnMainSync { model sendAction ConnectAction.SkipNextStep }
            instrumentation.waitForIdleSync()
            if (onboarding) {
                assertTrue(commands.first() is BackTo)
                assertEquals(if (improved) Screens.HomeFlow else Screens.HomeFlowVariantA, (commands.last() as Replace).screen)
            } else {
                assertTrue(commands.isEmpty())
            }
        }
    }

    @Test
    fun backReturnsToProfileOrFinishesOnboarding() = forEachContext { onboarding, improved ->
        withFixture(onboarding, improved) { model, commands, _ ->
            instrumentation.runOnMainSync { model sendAction AppAction.BackPressure }
            instrumentation.waitForIdleSync()
            if (onboarding) {
                assertEquals(if (improved) Screens.HomeFlow else Screens.HomeFlowVariantA, (commands.last() as Replace).screen)
            } else {
                assertTrue(commands.single() is Back)
            }
        }
    }

    @Test
    fun helpUsesExistingHelpScreen() {
        withFixture(false, true) { model, commands, _ ->
            instrumentation.runOnMainSync { model sendAction ConnectAction.NeedHelp }
            instrumentation.waitForIdleSync()
            assertEquals(Screens.ConnectHelpScreen, (commands.single() as Forward).screen)
        }
    }

    private fun forEachContext(test: (Boolean, Boolean) -> Unit) {
        listOf(false, true).forEach { onboarding -> listOf(false, true).forEach { improved -> test(onboarding, improved) } }
    }

    private fun withFixture(
        onboarding: Boolean,
        improved: Boolean,
        test: (ConnectTypeViewModel, List<Command>, List<AppMetricEvent>) -> Unit
    ) {
        val commands = mutableListOf<Command>()
        val metrics = mutableListOf<AppMetricEvent>()
        val store = ViewModelStore()
        lateinit var model: ConnectTypeViewModel
        val cicerone = Cicerone.create()
        instrumentation.runOnMainSync {
            cicerone.getNavigatorHolder().setNavigator(object : Navigator {
                override fun applyCommands(commandsToApply: Array<out Command>) {
                    commands.addAll(commandsToApply)
                }
            })
            val repository = object : RemoteConfigRepository {
                override suspend fun fetchRemoteConfig() = true
                override fun getFeatureConfig() = FeatureConfig(recoveryAccount = false, improvedEnablingLocation = improved)
            }
            val tracker = object : AppMetricTracker {
                override fun trackEvent(event: AppMetricEvent) { metrics.add(event) }
                override fun setProfileAttributes(attributes: List<AppMetricAttribute>) = Unit
            }
            model = ConnectTypeViewModel(GetFeatureConfigUseCase(repository), Analytics(emptyMap()), tracker)
            store.put("connect-type", model)
            model.setRouter(cicerone.router)
            model.handleFragmentArguments(Bundle().apply { putBoolean(IS_ON_BOARDING_ARGUMENT_NAME, onboarding) })
        }
        instrumentation.waitForIdleSync()
        try {
            test(model, commands, metrics)
        } finally {
            instrumentation.runOnMainSync {
                store.clear()
                cicerone.getNavigatorHolder().removeNavigator()
            }
        }
    }
}
