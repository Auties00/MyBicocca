package it.attendance100.mybicocca.data.dto.esse3

/**
 * Address information (residence or domicile).
 */
data class Esse3Address(
    val countryId: String?,
    val countryName: String?,
    val provinceCode: String?,
    val municipalityId: String?,
    val municipalityName: String?,
    val zipCode: String?,
    val district: String?,
    val street: String?,
    val houseNumber: String?,
    val phone: String?
)

/**
 * Contact information.
 */
data class Esse3ContactInfo(
    val email: String?,
    val mobilePhone: String?,
    val mobilePrefix: String?,
    val fax: String?,
    val contactAddressType: Esse3AddressType?,
    val taxCorrespondenceAddress: Esse3AddressType?
)

/**
 * Type of address for correspondence.
 */
enum class Esse3AddressType(val code: String) {
    RESIDENCE("R"),
    DOMICILE("D");

    companion object {
        fun fromCode(code: String): Esse3AddressType? = entries.find { it.code == code }
    }
}