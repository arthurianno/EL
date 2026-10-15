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
    fun `PIN LCD clears measured controls with tall keyboards and larger text`() {
        val normal = normalPinIllustrationLayout(393.dp, 780.dp)
        for (height in listOf(320, 380, 450, 780)) {
            for (header in listOf(140, 180)) {
                for (footer in listOf(68, 96)) {
                    val enlarged = enlargedPinIllustrationLayout(normal, height.dp, header.dp, footer.dp)
                    assertTrue(enlarged.width > 0.dp)
                    assertTrue(enlarged.width <= normal.width)
                    assertEquals(header + 12f, (enlarged.top + enlarged.width * 0.139f).value, 0.01f)
                    assertTrue("LCD overlaps action panel: $height/$header/$footer", enlarged.lcdBottom.value <= height - footer - 16f + 0.01f)
                }
            }
        }
    }
}
