package it.attendance100.mybicocca.data.datasource.appointment

import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.model.appointment.Appointment
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Esse3AppointmentDataSource @Inject constructor(
    private val esse3Api: Esse3Api,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getAppointments(calendarContext: String): List<Appointment> = withContext(ioDispatcher) {
        val personId = authTokenStore.esse3PersonId
        val bookings = esse3Api.appointmentsCalendar.getBookingsList(
            calendarContext = calendarContext,
            personId = personId,
        )
        bookings.mapNotNull { booking ->
            val id = booking.calendarCallEnrolledId ?: return@mapNotNull null
            Appointment(
                id = id,
                personId = personId,
                calendarContext = calendarContext,
                description = booking.calendarCallTypeDescription,
                date = booking.bookingDate,
                status = null,
            )
        }
    }
}
