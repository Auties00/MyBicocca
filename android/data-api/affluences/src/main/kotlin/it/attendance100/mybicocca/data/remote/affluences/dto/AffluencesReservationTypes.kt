package it.attendance100.mybicocca.data.remote.affluences.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The bookable resource types of a site, as returned by the reservation API
 * (`GET /sites/{site}/infos`).
 *
 * @property types The resource types that can be booked at the site (seat zones, study rooms...).
 * @property contactEmail The booking contact email of the site.
 */
@Serializable
data class AffluencesReservationSiteInfo(
    @SerialName("types")
    val types: List<AffluencesResourceTypeInfo> = emptyList(),
    @SerialName("contact_email")
    val contactEmail: String? = null
)

/**
 * A bookable resource type of a site (e.g. a seat zone or a category of study rooms).
 *
 * @property resourceTypeId The numeric identifier of the resource type, used to query
 * availability and filters.
 * @property imageUrl Absolute URL of the resource type illustration.
 * @property description The localized description of the resource type
 * (e.g. "Posto zona ROSSA").
 * @property subdescription The localized secondary description of the resource type.
 * @property rank The display rank of the resource type, when the site defines an ordering.
 * @property mapUrl Absolute URL of a map of the resources, when the site provides one.
 * @property timeLimit The ISO-8601 instant after which bookings of this type are accepted for
 * the current day, when the site limits same-day booking.
 */
@Serializable
data class AffluencesResourceTypeInfo(
    @SerialName("resource_type")
    val resourceTypeId: Int,
    @SerialName("image")
    val imageUrl: String? = null,
    @SerialName("localized_description")
    val description: String? = null,
    @SerialName("localized_subdescription")
    val subdescription: String? = null,
    @SerialName("rank")
    val rank: Int? = null,
    @SerialName("map")
    val mapUrl: String? = null,
    @SerialName("time_limit")
    val timeLimit: String? = null
)

/**
 * A bookable resource with its availability for a given day, as returned by the reservation API
 * (`GET /resources/{site}/available`).
 *
 * The wire response also contains `reservations_by_timeslot` and `services` fields whose shapes
 * are undocumented; they are intentionally not mapped.
 *
 * @property resourceId The numeric identifier of the resource, used to create reservations.
 * @property resourceName The display name of the resource (e.g. "R 1").
 * @property resourceTypeId The identifier of the type this resource belongs to.
 * @property granularityMinutes The booking granularity in minutes.
 * @property timeSlotCount The number of bookable slots in a day.
 * @property staticTimeSlot Whether the resource uses fixed time slots instead of free durations.
 * @property noteAvailable Whether a note can be attached to a reservation.
 * @property noteRequired Whether a note must be attached to a reservation.
 * @property noteDescription The localized description of what the note should contain.
 * @property description The plain-text description of the resource.
 * @property descriptionHtml The HTML description of the resource.
 * @property capacity The capacity of the resource. 0 means the resource is a single seat.
 * @property siteTimeZone The IANA time zone of the site.
 * @property userNameRequired Whether the user name must be provided when booking.
 * @property userPhoneRequired Whether the user phone must be provided when booking.
 * @property userNameAvailable Whether the user name can be provided when booking.
 * @property userPhoneAvailable Whether the user phone can be provided when booking.
 * @property timeBeforeReservationsClosed Minutes before the slot start after which booking
 * closes, when the site defines one.
 * @property minPlacesPerReservation The minimum number of places per reservation, when the
 * resource has a capacity.
 * @property maxPlacesPerReservation The maximum number of places per reservation, when the
 * resource has a capacity.
 * @property imageUrl Absolute URL of the resource illustration.
 * @property slotsState The overall availability of the resource for the requested day.
 * Known values include `available`.
 * @property hours The bookable slots of the requested day.
 */
