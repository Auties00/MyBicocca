package it.attendance100.mybicocca.data.remote.easystaff.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Represents a booking portal ("cliente") hosted by the Portale Planning backend.
 *
 * Each portal is an independent booking front-end with its own services, areas, and
 * configuration. The backend identifies a portal by a numeric id (sent as the `X-CLIENTE`
 * header) and by a string code (used in URL paths).
 *
 * @property id The numeric identifier of the portal.
 * @property code The string code of the portal, used in URL paths.
 */
enum class EasyStaffPlanningPortal(val id: Int, val code: String) {
    /**
     * The student information desks portal ("Sportelli Informativi"), used to book
     * appointments with the student administration offices (segreterie). Anonymous booking
     * is fully supported.
     */
    INFORMATION_DESKS(2, "unimib-segreteria"),

    /**
     * The legacy study rooms portal ("Prenotazione Aule Studio"). Public login is mandatory
     * and currently blocked, so anonymous requests return empty service and area listings.
     * Study room booking has since moved to the Affluences platform.
     */
    STUDY_ROOMS(1, "unimib-aulestudio")
}

/**
 * Represents a portal entry returned by the portal index.
 *
 * @property id The numeric identifier of the portal.
 * @property name The display name of the portal (e.g. "Prenotazione Aule Studio").
 * @property code The string code of the portal (e.g. "unimib-aulestudio").
 */
@Serializable
data class EasyStaffPlanningPortalSummary(
    @SerialName("id")
    val id: Int,

    @SerialName("Nome")
    val name: String,

    @SerialName("Codice")
    val code: String
)

/**
 * Represents the full configuration of a booking portal.
 *
 * @property id The numeric identifier of the portal.
 * @property name The display name of the portal (e.g. "Sportelli Informativi").
 * @property code The string code of the portal (e.g. "unimib-segreteria").
 * @property logoPath The path of the portal logo, relative to the backend storage root.
 * @property settings The behavioral configuration of the portal.
 * @property colors The branding colors of the portal.
 */
@Serializable
data class EasyStaffPlanningPortalConfig(
    @SerialName("id")
    val id: Int,

    @SerialName("Nome")
    val name: String,

    @SerialName("Codice")
    val code: String,

    @SerialName("Logo")
    val logoPath: String? = null,

    @SerialName("config")
    val settings: EasyStaffPlanningPortalSettings = EasyStaffPlanningPortalSettings(),

    @SerialName("colors")
    val colors: EasyStaffPlanningPortalColors = EasyStaffPlanningPortalColors()
)

/**
 * Represents the branding colors of a booking portal.
 *
 * @property textColor The accent text color of the portal as a hex string (e.g. "#B10035").
 */
@Serializable
data class EasyStaffPlanningPortalColors(
    @SerialName("scritte")
    val textColor: String? = null
)

/**
 * Represents the behavioral configuration of a booking portal.
 *
 * On the wire each group is an array of single-key objects with stringly-typed values; the
 * group serializers fold and coerce them into the typed classes below.
 *
 * @property general General booking behavior of the portal.
 * @property serviceCategories Behavior of the service category selection step.
 * @property publicLogin Public login requirements of the portal.
 * @property grid Display options of the availability grid.
 * @property portalFields Visibility of the selection fields in the booking wizard.
 * @property publicPortal Front-end options of the public portal pages.
 */
@Serializable
data class EasyStaffPlanningPortalSettings(
    @SerialName("general")
    @Serializable(with = GeneralSettingsSerializer::class)
    val general: EasyStaffPlanningGeneralSettings = EasyStaffPlanningGeneralSettings(),

    @SerialName("campi_tipologia_servizi")
    @Serializable(with = ServiceCategorySettingsSerializer::class)
    val serviceCategories: EasyStaffPlanningServiceCategorySettings = EasyStaffPlanningServiceCategorySettings(),

    @SerialName("login_pubblico")
    @Serializable(with = LoginSettingsSerializer::class)
    val publicLogin: EasyStaffPlanningLoginSettings = EasyStaffPlanningLoginSettings(),

    @SerialName("griglia")
    @Serializable(with = GridSettingsSerializer::class)
    val grid: EasyStaffPlanningGridSettings = EasyStaffPlanningGridSettings(),

    @SerialName("campi_portale")
    @Serializable(with = PortalFieldsSettingsSerializer::class)
    val portalFields: EasyStaffPlanningPortalFieldsSettings = EasyStaffPlanningPortalFieldsSettings(),

    @SerialName("portale_pubblico")
    @Serializable(with = PublicPortalSettingsSerializer::class)
    val publicPortal: EasyStaffPlanningPublicPortalSettings = EasyStaffPlanningPublicPortalSettings()
)

/**
 * Represents the general booking behavior of a portal.
 *
 * @property captchaRequired Whether reservations require a reCAPTCHA token. Both Bicocca
 * portals have this disabled.
 * @property concurrentBookingsAllowed Whether a user can hold multiple reservations at once.
 * @property bookingInProgressCheck Whether the backend tracks in-progress booking sessions.
 * @property publicPosition Whether the public portal exposes workstation positions.
 * @property shortServicesEnabled Whether the portal offers quick-booking short services.
 */
