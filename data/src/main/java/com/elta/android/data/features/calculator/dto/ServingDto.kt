package com.elta.android.data.features.calculator.dto

import com.google.gson.annotations.SerializedName

data class ServingDto(
    @SerializedName("calories") val calories: String,
    @SerializedName("carbohydrate") val carbohydrate: String,
    @SerializedName("cholesterol") val cholesterol: String,
    @SerializedName("fat") val fat: String,
    @SerializedName("fiber") val fiber: String,
    @SerializedName("measurement_description") val measurementDescription: String,
    @SerializedName("metric_serving_amount") val metricServingAmount: String,
    @SerializedName("metric_serving_unit") val metricServingUnit: String,
    @SerializedName("monounsaturated_fat") val monounsaturatedFat: String,
    @SerializedName("number_of_units") val numberOfUnits: String,
    @SerializedName("polyunsaturated_fat") val polyunsaturatedFat: String,
    @SerializedName("protein") val protein: String,
    @SerializedName("saturated_fat") val saturatedFat: String,
    @SerializedName("serving_description") val servingDescription: String,
    @SerializedName("serving_id") val servingId: String,
    @SerializedName("serving_url") val servingUrl: String,
    @SerializedName("sodium") val sodium: String,
    @SerializedName("sugar") val sugar: String,
    @SerializedName("trans_fat") val transFat: String
)
