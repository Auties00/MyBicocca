package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state

/**
 * One-shot effects emitted by `TaxesViewModel` for the pagoPA actions — launching the payment
 * URL, opening a downloaded PDF, surfacing a status message. Consumed once by the detail
 * screen (Channel-backed), never replayed across rotation.
 */
sealed interface TaxEvent {
    data class OpenUrl(val url: String) : TaxEvent
    class OpenPdf(val bytes: ByteArray, val fileName: String) : TaxEvent
    data class ShowMessage(val message: String) : TaxEvent
}
