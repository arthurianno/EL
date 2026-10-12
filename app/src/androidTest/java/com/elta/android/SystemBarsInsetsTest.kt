package com.elta.android

import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.View
import android.view.WindowInsets
import android.widget.TextView
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.features.app.ui.AppActivity
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.GlucoseInputType
import com.elta.android.presentation.widgets.picker.FormPicker
import net.cachapa.expandablelayout.ExpandableLayout
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

@SdkSuppress(minSdkVersion = 30)
class SystemBarsInsetsTest {
    private val composeRule = createEmptyComposeRule()
    private val activityRule = ActivityScenarioRule<AppActivity>(
        Intent(InstrumentationRegistry.getInstrumentation().targetContext, AppActivity::class.java)
            .putExtra("debug_open_how_to_connect", true)
    )

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(composeRule).around(activityRule)

    @Test
    fun scannerHintStaysAboveSystemNavigation() {
        activityRule.scenario.onActivity {
            it.router.newRootScreen(Screens.ScannerDmcScreen(isOnBoarding = false))
        }
        assertScannerHintIsSafe()
    }

    @Test
    fun variantAScannerHintStaysAboveSystemNavigation() {
        activityRule.scenario.onActivity {
            it.router.newRootScreen(Screens.ScannerDmcScreenVariantA(isOnBoarding = false))
        }
        assertScannerHintIsSafe()
    }

    @Test
    fun glucoseSaveButtonHasNoGapWithVisibleOrHiddenSyncStatus() {
        activityRule.scenario.onActivity {
            it.router.newRootScreen(Screens.EventsCreationScreen(EventType.Glucose(GlucoseInputType.MANUAL)))
        }
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val deadline = SystemClock.uptimeMillis() + 10_000
        var ready = false
        while (!ready && SystemClock.uptimeMillis() < deadline) {
            activityRule.scenario.onActivity { activity ->
                val button = activity.findViewById<View>(R.id.formSaveButtonView)
                if (button != null) {
                    activity.findViewById<FormPicker>(R.id.formPickerView).setValues(5, 0)
                    button.visibility = View.VISIBLE
                    val status = activity.findViewById<View>(R.id.syncStatusView)
                    status.findViewById<TextView>(R.id.statusTextView).setText(R.string.sync_with_backend_in_progress)
                    status.findViewById<View>(R.id.statusBackgroundView)
                        .setBackgroundColor(activity.getColor(R.color.color_background_backend_sync_started))
                    status.findViewById<ExpandableLayout>(R.id.statusExpandableLayout).expand(false)
                    ready = button.height > 0 && status.height > 0
                }
            }
            if (!ready) SystemClock.sleep(100)
        }
        assertTrue("Glucose form with sync status did not become visible", ready)
        instrumentation.waitForIdleSync()
        activityRule.scenario.onActivity { activity ->
            val button = activity.findViewById<View>(R.id.formSaveButtonView)
            val status = activity.findViewById<View>(R.id.syncStatusView)
            val buttonPosition = IntArray(2)
            val statusPosition = IntArray(2)
            button.getLocationInWindow(buttonPosition)
            status.getLocationInWindow(statusPosition)
            org.junit.Assert.assertEquals("There is a gap below Save", statusPosition[1], buttonPosition[1] + button.height)
            val decor = activity.window.decorView
            val safeBottom = decor.height - checkNotNull(decor.rootWindowInsets)
                .getInsets(WindowInsets.Type.navigationBars()).bottom
            org.junit.Assert.assertEquals("Sync background extends into system navigation", safeBottom, statusPosition[1] + status.height)
        }
        instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
            File(instrumentation.targetContext.externalCacheDir, "system-bars-form.png")
                .outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
        activityRule.scenario.onActivity { activity ->
            activity.findViewById<View>(R.id.syncStatusView)
                .findViewById<ExpandableLayout>(R.id.statusExpandableLayout).collapse(false)
        }
        instrumentation.waitForIdleSync()
        activityRule.scenario.onActivity { activity ->
            val button = activity.findViewById<View>(R.id.formSaveButtonView)
            val position = IntArray(2)
            button.getLocationInWindow(position)
            val decor = activity.window.decorView
            val safeBottom = decor.height - checkNotNull(decor.rootWindowInsets)
                .getInsets(WindowInsets.Type.navigationBars()).bottom
            org.junit.Assert.assertEquals("There is a gap after sync status hides", safeBottom, position[1] + button.height)
        }
    }

    private fun assertScannerHintIsSafe() {
        var hint = ""
        var safeBottom = 0
        activityRule.scenario.onActivity { activity ->
            hint = activity.getString(R.string.sync_connection_scanner_sheet_info_text)
            val decor = activity.window.decorView
            val navigation = checkNotNull(decor.rootWindowInsets)
                .getInsets(WindowInsets.Type.navigationBars())
            assertTrue("Test requires a visible bottom navigation bar", navigation.bottom > 0)
            safeBottom = decor.height - navigation.bottom
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(hint).fetchSemanticsNodes().any { node ->
                val bounds = node.boundsInWindow
                bounds.height > 0 && bounds.bottom <= safeBottom
            }
        }
    }
}
