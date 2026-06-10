package it.attendance100.mybicocca.domain.model.enrollment

/**
 * Annual re-enrollment ("iscrizione anni successivi") state, derived from the enrollment
 * list and the current academic year. Drives the renewal banner of the enrollments
 * sub-screen in the registry tab.
 *
 * There is no student-accessible REST endpoint on Bicocca's Esse3 to submit the annual
 * enrollment: every `/carriere/{stuId}/iscrizioni` mutation is checkUserTecnico
 * (segreteria only), and no domanda-iscrizione / rinnovo POST exists in the OpenAPI
 * specs (the only "proroga" endpoints concern PhD thesis extension). The submission is
 * therefore a web-only Esse3 flow that, once completed, generates the first-instalment
 * tax. The app surfaces the state and deep-links to the official web flow rather than
 * faking a write.
 */
sealed interface RenewalState {

    /**
     * The course is finished (last legal year done / awaiting degree) — renewal does not
     * apply.
     */
    data object NotApplicable : RenewalState

    /**
     * An active enrollment already exists for [academicYear]; the student is in good
     * standing.
     */
    data class Enrolled(val academicYear: Int) : RenewalState

    /** No enrollment yet for [academicYear]; the student can renew via the web flow. */
    data class Renewable(val academicYear: Int) : RenewalState
}
