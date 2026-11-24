package it.unimib.indoornav.lab.format

enum class BeaconFrameType(val wireName: String) {
    ED_UID("ed_uid"),
    ED_TLM("ed_tlm"),
    IBEACON("ibeacon"),
    ALTBEACON("altbeacon"),
    VENDOR("vendor"),
}

sealed interface CanonicalBeaconId {
    val canonical: String

    data class EddystoneUid(val namespaceHex: String, val instanceHex: String) : CanonicalBeaconId {
        override val canonical: String = "ed:${namespaceHex.lowercase()}:${instanceHex.lowercase()}"
    }

    data class IBeacon(val uuid: String, val major: Int, val minor: Int) : CanonicalBeaconId {
        override val canonical: String = "ib:${uuid.lowercase()}:$major:$minor"
    }

    data class AltBeacon(val idHex: String) : CanonicalBeaconId {
        override val canonical: String = "ab:${idHex.lowercase()}"
    }

    companion object {
        private val HEX_20 = Regex("^[0-9a-f]{20}$")
        private val HEX_12 = Regex("^[0-9a-f]{12}$")
        private val HEX_40 = Regex("^[0-9a-f]{40}$")
        private val UUID_REGEX = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")

        fun parse(raw: String): CanonicalBeaconId? {
            val trimmed = raw.trim().lowercase()
            val parts = trimmed.split(':')
            return when (parts.firstOrNull()) {
                "ed" -> {
                    if (parts.size != 3) return null
                    if (!HEX_20.matches(parts[1]) || !HEX_12.matches(parts[2])) return null
                    EddystoneUid(parts[1], parts[2])
                }
                "ib" -> {
                    if (parts.size != 4) return null
                    val uuid = normalizeUuid(parts[1]) ?: return null
                    val major = parts[2].toIntOrNull() ?: return null
                    val minor = parts[3].toIntOrNull() ?: return null
                    if (major !in 0..65535 || minor !in 0..65535) return null
                    IBeacon(uuid, major, minor)
                }
                "ab" -> {
                    if (parts.size != 2 || !HEX_40.matches(parts[1])) return null
                    AltBeacon(parts[1])
                }
                else -> null
            }
        }

        fun formatUuid(bytes: ByteArray, offset: Int = 0): String {
            require(offset + 16 <= bytes.size) { "Need 16 bytes for UUID" }
            val hex = bytes.sliceHex(offset, 16)
            return "${hex.substring(0, 8)}-${hex.substring(8, 12)}-${hex.substring(12, 16)}-${hex.substring(16, 20)}-${hex.substring(20, 32)}"
        }

        private fun normalizeUuid(input: String): String? {
            val lower = input.lowercase()
            if (UUID_REGEX.matches(lower)) return lower
            val stripped = lower.replace("-", "")
            if (stripped.length != 32 || !stripped.all { it in '0'..'9' || it in 'a'..'f' }) return null
            return "${stripped.substring(0, 8)}-${stripped.substring(8, 12)}-${stripped.substring(12, 16)}-${stripped.substring(16, 20)}-${stripped.substring(20, 32)}"
        }
    }
}

data class ParsedBeaconFrame(
    val frameType: BeaconFrameType,
    val beaconId: String?,
    val txPowerAdv: Int?,
    val tlmBattMv: Int?,
    val tlmTempC: Double?,
    val advHex: String,
)

object BeaconFrameParser {
    private const val AD_TYPE_SERVICE_DATA_16 = 0x16
    private const val AD_TYPE_MANUFACTURER_DATA = 0xFF
    private const val EDDYSTONE_UUID_LO = 0xAA
    private const val EDDYSTONE_UUID_HI = 0xFE
    private const val APPLE_COMPANY_ID = 0x004C

    fun parseRawAdvertisement(rawBytes: ByteArray): ParsedBeaconFrame? {
        if (rawBytes.isEmpty()) return null
        val activeLen = activeAdvertisementLength(rawBytes)
        val activeHex = rawBytes.sliceHex(0, activeLen)

        var index = 0
        while (index < activeLen) {
            val length = rawBytes[index].toInt() and 0xFF
            if (length == 0) {
                index++
                continue
            }
            val payloadEnd = index + 1 + length
            if (payloadEnd > activeLen) break

            val adType = rawBytes[index + 1].toInt() and 0xFF
            val dataOffset = index + 2
            val dataLen = length - 1

            when (adType) {
                AD_TYPE_SERVICE_DATA_16 -> {
                    if (dataLen >= 3 &&
                        (rawBytes[dataOffset].toInt() and 0xFF) == EDDYSTONE_UUID_LO &&
                        (rawBytes[dataOffset + 1].toInt() and 0xFF) == EDDYSTONE_UUID_HI
                    ) {
                        parseEddystonePayload(rawBytes, dataOffset + 2, dataLen - 2, activeHex)?.let {
                            return it
                        }
                    }
                }
                AD_TYPE_MANUFACTURER_DATA -> {
                    if (dataLen >= 4) {
                        val companyId = ((rawBytes[dataOffset + 1].toInt() and 0xFF) shl 8) or
                            (rawBytes[dataOffset].toInt() and 0xFF)
                        parseManufacturerPayload(companyId, rawBytes, dataOffset + 2, dataLen - 2, activeHex)?.let {
                            return it
                        }
                    }
                }
            }
            index = payloadEnd
        }
        return null
    }