@Serializable
data class EasyStaffPlanningGeneralSettings(
    @SerialName("captcha_control")
    @Serializable(with = TolerantBooleanSerializer::class)
    val captchaRequired: Boolean = false,

    @SerialName("prenotazioni_contemporanee")
    @Serializable(with = TolerantBooleanSerializer::class)
    val concurrentBookingsAllowed: Boolean = false,

    @SerialName("prenotazione_in_corso")
    @Serializable(with = TolerantBooleanSerializer::class)
    val bookingInProgressCheck: Boolean = false,

    @SerialName("public_position")
    @Serializable(with = TolerantBooleanSerializer::class)
    val publicPosition: Boolean = false,

    @SerialName("servizi_brevi")
    @Serializable(with = TolerantBooleanSerializer::class)
    val shortServicesEnabled: Boolean = false
)

/**
 * Represents the behavior of the service category selection step of a portal.
 *
 * @property publicChoice Whether the public portal lets the user choose a service category.
 */
@Serializable
data class EasyStaffPlanningServiceCategorySettings(
    @SerialName("scelta_pubblica")
    @Serializable(with = TolerantBooleanSerializer::class)
    val publicChoice: Boolean = false
)

/**
 * Represents the public login requirements of a portal.
 *
 * @property state Whether logging in is optional or mandatory for public users.
 * @property method The authentication method of the portal (e.g. "oauth2").
 * @property url The login entry point URL, or null when the portal has no dedicated one.
 * @property authBlocked Whether authentication is currently blocked for the portal.
 */
@Serializable
data class EasyStaffPlanningLoginSettings(
    @SerialName("stato")
    val state: EasyStaffPlanningLoginState = EasyStaffPlanningLoginState.Optional,

    @SerialName("metodo")
    val method: String? = null,

    @SerialName("url")
    @Serializable(with = ZeroAsNullStringSerializer::class)
    val url: String? = null,

    @SerialName("auth_blocked")
    @Serializable(with = TolerantBooleanSerializer::class)
    val authBlocked: Boolean = false
)

/**
 * Represents the display options of the availability grid of a portal.
 *
 * @property showEndTime Whether slot end times are shown alongside start times.
 */
@Serializable
data class EasyStaffPlanningGridSettings(
    @SerialName("mostra_ora_fine")
    @Serializable(with = TolerantBooleanSerializer::class)
    val showEndTime: Boolean = false
)

/**
 * Represents the visibility of the selection fields in the booking wizard of a portal.
 *
 * Visibility values are tri-state: 0 hides the field, 1 shows it, and 2 shows it with
 * additional behavior (observed on the area field of the information desks portal).
 *
 * @property serviceGroup Visibility of the service group selector.
 * @property service Visibility of the service selector.
 * @property areaGroup Visibility of the area group selector.
 * @property area Visibility of the area selector.
 * @property serviceGroupRequired Whether selecting a service group is mandatory.
 * @property areaGroupRequired Whether selecting an area group is mandatory.
 */
@Serializable
data class EasyStaffPlanningPortalFieldsSettings(
    @SerialName("raggruppamento_servizi")
    @Serializable(with = TolerantIntSerializer::class)
    val serviceGroup: Int = 0,

    @SerialName("servizi")
    @Serializable(with = TolerantIntSerializer::class)
    val service: Int = 0,

    @SerialName("raggruppamento_aree")
    @Serializable(with = TolerantIntSerializer::class)
    val areaGroup: Int = 0,

    @SerialName("aree")
    @Serializable(with = TolerantIntSerializer::class)
    val area: Int = 0,

    @SerialName("raggruppamento_servizi_obbligatorio")
    @Serializable(with = TolerantBooleanSerializer::class)
    val serviceGroupRequired: Boolean = false,

    @SerialName("raggruppamento_aree_obbligatorio")
    @Serializable(with = TolerantBooleanSerializer::class)
    val areaGroupRequired: Boolean = false
)

/**
 * Represents the front-end options of the public pages of a portal.
 *
 * @property hideEditButton Whether the edit button is hidden on managed reservations.
 * @property qrScanEnabled Whether the portal supports QR code check-in.
 * @property feedbackEnabled Whether the portal collects feedback after appointments.
 * @property showFreeResourceCount Whether the portal shows the number of free resources.
 * @property homeLayout The raw CSS-grid layout description of the portal home page.
 */
@Serializable
data class EasyStaffPlanningPublicPortalSettings(
    @SerialName("nascondi_tasto_modifica")
    @Serializable(with = TolerantBooleanSerializer::class)
    val hideEditButton: Boolean = false,

    @SerialName("scansiona_qr")
    @Serializable(with = TolerantBooleanSerializer::class)
    val qrScanEnabled: Boolean = false,

    @SerialName("feedback")
    @Serializable(with = TolerantBooleanSerializer::class)
    val feedbackEnabled: Boolean = false,

    @SerialName("mostra_numero_risorse_libere")
    @Serializable(with = TolerantBooleanSerializer::class)
    val showFreeResourceCount: Boolean = false,

    @SerialName("layout_home")
    val homeLayout: JsonElement? = null
)

/**
 * Represents whether public users must log in to use a portal.
 */
@Serializable(with = EasyStaffPlanningLoginStateSerializer::class)
sealed interface EasyStaffPlanningLoginState {
    /**
     * Logging in is optional ("facoltativo"): anonymous booking with an email address is
     * allowed.
     */
    @Serializable
    data object Optional : EasyStaffPlanningLoginState

    /**
     * Logging in is mandatory ("obbligatorio"): anonymous requests return empty listings.
     */
    @Serializable
    data object Mandatory : EasyStaffPlanningLoginState

