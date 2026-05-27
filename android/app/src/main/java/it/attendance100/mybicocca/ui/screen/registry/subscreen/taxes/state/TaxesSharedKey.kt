package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state

// Keys for the list-ticket -> detail-ticket shared element morph. The same (taxId, element)
// pair is registered on the list card and on the detail Invoice so Compose matches them.
data class TaxesSharedKey(val taxId: Long, val element: TaxesSharedElement)

enum class TaxesSharedElement { InvoiceNumber, Amount, Status, Description }
