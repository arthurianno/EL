package com.nullgr.android.domain.features.feature1.repository

import com.nullgr.android.domain.features.feature1.model.TestModel
import io.reactivex.Observable

interface TestRepository {

    fun getTestModel(): Observable<TestModel>
}