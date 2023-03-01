@file:Suppress("ReturnCount")

package com.elta.android.common.utils

fun <T> List<T>.takeFirst(count: Int): List<T> {
    require(count >= 0) { "Requested element count $count is less than zero." }
    if (count == 0) return emptyList()
    val size = size
    if (count >= size) return toList()
    if (count == 1) return listOf(first())
    val list = ArrayList<T>(count)

    for (index in 0 until count)
        list.add(this[index])
    return list
}

inline fun <T, R : Comparable<R>> Iterable<T>.isSortedBy(crossinline selector: (T) -> R): Boolean {
    val iterator = iterator()
    if (!iterator.hasNext()) {
        return true
    }
    var item = iterator.next()
    while (iterator.hasNext()) {
        val nextItem = iterator.next()
        if (selector(item) > selector(nextItem)) {
            return false
        }
        item = nextItem
    }
    return true
}
