package com.elta.android.presentation.features.main.records.ui.compose

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.airbnb.lottie.LottieAnimationView
import com.elta.android.presentation.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Truly Edge-to-Edge Fullscreen Animated transition overlay displayed when user taps the mini glucose chart card.
 * Overlays the entire screen (with seamless status bar and navigation bar coloring) and plays
 * the Lottie phone turn animation at a smooth pace before seamlessly fading out into the detailed chart.
 */
@Composable
fun GlucoseChartTransitionOverlay(
    isDarkTheme: Boolean = false,
    onAnimationFinished: () -> Unit
) {
    val overlayBg = if (isDarkTheme) {
        GlucoseDashboardTheme.DarkBackground
    } else {
        Color.White
    }

    val overlayAlpha = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var isDismissing by remember { mutableStateOf(false) }

    fun finishTransition() {
        if (isDismissing) return
        isDismissing = true
        coroutineScope.launch {
            overlayAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
            )
            onAnimationFinished()
        }
    }

    LaunchedEffect(Unit) {
        launch {
            overlayAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
            )
        }
        // Safety timeout
        launch {
            delay(3200)
            finishTransition()
        }
    }

    Dialog(
        onDismissRequest = { finishTransition() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false
        )
    ) {
        val view = LocalView.current
        SideEffect {
            (view.parent as? DialogWindowProvider)?.window?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                val bgColor = if (isDarkTheme) 0xFF3D4556.toInt() else android.graphics.Color.WHITE
                window.setBackgroundDrawable(ColorDrawable(bgColor))

                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = bgColor
                window.navigationBarColor = bgColor

                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = !isDarkTheme
                controller.isAppearanceLightNavigationBars = !isDarkTheme

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    window.attributes.layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayBg)
                .graphicsLayer {
                    alpha = overlayAlpha.value
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { finishTransition() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AndroidView(
                    modifier = Modifier.size(240.dp),
                    factory = { context ->
                        LottieAnimationView(context).apply {
                            setAnimation(R.raw.turn_the_phone)
                            setMinAndMaxFrame(0, 72)
                            speed = 0.85f
                            repeatCount = 0
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            addAnimatorListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    finishTransition()
                                }
                            })
                            playAnimation()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Поверните\nваш телефон",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.9f) else Color(0xFF5C6479),
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }
        }
    }
}
