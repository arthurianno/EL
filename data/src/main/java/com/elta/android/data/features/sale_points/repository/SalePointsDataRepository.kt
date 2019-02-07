package com.elta.android.data.features.sale_points.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.data.features.sale_points.datasource.SalePointsDataSource
import javax.inject.Inject

class SalePointsDataRepository @Inject constructor(
    @Remote private val remoreSource: SalePointsDataSource,
    @Cache private val cacheSource: SalePointsDataSource
)