    /**
     * A login state not known to this client.
     *
     * @property value The raw state label returned by the backend.
     */
    @Serializable
    data class Other(val value: String) : EasyStaffPlanningLoginState
}

/**
 * Represents how reservations of a service are finalized.
 */
@Serializable(with = EasyStaffPlanningReservationTypeSerializer::class)
sealed interface EasyStaffPlanningReservationType {
    /**
     * Reservations are confirmed automatically ("CONFERMATA") once the user completes the
     * booking flow.
     */
    @Serializable
    data object Confirmed : EasyStaffPlanningReservationType

    /**
     * A reservation type not known to this client.
     *
     * @property value The raw type label returned by the backend.
     */
    @Serializable
    data class Other(val value: String) : EasyStaffPlanningReservationType
}

/**
 * Represents a bookable service as returned by the service listing of a portal.
 *
 * @property id The numeric identifier of the service.
 * @property name The display name of the service (e.g. "Ritiro Badge").
 * @property durationSeconds The duration of an appointment, in seconds.
 * @property description The HTML description of the service, or null when empty.
 * @property publicGridResolution The resolution of the public availability grid, or 0 when
 * the default slot duration is used.
 * @property durationOptionsSeconds The appointment durations the user can choose from, in
 * seconds.
 * @property bookingPublicMethod The numeric code of the public booking method.
 * @property resourceGrid Whether the user picks a specific resource on a grid when booking.
 * @property group The name of the service group the service belongs to, or null when the
 * service is ungrouped.
 * @property shortService Whether the service is a quick-booking short service.
 * @property reservationType How reservations of the service are finalized.
 * @property resourceCount The number of resources (e.g. desks) serving the service.
 * @property multiBooking Whether a single booking session can create multiple reservations.
 */
@Serializable
data class EasyStaffPlanningService(
    @SerialName("id")
    val id: Int,

    @SerialName("type")
    val name: String,

    @SerialName("durata")
    val durationSeconds: Int,

    @SerialName("description")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val description: String? = null,

    @SerialName("risoluzione_griglia_pubblica")
    @Serializable(with = TolerantIntSerializer::class)
    val publicGridResolution: Int = 0,

    @SerialName("durate_select")
    val durationOptionsSeconds: List<Int> = emptyList(),

    @SerialName("booking_public_method")
    @Serializable(with = TolerantIntSerializer::class)
    val bookingPublicMethod: Int = 0,

    @SerialName("resource_grid")
    @Serializable(with = TolerantBooleanSerializer::class)
    val resourceGrid: Boolean = false,

    @SerialName("raggruppamento")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val group: String? = null,

    @SerialName("servizio_breve")
    @Serializable(with = TolerantBooleanSerializer::class)
    val shortService: Boolean = false,

    @SerialName("reservation_type")
    val reservationType: EasyStaffPlanningReservationType = EasyStaffPlanningReservationType.Confirmed,

    @SerialName("numero_risorse")
    @Serializable(with = TolerantIntSerializer::class)
    val resourceCount: Int = 1,

    @SerialName("multi_prenotazione")
    @Serializable(with = TolerantBooleanSerializer::class)
    val multiBooking: Boolean = false
)

/**
 * Represents a bookable service as embedded in the area listing, which exposes the full
 * booking constraints of the service.
 *
 * @property id The numeric identifier of the service.
 * @property portalId The numeric identifier of the portal the service belongs to.
 * @property name The display name of the service.
 * @property durationSeconds The duration of an appointment, in seconds.
 * @property description The HTML description of the service, or null when empty.
 * @property addDaysAhead The minimum number of days in advance a reservation must be created.
 * @property editDaysAhead The minimum number of days in advance a reservation can be edited.
 * @property addDeadline The time of day after which the advance-day requirement for creating
 * a reservation increases by one day.
 * @property editDeadline The time of day after which the advance-day requirement for editing
 * a reservation increases by one day.
 * @property maxRangeDays The booking horizon in days, or null when unbounded.
 * @property bookingLimitCount The maximum number of reservations a user can hold for the
 * service within [bookingLimitIntervalDays].
 * @property bookingLimitIntervalDays The number of days the booking limit applies to.
 * @property bookingLimitOnlyFuture Whether only future reservations count towards the limit.
 * @property bookingPublicMethod The numeric code of the public booking method.
 * @property virtual Whether appointments happen remotely (e.g. video call).
 * @property publicGridResolution The resolution of the public availability grid, or 0 when
 * the default slot duration is used.
 * @property followUpLink The URL sent to the user after the appointment, or null when none.
 * @property durationOptionsSeconds The appointment durations the user can choose from, in
 * seconds.
 * @property group The name of the service group the service belongs to, or null when the
 * service is ungrouped.
 * @property minimumSeatReleaseSeconds The minimum notice required to release a seat, or null
 * when seat release is unavailable.
 * @property seatReleaseEnabled Whether reservations can release their seat early.
 * @property suspensionPolicy The policy suspending users who repeatedly miss appointments,
 * or null when the service has none.
 * @property weeklyLimit The maximum number of reservations per week, or null when unbounded.
 * @property shortService Whether the service is a quick-booking short service.
 * @property resourceGrid Whether the user picks a specific resource on a grid when booking.
 * @property reservationType How reservations of the service are finalized.
 * @property multiBooking Whether a single booking session can create multiple reservations.
 */
