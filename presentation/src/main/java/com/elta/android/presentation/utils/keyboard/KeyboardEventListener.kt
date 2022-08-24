package com.elta.android.presentation.utils.keyboard

import android.view.ViewTreeObserver
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.elta.android.presentation.R
import com.elta.android.presentation.utils.isKeyboardOpen

class KeyboardEventListener(
    private val activity: AppCompatActivity,
    private val callback: (isOpen: Boolean) -> Unit
) : LifecycleObserver {
    private val rootView = activity.findViewById<CoordinatorLayout>(R.id.containerView)

    private val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
        private var lastState: Boolean = activity.isKeyboardOpen(rootView)

        override fun onGlobalLayout() {
            val isOpen = activity.isKeyboardOpen(rootView)
            if (isOpen == lastState) {
                return
            } else {
                callback(isOpen)
                lastState = isOpen
            }
        }
    }

    init {
        callback(activity.isKeyboardOpen(rootView))
        activity.lifecycle.addObserver(this)
        registerKeyboardListener()
    }

    private fun registerKeyboardListener() {
        rootView.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_PAUSE)
    @CallSuper
    fun onLifecyclePause() {
        unregisterKeyboardListener()
    }

    private fun unregisterKeyboardListener() {
        rootView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }
}
