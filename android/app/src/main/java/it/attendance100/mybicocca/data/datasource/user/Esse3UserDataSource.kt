package it.attendance100.mybicocca.data.datasource.user

import io.ktor.utils.io.readRemaining
import it.attendance100.mybicocca.data.api.elearning.ElearningApi
import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.model.user.User
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.readByteArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Esse3UserDataSource @Inject constructor(
    private val esse3Api: Esse3Api,
    private val elearningApi: ElearningApi,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getUser(): User = withContext(ioDispatcher) {
        val personId = authTokenStore.esse3PersonId
        val person = esse3Api.personalData.getPerson(personId)
        val profilePic = esse3Api.personalData.getPersonPhoto(personId)
            .readRemaining()
            .readByteArray()
        User(
            personId = personId,
            firstName = person.name ?: "",
            lastName = person.surname ?: "",
            fiscalCode = person.fiscalCode,
            gender = person.gender,
            birthDate = person.birthDate,
            birthPlace = person.birthMunicipalityDescription,
            email = person.universityEmail,
            mobilePhone = person.mobilePhone,
            residenceAddress = person.residenceStreet?.let { street ->
                "$street ${person.residenceStreetNumber ?: ""}".trim()
            },
            residenceCity = person.residenceMunicipalityDescription,
            residencePostalCode = person.residencePostalCode,
            // FIXME: Find photo
            profilePic = profilePic
        )
    }
}
