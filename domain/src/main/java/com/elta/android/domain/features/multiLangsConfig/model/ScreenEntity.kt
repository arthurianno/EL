package com.elta.android.domain.features.multiLangsConfig.model

data class ScreenEntity(
    val slug : String,
    val title : String?,
    val description : String?,
    val backgroundImageUrl : String?,
    val lang : String
)