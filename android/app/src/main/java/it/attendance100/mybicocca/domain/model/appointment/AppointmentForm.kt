package it.attendance100.mybicocca.domain.model.appointment

// Which section of the booking request a field belongs to. The split is a wire detail the
// repository needs back when submitting, so it travels with the field.
enum class AppointmentFormSection { User, Service }

data class AppointmentFormOption(
    // Submitted as the field value when the option is chosen.
    val value: String,
    val label: String,
)

sealed interface AppointmentFormField {
    val code: String
    val label: String
    val required: Boolean
    val placeholder: String?
    val section: AppointmentFormSection

    data class Email(
        override val code: String,
        override val label: String,
        override val required: Boolean,
        override val placeholder: String?,
        override val section: AppointmentFormSection,
        // The primary field keys the reservation: its value (the email address) is needed
        // to manage or cancel it later.
        val primary: Boolean,
    ) : AppointmentFormField

    data class Text(
        override val code: String,
        override val label: String,
        override val required: Boolean,
        override val placeholder: String?,
        override val section: AppointmentFormSection,
    ) : AppointmentFormField

    data class TextArea(
        override val code: String,
        override val label: String,
        override val required: Boolean,
        override val placeholder: String?,
        override val section: AppointmentFormSection,
    ) : AppointmentFormField

    data class Phone(
        override val code: String,
        override val label: String,
        override val required: Boolean,
        override val placeholder: String?,
        override val section: AppointmentFormSection,
    ) : AppointmentFormField

    data class Select(
        override val code: String,
        override val label: String,
        override val required: Boolean,
        override val placeholder: String?,
        override val section: AppointmentFormSection,
        val options: List<AppointmentFormOption>,
    ) : AppointmentFormField

    // The GDPR checkbox. Not submitted with the booking request, but the user must tick it.
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

data class AppointmentForm(
    val fields: List<AppointmentFormField>,
) {
    val primaryField: AppointmentFormField.Email? =
        fields.filterIsInstance<AppointmentFormField.Email>().firstOrNull { it.primary }
}
