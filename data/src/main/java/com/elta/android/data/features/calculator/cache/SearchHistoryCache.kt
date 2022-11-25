package com.elta.android.data.features.calculator.cache

import com.elta.android.data.features.calculator.cache.model.SearchHistoryDbEntity
import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import javax.inject.Inject

class SearchHistoryCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<SearchHistoryDbEntity>(factory) {
    override val classToken: Class<SearchHistoryDbEntity> = SearchHistoryDbEntity::class.java
}
