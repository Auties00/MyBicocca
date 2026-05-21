package it.attendance100.mybicocca.data.remote.esse3.api

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import it.attendance100.mybicocca.data.remote.common.exception.ApiRequestException
import it.attendance100.mybicocca.data.remote.common.exception.HtmlParsingException
import it.attendance100.mybicocca.data.remote.common.util.parseGrid
import it.attendance100.mybicocca.data.remote.esse3.dto.*

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
     * @throws HtmlParsingException If the server returns a non-OK status code.
     */
    suspend fun getPhoto(): ByteReadChannel {
        val response = executeGetRaw(
            PHOTO_DATA,
            mapOf("r" to System.currentTimeMillis().toString())
        )
        if(response.status != HttpStatusCode.OK) {
            throw ApiRequestException(response.status.value, "Error getting photo: status code ${response.status.value}")
        }
        return response.bodyAsChannel()
    }

    /**
     * Retrieves the student's personal registry data (Anagrafica).
     *
     * @return The parsed [Esse3PersonalData].
     * @throws HtmlParsingException If the required HTML elements or specific data fields are missing.
     */
    suspend fun getPersonalData(): Esse3PersonalData {
        val doc = executeGet(ANAGRAFICA_ENTRY_POINT)

        val data = doc.selectFirst("#idsummaryFormNestedTemplateBox_1")?.parseGrid()
            ?: throw HtmlParsingException("Error getting personal data: missing 'idsummaryFormNestedTemplateBox_1' table")

        val name = data.getTextOrThrow("nome")
        val surname = data.getTextOrThrow("cognome")
        val sex = data.getTextAsOrThrow("sesso") { Esse3Sex.fromCode(it) }
        val birthDate = data.getTextAsOrThrow("data di nascita") { parseDate(it) }
        val citizenship = data.getTextOrThrow("cittadinanza")
        val birthCountry = data.getTextOrThrow("nazione di nascita")
        val birthProvince = data.getTextOrThrow("provincia di nascita")
        val birthCity = data.getTextOrThrow("comune/città di nascita")
        val fiscalcode = data.getTextOrThrow("codice fiscale")

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
     * @throws HtmlParsingException If the form or select elements cannot be found in the DOM.
     */
    suspend fun getResidenceAddressOptions(): Esse3ResidenceAddressOptions {
        executeGet(ANAGRAFICA_ENTRY_POINT)
        val doc = executeGet(RESIDENCE_FORM)

        val form = doc.selectFirst("form[action*=IndirizziSubmit]")
            ?: throw HtmlParsingException("Error getting residence address options: missing form")

        val countrySelector = form.selectFirst("#selectionNazione")
            ?: throw HtmlParsingException("Error getting residence address: missing country selector")
        val countries = countrySelector.select("option")
            .map { it.attr("title") }

        val provinceSelector = form.selectFirst("#selectionProvincia")
            ?: throw HtmlParsingException("Error getting residence address: missing province selector")
        val provinces = provinceSelector.select("option")
            .map { it.attr("title") }

        val citySelector = form.selectFirst("#cmbComuni")
            ?: throw HtmlParsingException("Error getting residence address: missing city selector")
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
     * @throws HtmlParsingException If the address form or specific input fields are missing.
     */
    suspend fun getResidenceAddress(): Esse3ResidenceAddress {
        executeGet(ANAGRAFICA_ENTRY_POINT)
        val doc = executeGet(RESIDENCE_FORM)

        val form = doc.selectFirst("form[action*=IndirizziSubmit]")
            ?: throw HtmlParsingException("Error getting residence address: missing form")

        val countrySelector = form.selectFirst("#selectionNazione")
            ?: throw HtmlParsingException("Error getting residence address: missing country selector")
        val country = countrySelector.selectFirst("option[selected]")?.attr("title")
            ?: ""

        val provinceSelector = form.selectFirst("#selectionProvincia")
            ?: throw HtmlParsingException("Error getting residence address: missing province selector")
        val province = provinceSelector.selectFirst("option[selected]")?.attr("title")
            ?: ""

        val citySelector = form.selectFirst("#cmbComuni")
            ?: throw HtmlParsingException("Error getting residence address: missing city selector")
        val city = citySelector.selectFirst("option[selected]")?.attr("title")
            ?: ""

        val zipCodeInput = form.selectFirst("input[id*=cap_res]")
            ?: throw HtmlParsingException("Error getting residence address: missing zip input")
        val zipCode = zipCodeInput.attr("value")

        val districtInput = form.selectFirst("input[id*=fraz_res]")
            ?: throw HtmlParsingException("Error getting residence address: missing district input")
        val district = districtInput.attr("value")

        val streetInput = form.selectFirst("input[id*=via_res]")
            ?: throw HtmlParsingException("Error getting residence address: missing street input")
        val street = streetInput.attr("value")

        val houseNumberInput = form.selectFirst("input[id*=num_civ_res]")
            ?: throw HtmlParsingException("Error getting residence address: missing house number input")
        val houseNumber = houseNumberInput.attr("value")

        val phoneInput = form.selectFirst("input[id*=tel_res]")
            ?: throw HtmlParsingException("Error getting residence address: missing phone input")
        val phone = phoneInput.attr("value")

        val coincidesWithDomicileInput = form.selectFirst("input[id*=dom_come_res_flg1]")
            ?: throw HtmlParsingException("Error getting residence address: missing domicile coincides with residence radio button")
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
     * @throws HtmlParsingException If the provided country, province, or city does not match valid platform options.
     */
    suspend fun updateResidenceAddress(address: Esse3ResidenceAddress) {
        executeGet(ANAGRAFICA_ENTRY_POINT)
        val doc = executeGet(RESIDENCE_FORM)

        val form = doc.selectFirst("form[action*=IndirizziSubmit]")
            ?: throw HtmlParsingException("Error getting residence address: missing form")

        val countrySelector = form.selectFirst("#selectionNazione")
            ?: throw HtmlParsingException("Error getting residence address: missing country selector")
        val countryValue = countrySelector.select("option")
            .firstOrNull { it.attr("title") == address.country || it.attr("value") == address.country }
            ?.attr("value")
            ?: throw HtmlParsingException("Error getting residence address: country '${address.country}' not found")

        val provinceSelector = form.selectFirst("#selectionProvincia")
            ?: throw HtmlParsingException("Error getting residence address: missing province selector")
        val provinceValue = provinceSelector.select("option")
            .firstOrNull { it.attr("title") == address.province || it.attr("value") == address.province }
            ?.attr("value")
            ?: throw HtmlParsingException("Error getting residence address: province '${address.province}' not found")

        val citySelector = form.selectFirst("#cmbComuni")
            ?: throw HtmlParsingException("Error getting residence address: missing city selector")
        val cityValue = citySelector.select("option")
            .firstOrNull { it.attr("title") == address.city || it.attr("value") == address.city }
            ?.attr("value")
            ?: throw HtmlParsingException("Error getting residence address: city '${address.city}' not found")

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
     * @throws HtmlParsingException If the contact form or required inputs are missing.
     */
    suspend fun getContactInfo(): Esse3ContactInfo {
        executeGet(ANAGRAFICA_ENTRY_POINT)
        val doc = executeGet(CONTACT_FORM)

        val form = doc.selectFirst("form[action*=RecapitoSubmit]")
            ?: throw HtmlParsingException("Error getting contact info: missing form")

        val documentDeliveryInput = form.selectFirst("input[name*=tipo_indiriz_cod][checked]")
            ?: throw HtmlParsingException("Error getting contact info: missing document delivery radio button")
        val documentDeliveryValue = documentDeliveryInput.attr("value")
        val documentDelivery = Esse3AddressType.fromCode(documentDeliveryValue)
            ?: throw HtmlParsingException("Error getting contact info: invalid document delivery code '$documentDeliveryValue'")

        val taxDeliveryInput = form.selectFirst("input[name*=recapito_tasse][checked]")
            ?: throw HtmlParsingException("Error getting contact info: missing tax delivery radio button")
        val taxDeliveryValue = taxDeliveryInput.attr("value")
        val taxDelivery = Esse3AddressType.fromCode(taxDeliveryValue)
            ?: throw HtmlParsingException("Error getting contact info: invalid tax delivery code '$taxDeliveryValue'")

        val emailInput = form.selectFirst("input[id*=email]")
            ?: throw HtmlParsingException("Error getting contact info: missing email input")
        val email = emailInput.attr("value")

        val faxInput = form.selectFirst("input[id*=fax]")
            ?: throw HtmlParsingException("Error getting contact info: missing fax input")
        val fax = faxInput.attr("value")

        val mobilePrefixSelector = form.selectFirst("#INTL_PREFIX_CELLULARE")
            ?: throw HtmlParsingException("Error getting contact info: missing mobile prefix selector")
        val mobilePrefix = mobilePrefixSelector.selectFirst("option[selected]")?.attr("value")
            ?: ""

        val mobileInput = form.selectFirst("input[id*=/cellulare]")?.attr("value")
            ?: throw HtmlParsingException("Error getting contact info: missing mobile input")
        val mobile = "${mobilePrefix}${mobileInput}"

        val privacyConsentInput = form.selectFirst("input[name*=cons_dp_flg][checked]")
            ?: throw HtmlParsingException("Error getting contact info: missing privacy consent radio button")
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