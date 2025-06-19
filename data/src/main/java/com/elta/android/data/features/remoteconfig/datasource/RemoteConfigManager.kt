package com.elta.android.data.features.remoteconfig.datasource

interface RemoteConfigManager {

    suspend fun fetchAndActivate(): Boolean
    fun getString(key: String): String
    fun getBoolean(key: String): Boolean
    fun getLong(key: String): Long
    fun getDouble(key: String): Double
}
