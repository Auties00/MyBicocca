package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3AttachmentType(
    /** tipologia dell'allegato */
    @SerialName("typeId")
    val typeId: Int? = null,

    /** codice della tipologia dell'allegato */
    @SerialName("typeCod")
    val typeCode: String? = null,

    /** indica se l'allegato supporta il download */
    @SerialName("downloadSupported")
    val downloadSupported: Int? = null,

    /** indica se l'allegato supporta l'upload */
    @SerialName("uploadSupported")
    val uploadSupported: Int? = null
)

@Serializable
data class Esse3AttachmentMetadata(
    /** nome del file */
    @SerialName("filename")
    val fileName: String? = null,

    /** autore del file */
    @SerialName("autore")
    val author: String? = null,

    /** titolo del file */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione libera */
    @SerialName("descrizione")
    val description: String? = null,

    /** tipologia dell'allegato relativo all'entità p17_tipologia_allegati */
    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    /** indica se l'allegato è valido (1) oppure no (0) */
    @SerialName("validoFlg")
    val validFlag: Long? = null,

    /** codice del tipo di allegato */
    @SerialName("tipoAllegato")
    val attachmentType: String? = null,

    /** id dell'allegato */
    @SerialName("allegatoId")
    val attachmentId: Long? = null
)

@Serializable
data class Esse3DettaglioErroreAggiuntivo(
    /** descrizione del tipo di errore aggiuntivo */
    @SerialName("errorType")
    val errorType: String? = null,

    /** descrizione dell'errore */
    @SerialName("value")
    val value: String? = null,

    /** descrizione dell'errore */
    @SerialName("rawValue")
    val rawValue: String? = null
)

@Serializable
data class Esse3AttachmentTypeCode(
    /** id della lingua */
    @SerialName("linguaId")
    val languageId: Long? = null,

    /** codice ISO6392 della lingua */
    @SerialName("linguaCod")
    val languageCode: String? = null,

    /** codice della tipologia di allegato */
    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    /** descrizione della tipologia di allegato */
    @SerialName("des")
    val description: String? = null,

    /** massima dimensione in MB dei file in upload */
    @SerialName("maxUploadFileSize")
    val maxUploadFileSize: Long? = null,

    /** abilita la visibilità degli allegati della tipologia anche se all'atto del caricamento non erano associati alla regola che li mostra */
    @SerialName("abilVisNoReg")
    val noRegistrationVisibility: Int? = null,

    /** abilita la stampa dell'allegato della tipologia nella stampa generica degli allegati. */
    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3AttachmentExtension(
    /** id estensione */
    @SerialName("estensioneId")
    val extensionId: Long? = null,

    /** codice estensione */
    @SerialName("estensione")
    val extension: String? = null,

    /** descrizione estensione */
    @SerialName("estensioneDes")
    val extensionDescription: String? = null
)

@Serializable
data class Esse3GenericAttachmentInsertMetadata(
    @SerialName("proprietaAggiuntive")
    val additionalProperties: List<Esse3AdditionalProperty> = emptyList(),

    @SerialName("chiaviCollegamento")
    val linkKeys: List<Esse3LinkKey> = emptyList(),

    /** nome del file */
    @SerialName("filename")
    val fileName: String? = null,

    /** autore del file */
    @SerialName("autore")
    val author: String? = null,

    /** titolo del file */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione libera */
    @SerialName("descrizione")
    val description: String? = null,

    /** tipologia dell'allegato relativo all'entità p17_tipologia_allegati */
    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    /** indica se l'allegato è valido (1) oppure no (0) */
    @SerialName("validoFlg")
    val validFlag: Long? = null
)

@Serializable
data class Esse3PatchAttachmentMetadata(
    /** indica se l'allegato è valido (1) oppure no (0) */
    @SerialName("validoFlg")
    val validFlag: Long? = null
)

@Serializable
data class Esse3UploadMetadata(
    /** stato di upload dell'allegato (WAITING,PROCESSING,DONE) */
    @SerialName("statoUpload")
    val uploadState: String? = null,

    @SerialName("chiaviCollegamento")
    val linkKeys: List<Esse3LinkKey> = emptyList(),

    @SerialName("proprietaAggiuntive")
    val additionalProperties: List<Esse3AdditionalPropertyUpload> = emptyList(),

    /** indica se l'allegato è valido (1) oppure no (0) */
    @SerialName("validoFlg")
    val validFlag: Long? = null,

    /** tipologia dell'allegato relativo all'entità p17_tipologia_allegati */
    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    /** descrizione libera */
    @SerialName("descrizione")
    val description: String? = null,

    /** titolo del file */
    @SerialName("titolo")
    val title: String? = null,

    /** autore del file */
    @SerialName("autore")
    val author: String? = null,

    /** nome del file */
    @SerialName("filename")
    val fileName: String? = null,

    /** codice del tipo di allegato nel caso l'upload si riferisca ad un dato gestito negli allegati */
    @SerialName("tipoAllegato")
    val attachmentType: String? = null,

    /** id dell'allegato nel caso l'upload sia terminato e l'upload si riferisca ad un dato gestito negli allegati */
    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    /** id dell'upload */
    @SerialName("uploadId")
    val uploadId: Long? = null
)

@Serializable
data class Esse3AdditionalProperty(
    /** nome della proprietà aggiuntiva */
    @SerialName("nome")
    val name: String = "",

    /** valore della proprietà nel caso di stringa */
    @SerialName("valAlfa")
    val alphanumericValue: String? = null,

    /** valore della proprietà nel caso di numero */
    @SerialName("valNum")
    val numericValue: Float? = null,

    /** valore della proprietà nel caso di date time */
    @SerialName("valDate")
    val dateValue: String? = null
)

@Serializable
data class Esse3DettaglioErrore(
    /** Http Status Code */
    @SerialName("statusCode")
    val statusCode: Int? = null,

    /** codice di errore */
    @SerialName("retCode")
    val returnCode: Int? = null,

    /** descrizione dell'errore */
    @SerialName("retErrMsg")
    val returnErrorMessage: String? = null,

    @SerialName("errDetails")
    val errDetails: List<Esse3DettaglioErroreAggiuntivo> = emptyList()
)

@Serializable
data class Esse3LinkKey(
    /** contiene la chiave che mette in relazione l'allegato con le altre entità di esse3 */
    @SerialName("nome")
    val name: String? = null,

    /** contiene il valore dell'id */
    @SerialName("valore")
    val value: Long? = null
)

@Serializable
data class Esse3AdditionalPropertyUpload(
    /** nome della proprietà aggiuntiva */
    @SerialName("nome")
    val name: String = "",

    /** valore della proprietà nel caso di stringa */
    @SerialName("valAlfa")
    val alphanumericValue: String? = null,

    /** valore della proprietà nel caso di numero */
    @SerialName("valNum")
    val numericValue: Float? = null,

    /** valore della proprietà nel caso di date time */
    @SerialName("valDate")
    val dateValue: String? = null,

    /** id dell'upload */
    @SerialName("uploadId")
    val uploadId: Long? = null,

    /** id della proprietà aggiuntiva dell' upload */
    @SerialName("dettId")
    val detailId: Long? = null
)