@Serializable
data class EasyStaffPlanningDetailedService(
    @SerialName("id")
    val id: Int,

    @SerialName("cliente")
    val portalId: Int,

    @SerialName("type")
    val name: String,

    @SerialName("durata")
    val durationSeconds: Int,

    @SerialName("description")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val description: String? = null,

    @SerialName("add_prior")
    @Serializable(with = TolerantIntSerializer::class)
    val addDaysAhead: Int = 0,

    @SerialName("edit_prior")
    @Serializable(with = TolerantIntSerializer::class)
    val editDaysAhead: Int = 0,

    @SerialName("add_prior_limit_hour")
    @Serializable(with = LocalTimeSecondsSerializer::class)
    val addDeadline: LocalTime? = null,

    @SerialName("edit_prior_limit_hour")
    @Serializable(with = LocalTimeSecondsSerializer::class)
    val editDeadline: LocalTime? = null,

    @SerialName("max_range")
    val maxRangeDays: Int? = null,

    @SerialName("booking_limit_number")
    val bookingLimitCount: Int? = null,

    @SerialName("booking_limit_interval")
    val bookingLimitIntervalDays: Int? = null,

    @SerialName("booking_limit_only_future")
    @Serializable(with = TolerantBooleanSerializer::class)
    val bookingLimitOnlyFuture: Boolean = false,

    @SerialName("booking_public_method")
    @Serializable(with = TolerantIntSerializer::class)
    val bookingPublicMethod: Int = 0,

    @SerialName("virtuale")
    @Serializable(with = TolerantBooleanSerializer::class)
    val virtual: Boolean = false,

    @SerialName("risoluzione_griglia_pubblica")
    @Serializable(with = TolerantIntSerializer::class)
    val publicGridResolution: Int = 0,

    @SerialName("followup_link")
    @Serializable(with = TolerantStringSerializer::class)
    val followUpLink: String? = null,

    @SerialName("durate_select")
    val durationOptionsSeconds: List<Int> = emptyList(),

    @SerialName("raggruppamento")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val group: String? = null,

    @SerialName("min_libera_posto")
    val minimumSeatReleaseSeconds: Int? = null,

    @SerialName("libera_posto")
    @Serializable(with = TolerantBooleanSerializer::class)
    val seatReleaseEnabled: Boolean = false,

    @SerialName("daspo")
    @Serializable(with = SuspensionPolicySerializer::class)
    val suspensionPolicy: EasyStaffPlanningSuspensionPolicy? = null,

    @SerialName("limit_week")
    val weeklyLimit: Int? = null,

    @SerialName("servizio_breve")
    @Serializable(with = TolerantBooleanSerializer::class)
    val shortService: Boolean = false,

    @SerialName("resource_grid")
    @Serializable(with = TolerantBooleanSerializer::class)
    val resourceGrid: Boolean = false,

    @SerialName("reservation_type")
    val reservationType: EasyStaffPlanningReservationType = EasyStaffPlanningReservationType.Confirmed,

    @SerialName("multi_prenotazione")
    @Serializable(with = TolerantBooleanSerializer::class)
    val multiBooking: Boolean = false
)

/**
 * Represents the policy suspending users who repeatedly miss appointments ("daspo").
 *
 * @property intervalDays The number of days over which missed appointments are counted.
 * @property unattendedBookingsThreshold The number of missed appointments that triggers the
 * suspension.
 * @property suspensionDays The number of days the user is suspended for.
 * @property suspensionScope The scope of the suspension (e.g. "singolo" for the single
 * service).
 */
@Serializable
data class EasyStaffPlanningSuspensionPolicy(
    @SerialName("giorni_intervallo")
    @Serializable(with = TolerantIntSerializer::class)
    val intervalDays: Int = 0,

    @SerialName("prenotazioni_non_evase")
    @Serializable(with = TolerantIntSerializer::class)
    val unattendedBookingsThreshold: Int = 0,

    @SerialName("giorni_blocco")
    @Serializable(with = TolerantIntSerializer::class)
    val suspensionDays: Int = 0,

    @SerialName("tipo_blocco")
    val suspensionScope: String? = null
)

/**
 * Represents a group of areas (e.g. all the desks of a building).
 *
 * @property id The numeric identifier of the group.
 * @property name The display name of the group (e.g. "Edificio U17").
 * @property mapUrl The URL of the group map, or null when none.
 * @property capacity The declared capacity of the group.
 * @property order The display order of the group.
 * @property link The area-to-group association record.
 */
@Serializable
data class EasyStaffPlanningAreaGroup(
    @SerialName("ID")
    val id: Int,

    @SerialName("Nome")
    val name: String,

    @SerialName("Mappa")
    @Serializable(with = TolerantStringSerializer::class)
    val mapUrl: String? = null,

    @SerialName("Capienza")
    @Serializable(with = TolerantIntSerializer::class)
    val capacity: Int = 0,

    @SerialName("Ordine")
    @Serializable(with = TolerantIntSerializer::class)
    val order: Int = 0,

    @SerialName("pivot")
    val link: EasyStaffPlanningAreaGroupLink? = null
)

/**
 * Represents the association between an area and an area group.
 *
 * @property areaId The numeric identifier of the associated area ("Sede").
 * @property groupId The numeric identifier of the group.
 */
@Serializable
data class EasyStaffPlanningAreaGroupLink(
    @SerialName("Sede")
    val areaId: Int,

    @SerialName("Raggruppamento")
    val groupId: Int
)

