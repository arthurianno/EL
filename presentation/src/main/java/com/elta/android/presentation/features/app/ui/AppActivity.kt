package com.elta.android.presentation.features.app.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
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
    private val splashOverlay by lazy {
        findViewById<FrameLayout>(R.id.splashOverlay)
    }
    private var isOneSignalPermissionSynced = false

    private val rxPermissions by lazy { RxPermissions(this) }
    private val splashFallbackRunnable = Runnable {
        Log.w(TAG, "splashFallbackRunnable fired after timeout")
        hideSplashOverlay("fallback_timeout")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(
            TAG,
            "AppActivity.onCreate(savedInstanceState=${savedInstanceState != null}, localeDefault=${Locale.getDefault().language}, appLanguage=${LocaleHelper.getLanguage(this)})"
        )
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        if (LocaleHelper.consumePendingGreetingAfterLanguageSelection(this)) {
            Log.i(TAG, "AppActivity.onCreate: pending Greeting consumed, routing to GreetingFlow")
            router.newRootFlow(Screens.GreetingFlow)
        }

        if (LocaleHelper.consumePendingHomeAfterLanguageChange(this)) {
            Log.i(TAG, "AppActivity.onCreate: pending Home consumed, routing to HomeFlow")
            router.newRootScreen(Screens.HomeFlow)
        }

        if (savedInstanceState != null) {
            Log.i(TAG, "AppActivity.onCreate: restored activity, hide splash immediately")
            hideSplashOverlay("restored_activity")
        } else {
            Log.i(TAG, "AppActivity.onCreate: scheduling splash fallback 12000ms")
            splashOverlay.postDelayed(splashFallbackRunnable, 12_000L)
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
        if (BuildConfig.DEBUG) {
            rxPermissions.requestStatus(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe()
        }
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

        lifecycleScope.launch {
            runCatching {
                val accepted = OneSignal.Notifications.requestPermission(true)
                isOneSignalPermissionSynced = true
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
            hideSplashOverlay("images_loaded_command")
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "AppActivity.onDestroy: remove splash fallback callback")
        splashOverlay.removeCallbacks(splashFallbackRunnable)
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        DynamicLinkProcessor.from(intent)
            .deepLinkStartPassTo(presentationModel.deepLinkAction)
            .notificationStartPassTo(presentationModel.notificationStartAction)
            .build()
            .process()
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

    private fun hideSplashOverlay(reason: String) {
        Log.i(
            TAG,
            "hideSplashOverlay(reason=$reason, isVisible=${splashOverlay.visibility == View.VISIBLE}, alpha=${splashOverlay.alpha})"
        )
        splashOverlay.removeCallbacks(splashFallbackRunnable)
        if (splashOverlay.visibility != View.VISIBLE) {
            Log.i(TAG, "hideSplashOverlay: already hidden, skip")
            return
        }

        splashOverlay.animate()
            .alpha(0f)
            .setDuration(250)
            .withEndAction {
                splashOverlay.visibility = View.GONE
                Log.i(TAG, "hideSplashOverlay: animation complete, splash hidden")
            }
            .start()
    }
}
