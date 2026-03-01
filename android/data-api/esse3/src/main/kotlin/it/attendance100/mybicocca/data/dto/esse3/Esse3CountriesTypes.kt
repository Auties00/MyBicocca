package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3Country(
    @SerialName("nazioneId")
    val nationId: Long? = null,

    @SerialName("codFisc")
    val fiscalCode: String? = null,

    @SerialName("cod")
    val code: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("dataInizioVal")
    val evaluationStartDate: String? = null,

    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    @SerialName("codificaPrec")
    val previousEncoding: Long? = null,

    @SerialName("comuCodifFlg")
    val municipalityCodedFlag: Int? = null,

    @SerialName("nazioneCod")
    val nationCode: String? = null,

    @SerialName("csaCod")
    val csaCode: String? = null,

    @SerialName("territorioCedutoFlg")
    val cededTerritoryFlag: Int? = null,

    @SerialName("strIban")
    val ibanString: String? = null,

    @SerialName("prefixInternazionale")
    val internationalPrefix: String? = null,

    @SerialName("equipCorsoBancaItaFlg")
    val italianBankCourseEquivalentFlag: Int? = null,

    @SerialName("codIso31661")
    val iso31661Code: String? = null
)

@Serializable
data class Esse3PostalCode(
    @SerialName("nazioneId")
    val nationId: Long? = null,

    @SerialName("nazioneCodFisc")
    val nationFiscalCode: String? = null,

    @SerialName("regioneId")
    val regionId: Long? = null,

    @SerialName("regioneCod")
    val regionCode: String? = null,

    @SerialName("regioneDes")
    val regionDescription: String? = null,

    @SerialName("sigla")
    val abbreviation: String? = null,

    @SerialName("provinciaCod")
    val provinceCode: String? = null,

    @SerialName("provinciaDes")
    val provinceDescription: String? = null,

    @SerialName("comuneId")
    val municipalityId: Long? = null,

    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    @SerialName("comuCap")
    val municipalityPostalCode: String? = null,

    @SerialName("capId")
    val postalCodeId: Long? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("capDes")
    val postalCodeDescription: String? = null
)

@Serializable
data class Esse3Province(
    @SerialName("nazioneId")
    val nationId: Long? = null,

    @SerialName("nazioneCodFisc")
    val nationFiscalCode: String? = null,

    @SerialName("regioneId")
    val regionId: Long? = null,

    @SerialName("regioneCod")
    val regionCode: String? = null,

    @SerialName("regioneDes")
    val regionDescription: String? = null,

    @SerialName("codRegioVulc")
    val vulcanoRegionCode: String? = null,

    @SerialName("sigla")
    val abbreviation: String? = null,

    @SerialName("provinciaCod")
    val provinceCode: String? = null,

    @SerialName("provinciaDes")
    val provinceDescription: String? = null,

    @SerialName("annoDefinizione")
    val definitionYear: Long? = null,

    @SerialName("provinciaPrecedente")
    val previousProvince: String? = null,

    @SerialName("annoFineValidita")
    val validityEndYear: Long? = null,

    @SerialName("attivoId")
    val activeId: Int? = null
)

@Serializable
data class Esse3Municipality(
    @SerialName("nazioneId")
    val nationId: Long? = null,

    @SerialName("nazioneCodFisc")
    val nationFiscalCode: String? = null,

    @SerialName("regioneId")
    val regionId: Long? = null,

    @SerialName("regioneCod")
    val regionCode: String? = null,

    @SerialName("regioneDes")
    val regionDescription: String? = null,

    @SerialName("sigla")
    val abbreviation: String? = null,

    @SerialName("provinciaCod")
    val provinceCode: String? = null,

    @SerialName("provinciaDes")
    val provinceDescription: String? = null,

    @SerialName("comuneId")
    val municipalityId: Long? = null,

    @SerialName("idComune")
    val municipalityRefId: Long? = null,

    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    @SerialName("codIstat")
    val istatCode: String? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    @SerialName("codIstatMiur")
    val miurIstatCode: String? = null,

    @SerialName("distanzaKm")
    val distanceKm: Long? = null,

    @SerialName("var")
    val variable: String? = null,

    @SerialName("varCod")
    val variableCode: String? = null,

    @SerialName("varSigla")
    val variableAbbreviation: String? = null,

    @SerialName("varDes")
    val variableDescription: String? = null,

    @SerialName("dataCostit")
    val constitutionDate: String? = null,

    @SerialName("newComuneId")
    val newMunicipalityId: Long? = null,

    @SerialName("attivoId")
    val activeId: Int? = null
)
