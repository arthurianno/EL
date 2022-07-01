package com.elta.mobile.data

import com.elta.android.data.features.common.getPage
import org.junit.Test

class PaginatorTest {

    @Test
    fun paginator1() {
        val list = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val pageSize = 2

        val first = list.getPage(1, pageSize)
        assert(first.size == pageSize)
        assert(first.first() == 1)
        assert(first.last() == 2)

        val last = list.getPage(5, pageSize)
        assert(last.size == pageSize)
        assert(last.first() == 9)
        assert(last.last() == 10)
    }

    @Test
    fun paginator2() {
        val list = listOf(1, 2, 3)
        val pageSize = 2

        val last = list.getPage(2, pageSize)
        assert(last.size != pageSize)
        assert(last.first() == 3)
        assert(last.last() == 3)
    }
}
