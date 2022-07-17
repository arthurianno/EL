package com.elta.android.data.features.diary.insulin.api

import com.elta.android.domain.features.diary.events.model.InsulinType
import io.reactivex.Observable

class MockedInsulinNameApi : InsulinNameApi {
    override fun getInsulinNameByType(type: InsulinType): Observable<List<String>> =
        Observable.just(
            when (type) {
                InsulinType.ULTRASHORT -> insulinNamesUS
                InsulinType.SHORT -> insulinNamesS
                InsulinType.INTERMIDIATE -> insulinNamesI
                InsulinType.LONG -> insulinNamesL
                InsulinType.ULTRALONG -> insulinNamesUL
                InsulinType.MIXED -> insulinNamesM
            }
        )
}

private val insulinNamesUS = listOf("US1", "US2")
private val insulinNamesS = listOf("S1", "S2", "S3")
private val insulinNamesI = listOf("I1")
private val insulinNamesL = listOf("L1", "L2", "L3")
private val insulinNamesUL = listOf("UL1", "UL2", "UL3", "UL4")
private val insulinNamesM = listOf("M1")
