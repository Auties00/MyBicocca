package it.attendance100.mybicocca.ui.screen.registry.subscreen.library

import java.time.DayOfWeek

/**
 * Stable `testTag` identifiers for [LibraryPage] and its in-sheet pages, referenced by both the
 * screen and its UI tests so a user-visible copy change never breaks a test and a tag rename is a
 * compile error rather than a silently missed node.
 */
object LibraryTestTags {
    const val ROOT = "library:root"

    const val HOME_PAGE = "library:page:home"
    const val HOME_SIGN_IN = "library:home:signIn"
    const val HOME_ACCOUNT = "library:home:account"
    const val HOME_LIBRARIES_CARD = "library:home:librariesCard"
    const val HOME_NEXT_BOOKING = "library:home:nextBooking"
    const val HOME_BOOKINGS_ROW = "library:home:bookingsRow"
    const val HOME_SERVICES_ROW = "library:home:servicesRow"

    const val LIBRARIES_PAGE = "library:page:libraries"
    const val LIBRARIES_LOADING = "library:libraries:loading"
    const val LIBRARIES_ERROR = "library:libraries:error"
    const val LIBRARIES_EMPTY = "library:libraries:empty"

    const val DETAIL_PAGE = "library:page:detail"
    const val DETAIL_BOOK_BUTTON = "library:detail:bookButton"
    const val DETAIL_CROWDING_CHART = "library:detail:crowdingChart"
    const val DETAIL_CROWDING_DAY_DROPDOWN = "library:detail:crowdingDayDropdown"
    const val DETAIL_INFO_HOURS = "library:detail:infoHours"
    const val DETAIL_INFO_ADDRESS = "library:detail:infoAddress"
    const val DETAIL_INFO_FLOOR = "library:detail:infoFloor"
    const val DETAIL_INFO_WEBSITE = "library:detail:infoWebsite"
    const val DETAIL_INFO_PHONE = "library:detail:infoPhone"
    const val DETAIL_INFO_EMAIL = "library:detail:infoEmail"

    const val ADDRESS_CHOOSER_PAGE = "library:page:addressChooser"
    const val ADDRESS_CHOOSER_MAP = "library:addressChooser:map"
    const val ADDRESS_CHOOSER_EXTERNAL = "library:addressChooser:external"

    const val BOOKINGS_PAGE = "library:page:bookings"
    const val BOOKINGS_SYNCING = "library:bookings:syncing"
    const val BOOKINGS_EMPTY = "library:bookings:empty"
    const val BOOKINGS_EMPTY_BOOK = "library:bookings:emptyBook"
    const val BOOKINGS_HISTORY_EMPTY = "library:bookings:historyEmpty"
    const val BOOKINGS_LIST = "library:bookings:list"
    const val BOOKINGS_TAB_SWITCH = "library:bookings:tabSwitch"
    const val BOOKINGS_LIVE_VERIFY = "library:bookings:liveVerify"

    const val RESERVATION_DETAIL_PAGE = "library:page:reservationDetail"
    const val RESERVATION_DETAIL_MAP = "library:reservationDetail:map"
    const val RESERVATION_DETAIL_CANCEL = "library:reservationDetail:cancel"
    const val RESERVATION_DETAIL_VERIFY = "library:reservationDetail:verify"

    const val SERVICES_PAGE = "library:page:services"
    const val SERVICES_ALL = "library:services:all"

    const val ACCOUNT_PAGE = "library:page:account"
    const val ACCOUNT_LOGOUT = "library:account:logout"

    const val CONFIRM_INFO_CARD = "library:confirm:infoCard"
    const val CONFIRM_SUBMIT = "library:confirm:submit"

    const val LOGIN_PAGE = "library:page:login"
    const val LOGIN_RESEND = "library:login:resend"

    /** Per-library card in the bookable libraries list, keyed by the library id. */
    fun library(id: String): String = "library:card:$id"

    /** Per-booking card in My Bookings, keyed by the server reservation id. */
    fun booking(id: Int): String = "library:booking:$id"

    /** Per-service row in the Library Services directory, keyed by its URL slug. */
    fun service(slug: String): String = "library:service:$slug"

    /** Per-slot tap column in the weekly crowding chart, keyed by weekday and bucket index. */
    fun crowdingSlot(weekday: DayOfWeek, index: Int): String =
        "library:detail:crowdingSlot:${weekday.name}:$index"

    /** Active callout above the crowding bars on a given weekday page. */
    fun crowdingCallout(weekday: DayOfWeek): String =
        "library:detail:crowdingCallout:${weekday.name}"
}
