package com.elta.android.data.features.consultant

import com.elta.android.data.features.consultant.search.Bm25Searcher
import com.elta.android.domain.features.consultant.model.BotNode
import com.elta.android.domain.features.consultant.model.BotOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class Bm25SearcherTest {

    @Test
    fun testBm25Search() {
        val deliveryNode = BotNode(
            id = "delivery",
            text = "Как работает доставка и стоимость доставки?",
            options = listOf(BotOption("Курьерская доставка", "courier"))
        )
        val paymentNode = BotNode(
            id = "payment",
            text = "Как оплатить заказ? Способы оплаты картой и наличными.",
            options = listOf(BotOption("Оплата картой online", "card"))
        )
        val refundNode = BotNode(
            id = "refund",
            text = "Правила возврата денежных средств и товаров.",
            options = listOf(BotOption("Как вернуть товар", "return"))
        )

        val searcher = Bm25Searcher(listOf(deliveryNode, paymentNode, refundNode))

        // Ищем по слову "доставка"
        val result1 = searcher.search("доставка")
        assertNotNull(result1)
        assertEquals("delivery", result1?.id)

        // Ищем по слову "оплата"
        val result2 = searcher.search("оплата")
        assertNotNull(result2)
        assertEquals("payment", result2?.id)

        // Ищем по слову "вернуть"
        val result3 = searcher.search("хочу вернуть товар")
        assertNotNull(result3)
        assertEquals("refund", result3?.id)

        // Ищем нерелевантное слово
        val result4 = searcher.search("привет как дела")
        assertNull(result4)
    }
}
