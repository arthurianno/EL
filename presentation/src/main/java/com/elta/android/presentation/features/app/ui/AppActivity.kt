package com.elta.android.presentation.features.app.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.fragment.app.DialogFragment
import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.presentation.BuildConfig
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.permissions.requestStatus
import com.elta.android.presentation.core.ui.activity.BaseActivity
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.databinding.ActivityAppBinding
import com.elta.android.presentation.features.app.pm.AppPm
import com.elta.android.presentation.utils.LocaleHelper
import com.elta.android.presentation.utils.applyNavigationBarInsetsPadding
import com.elta.android.presentation.features.version.optional.ui.OptionalUpdateDialogFragment
import com.elta.android.presentation.utils.dynamiclinks.DynamicLinkProcessor
import com.elta.android.presentation.utils.keyboard.KeyboardEventListener
import com.elta.android.presentation.widgets.TwoStateStatusView
import com.elta.android.presentation.widgets.status.StatusView
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.passTo
import javax.inject.Inject
import java.util.Locale

class AppActivity : BaseActivity<AppPm>() {
    companion object {
       // const val OPEN_CONSULTANT_CHAT = "open_consultant_chat"
        private const val TAG = "LangFlow"
        private const val DEBUG_OPEN_HOW_TO_CONNECT = "debug_open_how_to_connect"
        private const val UPDATE_DIALOG_TAG = "optional_app_update"
        private const val DEBUG_PREVIEW_APP_UPDATE = "debug_preview_app_update"
    }

    @Inject
    lateinit var getFeatureConfigUseCase: GetFeatureConfigUseCase

    override val screenLayout: Int = R.layout.activity_app
    override val classToken: Class<AppPm> = AppPm::class.java

    override val binding by lazy { ActivityAppBinding.inflate(layoutInflater) }
    private val statusView by lazy {
        findViewById<StatusView>(R.id.syncStatusView)
    }
    private val connectionStatusView by lazy {
        findViewById<TwoStateStatusView>(R.id.connectionStatusView)
    }
    private var isAppReady = false
    private var updatePreviewRequested = false

    private val splashFallbackRunnable = Runnable {
        Log.w(TAG, "splashFallbackRunnable fired after timeout")
        isAppReady = true
        notifyUpdateUiReady()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        Log.i(
            TAG,
            "AppActivity.onCreate(savedInstanceState=${savedInstanceState != null}, localeDefault=${Locale.getDefault().language}, appLanguage=${LocaleHelper.getLanguage(this)})"
        )
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR  // тёмные иконки nav bar на светлой теме
        super.onCreate(savedInstanceState)

        val content = findViewById<View>(android.R.id.content)
        content.setBackgroundResource(R.color.color_window_background)
        content.applyNavigationBarInsetsPadding()

        supportFragmentManager.setFragmentResultListener(
            OptionalUpdateDialogFragment.RESULT_KEY, this
        ) { _, result ->
            presentationModel.optionalUpdateDialogVisibilityAction.consumer.accept(
                result.getBoolean(OptionalUpdateDialogFragment.RESULT_VISIBLE)
            )
        }

        splashScreen.setKeepOnScreenCondition {
            !isAppReady
        }

        if (openDebugUpdatePreviewIfRequested(intent) || openDebugHowToConnectIfRequested(intent)) return

        val shouldOpenGreetingAfterLanguageSelection =
            LocaleHelper.consumePendingGreetingAfterLanguageSelection(this)
        val shouldOpenHomeAfterLanguageChange =
            !shouldOpenGreetingAfterLanguageSelection &&
                LocaleHelper.consumePendingHomeAfterLanguageChange(this)

        when {
            shouldOpenGreetingAfterLanguageSelection -> {
                Log.i(TAG, "AppActivity.onCreate: continue to GreetingFlow after language selection")
                router.newRootFlow(Screens.GreetingFlow)
            }

            shouldOpenHomeAfterLanguageChange -> {
                val homeFlow = if (getFeatureConfigUseCase.invoke().improvedEnablingLocation) {
                    Screens.HomeFlow
                } else {
                    Screens.HomeFlowVariantA
                }
                Log.i(TAG, "AppActivity.onCreate: continue to ${homeFlow::class.java.simpleName} after language change")
                router.newRootFlow(homeFlow)
            }
        }

        if (savedInstanceState != null) {
            Log.i(TAG, "AppActivity.onCreate: restored activity, hide splash immediately")
            isAppReady = true
        } else {
            Log.i(TAG, "AppActivity.onCreate: scheduling splash fallback 12000ms")
            window.decorView.postDelayed(splashFallbackRunnable, 12_000L)
        }

        DynamicLinkProcessor.from(intent)
            .ignoreColdStart(false)
            .withSavedState(savedInstanceState)
            .coldStartPassTo(presentationModel.coldStartAction)
            .deepLinkStartPassTo(presentationModel.deepLinkAction)
            .consultantDeeplink(presentationModel.consultantDeepLinkAction)
            .newsDeeplink(presentationModel.newsDeepLinkAction)
            .coldStartByDeepLinkPassTo(presentationModel.coldStartDeepLinkAction)
            .notificationStartPassTo(presentationModel.notificationStartAction)
            .build()
            .process()
    }

