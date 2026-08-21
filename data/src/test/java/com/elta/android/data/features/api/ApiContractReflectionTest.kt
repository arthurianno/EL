package com.elta.android.data.features.api

import com.elta.android.data.features.diary.medicines.api.MedicinesApi
import com.elta.android.data.features.newsChannel.datasource.NewsApi
import com.elta.android.data.features.reports.api.ReportsApi
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.lang.reflect.Method

class ApiContractReflectionTest {

    @Test
    fun `diary medicaments request contains languageTag and countryCode query params`() {
        val method = MedicinesApi::class.java.methods.first { it.name == "getMedicaments" }

        assertTrue(method.queryNames().containsAll(listOf("touchedAfter", "languageTag", "countryCode")))
    }

    @Test
    fun `insulin medicaments request contains languageTag and countryCode query params`() {
        val method = MedicinesApi::class.java.methods.first { it.name == "getInsulinMedicines" }

        assertTrue(method.queryNames().containsAll(listOf("languageTag", "countryCode")))
    }

    @Test
    fun `news request contains targeting query params`() {
        val method = NewsApi::class.java.methods.first { it.name == "getNewsList" }

        assertTrue(
            method.queryNames().containsAll(
                listOf(
                    "cursor",
                    "limit",
                    "direction",
                    "languageTag",
                    "platform",
                    "appVersion",
                    "countryCode"
                )
            )
        )
    }

    @Test
    fun `observable report token request contains languageTag query param`() {
        val method = ReportsApi::class.java.methods.first { it.name == "getObservableReportToken" }

        assertTrue(
            method.queryNames().containsAll(
                listOf("startPeriod", "endPeriod", "glucoseFormat", "languageTag")
            )
        )
    }

    @Test
    fun `xlsx report request uses v2 events endpoint with xlsx accept header and required query params`() {
        val method = ReportsApi::class.java.methods.first { it.name == "downloadGlycemicProfileXlsxReport" }

        assertTrue(method.getAnnotation(GET::class.java)?.value == "api/reports/v2/events")
        assertTrue(
            method.queryNames().containsAll(
                listOf(
                    "reportPeriodStart",
                    "reportPeriodEnd",
                    "glucoseFormat",
                    "glucoseUnit",
                    "locale",
                    "timezoneOffset"
                )
            )
        )
        assertTrue(
            method.getAnnotation(Headers::class.java)
                ?.value
                ?.contains("Accept: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") == true
        )
    }

    private fun Method.queryNames(): List<String> =
        parameterAnnotations
            .flatMap { annotations ->
                annotations.mapNotNull { annotation -> (annotation as? Query)?.value }
            }
}
