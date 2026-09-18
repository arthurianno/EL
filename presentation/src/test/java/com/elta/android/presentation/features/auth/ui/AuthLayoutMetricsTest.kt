package com.elta.android.presentation.features.auth.ui

import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

class AuthLayoutMetricsTest {

    @Test
    fun `tall viewport keeps expanded image and anchors form at bottom`() {
        val layout = metrics(viewportHeight = 800)

        assertEquals(IntSize(375, 360), layout.illustrationSize)
        assertEquals(600, layout.formY)
        assertEquals(800, layout.contentHeight)
    }

    @Test
    fun `keyboard sizes remain screen specific when space permits`() {
        listOf(IntSize(155, 149), IntSize(125, 120), IntSize(183, 176)).forEach { compactSize ->
            val layout = metrics(viewportHeight = 500, preferredSize = compactSize)

            assertEquals(compactSize, layout.illustrationSize)
            assertEquals(500, layout.contentHeight)
        }
    }

    @Test
    fun `reduced viewport shrinks image before introducing scroll`() {
        val layout = metrics(viewportHeight = 420, preferredSize = IntSize(183, 176))

        assertEquals(100, layout.illustrationSize.height)
        assertEquals(420, layout.contentHeight)
        assertEquals(220, layout.formY)
    }

    @Test
    fun `larger text or errors reduce illustration instead of clipping form`() {
        val normal = metrics(viewportHeight = 700)
        val withErrors = metrics(viewportHeight = 700, formHeight = 350)

        assertTrue(withErrors.illustrationSize.height < normal.illustrationSize.height)
        assertEquals(350, withErrors.formY)
        assertEquals(700, withErrors.contentHeight)
    }

    @Test
    fun `very short viewport retains natural text and form heights in scroll content`() {
        val layout = metrics(viewportHeight = 250, headingHeight = 180, formHeight = 320)

        assertEquals(72, layout.illustrationSize.height)
        assertEquals(252, layout.formY)
        assertEquals(572, layout.contentHeight)
    }

    @Test
    fun `narrow viewport constrains width while preserving image proportions`() {
        val layout = metrics(viewportHeight = 800, contentWidth = 100)

        assertEquals(IntSize(100, 96), layout.illustrationSize)
        assertEquals(800, layout.contentHeight)
    }

    @Test
    fun `content does not overlap across widths densities and text sizes`() {
        for (density in listOf(1f, 1.5f, 2f, 3f)) {
            fun px(dp: Int) = (dp * density).roundToInt()
            for (width in listOf(240, 320, 375, 412, 600)) {
                for (height in listOf(0, 180, 320, 480, 800)) {
                    for (headingHeight in listOf(100, 250)) {
                        for (formHeight in listOf(160, 400)) {
                            val layout = calculateAuthLayoutMetrics(
                                viewportHeight = px(height),
                                contentWidth = px(width),
                                headingHeight = px(headingHeight),
                                formHeight = px(formHeight),
                                preferredIllustrationSize = IntSize(px(375), px(360)),
                                minimumIllustrationHeight = px(72)
                            )

                            assertTrue(layout.illustrationSize.width <= px(width))
                            assertTrue(layout.illustrationSize.height >= 0)
                            assertTrue(layout.formY >= px(headingHeight) + layout.illustrationSize.height)
                            assertEquals(layout.formY + px(formHeight), layout.contentHeight)
                            assertTrue(layout.contentHeight >= px(height))
                        }
                    }
                }
            }
        }
    }

    private fun metrics(
        viewportHeight: Int,
        contentWidth: Int = 375,
        headingHeight: Int = 120,
        formHeight: Int = 200,
        preferredSize: IntSize = IntSize(375, 360)
    ) = calculateAuthLayoutMetrics(
        viewportHeight = viewportHeight,
        contentWidth = contentWidth,
        headingHeight = headingHeight,
        formHeight = formHeight,
        preferredIllustrationSize = preferredSize,
        minimumIllustrationHeight = 72
    )
}
