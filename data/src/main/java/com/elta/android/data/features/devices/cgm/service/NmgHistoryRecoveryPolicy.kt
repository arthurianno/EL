package com.elta.android.data.features.devices.cgm.service

/**
 * History is persisted in two-minute buckets. A newer advertisement fills the next bucket by
 * itself, so GATT recovery is necessary only when at least one complete bucket is absent.
 */
internal object NmgHistoryRecoveryPolicy {
    private const val HISTORY_INTERVAL_SECONDS = 120L

    fun hasMissingHistoryBucket(
        lastStoredUptimeSeconds: Long,
        currentUptimeSeconds: Long
    ): Boolean = currentUptimeSeconds / HISTORY_INTERVAL_SECONDS >
        lastStoredUptimeSeconds / HISTORY_INTERVAL_SECONDS + 1
}
