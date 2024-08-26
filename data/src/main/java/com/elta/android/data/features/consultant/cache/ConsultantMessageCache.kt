package com.elta.android.data.features.consultant.cache

import ru.webim.android.sdk.Message
import javax.inject.Singleton

@Singleton
class ConsultantMessageCache {
    private val cache = mutableMapOf<String, Message>()

    fun put(key: String, value: Message) {
        cache[key] = value
    }

    fun get(key: String): Message? {
        return cache[key]
    }

    fun clear() {
        cache.clear()
    }
}
