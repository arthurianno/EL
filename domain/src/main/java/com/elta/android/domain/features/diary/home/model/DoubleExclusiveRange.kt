package com.elta.android.domain.features.diary.home.model

class DoubleExclusiveRange(start: Double, end: Double) {

    private val _start: Double = start
    private val _end: Double = end

    operator fun contains(value: Double): Boolean = value >= _start && value < _end

    fun isEmpty(): Boolean = _start > _end
}