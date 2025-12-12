package com.elta.android.presentation.utils.cacheHelper

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest

object ImageCacheHelper{

    suspend fun prefetchImage(url: String, context: Context, imageLoader: ImageLoader) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .memoryCacheKey(url)
            .diskCacheKey(url)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
        imageLoader.execute(request)
    }
}