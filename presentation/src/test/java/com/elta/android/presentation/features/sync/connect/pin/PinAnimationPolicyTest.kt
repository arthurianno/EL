package com.elta.android.presentation.features.sync.connect.pin

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinAnimationPolicyTest {
    @Test
    fun `only waiting states repeat and instruction plays once`() {
        val waiting = setOf(PinConnectionStage.SEARCH, PinConnectionStage.CONNECTING, PinConnectionStage.CONNECTED)
        PinConnectionStage.entries.forEach { stage ->
            assertEquals(waiting.contains(stage), stage.steadyAnimation().repeat)
            assertEquals(stage == PinConnectionStage.HOW_TO_CONNECT, stage.steadyAnimation().play)
        }
    }

    @Test
    fun `all steady and transition resources are included in preloading`() {
        assertEquals(13, pinAnimationResources.distinct().size)
        PinConnectionStage.entries.forEach { to ->
            assertTrue(to.steadyAnimation().resource in pinAnimationResources)
            PinConnectionStage.entries.forEach { from ->
                pinTransitionAnimation(from, to)?.let { assertTrue(it in pinAnimationResources) }
            }
        }
    }

    @Test
    fun `normal illustration reserves header space on short viewports`() {
        for (height in listOf(480, 600, 734, 900)) {
            val frame = normalPinIllustrationLayout(375.dp, height.dp)
            assertTrue(frame.width <= 375.dp)
            assertTrue(frame.meterTop >= 176.dp)
            assertEquals(height.dp - 88.dp, frame.top + frame.height)
        }
    }

    @Test
    fun `PIN LCD stays above keyboard and button on smaller viewports`() {
        val normal = normalPinIllustrationLayout(375.dp, 734.dp)
        for (height in listOf(320, 400, 483, 734)) {
            for (scale in listOf(1f, 1.3f)) {
                val enlarged = enlargedPinIllustrationLayout(normal, height.dp, scale)
                assertTrue(enlarged.width <= normal.width)
                val lcdBottom = enlarged.lcdTop + enlarged.width * 0.352f
                assertTrue(lcdBottom <= height.dp - 79.dp)
            }
        }
    }
}
