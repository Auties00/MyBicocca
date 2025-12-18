package it.attendance100.mybicocca.data.api.esse3

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * # Esse3 Exams API
 *
 * Handles exam management, including viewing available exam sessions (Appelli),
 * booking exams, viewing results, and checking the exam calendar.
 *
 * ## Key Features
 *
 * - **Exam Sessions:** List available exams for booking.
 * - **Booking:** Book or cancel exam reservations.
 * - **Results:** View grades and outcomes of taken exams.
 * - **Calendar:** View exam schedule.
 * - **Slips (Statini):** Generate exam slips for attendance.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // List available exam sessions
 * val sessions = examsApi.getExamSessions()
 *
 * // Book an exam shift
 * examsApi.submitShiftBooking(
 *     shiftId = "12345",
 *     calType = "WRITTEN"
 * )
 * ```
 */
interface Esse3ExamsApi {

    /**
     * Retrieves the list of available Exam Sessions (Appelli).
     *
     * Displays exams that the student can currently book.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/studente/Appelli/Appelli.do")
    suspend fun getExamSessions(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Retrieves the list of currently booked exams (Bacheca Prenotazioni).
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/studente/Appelli/BachecaPrenotazioni.do")
    suspend fun getBookedExams(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Retrieves the board of exam results (Bacheca Esiti).
     *
     * Shows grades for completed exams waiting for acceptance or already recorded.
     *
     * @param menuOpenedCod Optional menu context.
     * @return A [Response] containing the HTML board.
     */
    @GET("auth/studente/Appelli/BachecaEsiti.do")
    suspend fun getExamResults(@Query("menu_opened_cod") menuOpenedCod: String? = null): Response<String>

    /**
     * Generates the Exam Slip (Statino) download.
     *
     * Triggers the generation of the attendance slip.
     *
     * @param examCourseId ID of the exam course.
     * @param appId Exam session application ID.
     * @param activityId Didactic activity ID.
     * @param freqYear Frequency academic year.
     * @param preCourseId Pre-course ID.
     * @param preActivityId Pre-activity ID.
     * @param offYear Offer academic year.
     * @param adsceId Student career activity ID.
     * @param startDate Exam start date.
     * @return A [Response] containing [Unit].
     */
    @GET("auth/studente/Appelli/StampaStatino.do")
    suspend fun printExamSlip(
        @Query("CDS_ESA_ID") examCourseId: String? = null,
        @Query("APP_ID") appId: String? = null,
        @Query("ATT_DID_ESA_ID") activityId: String? = null,
        @Query("AA_FREQ_ID") freqYear: String? = null,
        @Query("CDS_PRE_ID") preCourseId: String? = null,
        @Query("AD_PRE_ID") preActivityId: String? = null,
        @Query("AA_OFF_PRE_ID") offYear: String? = null,
        @Query("ADSCE_ID") adsceId: String? = null,
        @Query("DATA_INIZIO_APP") startDate: String? = null
    ): Response<Unit>

    /**
     * Downloads the Exam Slip (Statino) as a PDF.
     *
     * @param examCourseId ID of the exam course.
     * @param appId Exam session application ID.
     * @param activityId Didactic activity ID.
     * @param freqYear Frequency academic year.
     * @param preCourseId Pre-course ID.
     * @param preActivityId Pre-activity ID.
     * @param offYear Offer academic year.
     * @param adsceId Student career activity ID.
     * @param startDate Exam start date.
     * @return A [Response] containing the PDF [ResponseBody].
     */
    @GET("auth/studente/Appelli/StampaStatinoPDF.do")
    suspend fun printExamSlipPdf(
        @Query("CDS_ESA_ID") examCourseId: String? = null,
        @Query("APP_ID") appId: String? = null,
        @Query("ATT_DID_ESA_ID") activityId: String? = null,
        @Query("AA_FREQ_ID") freqYear: String? = null,
        @Query("CDS_PRE_ID") preCourseId: String? = null,
        @Query("AD_PRE_ID") preActivityId: String? = null,
        @Query("AA_OFF_PRE_ID") offYear: String? = null,
        @Query("ADSCE_ID") adsceId: String? = null,
        @Query("DATA_INIZIO_APP") startDate: String? = null
    ): Response<ResponseBody>

    /**
     * Retrieves the Calendar Appointments list for booking.
     *
     * @return A [Response] containing the HTML list.
     */
    @GET("auth/Calendar/CAPrenotazCalendarioAppElenco.do")
    suspend fun getCalendarAppointments(): Response<String>

    /**
     * Starts the booking process for a specific calendar shift.
     *
     * @param menuOpenedCod Optional menu context.
     * @param shiftId The ID of the shift (turno) to book.
     * @param calType Calendar type code.
     * @param subContType Sub-context type.
     * @param formId Form identifier.
     * @param btnSave Button action to save.
     * @param gestType Management type code.
     * @return A [Response] containing [Unit].
     */
    @GET("auth/Calendar/CAPrenotazCalendarioAppStartFakeProcessAction.do")
    suspend fun startBookingProcess(
        @Query("menu_opened_cod") menuOpenedCod: String? = null,
        @Query("sel_turno") shiftId: String? = null,
        @Query("TIPO_CAL_COD") calType: String? = null,
        @Query("TIPO_SUBCONT_COD") subContType: String? = null,
        @Query("form_id_formTurni") formId: String? = null,
        @Query("btnSalva") btnSave: String? = null,
        @Query("TIPO_GEST_COD_CAL") gestType: String? = null
    ): Response<Unit>

    /**
     * Retrieves the Shift Booking form.
     *
     * @return A [Response] containing the HTML form.
     */
    @GET("auth/Calendar/PrenotazioneTurnoForm.do")
    suspend fun getShiftBookingForm(): Response<String>

    /**
     * Submits a booking for an exam shift.
     *
     * @param shiftId The ID of the shift.
     * @param calType Calendar type code.
     * @param gestType Management type code.
     * @param subContType Sub-context type.
     * @param formId Form identifier, defaults to "formTurni".
     * @param btnSave Save button action.
     * @param dynamicFields Additional dynamic fields (e.g., `sel_turno_nota_{id}`).
     * @return A [Response] containing the HTML response.
     */
    @FormUrlEncoded
    @POST("auth/Calendar/PrenotazioneTurnoFormSubmit.do")
    suspend fun submitShiftBooking(
        @Field("sel_turno") shiftId: String? = null,
        @Field("TIPO_CAL_COD") calType: String? = null,
        @Field("TIPO_GEST_COD_CAL") gestType: String? = null,
        @Field("TIPO_SUBCONT_COD") subContType: String? = null,
        @Field("form_id_formTurni") formId: String? = "formTurni",
        @Field("btnSalva") btnSave: String? = null,
        @FieldMap dynamicFields: Map<String, String> = emptyMap()
    ): Response<String>

    /**
     * Checks if an appointment can be cancelled.
     *
     * @param subContType Sub-context type.
     * @param gestType Management type code.
     * @param calType Calendar type code.
     * @param appointmentId The ID of the booked appointment.
     * @param description Description of the appointment.
     * @return A [Response] containing the HTML confirmation or error.
     */
    @GET("auth/Calendar/CACancellaAppuntamentoSubmit.do")
    suspend fun checkCancelAppointment(
        @Query("TIPO_SUBCONT_COD") subContType: String? = null,
        @Query("TIPO_GEST_COD_CAL") gestType: String? = null,
        @Query("TIPO_CAL_COD") calType: String? = null,
        @Query("CAL_APP_ISCRITTI_ID") appointmentId: String? = null,
        @Query("APP_DES") description: String? = null
    ): Response<String>

    /**
     * Submits the cancellation of an appointment.
     *
     * @param subContType Sub-context type.
     * @param gestType Management type code.
     * @param calType Calendar type code.
     * @param appointmentId The ID of the booked appointment.
     * @param formId Form identifier, defaults to "form1".
     * @param btnOk Confirmation button.
     * @return A [Response] containing [Unit].
     */
    @FormUrlEncoded
    @POST("auth/Calendar/CACancellaAppuntamentoSubmit.do")
    suspend fun submitCancelAppointment(
        @Field("TIPO_SUBCONT_COD") subContType: String? = null,
        @Field("TIPO_GEST_COD_CAL") gestType: String? = null,
        @Field("TIPO_CAL_COD") calType: String? = null,
        @Field("CAL_APP_ISCRITTI_ID") appointmentId: String? = null,
        @Field("form_id_form1") formId: String? = "form1",
        @Field("btnOk") btnOk: String? = null
    ): Response<Unit>
}
