package com.elta.android.presentation.features.app.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.presentation.BuildConfig
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.permissions.requestStatus
import com.elta.android.presentation.core.ui.activity.BaseActivity
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.databinding.ActivityAppBinding
import com.elta.android.presentation.features.app.pm.AppPm
import com.elta.android.presentation.features.sync.control.checkPermissions
import com.elta.android.presentation.features.sync.control.checkPermissionsVariantA
import com.elta.android.presentation.utils.LocaleHelper
import com.elta.android.presentation.features.version.optional.ui.OptionalUpdateDialogFragment
import com.elta.android.presentation.utils.dynamiclinks.DynamicLinkProcessor
import com.elta.android.presentation.utils.keyboard.KeyboardEventListener
import com.elta.android.presentation.widgets.TwoStateStatusView
import com.elta.android.presentation.widgets.status.StatusView
import com.nullgr.core.ui.fragments.showDialog
import com.onesignal.OneSignal
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.coroutines.launch
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.passTo
import javax.inject.Inject
import java.util.Locale

class AppActivity : BaseActivity<AppPm>() {
    companion object {
       // const val OPEN_CONSULTANT_CHAT = "open_consultant_chat"
        private const val TAG = "LangFlow"
        private const val DEBUG_OPEN_HOW_TO_CONNECT = "debug_open_how_to_connect"
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
    private var isOneSignalPermissionSynced = false

    private val rxPermissions by lazy { RxPermissions(this) }
    private val splashFallbackRunnable = Runnable {
        Log.w(TAG, "splashFallbackRunnable fired after timeout")
        isAppReady = true
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

        splashScreen.setKeepOnScreenCondition {
            !isAppReady
        }

        if (openDebugHowToConnectIfRequested(intent)) return

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

        // fixme Variant A : improved_enabling_location
        val improvedEnablingLocation = getFeatureConfigUseCase.invoke().improvedEnablingLocation
        if (improvedEnablingLocation) checkPermissions(this)
        else checkPermissionsVariantA(this)
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
        syncOneSignalPermissionOnce()
        KeyboardEventListener(this) { isKeyboardOpen ->
            connectionStatusView.isVisible = !isKeyboardOpen
        }
    }

    private fun syncOneSignalPermissionOnce() {
        if (isOneSignalPermissionSynced) return

        if (OneSignal.Notifications.permission) {
            isOneSignalPermissionSynced = true
            return
        }

        isOneSignalPermissionSynced = true
        lifecycleScope.launch {
            runCatching {
                val accepted = OneSignal.Notifications.requestPermission(false)
                Log.i("OneSignal", "Permission sync from AppActivity accepted=$accepted")
            }.onFailure {
                Log.e("OneSignal", "Permission sync from AppActivity failed: ${it.message}")
            }
        }
    }

    override fun onBindPresentationModel(pm: AppPm) {
        super.onBindPresentationModel(pm)
        pm.networkStateCommand.bindTo(connectionStatusView.changeState())
        pm.syncStatusVisibility.bindTo(statusView.visibleChanges())
        pm.syncStatusState.bindTo(statusView.statusChanges())
        pm.showOptionalUpdateDialogCommand.bindTo {
            supportFragmentManager.showDialog(OptionalUpdateDialogFragment.newInstance())
        }
        pm.imagesLoadedCommand.bindTo {
            Log.i(TAG, "AppActivity: imagesLoadedCommand received")
            isAppReady = true
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "AppActivity.onDestroy: remove splash fallback callback")
        window.decorView.removeCallbacks(splashFallbackRunnable)
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
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
