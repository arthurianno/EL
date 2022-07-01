package com.elta.android.presentation.widgets.charts.statistics.models

import android.graphics.RectF
import android.graphics.Shader

data class SectionDataModel(
    val lowRect: RectF?,
    val normalRect: RectF?,
    val highRect: RectF?,
    val lowShader: Shader?,
    val normalShader: Shader?,
    val highShader: Shader?
)
