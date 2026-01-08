package it.attendance100.mybicocca.data.dto.esse3

import java.time.LocalDate


/**
 * Personal data.
 */
data class Esse3PersonalData(
    val name: String,
    val surname: String,
    val sex: Esse3Sex,
    val birthDate: LocalDate,
    val citizenship: String,
    val birthCountry: String,
    val birthProvince: String,
    val birthCity: String,
    val fiscalcode: String
)

/**
 * Birth sex.
 */
enum class Esse3Sex(val code: String) {
    MALE("Maschio"),
    FEMALE("Femmina");

    companion object {
        fun fromCode(code: String): Esse3Sex? = Esse3Sex.entries.find { it.code == code }
    }
}

/**
 * Address options for dropdown menus.
 */
data class Esse3ResidenceAddressOptions(
    val countries: List<String>,
    val cities: List<String>,
    val provinces: List<String>
)

/**
 * Address information (residence or domicile).
 */
data class Esse3ResidenceAddress(
    val country: String,
    val province: String,
    val city: String,
    val zipCode: String,
    val district: String,
    val street: String,
    val houseNumber: String,
    val phone: String,
    val coincidesWithDomicile: Boolean
)

/**
 * Contact information.
 */
data class Esse3ContactInfo(
    val documentDelivery: Esse3AddressType,
    val taxDelivery: Esse3AddressType,
    val email: String,
    val fax: String,
    val mobile: String,
    val privacyConsent: Boolean
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