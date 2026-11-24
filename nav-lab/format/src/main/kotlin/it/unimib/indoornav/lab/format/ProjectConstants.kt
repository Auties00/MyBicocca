package it.unimib.indoornav.lab.format

object ProjectConstants {
    const val FORMAT_VERSION = 1
    const val DEFAULT_BUILDING = "U14"
    const val DEFAULT_BUILDING_CODE = 14
    const val DEFAULT_FLOOR = 1
    const val PROJECT_UUID = "515b8618-bead-46de-ba72-5cea0ec01b22"
    const val EDDYSTONE_NAMESPACE = "515b86185cea0ec01b22"
    val DEFAULT_UNIVERSITY_SSIDS = listOf("eduroam")

    val PROJECT_UUID_BYTES: ByteArray = hexToBytes(PROJECT_UUID.replace("-", ""))

    fun formatProjectLabel(beaconNumber: Int): String {
        require(beaconNumber >= 0) { "Beacon number must be non-negative" }
        return "B" + beaconNumber.toString().padStart(2, '0')
    }

    fun expectedIBeaconId(buildingCode: Int, beaconNumber: Int): String =
        "ib:$PROJECT_UUID:$buildingCode:$beaconNumber"

    fun expectedEddystoneUid(buildingCode: Int, beaconNumber: Int): String {
        val bHex = (buildingCode and 0xFFFF).toString(16).padStart(4, '0')
        val nHex = (beaconNumber and 0xFFFF).toString(16).padStart(4, '0')
        return "ed:$EDDYSTONE_NAMESPACE:0000$bHex$nHex"
    }

    fun matchProjectLabel(canonicalId: String, buildingCode: Int = DEFAULT_BUILDING_CODE): String? {
        val parts = canonicalId.split(':')
        return when (parts.firstOrNull()) {
            "ib" -> {
                if (parts.size != 4) return null
                val uuid = parts[1].lowercase()
                val major = parts[2].toIntOrNull() ?: return null
                val minor = parts[3].toIntOrNull() ?: return null
                if (uuid == PROJECT_UUID && major == buildingCode && minor >= 0) {
                    formatProjectLabel(minor)
                } else {
                    null
                }
            }
            "ed" -> {
                if (parts.size != 3) return null
                val ns = parts[1].lowercase()
                val inst = parts[2].lowercase()
                if (ns != EDDYSTONE_NAMESPACE || inst.length != 12) return null
                val prefix = inst.substring(0, 4)
                val bCode = inst.substring(4, 8).toIntOrNull(16) ?: return null
                val bNum = inst.substring(8, 12).toIntOrNull(16) ?: return null
                if (prefix == "0000" && bCode == buildingCode) {
                    formatProjectLabel(bNum)
                } else {
                    null
                }
            }
            else -> null
        }
    }

    internal fun hexToBytes(hex: String): ByteArray {
        val clean = hex.trim()
        require(clean.length % 2 == 0) { "Hex string must have even length" }
        return ByteArray(clean.length / 2) { i ->
            clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}
