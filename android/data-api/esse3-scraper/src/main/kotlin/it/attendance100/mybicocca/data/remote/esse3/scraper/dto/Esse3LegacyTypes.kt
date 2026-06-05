package it.attendance100.mybicocca.data.remote.esse3.scraper.dto

/**
 * DTOs for [it.attendance100.mybicocca.data.remote.esse3.scraper.api.Esse3LegacyApi].
 *
 * These cover the Esse3 student areas that have **no REST counterpart** on the
 * Bicocca instance and are therefore only reachable by scraping the legacy
 * Struts (`.do`) pages. Currently modelled:
 *
 * - Autocertificazioni / certificati (self-declarations → PDF)
 *
 * Field naming mirrors the REST DTOs in the sibling `esse3` module: camelCase,
 * `Esse3` prefix, `*Status`/`*Type` sealed interfaces with a `fromX` companion.
 */

/**
 * A self-declaration template offered on `/auth/studente/Certificati/ListaCertificati.do`.
 *
 * Each entry, when requested, produces a pre-filled PDF. The identifying
 * parameters below are exactly those Esse3 puts on the request link
 * (`MessaggiCertificato.do?cert_conf_id=…&doc_id=…&…`).
 */
data class Esse3SelfCertification(
    /**
     * Certificate configuration id (`cert_conf_id`).
     */
    val configurationId: Int,

    /**
     * Underlying document template id (`doc_id`).
     */
    val documentId: Int,

    /**
     * Human-readable label, e.g. "Autodichiarazione Iscrizione con Esami".
     */
    val description: String,

    /**
     * Certificate type code (`tipo_cert_cod`): I = iscrizione, L = laurea,
     * TAX = tasse, … See [Esse3SelfCertificationType].
     */
    val type: Esse3SelfCertificationType,

    /**
     * Solar year the declaration refers to (`annosolare`), when applicable
     * (mainly the tax declarations). `null` for year-independent certificates.
     */
    val solarYear: Int?,

    /**
     * Whether the certificate is issued with a digital signature
     * (`firma_dig_flg` set).
     */
    val digitallySigned: Boolean,

    /**
     * Relative path of the request link; pass to
     * [it.attendance100.mybicocca.data.remote.esse3.scraper.api.Esse3LegacyApi.downloadSelfCertification].
     */
    val requestPath: String
)

/**
 * The certificate-type code carried by `tipo_cert_cod`.
 */
sealed interface Esse3SelfCertificationType {
    /** Enrolment-related declaration (`I`). */
    data object Enrolment : Esse3SelfCertificationType

    /** Degree-award declaration (`L`). */
    data object DegreeAward : Esse3SelfCertificationType

    /** Tuition-fees declaration (`TAX`). */
    data object TuitionFees : Esse3SelfCertificationType

    /** Any other / future code. */
    data class Other(val code: String) : Esse3SelfCertificationType

    companion object {
        fun fromCode(code: String): Esse3SelfCertificationType =
            when (code.trim().uppercase()) {
                "I" -> Enrolment
                "L" -> DegreeAward
                "TAX" -> TuitionFees
                else -> Other(code)
            }
    }
}

// TODO: Diritto allo studio bandi DTOs (iniziative, borse, mobilità, 150 ore,
//  corsi elettivi). Removed until a populated bando can be scraped to model the
//  list rows and the per-bando detail page in depth.

// TODO: Proroga DTOs (extension request + receipt). Removed until a real
//  submitted request can be scraped to model the row and detail markup.
