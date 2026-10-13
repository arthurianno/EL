package com.elta.android.domain.version

import com.elta.android.domain.features.version.model.AppUpdateConfig
import com.elta.android.domain.features.version.model.AppUpdateStore
import com.elta.android.domain.features.version.model.PublishedAppUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateConfigTest {
    private val config = AppUpdateConfig(
        enabled = true,
        googlePlay = PublishedAppUpdate(420, GOOGLE_URL),
        ruStore = PublishedAppUpdate(421, RUSTORE_URL)
    )

    @Test
    fun `disabled config never recommends an update`() {
        assertTrue(config.copy(enabled = false).availableUpdates(419, null, PACKAGE).isEmpty())
        assertTrue(AppUpdateConfig().availableUpdates(419, null, PACKAGE).isEmpty())
    }

    @Test
    fun `installed store selects only its published build`() {
        val google = config.availableUpdates(419, "com.android.vending", PACKAGE)
        val rustore = config.availableUpdates(419, "ru.vk.store", PACKAGE)
        assertEquals(listOf(AppUpdateStore.GOOGLE_PLAY), google.map { it.store })
        assertEquals(listOf(AppUpdateStore.RUSTORE), rustore.map { it.store })
        assertEquals(420L, google.single().versionCode)
        assertEquals(RUSTORE_URL, rustore.single().url)
    }

    @Test
    fun `never sends a Play install to RuStore when only RuStore has an update`() {
        assertTrue(config.availableUpdates(420, "com.android.vending", PACKAGE).isEmpty())
    }

    @Test
    fun `unknown installer offers a choice of stores with newer builds`() {
        assertEquals(2, config.availableUpdates(419, null, PACKAGE).size)
        assertEquals(2, config.availableUpdates(419, "com.android.shell", PACKAGE).size)
        assertEquals(listOf(AppUpdateStore.RUSTORE), config.availableUpdates(420, null, PACKAGE).map { it.store })
    }

    @Test
    fun `equal older unpublished and out of range versions are ignored`() {
        listOf(0L, -1L, 418L, 419L, 2_100_000_001L).forEach { version ->
            val invalid = config.copy(googlePlay = PublishedAppUpdate(version, GOOGLE_URL))
            assertTrue(invalid.availableUpdates(419, "com.android.vending", PACKAGE).isEmpty())
        }
        assertTrue(config.availableUpdates(0, null, PACKAGE).isEmpty())
        assertTrue(config.availableUpdates(Long.MAX_VALUE, null, PACKAGE).isEmpty())
    }

    @Test
    fun `invalid URLs and links to a different app cannot trigger a prompt`() {
        listOf(
            "", "not a URL", "http://play.google.com/store/apps/details?id=$PACKAGE",
            "https://example.com/store/apps/details?id=$PACKAGE",
            "https://play.google.com/store/apps/details?id=another.app",
            "$GOOGLE_URL&id=another.app", "https://user@play.google.com/store/apps/details?id=$PACKAGE"
        ).forEach { url ->
            val invalid = config.copy(googlePlay = PublishedAppUpdate(420, url))
            assertTrue(url, invalid.availableUpdates(419, "com.android.vending", PACKAGE).isEmpty())
        }
        val invalidRuStore = config.copy(ruStore = PublishedAppUpdate(420, "https://www.rustore.ru/catalog/app/another.app"))
        assertTrue(invalidRuStore.availableUpdates(419, "ru.vk.store", PACKAGE).isEmpty())
    }

    private companion object {
        const val PACKAGE = "com.elta.android"
        const val GOOGLE_URL = "https://play.google.com/store/apps/details?id=com.elta.android"
        const val RUSTORE_URL = "https://www.rustore.ru/catalog/app/com.elta.android"
    }
}
