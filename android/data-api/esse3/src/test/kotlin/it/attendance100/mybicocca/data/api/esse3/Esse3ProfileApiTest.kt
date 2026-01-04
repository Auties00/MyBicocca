package it.attendance100.mybicocca.data.api.esse3

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Integration tests for [Esse3ProfileApi].
 */
class Esse3ProfileApiTest : Esse3TestBase() {
    companion object {
        private const val TEST_PHONE = "+393476927192"
        private const val TEST_FAX = "+390212345678"
        private const val TEST_EMAIL = "test.automated@example.com"
        private const val TEST_MOBILE = "3331234567"
        private const val TEST_STREET = "Via Test Automatizzato"
        private const val TEST_HOUSE_NUMBER = "999"
    }

    @Test
    fun `getPhotoBytes returns photo data`() {
        runBlocking {
            val photoBytes = api.profile.getPhotoBytes()

            if (photoBytes != null) {
                assertTrue(photoBytes.isNotEmpty(), "Photo bytes should not be empty")
                println("Photo retrieved: ${photoBytes.size} bytes")

                // Check if it starts with common image headers
                val isJpeg = photoBytes.size > 2 && photoBytes[0] == 0xFF.toByte() && photoBytes[1] == 0xD8.toByte()
                val isPng = photoBytes.size > 8 && photoBytes[0] == 0x89.toByte() && photoBytes[1] == 0x50.toByte()

                if (isJpeg) println("Format: JPEG")
                if (isPng) println("Format: PNG")
            } else {
                println("No photo available for this student")
            }
        }
    }

    @Test
    fun `getResidenceAddress returns address data`() {
        runBlocking {
            val address = api.address.getResidenceAddress()
                ?: fail("Residence address should not be null")

            println("Residence address retrieved:")
            address.countryName?.let { println("  Country: $it") }
            address.provinceCode?.let { println("  Province: $it") }
            address.municipalityName?.let { println("  Municipality: $it") }
            address.zipCode?.let { println("  ZIP: $it") }
            address.street?.let { println("  Street: $it") }
            address.houseNumber?.let { println("  Number: $it") }
            address.phone?.let { println("  Phone: $it") }
        }
    }

    @Test
    fun `getContactInfo returns contact data`() {
        runBlocking {
            val contactInfo = api.address.getContactInfo()

            assertNotNull(contactInfo, "Contact info should not be null")

            println("Contact information retrieved:")
            contactInfo.email?.let { println("  Email: $it") }
            contactInfo.mobilePhone?.let { println("  Mobile: ${contactInfo.mobilePrefix}$it") }
            contactInfo.fax?.let { println("  Fax: $it") }
            contactInfo.contactAddressType?.let { println("  Contact Address Type: $it") }
            contactInfo.taxCorrespondenceAddress?.let { println("  Tax Correspondence: $it") }
        }
    }

    @Test
    fun `updateResidenceAddress changes data and rollback restores original`() {
        runBlocking {
            // Step 1: Get the original address
            val originalAddress = api.address.getResidenceAddress()
                ?: fail("Original residence address should not be null")

            println("Original residence address:")
            println("  Street: ${originalAddress.street}")
            println("  Number: ${originalAddress.houseNumber}")
            println("  ZIP: ${originalAddress.zipCode}")
            println("  Phone: ${originalAddress.phone}")

            // Step 2: Create a modified address with multiple changes
            val modifiedAddress = originalAddress.copy(
                street = TEST_STREET,
                houseNumber = TEST_HOUSE_NUMBER,
                phone = TEST_PHONE
            )

            // Step 3: Update with modified data
            println("\nUpdating residence address with new street, number and phone...")
            api.address.updateResidenceAddress(modifiedAddress)

            // Step 4: Verify the data actually changed
            val updatedAddress = api.address.getResidenceAddress()
                ?: fail("Updated residence address should not be null")
            
            println("After update:")
            println("  Street: ${updatedAddress.street}")
            println("  Number: ${updatedAddress.houseNumber}")
            println("  Phone: ${updatedAddress.phone}")

            assertEquals(TEST_STREET, updatedAddress.street, "Street should be updated")
            assertEquals(TEST_HOUSE_NUMBER, updatedAddress.houseNumber, "House number should be updated")
            assertEquals(TEST_PHONE, updatedAddress.phone, "Phone should be updated")

            // Step 5: Rollback to original data
            println("\nRolling back to original values...")
            api.address.updateResidenceAddress(originalAddress)

            // Step 6: Verify rollback worked
            val restoredAddress = api.address.getResidenceAddress()
                ?: fail("Restored residence address should not be null")
            
            println("After rollback:")
            println("  Street: ${restoredAddress.street}")
            println("  Number: ${restoredAddress.houseNumber}")
            println("  Phone: ${restoredAddress.phone}")

            assertEquals(originalAddress.street, restoredAddress.street, "Street should be restored")
            assertEquals(originalAddress.houseNumber, restoredAddress.houseNumber, "House number should be restored")
            assertEquals(originalAddress.phone, restoredAddress.phone, "Phone should be restored")

            println("\nResidence address update test completed successfully with rollback")
        }
    }

    @Test
    fun `updateContactInfo changes data and rollback restores original`() {
        runBlocking {
            // Step 1: Get the original contact info
            val originalContactInfo = api.address.getContactInfo()
            assertNotNull(originalContactInfo, "Original contact info should not be null")
            println("Original contact info:")
            println("  Email: ${originalContactInfo.email}")
            println("  Mobile: ${originalContactInfo.mobilePhone}")
            println("  Fax: ${originalContactInfo.fax}")
            
            // Step 2: Create a modified contact info
            val modifiedContactInfo = originalContactInfo.copy(
                email = TEST_EMAIL,
                mobilePhone = TEST_MOBILE,
                fax = TEST_FAX
            )

            // Step 3: Update with modified data
            println("\nUpdating contact info with email, mobile and fax...")
            api.address.updateContactInfo(modifiedContactInfo)

            // Step 4: Verify the data actually changed
            val updatedContactInfo = api.address.getContactInfo()
            assertNotNull(updatedContactInfo, "Updated contact info should not be null")
            println("After update:")
            println("  Email: ${updatedContactInfo.email}")
            println("  Mobile: ${updatedContactInfo.mobilePhone}")
            println("  Fax: ${updatedContactInfo.fax}")
            
            assertEquals(TEST_EMAIL, updatedContactInfo.email, "Email should be updated")
            assertEquals(TEST_MOBILE, updatedContactInfo.mobilePhone, "Mobile phone should be updated")
            assertEquals(TEST_FAX, updatedContactInfo.fax, "Fax should be updated")

            // Step 5: Rollback to original data
            println("\nRolling back to original values...")
            api.address.updateContactInfo(originalContactInfo)

            // Step 6: Verify rollback worked
            val restoredContactInfo = api.address.getContactInfo()
            assertNotNull(restoredContactInfo, "Restored contact info should not be null")
            println("After rollback:")
            println("  Email: ${restoredContactInfo.email}")
            println("  Mobile: ${restoredContactInfo.mobilePhone}")
            println("  Fax: ${restoredContactInfo.fax}")
            
            assertEquals(originalContactInfo.email, restoredContactInfo.email, "Email should be restored")
            assertEquals(originalContactInfo.mobilePhone, restoredContactInfo.mobilePhone, "Mobile phone should be restored")
            assertEquals(originalContactInfo.fax, restoredContactInfo.fax, "Fax should be restored")

            println("\nContact info update test completed successfully with rollback")
        }
    }
}
