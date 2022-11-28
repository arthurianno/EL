package com.elta.android.presentation.features.app.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.elta.android.presentation.BuildConfig
import com.elta.android.presentation.R
import com.elta.android.presentation.core.permissions.requestStatus
import com.elta.android.presentation.core.ui.activity.BaseActivity
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.databinding.ActivityAppBinding
import com.elta.android.presentation.features.app.pm.AppPm
import com.elta.android.presentation.features.sync.control.checkBluetoothPermissions
import com.elta.android.presentation.utils.dynamiclinks.DynamicLinkProcessor
import com.elta.android.presentation.utils.keyboard.KeyboardEventListener
import com.elta.android.presentation.widgets.TwoStateStatusView
import com.elta.android.presentation.widgets.status.StatusView
import com.tbruyelle.rxpermissions2.RxPermissions
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.passTo

class AppActivity : BaseActivity<AppPm>() {
    override val screenLayout: Int = R.layout.activity_app
    override val classToken: Class<AppPm> = AppPm::class.java

    override val binding by lazy { ActivityAppBinding.inflate(layoutInflater) }
    private val statusView by lazy {
        findViewById<StatusView>(R.id.syncStatusView)
    }
    private val connectionStatusView by lazy {
        findViewById<TwoStateStatusView>(R.id.connectionStatusView)
    }

    private val rxPermissions by lazy { RxPermissions(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        DynamicLinkProcessor.from(intent)
            .ignoreColdStart(false)
            .withSavedState(savedInstanceState)
            .coldStartPassTo(presentationModel.coldStartAction)
            .deepLinkStartPassTo(presentationModel.deepLinkAction)
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
        KeyboardEventListener(this) { isKeyboardOpen ->
            connectionStatusView.isVisible = !isKeyboardOpen
        }
        checkBluetoothPermissions(this)
    }

    override fun onBindPresentationModel(pm: AppPm) {
        super.onBindPresentationModel(pm)
        pm.networkStateCommand.bindTo(connectionStatusView.changeState())
        pm.syncStatusVisibility.bindTo(statusView.visibleChanges())
        pm.syncStatusState.bindTo(statusView.statusChanges())
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        DynamicLinkProcessor.from(intent)
            .deepLinkStartPassTo(presentationModel.deepLinkAction)
            .notificationStartPassTo(presentationModel.notificationStartAction)
            .build()
            .process()
    }

    override fun onStop() {
        super.onStop()
        findLastNestedFragmentAndSendEvent(currentFragment)
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
