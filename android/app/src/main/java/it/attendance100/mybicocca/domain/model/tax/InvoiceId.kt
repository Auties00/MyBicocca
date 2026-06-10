package it.attendance100.mybicocca.domain.model.tax

/**
 * Identifier of an Esse3 tuition invoice (fattura), used to key payment actions such as
 * starting a pagoPA transaction or downloading the notice/receipt PDFs.
 *
 * @property value The numeric invoice id assigned by Esse3.
 */
@JvmInline
value class InvoiceId(val value: Long)
