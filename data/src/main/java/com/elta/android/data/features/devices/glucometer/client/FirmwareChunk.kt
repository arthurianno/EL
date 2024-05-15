package com.elta.android.data.features.devices.glucometer.client

/**
 * Класс содержащий чанк ByteArray заданных в определенной структуре(start cmd adr num data) для обновления прошивки глюкометра.
 *
 * Созданный объекта/чанк отсылается к глюкометру. В случае успешного прохождения команды,мы посылаем следующий чанк и так до тех пор пока загрузим всю информацию из файла.
 */
class FirmwareChunk(
    val cmd: Byte,
    val adr: Int,
    val num: UByte,
    val data: ByteArray
) {
    private val start: Byte = 0x24

    init {
        require(cmd == CMD_BIN_CODE || cmd == CMD_DAT_CODE) { "Command code must be 0x01 or 0x04" }
        require(adr % 4 == 0) { "Address must be divisible by 4" }
        require(adr in 0x00000000..0x0001FFFF) { "Address must be in range 0x00000000 to 0x0001FFFF" }
        require(num % 4u == 0u) { "Number of bytes must be divisible by 4" }
        require(num in 4u..236u) { "Number of bytes must be between 4 and 236" }
        require(data.size == num.toInt()) { "Size of data must match the specified number of bytes" }
    }

    fun toByteArray(): ByteArray {
        val byteArray = ByteArray(7 + data.size)
        byteArray[0] = start
        byteArray[1] = cmd
        byteArray[2] = (adr and 0xFF).toByte()
        byteArray[3] = ((adr shr 8) and 0xFF).toByte()
        byteArray[4] = ((adr shr 16) and 0xFF).toByte()
        byteArray[5] = ((adr shr 24) and 0xFF).toByte()
        byteArray[6] = num.toByte()
        System.arraycopy(data, 0, byteArray, 7, data.size)
        return byteArray
    }

    companion object {
        const val MAX_DATA_BYTES_IN_CHUNK = 236
        const val START_ADDRESS = 0x000000
        const val CMD_BIN_CODE: Byte = 0x01
        const val CMD_DAT_CODE: Byte = 0x04
        const val NUM_DAT = 0x10
    }
}
