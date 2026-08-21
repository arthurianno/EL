package android.util

class LruCache<K, V>(private val maxSize: Int) {

    private val values = object : LinkedHashMap<K, V>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean =
            size > maxSize
    }

    fun get(key: K): V? = values[key]

    fun put(key: K, value: V): V? = values.put(key, value)
}