/**
 * Represents a physical or virtual location where appointments take place.
 *
 * @property id The numeric identifier of the area.
 * @property portalId The numeric identifier of the portal the area belongs to.
 * @property code The short code of the area (e.g. "U17").
 * @property name The display name of the area (e.g. "Edificio U17 - IPAZIA").
 * @property address The street address of the area, or null when none (e.g. video calls).
 * @property googleMapsLink The Google Maps link of the area, or null when none.
 * @property publicHidden Whether the area is hidden from the public portal.
 * @property suspensionPolicy The missed-appointment suspension policy of the area, or null
 * when the area has none.
 * @property contact The contact person of the area, or null when none.
 * @property services The services bookable at the area, with their full booking constraints.
 * Only populated when the area listing is filtered by service; empty otherwise.
 */
@Serializable
data class EasyStaffPlanningArea(
    @SerialName("id")
    val id: Int,

    @SerialName("cliente")
    val portalId: Int,

    @SerialName("area_code")
    val code: String,

    @SerialName("area_name")
    val name: String,

    @SerialName("address")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val address: String? = null,

    @SerialName("google_map_link")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val googleMapsLink: String? = null,

    @SerialName("public_hidden")
    @Serializable(with = TolerantBooleanSerializer::class)
    val publicHidden: Boolean = false,

    @SerialName("daspo")
    @Serializable(with = SuspensionPolicySerializer::class)
    val suspensionPolicy: EasyStaffPlanningSuspensionPolicy? = null,

    @SerialName("referente")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val contact: String? = null,

    @SerialName("servizi")
    val services: List<EasyStaffPlanningDetailedService> = emptyList()
)

/**
 * Represents the input type of a booking form field.
 */
@Serializable(with = EasyStaffPlanningFieldTypeSerializer::class)
sealed interface EasyStaffPlanningFieldType {
    /**
     * An email address input.
     */
    @Serializable
    data object Email : EasyStaffPlanningFieldType

    /**
     * A single-line text input.
     */
    @Serializable
    data object Text : EasyStaffPlanningFieldType

    /**
     * A multi-line text input.
     */
    @Serializable
    data object TextArea : EasyStaffPlanningFieldType

    /**
     * A checkbox, used for consent fields like the GDPR privacy policy.
     */
    @Serializable
    data object Checkbox : EasyStaffPlanningFieldType

    /**
     * A selection between predefined options.
     */
    @Serializable
    data object Select : EasyStaffPlanningFieldType

    /**
     * A field type not known to this client.
     *
     * @property value The raw type label returned by the backend.
     */
    @Serializable
    data class Other(val value: String) : EasyStaffPlanningFieldType
}

/**
 * Represents which section of the booking request a form field belongs to.
 */
@Serializable(with = EasyStaffPlanningFieldPositionSerializer::class)
sealed interface EasyStaffPlanningFieldPosition {
    /**
     * The field describes the user (position 1) and is sent in the `utente` map of the
     * booking request.
     */
    @Serializable
    data object User : EasyStaffPlanningFieldPosition

    /**
     * The field describes the appointment (position 2) and is sent in the `servizio` map of
     * the booking request.
     */
    @Serializable
    data object Service : EasyStaffPlanningFieldPosition

    /**
     * The field is the GDPR consent checkbox (position 100) and is not sent in the booking
     * request.
     */
    @Serializable
    data object Gdpr : EasyStaffPlanningFieldPosition

    /**
     * A position not known to this client.
     *
     * @property value The raw position code returned by the backend.
     */
    @Serializable
    data class Other(val value: Int) : EasyStaffPlanningFieldPosition
}

/**
 * Represents a field of the dynamic booking form of a service.
 *
 * @property id The numeric identifier of the field.
 * @property label The display label of the field (e.g. "Cognome e Nome").
 * @property code The submission key of the field (e.g. "cognome_nome").
 * @property type The input type of the field.
 * @property values The predefined values of the field: the options of a select, or the
 * information URL of a consent checkbox. Null for free inputs.
 * @property defaultValue The pre-filled value of the field, or null when none.
 * @property placeholder The placeholder text of the field, or null when none.
 * @property primary Whether the field is the primary booking key of the portal (the email
 * address used to manage the reservation).
 * @property required Whether the field must be filled to submit the form.
 * @property serviceId The service the field is specific to, or null when the field is shared
 * by every service of the portal.
 * @property portalId The numeric identifier of the portal the field belongs to.
 * @property softwareId The numeric identifier of the EasyStaff product the field belongs to.
 * @property authPrefilled Whether the field is pre-filled from the logged-in user profile.
 * @property identifying Whether the field identifies the person the reservation is for.
 * @property blocked Whether the field is read-only.
 * @property position The section of the booking request the field belongs to.
 */
