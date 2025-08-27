package com.elta.android.data.features.multiLang.models

// Response model
// package com.elta.android.data.features.config.models

data class ScreenConfigResponse(
    val slug: String,
    val description: Map<String, String>,
    val backgroundImageUrl: String? = null
)