package it.attendance100.mybicocca.data.api.esse3

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import it.attendance100.mybicocca.data.dto.esse3.*

/**
 * Manages student profile operations including personal data, residence, and contact details.
 */
class Esse3ProfileApi(
    client: HttpClient
) : Esse3AbstractApi(client) {
    companion object {
        private const val ANAGRAFICA_ENTRY_POINT = "/auth/studente/Anagrafica/Anagrafica.do?menu_opened_cod=menu_link-navbox_studenti_Anagrafica"
        private const val RESIDENCE_FORM = "/auth/AddressBook/ABSubWizIndirizziResForm.do"
        private const val CONTACT_FORM = "/auth/AddressBook/ABSubWizRecapitoForm.do"
        private const val ADDRESS_SUBMIT = "/AddressBook/IndirizziSubmit.do"
        private const val CONTACT_SUBMIT = "/AddressBook/RecapitoSubmit.do"
        private const val PHOTO_DATA = "/auth/AddressBook/DownloadFoto.do"
    }

    /**
     * Downloads the student's ID photo.
     *
     * @return A [ByteReadChannel] containing the image data.
     * @throws IllegalStateException If the server returns a non-OK status code.
     */
    suspend fun getPhoto(): ByteReadChannel {
        val response = executeGetRaw(
            PHOTO_DATA,
            mapOf("r" to System.currentTimeMillis().toString())
        )
        if(response.status != HttpStatusCode.OK) {
            throw IllegalStateException("Error getting photo: status code ${response.status.value}")
        }
        return response.bodyAsChannel()
    }

    /**
     * Retrieves the student's personal registry data (Anagrafica).
     *
     * @return The parsed [Esse3PersonalData].
     * @throws IllegalStateException If the required HTML elements or specific data fields are missing.
     */
    suspend fun getPersonalData(): Esse3PersonalData {
        val doc = executeGet(ANAGRAFICA_ENTRY_POINT)

        val personalData = doc.selectFirst(".idsummaryFormNestedTemplateBox_1")
            ?: throw IllegalStateException("Error getting personal data: missing 'idsummaryFormNestedTemplateBox_1' table")
        val personalDataMap = personalData.select("dt").associate { dt ->
            val key = dt.text().trim().removeSuffix(":")
            val value = dt.nextElementSibling()
            key to value?.nodeValue()
        }

        val name = personalDataMap["Nome"]
            ?: throw IllegalStateException("Error getting personal data: missing 'Nome' in table")
        val surname = personalDataMap["Cognome"]
            ?: throw IllegalStateException("Error getting personal data: missing 'Cognome' in table")
        val sex = personalDataMap["Sesso"]?.let { Esse3Sex.fromCode(it) }
            ?: throw IllegalStateException("Error getting personal data: missing 'Sesso' in table")
        val birthDate = personalDataMap["Data di nascita"]?.let { parseDate(it) }
            ?: throw IllegalStateException("Error getting personal data: missing 'Data di nascita' in table")
        val citizenship = personalDataMap["Cittadinanza"]
            ?: throw IllegalStateException("Error getting personal data: missing 'Cittadinanza' table")
        val birthCountry = personalDataMap["Nazione di nascita"]
            ?: throw IllegalStateException("Error getting personal data: missing 'Nazione di nascita' table")
        val birthProvince = personalDataMap["Provincia di nascita"]
            ?: throw IllegalStateException("Error getting personal data: missing 'Provincia di nascita' in table")
        val birthCity = personalDataMap["Comune/Città di nascita"]
            ?: throw IllegalStateException("Error getting personal data: missing 'Comune/Città di nascita' in table")
        val fiscalcode = personalDataMap["Codice Fiscale"]
            ?: throw IllegalStateException("Error getting personal data: missing 'Codice Fiscale' in table")

        return Esse3PersonalData(
            name = name,
            surname = surname,
            sex = sex,
            birthDate = birthDate,
            citizenship = citizenship,
            birthCountry = birthCountry,
            birthProvince = birthProvince,
            birthCity = birthCity,
            fiscalcode = fiscalcode
        )
    }

    /**
     * Retrieves valid form options for updating a residence address.
     *
     * @return An object containing lists of valid Countries, Provinces, and Cities.
     * @throws IllegalStateException If the form or select elements cannot be found in the DOM.
     */
    suspend fun getResidenceAddressOptions(): Esse3ResidenceAddressOptions {
        executeGet(ANAGRAFICA_ENTRY_POINT)
        val doc = executeGet(RESIDENCE_FORM)

        val form = doc.selectFirst("form[action*=IndirizziSubmit]")
            ?: throw IllegalStateException("Error getting residence address options: missing form")

        val countrySelector = form.selectFirst("#selectionNazione")
            ?: throw IllegalStateException("Error getting residence address: missing country selector")
        val countries = countrySelector.select("option")
            .map { it.attr("title") }

        val provinceSelector = form.selectFirst("#selectionProvincia")
            ?: throw IllegalStateException("Error getting residence address: missing province selector")
        val provinces = provinceSelector.select("option")
            .map { it.attr("title") }

        val citySelector = form.selectFirst("#cmbComuni")
            ?: throw IllegalStateException("Error getting residence address: missing city selector")
        val cities = citySelector.select("option")
            .map { it.attr("title") }

        return Esse3ResidenceAddressOptions(
            countries = countries,
            provinces = provinces,
            cities = cities
        )
    }

    /**
     * Retrieves the current residence address details populated in the platform.
     *
     * @return The current [Esse3ResidenceAddress].
     * @throws IllegalStateException If the address form or specific input fields are missing.
     */
    suspend fun getResidenceAddress(): Esse3ResidenceAddress {
        executeGet(ANAGRAFICA_ENTRY_POINT)
        val doc = executeGet(RESIDENCE_FORM)

        val form = doc.selectFirst("form[action*=IndirizziSubmit]")
            ?: throw IllegalStateException("Error getting residence address: missing form")

        val countrySelector = form.selectFirst("#selectionNazione")
            ?: throw IllegalStateException("Error getting residence address: missing country selector")
        val country = countrySelector.selectFirst("option[selected]")?.attr("value")
            ?: ""

        val provinceSelector = form.selectFirst("#selectionProvincia")
            ?: throw IllegalStateException("Error getting residence address: missing province selector")
        val province = provinceSelector.selectFirst("option[selected]")?.attr("value")
            ?: ""

        val citySelector = form.selectFirst("#cmbComuni")
            ?: throw IllegalStateException("Error getting residence address: missing city selector")
        val city = citySelector.selectFirst("option[selected]")?.attr("value")
            ?: ""

        val zipCodeInput = form.selectFirst("input[id*=cap_res]")
            ?: throw IllegalStateException("Error getting residence address: missing zip input")
        val zipCode = zipCodeInput.attr("value")

        val districtInput = form.selectFirst("input[id*=fraz_res]")
            ?: throw IllegalStateException("Error getting residence address: missing district input")
        val district = districtInput.attr("value")

        val streetInput = form.selectFirst("input[id*=via_res]")
            ?: throw IllegalStateException("Error getting residence address: missing street input")
        val street = streetInput.attr("value")

        val houseNumberInput = form.selectFirst("input[id*=num_civ_res]")
            ?: throw IllegalStateException("Error getting residence address: missing house number input")
        val houseNumber = houseNumberInput.attr("value")

        val phoneInput = form.selectFirst("input[id*=tel_res]")
            ?: throw IllegalStateException("Error getting residence address: missing phone input")
        val phone = phoneInput.attr("value")

        val coincidesWithDomicileInput = form.selectFirst("input[id*=dom_come_res_flg1]")
            ?: throw IllegalStateException("Error getting residence address: missing domicile coincides with residence radio button")
        val coincidesWithDomicile = coincidesWithDomicileInput.attr("value") == "1"

        return Esse3ResidenceAddress(
            country = country,
            province = province,
            city = city,
            zipCode = zipCode,
            district = district,
            street = street,
            houseNumber = houseNumber,
            phone = phone,
            coincidesWithDomicile = coincidesWithDomicile
        )
    }

    /**
     * Submits a new residence address to the platform.
     *
     * @param address The new address data.
     * @throws IllegalStateException If the provided country, province, or city does not match valid platform options.
     */
    suspend fun updateResidenceAddress(address: Esse3ResidenceAddress) {
        executeGet(ANAGRAFICA_ENTRY_POINT)
        val doc = executeGet(RESIDENCE_FORM)

        val form = doc.selectFirst("form[action*=IndirizziSubmit]")
            ?: throw IllegalStateException("Error getting residence address: missing form")

        val countrySelector = form.selectFirst("#selectionNazione")
            ?: throw IllegalStateException("Error getting residence address: missing country selector")
        val countryValue = countrySelector.select("option")
            .firstOrNull { it.text() == address.country }
            ?.attr("value")
            ?: throw IllegalStateException("Error getting residence address: country '${address.country}' not found")

        val provinceSelector = form.selectFirst("#selectionProvincia")
            ?: throw IllegalStateException("Error getting residence address: missing province selector")
        val provinceValue = provinceSelector.select("option")
            .firstOrNull { it.text() == address.province }
            ?.attr("value")
            ?: throw IllegalStateException("Error getting residence address: province '${address.province}' not found")

        val citySelector = form.selectFirst("#cmbComuni")
            ?: throw IllegalStateException("Error getting residence address: missing city selector")
        val cityValue = citySelector.select("option")
            .firstOrNull { it.text() == address.province }
            ?.attr("value")
            ?: throw IllegalStateException("Error getting residence address: city '${address.city}' not found")

        val formFields = Parameters.build {
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/naz_res_id", countryValue)
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/p01_comu_comu_res_sigla", provinceValue)
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/cap_res", address.zipCode)
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/com_res_id", cityValue)
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/fraz_res", address.district)
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/via_res", address.street)
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/num_civ_res", address.houseNumber)
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/tel_res", address.phone)
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/dom_come_res_flg", if (address.coincidesWithDomicile) "1" else "0")
            append("form_id_formResDom", "formResDom")
            append("procedi", "Avanti")
        }
        val response = executePostRaw(ADDRESS_SUBMIT, formFields)
        checkForUpdateError(response)
    }

    /**
     * Retrieves current contact information preferences.
     *
     * @return The current [Esse3ContactInfo].
     * @throws IllegalStateException If the contact form or required inputs are missing.
     */
    suspend fun getContactInfo(): Esse3ContactInfo {
        executeGet(ANAGRAFICA_ENTRY_POINT)
        val doc = executeGet(CONTACT_FORM)

        val form = doc.selectFirst("form[action*=RecapitoSubmit]")
            ?: throw IllegalStateException("Error getting contact info: missing form")

        val documentDeliveryInput = form.selectFirst("input[name*=tipo_indiriz_cod][checked]")
            ?: throw IllegalStateException("Error getting contact info: missing document delivery radio button")
        val documentDeliveryValue = documentDeliveryInput.attr("value")
        val documentDelivery = Esse3AddressType.fromCode(documentDeliveryValue)
            ?: throw IllegalStateException("Error getting contact info: invalid document delivery code '$documentDeliveryValue'")

        val taxDeliveryInput = form.selectFirst("input[name*=recapito_tasse][checked]")
            ?: throw IllegalStateException("Error getting contact info: missing tax delivery radio button")
        val taxDeliveryValue = taxDeliveryInput.attr("value")
        val taxDelivery = Esse3AddressType.fromCode(taxDeliveryValue)
            ?: throw IllegalStateException("Error getting contact info: invalid tax delivery code '$taxDeliveryValue'")

        val emailInput = form.selectFirst("input[id*=email]")
            ?: throw IllegalStateException("Error getting contact info: missing email input")
        val email = emailInput.attr("value")

        val faxInput = form.selectFirst("input[id*=fax]")
            ?: throw IllegalStateException("Error getting contact info: missing fax input")
        val fax = faxInput.attr("value")

        val mobilePrefixSelector = form.selectFirst("#INTL_PREFIX_CELLULARE")
            ?: throw IllegalStateException("Error getting contact info: missing mobile prefix selector")
        val mobilePrefix = mobilePrefixSelector.selectFirst("option[selected]")?.attr("value")
            ?: ""

        val mobileInput = form.selectFirst("input[id*=cellulare]")
            ?: throw IllegalStateException("Error getting contact info: missing mobile input")
        val mobile = "${mobilePrefix}${mobileInput.attr("value")}$"

        val privacyConsentInput = form.selectFirst("input[name*=cons_dp_flg][checked]")
            ?: throw IllegalStateException("Error getting contact info: missing privacy consent radio button")
        val privacyConsent = privacyConsentInput.attr("value") == "1"

        return Esse3ContactInfo(
            documentDelivery = documentDelivery,
            taxDelivery = taxDelivery,
            email = email,
            fax = fax,
            mobile = mobile,
            privacyConsent = privacyConsent
        )
    }

    /**
     * Updates the student's contact information.
     *
     * @param contactInfo The new contact data.
     * @throws IllegalArgumentException If the provided phone number cannot be parsed.
     */
    suspend fun updateContactInfo(contactInfo: Esse3ContactInfo) {
        fun parsePhoneNumber(input: String): Pair<String, String> {
            try {
                val phoneUtil = PhoneNumberUtil.getInstance()
                val numberProto = phoneUtil.parse(input, "IT")
                val prefix = "+${numberProto.countryCode}"
                val national = phoneUtil.getNationalSignificantNumber(numberProto)
                return Pair(prefix, national)
            } catch (_: NumberParseException) {
                throw IllegalArgumentException("Invalid phone number: $input")
            }
        }

        val (prefix, national) = parsePhoneNumber(contactInfo.mobile)

        executeGet(ANAGRAFICA_ENTRY_POINT)
        executeGet(CONTACT_FORM)

        val formFields = Parameters.build {
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/tipo_indiriz_cod", contactInfo.documentDelivery.code)
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/recapito_tasse", contactInfo.taxDelivery.code)
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/email", contactInfo.email)
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/fax", contactInfo.fax)
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/cellulare", national)
            append("INTL_PREFIX_CELLULARE", "")
            append("INTL_PREFIX_TXT_CELLULARE",  prefix)
        }
        val response = executePostRaw(CONTACT_SUBMIT, formFields)
        checkForUpdateError(response)
    }
}