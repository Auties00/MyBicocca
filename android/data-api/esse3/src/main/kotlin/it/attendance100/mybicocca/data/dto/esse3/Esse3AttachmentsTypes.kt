package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3AttachmentType(
    @SerialName("typeId")
    val typeId: Int? = null,

    @SerialName("typeCod")
    val typeCode: String? = null,

    @SerialName("downloadSupported")
    val downloadSupported: Int? = null,

    @SerialName("uploadSupported")
    val uploadSupported: Int? = null
)

@Serializable
data class Esse3AttachmentMetadata(
    @SerialName("filename")
    val fileName: String? = null,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("descrizione")
    val description: String? = null,

    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    @SerialName("validoFlg")
    val validFlag: Long? = null,

    @SerialName("tipoAllegato")
    val attachmentType: String? = null,

    @SerialName("allegatoId")
    val attachmentId: Long? = null
)

@Serializable
data class Esse3DettaglioErroreAggiuntivo(
    @SerialName("errorType")
    val errorType: String? = null,

    @SerialName("value")
    val value: String? = null,

    @SerialName("rawValue")
    val rawValue: String? = null
)

@Serializable
data class Esse3AttachmentTypeCode(
    @SerialName("linguaId")
    val languageId: Long? = null,

    @SerialName("linguaCod")
    val languageCode: String? = null,

    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("maxUploadFileSize")
    val maxUploadFileSize: Long? = null,

    @SerialName("abilVisNoReg")
    val noRegistrationVisibility: Int? = null,

    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3AttachmentExtension(
    @SerialName("estensioneId")
    val extensionId: Long? = null,

    @SerialName("estensione")
    val extension: String? = null,

    @SerialName("estensioneDes")
    val extensionDescription: String? = null
)

@Serializable
data class Esse3GenericAttachmentInsertMetadata(
    @SerialName("proprietaAggiuntive")
    val additionalProperties: List<Esse3AdditionalProperty> = emptyList(),

    @SerialName("chiaviCollegamento")
    val linkKeys: List<Esse3LinkKey> = emptyList(),

    @SerialName("filename")
    val fileName: String? = null,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("descrizione")
    val description: String? = null,

    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    @SerialName("validoFlg")
    val validFlag: Long? = null
)

@Serializable
data class Esse3PatchAttachmentMetadata(
    @SerialName("validoFlg")
    val validFlag: Long? = null
)

@Serializable
data class Esse3UploadMetadata(
    @SerialName("statoUpload")
    val uploadState: String? = null,

    @SerialName("chiaviCollegamento")
    val linkKeys: List<Esse3LinkKey> = emptyList(),

    @SerialName("proprietaAggiuntive")
    val additionalProperties: List<Esse3AdditionalPropertyUpload> = emptyList(),

    @SerialName("validoFlg")
    val validFlag: Long? = null,

    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    @SerialName("descrizione")
    val description: String? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("filename")
    val fileName: String? = null,

    @SerialName("tipoAllegato")
    val attachmentType: String? = null,

    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    @SerialName("uploadId")
    val uploadId: Long? = null
)

@Serializable
data class Esse3AdditionalProperty(
    @SerialName("nome")
    val name: String,

    @SerialName("valAlfa")
    val alphanumericValue: String? = null,

    @SerialName("valNum")
    val numericValue: Float? = null,

    @SerialName("valDate")
    val dateValue: String? = null
)

@Serializable
data class Esse3DettaglioErrore(
    @SerialName("statusCode")
    val statusCode: Int? = null,

    @SerialName("retCode")
    val returnCode: Int? = null,

    @SerialName("retErrMsg")
    val returnErrorMessage: String? = null,

    @SerialName("errDetails")
    val errDetails: List<Esse3DettaglioErroreAggiuntivo> = emptyList()
)

@Serializable
data class Esse3LinkKey(
    @SerialName("nome")
    val name: String? = null,

    @SerialName("valore")
    val value: Long? = null
)

@Serializable
data class Esse3AdditionalPropertyUpload(
    @SerialName("nome")
    val name: String,

    @SerialName("valAlfa")
    val alphanumericValue: String? = null,

    @SerialName("valNum")
    val numericValue: Float? = null,

    @SerialName("valDate")
    val dateValue: String? = null,

    @SerialName("uploadId")
    val uploadId: Long? = null,

    @SerialName("dettId")
    val detailId: Long? = null
)
