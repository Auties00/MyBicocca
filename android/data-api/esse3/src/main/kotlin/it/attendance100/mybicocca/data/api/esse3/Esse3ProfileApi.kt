package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.http.Parameters
import it.attendance100.mybicocca.data.dto.esse3.*
import org.jsoup.nodes.Document

/**
 * API for student profile operations.
 *
 * Provides access to:
 * - Residence/domicile addresses
 * - Contact information
 * - Privacy consents
 * - Disability declarations
 * - Photo
 */
class Esse3ProfileApi(
    client: HttpClient
) : Esse3AbstractApi(client) {
    companion object {
        // Entry point URL that initializes the address book wizard
        private const val ANAGRAFICA_ENTRY_POINT =
            "/auth/studente/Anagrafica/Anagrafica.do?menu_opened_cod=menu_link-navbox_studenti_Anagrafica"

        // Subform URLs (must be accessed after initializing wizard state)
        private const val RESIDENCE_FORM = "/auth/AddressBook/ABSubWizIndirizziResForm.do"
        private const val CONTACT_FORM = "/auth/AddressBook/ABSubWizRecapitoForm.do"

        // Submit URLs (note: no /auth/ prefix for address/contact)
        private const val ADDRESS_SUBMIT = "/AddressBook/IndirizziSubmit.do"
        private const val CONTACT_SUBMIT = "/AddressBook/RecapitoSubmit.do"
    }

    /**
     * Initializes the address book wizard by calling the entry point.
     * This sets up the server-side session state required for accessing subforms.
     */
    private suspend fun initAddressBookWizard() {
        executeGet(ANAGRAFICA_ENTRY_POINT)
    }

    /**
     * Gets the student's photo as bytes.
     *
     * @return The photo bytes or null if not available
     */
    suspend fun getPhotoBytes(): ByteArray? {
        return runCatching {
            val response = executeGetRaw(
                "/auth/AddressBook/DownloadFoto.do",
                mapOf("r" to System.currentTimeMillis().toString())
            )
            response.call.body<ByteArray>()
        }.getOrNull()
    }

    /**
     * Gets the residence address form with current values.
     *
     * @return Current address data
     */
    suspend fun getResidenceAddress(): Esse3Address? {
        initAddressBookWizard()
        val doc = executeGet(RESIDENCE_FORM)
        return parseAddressForm(doc)
    }

    /**
     * Updates the residence address.
     *
     * @param address The new address data
     * @param domicileSameAsResidence Whether domicile should be same as residence
     * @return True if successful
     */
    suspend fun updateResidenceAddress(
        address: Esse3Address,
        domicileSameAsResidence: Boolean = true
    ) {
        initAddressBookWizard()
        executeGet(RESIDENCE_FORM)
        val formFields = Parameters.build {
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/naz_res_id", address.countryId?.toString() ?: "")
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/p01_comu_comu_res_sigla", address.provinceCode ?: "")
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/com_res_id", address.municipalityId?.toString() ?: "")
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/cap_res", address.zipCode ?: "")
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/fraz_res", address.district ?: "")
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/via_res", address.street ?: "")
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/num_civ_res", address.houseNumber ?: "")
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/tel_res", address.phone ?: "")
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/dom_come_res_flg", if (domicileSameAsResidence) "1" else "0")
            append("form_id_formResDom", "formResDom")
            append("procedi", "Avanti")
        }
        val response = executePostRaw(ADDRESS_SUBMIT, formFields)
        checkForUpdateError(response)
    }

    /**
     * Gets the contact information form with current values.
     *
     * @return Current contact data
     */
    suspend fun getContactInfo(): Esse3ContactInfo {
        // First, initialize the wizard state
        initAddressBookWizard()
        // Then navigate to the contact form
        val doc = executeGet(CONTACT_FORM)
        return parseContactForm(doc)
    }

    /**
     * Updates contact information.
     *
     * @param contactInfo The new contact data
     * @return True if successful
     */
    suspend fun updateContactInfo(contactInfo: Esse3ContactInfo) {
        initAddressBookWizard()
        executeGet(CONTACT_FORM)
        val formFields = Parameters.build {
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/tipo_indiriz_cod", contactInfo.contactAddressType?.code ?: "")
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/recapito_tasse", contactInfo.taxCorrespondenceAddress?.code ?: "")
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/email", contactInfo.email ?: "")
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/fax", contactInfo.fax ?: "")
            append("/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/cellulare", contactInfo.mobilePhone ?: "")
            if(contactInfo.mobilePrefix != null) {
                append("INTL_PREFIX_CELLULARE", "")
                append("INTL_PREFIX_TXT_CELLULARE",  contactInfo.mobilePrefix)
            } else {
                append("INTL_PREFIX_CELLULARE", "+39")
                append("INTL_PREFIX_TXT_CELLULARE", "")
            }
        }
        val response = executePostRaw(CONTACT_SUBMIT, formFields)
        checkForUpdateError(response)
    }

    private fun parseAddressForm(doc: Document): Esse3Address? {
        val form = doc.selectFirst("form[action*=IndirizziSubmit]") ?: return null

        fun getValue(field: String): String? {
            val input = form.selectFirst("input[name*='/$field'], select[name*='/$field']")
            return when (input?.tagName()) {
                "select" -> input.selectFirst("option[selected]")?.attr("value")
                else -> input?.attr("value")
            }?.takeIf { it.isNotBlank() }
        }

        return Esse3Address(
            countryId = getValue("naz_res_id"),
            countryName = form.selectFirst("select[name*=naz_res_id] option[selected]")?.text(),
            provinceCode = getValue("p01_comu_comu_res_sigla"),
            municipalityId = getValue("com_res_id"),
            municipalityName = form.selectFirst("select[name*=com_res_id] option[selected]")?.text(),
            zipCode = getValue("cap_res"),
            district = getValue("fraz_res"),
            street = getValue("via_res"),
            houseNumber = getValue("num_civ_res"),
            phone = getValue("tel_res")
        )
    }

    private fun parseContactForm(doc: Document): Esse3ContactInfo {
        val form = doc.selectFirst("form[action*=RecapitoSubmit]") ?: return Esse3ContactInfo(
            null, null, null, null, null, null
        )

        fun getValue(field: String): String? {
            // Find inputs that contain the field name in their name attribute
            val input = form.select("input, select").firstOrNull { element ->
                val name = element.attr("name")
                name.endsWith("/$field") || name == field
            }
            return input?.attr("value")?.takeIf { it.isNotBlank() }
        }

        fun getRadioValue(field: String): String? {
            // Find checked radio inputs that contain the field name
            return form.select("input[type=radio][checked]").firstOrNull { element ->
                val name = element.attr("name")
                name.endsWith("/$field") || name.contains(field)
            }?.attr("value")
        }


        val email = getValue("email")
        val mobilePhone = getValue("cellulare")
        val mobilePrefixSelector = getValue("INTL_PREFIX_CELLULARE")
        val mobilePrefixTxt = getValue("INTL_PREFIX_CELLULARE")
        val mobilePrefix = if(mobilePrefixSelector != null && mobilePrefixSelector.startsWith("+")) {
            mobilePrefixSelector
        } else {
            mobilePrefixTxt ?: "+39"
        }
        val fax = getValue("fax")
        val contactAddressType = getRadioValue("tipo_indiriz_cod")?.let {
            Esse3AddressType.fromCode(it)
        }
        val taxCorrespondenceAddress = getRadioValue("recapito_tasse")?.let {
            Esse3AddressType.fromCode(it)
        }
        return Esse3ContactInfo(
            email = email,
            mobilePhone = mobilePhone,
            mobilePrefix = mobilePrefix,
            fax = fax,
            contactAddressType = contactAddressType,
            taxCorrespondenceAddress = taxCorrespondenceAddress
        )
    }
}
