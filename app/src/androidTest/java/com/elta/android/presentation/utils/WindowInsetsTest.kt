package com.elta.android.presentation.utils

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.test.platform.app.InstrumentationRegistry
import android.graphics.Insets
import android.view.WindowInsets
import androidx.test.filters.SdkSuppress
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.FragmentAuthBaseBinding
import com.elta.android.presentation.databinding.FragmentEventFormBinding
import com.elta.android.presentation.databinding.FragmentEventsOptionsChooserBinding
import com.elta.android.presentation.databinding.FragmentGlucoseEventBinding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@SdkSuppress(minSdkVersion = 30)
class WindowInsetsTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun eventSaveButtonStaysAboveNavigationBar() = onMain {
        val binding = FragmentEventFormBinding.inflate(inflater())
        binding.formSaveButtonView.visibility = View.VISIBLE
        val offsets = mutableListOf<Int>()
        val listener = WindowBottomInsetsForViewListenerFactory.instance(
            binding.formSaveButtonView, binding.formContainerView
        ) { offsets.add(it) }
        binding.root.applyWindowBottomInsetsListener(listener)

        dispatch(binding.root, navigationBottom = 126)
        measure(binding.root)

        assertTrue(binding.formSaveButtonView.bottom <= HEIGHT - 126)
        assertEquals(listOf(0), offsets)
    }

    @Test
    fun eventChooserConfirmationStaysAboveNavigationBar() = onMain {
        val binding = FragmentEventsOptionsChooserBinding.inflate(inflater())
        binding.confirmButtonView.visibility = View.VISIBLE
        binding.root.applyWindowBottomInsetsListener(
            WindowBottomInsetsForViewListenerFactory.instance { }
        )
        dispatch(binding.root, navigationBottom = 126)
        measure(binding.root)
        val parent = binding.confirmButtonView.parent as View
        assertTrue(parent.top + binding.confirmButtonView.bottom <= HEIGHT - 126)
    }

    @Test
    fun glucoseEventSaveButtonStaysAboveNavigationBar() = onMain {
        val binding = FragmentGlucoseEventBinding.inflate(inflater())
        binding.formSaveButtonView.visibility = View.VISIBLE
        binding.root.applyWindowBottomInsetsListener(
            WindowBottomInsetsForViewListenerFactory.instance(
                binding.formSaveButtonView, binding.formContainerView
            ) { }
        )
        dispatch(binding.root, navigationBottom = 126)
        measure(binding.root)
        assertTrue(binding.formSaveButtonView.bottom <= HEIGHT - 126)
    }

    @Test
    fun keyboardAndNavigationInsetsDoNotAccumulateOrLoseOriginalMargins() = onMain {
        val root = FrameLayout(context).apply { setPadding(3, 7, 5, 11) }
        val button = View(context).apply {
            layoutParams = FrameLayout.LayoutParams(100, 100).apply { bottomMargin = 17 }
        }
        root.addView(button)
        val offsets = mutableListOf<Int>()
        root.applyWindowBottomInsetsListener(
            WindowBottomInsetsForViewListenerFactory.instance(button) { offsets.add(it) }
        )

        dispatch(root, navigationBottom = 126, imeBottom = 700)
        dispatch(root, navigationBottom = 126, imeBottom = 700)
        assertEquals(137, root.paddingBottom)
        assertEquals(591, (button.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin)
        dispatch(root, navigationBottom = 126)
        assertEquals(137, root.paddingBottom)
        assertEquals(17, (button.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin)
        assertEquals(listOf(574, 574, 0), offsets)
    }

    @Test
    fun landscapeNavigationBarProtectsTheSideOfTheForm() = onMain {
        val root = FrameLayout(context).apply { setPadding(3, 7, 5, 11) }
        root.applyWindowBottomInsetsListener(
            WindowBottomInsetsForViewListenerFactory.instance { }
        )
        dispatch(root, navigationRight = 126)
        assertEquals(131, root.paddingRight)
        assertEquals(3, root.paddingLeft)
        assertEquals(7, root.paddingTop)
        assertEquals(11, root.paddingBottom)
    }

    @Test
    fun loginButtonStaysAboveNavigationAndKeyboardAfterRepeatedInsets() = onMain {
        val binding = FragmentAuthBaseBinding.inflate(inflater())
        binding.root.applyStatusBarInsetsPadding(
            applyNavigationBarInset = true, applyImeInset = true
        )
        dispatch(binding.root, navigationBottom = 126)
        measure(binding.root)
        assertTrue(binding.continueButtonView.bottom <= HEIGHT - 126)

        dispatch(binding.root, navigationBottom = 126, imeBottom = 700)
        dispatch(binding.root, navigationBottom = 126, imeBottom = 700)
        measure(binding.root)
        assertTrue(binding.continueButtonView.bottom <= HEIGHT - 700)
        dispatch(binding.root, navigationBottom = 126)
        assertEquals(126, binding.root.paddingBottom)
    }

    @Test
    fun removingBottomListenerStopsCallbacks() = onMain {
        val root = FrameLayout(context)
        var callbacks = 0
        root.applyWindowBottomInsetsListener(
            WindowBottomInsetsForViewListenerFactory.instance { callbacks++ }
        )
        dispatch(root, navigationBottom = 126)
        root.removeWindowBottomInsetsListener()
        dispatch(root, navigationBottom = 126, imeBottom = 700)
        assertEquals(1, callbacks)
    }

    @Test
    fun loginUsesNavigationInsetEvenWhenImeIsSmaller() = onMain {
        val root = FrameLayout(context)
        root.applyStatusBarInsetsPadding(applyNavigationBarInset = true, applyImeInset = true)
        dispatch(root, navigationBottom = 126, navigationRight = 20, imeBottom = 50)
        assertEquals(126, root.paddingBottom)
        assertEquals(20, root.paddingRight)
    }

    private fun inflater() = LayoutInflater.from(
        ContextThemeWrapper(context, R.style.AppTheme)
    )

    private fun dispatch(
        root: View,
        navigationBottom: Int = 0,
        navigationRight: Int = 0,
        imeBottom: Int = 0
    ) {
        val insets = WindowInsets.Builder()
            .setInsets(WindowInsets.Type.statusBars(), Insets.of(0, 63, 0, 0))
            .setInsets(WindowInsets.Type.navigationBars(), Insets.of(0, 0, navigationRight, navigationBottom))
            .setInsets(WindowInsets.Type.ime(), Insets.of(0, 0, 0, imeBottom))
            .setVisible(WindowInsets.Type.ime(), imeBottom > 0)
            .build()
        root.dispatchApplyWindowInsets(insets)
    }

    private fun measure(root: View) {
        root.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(HEIGHT, View.MeasureSpec.EXACTLY)
        )
        root.layout(0, 0, 1080, HEIGHT)
    }

    private fun onMain(block: () -> Unit) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(block)
    }

    private companion object {
        const val HEIGHT = 2400
    }
}
