package it.attendance100.mybicocca.data.remote.esse3.api

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AddressType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ContactInfo
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ResidenceAddress
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class Esse3ProfileApiTest : Esse3TestBase() {
    companion object {
        private val MOCK_ADDRESS = Esse3ResidenceAddress(
            country = "ITALIA",
            province = "MI",
            city = "Milano",
            zipCode = "20126",
            district = "",
            street = "Via Bicocca degli Arcimboldi",
            houseNumber = "8",
            phone = "+390264481",
            coincidesWithDomicile = true
        )

        private val MOCK_CONTACT_INFO = Esse3ContactInfo(
            documentDelivery = Esse3AddressType.RESIDENCE,
            taxDelivery = Esse3AddressType.RESIDENCE,
            email = "test.studente@campus.unimib.it",
            fax = "+390264482",
            mobile = "+393331234567",
            privacyConsent = true
        )
    }

    @Test
    suspend fun getPhoto() {
        val photoChannel = api.profile.getPhoto()
        assertNotNull(photoChannel)
    }

    @Test
    suspend fun getPersonalData() {
        val personalData = api.profile.getPersonalData()
        assertNotNull(personalData.name)
        assertNotNull(personalData.surname)
        assertNotNull(personalData.fiscalcode)
        assertNotNull(personalData.birthDate)
    }

    @Test
    suspend fun getResidenceAddressOptions() {
        val options = api.profile.getResidenceAddressOptions()
        assertNotNull(options.countries)
        assertNotNull(options.provinces)
        assertNotNull(options.cities)
        assertTrue(options.countries.isNotEmpty())
    }

    @Test
    suspend fun getResidenceAddress() {
        val address = api.profile.getResidenceAddress()
        assertNotNull(address)
    }

    @Test
    suspend fun updateResidenceAddress() {
        val originalAddress = api.profile.getResidenceAddress()

        api.profile.updateResidenceAddress(MOCK_ADDRESS)

        val verifyAddress = api.profile.getResidenceAddress()
        assertNotNull(verifyAddress)

        api.profile.updateResidenceAddress(originalAddress)

        val restoredAddress = api.profile.getResidenceAddress()
        assertNotNull(restoredAddress)
    }

    @Test
    suspend fun getContactInfo() {
        val contactInfo = api.profile.getContactInfo()
        assertNotNull(contactInfo.documentDelivery)
        assertNotNull(contactInfo.taxDelivery)
        assertNotNull(contactInfo.email)
    }

    @Test
    suspend fun updateContactInfo() {
        val originalContactInfo = api.profile.getContactInfo()

        api.profile.updateContactInfo(MOCK_CONTACT_INFO)

        val verifyContactInfo = api.profile.getContactInfo()
        assertNotNull(verifyContactInfo)

        api.profile.updateContactInfo(originalContactInfo)

        val restoredContactInfo = api.profile.getContactInfo()
        assertNotNull(restoredContactInfo)
    }
}
