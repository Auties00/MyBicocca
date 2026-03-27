package it.attendance100.mybicocca.data.model.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val personId: Long,
    val firstName: String,
    val lastName: String,
    val fiscalCode: String? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val birthPlace: String? = null,
    val email: String? = null,
    val mobilePhone: String? = null,
    val residenceAddress: String? = null,
    val residenceCity: String? = null,
    val residencePostalCode: String? = null,
    val profilePic: ByteArray? = null
) {
    val fullName: String get() = "$firstName $lastName"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (personId != other.personId) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (fiscalCode != other.fiscalCode) return false
        if (gender != other.gender) return false
        if (birthDate != other.birthDate) return false
        if (birthPlace != other.birthPlace) return false
        if (email != other.email) return false
        if (mobilePhone != other.mobilePhone) return false
        if (residenceAddress != other.residenceAddress) return false
        if (residenceCity != other.residenceCity) return false
        if (residencePostalCode != other.residencePostalCode) return false
        if (!profilePic.contentEquals(other.profilePic)) return false
        if (fullName != other.fullName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = personId.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + (fiscalCode?.hashCode() ?: 0)
        result = 31 * result + (gender?.hashCode() ?: 0)
        result = 31 * result + (birthDate?.hashCode() ?: 0)
        result = 31 * result + (birthPlace?.hashCode() ?: 0)
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (mobilePhone?.hashCode() ?: 0)
        result = 31 * result + (residenceAddress?.hashCode() ?: 0)
        result = 31 * result + (residenceCity?.hashCode() ?: 0)
        result = 31 * result + (residencePostalCode?.hashCode() ?: 0)
        result = 31 * result + (profilePic?.contentHashCode() ?: 0)
        result = 31 * result + fullName.hashCode()
        return result
    }
}
