package com.elta.android.domain.features.appsettings.model

enum class BackendVariant(val url: String) {
    TEST("https://test.vdiabete.com"),
    DEV("https://dev.vdiabete.com"),
    STAGE("https://stage2.vdiabete.com"),
    PROD("https://vdiabete.com");

    companion object {
        fun String.toBackendVariantName(): String =
            when (this) {
                TEST.url -> TEST.name
                STAGE.url -> STAGE.name
                PROD.url -> PROD.name
                else -> DEV.name
            }

        const val NAME_BACKEND_VARIANT = "backend_variant"
    }
}
