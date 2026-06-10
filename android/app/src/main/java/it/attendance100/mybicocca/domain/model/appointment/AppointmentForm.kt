package it.attendance100.mybicocca.domain.model.appointment

/**
 * Which section of the booking request a field belongs to. The split is a wire detail the
 * repository needs back when submitting, so it travels with the field.
 */
enum class AppointmentFormSection { User, Service }

/**
 * One choice of a [AppointmentFormField.Select] field.
 *
 * @property value Option key submitted as the field value when the option is chosen.
 * @property label Display label of the option.
 */
data class AppointmentFormOption(
    val value: String,
    val label: String,
)

/**
 * A field of the Portale Planning booking form, rendered by the "Appuntamenti" booking flow
 * and submitted (keyed by [code]) with the reservation request.
 */
sealed interface AppointmentFormField {
    /** Wire code of the field; keys the submitted value. */
    val code: String

    /** Display label of the field. */
    val label: String

    /** Whether the field must be filled before submitting. */
    val required: Boolean

    /** Placeholder text supplied by the portal, when any. */
    val placeholder: String?

    /** Booking-request section the value must be submitted under. */
    val section: AppointmentFormSection

    /**
     * An email input.
     *
     * @property primary The primary field keys the reservation: its value (the email address)
     *   is needed to manage or cancel the booking later.
     */
    data class Email(
        override val code: String,
        override val label: String,
        override val required: Boolean,
        override val placeholder: String?,
        override val section: AppointmentFormSection,
        val primary: Boolean,
    ) : AppointmentFormField

    /** A single-line text input. */
    data class Text(
        override val code: String,
        override val label: String,
        override val required: Boolean,
        override val placeholder: String?,
        override val section: AppointmentFormSection,
    ) : AppointmentFormField

    /** A multi-line text input. */
    data class TextArea(
        override val code: String,
        override val label: String,
        override val required: Boolean,
        override val placeholder: String?,
        override val section: AppointmentFormSection,
    ) : AppointmentFormField

    /** A phone-number input. */
    data class Phone(
        override val code: String,
        override val label: String,
        override val required: Boolean,
        override val placeholder: String?,
        override val section: AppointmentFormSection,
    ) : AppointmentFormField

    /**
     * A single-choice dropdown.
     *
     * @property options Choices offered by the portal; the chosen option's key is what gets
     *   submitted.
     */
    data class Select(
        override val code: String,
        override val label: String,
        override val required: Boolean,
        override val placeholder: String?,
        override val section: AppointmentFormSection,
        val options: List<AppointmentFormOption>,
    ) : AppointmentFormField

    /**
     * The GDPR checkbox. Not submitted with the booking request, but the user must tick it.
     *
     * @property policyUrl Link to the privacy-policy document, when the portal provides one.
     */
    data class Consent(
        override val code: String,
        override val label: String,
        override val required: Boolean,
        val policyUrl: String?,
    ) : AppointmentFormField {
        override val placeholder: String? get() = null
        override val section: AppointmentFormSection get() = AppointmentFormSection.User
    }
}

/**
 * The booking form of a Portale Planning service.
 *
 * @property fields Ordered fields to render.
 * @property primaryField The email field that keys the reservation, when the form has one.
 */
data class AppointmentForm(
    val fields: List<AppointmentFormField>,
) {
    val primaryField: AppointmentFormField.Email? =
        fields.filterIsInstance<AppointmentFormField.Email>().firstOrNull { it.primary }
}
