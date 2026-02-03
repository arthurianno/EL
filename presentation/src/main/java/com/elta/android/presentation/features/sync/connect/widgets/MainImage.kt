package com.elta.android.presentation.features.sync.connect.widgets

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.elta.android.presentation.theme.GetLocalProperties



@Composable
internal fun ColumnScope.MainImage(
    imageUrl: String? = null,
    @DrawableRes imageId: Int
) {
    val context = LocalContext.current
    GetLocalProperties { dimens, _, _, _, _ ->
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(dimens.bigDim),
            contentAlignment = Alignment.Center
        ) {
           when(imageUrl != null){
                true -> {
                    Log.d("MainImage", "🖼️ [Compose] Загружаем изображение из URL: $imageUrl")
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(false)
                            .listener(
                                onStart = {
                                    Log.d("MainImage", "⏳ [Compose] Начало загрузки изображения")
                                },
                                onSuccess = { _, result ->
                                    val source = result.dataSource.toString()
                                    Log.i("MainImage", "✅ [Compose] Изображение загружено из: $source")
                                },
                                onError = { _, result ->
                                    Log.e("MainImage", "❌ [Compose] Ошибка загрузки: ${result.throwable.message}")
                                }
                            )
                            .build(),
                        contentDescription = null
                    )
                }
                false -> {
                    Log.d("MainImage", "📋 [Compose] Используем дефолтное изображение: $imageId")
                    Image(
                        painter = painterResource(id = imageId),
                        contentDescription = null
                    )
                }
           }
        }
    }
}
