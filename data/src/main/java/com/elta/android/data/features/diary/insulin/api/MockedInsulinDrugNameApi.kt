package com.elta.android.data.features.diary.insulin.api

import com.elta.android.domain.features.diary.events.model.InsulinType
import io.reactivex.Observable

class MockedInsulinDrugNameApi : InsulinDrugNameApi {
    override fun getDrugNamesByInsulinType(type: InsulinType): Observable<List<String>> =
        Observable.just(
            when (type) {
                InsulinType.ULTRASHORT -> insulinDrugNamesUS
                InsulinType.SHORT -> insulinDrugNamesS
                InsulinType.INTERMIDIATE -> insulinDrugNamesI
                InsulinType.LONG -> insulinDrugNamesL
                InsulinType.ULTRALONG -> insulinDrugNamesUL
                InsulinType.MIXED -> insulinDrugNamesM
            }
        )
}

private val insulinDrugNamesUS = listOf("US1", "US2")
private val insulinDrugNamesS = listOf("S1", "S2", "S3")
private val insulinDrugNamesI = listOf("I1")
private val insulinDrugNamesL = listOf("L1", "L2", "L3")
private val insulinDrugNamesUL = listOf("UL1", "UL2", "UL3", "UL4")
private val insulinDrugNamesM = listOf("M1")