    override fun onResume() {
        super.onResume()
        Log.i(
            TAG,
            "AppActivity.onResume(localeDefault=${Locale.getDefault().language}, appLanguage=${LocaleHelper.getLanguage(this)})"
        )
        KeyboardEventListener(this) { isKeyboardOpen ->
            connectionStatusView.isVisible = !isKeyboardOpen
        }
    }

    override fun onBindPresentationModel(pm: AppPm) {
        super.onBindPresentationModel(pm)
        pm.networkStateCommand.bindTo(connectionStatusView.changeState())
        pm.syncStatusVisibility.bindTo(statusView.visibleChanges())
        pm.syncStatusState.bindTo(statusView.statusChanges())
        pm.closeOptionalUpdateDialogCommand.bindTo {
            val dialog = supportFragmentManager.findFragmentByTag(UPDATE_DIALOG_TAG) as? DialogFragment
            if (supportFragmentManager.isStateSaved) dialog?.dismissAllowingStateLoss()
            else dialog?.dismiss()
        }
        pm.showOptionalUpdateDialogCommand.bindTo { updates ->
            val manager = supportFragmentManager
            when {
                manager.findFragmentByTag(UPDATE_DIALOG_TAG) != null ->
                    pm.optionalUpdateDialogVisibilityAction.consumer.accept(true)
                manager.isStateSaved || !lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) ||
                    manager.fragments.any { it is DialogFragment && it.isVisible } ->
                    pm.optionalUpdateDialogVisibilityAction.consumer.accept(false)
                else -> OptionalUpdateDialogFragment.newInstance(updates).show(manager, UPDATE_DIALOG_TAG)
            }
        }
        pm.imagesLoadedCommand.bindTo {
            Log.i(TAG, "AppActivity: imagesLoadedCommand received")
            isAppReady = true
            notifyUpdateUiReady()
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        window.decorView.post {
            notifyUpdateUiReady()
            showPendingUpdatePreview()
        }
    }

    override fun onPause() {
        presentationModel.optionalUpdateUiReadyAction.consumer.accept(false)
        super.onPause()
    }

    private fun notifyUpdateUiReady() {
        presentationModel.optionalUpdateUiReadyAction.consumer.accept(
            isAppReady && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        )
    }

    override fun onDestroy() {
        Log.i(TAG, "AppActivity.onDestroy: remove splash fallback callback")
        window.decorView.removeCallbacks(splashFallbackRunnable)
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (openDebugUpdatePreviewIfRequested(intent)) {
            showPendingUpdatePreview()
            return
        }
        if (openDebugHowToConnectIfRequested(intent)) return

        DynamicLinkProcessor.from(intent)
            .deepLinkStartPassTo(presentationModel.deepLinkAction)
            .consultantDeeplink(presentationModel.consultantDeepLinkAction)
            .newsDeeplink(presentationModel.newsDeepLinkAction)
            .notificationStartPassTo(presentationModel.notificationStartAction)
            .build()
            .process()
    }

    private fun openDebugHowToConnectIfRequested(intent: Intent?): Boolean {
        if (!BuildConfig.DEBUG) return false
        if (intent?.getBooleanExtra(DEBUG_OPEN_HOW_TO_CONNECT, false) != true) return false

        Log.i(TAG, "AppActivity: opening HowToConnect via debug hook")
        val screen = if (getFeatureConfigUseCase.invoke().improvedEnablingLocation) {
            Screens.HowToConnectScreen(isOnBoarding = false)
        } else {
            Screens.HowToConnectScreenVariantA(isOnBoarding = false)
        }
        router.newRootScreen(screen)
        isAppReady = true
        return true
    }

    private fun openDebugUpdatePreviewIfRequested(intent: Intent?): Boolean {
        if (!BuildConfig.DEBUG || intent?.getBooleanExtra(DEBUG_PREVIEW_APP_UPDATE, false) != true) return false
        isAppReady = true
        updatePreviewRequested = true
        router.newRootFlow(Screens.GreetingFlow)
        return true
    }

    private fun showPendingUpdatePreview() {
        if (!updatePreviewRequested || supportFragmentManager.isStateSaved ||
            !lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        ) return
        updatePreviewRequested = false
        if (supportFragmentManager.findFragmentByTag(UPDATE_DIALOG_TAG) == null) {
            OptionalUpdateDialogFragment.newPreviewInstance().show(supportFragmentManager, UPDATE_DIALOG_TAG)
        }
    }

    override fun onStop() {
        presentationModel.uploadLogs()
        findLastNestedFragmentAndSendEvent(currentFragment)
        super.onStop()
    }

    private tailrec fun findLastNestedFragmentAndSendEvent(parentFragment: BaseFragment<*, *>?) {
        val nestedFragment = parentFragment
            ?.childFragmentManager
            ?.findFragmentById(R.id.containerView)
                as? BaseFragment<*, *>

        if (nestedFragment == null) {
            parentFragment?.javaClass?.simpleName?.passTo(presentationModel.onStopAction)
        } else {
            findLastNestedFragmentAndSendEvent(nestedFragment)
        }
    }


}
