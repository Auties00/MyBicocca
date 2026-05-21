package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3BadgeClass(
    /** identificativo numerico */
    @SerialName("id")
    val id: Long = 0L,

    /** campo Identificativo */
    @SerialName("identificativo")
    val identifier: String? = null,

    /** status */
    @SerialName("status")
    val status: String = "",

    /** nome della badge class */
    @SerialName("obiname")
    val obiName: String = "",

    /** descrizione */
    @SerialName("obidescription")
    val obiDescription: String = "",

    /** data di creazione */
    @SerialName("createdAt")
    val createdAt: String = "",

    /** data di ultima modifica */
    @SerialName("updatedAt")
    val updatedAt: String? = null,

    /** visual del badge */
    @SerialName("imageUrl")
    val imageUrl: String? = null,

    /** attributo aggiuntivo alla badge class viene recuperato solo in get ignorato nella post */
    @SerialName("alignment")
    val alignment: String? = null,

    @SerialName("issuer")
    val issuer: Esse3Issuer,

    @SerialName("customData")
    val customData: List<Esse3CustomData> = emptyList()
)

@Serializable
data class Esse3Issuer(
    /** id alfanumerico del issuer */
    @SerialName("idIssuer")
    val issuerId: String = "",

    /** codice miur dell issuer */
    @SerialName("miurCode")
    val miurCode: String? = null,

    /** sito web istituzionale o dell organizzazione */
    @SerialName("url")
    val url: String? = null,

    /** email dell issuer */
    @SerialName("email")
    val email: String? = null,

    /** nome dell issuer */
    @SerialName("name")
    val name: String? = null,

    /** visual dell'issuer */
    @SerialName("imageUrl")
    val imageUrl: String? = null,

    /** slug dell'issuer */
    @SerialName("slug")
    val slug: String? = null
)

@Serializable
data class Esse3AwardReturn(
    /** identificativo numerico della testata */
    @SerialName("tstId")
    val testId: Long = 0L,

    /** data di creazione della testata */
    @SerialName("dataCreazione")
    val creationDate: String? = null,

    /** stato dell elenco */
    @SerialName("statoCod")
    val stateCode: String? = null,

    /** nota */
    @SerialName("nota")
    val note: String? = null,

    /** identificativo numerico */
    @SerialName("idBadgeClass")
    val badgeClassId: Long? = null,

    /** codice della badge class */
    @SerialName("identificativoBadge")
    val badgeIdentifier: String? = null,

    @SerialName("awardInfo")
    val awardInfo: List<Esse3AwardInfo> = emptyList(),

    @SerialName("awardErrorDett")
    val awardErrorDetails: List<Esse3AwardErrorDetail> = emptyList()
)

@Serializable
data class Esse3BadgeClassReturn(
    /** identificativo numerico della testata */
    @SerialName("tstId")
    val testId: Long = 0L,

    /** data di creazione della testata */
    @SerialName("dataCreazione")
    val creationDate: String? = null,

    /** stato dell elenco */
    @SerialName("statoCod")
    val stateCode: String? = null,

    /** nota */
    @SerialName("nota")
    val note: String? = null,

    @SerialName("badgeClassesInfo")
    val badgeClassesInfo: List<Esse3BadgeClassInfo> = emptyList(),

    @SerialName("badgeClassesErrorDett")
    val badgeClassesErrorDetails: List<Esse3BadgeClassErrorDetail> = emptyList()
)

