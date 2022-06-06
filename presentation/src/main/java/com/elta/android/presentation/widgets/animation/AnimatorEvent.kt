package com.elta.android.presentation.widgets.animation

import android.animation.Animator

sealed class AnimatorEvent(val animator: Animator) {
    class Cancel(animator: Animator) : AnimatorEvent(animator)
    class End(animator: Animator) : AnimatorEvent(animator)
    class Repeat(animator: Animator) : AnimatorEvent(animator)
    class Start(animator: Animator) : AnimatorEvent(animator)
    class Pause(animator: Animator) : AnimatorEvent(animator)
    class Resume(animator: Animator) : AnimatorEvent(animator)
}