@Serializable
data class EasyStaffPlanningFormField(
    @SerialName("id")
    val id: Int,

    @SerialName("label")
    val label: String,

    @SerialName("codice")
    val code: String,

    @SerialName("tipo")
    val type: EasyStaffPlanningFieldType,

    @SerialName("valori")
    @Serializable(with = FieldValuesSerializer::class)
    val values: List<String>? = null,

    @SerialName("predefinito")
    @Serializable(with = TolerantStringSerializer::class)
    val defaultValue: String? = null,

    @SerialName("placeholder")
    @Serializable(with = TolerantStringSerializer::class)
    val placeholder: String? = null,

    @SerialName("primaria")
    @Serializable(with = TolerantBooleanSerializer::class)
    val primary: Boolean = false,

    @SerialName("obbligatorio")
    @Serializable(with = TolerantBooleanSerializer::class)
    val required: Boolean = false,

    @SerialName("servizio")
    val serviceId: Int? = null,

    @SerialName("cliente")
    val portalId: Int,

    @SerialName("software")
    val softwareId: Int = 0,

    @SerialName("valore_auth")
    @Serializable(with = TolerantBooleanSerializer::class)
    val authPrefilled: Boolean = false,

    @SerialName("identificativo")
    @Serializable(with = TolerantBooleanSerializer::class)
    val identifying: Boolean = false,

    @SerialName("blocked")
    @Serializable(with = TolerantBooleanSerializer::class)
    val blocked: Boolean = false,

    @SerialName("posizione")
    val position: EasyStaffPlanningFieldPosition = EasyStaffPlanningFieldPosition.User
)

/**
 * Represents a time window within a day, used for availability windows and slot labels.
 *
 * @property start The inclusive start of the window.
 * @property end The exclusive end of the window.
 */
@Serializable(with = EasyStaffPlanningTimeRangeSerializer::class)
data class EasyStaffPlanningTimeRange(
    val start: LocalTime,
    val end: LocalTime
) : Comparable<EasyStaffPlanningTimeRange> {
    override fun compareTo(other: EasyStaffPlanningTimeRange): Int =
        compareValuesBy(this, other, { it.start }, { it.end })

    override fun toString(): String = "$start-$end"
}

/**
 * Represents the availability of a service at an area over a month.
 *
 * @property firstAvailable The first bookable moment of the queried month, or null when the
 * month has no availability.
 * @property days The bookable windows of each day with availability. Days without
 * availability are absent.
 */
@Serializable
data class EasyStaffPlanningMonthSchedule(
    @SerialName("prima_disp")
    @Serializable(with = PlanningLocalDateTimeSerializer::class)
    val firstAvailable: LocalDateTime? = null,

    @SerialName("schedule")
    @Serializable(with = DateToTimeRangesSerializer::class)
    val days: Map<LocalDate, List<EasyStaffPlanningTimeRange>> = emptyMap()
)

/**
 * Represents the bookable state of a single time slot.
 *
 * @property availableCount The number of resources still free during the slot.
 * @property totalCount The total number of resources serving the slot.
 * @property reserved Whether the current user already holds a reservation for the slot.
 */
@Serializable
data class EasyStaffPlanningSlotAvailability(
    @SerialName("disponibili")
    @Serializable(with = TolerantIntSerializer::class)
    val availableCount: Int = 0,

    @SerialName("su")
    @Serializable(with = TolerantIntSerializer::class)
    val totalCount: Int = 0,

    @SerialName("reserved")
    @Serializable(with = TolerantBooleanSerializer::class)
    val reserved: Boolean = false
)

/**
 * Represents the slot-by-slot availability of a service at an area on a day.
 *
 * @property days The slots of each queried day, keyed by their time window.
 */
@Serializable
data class EasyStaffPlanningDaySchedule(
    @SerialName("schedule")
    @Serializable(with = DateToSlotsSerializer::class)
    val days: Map<LocalDate, Map<EasyStaffPlanningTimeRange, EasyStaffPlanningSlotAvailability>> = emptyMap()
) {
    /**
     * The slots of the single queried day, keyed by their time window.
     */
    val slots: Map<EasyStaffPlanningTimeRange, EasyStaffPlanningSlotAvailability>
        get() = days.values.firstOrNull().orEmpty()
}

/**
 * Represents a bookable resource of an area, like a desk or a counter window.
 *
 * @property id The numeric identifier of the resource.
 * @property areaId The numeric identifier of the area the resource belongs to.
 * @property code The short code of the resource (e.g. "Sportello 4").
 * @property name The display name of the resource.
 * @property description The description of the resource, or null when empty.
 * @property type The numeric code of the resource type.
 * @property orderNumber The display order of the resource, or null when unset.
 * @property visible Whether the resource is visible on the public portal.
 * @property disabled Whether the resource is currently disabled.
 * @property virtual Whether the resource hosts remote appointments.
 */
@Serializable
data class EasyStaffPlanningResource(
    @SerialName("id")
    val id: Int,

    @SerialName("area_id")
    val areaId: Int,

    @SerialName("resource_code")
    val code: String,

    @SerialName("resource_name")
    val name: String,

    @SerialName("description")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val description: String? = null,

    @SerialName("type")
    @Serializable(with = TolerantIntSerializer::class)
    val type: Int = 0,

    @SerialName("order_number")
    val orderNumber: Int? = null,

    @SerialName("visible")
    @Serializable(with = TolerantBooleanSerializer::class)
    val visible: Boolean = true,

    @SerialName("disabled")
    @Serializable(with = TolerantBooleanSerializer::class)
    val disabled: Boolean = false,

    @SerialName("virtuale")
    @Serializable(with = TolerantBooleanSerializer::class)
    val virtual: Boolean = false
)

/**
 * Represents the availability of a single resource for a service on a day.
 *
 * @property resource The resource the availability refers to.
 * @property openWindows The working windows of the resource on each queried day.
 * @property exceptions The windows of each queried day where the resource deviates from its
 * working schedule.
 * @property occupiedSlots The slots of each queried day that are already taken.
 */