@Serializable
data class AffluencesAvailableResource(
    @SerialName("resource_id")
    val resourceId: Int,
    @SerialName("resource_name")
    val resourceName: String? = null,
    @SerialName("resource_type")
    val resourceTypeId: Int? = null,
    @SerialName("granularity")
    val granularityMinutes: Int? = null,
    @SerialName("time_slot_count")
    val timeSlotCount: Int? = null,
    @SerialName("static_time_slot")
    val staticTimeSlot: Boolean = false,
    @SerialName("note_available")
    val noteAvailable: Boolean = false,
    @SerialName("note_required")
    val noteRequired: Boolean = false,
    @SerialName("note_description")
    val noteDescription: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("description_html")
    val descriptionHtml: String? = null,
    @SerialName("capacity")
    val capacity: Int = 0,
    @SerialName("site_timezone")
    val siteTimeZone: String? = null,
    @SerialName("user_name_required")
    val userNameRequired: Boolean = false,
    @SerialName("user_phone_required")
    val userPhoneRequired: Boolean = false,
    @SerialName("user_name_available")
    val userNameAvailable: Boolean = false,
    @SerialName("user_phone_available")
    val userPhoneAvailable: Boolean = false,
    @SerialName("time_before_reservations_closed")
    val timeBeforeReservationsClosed: Int? = null,
    @SerialName("min_places_per_reservation")
    val minPlacesPerReservation: Int? = null,
    @SerialName("max_places_per_reservation")
    val maxPlacesPerReservation: Int? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("slots_state")
    val slotsState: String? = null,
    @SerialName("hours")
    val hours: List<AffluencesTimeSlot> = emptyList()
)

/**
 * A bookable time slot of a resource.
 *
 * The wire response also contains a `reservations` field whose shape is undocumented;
 * it is intentionally not mapped.
 *
 * @property hour The start of the slot, as an `HH:mm` time string local to the site time zone.
 * @property state The availability of the slot. Known values include `available`.
 * @property granularityMinutes The duration of the slot in minutes.
 * @property personCount The number of people already booked into the slot, for shared resources.
 * @property placesAvailable The number of places still available in the slot.
 * @property placesBookable The number of places that can still be booked in the slot.
 */
@Serializable
data class AffluencesTimeSlot(
    @SerialName("hour")
    val hour: String,
    @SerialName("state")
    val state: String? = null,
    @SerialName("granularity")
    val granularityMinutes: Int? = null,
    @SerialName("person_count")
    val personCount: Int = 0,
    @SerialName("places_available")
    val placesAvailable: Int = 0,
    @SerialName("places_bookable")
    val placesBookable: Int = 0
)

/**
 * The booking filters of a resource type for a given day, as returned by the reservation API
 * (`GET /sites/{site}/types/{type}/filters`).
 *
 * @property resourceTypeId The identifier of the resource type.
 * @property openDays The days bookings are accepted for, as `yyyy-MM-dd` date strings.
 * @property openHours The slot start times bookings are accepted for, as `HH:mm` time strings.
 * @property durations The accepted booking durations in minutes.
 * @property capacities The accepted party sizes.
 * @property resources The bookable resources of the type.
 */
@Serializable
data class AffluencesResourceTypeFilters(
    @SerialName("resource_type_id")
    val resourceTypeId: Int,
    @SerialName("open_days")
    val openDays: List<String> = emptyList(),
    @SerialName("open_hours")
    val openHours: List<String> = emptyList(),
    @SerialName("durations")
    val durations: List<Int> = emptyList(),
    @SerialName("capacities")
    val capacities: List<Int> = emptyList(),
    @SerialName("resources")
    val resources: List<AffluencesResourceSummary> = emptyList()
)

/**
 * A compact reference to a bookable resource.
 *
 * @property resourceId The numeric identifier of the resource.
 * @property resourceName The display name of the resource.
 */
@Serializable
data class AffluencesResourceSummary(
    @SerialName("resource_id")
    val resourceId: Int,
    @SerialName("resource_name")
    val resourceName: String? = null
)

