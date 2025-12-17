package com.elta.android.presentation.utils.cacheHelper

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlin.times

@OptIn(ExperimentalCoilApi::class)
object ImageCacheHelper {


    // Список URL, которые не удалось загрузить (чтобы не пытаться повторно)
    private val failedUrls = mutableSetOf<String>()

    suspend fun prefetchImage(url: String, context: Context, imageLoader: ImageLoader): Pair<Boolean, Long> {
        // Если этот URL уже падал с ошибкой, не пытаемся снова
        if (failedUrls.contains(url)) {
            Log.w("ImageCacheHelper", "⏭️ Skipping previously failed URL: $url")
            return Pair(false, 0L)
        }

        return try {
            val startTime = System.currentTimeMillis()
            val threadName = Thread.currentThread().name
            Log.d("ImageCacheHelper", "⬇️ [Thread: $threadName] Starting prefetch: $url")

            // Время начала сетевого запроса
            val networkStartTime = System.currentTimeMillis()

            val request = ImageRequest.Builder(context)
                .data(url)
                .memoryCacheKey(url)
                .diskCacheKey(url)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .listener(
                    onStart = {
                        Log.d("ImageCacheHelper", "🌐 [Thread: $threadName] Network request started: ${url.takeLast(50)}")
                    },
                    onSuccess = { _, result ->
                        val totalDuration = System.currentTimeMillis() - startTime
                        val networkDuration = System.currentTimeMillis() - networkStartTime
                        val decodeDuration = totalDuration - networkDuration

                        Log.d("ImageCacheHelper", "✅ Total: ${totalDuration}ms (Network: ${networkDuration}ms, Decode: ${decodeDuration}ms)")
                    },
                    onError = { _, error ->
                        val networkDuration = System.currentTimeMillis() - networkStartTime
                        Log.e("ImageCacheHelper", "🌐 [Thread: $threadName] Network error after ${networkDuration}ms: ${error.throwable.message}")
                    }
                )
                .build()

            // Проверяем успешность загрузки
            when (val result = imageLoader.execute(request)) {
                is SuccessResult -> {
                    val duration = System.currentTimeMillis() - startTime
                    val dataSource = result.dataSource
                    Log.d("ImageCacheHelper", "✅ [Thread: $threadName] Success in ${duration}ms from $dataSource: ${url.takeLast(50)}")
                    Pair(true, duration)
                }
                is ErrorResult -> {
                    val error = result.throwable
                    val duration = System.currentTimeMillis() - startTime
                    Log.e("ImageCacheHelper", "❌ [Thread: $threadName] Failed in ${duration}ms: ${url.takeLast(50)}")
                    Log.e("ImageCacheHelper", "Error: ${error.message}")

                    // Добавляем в черный список
                    failedUrls.add(url)
                    Pair(false, duration)
                }
            }
        } catch (e: Exception) {
            Log.e("ImageCacheHelper", "❌ Exception prefetching: $url", e)
            failedUrls.add(url)
            Pair(false, 0L)
        }
    }

    suspend fun isImageInCache(url: String, context: Context, imageLoader: ImageLoader): Boolean {
        val request = ImageRequest.Builder(context)
            .data(url)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.DISABLED)
            .build()

        val result = imageLoader.execute(request)
        val isInCache = result is SuccessResult

        Log.d("ImageCacheHelper", "Image URL: $url isInCache: $isInCache")
        return isInCache
    }
}