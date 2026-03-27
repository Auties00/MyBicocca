package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.AppointmentDao
import it.attendance100.mybicocca.data.datasource.appointment.Esse3AppointmentDataSource
import it.attendance100.mybicocca.data.model.appointment.Appointment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(
    private val dataSource: Esse3AppointmentDataSource,
    private val dao: AppointmentDao,
) {
    fun observeAll(): Flow<List<Appointment>> = dao.observeAll()

    fun observeByPerson(personId: Long): Flow<List<Appointment>> =
        dao.observeByPerson(personId)

    suspend fun refresh(calendarContext: String): Result<Unit> = runCatching {
        val appointments = dataSource.getAppointments(calendarContext)
        dao.deleteAll()
        dao.upsertAll(appointments)
    }
}
