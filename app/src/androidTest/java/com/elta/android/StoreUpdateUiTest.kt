package com.elta.android

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.View
import android.view.WindowInsets
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.lifecycle.Lifecycle
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.elta.android.domain.features.version.model.AppUpdateOffer
import com.elta.android.domain.features.version.model.AppUpdateStore
import com.elta.android.presentation.R
import com.elta.android.presentation.features.app.ui.AppActivity
import com.elta.android.presentation.features.version.openAppUpdateOffer
import com.elta.android.presentation.features.version.optional.ui.OptionalUpdateDialogFragment
import com.nullgr.core.preferences.defaultPrefs
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@SdkSuppress(minSdkVersion = 30)
class StoreUpdateUiTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val preferences get() = defaultPrefs(instrumentation.targetContext)
    private var originalPromptTime: Long? = null

    @get:Rule
    val activityRule = ActivityScenarioRule<AppActivity>(
        Intent(InstrumentationRegistry.getInstrumentation().targetContext, AppActivity::class.java)
            .putExtra("debug_open_how_to_connect", true)
    )

    @Before
    fun rememberPromptTime() {
        originalPromptTime = if (preferences.contains(PROMPT_TIME_KEY)) preferences.getLong(PROMPT_TIME_KEY, 0) else null
    }

    @After
    fun restorePromptTime() {
        activityRule.scenario.close()
        preferences.edit().apply {
            originalPromptTime?.let { putLong(PROMPT_TIME_KEY, it) } ?: remove(PROMPT_TIME_KEY)
        }.commit()
    }

    @Test
    fun updateButtonStaysAboveNavigationAndRotationKeepsTheOriginalPromptTime() {
        showDialog(listOf(GOOGLE_OFFER))
        val recordedTime = preferences.getLong(PROMPT_TIME_KEY, 0)
        assertTrue(recordedTime > 0)
        assertUpdateButtonIsSafe()
        saveScreenshot("optional-update-prompt.png")
        activityRule.scenario.recreate()
        waitForDialog()
        assertEquals(recordedTime, preferences.getLong(PROMPT_TIME_KEY, 0))
        assertUpdateButtonIsSafe()
    }

    @Test
    fun unknownInstallerCanChooseBetweenGooglePlayAndRuStore() {
        showDialog(listOf(GOOGLE_OFFER, RUSTORE_OFFER))
        assertUpdateButtonIsSafe()
        activityRule.scenario.onActivity { activity ->
            val dialog = activity.supportFragmentManager.findFragmentByTag(DIALOG_TAG) as OptionalUpdateDialogFragment
            checkNotNull(dialog.view).findViewById<View>(R.id.updateActionButtonView).performClick()
        }
        onView(withText("Google Play")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("RuStore")).inRoot(isDialog()).check(matches(isDisplayed()))
        saveScreenshot("optional-update-stores.png")
    }

    @Test
    fun storeNavigationTargetsTheSelectedStoreAndFallsBackToTheSameWebPage() {
        val installed = RecordingContext(instrumentation.targetContext, failStore = false)
        assertTrue(installed.openAppUpdateOffer(RUSTORE_OFFER))
        assertEquals("ru.vk.store", installed.intents.single().`package`)
        assertEquals(RUSTORE_OFFER.url, installed.intents.single().dataString)
        val missing = RecordingContext(instrumentation.targetContext, failStore = true)
        assertTrue(missing.openAppUpdateOffer(GOOGLE_OFFER))
        assertEquals("com.android.vending", missing.intents.first().`package`)
        assertEquals(null, missing.intents.last().`package`)
        assertEquals(GOOGLE_OFFER.url, missing.intents.last().dataString)
    }

    @Test
    fun previewDoesNotRecordTheThreeDayPromptTimer() {
        val before = preferences.getLong(PROMPT_TIME_KEY, 0)
        activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        instrumentation.waitForIdleSync()
        activityRule.scenario.onActivity {
            OptionalUpdateDialogFragment.newPreviewInstance().showNow(it.supportFragmentManager, DIALOG_TAG)
        }
        waitForDialog()
        assertUpdateButtonIsSafe()
        assertEquals(before, preferences.getLong(PROMPT_TIME_KEY, 0))
    }

    private fun showDialog(updates: List<AppUpdateOffer>) {
        activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        val deadline = SystemClock.uptimeMillis() + 10_000
        var resumed = false
        while (!resumed && SystemClock.uptimeMillis() < deadline) {
            instrumentation.waitForIdleSync()
            activityRule.scenario.onActivity {
                resumed = it.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) &&
                    !it.supportFragmentManager.isStateSaved
            }
            if (!resumed) SystemClock.sleep(100)
        }
        assertTrue("Activity must be resumed before showing an update dialog", resumed)
        activityRule.scenario.onActivity {
            OptionalUpdateDialogFragment.newInstance(updates).showNow(it.supportFragmentManager, DIALOG_TAG)
        }
        waitForDialog()
    }

    private fun waitForDialog() {
        val deadline = SystemClock.uptimeMillis() + 10_000
        var ready = false
        while (!ready && SystemClock.uptimeMillis() < deadline) {
            activityRule.scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentByTag(DIALOG_TAG) as? OptionalUpdateDialogFragment
                val button = fragment?.view?.findViewById<View>(R.id.updateActionButtonView)
                ready = fragment?.isResumed == true && button?.isShown == true && button.height > 0
            }
            if (!ready) SystemClock.sleep(100)
        }
        assertTrue("Update dialog did not become visible", ready)
        instrumentation.waitForIdleSync()
    }

    private fun assertUpdateButtonIsSafe() {
        val deadline = SystemClock.uptimeMillis() + 3_000
        var safe = false
        var details = "Update button is missing"
        while (!safe && SystemClock.uptimeMillis() < deadline) {
            activityRule.scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentByTag(DIALOG_TAG) as OptionalUpdateDialogFragment
                val button = checkNotNull(fragment.view).findViewById<View>(R.id.updateActionButtonView)
                val decor = checkNotNull(checkNotNull(fragment.dialog).window).decorView
                val bottomInset = checkNotNull(decor.rootWindowInsets).getInsets(WindowInsets.Type.navigationBars()).bottom
                val buttonPosition = IntArray(2)
                val decorPosition = IntArray(2)
                button.getLocationOnScreen(buttonPosition)
                decor.getLocationOnScreen(decorPosition)
                val safeBottom = decorPosition[1] + decor.height - bottomInset
                safe = button.isShown && buttonPosition[1] + button.height <= safeBottom
                details = "Update button bottom=${buttonPosition[1] + button.height}, safe bottom=$safeBottom"
            }
            if (!safe) SystemClock.sleep(100)
        }
        assertTrue(details, safe)
    }

    private fun saveScreenshot(name: String) {
        instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
            File(instrumentation.targetContext.externalCacheDir, name).outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            bitmap.recycle()
        }
    }

    private class RecordingContext(context: Context, private val failStore: Boolean) : ContextWrapper(context) {
        val intents = mutableListOf<Intent>()
        override fun startActivity(intent: Intent) {
            intents += Intent(intent)
            if (failStore && intent.`package` != null) throw ActivityNotFoundException()
        }
    }

    private companion object {
        const val DIALOG_TAG = "optional_update_test"
        const val PROMPT_TIME_KEY = "optional_store_update_prompt_time_ms"
        val GOOGLE_OFFER = AppUpdateOffer(AppUpdateStore.GOOGLE_PLAY, 420,
            "https://play.google.com/store/apps/details?id=com.elta.android")
        val RUSTORE_OFFER = AppUpdateOffer(AppUpdateStore.RUSTORE, 421,
            "https://www.rustore.ru/catalog/app/com.elta.android")
    }
}
