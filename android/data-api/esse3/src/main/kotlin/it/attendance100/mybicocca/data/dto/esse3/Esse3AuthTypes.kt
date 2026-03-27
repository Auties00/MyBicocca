package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3ChangePasswordResult(
    @SerialName("result")
    val result: String? = null
)

@Serializable
data class Esse3User(
    @SerialName("firstName")
    val firstName: String,

    @SerialName("lastName")
    val lastName: String,

    @SerialName("sex")
    val sex: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    @SerialName("id")
    val id: Long,

    @SerialName("grpId")
    val groupId: Int,

    @SerialName("grpUserTecnicoFlg")
    val technicalUserGroupFlag: Int,

    @SerialName("grpPta")
    val ptaGroup: Int,

    @SerialName("grpDes")
    val groupDescription: String,

    @SerialName("sessionTimeout")
    val sessionTimeout: Int? = null,

    @SerialName("userId")
    val userId: String,

    @SerialName("tipoFirmaId")
    val signatureTypeId: Int? = null,

    @SerialName("tipoFirmaFaId")
    val faSignatureTypeId: Int? = null,

    @SerialName("aliasName")
    val aliasName: String? = null,

    @SerialName("trattiCarriera")
    val careerSegments: List<Esse3CareerSegmentKeys> = emptyList()
)

@Serializable
data class Esse3JWKModel(
    @SerialName("keys")
    val keys: List<Esse3JWKKey> = emptyList()
)

@Serializable
data class Esse3CareerSegmentKeys(
    @SerialName("stuId")
    val studentId: Long,

    @SerialName("matId")
    val matId: Long,

    @SerialName("matricola")
    val matricola: String,

    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String,

    @SerialName("staStuCod")
    val studentStatusCode: String,

    @SerialName("staStuDes")
    val studentStatusDescription: String,

    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    @SerialName("staMatCod")
    val matStatusCode: String,

    @SerialName("staMatDes")
    val matStatusDescription: String,

    @SerialName("dettaglioTratto")
    val segmentDetail: Esse3CareerPortion? = null
)

@Serializable
data class Esse3UserSession(
    @SerialName("user")
    val user: Esse3User,

    @SerialName("authToken")
    val authToken: String,

    @SerialName("internalAuthToken")
    val internalAuthToken: String? = null,

    @SerialName("expPwd")
    val passwordExpiration: Boolean,

    @SerialName("credentials")
    val credentials: Esse3AuthenticationCredentials? = null,

    @SerialName("jwt")
    val jwt: String? = null,

    @SerialName("profili")
    val profiles: List<Esse3UserProfile> = emptyList()
)

@Serializable
data class Esse3ChangeUserPasswordParameters(
    @SerialName("username")
    val username: String,

    @SerialName("oldPassword")
    val oldPassword: String? = null,

    @SerialName("newPassword")
    val newPassword: String
)

@Serializable
data class Esse3UserProfile(
    @SerialName("grpId")
    val groupId: Long? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("userId")
    val userId: String? = null
)

@Serializable
data class Esse3JWKKey(
    @SerialName("kty")
    val kty: String? = null,

    @SerialName("kid")
    val kid: String? = null,

    @SerialName("alg")
    val algorithm: String? = null,

    @SerialName("n")
    val n: String? = null,

    @SerialName("e")
    val e: String? = null
)

@Serializable
data class Esse3SessionLanguage(
    @SerialName("linguaCod")
    val languageCode: String
)

@Serializable
data class Esse3AuthenticationCredentials(
    @SerialName("kind")
    val kind: Esse3Kind,

    @SerialName("profile")
    val profile: Esse3Profile? = null,

    @SerialName("jwtKeyId")
    val jwtKeyId: String? = null,

    @SerialName("user")
    val user: String? = null
)

@Serializable
data class Esse3CareerPortion(
    @SerialName("profCod")
    val professionCode: String? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    @SerialName("staIscrCod")
    val enrollmentStatusCode: String? = null,

    @SerialName("motStaiscrCod")
    val enrollmentStatusReasonCode: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("anniFC")
    val fcYears: Int? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    @SerialName("durataAnni")
    val durationYears: Int? = null,

    @SerialName("ultimoAnnoFlg")
    val lastYearFlag: Int? = null,

    @SerialName("condFlg")
    val conditionFlag: Int? = null,

    @SerialName("domiscrFlg")
    val domicileEnrollmentFlag: Int? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("aaRegId")
    val academicYearRegulationId: Int? = null,

    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("passaggioFlg")
    val transitionFlag: Int? = null,

    @SerialName("notaBloccanteFlg")
    val blockingNoteFlag: Int? = null,

    @SerialName("mobilFlg")
    val mobileFlag: Int? = null,

    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    @SerialName("normId")
    val normId: Long? = null,

    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null
)

@Serializable
data class Esse3CheckLoginResult(
    @SerialName("ok")
    val ok: Boolean? = null,

    @SerialName("changePassword")
    val changePassword: Boolean? = null
)

@Serializable
data class Esse3CacheInfo(
    @SerialName("httpCacheEnable")
    val httpCacheEnable: Int,

    @SerialName("serverCacheEnable")
    val serverCacheEnable: Int
)

@Serializable
data class Esse3JWTModel(
    @SerialName("jwt")
    val jwt: String? = null
)
