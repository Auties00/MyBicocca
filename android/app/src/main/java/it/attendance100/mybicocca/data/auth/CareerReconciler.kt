package it.attendance100.mybicocca.data.auth

import it.attendance100.mybicocca.domain.model.account.AccountEvent
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.isSelectable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareerReconciler @Inject constructor() {

    fun reconcile(
        accountId: AccountId,
        previous: List<Career>,
        current: List<Career>,
        currentSelectedId: CareerId,
    ): List<AccountEvent> {
        val events = mutableListOf<AccountEvent>()
        val previousIds = previous.mapTo(mutableSetOf()) { it.id }

        for (career in current) {
            if (career.id !in previousIds && career.status.isSelectable) {
                events += AccountEvent.NewCareerAvailable(accountId, career)
            }
        }

        val selected = current.firstOrNull { it.id == currentSelectedId }
        when {
            selected == null -> events += AccountEvent.SelectedCareerMissing(accountId)
            !selected.status.isSelectable -> events += AccountEvent.SelectedCareerEnded(accountId, selected)
        }

        return events
    }
}