/**
 * Envelope of the agreements response.
 *
 * @property agreements The agreements of the site.
 */
@Serializable
internal class AffluencesAgreementsEnvelope(
    @SerialName("agreements")
    val agreements: List<AffluencesAgreement> = emptyList()
)

/**
 * A terms-of-use agreement a user must consent to before booking, as returned by the
 * reservation API (`GET /sites/{site}/agreements`).
 *
 * @property agreementId The numeric identifier of the agreement.
 * @property agreementDetailsId The numeric identifier of the current version of the agreement.
 * @property latestChange The ISO-8601 instant of the last change to the agreement.
 * @property name The localized display name of the agreement.
 * @property url External URL of the full agreement text.
 * @property mandatoryConsent Whether consent is required to book.
 */
@Serializable
data class AffluencesAgreement(
    @SerialName("agreements_id")
    val agreementId: Int,
    @SerialName("agreements_details_id")
    val agreementDetailsId: Int? = null,
    @SerialName("latest_change")
    val latestChange: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("url")
    val url: String? = null,
    @SerialName("mandatory_consent")
    val mandatoryConsent: Boolean = false
)

/**
 * The email restrictions of a resource, as returned by the reservation API
 * (`GET /resource/{resource}`).
 *
 * Sites can restrict booking to institutional email addresses (e.g. university domains).
 *
 * @property filterErrorMessage The localized plain-text message shown when the email does not
 * match the restrictions.
 * @property filterErrorMessageHtml The localized HTML message shown when the email does not
 * match the restrictions.
 * @property filters The accepted email patterns. Empty when any email is accepted.
 */
@Serializable
data class AffluencesResourceEmailPolicy(
    @SerialName("filter_error_message")
    val filterErrorMessage: String? = null,
    @SerialName("filter_error_message_html")
    val filterErrorMessageHtml: String? = null,
    @SerialName("filters")
    val filters: List<String> = emptyList()
)

/**
 * The custom booking form of a resource, as returned by the reservation API
 * (`GET /resource/{resource}/forms`).
 *
 * @property customForm The server-driven dynamic form definition, exposed raw because its schema
 * is defined by each site, or null when the resource has no custom form.
 */
@Serializable
data class AffluencesResourceForms(
    @SerialName("customForm")
    val customForm: JsonElement? = null
)

/**
 * Envelope of the identity providers response.
 *
 * @property identityProviders The identity providers of the resource.
 */
@Serializable
internal class AffluencesIdentityProvidersEnvelope(
    @SerialName("identityProviders")
    val identityProviders: List<AffluencesIdentityProvider> = emptyList()
)

/**
 * A single sign-on identity provider required to book a resource, as returned by the
 * reservation API (`GET /resource/{resource}/identity-providers`).
 *
 * When a resource declares identity providers, reservations must include an `Authorization`
 * credential obtained from one of them (a SAML auth token or an OIDC id token).
 *
 * The shape of this payload could not be fully verified against live data; all fields
 * are therefore optional.
 *
 * @property type The protocol of the provider. Known values are `SAML` and `OIDC`.
 * @property discoveryUrl The OIDC discovery URL, for OIDC providers.
 * @property clientId The OIDC client identifier, for OIDC providers.
 * @property scopes The OIDC scopes to request, for OIDC providers.
 */
@Serializable
data class AffluencesIdentityProvider(
    @SerialName("type")
    val type: String? = null,
    @SerialName("discoveryUrl")
    val discoveryUrl: String? = null,
    @SerialName("clientId")
    val clientId: String? = null,
    @SerialName("scopes")
    val scopes: List<String> = emptyList()
)

/**
 * The live status of a resource, as returned by the reservation API (`GET /status/{resource}`).
 *
 * @property reservationEmail The booking contact email of the site.
 * @property hours The slots of the current day with their states.
 * @property currentState The current state of the resource. Known values include `available`,
 * `out_of_bounds` (outside opening hours).
 * @property resource The detailed description of the resource.
 */