    fun parseEddystoneServiceData(serviceData: ByteArray, advHex: String = serviceData.toHexLower()): ParsedBeaconFrame? {
        return parseEddystonePayload(serviceData, 0, serviceData.size, advHex)
    }

    fun parseManufacturerData(
        companyId: Int,
        data: ByteArray,
        advHex: String = data.toHexLower(),
    ): ParsedBeaconFrame? {
        return parseManufacturerPayload(companyId, data, 0, data.size, advHex)
    }

    fun activeAdvertisementLength(rawBytes: ByteArray): Int {
        var index = 0
        var lastValidEnd = 0
        while (index < rawBytes.size) {
            val length = rawBytes[index].toInt() and 0xFF
            if (length == 0) {
                index++
                continue
            }
            val next = index + 1 + length
            if (next > rawBytes.size) break
            lastValidEnd = next
            index = next
        }
        return lastValidEnd
    }

    private fun parseEddystonePayload(
        bytes: ByteArray,
        offset: Int,
        length: Int,
        advHex: String,
    ): ParsedBeaconFrame? {
        if (length < 1) return null
        return when (bytes[offset].toInt() and 0xFF) {
            0x00 -> {
                // Eddystone-UID: frame(1) + txPower0m(1) + namespace(10) + instance(6)
                if (length < 18) return null
                val txPower = bytes[offset + 1].toInt()
                val namespace = bytes.sliceHex(offset + 2, 10)
                val instance = bytes.sliceHex(offset + 12, 6)
                ParsedBeaconFrame(
                    frameType = BeaconFrameType.ED_UID,
                    beaconId = CanonicalBeaconId.EddystoneUid(namespace, instance).canonical,
                    txPowerAdv = txPower,
                    tlmBattMv = null,
                    tlmTempC = null,
                    advHex = advHex,
                )
            }
            0x20 -> {
                // Unencrypted Eddystone-TLM: frame(1) + version(1) + vbatt(2) + temp(2) + advCnt(4) + secCnt(4)
                if (length < 14) return null
                val version = bytes[offset + 1].toInt() and 0xFF
                if (version != 0x00) return null
                val battMv = ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
                    (bytes[offset + 3].toInt() and 0xFF)
                val rawTemp = ((bytes[offset + 4].toInt() and 0xFF) shl 8) or
                    (bytes[offset + 5].toInt() and 0xFF)
                val tempC = if (rawTemp == 0x8000) null else rawTemp.toShort().toDouble() / 256.0
                ParsedBeaconFrame(
                    frameType = BeaconFrameType.ED_TLM,
                    beaconId = null,
                    txPowerAdv = null,
                    tlmBattMv = battMv,
                    tlmTempC = tempC,
                    advHex = advHex,
                )
            }
            else -> null
        }
    }

    private fun parseManufacturerPayload(
        companyId: Int,
        bytes: ByteArray,
        offset: Int,
        length: Int,
        advHex: String,
    ): ParsedBeaconFrame? {
        if (companyId == APPLE_COMPANY_ID && length >= 23 &&
            (bytes[offset].toInt() and 0xFF) == 0x02 &&
            (bytes[offset + 1].toInt() and 0xFF) == 0x15
        ) {
            val uuid = CanonicalBeaconId.formatUuid(bytes, offset + 2)
            val major = ((bytes[offset + 18].toInt() and 0xFF) shl 8) or
                (bytes[offset + 19].toInt() and 0xFF)
            val minor = ((bytes[offset + 20].toInt() and 0xFF) shl 8) or
                (bytes[offset + 21].toInt() and 0xFF)
            val txPower = bytes[offset + 22].toInt()
            return ParsedBeaconFrame(
                frameType = BeaconFrameType.IBEACON,
                beaconId = CanonicalBeaconId.IBeacon(uuid, major, minor).canonical,
                txPowerAdv = txPower,
                tlmBattMv = null,
                tlmTempC = null,
                advHex = advHex,
            )
        }

        if (length >= 24 &&
            (bytes[offset].toInt() and 0xFF) == 0xBE &&
            (bytes[offset + 1].toInt() and 0xFF) == 0xAC
        ) {
            val idHex = bytes.sliceHex(offset + 2, 20)
            val txPower = bytes[offset + 22].toInt()
            return ParsedBeaconFrame(
                frameType = BeaconFrameType.ALTBEACON,
                beaconId = CanonicalBeaconId.AltBeacon(idHex).canonical,
                txPowerAdv = txPower,
                tlmBattMv = null,
                tlmTempC = null,
                advHex = advHex,
            )
        }

        return null
    }
}

internal fun ByteArray.sliceHex(offset: Int, length: Int): String {
    val sb = StringBuilder(length * 2)
    val end = minOf(size, offset + length)
    for (i in offset until end) {
        val v = this[i].toInt() and 0xFF
        sb.append(HEX_CHARS[v ushr 4])
        sb.append(HEX_CHARS[v and 0x0F])
    }
    return sb.toString()
}

fun ByteArray.toHexLower(): String = sliceHex(0, size)

private val HEX_CHARS = "0123456789abcdef".toCharArray()
