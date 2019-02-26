package com.elta.android.data.features.sale_points.api

import android.content.Context
import com.elta.android.common.utils.log
import com.elta.android.data.features.common.dto.MetaDto
import com.elta.android.data.features.common.getPage
import com.elta.android.data.features.sale_points.dto.SalePointDto
import com.elta.android.data.features.sale_points.dto.SalePointsDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import io.reactivex.Observable
import java.io.InputStreamReader

class MockedSalePointsApi(private val context: Context) : SalePointsApi {

    private val list: MutableList<SalePointDto> = mutableListOf()

    override fun getSalePoints(lastSync: Long?, page: Int, pageSize: Int): Observable<SalePointsDto> =
        Observable.fromCallable {
            if (list.isEmpty()) {
                val file = context.assets.open("points.json")
                val type = object : TypeToken<List<SalePointDto>>() {}.type
                val reader = JsonReader(InputStreamReader(file))
                list.addAll(Gson().fromJson<List<SalePointDto>>(reader, type))
            }

            val pageOfData = list.getPage(page, PAGE_SIZE)
            SalePointsDto(pageOfData, MetaDto(list.size, page, PAGE_SIZE))
        }.log("Points", "meta") { it.meta.toString() }

    private companion object {
        const val PAGE_SIZE = 500
    }
}