@Serializable
data class EasyStaffPlanningResourceAvailability(
    @SerialName("risorsa")
    val resource: EasyStaffPlanningResource,

    @SerialName("schedule")
    @Serializable(with = DateToTimeRangesSerializer::class)
    val openWindows: Map<LocalDate, List<EasyStaffPlanningTimeRange>> = emptyMap(),

    @SerialName("exceptions")
    @Serializable(with = DateToTimeRangesSerializer::class)
    val exceptions: Map<LocalDate, List<EasyStaffPlanningTimeRange>> = emptyMap(),

    @SerialName("periods")
    @Serializable(with = DateToTimeRangesSerializer::class)
    val occupiedSlots: Map<LocalDate, List<EasyStaffPlanningTimeRange>> = emptyMap()
)

/**
 * Represents the payload of a reservation creation request.
 *
 * Reservations created through this request are provisional: they hold the slot but must be
 * finalized with the confirm endpoint within the booking session window (the web front-end
 * uses 30 minutes), and can be discarded with a forced delete.
 *
 * Prefer building instances through
 * [it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffPlanningApi.buildReservationRequest],
 * which derives the field maps from the dynamic booking form.
 *
 * @property reservationNumber The zero-based index of the reservation within a multi-booking
 * session.
 * @property portalCode The string code of the target portal.
 * @property startTimeEpochSeconds The start of the appointment as a Unix timestamp.
 * @property endTimeEpochSeconds The end of the appointment as a Unix timestamp.
 * @property durationSeconds The duration of the appointment, in seconds.
 * @property serviceId The numeric identifier of the booked service.
 * @property areaId The numeric identifier of the area of the appointment.
 * @property primaryValue The value of the primary form field (the email address used to
 * manage the reservation).
 * @property userFields The user form fields, keyed by field code.
 * @property serviceFields The appointment form fields, keyed by field code.
 * @property resourceId The specific resource to book, or null to let the backend assign one.
 * @property recaptchaToken The reCAPTCHA token, or null when the portal does not require one.
 * @property timeZone The IANA time zone the appointment times are expressed in.
 */
@Serializable
data class EasyStaffPlanningReservationRequest(
    @SerialName("reservation_number")
    val reservationNumber: Int,

    @SerialName("cliente")
    val portalCode: String,

    @SerialName("start_time")
    val startTimeEpochSeconds: Long,

    @SerialName("end_time")
    val endTimeEpochSeconds: Long,

    @SerialName("durata")
    val durationSeconds: Int,

    @SerialName("entry_type")
    val serviceId: Int,

    @SerialName("area")
    val areaId: Int,

    @SerialName("public_primary")
    val primaryValue: String,

    @SerialName("utente")
    val userFields: Map<String, String>,

    @SerialName("servizio")
    val serviceFields: Map<String, String>,

    @SerialName("risorsa")
    val resourceId: Int?,

    @SerialName("recaptchaToken")
    val recaptchaToken: String?,

    @SerialName("timezone")
    val timeZone: String
)

/**
 * Represents the outcome of a reservation creation.
 *
 * @property reservationCode The code identifying the reservation, used together with the
 * primary value to manage it.
 * @property primaryValue The primary value (email address) the reservation is keyed by.
 * @property entryId The numeric identifier of the created entry, used to confirm the
 * reservation and to download its PDF.
 * @property userIdentifier The value of the identifying form field (e.g. the full name).
 * @property startTimeEpochSeconds The start of the appointment as a Unix timestamp.
 * @property endTimeEpochSeconds The end of the appointment as a Unix timestamp.
 * @property resource The resource assigned to the appointment.
 * @property repeatId The repetition identifier of the reservation, used to download its ICS
 * file.
 * @property fieldLabels The display labels of the booking form fields, keyed by field code.
 * @property webConferenceUrl The link of the appointment video call, for virtual services.
 * @property qrCode The check-in QR code of the reservation as a base64 data URL.
 */
@Serializable
data class EasyStaffPlanningReservationResult(
    @SerialName("codice_prenotazione")
    @Serializable(with = TolerantStringSerializer::class)
    val reservationCode: String? = null,

    @SerialName("user_primary")
    @Serializable(with = TolerantStringSerializer::class)
    val primaryValue: String? = null,

    @SerialName("entry")
    @Serializable(with = EntryIdSerializer::class)
    val entryId: Int? = null,

    @SerialName("user_identifier")
    @Serializable(with = TolerantStringSerializer::class)
    val userIdentifier: String? = null,

    @SerialName("start_time")
    val startTimeEpochSeconds: Long? = null,

    @SerialName("end_time")
    val endTimeEpochSeconds: Long? = null,

    @SerialName("risorsa")
    val resource: EasyStaffPlanningResource? = null,

    @SerialName("repeat_id")
    val repeatId: Int? = null,

    @SerialName("public_fields")
    @Serializable(with = FieldLabelsSerializer::class)
    val fieldLabels: Map<String, String> = emptyMap(),

    @SerialName("web_conference_url")
    @Serializable(with = TolerantStringSerializer::class)
    val webConferenceUrl: String? = null,

    @SerialName("qr_code")
    @Serializable(with = TolerantStringSerializer::class)
    val qrCode: String? = null
)

