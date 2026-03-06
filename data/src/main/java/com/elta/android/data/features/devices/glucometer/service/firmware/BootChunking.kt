package com.elta.android.data.features.devices.glucometer.service.firmware

internal fun splitChunkSizes(totalSize: Int, chunkSize: Int): List<Int> {
    require(totalSize >= 0) { "Total size must be non-negative" }
    require(chunkSize > 0) { "Chunk size must be positive" }
    if (totalSize == 0) return emptyList()

    val fullChunks = totalSize / chunkSize
    val remainder = totalSize % chunkSize
    val sizes = MutableList(fullChunks) { chunkSize }
    if (remainder > 0) sizes.add(remainder)
    return sizes
}