@Serializable
data class Esse3BadgeClassInfo(
    /** identificativo */
    @SerialName("id")
    val id: Long = 0L,

    /** identificativo numerico */
    @SerialName("idNumerico")
    val numericId: Long? = null,

    /** codice dello stato */
    @SerialName("statoCod")
    val stateCode: String? = null,

    /** campo Identificativo */
    @SerialName("identificativo")
    val identifier: String? = null,

    /** status */
    @SerialName("status")
    val status: String = "",

    /** nome della badge class */
    @SerialName("name")
    val name: String? = null,

    /** descrizione */
    @SerialName("description")
    val description: String? = null,

    /** data di creazione */
    @SerialName("createdAt")
    val createdAt: String = "",

    /** data di ultima modifica */
    @SerialName("updatedAt")
    val updatedAt: String? = null,

    /** visual del badge */
    @SerialName("imageUrl")
    val imageUrl: String? = null,

    /** id alfanumerico del issuer */
    @SerialName("idIssuer")
    val issuerId: String? = null,

    /** codice miur dell issuer */
    @SerialName("codMiurIssuer")
    val miurIssuerCode: String? = null,

    /** sito web istituzionale o dell organizzazione */
    @SerialName("urlIssuer")
    val issuerUrl: String? = null,

    /** email dell issuer */
    @SerialName("emailIssuer")
    val issuerEmail: String? = null,

    /** nome dell issuer */
    @SerialName("nomeIssuer")
    val issuerName: String? = null,

    /** visual dell'issuer */
    @SerialName("imageUrlIssuer")
    val issuerImageUrl: String? = null,

    /** nota */
    @SerialName("nota")
    val note: String? = null,

    /** slug dell'issuer */
    @SerialName("slugIssuer")
    val issuerSlug: String? = null,

    /** tipo titolo */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    /** descrizione del titpo titolo titolo di scuola superiore */
    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    /** codice del livello titolo */
    @SerialName("livelloCod")
    val levelCode: String? = null,

    /** descrizione del livello titolo */
    @SerialName("livelloDes")
    val levelDescription: String? = null
)

@Serializable
data class Esse3BadgeClassErrorDetail(
    /** identificativo */
    @SerialName("id")
    val id: Long = 0L,

    /** identificativo numerico */
    @SerialName("idNumerico")
    val numericId: Long? = null,

    /** codice dello stato */
    @SerialName("statoCod")
    val stateCode: String? = null,

    /** campo Identificativo */
    @SerialName("identificativo")
    val identifier: String? = null,

    /** status */
    @SerialName("status")
    val status: String? = null,

    /** nome della badge class */
    @SerialName("name")
    val name: String? = null,

    /** descrizione */
    @SerialName("description")
    val description: String? = null,

    /** data di creazione */
    @SerialName("createdAt")
    val createdAt: String? = null,

    /** data di ultima modifica */
    @SerialName("updatedAt")
    val updatedAt: String? = null,

    /** visual del badge */
    @SerialName("imageUrl")
    val imageUrl: String? = null,

    /** id alfanumerico del issuer */
    @SerialName("idIssuer")
    val issuerId: String? = null,

    /** codice miur dell issuer */
    @SerialName("codMiurIssuer")
    val miurIssuerCode: String? = null,

    /** sito web istituzionale o dell organizzazione */
    @SerialName("urlIssuer")
    val issuerUrl: String? = null,

    /** email dell issuer */
    @SerialName("emailIssuer")
    val issuerEmail: String? = null,

    /** nome dell issuer */
    @SerialName("nomeIssuer")
    val issuerName: String? = null,

    /** visual dell'issuer */
    @SerialName("imageUrlIssuer")
    val issuerImageUrl: String? = null,

    /** nota */
    @SerialName("nota")
    val note: String? = null,

    /** slug dell'issuer */
    @SerialName("slugIssuer")
    val issuerSlug: String? = null
)

@Serializable
data class Esse3BadgeIssuanceNotification(
    /** tipo di evento, verificatosi in Esse3, che ha portato alla richiesta di emissione o annullamento badge (SUPERAMENTO=superamento esame, ANNULLAMENTO=annullamento esame, CONSEG_TITOLO=conseguimento titolo) */
    @SerialName("evento")
    val event: String = "",

    /** ID univoco del dato a cui si riferisce l'evento (nel caso di superamento esame è l'ID della riga di libretto studente con l'AD superata) */
    @SerialName("refId")
    val referenceId: Long = 0L,

    /** ID univoco del badge emesso sul sistema esterno */
    @SerialName("badgeId")
    val badgeId: String? = null,

    /** email dello studente per il quale è avvenuta l’emissione del badge sul sistema esterno */
    @SerialName("email")
    val email: String? = null,

    /** data/ora di emissione del badge sul sistema esterno */
    @SerialName("dataEmissione")
    val issuanceDate: String? = null,

    /** causale relativa all’emissione/annullamento del badge sul sistema esterno */
    @SerialName("causale")
    val reason: String? = null
)

