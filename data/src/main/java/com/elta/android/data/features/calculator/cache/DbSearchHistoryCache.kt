package com.elta.android.data.features.calculator.cache

import com.elta.android.data.features.calculator.cache.dto.SearchHistoryDto
import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import javax.inject.Inject

class DbSearchHistoryCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<SearchHistoryDto>(factory) {
    override val classToken: Class<SearchHistoryDto> = SearchHistoryDto::class.java
}
