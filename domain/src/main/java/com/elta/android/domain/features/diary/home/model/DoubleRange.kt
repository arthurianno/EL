package com.elta.android.domain.features.diary.home.model

class DoubleRange(start: Double, end: Double) {

    private val _start: Double = start
    private val _end: Double = end

    val start: Double
        get() = _start

    val end: Double
        get() = _end

    operator fun contains(value: Double): Boolean = value in _start.._end

    fun isEmpty(): Boolean = _start > _end
}