package com.elta.android.data.features.common.cache

import android.content.Context
import com.elta.android.data.features.MyObjectBox
import com.elta.android.data.features.common.storage.UserHolder
import io.objectbox.BoxStore
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoxStoreFactory @Inject constructor(
    private val context: Context,
    private val userHolder: UserHolder
) {

    private val stores = ConcurrentHashMap<Long, BoxStore>()
    private val commonId = "common".hashCode().toLong()

    fun getBoxStore(level: BoxScope): BoxStore {
        synchronized(BoxStoreFactory::class.java) {
            return when (level) {
                BoxScope.PER_APP -> createOrGetStore(commonId)
                BoxScope.PER_USER -> {
                    val user = userHolder.currentUser
                    if (user != null) {
                        createOrGetStore(user)
                    } else {
                        throw AccessDeniedError
                    }
                }
            }
        }
    }

    private fun createOrGetStore(id: Long): BoxStore = stores[id]
        ?: createBoxStore(id).also { stores[id] = it }

    private fun createBoxStore(id: Long): BoxStore =
        MyObjectBox.builder().androidContext(context).name("boxStore_$id").build()
}
