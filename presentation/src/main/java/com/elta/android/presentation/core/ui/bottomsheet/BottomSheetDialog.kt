package com.elta.android.presentation.core.ui.bottomsheet

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.elta.android.presentation.R
import com.google.android.material.bottomsheet.BottomSheetBehavior

/**
 * Base class for [android.app.Dialog]s styled as a bottom sheet.
 */
class BottomSheetDialog(context: Context, @StyleRes theme: Int = 0) : AppCompatDialog(
    context,
    getThemeResId(context, theme)
) {
    private var behavior: BottomSheetBehavior<FrameLayout>? = null
    private val handler = Handler()
    private val startDelay = 0L
    private var cancelable = true
    private var canceledOnTouchOutside = true
    private var canceledOnTouchOutsideSet = false
    private var initialState = BottomSheetBehavior.STATE_EXPANDED
    private val color1 = Color.TRANSPARENT
    private val color2 = Color.parseColor("#B3000000")
    private var inAnimator: ObjectAnimator? = null
    private var outAnimator: ObjectAnimator? = null
    private val colorEvaluator: TypeEvaluator<*> = ArgbEvaluator()
    private val inColorAnimationDuration: Long = 400
    private val outColorAnimationDuration: Long = 300
    private var isAnimationFinished = false
    private var isDismissing = false

    init {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setOnShowListener()
    }

    override fun setContentView(@LayoutRes layoutResId: Int) {
        super.setContentView(wrapInBottomSheet(layoutResId, null, null))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.let { window ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val view = window.decorView
            inAnimator = ObjectAnimator.ofInt(view, "backgroundColor", color1, color2).apply {
                setEvaluator(colorEvaluator)
                duration = inColorAnimationDuration
            }
            outAnimator = ObjectAnimator.ofInt(view, "backgroundColor", color2, color1).apply {
                setEvaluator(colorEvaluator)
                duration = outColorAnimationDuration
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        isAnimationFinished = true
                        checkAndDismiss()
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        isAnimationFinished = true
                        checkAndDismiss()
                    }
                })
                animateIn()
            }
        }
    }

    override fun setContentView(view: View) {
        super.setContentView(wrapInBottomSheet(0, view, null))
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        super.setContentView(wrapInBottomSheet(0, view, params))
    }

    override fun setCancelable(cancelable: Boolean) {
        super.setCancelable(cancelable)
        if (this.cancelable != cancelable) {
            this.cancelable = cancelable
            behavior?.isHideable = cancelable
        }
    }

    override fun onStart() {
        super.onStart()
        if (behavior?.state == BottomSheetBehavior.STATE_HIDDEN) {
            behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        super.setCanceledOnTouchOutside(cancel)
        if (cancel && !cancelable) {
            cancelable = true
        }
        canceledOnTouchOutside = cancel
        canceledOnTouchOutsideSet = true
    }

    override fun dismiss() {
        closeSmooth()
    }

    fun getBehavior(): BottomSheetBehavior<FrameLayout> {
        return behavior!!
    }

    fun setInitialState(initialState: Int) {
        this.initialState = initialState
    }

    fun closeSmooth() {
        animateOut()
        behavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun wrapInBottomSheet(
        layoutResId: Int,
        view: View?,
        params: ViewGroup.LayoutParams?
    ): View {
        var mView = view
        val container: FrameLayout =
            View.inflate(context, R.layout.design_bottom_sheet_dialog, null) as FrameLayout
        val coordinator: CoordinatorLayout =
            container.findViewById<CoordinatorLayout>(R.id.coordinator)
        if (layoutResId != 0 && mView == null) {
            mView = layoutInflater.inflate(layoutResId, coordinator, false)
        }
        val bottomSheet: FrameLayout =
            coordinator.findViewById(R.id.design_bottom_sheet)
        behavior = BottomSheetBehavior.from(bottomSheet).apply {
            addBottomSheetCallback(bottomSheetCallback)
            isHideable = cancelable
            setPeekHeight(0)
        }
        if (params == null) {
            bottomSheet.addView(mView)
        } else {
            bottomSheet.addView(mView, params)
        }
        // We treat the CoordinatorLayout as outside the dialog though it is technically inside
        coordinator
            .findViewById<View>(R.id.touch_outside)
            .setOnClickListener {
                if (cancelable && isShowing && shouldWindowCloseOnTouchOutside()) {
                    cancel()
                }
            }
        // Handle accessibility events
        ViewCompat.setAccessibilityDelegate(
            bottomSheet,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(
                    host: View,
                    info: AccessibilityNodeInfoCompat
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    if (cancelable) {
                        info.addAction(AccessibilityNodeInfoCompat.ACTION_DISMISS)
                        info.isDismissable = true
                    } else {
                        info.isDismissable = false
                    }
                }

                override fun performAccessibilityAction(
                    host: View,
                    action: Int,
                    args: Bundle?
                ): Boolean {
                    if (action == AccessibilityNodeInfoCompat.ACTION_DISMISS && cancelable) {
                        cancel()
                        return true
                    }
                    return super.performAccessibilityAction(host, action, args)
                }
            }
        )
        bottomSheet.setOnTouchListener { _, _ ->
            true
        }
        return container
    }

    fun shouldWindowCloseOnTouchOutside(): Boolean {
        if (!canceledOnTouchOutsideSet) {
            val a: TypedArray = context
                .obtainStyledAttributes(intArrayOf(android.R.attr.windowCloseOnTouchOutside))
            canceledOnTouchOutside = a.getBoolean(0, true)
            a.recycle()
            canceledOnTouchOutsideSet = true
        }
        return canceledOnTouchOutside
    }

    private fun setOnShowListener() {
        setOnShowListener {
            if (behavior?.state != initialState) {
                handler.postDelayed({ behavior?.setState(initialState) }, startDelay)
            }
        }
    }

    private fun animateIn() {
        inAnimator?.start()
    }

    private fun animateOut() {
        if (!isDismissing && outAnimator?.isStarted == false) {
            outAnimator?.start()
        }
    }

    private fun checkAndDismiss() {
        val newState = behavior?.state
        if ((newState == BottomSheetBehavior.STATE_HIDDEN || newState == BottomSheetBehavior.STATE_COLLAPSED) &&
            isAnimationFinished &&
            !isDismissing
        ) {
            isDismissing = true
            super@BottomSheetDialog.dismiss()
        }
    }

    private val bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback =
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(var1: View, var2: Int) {
                if (var2 == BottomSheetBehavior.STATE_HIDDEN || var2 == BottomSheetBehavior.STATE_COLLAPSED) {
                    checkAndDismiss()
                    animateOut()
                }
            }

            override fun onSlide(p0: View, p1: Float) {}
        }

    companion object {
        private fun getThemeResId(context: Context, themeId: Int): Int {
            var mThemeId = themeId
            if (mThemeId == 0) {
                // If the provided theme is 0, then retrieve the dialogTheme from our theme
                val outValue = TypedValue()
                mThemeId = if (context.theme.resolveAttribute(
                        R.attr.bottomSheetDialogTheme,
                        outValue,
                        true
                    )
                ) {
                    outValue.resourceId
                } else {
                    // bottomSheetDialogTheme is not provided; we default to our light theme
                    R.style.Theme_Design_Light_BottomSheetDialog
                }
            }
            return mThemeId
        }
    }
}
