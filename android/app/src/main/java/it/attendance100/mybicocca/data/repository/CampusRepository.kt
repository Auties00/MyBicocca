package it.attendance100.mybicocca.data.repository

import android.text.Html
import androidx.lifecycle.LiveData
import it.attendance100.mybicocca.data.api.bicoccapp.BicoccappApi
import it.attendance100.mybicocca.di.AppDatabase
import it.attendance100.mybicocca.domain.model.MapLocation
import it.attendance100.mybicocca.domain.model.Teacher
import it.attendance100.mybicocca.domain.model.TeacherBuilding
import javax.inject.Inject
import it.attendance100.mybicocca.domain.repository.CampusRepository as ICampusRepository

class CampusRepository @Inject constructor(
	private val api: BicoccappApi,
	private val database: AppDatabase,
) : ICampusRepository {

	override fun observeLocations(): LiveData<List<MapLocation>> {
		return database.campusDao().observeLocations()
	}

    override suspend fun syncLocations(): Boolean {
        val response = api.campus.getPointsOfInterest()
        if (!response.isSuccessful) return false

        val body = response.body() ?: return false

        val domainLocations = mutableListOf<MapLocation>()

        body.maps?.let { maps ->
            maps.mapLocations.forEach {
                val name = it.name ?: return@forEach
                val latitude = it.latitude?.toDoubleOrNull() ?: return@forEach
                val longitude = it.longitude?.toDoubleOrNull() ?: return@forEach
                val description = it.description ?: ""
                val category = it.type ?: "other"
                domainLocations.add(
                    MapLocation(
                        name = name,
                        description = description,
                        category = category,
                        latitude = latitude,
                        longitude = longitude,
                    )
                )
            }
        }

        database.campusDao()
            .deleteLocations()

        database.campusDao()
            .insertLocations(domainLocations)

        return true
    }

	override suspend fun searchTeacher(email: String): Teacher? {
		val local = database.campusDao().getTeacherByEmail(email)
		if (local != null) return local

		val response = api.campus.getTeacherByEmail(email)
        if (!response.isSuccessful) return null

        val responseBody = response.body() ?: return null

        val teacher = responseBody.teacher ?: return null

        val email = teacher.email ?: email

        val firstName = teacher.name ?: ""
        val lastName = teacher.surname ?: ""
        val fullName = "$firstName $lastName".trim()


        val officesIterator = teacher.offices.listIterator()
        val roomsIterator = teacher.rooms.listIterator()
        val buildings = mutableListOf<TeacherBuilding>()
        while(roomsIterator.hasNext()) {
            val roomInfo = roomsIterator.next()
            val officeInfo = if(officesIterator.hasNext()) {
                officesIterator.next()
            } else {
                null
            }

            val location = roomInfo.roomPlace ?: continue
            val description = officeInfo?.officeDescription
            buildings.add(
                TeacherBuilding(
                    location = location,
                    description = description
                )
            )
        }

        teacher.receivesOn.let {
            Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
                .toString()
                .trim()
        }

        val domainTeacher = Teacher(
            email = email,
            firstName = firstName,
            lastName = lastName,
            fullName = fullName,
            buildings = buildings,
            receivesOn = teacher.receivesOn
        )

        database.campusDao()
            .insertTeacher(domainTeacher)

        return domainTeacher
    }
}