/**
 * Represents a reservation as returned by the manage endpoint.
 *
 * @property success Whether the reservation was found. The backend omits the flag on
 * success, so it defaults to true; explicit `success: false` payloads carry an error.
 * @property reservationCode The code identifying the reservation.
 * @property primaryValue The primary value (email address) the reservation is keyed by.
 * @property publicFields The user-editable form fields of the reservation, keyed by field
 * code.
 * @property area The location of the appointment.
 * @property entry The appointment record of the reservation.
 * @property service The booked service.
 * @property seatReleaseEnabled Whether the reservation can release its seat early.
 * @property resourceName The display name of the assigned resource.
 * @property confirmed Whether the user confirmed their presence at the appointment.
 * @property status The state label of the reservation (e.g. "CONFERMATA").
 * @property qrCode The check-in QR code of the reservation as a base64 data URL, when the
 * portal uses QR check-in.
 */
@Serializable
data class EasyStaffPlanningManagedReservation(
    @SerialName("success")
    @Serializable(with = TolerantBooleanSerializer::class)
    val success: Boolean = true,

    @SerialName("codice")
    @Serializable(with = TolerantStringSerializer::class)
    val reservationCode: String? = null,

    @SerialName("primaria")
    @Serializable(with = TolerantStringSerializer::class)
    val primaryValue: String? = null,

    @SerialName("public_fields")
    @Serializable(with = StringValuesMapSerializer::class)
    val publicFields: Map<String, String?> = emptyMap(),

    @SerialName("area")
    val area: EasyStaffPlanningReservationArea? = null,

    @SerialName("entry")
    val entry: EasyStaffPlanningReservationEntry? = null,

    @SerialName("servizio")
    val service: EasyStaffPlanningReservationService? = null,

    @SerialName("libera_posto")
    @Serializable(with = TolerantBooleanSerializer::class)
    val seatReleaseEnabled: Boolean = false,

    @SerialName("risorsa")
    @Serializable(with = TolerantStringSerializer::class)
    val resourceName: String? = null,

    @SerialName("confermata")
    @Serializable(with = TolerantBooleanSerializer::class)
    val confirmed: Boolean = false,

    @SerialName("stato")
    @Serializable(with = TolerantStringSerializer::class)
    val status: String? = null,

    @SerialName("qr_code")
    @Serializable(with = TolerantStringSerializer::class)
    val qrCode: String? = null
)

/**
 * Represents the location of a managed reservation.
 *
 * @property name The display name of the area (e.g. "Edificio U7 - CIVITAS").
 * @property address The street address of the area, or null when none.
 */
@Serializable
data class EasyStaffPlanningReservationArea(
    @SerialName("name")
    val name: String? = null,

    @SerialName("address")
    @Serializable(with = TolerantStringSerializer::class)
    val address: String? = null
)

/**
 * Represents the appointment record of a managed reservation.
 *
 * @property id The numeric identifier of the entry, used to confirm the reservation and to
 * download its PDF.
 * @property startTimeEpochSeconds The start of the appointment as a Unix timestamp.
 * @property endTimeEpochSeconds The end of the appointment as a Unix timestamp.
 * @property date The day of the appointment.
 */
@Serializable
data class EasyStaffPlanningReservationEntry(
    @SerialName("id")
    val id: Int,

    @SerialName("start_time")
    val startTimeEpochSeconds: Long? = null,

    @SerialName("end_time")
    val endTimeEpochSeconds: Long? = null,

    @SerialName("day_time")
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate? = null
)

/**
 * Represents the booked service of a managed reservation.
 *
 * @property name The display name of the service.
 * @property shortService Whether the service is a quick-booking short service.
 */
@Serializable
data class EasyStaffPlanningReservationService(
    @SerialName("type")
    val name: String? = null,

    @SerialName("servizio_breve")
    @Serializable(with = TolerantBooleanSerializer::class)
    val shortService: Boolean = false
)

/**
 * Represents the generic acknowledgement the write endpoints return.
 *
 * @property success Whether the operation succeeded. Operations that complete with a
 * successful status code but no explicit flag are reported as successful.
 */
@Serializable
data class EasyStaffPlanningResult(
    @SerialName("success")
    @Serializable(with = TolerantBooleanSerializer::class)
    val success: Boolean = true
)

/**
 * Envelope of the service group listing.
 *
 * @property groups The service groups of the portal.
 */
@Serializable
internal data class EasyStaffPlanningServiceGroupsEnvelope(
    @SerialName("raggruppamenti_servizi")
    val groups: List<EasyStaffPlanningNamedGroup> = emptyList()
)

/**
 * A group entry identified only by its name.
 *
 * @property name The display name of the group.
 */
@Serializable
internal data class EasyStaffPlanningNamedGroup(
    @SerialName("nome")
    val name: String
)

/**
 * Envelope of the service listing.
 *
 * @property services The services of the portal.
 */
@Serializable
internal data class EasyStaffPlanningServicesEnvelope(
    @SerialName("servizi")
    val services: List<EasyStaffPlanningService> = emptyList()
)

/**
 * Envelope of the area group listing.
 *
 * @property groups The area groups of the portal.
 */
@Serializable
internal data class EasyStaffPlanningAreaGroupsEnvelope(
    @SerialName("raggruppamenti_area")
    val groups: List<EasyStaffPlanningAreaGroup> = emptyList()
)

/**
 * Envelope of the area listing.
 *
 * @property areas The areas of the portal.
 */
@Serializable
internal data class EasyStaffPlanningAreasEnvelope(
    @SerialName("aree")
    val areas: List<EasyStaffPlanningArea> = emptyList()
)
