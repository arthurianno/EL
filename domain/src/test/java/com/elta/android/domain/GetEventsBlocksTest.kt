package com.elta.android.domain

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.factory.TagTestFactory
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.interactor.getEventsBlocks
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import org.junit.Test

class GetEventsBlocksTest {

    @Test
    fun getEventsBlocks_OneEventWithTag_OneBlock() {
        val tagId = TagTestFactory.nextId
        val event = EventTestFactory.create(type = EventType.Glucose, tagId = tagId)
        val tag = TagTestFactory.create(tagId)

        val blocks = getEventsBlocks(
            listOf(event),
            listOf(tag),
            CalculatorFlow.BREAD_UNITS
        )
        val block = blocks[0]
        assert(blocks.size == 1)
        assert(block.tag != null)
        assert(block.tag == tag)
        assert(block.events.size == 1)
        assert(block.events[0] == event)
    }

    @Test
    fun getEventsBlocks_OneEventWithoutTag_OneBlock_NullTag() {
        val event = EventTestFactory.create(type = EventType.Glucose)

        val blocks = getEventsBlocks(
            listOf(event),
            emptyList(),
            CalculatorFlow.BREAD_UNITS
        )
        val block = blocks[0]
        assert(blocks.size == 1)
        assert(block.tag == null)
        assert(block.events.size == 1)
        assert(block.events[0] == event)
    }

    @Test
    fun getEventsBlocks_TwoEvents_TwoBlocks() {
        val tagId = TagTestFactory.nextId
        val event1 = EventTestFactory.create(type = EventType.Glucose)
        val event2 = EventTestFactory.create(type = EventType.Glucose, tagId = tagId)

        val tag = TagTestFactory.create(tagId)

        val blocks = getEventsBlocks(
            arrayListOf(event1, event2),
            arrayListOf(tag),
            CalculatorFlow.BREAD_UNITS
        )
        assert(blocks.size == 2)

        assert(blocks[0].tag == null)
        assert(blocks[0].events.size == 1)
        assert(blocks[0].events[0] == event1)

        assert(blocks[1].tag == tag)
        assert(blocks[1].events.size == 1)
        assert(blocks[1].events[0] == event2)
    }
}