@Serializable
data class AffluencesResourceStatus(
    @SerialName("reservation_mail")
    val reservationEmail: String? = null,
    @SerialName("hours")
    val hours: List<AffluencesTimeSlot> = emptyList(),
    @SerialName("current_state")
    val currentState: String? = null,
    @SerialName("resource")
    val resource: AffluencesResourceDetail? = null
)

/**
 * The detailed description of a resource as embedded in [AffluencesResourceStatus].
 *
 * @property resourceId The numeric identifier of the resource.
 * @property resourceName The display name of the resource.
 * @property description The plain-text description of the resource.
 * @property descriptionHtml The HTML description of the resource.
 * @property capacity The capacity of the resource. 0 means the resource is a single seat.
 * @property resourceTypeId The identifier of the type this resource belongs to.
 * @property granularityMinutes The booking granularity in minutes.
 * @property timeSlotCount The number of bookable slots in a day.
 * @property reservationMandatory Whether a reservation is required to use the resource.
 * @property timeLimit The number of days in advance bookings are accepted.
 * @property siteSettingsMessage The localized plain-text booking instructions of the site.
 * @property siteSettingsMessageHtml The localized HTML booking instructions of the site.
 * @property siteSettingsUrl External URL with the booking instructions of the site.
 * @property siteSettingsContactEmail The booking contact email of the site.
 * @property extraInfosMessage Additional localized plain-text information about the resource.
 * @property extraInfosMessageHtml Additional localized HTML information about the resource.
 * @property extraInfosUrl External URL with additional information about the resource.
 * @property siteName The main display name of the site the resource belongs to.
 * @property siteSecondaryName The secondary display name of the site.
 * @property siteUuid The UUID of the site the resource belongs to.
 */
@Serializable
data class AffluencesResourceDetail(
    @SerialName("resource_id")
    val resourceId: Int,
    @SerialName("resource_name")
    val resourceName: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("description_html")
    val descriptionHtml: String? = null,
    @SerialName("capacity")
    val capacity: Int = 0,
    @SerialName("resource_type")
    val resourceTypeId: Int? = null,
    @SerialName("granularity")
    val granularityMinutes: Int? = null,
    @SerialName("time_slot_count")
    val timeSlotCount: Int? = null,
    @SerialName("reservation_mandatory")
    val reservationMandatory: Boolean = false,
    @SerialName("time_limit")
    val timeLimit: Int? = null,
    @SerialName("site_settings_message")
    val siteSettingsMessage: String? = null,
    @SerialName("site_settings_message_html")
    val siteSettingsMessageHtml: String? = null,
    @SerialName("site_settings_url")
    val siteSettingsUrl: String? = null,
    @SerialName("site_settings_contact_email")
    val siteSettingsContactEmail: String? = null,
    @SerialName("extra_infos_message")
    val extraInfosMessage: String? = null,
    @SerialName("extra_infos_message_html")
    val extraInfosMessageHtml: String? = null,
    @SerialName("extra_infos_url")
    val extraInfosUrl: String? = null,
    @SerialName("site_name")
    val siteName: String? = null,
    @SerialName("secondary_name")
    val siteSecondaryName: String? = null,
    @SerialName("site_uuid")
    val siteUuid: String? = null
)

