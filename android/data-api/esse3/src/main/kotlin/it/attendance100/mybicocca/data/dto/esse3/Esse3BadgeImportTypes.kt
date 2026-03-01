package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3BadgeClass(
    @SerialName("id")
    val id: Long,

    @SerialName("identificativo")
    val identifier: String? = null,

    @SerialName("status")
    val status: String,

    @SerialName("obiname")
    val obiName: String,

    @SerialName("obidescription")
    val obiDescription: String,

    @SerialName("createdAt")
    val createdAt: String,

    @SerialName("updatedAt")
    val updatedAt: String? = null,

    @SerialName("imageUrl")
    val imageUrl: String? = null,

    @SerialName("alignment")
    val alignment: String? = null,

    @SerialName("issuer")
    val issuer: Esse3Issuer,

    @SerialName("customData")
    val customData: List<Esse3CustomData> = emptyList()
)

@Serializable
data class Esse3Issuer(
    @SerialName("idIssuer")
    val issuerId: String,

    @SerialName("miurCode")
    val miurCode: String? = null,

    @SerialName("url")
    val url: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("imageUrl")
    val imageUrl: String? = null,

    @SerialName("slug")
    val slug: String? = null
)

@Serializable
data class Esse3AwardReturn(
    @SerialName("tstId")
    val testId: Long,

    @SerialName("dataCreazione")
    val creationDate: String? = null,

    @SerialName("statoCod")
    val stateCode: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("idBadgeClass")
    val badgeClassId: Long? = null,

    @SerialName("identificativoBadge")
    val badgeIdentifier: String? = null,

    @SerialName("awardInfo")
    val awardInfo: List<Esse3AwardInfo> = emptyList(),

    @SerialName("awardErrorDett")
    val awardErrorDetails: List<Esse3AwardErrorDetail> = emptyList()
)

@Serializable
data class Esse3BadgeClassReturn(
    @SerialName("tstId")
    val testId: Long,

    @SerialName("dataCreazione")
    val creationDate: String? = null,

    @SerialName("statoCod")
    val stateCode: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("badgeClassesInfo")
    val badgeClassesInfo: List<Esse3BadgeClassInfo> = emptyList(),

    @SerialName("badgeClassesErrorDett")
    val badgeClassesErrorDetails: List<Esse3BadgeClassErrorDetail> = emptyList()
)

@Serializable
data class Esse3BadgeClassInfo(
    @SerialName("id")
    val id: Long,

    @SerialName("idNumerico")
    val numericId: Long? = null,

    @SerialName("statoCod")
    val stateCode: String? = null,

    @SerialName("identificativo")
    val identifier: String? = null,

    @SerialName("status")
    val status: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("createdAt")
    val createdAt: String,

    @SerialName("updatedAt")
    val updatedAt: String? = null,

    @SerialName("imageUrl")
    val imageUrl: String? = null,

    @SerialName("idIssuer")
    val issuerId: String? = null,

    @SerialName("codMiurIssuer")
    val miurIssuerCode: String? = null,

    @SerialName("urlIssuer")
    val issuerUrl: String? = null,

    @SerialName("emailIssuer")
    val issuerEmail: String? = null,

    @SerialName("nomeIssuer")
    val issuerName: String? = null,

    @SerialName("imageUrlIssuer")
    val issuerImageUrl: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("slugIssuer")
    val issuerSlug: String? = null,

    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    @SerialName("livelloCod")
    val levelCode: String? = null,

    @SerialName("livelloDes")
    val levelDescription: String? = null
)

@Serializable
data class Esse3BadgeClassErrorDetail(
    @SerialName("id")
    val id: Long,

    @SerialName("idNumerico")
    val numericId: Long? = null,

    @SerialName("statoCod")
    val stateCode: String? = null,

    @SerialName("identificativo")
    val identifier: String? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null,

    @SerialName("imageUrl")
    val imageUrl: String? = null,

    @SerialName("idIssuer")
    val issuerId: String? = null,

    @SerialName("codMiurIssuer")
    val miurIssuerCode: String? = null,

    @SerialName("urlIssuer")
    val issuerUrl: String? = null,

    @SerialName("emailIssuer")
    val issuerEmail: String? = null,

    @SerialName("nomeIssuer")
    val issuerName: String? = null,

    @SerialName("imageUrlIssuer")
    val issuerImageUrl: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("slugIssuer")
    val issuerSlug: String? = null
)

@Serializable
data class Esse3BadgeIssuanceNotification(
    @SerialName("evento")
    val event: String,

    @SerialName("refId")
    val referenceId: Long,

    @SerialName("badgeId")
    val badgeId: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("dataEmissione")
    val issuanceDate: String? = null,

    @SerialName("causale")
    val reason: String? = null
)

@Serializable
data class Esse3AwardErrorDetail(
    @SerialName("id")
    val id: Long,

    @SerialName("statoCod")
    val stateCode: String,

    @SerialName("awardedUser")
    val awardedUser: Long? = null,

    @SerialName("earnedBadge")
    val earnedBadge: Long? = null,

    @SerialName("awardedEmail")
    val awardedEmail: String,

    @SerialName("createdAt")
    val createdAt: String,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null
)

@Serializable
data class Esse3Award(
    @SerialName("awardedUser")
    val awardedUser: Long? = null,

    @SerialName("earnedBadge")
    val earnedBadge: Long,

    @SerialName("awardedEmail")
    val awardedEmail: String,

    @SerialName("createdAt")
    val createdAt: String,

    @SerialName("customData")
    val customData: List<Esse3CustomData> = emptyList()
)

@Serializable
data class Esse3CustomData(
    @SerialName("externalKey")
    val externalKey: String,

    @SerialName("externalValue")
    val externalValue: String
)

@Serializable
data class Esse3AwardInfo(
    @SerialName("id")
    val id: Long,

    @SerialName("statoCod")
    val stateCode: String? = null,

    @SerialName("awardedUser")
    val awardedUser: Long? = null,

    @SerialName("earnedBadge")
    val earnedBadge: Long,

    @SerialName("awardedEmail")
    val awardedEmail: String,

    @SerialName("createdAt")
    val createdAt: String,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("aaConsegTitolo")
    val academicYearTitleAward: Long? = null,

    @SerialName("dataConseguimentoTitolo")
    val titleAchievementDate: String? = null,

    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    @SerialName("tititCod")
    val titleTypeCode: String? = null,

    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    @SerialName("stessoAteneoFlg")
    val sameUniversityFlag: Long? = null,

    @SerialName("staTitItCod")
    val italianTitleStatusCode: String? = null,

    @SerialName("dataScadenza")
    val expirationDate: String? = null
)
