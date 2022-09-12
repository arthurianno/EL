package com.elta.android.data.features.sale_points.datasource

import com.elta.android.data.features.sale_points.dto.SalePointDto
import io.reactivex.Observable

interface SalePointsDataSource {

    /**
     * Returns SalePoints by type.
     * @param type String representation of sale point's [com.elta.android.domain.features.sale_points.model.Type] -
     * can be one of [com.elta.android.domain.features.sale_points.model.Type.SERVICE] or
     * [com.elta.android.domain.features.sale_points.model.Type.SALE]. Can be null to fetch all points
     * @return Observable that emits sale points
     */
    fun getSalePoints(type: String?): Observable<List<SalePointDto>>

    // TODO if this method will be ever used, support of by type search should be added
    fun getSalePoints(
        southWestLatitude: Double,
        southWestLongitude: Double,
        northEastLatitude: Double,
        northEastLongitude: Double
    ): Observable<List<SalePointDto>>

    /**
     * Returns SalePoints by type and query.
     * @param type String representation of sale point's [com.elta.android.domain.features.sale_points.model.Type] -
     * can be one of [com.elta.android.domain.features.sale_points.model.Type.SERVICE] or
     * [com.elta.android.domain.features.sale_points.model.Type.SALE]. Can be null to fetch all points
     * @param query Search query
     * @return Observable that emits sale points
     */
    fun searchSalePoints(query: String, type: String?): Observable<List<SalePointDto>>
}