/**
 * Request payload for creating a reservation (`POST /reserve/{resource}`).
 *
 * @property email The email address of the user. The reservation must be validated through a
 * code sent to this address, unless the site uses single sign-on.
 * @property date The day of the reservation, as a `yyyy-MM-dd` date string.
 * @property startTime The start of the reservation, as an `HH:mm` time string local to the
 * site time zone.
 * @property endTime The end of the reservation, as an `HH:mm` time string local to the
 * site time zone.
 * @property personCount The number of people the reservation is for.
 * @property authType The single sign-on protocol used to authenticate (`SAML` or `OIDC`),
 * or null for email-validated reservations.
 * @property note The note attached to the reservation, when the resource requires one
 * (see [AffluencesAvailableResource.noteRequired]).
 * @property userFirstName The first name of the user, when the resource requires it.
 * @property userLastName The last name of the user, when the resource requires it.
 * @property userPhone The phone number of the user, when the resource requires it.
 * @property userDeviceToken The trusted device token of the user, obtained through the
 * trust-device endpoint, which skips email validation on subsequent bookings.
 */
@Serializable
data class AffluencesReservationRequest(
    @SerialName("email")
    val email: String,
    @SerialName("date")
    val date: String,
    @SerialName("start_time")
    val startTime: String,
    @SerialName("end_time")
    val endTime: String,
    @SerialName("person_count")
    val personCount: Int = 1,
    @SerialName("auth_type")
    val authType: String? = null,
    @SerialName("note")
    val note: String? = null,
    @SerialName("user_firstname")
    val userFirstName: String? = null,
    @SerialName("user_lastname")
    val userLastName: String? = null,
    @SerialName("user_phone")
    val userPhone: String? = null,
    @SerialName("user_device_token")
    val userDeviceToken: String? = null
)

/**
 * Result of creating or confirming a reservation.
 *
 * The success shape could not be fully verified against live data (verification would create a
 * real booking); all fields are therefore optional and unrecognized fields are ignored.
 *
 * @property reservationToken The token identifying the reservation, used to confirm it.
 * @property successMessage The localized success message.
 * @property showMessage Whether the server intends [successMessage] to be shown to the user.
 */
@Serializable
data class AffluencesReservationResult(
    @SerialName("reservationToken")
    val reservationToken: String? = null,
    @SerialName("successMessage")
    val successMessage: String? = null,
    @SerialName("showMessage")
    val showMessage: Boolean? = null
)

/**
 * Request payload for validating a reservation with the emailed code
 * (`POST /validateReservation`).
 *
 * @property validationCode The validation code received by email.
 * @property email The email address the reservation was created with.
 */
@Serializable
internal class AffluencesValidateReservationRequest(
    @SerialName("validationCode")
    val validationCode: String,
    @SerialName("email")
    val email: String
)

/**
 * Result of validating a reservation with the emailed code.
 *
 * @property validatedReservations The reservations validated by the code, exposed raw because
 * the shape could not be verified against live data (verification would require a real booking).
 */
@Serializable
data class AffluencesReservationValidation(
    @SerialName("validateReservationWithCode")
    val validatedReservations: JsonElement? = null
)

/**
 * Result of cancelling a reservation (`DELETE /cancelReservation`).
 *
 * @property successMessage The localized success message.
 */
@Serializable
data class AffluencesReservationCancellation(
    @SerialName("successMessage")
    val successMessage: String? = null
)

/**
 * Result of confirming the email address of a user
 * (`GET /myreservations/email_confirmation/{token}`).
 *
 * @property redirect Whether the caller should redirect the user.
 * @property confirmFailed Whether the confirmation failed.
 * @property notFound Whether the confirmation token is unknown.
 * @property alreadyConfirmed Whether the email address was already confirmed.
 */
@Serializable
data class AffluencesEmailConfirmation(
    @SerialName("redirect")
    val redirect: Boolean = false,
    @SerialName("confirmFailed")
    val confirmFailed: Boolean = false,
    @SerialName("not_found")
    val notFound: Boolean = false,
    @SerialName("already_confirmed")
    val alreadyConfirmed: Boolean = false
)

/**
 * Request payload for trusting a device (`POST /trust-device`).
 *
 * @property email The email address of the user.
 * @property token The trust token received by email.
 */
@Serializable
internal class AffluencesTrustDeviceRequest(
    @SerialName("email")
    val email: String,
    @SerialName("token")
    val token: String
)