@Serializable
data class Esse3AwardErrorDetail(
    /** identificativo del dettaglio */
    @SerialName("id")
    val id: Long = 0L,

    /** codice dello stato */
    @SerialName("statoCod")
    val stateCode: String = "",

    /** identificativo numerico interno dell utente a cui è stato dato l award */
    @SerialName("awardedUser")
    val awardedUser: Long? = null,

    /** identificativo numerico interno del Badge a cui si fa riferimento */
    @SerialName("earnedBadge")
    val earnedBadge: Long? = null,

    /** email  dell' utente a cui è stato rilasciato il badge */
    @SerialName("awardedEmail")
    val awardedEmail: String = "",

    /** data di creazione */
    @SerialName("createdAt")
    val createdAt: String = "",

    /** nota */
    @SerialName("nota")
    val note: String? = null,

    /** Utente di inserimento. */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data di inserimento. */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente di ultima modifica. */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data di ultima modifica. */
    @SerialName("dataMod")
    val modificationDate: String? = null
)

@Serializable
data class Esse3Award(
    /** identificativo numerico interno dell utente a cui è stato dato l award */
    @SerialName("awardedUser")
    val awardedUser: Long? = null,

    /** identificativo numerico interno del Badge a cui si fa riferimento */
    @SerialName("earnedBadge")
    val earnedBadge: Long = 0L,

    /** email  dell' utente a cui è stato rilasciato il badge */
    @SerialName("awardedEmail")
    val awardedEmail: String = "",

    /** data di creazione dell award */
    @SerialName("createdAt")
    val createdAt: String = "",

    @SerialName("customData")
    val customData: List<Esse3CustomData> = emptyList()
)

@Serializable
data class Esse3CustomData(
    /** codice attributo esterno */
    @SerialName("externalKey")
    val externalKey: String = "",

    /** identificativo attributo esterno */
    @SerialName("externalValue")
    val externalValue: String = ""
)

@Serializable
data class Esse3AwardInfo(
    /** identificativo */
    @SerialName("id")
    val id: Long = 0L,

    /** codice dello stato */
    @SerialName("statoCod")
    val stateCode: String? = null,

    /** identificativo numerico interno dell utente a cui è stato dato l award */
    @SerialName("awardedUser")
    val awardedUser: Long? = null,

    /** identificativo numerico interno del Badge a cui si fa riferimento */
    @SerialName("earnedBadge")
    val earnedBadge: Long = 0L,

    /** email  dell' utente a cui è stato rilasciato il badge */
    @SerialName("awardedEmail")
    val awardedEmail: String = "",

    /** data di creazione */
    @SerialName("createdAt")
    val createdAt: String = "",

    /** nota */
    @SerialName("nota")
    val note: String? = null,

    /** identificativo  della persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** anno di conseguimento titolo */
    @SerialName("aaConsegTitolo")
    val academicYearTitleAward: Long? = null,

    /** data di conseguimento titolo */
    @SerialName("dataConseguimentoTitolo")
    val titleAchievementDate: String? = null,

    /** tipo titolo */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    /** codice di dettaglio del titolo */
    @SerialName("tititCod")
    val titleTypeCode: String? = null,

    /** Indica in quale forma è stato depositato il titolo originale, fotocopia, autocertificazione. */
    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    /** Flag che indica se il titolo è stato ottenuto nello stesso ateneo. */
    @SerialName("stessoAteneoFlg")
    val sameUniversityFlag: Long? = null,

    /** Stato del titolo */
    @SerialName("staTitItCod")
    val italianTitleStatusCode: String? = null,

    /** Data di scadenza */
    @SerialName("dataScadenza")
    val expirationDate: String? = null
)
