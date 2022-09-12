@file:Suppress("ReturnCount")
package com.elta.android.common.utils

fun <T> List<T>.takeFirst(n: Int): List<T> {
    require(n >= 0) { "Requested element count $n is less than zero." }
    if (n == 0) return emptyList()
    val size = size
    if (n >= size) return toList()
    if (n == 1) return listOf(first())
    val list = ArrayList<T>(n)

    for (index in 0 until n)
        list.add(this[index])
    return list
}

inline fun <T, R : Comparable<R>> Iterable<T>.isSortedBy(crossinline selector: (T) -> R): Boolean {
    val iter = iterator()
    if (!iter.hasNext()) {
        return true
    }
    var t = iter.next()
    while (iter.hasNext()) {
        val t2 = iter.next()
        if (selector(t) > selector(t2)) {
            return false
        }
        t = t2
    }
    return true
}
