package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state

/** Pages stacked inside the Mensa [it.attendance100.mybicocca.ui.component.modal.SheetPager]. */
sealed interface MensaSubPage {
    val key: String

    data object Home : MensaSubPage {
        override val key: String = "home"
    }

    data object Menu : MensaSubPage {
        override val key: String = "menu"
    }

    data object FoodCatalog : MensaSubPage {
        override val key: String = "food_catalog"
    }

    data object Allergens : MensaSubPage {
        override val key: String = "allergens"
    }

    data object Account : MensaSubPage {
        override val key: String = "account"
    }

    data class AccountEdit(val field: MensaAccountField) : MensaSubPage {
        override val key: String = "account_edit_${field.name.lowercase()}"
    }

    /** First step: where (one of the two canteens or the three bars). */
    data object PlacePicker : MensaSubPage {
        override val key: String = "place_picker"
    }

    /** Second step, for the canteens only: the menu or its gluten-free variant. */
    data object MenuPicker : MensaSubPage {
        override val key: String = "menu_picker"
    }

    /** Third step: which day, and which meal when the place serves more than one. */
    data object MenuSetup : MensaSubPage {
        override val key: String = "menu_setup"
    }

    data object DishPager : MensaSubPage {
        override val key: String = "dish_pager"
    }

    data object AccountDelete : MensaSubPage {
        override val key: String = "account_delete"
    }

    data object Login : MensaSubPage {
        override val key: String = "login"
    }

    /** Compass's sign-up form, opened from the login page. */
    data object Register : MensaSubPage {
        override val key: String = "register"
    }

    data object OrderSummary : MensaSubPage {
        override val key: String = "order_summary"
    }

    data object TakeawayOrder : MensaSubPage {
        override val key: String = "takeaway_order"
    }

    data object Reservations : MensaSubPage {
        override val key: String = "reservations"
    }

    data class ReservationDetail(val reservationId: Int) : MensaSubPage {
        override val key: String = "reservation_$reservationId"
    }
}

/** What the dish carousel browses: a list of dishes and the one it opens on. */
data class MensaDishPagerRequest(
    val dishes: List<it.attendance100.mybicocca.domain.model.mensa.MensaDish>,
    val startIndex: Int,
    /** When opened from the menu: the list item of each dish, so the menu can follow the carousel. */
    val menuRows: List<Int>? = null,
)

/** Loading state of one dish's full detail, fetched as the carousel reaches it. */
sealed interface MensaDishDetailState {
    data object Loading : MensaDishDetailState
    data class Loaded(val detail: it.attendance100.mybicocca.domain.model.mensa.MensaDishDetail) : MensaDishDetailState
    data object Failed : MensaDishDetailState
}

/** Outcome of asking the operator whether the signed-in account can be deleted. */
sealed interface MensaDeletionCheck {
    data object Checking : MensaDeletionCheck
    data object Allowed : MensaDeletionCheck
    data class NotAllowed(val reason: String?) : MensaDeletionCheck
    data object Failed : MensaDeletionCheck
}

/** Account details the user can change from the Mensa account page. */
enum class MensaAccountField { PROFILE, EMAIL, PASSWORD }

/** One-shot events emitted by [it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaViewModel]. */
sealed interface MensaEvent {
    data object LoginSucceeded : MensaEvent
    data class AccountUpdated(val field: MensaAccountField) : MensaEvent
    data class AccountUpdateFailed(val cause: Throwable) : MensaEvent
    data class AccountDeletionRequested(val message: String?) : MensaEvent
    data class AccountDeletionFailed(val cause: Throwable) : MensaEvent
    data class LoginFailed(val cause: Throwable) : MensaEvent
    data class OrderSubmitted(val reservationCode: String?) : MensaEvent
    data class OrderFailed(val cause: Throwable) : MensaEvent
    data object OrderCancelled : MensaEvent
    data class CancelFailed(val cause: Throwable) : MensaEvent
    data class SyncFailed(val cause: Throwable) : MensaEvent
}
