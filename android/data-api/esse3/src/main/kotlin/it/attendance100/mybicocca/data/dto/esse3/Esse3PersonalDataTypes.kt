package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3GetEnrollmentNumberAttachmentMetadata(
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    @SerialName("dimensione")
    val size: Long? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("filename")
    val fileName: String? = null,

    @SerialName("estensione")
    val extension: String? = null,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int? = null,

    @SerialName("abilVisWeb")
    val webVisibility: Int? = null,

    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3AcademicYearLookup(
    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("dataInizio")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null
)

@Serializable
data class Esse3ExternalSubjectsConsentsReplica(
    @SerialName("soggEstId")
    val externalSubjectId: Int? = null,

    @SerialName("tipoConsensoCod")
    val consentTypeCode: String? = null,

    @SerialName("tipoConsensoDes")
    val consentTypeDescription: String? = null,

    @SerialName("tipiConsensoEtichetta")
    val consentTypesLabel: String? = null,

    @SerialName("consensoFlg")
    val consentFlag: Int? = null,

    @SerialName("dataIni")
    val startDate: String? = null,

    @SerialName("procAmmCod")
    val administrativeProcedureCode: String? = null,

    @SerialName("procAmmDes")
    val administrativeProcedureDescription: String? = null,

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
data class Esse3PersonalDocument(
    @SerialName("docPersId")
    val personalDocumentId: Long? = null,

    @SerialName("docIdentTipoCod")
    val identityDocumentTypeCode: String? = null,

    @SerialName("docIdentTipoDes")
    val identityDocumentTypeDescription: String? = null,

    @SerialName("num")
    val number: String? = null,

    @SerialName("dataRilascio")
    val releaseDate: String? = null,

    @SerialName("dataScadenza")
    val expirationDate: String? = null,

    @SerialName("enteRilascio")
    val issuingEntity: String? = null,

    @SerialName("statoDocPers")
    val personalDocumentState: String? = null,

    @SerialName("statoDocPersDes")
    val personalDocumentStateDescription: String? = null,

    @SerialName("nazioneEmissioneId")
    val issuanceNationId: Long? = null,

    @SerialName("nazioneEmissioneCodFis")
    val issuanceNationFiscalCode: String? = null,

    @SerialName("nazioneEmissioneDes")
    val issuanceNationDescription: String? = null,

    @SerialName("nazioneEmissioneNazioneCod")
    val issuanceNationCode: String? = null,

    @SerialName("nazioneEmissioneCodInt")
    val issuanceNationInternationalCode: String? = null,

    @SerialName("comuneEmissioneId")
    val issuanceMunicipalityId: Long? = null,

    @SerialName("comuneEmissioneCod")
    val issuanceMunicipalityCode: String? = null,

    @SerialName("comuneEmissioneCodCatastale")
    val issuanceMunicipalityCadastralCode: String? = null,

    @SerialName("comuneEmissioneIstatMiur")
    val issuanceMunicipalityMiurIstat: String? = null,

    @SerialName("comuneEmissioneDes")
    val issuanceMunicipalityDescription: String? = null,

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
data class Esse3PersonCommonRegistry(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    @SerialName("persCod")
    val personCode: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("patronimico")
    val patronymic: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("comuNascId")
    val birthMunicipalityId: Long? = null,

    @SerialName("comuNascCod")
    val birthMunicipalityCode: String? = null,

    @SerialName("comuNascCodCatastale")
    val birthMunicipalityCadastralCode: String? = null,

    @SerialName("comuNascCodIstatMiur")
    val birthMunicipalityMiurIstatCode: String? = null,

    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    @SerialName("naziNascId")
    val birthNationId: Long? = null,

    @SerialName("naziNascCod")
    val birthNationCode: String? = null,

    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    @SerialName("provNascDes")
    val birthProvinceDescription: String? = null,

    @SerialName("naziNascNazioneCod")
    val birthCountryCode: String? = null,

    @SerialName("naziNascDes")
    val birthNationDescription: String? = null,

    @SerialName("naziNascCodInt")
    val birthNationInternationalCode: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Int? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("fotoId")
    val photoId: Long? = null,

    @SerialName("naziResCod")
    val residenceNationCode: String? = null,

    @SerialName("naziResDes")
    val residenceNationDescription: String? = null,

    @SerialName("comuResCod")
    val residenceMunicipalityCode: String? = null,

    @SerialName("comuResDes")
    val residenceMunicipalityDescription: String? = null,

    @SerialName("comuResSigla")
    val residenceMunicipalityAbbreviation: String? = null,

    @SerialName("viaRes")
    val residenceStreet: String? = null,

    @SerialName("numCivRes")
    val residenceStreetNumber: String? = null,

    @SerialName("capRes")
    val residencePostalCode: String? = null,

    @SerialName("telRes")
    val residencePhone: String? = null,

    @SerialName("naziDomCod")
    val domicileNationCode: String? = null,

    @SerialName("naziDomDes")
    val domicileNationDescription: String? = null,

    @SerialName("comuDomCod")
    val domicileMunicipalityCode: String? = null,

    @SerialName("comuDomDes")
    val domicileMunicipalityDescription: String? = null,

    @SerialName("comuDomSigla")
    val domicileMunicipalityAbbreviation: String? = null,

    @SerialName("viaDom")
    val domicileStreet: String? = null,

    @SerialName("numCivDom")
    val domicileStreetNumber: String? = null,

    @SerialName("capDom")
    val domicilePostalCode: String? = null,

    @SerialName("telDom")
    val domicilePhone: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailAte")
    val ateEmail: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("codCittadinanza")
    val citizenshipCode: String? = null,

    @SerialName("desCittadinanza")
    val citizenshipDescription: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("permsogScadutoFlg")
    val authorizedSubjectExpiredFlag: Long? = null,

    @SerialName("presenzaPermSogFlg")
    val authorizedSubjectPresenceFlag: Long? = null,

    @SerialName("permsogDataScad")
    val authorizedSubjectExpirationDate: String? = null,

    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    @SerialName("naziResId")
    val residenceNationId: Long? = null,

    @SerialName("naziResNazioneCod")
    val residenceCountryCode: String? = null,

    @SerialName("naziResCodInt")
    val residenceNationInternationalCode: String? = null,

    @SerialName("comuResId")
    val residenceMunicipalityId: Long? = null,

    @SerialName("comuResCodCatastale")
    val residenceMunicipalityCadastralCode: String? = null,

    @SerialName("comuResCodIstatMiur")
    val residenceMunicipalityMiurIstatCode: String? = null,

    @SerialName("provResDes")
    val residenceProvinceDescription: String? = null,

    @SerialName("citstraRes")
    val residenceForeignCity: String? = null,

    @SerialName("frazRes")
    val residenceFraction: String? = null,

    @SerialName("prefixInternazRes")
    val residenceInternationalPrefix: String? = null,

    @SerialName("dataIniValRes")
    val residenceEvaluationStartDate: String? = null,

    @SerialName("domComeResFlg")
    val domicileSameAsResidenceFlag: Int? = null,

    @SerialName("naziDomId")
    val domicileNationId: Int? = null,

    @SerialName("naziDomNazioneCod")
    val domicileCountryCode: String? = null,

    @SerialName("naziDomCodInt")
    val domicileNationInternationalCode: String? = null,

    @SerialName("comuDomId")
    val domicileMunicipalityId: Long? = null,

    @SerialName("comuDomCodCatastale")
    val domicileMunicipalityCadastralCode: String? = null,

    @SerialName("comuDomCodIstatMiur")
    val domicileMunicipalityMiurIstatCode: String? = null,

    @SerialName("citstraDom")
    val domicileForeignCity: String? = null,

    @SerialName("frazDom")
    val domicileFraction: String? = null,

    @SerialName("prefixInternazDom")
    val domicileInternationalPrefix: String? = null,

    @SerialName("cO")
    val co: String? = null,

    @SerialName("dataIniValDom")
    val domicileEvaluationStartDate: String? = null,

    @SerialName("tipoIndirizCod")
    val addressTypeCode: String? = null,

    @SerialName("recapitoTasse")
    val taxesContact: String? = null,

    @SerialName("recapitoBadge")
    val badgeContact: String? = null,

    @SerialName("fax")
    val fax: String? = null,

    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    @SerialName("citt1NazioneCod")
    val citizenship1CountryCode: String? = null,

    @SerialName("citt1Dataini")
    val citizenship1StartDate: String? = null,

    @SerialName("citt1Datafin")
    val citizenship1EndDate: String? = null,

    @SerialName("citt2Cod")
    val citizenship2Code: String? = null,

    @SerialName("citt2Des")
    val citizenship2Description: String? = null,

    @SerialName("citt2NazioneCod")
    val citizenship2CountryCode: String? = null,

    @SerialName("citt2Dataini")
    val citizenship2StartDate: String? = null,

    @SerialName("citt2Datafin")
    val citizenship2EndDate: String? = null,

    @SerialName("citt3Cod")
    val citizenship3Code: String? = null,

    @SerialName("citt3Des")
    val citizenship3Description: String? = null,

    @SerialName("citt3NazioneCod")
    val citizenship3CountryCode: String? = null,

    @SerialName("citt3Dataini")
    val citizenship3StartDate: String? = null,

    @SerialName("citt3Datafin")
    val citizenship3EndDate: String? = null,

    @SerialName("naziCittadCodInt")
    val citizenshipNationInternationalCode: String? = null,

    @SerialName("naziCittadDes")
    val citizenshipNationDescription: String? = null,

    @SerialName("prefixCell")
    val mobilePrefix: String? = null,

    @SerialName("consDpFlg")
    val consentDpFlag: Int? = null,

    @SerialName("consDiffDpFlg")
    val consentDifferentDpFlag: Int? = null,

    @SerialName("consSmsFlg")
    val consentSmsFlag: Int? = null,

    @SerialName("consComunicDpFlg")
    val consentCommunicationDpFlag: Int? = null,

    @SerialName("consComunicErFlg")
    val consentCommunicationErFlag: Int? = null,

    @SerialName("religiosoFlg")
    val religiousFlag: Int? = null,

    @SerialName("decedutoFlg")
    val deceasedFlag: Int? = null,

    @SerialName("extPersCod")
    val externalPersonCode: String? = null,

    @SerialName("notaPers")
    val personalNote: String? = null,

    @SerialName("professione")
    val profession: String? = null,

    @SerialName("statoCivileCod")
    val maritalStatusCode: String? = null,

    @SerialName("statoCivileDes")
    val maritalStatusDescription: String? = null,

    @SerialName("emergNome")
    val emergencyName: String? = null,

    @SerialName("emergCognome")
    val emergencySurname: String? = null,

    @SerialName("emergTel")
    val emergencyPhone: String? = null,

    @SerialName("emergPrefixInternaz")
    val emergencyInternationalPrefix: String? = null,

    @SerialName("emergEmail")
    val emergencyEmail: String? = null,

    @SerialName("emergRapporto")
    val emergencyRelationship: String? = null,

    @SerialName("anaperCodExt")
    val personExternalCode: List<Esse3PersonalDataExternalCode> = emptyList(),

    @SerialName("anaperStorico")
    val personHistory: List<Esse3PersonalDataHistory> = emptyList(),

    @SerialName("anaperConsensi")
    val personConsents: List<Esse3PersonalDataConsents> = emptyList(),

    @SerialName("anaperContatti")
    val personContacts: List<Esse3PersonalDataContacts> = emptyList(),

    @SerialName("datiBancari")
    val bankDetails: List<Esse3PersonalDataBankDetails> = emptyList(),

    @SerialName("docPers")
    val personalDocument: List<Esse3PersonalDocument> = emptyList(),

    @SerialName("dicHand")
    val handicapDeclaration: List<Esse3HandicapDeclarationReplica> = emptyList(),

    @SerialName("permSog")
    val authorizedSubject: List<Esse3SubjectPermission> = emptyList(),

    @SerialName("indirizzi")
    val addresses: List<Esse3PersonalDataAddresses> = emptyList(),

    @SerialName("autorizzati")
    val authorized: List<Esse3Authorized> = emptyList(),

    @SerialName("autorizzazioni")
    val authorizations: List<Esse3Authorizations> = emptyList(),

    @SerialName("tutori")
    val tutors: List<Esse3Tutors> = emptyList(),

    @SerialName("matur")
    val highSchoolGraduation: List<Esse3HighSchoolDiploma> = emptyList(),

    @SerialName("titIt")
    val italianTitle: List<Esse3AllItalianTitles> = emptyList(),

    @SerialName("titStra")
    val foreignTitle: List<Esse3ForeignTitle> = emptyList()
)

@Serializable
data class Esse3HandicapDeclaration(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("dichiarId")
    val declarationId: Long? = null,

    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    @SerialName("tipiHandicapDes")
    val handicapTypesDescription: String? = null,

    @SerialName("percHand")
    val handicapPercentage: Int? = null,

    @SerialName("dataDichiar")
    val declarationDate: String? = null,

    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    @SerialName("statiDicHandDes")
    val handicapDeclarationStatesDescription: String? = null,

    @SerialName("dataIniStato")
    val stateStartDate: String? = null,

    @SerialName("tutoratoFlg")
    val tutoringFlag: Int? = null,

    @SerialName("autTutorFlg")
    val tutorAuthorizationFlag: Int? = null,

    @SerialName("dataIni")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("aaIdCompIni")
    val academicYearComponentStartId: Long? = null,

    @SerialName("aaIdCompFine")
    val academicYearComponentEndId: Long? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("consDsFlg")
    val consentDsFlag: Int? = null,

    @SerialName("handNormativaCod")
    val handicapRegulationCode: String? = null,

    @SerialName("p01HandNormativaDes")
    val p01HandicapRegulationDescription: String? = null,

    @SerialName("besCheckFlg")
    val besCheckFlag: Int? = null,

    @SerialName("dicHandId")
    val handicapDeclarationId: Int? = null,

    @SerialName("misureComp")
    val compensatoryMeasures: Int? = null
)

@Serializable
data class Esse3PhoneParameters(
    @SerialName("numTelefono")
    val phoneNumber: String? = null,

    @SerialName("prefix")
    val prefix: String? = null
)

@Serializable
data class Esse3HandicapDeclarationToValidate(
    @SerialName("userId")
    val userId: String? = null,

    @SerialName("id")
    val id: Int? = null,

    @SerialName("dicHandId")
    val handicapDeclarationId: Int? = null,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    @SerialName("tipoHandicapDes")
    val handicapTypeDescription: String? = null,

    @SerialName("percHand")
    val handicapPercentage: Int? = null,

    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    @SerialName("statoDicHandDes")
    val handicapDeclarationStateDescription: String? = null,

    @SerialName("dataIni")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("aaIdCompIni")
    val academicYearComponentStartId: Long? = null,

    @SerialName("aaIdCompFine")
    val academicYearComponentEndId: Long? = null,

    @SerialName("dataDichiar")
    val declarationDate: String? = null,

    @SerialName("autTutorFlg")
    val tutorAuthorizationFlag: Int? = null,

    @SerialName("tutoratoFlg")
    val tutoringFlag: Int? = null,

    @SerialName("allegati")
    val attachments: Int? = null,

    @SerialName("misureComp")
    val compensatoryMeasures: Int? = null,

    @SerialName("bes")
    val bes: Int? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    @SerialName("abilFlg")
    val authorizationFlag: Int? = null
)

@Serializable
data class Esse3HighSchoolGradeRange(
    @SerialName("annoDa")
    val yearFrom: Int? = null,

    @SerialName("annoA")
    val yearTo: Int? = null,

    @SerialName("votoMin")
    val minGrade: Int? = null,

    @SerialName("votoMax")
    val maxGrade: Int? = null,

    @SerialName("lodeFlg")
    val cumLaudeFlag: Int? = null
)

@Serializable
data class Esse3AttachmentMetadataInfo(
    @SerialName("filename")
    val fileName: String,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("titolo")
    val title: String,

    @SerialName("descrizione")
    val description: String,

    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int
)

@Serializable
data class Esse3MobileParameter(
    @SerialName("cellulare")
    val mobilePhone: String? = null
)

@Serializable
data class Esse3ExtendedPerson(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("fotoId")
    val photoId: Long? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailAte")
    val ateEmail: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("cognomeNormalizzato")
    val normalizedSurname: String? = null,

    @SerialName("nomeNormalizzato")
    val normalizedName: String? = null
)

@Serializable
data class Esse3ForeignTitlesEnrollment(
    @SerialName("dataConsegTitolo")
    val titleDeliveryDate: String? = null,

    @SerialName("tipoTitstCod")
    val titleStatusTypeCode: String? = null,

    @SerialName("ateneoStranieroErasmusCod")
    val foreignUniversityErasmusCode: String? = null,

    @SerialName("cdsStraniero")
    val foreignCourseOfStudy: String? = null,

    @SerialName("durataAnni")
    val durationYears: Long? = null,

    @SerialName("voto")
    val grade: Float? = null,

    @SerialName("votoBase")
    val baseGrade: Long? = null,

    @SerialName("lode")
    val cumLaude: Long? = null,

    @SerialName("votoAlfanumerico")
    val alphanumericGrade: String? = null,

    @SerialName("dichiarazValoreFlg")
    val valueDeclarationFlag: Long? = null,

    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    @SerialName("appellativoF")
    val femaleTitle: String? = null,

    @SerialName("appellativoM")
    val maleTitle: String? = null,

    @SerialName("desTitolo")
    val titleDescription: String? = null,

    @SerialName("staTitStraCod")
    val foreignTitleStatusCode: String,

    @SerialName("naziCodFis")
    val nationFiscalCode: String? = null,

    @SerialName("aaConsegId")
    val academicYearAwardId: Long? = null,

    @SerialName("citstraConseg")
    val deliveryForeignCity: String? = null,

    @SerialName("votoMin")
    val minGrade: Float? = null,

    @SerialName("votoConvertito")
    val convertedGrade: Float? = null,

    @SerialName("votoMinConvertito")
    val convertedMinGrade: Float? = null,

    @SerialName("votoBaseConvertito")
    val convertedBaseGrade: Float? = null,

    @SerialName("tipoDicValCod")
    val valueDeclarationTypeCode: String? = null,

    @SerialName("naziOrdCodFis")
    val orderNationFiscalCode: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("desAteneo")
    val universityDescription: String? = null
)

@Serializable
data class Esse3HigherSchoolTitleType(
    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    @SerialName("tipologiaCod")
    val typologyCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    @SerialName("idDiploma")
    val diplomaId: Int? = null,

    @SerialName("almaCod")
    val almaCode: Int? = null,

    @SerialName("annoIntFlg")
    val integrationYearFlag: Int? = null,

    @SerialName("abilVisFlg")
    val visibilityFlag: Int? = null,

    @SerialName("miurDes")
    val miurDescription: String? = null,

    @SerialName("tipoScuolaCod")
    val schoolTypeCode: String? = null,

    @SerialName("idTipoIst")
    val institutionTypeId: Int? = null,

    @SerialName("descTipo")
    val typeDescription: String? = null,

    @SerialName("idMacroTipo")
    val macroTypeId: Int? = null,

    @SerialName("descMacroTipo")
    val macroTypeDescription: String? = null,

    @SerialName("desEng")
    val descriptionEnglish: String? = null
)

@Serializable
data class Esse3ExternalEntitiesReplica(
    @SerialName("soggEstId")
    val externalSubjectId: Int? = null,

    @SerialName("enteId")
    val entityId: Int? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("cod")
    val code: String? = null,

    @SerialName("direttore")
    val director: String? = null,

    @SerialName("tipoEnteCod")
    val entityTypeCode: String? = null,

    @SerialName("tipoEnteDes")
    val entityTypeDescription: String? = null,

    @SerialName("settEnteCod")
    val entitySectorCode: String? = null,

    @SerialName("settEnteDes")
    val entitySectorDescription: String? = null,

    @SerialName("privatoFlg")
    val privateFlag: Int? = null,

    @SerialName("link")
    val link: String? = null,

    @SerialName("sdrId")
    val siteId: Int? = null,

    @SerialName("strutSdrCod")
    val structureSiteCode: String? = null,

    @SerialName("strutSdrDes")
    val structureSiteDescription: String? = null,

    @SerialName("strutSdrTip")
    val structureSiteType: String? = null,

    @SerialName("tipiSdrDes")
    val siteTypesDescription: String? = null,

    @SerialName("statoEnteCod")
    val entityStateCode: String? = null,

    @SerialName("statoEnteDes")
    val entityStateDescription: String? = null,

    @SerialName("fasciaDipCod")
    val departmentBandCode: String? = null,

    @SerialName("fasciaDipDes")
    val departmentBandDescription: String? = null,

    @SerialName("desAtestra")
    val foreignTestDescription: String? = null,

    @SerialName("autPrivacyFlg")
    val privacyAuthorizationFlag: Int? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("gruppoAppart")
    val belongingGroup: String? = null,

    @SerialName("codiceAssociativo")
    val associativeCode: String? = null,

    @SerialName("fatturato")
    val invoiced: String? = null,

    @SerialName("settAtecoId")
    val atecoSectorId: Int? = null,

    @SerialName("settAtecoCod")
    val atecoSectorCode: String? = null,

    @SerialName("settAtecoDes")
    val atecoSectorDescription: String? = null,

    @SerialName("profiloAziId")
    val companyProfileId: Int? = null,

    @SerialName("profiloAziDes")
    val companyProfileDescription: String? = null,

    @SerialName("codAtecoId")
    val atecoCodeId: Int? = null,

    @SerialName("codAtecoDes")
    val atecoCodeDescription: String? = null,

    @SerialName("duns")
    val duns: String? = null,

    @SerialName("genOppEvidFlg")
    val generateOpportunityEvidenceFlag: Int? = null,

    @SerialName("crmCod")
    val crmCode: String? = null,

    @SerialName("associazioneInprenditoriale")
    val businessAssociation: String? = null,

    @SerialName("crmSyncFlg")
    val crmSyncFlag: Int? = null,

    @SerialName("regAziId")
    val companyRegistrationId: Int? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("prodotti")
    val products: String? = null,

    @SerialName("lingueLavoro")
    val workLanguages: String? = null,

    @SerialName("lingueLavoroGruppo")
    val workLanguagesGroup: String? = null,

    @SerialName("notaAzi")
    val companyNote: String? = null,

    @SerialName("responsabileProtdatiEmail")
    val dataProtectionResponsibleEmail: String? = null,

    @SerialName("sedi")
    val sites: List<Esse3ExternalEntitiesLocationsReplica> = emptyList()
)

@Serializable
data class Esse3PersonPhotoAttachmentMetadata(
    @SerialName("filename")
    val fileName: String,

    @SerialName("fotoValidata")
    val validatedPhoto: Int? = null
)

@Serializable
data class Esse3PersonalDataConsents(
    @SerialName("tipoConsensoCod")
    val consentTypeCode: String? = null,

    @SerialName("tipiConsensoDes")
    val consentTypesDescription: String? = null,

    @SerialName("tipiConsensoEtichetta")
    val consentTypesLabel: String? = null,

    @SerialName("consensoFlg")
    val consentFlag: Int? = null,

    @SerialName("dataIni")
    val startDate: String? = null,

    @SerialName("procAmmCod")
    val administrativeProcedureCode: String? = null,

    @SerialName("procAmmDes")
    val administrativeProcedureDescription: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("storicoConsensi")
    val consentHistory: List<Esse3PersonalDataConsentsHistory> = emptyList()
)

@Serializable
data class Esse3ForeignTitleType(
    @SerialName("tipoTitst_cod")
    val titleStatusTypeCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("livelloCod")
    val levelCode: String? = null,

    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    @SerialName("visTrovacvFlg")
    val cvSearchVisibleFlag: Int? = null,

    @SerialName("extCod")
    val externalCode: String? = null
)

@Serializable
data class Esse3CareerClosureParameters(
    @SerialName("motStastuCod")
    val statusReasonCode: String,

    @SerialName("dataChiusura")
    val closingDate: String
)

@Serializable
data class Esse3PhDProgramDirector(
    @SerialName("respNome")
    val responsibleName: String? = null,

    @SerialName("respCognome")
    val responsibleSurname: String? = null,

    @SerialName("respCodFis")
    val responsibleFiscalCode: String? = null,

    @SerialName("respDataNascita")
    val responsibleBirthDate: String? = null,

    @SerialName("respLuogoNascita")
    val responsibleBirthPlace: String? = null,

    @SerialName("respMatricola")
    val responsibleMatricola: String? = null,

    @SerialName("respIdAb")
    val responsibleAbbreviatedId: String? = null,

    @SerialName("respDesCarica")
    val responsiblePositionDescription: String? = null
)

@Serializable
data class Esse3PostCompensatoryMeasuresHandicapDeclarationParameters(
    @SerialName("statoMisuraComp")
    val compensatoryMeasureState: String? = null,

    @SerialName("misuraDataIni")
    val measureStartDate: String? = null,

    @SerialName("misuraDataFine")
    val measureEndDate: String? = null,

    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String? = null,

    @SerialName("desLiberaMisura")
    val freeMeasureDescription: String? = null
)

@Serializable
data class Esse3CareerParameters(
    @SerialName("numProtocollo")
    val protocolNumber: String
)

@Serializable
data class Esse3AcademicYearRegistrationHandicapDeclaration(
    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3AuthorizedPersonalDocument(
    @SerialName("autDocPersId")
    val personalDataDocAuthorizationId: Long? = null,

    @SerialName("autorizzatoId")
    val authorizedId: Long? = null,

    @SerialName("docIdentTipoCod")
    val identityDocumentTypeCode: String? = null,

    @SerialName("docIdentTipoDes")
    val identityDocumentTypeDescription: String? = null,

    @SerialName("num")
    val number: String? = null,

    @SerialName("dataRilascio")
    val releaseDate: String? = null,

    @SerialName("dataScadenza")
    val expirationDate: String? = null,

    @SerialName("enteRilascio")
    val issuingEntity: String? = null,

    @SerialName("statoDocPers")
    val personalDocumentState: String? = null,

    @SerialName("nazioneEmissioneId")
    val issuanceNationId: Int? = null,

    @SerialName("nazioneEmissioneCodFis")
    val issuanceNationFiscalCode: String? = null,

    @SerialName("nazioneEmissioneDes")
    val issuanceNationDescription: String? = null,

    @SerialName("comuneEmissioneId")
    val issuanceMunicipalityId: Long? = null,

    @SerialName("comuneEmissioneCodFis")
    val issuanceMunicipalityFiscalCode: String? = null,

    @SerialName("comuneEmissioneDes")
    val issuanceMunicipalityDescription: String? = null,

    @SerialName("citstraEmissione")
    val issuanceForeignCity: String? = null
)

@Serializable
data class Esse3BulkDownloadBody(
    @SerialName("emails")
    val emails: List<String> = emptyList(),

    @SerialName("dicHandIds")
    val handicapDeclarationIds: List<Long> = emptyList(),

    @SerialName("description")
    val description: String,

    @SerialName("zipFileName")
    val zipFileName: String
)

@Serializable
data class Esse3Suspensions(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    @SerialName("stuStaStuId")
    val studentStatusStudentId: Long? = null,

    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    @SerialName("staStuDes")
    val studentStatusDescription: String? = null,

    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    @SerialName("aaInizioId")
    val academicYearStartId: String? = null,

    @SerialName("aaFineId")
    val academicYearEndId: String? = null,

    @SerialName("dataIniSosp")
    val suspensionStartDate: String? = null,

    @SerialName("dataFineSosp")
    val suspensionEndDate: String? = null
)

@Serializable
data class Esse3HandicapDeclarationStatesLookup(
    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("abilVisCommissioneFlg")
    val commissionVisibilityFlag: Long? = null
)

@Serializable
data class Esse3ForeignTitleValidationDeclaration(
    @SerialName("tipoDicValCod")
    val valueDeclarationTypeCode: String? = null,

    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3PersonalDataBankDetails(
    @SerialName("tipoDatiBancaCod")
    val bankDataTypeCode: String? = null,

    @SerialName("tipiDatiBancaDes")
    val bankDataTypesDescription: String? = null,

    @SerialName("tipoRimbPagCod")
    val paymentRefundTypeCode: String? = null,

    @SerialName("tipiRimbPagDes")
    val paymentRefundTypesDescription: String? = null,

    @SerialName("bancaDes")
    val bankDescription: String? = null,

    @SerialName("ccIntestatario")
    val currentAccountHolder: String? = null,

    @SerialName("ccIntestatarioCf")
    val currentAccountHolderFiscalCode: String? = null,

    @SerialName("ibanCod")
    val ibanCode: String? = null,

    @SerialName("nConto")
    val accountNumber: String? = null,

    @SerialName("nazioneId")
    val nationId: Long? = null,

    @SerialName("naziCod")
    val nationCode: String? = null,

    @SerialName("naziCodFis")
    val nationFiscalCode: String? = null,

    @SerialName("naziDes")
    val nationDescription: String? = null,

    @SerialName("swiftCod")
    val swiftCode: String? = null,

    @SerialName("modaccDati")
    val dataAccessMode: Long? = null,

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
data class Esse3ForeignTitle(
    @SerialName("titStraId")
    val foreignTitleId: Long? = null,

    @SerialName("tipoTitstraDes")
    val foreignTitleTypeDescription: String? = null,

    @SerialName("aaConsegId")
    val academicYearAwardId: Int? = null,

    @SerialName("dataConsegTitolo")
    val titleDeliveryDate: String? = null,

    @SerialName("ateneoStranieroId")
    val foreignUniversityId: Long? = null,

    @SerialName("atestraCod")
    val foreignTestCode: String? = null,

    @SerialName("atestraDes")
    val foreignTestDescription: String? = null,

    @SerialName("nazioneCod")
    val nationCode: String? = null,

    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    @SerialName("voto")
    val grade: Double? = null,

    @SerialName("votoBase")
    val baseGrade: Int? = null,

    @SerialName("votoAlfanumerico")
    val alphanumericGrade: String? = null,

    @SerialName("cdsStraniero")
    val foreignCourseOfStudy: String? = null,

    @SerialName("durataAnni")
    val durationYears: Int? = null,

    @SerialName("staTitStraCod")
    val foreignTitleStatusCode: String? = null,

    @SerialName("statiTitDes")
    val titleStatesDescription: String? = null,

    @SerialName("dichiarazValoreFlg")
    val valueDeclarationFlag: Int? = null,

    @SerialName("titoloEquipFlg")
    val equivalentTitleFlag: Int? = null,

    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    @SerialName("tipoDepositoDes")
    val depositTypeDescription: String? = null,

    @SerialName("desTitolo")
    val titleDescription: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    @SerialName("linguaDidIso6391Cod")
    val teachingLanguageIso6391Code: String? = null,

    @SerialName("linguaDidIso6392Cod")
    val teachingLanguageIso6392Code: String? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    @SerialName("lauEntroDnFlg")
    val graduationWithinDeadlineFlag: Int? = null,

    @SerialName("lode")
    val cumLaude: Int? = null,

    @SerialName("valutatoFlg")
    val evaluatedFlag: Int? = null,

    @SerialName("citstraConseg")
    val deliveryForeignCity: String? = null,

    @SerialName("votoMin")
    val minGrade: Double? = null,

    @SerialName("votoConvertito")
    val convertedGrade: Double? = null,

    @SerialName("votoMinConvertito")
    val convertedMinGrade: Double? = null,

    @SerialName("votoBaseConvertito")
    val convertedBaseGrade: Double? = null,

    @SerialName("nazioneOrdId")
    val orderNationId: Int? = null,

    @SerialName("nazioneOrdCod")
    val orderNationCode: String? = null,

    @SerialName("nazioneOrdDes")
    val orderNationDescription: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("tipoDicValCod")
    val valueDeclarationTypeCode: String? = null,

    @SerialName("tipoDicValDes")
    val valueDeclarationTypeDescription: String? = null
)

@Serializable
data class Esse3ExternalEntitiesLocationsReplica(
    @SerialName("soggEstId")
    val externalSubjectId: Int? = null,

    @SerialName("sediEntiEstId")
    val externalEntitiesSitesId: Int? = null,

    @SerialName("enteId")
    val entityId: Int? = null,

    @SerialName("tipoSedeCod")
    val siteTypeCode: String? = null,

    @SerialName("tipoSedeDes")
    val siteTypeDescription: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("via")
    val street: String? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("cF")
    val fiscalCode: String? = null,

    @SerialName("piva")
    val vatNumber: String? = null,

    @SerialName("citstra")
    val foreignCity: String? = null,

    @SerialName("comuneId")
    val municipalityId: Int? = null,

    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    @SerialName("comuneCodCatastale")
    val municipalityCadastralCode: String? = null,

    @SerialName("comuneCodIstatMiur")
    val municipalityMiurIstatCode: String? = null,

    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    @SerialName("comuneSigla")
    val municipalityAbbreviation: String? = null,

    @SerialName("provDes")
    val provinceDescription: String? = null,

    @SerialName("nazioneId")
    val nationId: Int? = null,

    @SerialName("nazieNascCod")
    val birthNationRefCode: String? = null,

    @SerialName("naziNascNazioneCod")
    val birthCountryCode: String? = null,

    @SerialName("naziNascCodInt")
    val birthNationInternationalCode: String? = null,

    @SerialName("numTel")
    val phoneNumber: String? = null,

    @SerialName("prefixInternaz")
    val internationalPrefix: String? = null,

    @SerialName("fax")
    val fax: String? = null,

    @SerialName("codSede")
    val siteCode: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailVisWeb")
    val webVisibleEmail: Int? = null,

    @SerialName("iataCod")
    val iataCode: String? = null,

    @SerialName("disattivaFlg")
    val deactivateFlag: Int? = null,

    @SerialName("pivaGruppo")
    val groupVatNumber: String? = null,

    @SerialName("codiceSdi")
    val sdiCode: String? = null,

    @SerialName("fraz")
    val fraction: String? = null,

    @SerialName("emailCertigficata")
    val certifiedEmail: String? = null,

    @SerialName("cig")
    val cig: String? = null,

    @SerialName("cup")
    val cup: String? = null,

    @SerialName("ipa")
    val ipa: String? = null,

    @SerialName("splitpayementFlg")
    val splitPaymentFlag: Int? = null
)

@Serializable
data class Esse3ItalianTitlesEnrollment(
    @SerialName("dataConsegTitolo")
    val titleDeliveryDate: String? = null,

    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    @SerialName("cdsAteId")
    val courseOfStudyAteId: Long? = null,

    @SerialName("percorsoDiStudio")
    val studyPath: String? = null,

    @SerialName("voto")
    val grade: Float? = null,

    @SerialName("baseVoto")
    val baseGrade: Long? = null,

    @SerialName("lode")
    val cumLaude: Long? = null,

    @SerialName("stessoAteneoFlg")
    val sameUniversityFlag: Long? = null,

    @SerialName("ateneiIstatCod")
    val universitiesIstatCode: String? = null,

    @SerialName("ateneiCodUn")
    val universitiesUnifiedCode: String? = null,

    @SerialName("idTipoLaurea")
    val degreeTypeId: String? = null,

    @SerialName("sessione")
    val session: String? = null,

    @SerialName("iscrAlboFlg")
    val registerEnrollmentFlag: Long? = null,

    @SerialName("cdsAteneiItaIstatCod")
    val italianUniversitiesCourseOfStudyIstatCode: String? = null,

    @SerialName("appellativoF")
    val femaleTitle: String? = null,

    @SerialName("appellativoM")
    val maleTitle: String? = null,

    @SerialName("aaConsegTitolo")
    val academicYearTitleAward: Long? = null,

    @SerialName("abilFlg")
    val authorizationFlag: Long? = null,

    @SerialName("staTitItCod")
    val italianTitleStatusCode: String,

    @SerialName("ordine")
    val order: Long? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("titoloTesi")
    val thesisTitle: String? = null,

    @SerialName("domRicoStraFlg")
    val domicileForeignRecoveryFlag: Long? = null,

    @SerialName("ricoTitStraFlg")
    val foreignTitleRecoveryFlag: Long? = null,

    @SerialName("dataIniTiro")
    val internshipStartDate: String? = null,

    @SerialName("dataFinTiro")
    val internshipEndDate: String? = null,

    @SerialName("dataIscrOrdProf")
    val professionalOrderEnrollmentDate: String? = null,

    @SerialName("desSede")
    val siteDescription: String? = null,

    @SerialName("lauEntroDnFlg")
    val graduationWithinDeadlineFlag: Long? = null,

    @SerialName("cfu")
    val credits: Float? = null,

    @SerialName("giudizioFinDes")
    val finalJudgmentDescription: String? = null,

    @SerialName("tirocinioFlg")
    val internshipFlag: Long? = null,

    @SerialName("desCds")
    val courseOfStudyDescription: String? = null,

    @SerialName("sdrConsegDes")
    val deliverySiteDescription: String? = null,

    @SerialName("sdrConsegNazioneFisCod")
    val deliverySiteFiscalNationCode: String? = null,

    @SerialName("sdrConsegCitstra")
    val deliverySiteForeignCity: String? = null,

    @SerialName("sdrConsegComuneCod")
    val deliverySiteMunicipalityCode: String? = null,

    @SerialName("sdrConsegVia")
    val deliverySiteStreet: String? = null,

    @SerialName("sdrConsegNumCiv")
    val deliverySiteStreetNumber: String? = null,

    @SerialName("sdrConsegCap")
    val deliverySitePostalCode: String? = null,

    @SerialName("tititCod")
    val titleTypeCode: String? = null,

    @SerialName("dataScadenza")
    val expirationDate: String? = null
)

@Serializable
data class Esse3Authorized(
    @SerialName("autorizzatoId")
    val authorizedId: Long? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Long? = null,

    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("tipoParCod")
    val paragraphTypeCode: String? = null,

    @SerialName("tipiParDes")
    val paragraphTypesDescription: String? = null,

    @SerialName("nazioneId")
    val nationId: Long? = null,

    @SerialName("naziCod")
    val nationCode: String? = null,

    @SerialName("naziDes")
    val nationDescription: String? = null,

    @SerialName("naziNazioneCod")
    val nationNationCode: String? = null,

    @SerialName("naziCodInt")
    val nationInternationalCode: String? = null,

    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    @SerialName("comuneId")
    val municipalityId: Long? = null,

    @SerialName("comuCod")
    val municipalityCode: String? = null,

    @SerialName("comuCodCatastale")
    val municipalityCadastralCode: String? = null,

    @SerialName("comuCodIstatMiur")
    val municipalityMiurIstatCode: String? = null,

    @SerialName("comuDes")
    val municipalityDescription: String? = null,

    @SerialName("comuSigla")
    val municipalityAbbreviation: String? = null,

    @SerialName("provDes")
    val provinceDescription: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("autExtCod")
    val externalAuthorizationCode: String? = null
)

@Serializable
data class Esse3HighSchoolDiplomaPerson(
    @SerialName("persId")
    val personId: String? = null,

    @SerialName("aaIntScuolaComuCod")
    val academicYearInternalSchoolMunicipalityCode: String? = null,

    @SerialName("aaIntScuolaComuDes")
    val academicYearInternalSchoolMunicipalityDescription: String? = null,

    @SerialName("aaIntScuolaComuSigla")
    val academicYearInternalSchoolMunicipalityAbbreviation: String? = null,

    @SerialName("aaIntScuolaDes")
    val academicYearInternalSchoolDescription: String? = null,

    @SerialName("aaIntTipiDepDes")
    val academicYearInternalDepositTypesDescription: String? = null,

    @SerialName("anniIntegrativi")
    val supplementaryYears: Int? = null,

    @SerialName("anniScolarita")
    val anniScolarita: Int? = null,

    @SerialName("annoIntegrazione")
    val integrationYear: Int? = null,

    @SerialName("annoIntFlg")
    val integrationYearFlag: Int? = null,

    @SerialName("annoMaturita")
    val highSchoolGraduationYear: Int? = null,

    @SerialName("certAns")
    val certAnswer: Int? = null,

    @SerialName("citstraConseg")
    val deliveryForeignCity: String? = null,

    @SerialName("codExt")
    val externalCode: String? = null,

    @SerialName("dataDepositoTitolo")
    val titleDepositDate: String? = null,

    @SerialName("dataMaturita")
    val highSchoolGraduationDate: String? = null,

    @SerialName("dataRestituzione")
    val returnDate: String? = null,

    @SerialName("desScuola")
    val schoolDescription: String? = null,

    @SerialName("desScuolaAnnoInt")
    val schoolInternationalYearDescription: String? = null,

    @SerialName("aaIntScuolaCodMiur")
    val academicYearInternalSchoolMiurCode: String? = null,

    @SerialName("aaIntScuolaCodScuola")
    val academicYearInternalSchoolCode: String? = null,

    @SerialName("dichiarazValoreFlg")
    val valueDeclarationFlag: Int? = null,

    @SerialName("id")
    val id: Int? = null,

    @SerialName("identificativoGed")
    val gedIdentifier: String? = null,

    @SerialName("indirizzo")
    val address: String? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    @SerialName("lodeFlg")
    val cumLaudeFlag: Int? = null,

    @SerialName("naziConsegCod")
    val deliveryNationCode: String? = null,

    @SerialName("naziConsegDes")
    val deliveryNationDescription: String? = null,

    @SerialName("naziOrdCod")
    val orderNationCode: String? = null,

    @SerialName("naziOrdDes")
    val orderNationDescription: String? = null,

    @SerialName("primaLinguaDes")
    val firstLanguageDescription: String? = null,

    @SerialName("p01ConsolatoDes")
    val p01ConsulateDescription: String? = null,

    @SerialName("raccomandataNum")
    val registeredMailNumber: String? = null,

    @SerialName("restituitoFlg")
    val returnedFlag: Int? = null,

    @SerialName("restituitoTipo")
    val returnedType: String? = null,

    @SerialName("richiestaRestitFlg")
    val returnRequestFlag: Long? = null,

    @SerialName("scuolaComuCod")
    val schoolMunicipalityCode: String? = null,

    @SerialName("scuolaComuDes")
    val schoolMunicipalityDescription: String? = null,

    @SerialName("scuolaComuSigla")
    val schoolMunicipalityAbbreviation: String? = null,

    @SerialName("scuolaDes")
    val schoolName: String? = null,

    @SerialName("scuolaCodMiur")
    val schoolMiurCode: String? = null,

    @SerialName("codScuola")
    val schoolCode: String? = null,

    @SerialName("scuolaSupAnnoIntIdMiur")
    val higherSchoolInternationalYearMiurId: Long? = null,

    @SerialName("scuolaSupIdMiur")
    val miurHigherSchoolId: Long? = null,

    @SerialName("secondaLinguaDes")
    val secondLanguageDescription: String? = null,

    @SerialName("staTitItCod")
    val italianTitleStatusCode: String? = null,

    @SerialName("statiTitItDes")
    val italianTitleStatesDescription: String? = null,

    @SerialName("terzaLinguaDes")
    val thirdLanguageDescription: String? = null,

    @SerialName("tipiDepDes")
    val depositTypesDescription: String? = null,

    @SerialName("tipiTitoloSupAnnoIntFlg")
    val higherTitleInternationalYearFlag: Int? = null,

    @SerialName("tipiTitoloSupDes")
    val higherTitleTypesDescription: String? = null,

    @SerialName("tipiTitstDes")
    val titleStatusTypesDescription: String? = null,

    @SerialName("tipoDepositoAnnoIntCod")
    val internationalYearDepositTypeCode: String? = null,

    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    @SerialName("tipoTitstCod")
    val titleStatusTypeCode: String? = null,

    @SerialName("titAccAmm")
    val adminTitleAccess: Long? = null,

    @SerialName("titAccMat")
    val matTitleAccess: Long? = null,

    @SerialName("titAccMatStu")
    val studentMatTitleAccess: Long? = null,

    @SerialName("valutatoFlg")
    val evaluatedFlag: Long? = null,

    @SerialName("voto")
    val grade: Float? = null,

    @SerialName("votoAlfa")
    val alphanumericGrade: String? = null,

    @SerialName("votoMax")
    val maxGrade: Float? = null,

    @SerialName("votoMin")
    val minGrade: Float? = null
)

@Serializable
data class Esse3ExemptionTypeParameters(
    @SerialName("tipoEsoCod")
    val exemptionTypeCode: String? = null,

    @SerialName("dataIscr")
    val enrollmentDate: String? = null
)

@Serializable
data class Esse3AuthorizedPerson(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("autorizzatoId")
    val authorizedId: Long? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("comuneId")
    val municipalityId: Long? = null,

    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    @SerialName("tipoParCod")
    val paragraphTypeCode: String? = null,

    @SerialName("tipoParDes")
    val paragraphTypeDescription: String? = null,

    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Long? = null,

    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    @SerialName("nazioneId")
    val nationId: Long? = null,

    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("autExtCod")
    val externalAuthorizationCode: String? = null,

    @SerialName("documentiIdentita")
    val identityDocuments: List<Esse3AuthorizedPersonalDocument> = emptyList()
)

@Serializable
data class Esse3PostSpecialNeedsHandicapDeclarationParameters(
    @SerialName("dicHandId")
    val handicapDeclarationId: Int? = null,

    @SerialName("bisognoSpecialeCod")
    val specialNeedCode: String? = null
)

@Serializable
data class Esse3CompensatoryMeasures(
    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    @SerialName("desLiberaFlg")
    val freeDescriptionFlag: Int? = null
)

@Serializable
data class Esse3Person(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    @SerialName("persCod")
    val personCode: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("patronimico")
    val patronymic: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("comuNascId")
    val birthMunicipalityId: Long? = null,

    @SerialName("comuNascCod")
    val birthMunicipalityCode: String? = null,

    @SerialName("comuNascCodCatastale")
    val birthMunicipalityCadastralCode: String? = null,

    @SerialName("comuNascCodIstatMiur")
    val birthMunicipalityMiurIstatCode: String? = null,

    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    @SerialName("naziNascId")
    val birthNationId: Long? = null,

    @SerialName("naziNascCod")
    val birthNationCode: String? = null,

    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    @SerialName("provNascDes")
    val birthProvinceDescription: String? = null,

    @SerialName("naziNascNazioneCod")
    val birthCountryCode: String? = null,

    @SerialName("naziNascDes")
    val birthNationDescription: String? = null,

    @SerialName("naziNascCodInt")
    val birthNationInternationalCode: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Int? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("fotoId")
    val photoId: Long? = null,

    @SerialName("naziResCod")
    val residenceNationCode: String? = null,

    @SerialName("naziResDes")
    val residenceNationDescription: String? = null,

    @SerialName("comuResCod")
    val residenceMunicipalityCode: String? = null,

    @SerialName("comuResDes")
    val residenceMunicipalityDescription: String? = null,

    @SerialName("comuResSigla")
    val residenceMunicipalityAbbreviation: String? = null,

    @SerialName("viaRes")
    val residenceStreet: String? = null,

    @SerialName("numCivRes")
    val residenceStreetNumber: String? = null,

    @SerialName("capRes")
    val residencePostalCode: String? = null,

    @SerialName("telRes")
    val residencePhone: String? = null,

    @SerialName("naziDomCod")
    val domicileNationCode: String? = null,

    @SerialName("naziDomDes")
    val domicileNationDescription: String? = null,

    @SerialName("comuDomCod")
    val domicileMunicipalityCode: String? = null,

    @SerialName("comuDomDes")
    val domicileMunicipalityDescription: String? = null,

    @SerialName("comuDomSigla")
    val domicileMunicipalityAbbreviation: String? = null,

    @SerialName("viaDom")
    val domicileStreet: String? = null,

    @SerialName("numCivDom")
    val domicileStreetNumber: String? = null,

    @SerialName("capDom")
    val domicilePostalCode: String? = null,

    @SerialName("telDom")
    val domicilePhone: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailAte")
    val ateEmail: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("codCittadinanza")
    val citizenshipCode: String? = null,

    @SerialName("desCittadinanza")
    val citizenshipDescription: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("permsogScadutoFlg")
    val authorizedSubjectExpiredFlag: Long? = null,

    @SerialName("presenzaPermSogFlg")
    val authorizedSubjectPresenceFlag: Long? = null,

    @SerialName("permsogDataScad")
    val authorizedSubjectExpirationDate: String? = null,

    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    @SerialName("naziResId")
    val residenceNationId: Long? = null,

    @SerialName("naziResNazioneCod")
    val residenceCountryCode: String? = null,

    @SerialName("naziResCodInt")
    val residenceNationInternationalCode: String? = null,

    @SerialName("comuResId")
    val residenceMunicipalityId: Long? = null,

    @SerialName("comuResCodCatastale")
    val residenceMunicipalityCadastralCode: String? = null,

    @SerialName("comuResCodIstatMiur")
    val residenceMunicipalityMiurIstatCode: String? = null,

    @SerialName("provResDes")
    val residenceProvinceDescription: String? = null,

    @SerialName("citstraRes")
    val residenceForeignCity: String? = null,

    @SerialName("frazRes")
    val residenceFraction: String? = null,

    @SerialName("prefixInternazRes")
    val residenceInternationalPrefix: String? = null,

    @SerialName("dataIniValRes")
    val residenceEvaluationStartDate: String? = null,

    @SerialName("domComeResFlg")
    val domicileSameAsResidenceFlag: Int? = null,

    @SerialName("naziDomId")
    val domicileNationId: Int? = null,

    @SerialName("naziDomNazioneCod")
    val domicileCountryCode: String? = null,

    @SerialName("naziDomCodInt")
    val domicileNationInternationalCode: String? = null,

    @SerialName("comuDomId")
    val domicileMunicipalityId: Long? = null,

    @SerialName("comuDomCodCatastale")
    val domicileMunicipalityCadastralCode: String? = null,

    @SerialName("comuDomCodIstatMiur")
    val domicileMunicipalityMiurIstatCode: String? = null,

    @SerialName("citstraDom")
    val domicileForeignCity: String? = null,

    @SerialName("frazDom")
    val domicileFraction: String? = null,

    @SerialName("prefixInternazDom")
    val domicileInternationalPrefix: String? = null,

    @SerialName("cO")
    val co: String? = null,

    @SerialName("dataIniValDom")
    val domicileEvaluationStartDate: String? = null,

    @SerialName("tipoIndirizCod")
    val addressTypeCode: String? = null,

    @SerialName("recapitoTasse")
    val taxesContact: String? = null,

    @SerialName("recapitoBadge")
    val badgeContact: String? = null,

    @SerialName("fax")
    val fax: String? = null,

    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    @SerialName("citt1NazioneCod")
    val citizenship1CountryCode: String? = null,

    @SerialName("citt1Dataini")
    val citizenship1StartDate: String? = null,

    @SerialName("citt1Datafin")
    val citizenship1EndDate: String? = null,

    @SerialName("citt2Cod")
    val citizenship2Code: String? = null,

    @SerialName("citt2Des")
    val citizenship2Description: String? = null,

    @SerialName("citt2NazioneCod")
    val citizenship2CountryCode: String? = null,

    @SerialName("citt2Dataini")
    val citizenship2StartDate: String? = null,

    @SerialName("citt2Datafin")
    val citizenship2EndDate: String? = null,

    @SerialName("citt3Cod")
    val citizenship3Code: String? = null,

    @SerialName("citt3Des")
    val citizenship3Description: String? = null,

    @SerialName("citt3NazioneCod")
    val citizenship3CountryCode: String? = null,

    @SerialName("citt3Dataini")
    val citizenship3StartDate: String? = null,

    @SerialName("citt3Datafin")
    val citizenship3EndDate: String? = null,

    @SerialName("naziCittadCodInt")
    val citizenshipNationInternationalCode: String? = null,

    @SerialName("naziCittadDes")
    val citizenshipNationDescription: String? = null,

    @SerialName("prefixCell")
    val mobilePrefix: String? = null,

    @SerialName("consDpFlg")
    val consentDpFlag: Int? = null,

    @SerialName("consDiffDpFlg")
    val consentDifferentDpFlag: Int? = null,

    @SerialName("consSmsFlg")
    val consentSmsFlag: Int? = null,

    @SerialName("consComunicDpFlg")
    val consentCommunicationDpFlag: Int? = null,

    @SerialName("consComunicErFlg")
    val consentCommunicationErFlag: Int? = null,

    @SerialName("religiosoFlg")
    val religiousFlag: Int? = null,

    @SerialName("decedutoFlg")
    val deceasedFlag: Int? = null,

    @SerialName("extPersCod")
    val externalPersonCode: String? = null,

    @SerialName("notaPers")
    val personalNote: String? = null,

    @SerialName("professione")
    val profession: String? = null,

    @SerialName("statoCivileCod")
    val maritalStatusCode: String? = null,

    @SerialName("statoCivileDes")
    val maritalStatusDescription: String? = null,

    @SerialName("emergNome")
    val emergencyName: String? = null,

    @SerialName("emergCognome")
    val emergencySurname: String? = null,

    @SerialName("emergTel")
    val emergencyPhone: String? = null,

    @SerialName("emergPrefixInternaz")
    val emergencyInternationalPrefix: String? = null,

    @SerialName("emergEmail")
    val emergencyEmail: String? = null,

    @SerialName("emergRapporto")
    val emergencyRelationship: String? = null
)

@Serializable
data class Esse3HandicapDeclarationPatch(
    @SerialName("validoFlg")
    val validFlag: Long? = null
)

@Serializable
data class Esse3HandicapDeclarationSpecialNeeds(
    @SerialName("dicHandId")
    val handicapDeclarationId: Int? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    @SerialName("percHand")
    val handicapPercentage: Int? = null,

    @SerialName("dataDichiar")
    val declarationDate: String? = null,

    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    @SerialName("dicHandBisId")
    val handicapDeclarationBisId: Int? = null,

    @SerialName("bisognoSpecialeCod")
    val specialNeedCode: String? = null,

    @SerialName("bisognoSpecialeDes")
    val specialNeedDescription: String? = null,

    @SerialName("abilVisOnLine")
    val onlineVisibility: Int? = null
)

@Serializable
data class Esse3AllItalianTitles(
    @SerialName("titItId")
    val italianTitleId: Long? = null,

    @SerialName("dataConsegTitolo")
    val titleDeliveryDate: String? = null,

    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    @SerialName("tipoDepositoDes")
    val depositTypeDescription: String? = null,

    @SerialName("voto")
    val grade: Double? = null,

    @SerialName("baseVoto")
    val baseGrade: Int? = null,

    @SerialName("lode")
    val cumLaude: Int? = null,

    @SerialName("ateneoId")
    val universityId: Long? = null,

    @SerialName("ateneoIstatCod")
    val universityIstatCode: String? = null,

    @SerialName("ateneoCodeUn")
    val universityUnifiedCode: String? = null,

    @SerialName("ateneoDes")
    val universityDescription: String? = null,

    @SerialName("comuCod")
    val municipalityCode: String? = null,

    @SerialName("comuCodCatastale")
    val municipalityCadastralCode: String? = null,

    @SerialName("comuCodIstatMiur")
    val municipalityMiurIstatCode: String? = null,

    @SerialName("comuDes")
    val municipalityDescription: String? = null,

    @SerialName("comuSigla")
    val municipalityAbbreviation: String? = null,

    @SerialName("aaConsegTitolo")
    val academicYearTitleAward: Int? = null,

    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("staTitItCod")
    val italianTitleStatusCode: String? = null,

    @SerialName("statiTitDes")
    val titleStatesDescription: String? = null,

    @SerialName("tititCod")
    val titleTypeCode: String? = null,

    @SerialName("tititDes")
    val titleTypeDescription: String? = null,

    @SerialName("lauEntroDnFlg")
    val graduationWithinDeadlineFlag: Int? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("desSede")
    val siteDescription: String? = null,

    @SerialName("percorsoDiStudio")
    val studyPath: String? = null,

    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    @SerialName("linguaDidIso6391Cod")
    val teachingLanguageIso6391Code: String? = null,

    @SerialName("linguaDidIso6392Cod")
    val teachingLanguageIso6392Code: String? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    @SerialName("giudizioFinDes")
    val finalJudgmentDescription: String? = null,

    @SerialName("tirocinioFlg")
    val internshipFlag: Int? = null,

    @SerialName("dataIniAttivita")
    val activityStartDate: String? = null,

    @SerialName("dataFineAttivita")
    val activityEndDate: String? = null,

    @SerialName("desEstesa")
    val extendedDescription: String? = null,

    @SerialName("numAnniConseguimento")
    val achievementYearsNumber: Int? = null,

    @SerialName("mediaVoti")
    val gradesAverage: Double? = null,

    @SerialName("valutatoFlg")
    val evaluatedFlag: Int? = null,

    @SerialName("dataScadenza")
    val expirationDate: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null
)

@Serializable
data class Esse3CareerGDPR(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailAte")
    val ateEmail: String? = null,

    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("dataImm")
    val matriculationDate: String? = null,

    @SerialName("statiStuDes")
    val studentStatesDescription: String? = null,

    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    @SerialName("numProtocollo")
    val protocolNumber: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("domCtStato")
    val domicileCommitteeState: String? = null,

    @SerialName("statiDomCtDes")
    val committeeApplicationStatesDescription: String? = null,

    @SerialName("aaDes")
    val academicYearDescription: String? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("sedeId")
    val siteId: Int? = null,

    @SerialName("sediDes")
    val sitesDescription: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("lingue")
    val languages: String? = null,

    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("settDes")
    val sectorDescription: String? = null,

    @SerialName("areaCod")
    val areaCode: String? = null,

    @SerialName("areaDes")
    val areaDescription: String? = null,

    @SerialName("areaCodStatMiur")
    val areaMiurStatisticalCode: String? = null,

    @SerialName("sdrCod")
    val siteCode: String? = null,

    @SerialName("sdrDes")
    val siteDescription: String? = null,

    @SerialName("sdrCsaCod")
    val siteCsaCode: Int? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("facCsaCod")
    val facultyCsaCode: String? = null,

    @SerialName("idAb")
    val abbreviatedId: Int? = null,

    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    @SerialName("responsabile")
    val responsible: Esse3PhDProgramDirector? = null,

    @SerialName("tutor")
    val tutor: Esse3TutorData? = null,

    @SerialName("attlauFlg")
    val degreeAwardFlag: Int? = null,

    @SerialName("dataAttlau")
    val degreeAwardDate: String? = null,

    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    @SerialName("profstuCod")
    val studentProfessionCode: String? = null,

    @SerialName("profstuDes")
    val studentProfessionDescription: String? = null,

    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    @SerialName("sospFlg")
    val suspensionFlag: Int? = null,

    @SerialName("p06CdsCod")
    val p06CourseOfStudyCode: String? = null,

    @SerialName("p06CdsDes")
    val p06CourseOfStudyDescription: String? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("dataChiusura")
    val closingDate: String? = null,

    @SerialName("aaImm1")
    val academicYearImm1: Int? = null,

    @SerialName("aaRegId")
    val academicYearRegulationId: Long? = null,

    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    @SerialName("nomeAlias")
    val aliasName: String? = null,

    @SerialName("datiCarrieraStudente")
    val studentCareerData: List<Esse3Coordinator> = emptyList(),

    @SerialName("datiIscrizioneStudente")
    val studentEnrollmentData: List<Esse3PhDSupervisorTutor> = emptyList()
)

@Serializable
data class Esse3HandicapDeclarationValidationFilterParam(
    @SerialName("tipiHandicap")
    val handicapTypes: List<Esse3HandicapDeclarationType> = emptyList(),

    @SerialName("statiDichiarazioneInvalidita")
    val invalidityDeclarationStates: List<Esse3HandicapDeclarationStates> = emptyList(),

    @SerialName("annoAccademicoIscrDicHand")
    val academicYearHandicapDeclarationEnrollment: List<Esse3AcademicYearRegistrationHandicapDeclaration> = emptyList()
)

@Serializable
data class Esse3StudyCourse(
    @SerialName("cdsAteId")
    val courseOfStudyAteId: String? = null,

    @SerialName("ateneoId")
    val universityId: Int? = null,

    @SerialName("ateneiIstatCod")
    val universitiesIstatCode: String? = null,

    @SerialName("ateneiDes")
    val universitiesDescription: String? = null,

    @SerialName("ateneiCodeUn")
    val universitiesUnifiedCode: String? = null,

    @SerialName("ateneiErasmusCod")
    val universitiesErasmusCode: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("istatCod")
    val istatCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("aaDisattivazione")
    val academicYearDeactivation: Int? = null,

    @SerialName("durataAnni")
    val durationYears: Int? = null,

    @SerialName("riformaFlg")
    val reformFlag: Int? = null,

    @SerialName("cod")
    val code: String? = null,

    @SerialName("settFlg")
    val sectorFlag: Int? = null,

    @SerialName("genericoFlg")
    val genericFlag: Int? = null,

    @SerialName("codicione")
    val bigCode: String? = null,

    @SerialName("normId")
    val normId: Int? = null,

    @SerialName("normativaCod")
    val regulationCode: String? = null,

    @SerialName("normativaDes")
    val regulationDescription: String? = null,

    @SerialName("normativaNote")
    val regulationNotes: String? = null,

    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("classe")
    val `class`: List<Esse3StudyCourseClass> = emptyList()
)

@Serializable
data class Esse3TutorData(
    @SerialName("cognomeTutor")
    val tutorSurname: String? = null,

    @SerialName("nomeTutor")
    val tutorName: String? = null,

    @SerialName("docenteIdTutor")
    val lecturerTutorId: Int? = null,

    @SerialName("soggEstIdTutor")
    val externalSubjectTutorId: Int? = null,

    @SerialName("idAbTutor")
    val tutorAbbreviatedId: Int? = null,

    @SerialName("matricolaTutor")
    val tutorMatricola: String? = null
)

@Serializable
data class Esse3StudyCourseClass(
    @SerialName("cdsAteId")
    val courseOfStudyAteId: String? = null,

    @SerialName("cod")
    val code: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("claAreaId")
    val classAreaId: Long? = null,

    @SerialName("iscedF1")
    val iscedF1: String? = null,

    @SerialName("iscedF2")
    val iscedF2: String? = null,

    @SerialName("iscedF3")
    val iscedF3: String? = null
)

@Serializable
data class Esse3PersonalDataContacts(
    @SerialName("contattoId")
    val contactId: Long? = null,

    @SerialName("tipoContattoCod")
    val contactTypeCode: String? = null,

    @SerialName("tipoContattoDes")
    val contactTypeDescription: String? = null,

    @SerialName("ordNum")
    val orderNumber: Long? = null,

    @SerialName("valore")
    val value: String? = null,

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
data class Esse3PutCompensatoryMeasuresHandicapDeclarationParameters(
    @SerialName("statoMisuraComp")
    val compensatoryMeasureState: String? = null,

    @SerialName("misuraDataIni")
    val measureStartDate: String? = null,

    @SerialName("misuraDataFine")
    val measureEndDate: String? = null
)

@Serializable
data class Esse3PersonalDataAddresses(
    @SerialName("anaperIndId")
    val personAddressId: Long? = null,

    @SerialName("tipoIndirizCod")
    val addressTypeCode: String? = null,

    @SerialName("tipoIndirizDes")
    val addressTypeDescription: String? = null,

    @SerialName("nazioneId")
    val nationId: Long? = null,

    @SerialName("naziIndCod")
    val addressNationCode: String? = null,

    @SerialName("naziIndDes")
    val addressNationDescription: String? = null,

    @SerialName("naziIndNazioneCod")
    val addressCountryCode: String? = null,

    @SerialName("naziIndCodInt")
    val addressNationInternationalCode: String? = null,

    @SerialName("comuneId")
    val municipalityId: Long? = null,

    @SerialName("comuNascId")
    val birthMunicipalityId: Long? = null,

    @SerialName("comuNascCod")
    val birthMunicipalityCode: String? = null,

    @SerialName("comuNascCodCatastale")
    val birthMunicipalityCadastralCode: String? = null,

    @SerialName("comuNascCodIstatMiur")
    val birthMunicipalityMiurIstatCode: String? = null,

    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    @SerialName("provIndDes")
    val addressProvinceDescription: String? = null,

    @SerialName("fraz")
    val fraction: String? = null,

    @SerialName("citstra")
    val foreignCity: String? = null,

    @SerialName("via")
    val street: String? = null,

    @SerialName("numCiv")
    val streetNumber: String? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("tel")
    val phone: String? = null,

    @SerialName("prefixInternaz")
    val internationalPrefix: String? = null,

    @SerialName("fax")
    val fax: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("aziendaleFlg")
    val companyRelatedFlag: Int? = null,

    @SerialName("ragioneSociale")
    val companyName: String? = null,

    @SerialName("piva")
    val vatNumber: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    @SerialName("codiceSdi")
    val sdiCode: String? = null,

    @SerialName("url")
    val url: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cig")
    val cig: String? = null,

    @SerialName("cup")
    val cup: String? = null,

    @SerialName("ipa")
    val ipa: String? = null,

    @SerialName("splitpayementFlg")
    val splitPaymentFlag: Int? = null,

    @SerialName("indirizziStorico")
    val historicalAddresses: List<Esse3PersonalDataAddressesHistory> = emptyList()
)

@Serializable
data class Esse3ValidationFlag(
    @SerialName("validaFlg")
    val validationFlag: Int? = null
)

@Serializable
data class Esse3PersonGDPR(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    @SerialName("persCod")
    val personCode: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("patronimico")
    val patronymic: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("comuNascId")
    val birthMunicipalityId: Long? = null,

    @SerialName("comuNascCod")
    val birthMunicipalityCode: String? = null,

    @SerialName("comuNascCodCatastale")
    val birthMunicipalityCadastralCode: String? = null,

    @SerialName("comuNascCodIstatMiur")
    val birthMunicipalityMiurIstatCode: String? = null,

    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    @SerialName("naziNascId")
    val birthNationId: Long? = null,

    @SerialName("naziNascCod")
    val birthNationCode: String? = null,

    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    @SerialName("provNascDes")
    val birthProvinceDescription: String? = null,

    @SerialName("naziNascNazioneCod")
    val birthCountryCode: String? = null,

    @SerialName("naziNascDes")
    val birthNationDescription: String? = null,

    @SerialName("naziNascCodInt")
    val birthNationInternationalCode: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Int? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("fotoId")
    val photoId: Long? = null,

    @SerialName("naziResCod")
    val residenceNationCode: String? = null,

    @SerialName("naziResDes")
    val residenceNationDescription: String? = null,

    @SerialName("comuResCod")
    val residenceMunicipalityCode: String? = null,

    @SerialName("comuResDes")
    val residenceMunicipalityDescription: String? = null,

    @SerialName("comuResSigla")
    val residenceMunicipalityAbbreviation: String? = null,

    @SerialName("viaRes")
    val residenceStreet: String? = null,

    @SerialName("numCivRes")
    val residenceStreetNumber: String? = null,

    @SerialName("capRes")
    val residencePostalCode: String? = null,

    @SerialName("telRes")
    val residencePhone: String? = null,

    @SerialName("naziDomCod")
    val domicileNationCode: String? = null,

    @SerialName("naziDomDes")
    val domicileNationDescription: String? = null,

    @SerialName("comuDomCod")
    val domicileMunicipalityCode: String? = null,

    @SerialName("comuDomDes")
    val domicileMunicipalityDescription: String? = null,

    @SerialName("comuDomSigla")
    val domicileMunicipalityAbbreviation: String? = null,

    @SerialName("viaDom")
    val domicileStreet: String? = null,

    @SerialName("numCivDom")
    val domicileStreetNumber: String? = null,

    @SerialName("capDom")
    val domicilePostalCode: String? = null,

    @SerialName("telDom")
    val domicilePhone: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailAte")
    val ateEmail: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("codCittadinanza")
    val citizenshipCode: String? = null,

    @SerialName("desCittadinanza")
    val citizenshipDescription: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("permsogScadutoFlg")
    val authorizedSubjectExpiredFlag: Long? = null,

    @SerialName("presenzaPermSogFlg")
    val authorizedSubjectPresenceFlag: Long? = null,

    @SerialName("permsogDataScad")
    val authorizedSubjectExpirationDate: String? = null,

    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    @SerialName("naziResId")
    val residenceNationId: Long? = null,

    @SerialName("naziResNazioneCod")
    val residenceCountryCode: String? = null,

    @SerialName("naziResCodInt")
    val residenceNationInternationalCode: String? = null,

    @SerialName("comuResId")
    val residenceMunicipalityId: Long? = null,

    @SerialName("comuResCodCatastale")
    val residenceMunicipalityCadastralCode: String? = null,

    @SerialName("comuResCodIstatMiur")
    val residenceMunicipalityMiurIstatCode: String? = null,

    @SerialName("provResDes")
    val residenceProvinceDescription: String? = null,

    @SerialName("citstraRes")
    val residenceForeignCity: String? = null,

    @SerialName("frazRes")
    val residenceFraction: String? = null,

    @SerialName("prefixInternazRes")
    val residenceInternationalPrefix: String? = null,

    @SerialName("dataIniValRes")
    val residenceEvaluationStartDate: String? = null,

    @SerialName("domComeResFlg")
    val domicileSameAsResidenceFlag: Int? = null,

    @SerialName("naziDomId")
    val domicileNationId: Int? = null,

    @SerialName("naziDomNazioneCod")
    val domicileCountryCode: String? = null,

    @SerialName("naziDomCodInt")
    val domicileNationInternationalCode: String? = null,

    @SerialName("comuDomId")
    val domicileMunicipalityId: Long? = null,

    @SerialName("comuDomCodCatastale")
    val domicileMunicipalityCadastralCode: String? = null,

    @SerialName("comuDomCodIstatMiur")
    val domicileMunicipalityMiurIstatCode: String? = null,

    @SerialName("citstraDom")
    val domicileForeignCity: String? = null,

    @SerialName("frazDom")
    val domicileFraction: String? = null,

    @SerialName("prefixInternazDom")
    val domicileInternationalPrefix: String? = null,

    @SerialName("cO")
    val co: String? = null,

    @SerialName("dataIniValDom")
    val domicileEvaluationStartDate: String? = null,

    @SerialName("tipoIndirizCod")
    val addressTypeCode: String? = null,

    @SerialName("recapitoTasse")
    val taxesContact: String? = null,

    @SerialName("recapitoBadge")
    val badgeContact: String? = null,

    @SerialName("fax")
    val fax: String? = null,

    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    @SerialName("citt1NazioneCod")
    val citizenship1CountryCode: String? = null,

    @SerialName("citt1Dataini")
    val citizenship1StartDate: String? = null,

    @SerialName("citt1Datafin")
    val citizenship1EndDate: String? = null,

    @SerialName("citt2Cod")
    val citizenship2Code: String? = null,

    @SerialName("citt2Des")
    val citizenship2Description: String? = null,

    @SerialName("citt2NazioneCod")
    val citizenship2CountryCode: String? = null,

    @SerialName("citt2Dataini")
    val citizenship2StartDate: String? = null,

    @SerialName("citt2Datafin")
    val citizenship2EndDate: String? = null,

    @SerialName("citt3Cod")
    val citizenship3Code: String? = null,

    @SerialName("citt3Des")
    val citizenship3Description: String? = null,

    @SerialName("citt3NazioneCod")
    val citizenship3CountryCode: String? = null,

    @SerialName("citt3Dataini")
    val citizenship3StartDate: String? = null,

    @SerialName("citt3Datafin")
    val citizenship3EndDate: String? = null,

    @SerialName("naziCittadCodInt")
    val citizenshipNationInternationalCode: String? = null,

    @SerialName("naziCittadDes")
    val citizenshipNationDescription: String? = null,

    @SerialName("prefixCell")
    val mobilePrefix: String? = null,

    @SerialName("consDpFlg")
    val consentDpFlag: Int? = null,

    @SerialName("consDiffDpFlg")
    val consentDifferentDpFlag: Int? = null,

    @SerialName("consSmsFlg")
    val consentSmsFlag: Int? = null,

    @SerialName("consComunicDpFlg")
    val consentCommunicationDpFlag: Int? = null,

    @SerialName("consComunicErFlg")
    val consentCommunicationErFlag: Int? = null,

    @SerialName("religiosoFlg")
    val religiousFlag: Int? = null,

    @SerialName("decedutoFlg")
    val deceasedFlag: Int? = null,

    @SerialName("extPersCod")
    val externalPersonCode: String? = null,

    @SerialName("notaPers")
    val personalNote: String? = null,

    @SerialName("professione")
    val profession: String? = null,

    @SerialName("statoCivileCod")
    val maritalStatusCode: String? = null,

    @SerialName("statoCivileDes")
    val maritalStatusDescription: String? = null,

    @SerialName("emergNome")
    val emergencyName: String? = null,

    @SerialName("emergCognome")
    val emergencySurname: String? = null,

    @SerialName("emergTel")
    val emergencyPhone: String? = null,

    @SerialName("emergPrefixInternaz")
    val emergencyInternationalPrefix: String? = null,

    @SerialName("emergEmail")
    val emergencyEmail: String? = null,

    @SerialName("emergRapporto")
    val emergencyRelationship: String? = null,

    @SerialName("nomeAlias")
    val aliasName: String? = null
)

@Serializable
data class Esse3ExternalSubjectReplica(
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    @SerialName("dipId")
    val departmentId: Long? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("appellativo")
    val title: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("tipoSoggEstCod")
    val externalSubjectTypeCode: String? = null,

    @SerialName("dataIniAtt")
    val activityStartDate: String? = null,

    @SerialName("dataFinAtt")
    val activityEndDate: String? = null,

    @SerialName("sdrId")
    val siteId: Long? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("strutSdrCod")
    val structureSiteCode: String? = null,

    @SerialName("strutSdrDes")
    val structureSiteDescription: String? = null,

    @SerialName("strutSdrTip")
    val structureSiteType: String? = null,

    @SerialName("naziNascId")
    val birthNationId: Long? = null,

    @SerialName("naziNascCod")
    val birthNationCode: String? = null,

    @SerialName("naziNascDes")
    val birthNationDescription: String? = null,

    @SerialName("naziNascCodInt")
    val birthNationInternationalCode: String? = null,

    @SerialName("comuNascId")
    val birthMunicipalityId: Long? = null,

    @SerialName("comuNascCod")
    val birthMunicipalityCode: String? = null,

    @SerialName("comuNascCodCatastale")
    val birthMunicipalityCadastralCode: String? = null,

    @SerialName("comuNascCodIstatMiur")
    val birthMunicipalityMiurIstatCode: String? = null,

    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    @SerialName("provNascDes")
    val birthProvinceDescription: String? = null,

    @SerialName("cistraNasc")
    val birthForeignCitizenship: String? = null,

    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    @SerialName("cittadDes")
    val citizenshipDescription: String? = null,

    @SerialName("tel")
    val phone: String? = null,

    @SerialName("prefixInternaz")
    val internationalPrefix: String? = null,

    @SerialName("fax")
    val fax: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("operCellulare")
    val mobileOperator: Long? = null,

    @SerialName("operCellulareDes")
    val mobileOperatorDescription: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("modInsDati")
    val dataInsertionMode: Long? = null,

    @SerialName("tipiSoggEstDes")
    val externalSubjectTypesDescription: String? = null,

    @SerialName("persAteFlg")
    val atePersonFlag: Long? = null,

    @SerialName("tipiSdrDes")
    val siteTypesDescription: String? = null,

    @SerialName("firmaId")
    val signatureId: Long? = null,

    @SerialName("nominativoAlt")
    val alternativeFullName: String? = null,

    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    @SerialName("consSmsFlg")
    val consentSmsFlag: Long? = null,

    @SerialName("ateIdAccreditamento")
    val ateAccreditationId: Long? = null,

    @SerialName("istatCod")
    val istatCode: String? = null,

    @SerialName("ateneoDes")
    val universityDescription: String? = null,

    @SerialName("consensiSoggEsterni")
    val consentsExternalSubjects: List<Esse3ExternalSubjectsConsentsReplica> = emptyList(),

    @SerialName("entiSoggEsterni")
    val externalSubjectsEntities: List<Esse3ExternalEntitiesReplica> = emptyList()
)

@Serializable
data class Esse3AuthorizationAttachmentMetadata(
    @SerialName("filename")
    val fileName: String,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("titolo")
    val title: String,

    @SerialName("descrizione")
    val description: String,

    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int,

    @SerialName("abilVisWeb")
    val webVisibility: Int
)

@Serializable
data class Esse3HandicapDeclarationAll(
    @SerialName("dicHandId")
    val handicapDeclarationId: Int? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("estensione")
    val extension: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int? = null,

    @SerialName("allegatoTypeCod")
    val attachmentCategoryCode: String? = null,

    @SerialName("filename")
    val fileName: String? = null
)

@Serializable
data class Esse3GetGenericAttachmentMetadata(
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    @SerialName("dimensione")
    val size: Long? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("filename")
    val fileName: String? = null,

    @SerialName("estensione")
    val extension: String? = null,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null
)

@Serializable
data class Esse3TutorsRulesDetail(
    @SerialName("regTutoriDettId")
    val tutorsDetailRegistrationId: Long? = null,

    @SerialName("regTutoriTstId")
    val tutorsTestRegistrationId: Long? = null,

    @SerialName("cod")
    val code: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("etichetta")
    val label: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("tipoParCod")
    val paragraphTypeCode: String? = null,

    @SerialName("tipoParDes")
    val paragraphTypeDescription: String? = null,

    @SerialName("nMin")
    val minNumber: Int? = null,

    @SerialName("nMax")
    val maxNumber: Int? = null,

    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null
)

@Serializable
data class Esse3IdentityDocumentAttachmentMetadata(
    @SerialName("filename")
    val fileName: String,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("titolo")
    val title: String,

    @SerialName("descrizione")
    val description: String,

    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int,

    @SerialName("docIdentTipoCod")
    val identityDocumentTypeCode: String,

    @SerialName("docPersId")
    val personalDocumentId: Long? = null,

    @SerialName("abilVisWeb")
    val webVisibility: Int
)

@Serializable
data class Esse3PersonCompensatoryMeasuresEvaluation(
    @SerialName("dicHandId")
    val handicapDeclarationId: Long? = null,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("percHand")
    val handicapPercentage: Int? = null,

    @SerialName("dataDichiar")
    val declarationDate: String? = null,

    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    @SerialName("dicHandMisureId")
    val handicapDeclarationMeasuresId: Long? = null,

    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String? = null,

    @SerialName("misuraCompensativaDes")
    val compensatoryMeasureDescription: String? = null,

    @SerialName("desLiberaFlg")
    val freeDescriptionFlag: Int? = null,

    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    @SerialName("statoMisuraComp")
    val compensatoryMeasureState: String? = null,

    @SerialName("statoMisuraCompDes")
    val compensatoryMeasureStateDescription: String? = null,

    @SerialName("misuraDataIni")
    val measureStartDate: String? = null,

    @SerialName("misuraDataFine")
    val measureEndDate: String? = null
)

@Serializable
data class Esse3TeachersTimetable(
    @SerialName("docenteOrarioId")
    val lecturerScheduleId: Long,

    @SerialName("docenteId")
    val lecturerId: Long,

    @SerialName("giorno")
    val day: Long? = null,

    @SerialName("giornoDes")
    val dayDescription: String? = null,

    @SerialName("oraInizio")
    val startTime: String? = null,

    @SerialName("oraFine")
    val endTime: String? = null,

    @SerialName("desLuogo")
    val placeDescription: String? = null,

    @SerialName("nota")
    val note: String? = null
)

@Serializable
data class Esse3RelationshipTypes(
    @SerialName("tipoParCod")
    val paragraphTypeCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    @SerialName("percPesoReddito")
    val incomeWeightPercentage: Int? = null,

    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null
)

@Serializable
data class Esse3GetItalianTitleAttachmentMetadata(
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    @SerialName("dimensione")
    val size: Long? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("filename")
    val fileName: String? = null,

    @SerialName("estensione")
    val extension: String? = null,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int? = null,

    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3PersonalDataHistory(
    @SerialName("anaperStoId")
    val personHistoricalId: Long? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("patronimico")
    val patronymic: String? = null,

    @SerialName("nomeAlias")
    val aliasName: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("naziNascId")
    val birthNationId: Long? = null,

    @SerialName("naziNascCod")
    val birthNationCode: String? = null,

    @SerialName("naziNascDes")
    val birthNationDescription: String? = null,

    @SerialName("naziNascNazioneCod")
    val birthCountryCode: String? = null,

    @SerialName("naziNascCodInt")
    val birthNationInternationalCode: String? = null,

    @SerialName("comuNascId")
    val birthMunicipalityId: Long? = null,

    @SerialName("comuNascCod")
    val birthMunicipalityCode: String? = null,

    @SerialName("comuNascCodCatastale")
    val birthMunicipalityCadastralCode: String? = null,

    @SerialName("comuNascCodIstatMiur")
    val birthMunicipalityMiurIstatCode: String? = null,

    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    @SerialName("provNascDes")
    val birthProvinceDescription: String? = null,

    @SerialName("citt1Cod")
    val citizenship1Code: String? = null,

    @SerialName("citt1Des")
    val citizenship1Description: String? = null,

    @SerialName("citt1NazioneCod")
    val citizenship1CountryCode: String? = null,

    @SerialName("citt2Cod")
    val citizenship2Code: String? = null,

    @SerialName("citt2Des")
    val citizenship2Description: String? = null,

    @SerialName("citt2NazioneCod")
    val citizenship2CountryCode: String? = null,

    @SerialName("citt3Cod")
    val citizenship3Code: String? = null,

    @SerialName("citt3Des")
    val citizenship3Description: String? = null,

    @SerialName("citt3NazioneCod")
    val citizenship3CountryCode: String? = null,

    @SerialName("consDiffDpFlg")
    val consentDifferentDpFlag: Int? = null,

    @SerialName("consComunicDpFlg")
    val consentCommunicationDpFlag: Int? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null
)

@Serializable
data class Esse3EnrollmentNumberAttachmentMetadata(
    @SerialName("filename")
    val fileName: String,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("titolo")
    val title: String,

    @SerialName("descrizione")
    val description: String,

    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int,

    @SerialName("abilVisWeb")
    val webVisibility: Int
)

@Serializable
data class Esse3BankDetails(
    @SerialName("persId")
    val personId: Long,

    @SerialName("tipoDatiBancaCod")
    val bankDataTypeCode: String? = null,

    @SerialName("tipiDatiBancaDes")
    val bankDataTypesDescription: String? = null,

    @SerialName("tipoRimbPagCod")
    val paymentRefundTypeCode: String? = null,

    @SerialName("tipiRimbPagDes")
    val paymentRefundTypesDescription: String? = null,

    @SerialName("bancaDes")
    val bankDescription: String? = null,

    @SerialName("ccIntestatario")
    val currentAccountHolder: String? = null,

    @SerialName("ccIntestatarioCf")
    val currentAccountHolderFiscalCode: String? = null,

    @SerialName("ibanCod")
    val ibanCode: String? = null,

    @SerialName("numConto")
    val accountNumber: String? = null,

    @SerialName("nazioneId")
    val nationId: Int? = null,

    @SerialName("naziCod")
    val nationCode: String? = null,

    @SerialName("naziDes")
    val nationDescription: String? = null,

    @SerialName("naziCodFis")
    val nationFiscalCode: String? = null,

    @SerialName("swiftCod")
    val swiftCode: String? = null
)

@Serializable
data class Esse3GetPersonalDocumentAuthorizationMetadata(
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    @SerialName("dimensione")
    val size: Long? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("filename")
    val fileName: String? = null,

    @SerialName("estensione")
    val extension: String? = null,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int? = null,

    @SerialName("abilVisWeb")
    val webVisibility: Int? = null,

    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3ForeignTitleAttachmentMetadata(
    @SerialName("filename")
    val fileName: String,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("titolo")
    val title: String,

    @SerialName("descrizione")
    val description: String,

    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int,

    @SerialName("aaConsegTit")
    val academicYearAwardedTitle: Int,

    @SerialName("tipoTitstCod")
    val titleStatusTypeCode: String? = null,

    @SerialName("titStraId")
    val foreignTitleId: Long? = null
)

@Serializable
data class Esse3BulkDownloadEnabled(
    @SerialName("enabled")
    val enabled: Boolean? = null
)

@Serializable
data class Esse3HandicapDeclarationFiltersWithoutLimits(
    @SerialName("anniAccademici")
    val academicYears: List<Esse3AcademicYearLookup> = emptyList(),

    @SerialName("tipiHandicap")
    val handicapTypes: List<Esse3HandicapTypesLookup> = emptyList(),

    @SerialName("statiDicHand")
    val handicapDeclarationStates: List<Esse3HandicapDeclarationStatesLookup> = emptyList()
)

@Serializable
data class Esse3PersonCompensatoryMeasures(
    @SerialName("dicHandMisureId")
    val handicapDeclarationMeasuresId: Long? = null,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    @SerialName("percHand")
    val handicapPercentage: Int? = null,

    @SerialName("dataDicharaz")
    val declarationDate: String? = null,

    @SerialName("statoDicHandValidoFlg")
    val validHandicapDeclarationStateFlag: Int? = null,

    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String? = null,

    @SerialName("misuraCompensativaDes")
    val compensatoryMeasureDescription: String? = null,

    @SerialName("misuraDesLiberaFlg")
    val freeMeasureDescriptionFlag: Int? = null,

    @SerialName("misuraVisWebFlg")
    val webVisibleMeasureFlag: Int? = null,

    @SerialName("statoMisuraComp")
    val compensatoryMeasureState: String? = null,

    @SerialName("misuraDataIni")
    val measureStartDate: String? = null,

    @SerialName("misuraDataFine")
    val measureEndDate: String? = null,

    @SerialName("dichHandDataIni")
    val handicapDeclarationStartDate: String? = null,

    @SerialName("dichHandDataFine")
    val handicapDeclarationEndDate: String? = null
)

@Serializable
data class Esse3GetHighSchoolDiplomaAttachmentMetadata(
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    @SerialName("dimensione")
    val size: Long? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("filename")
    val fileName: String? = null,

    @SerialName("estensione")
    val extension: String? = null,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int? = null,

    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3ExternalSubject(
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("tipoSoggEstCod")
    val externalSubjectTypeCode: String? = null,

    @SerialName("tipoSoggEstDes")
    val externalSubjectTypeDescription: String? = null,

    @SerialName("dataIniAtt")
    val activityStartDate: String? = null,

    @SerialName("dataFinAtt")
    val activityEndDate: String? = null,

    @SerialName("appellativo")
    val title: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("strutturaDidattResp")
    val didacticResponsibleStructure: String? = null,

    @SerialName("dipartimento")
    val department: String? = null
)

@Serializable
data class Esse3AnnualEnrollment(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    @SerialName("aaRegId")
    val academicYearRegulationId: Long? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Long? = null,

    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("indirizzoDes")
    val addressDescription: String? = null,

    @SerialName("ordinamentoCod")
    val studyOrderCode: String? = null,

    @SerialName("ordinamentoDes")
    val studyOrderDescription: String? = null,

    @SerialName("durataCorso")
    val courseDuration: Int? = null,

    @SerialName("valoreMin")
    val minimumValue: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("anniFc")
    val fcYears: Int? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("tipoIscrDes")
    val enrollmentTypeDescription: String? = null,

    @SerialName("staIscrCod")
    val enrollmentStatusCode: String? = null,

    @SerialName("motStaiscrCod")
    val enrollmentStatusReasonCode: String? = null,

    @SerialName("condFlg")
    val conditionFlag: Int? = null,

    @SerialName("ricFlg")
    val searchFlag: Int? = null,

    @SerialName("attlauFlg")
    val degreeAwardFlag: Int? = null,

    @SerialName("ateneoCod")
    val universityCode: String? = null,

    @SerialName("ateneoDes")
    val universityDescription: String? = null,

    @SerialName("ateneoSiglaUniv")
    val universityAbbreviation: String? = null,

    @SerialName("sedeId")
    val siteId: Long? = null,

    @SerialName("sedeDes")
    val siteDescription: String? = null,

    @SerialName("linguaDid")
    val teachingLanguage: String? = null,

    @SerialName("normCod")
    val normCode: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("aaUltimaIscr")
    val academicYearLastEnrollment: Int? = null,

    @SerialName("orientCod")
    val orientationCode: String? = null,

    @SerialName("orientDes")
    val orientationDescription: String? = null,

    @SerialName("claMurstCod")
    val classMurstCode: String? = null,

    @SerialName("claMurstDes")
    val classMurstDescription: String? = null,

    @SerialName("claAteneoCod")
    val classUniversityCode: String? = null,

    @SerialName("claAteneoDes")
    val classUniversityDescription: String? = null,

    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    @SerialName("ptCfu")
    val ptCredits: Int? = null,

    @SerialName("aptId")
    val aptId: Long? = null,

    @SerialName("ptCfuExtra")
    val ptExtraCredits: Int? = null,

    @SerialName("ptBloccatoFlg")
    val ptBlockedFlag: Int? = null,

    @SerialName("ptSlotId")
    val ptSlotId: Long? = null,

    @SerialName("fasciaId")
    val bandId: Long? = null,

    @SerialName("sospFlg")
    val suspensionFlag: Int? = null,

    @SerialName("motSospCod")
    val suspensionReasonCode: String? = null,

    @SerialName("tipoGruppoId")
    val groupTypeId: Long? = null,

    @SerialName("fasMeritoId")
    val meritBandId: Long? = null,

    @SerialName("regFasId")
    val bandRegistrationId: Long? = null,

    @SerialName("dtCalcMerito")
    val meritCalculationDate: String? = null,

    @SerialName("notaMerito")
    val meritNote: String? = null,

    @SerialName("notaIscr")
    val enrollmentNote: String? = null,

    @SerialName("povFlg")
    val povFlag: Int? = null,

    @SerialName("ueFlg")
    val ueFlag: Int? = null,

    @SerialName("nazioneProvId")
    val provinceNationId: Long? = null,

    @SerialName("fasciaMensaId")
    val canteenBandId: Long? = null,

    @SerialName("codTipoHandicap")
    val handicapTypeCode: String? = null,

    @SerialName("desTipoHandicap")
    val handicapTypeDescription: String? = null,

    @SerialName("percHandicap")
    val disabilityPercentage: Float? = null,

    @SerialName("tipoPostoRisCod")
    val reservedSeatTypeCode: String? = null,

    @SerialName("tipoPostoRiservato")
    val reservedSeatType: String? = null,

    @SerialName("codiceClasseIscrizione")
    val enrollmentClassCode: String? = null,

    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    @SerialName("tipoEsoCod")
    val exemptionTypeCode: String? = null,

    @SerialName("tipoEsoDes")
    val exemptionTypeDescription: String? = null,

    @SerialName("dataIniContratto")
    val contractStartDate: String? = null,

    @SerialName("orientId")
    val orientationId: Long? = null,

    @SerialName("fasciaDichiarId")
    val bandDeclarationId: Long? = null,

    @SerialName("poloId")
    val poleId: Long? = null,

    @SerialName("rifPolFlg")
    val policyReferenceFlag: Int? = null,

    @SerialName("dataAttlau")
    val degreeAwardDate: String? = null,

    @SerialName("tipoStuCod")
    val studentTypeCode: String? = null,

    @SerialName("tipoStuDes")
    val studentTypeDescription: String? = null,

    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null
)

@Serializable
data class Esse3HandicapTypesLookup(
    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("disabilPercFlg")
    val disabilityPercentageFlag: Int? = null,

    @SerialName("disabilitaFlg")
    val disabilityFlag: Int? = null,

    @SerialName("flg104")
    val law104Flag: Int? = null,

    @SerialName("ordWeb")
    val orderWeb: Int? = null
)

@Serializable
data class Esse3HandicapDeclarationReplica(
    @SerialName("dichiarId")
    val declarationId: Long? = null,

    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    @SerialName("tipoHandicapDes")
    val handicapTypeDescription: String? = null,

    @SerialName("disabilPercFlg")
    val disabilityPercentageFlag: Int? = null,

    @SerialName("disabilitaFlg")
    val disabilityFlag: Int? = null,

    @SerialName("inv104Flg")
    val law104InvitationFlag: Int? = null,

    @SerialName("percHand")
    val handicapPercentage: Int? = null,

    @SerialName("dataDichiar")
    val declarationDate: String? = null,

    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    @SerialName("statoDicHandDes")
    val handicapDeclarationStateDescription: String? = null,

    @SerialName("dataIniStato")
    val stateStartDate: String? = null,

    @SerialName("tutoratoFlg")
    val tutoringFlag: Int? = null,

    @SerialName("autTutorFlg")
    val tutorAuthorizationFlag: Int? = null,

    @SerialName("dataIni")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("aaIdCompIni")
    val academicYearComponentStartId: Int? = null,

    @SerialName("aaIdCompFine")
    val academicYearComponentEndId: Int? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("consDsFlg")
    val consentDsFlag: Int? = null,

    @SerialName("handNormativaCod")
    val handicapRegulationCode: String? = null,

    @SerialName("handNormativaDes")
    val handicapRegulationDescription: String? = null,

    @SerialName("besCheckFlg")
    val besCheckFlag: Int? = null,

    @SerialName("dicHandId")
    val handicapDeclarationId: Long? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("misureComp")
    val compensatoryMeasures: List<Esse3HandicapDeclarationCompensatoryMeasures> = emptyList()
)

@Serializable
data class Esse3University(
    @SerialName("ateneoId")
    val universityId: Int? = null,

    @SerialName("istatCod")
    val istatCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("via")
    val street: String? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("citta")
    val city: String? = null,

    @SerialName("prov")
    val province: String? = null,

    @SerialName("cf")
    val fiscalCode: String? = null,

    @SerialName("piva")
    val vatNumber: String? = null,

    @SerialName("desMav1")
    val mav1Description: String? = null,

    @SerialName("desMav2")
    val mav2Description: String? = null,

    @SerialName("desMav3")
    val mav3Description: String? = null,

    @SerialName("desMav4")
    val mav4Description: String? = null,

    @SerialName("almaPref")
    val almaPrefix: String? = null,

    @SerialName("desBd1")
    val bd1Description: String? = null,

    @SerialName("codeUn")
    val unifiedCode: String? = null,

    @SerialName("comuneId")
    val municipalityId: Int? = null,

    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    @SerialName("comuneSigla")
    val municipalityAbbreviation: String? = null,

    @SerialName("comuneCodIstat")
    val municipalityIstatCode: String? = null,

    @SerialName("comuneCap")
    val municipalityPostalCode: String? = null,

    @SerialName("csaUltElab")
    val csaLastProcessing: String? = null,

    @SerialName("urlGuidaWeb")
    val webGuideUrl: String? = null,

    @SerialName("erasmusCod")
    val erasmusCode: String? = null,

    @SerialName("prodotto")
    val product: String? = null,

    @SerialName("webFlg")
    val webFlag: Int? = null,

    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    @SerialName("tipoUnivCod")
    val universityTypeCode: String? = null,

    @SerialName("tipiUnivDes")
    val universityTypesDescription: String? = null,

    @SerialName("tipiUnivNote")
    val universityTypesNotes: String? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("icNumber")
    val icNumber: String? = null,

    @SerialName("siglaUniv")
    val universityAbbreviation: String? = null,

    @SerialName("telefono")
    val phone: String? = null,

    @SerialName("fax")
    val fax: String? = null,

    @SerialName("nazioneId")
    val nationId: Int? = null,

    @SerialName("nazioneCod")
    val nationCode: String? = null,

    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    @SerialName("nazioneCodFisc")
    val nationFiscalCode: String? = null,

    @SerialName("nazioneNazioneCod")
    val nationNationCode: String? = null,

    @SerialName("desCert")
    val certificateDescription: String? = null,

    @SerialName("desCertGenit")
    val parentsCertificateDescription: String? = null,

    @SerialName("desCertLocat")
    val locationCertificateDescription: String? = null,

    @SerialName("desCertVocat")
    val vocationCertificateDescription: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("cvIdIntermediario")
    val cvIntermediaryId: String? = null,

    @SerialName("cvEmail")
    val cvEmail: String? = null,

    @SerialName("ipaCod")
    val ipaCode: String? = null,

    @SerialName("aooCod")
    val officeCode: String? = null,

    @SerialName("bestrCod")
    val bestPracticeCode: String? = null,

    @SerialName("emailCertificata")
    val certifiedEmail: String? = null
)

@Serializable
data class Esse3GetForeignTitleAttachmentMetadata(
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    @SerialName("dimensione")
    val size: Long? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("filename")
    val fileName: String? = null,

    @SerialName("estensione")
    val extension: String? = null,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int? = null,

    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3ForeignTitlePerson(
    @SerialName("persId")
    val personId: String? = null,

    @SerialName("aaConsegId")
    val academicYearAwardId: Long? = null,

    @SerialName("appellativoF")
    val femaleTitle: String? = null,

    @SerialName("appellativoM")
    val maleTitle: String? = null,

    @SerialName("ateneoEquipId")
    val universityEquivalentId: Long? = null,

    @SerialName("cdsItEquipId")
    val courseOfStudyItalianEquivalentId: Long? = null,

    @SerialName("cdsStraniero")
    val foreignCourseOfStudy: String? = null,

    @SerialName("citstraConseg")
    val deliveryForeignCity: String? = null,

    @SerialName("codExt")
    val externalCode: String? = null,

    @SerialName("dataConsegTitolo")
    val titleDeliveryDate: String? = null,

    @SerialName("desAteneo")
    val universityDescription: String? = null,

    @SerialName("desTitolo")
    val titleDescription: String? = null,

    @SerialName("dichiarazValoreFlg")
    val valueDeclarationFlag: Int? = null,

    @SerialName("durataAnni")
    val durationYears: Long? = null,

    @SerialName("identificativoGed")
    val gedIdentifier: String? = null,

    @SerialName("lauEntroDnFlg")
    val graduationWithinDeadlineFlag: Int? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    @SerialName("lode")
    val cumLaude: Int? = null,

    @SerialName("percorsoEquip")
    val equivalentPath: String? = null,

    @SerialName("p01AtestraDes")
    val p01ForeignTestDescription: String? = null,

    @SerialName("p01CdsAteneiItaDes")
    val p01ItalianUniversitiesCourseOfStudyDescription: String? = null,

    @SerialName("p01NaziCod")
    val p01NationCode: String? = null,

    @SerialName("p01NaziDes")
    val p01NationDescription: String? = null,

    @SerialName("p06ADes")
    val p06TeachingActivityDescription: String? = null,

    @SerialName("p06AteneiDes")
    val p06UniversitiesDescription: String? = null,

    @SerialName("p06AteneiIstatCod")
    val p06UniversitiesIstatCode: String? = null,

    @SerialName("p06SdrCod")
    val p06SiteCode: String? = null,

    @SerialName("p06SdrDes")
    val p06SiteDescription: String? = null,

    @SerialName("sdrTip")
    val siteType: String? = null,

    @SerialName("statiTitItDes")
    val italianTitleStatesDescription: String? = null,

    @SerialName("staTitStraCod")
    val foreignTitleStatusCode: String? = null,

    @SerialName("tipiDepositoDes")
    val depositTypesDescription: String? = null,

    @SerialName("tipiEnteDes")
    val entityTypesDescription: String? = null,

    @SerialName("tipiSdrDes")
    val siteTypesDescription: String? = null,

    @SerialName("tipiSdrSdrTip")
    val siteTypesSiteType: String? = null,

    @SerialName("tipiTitstDes")
    val titleStatusTypesDescription: String? = null,

    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    @SerialName("tipoEnteCod")
    val entityTypeCode: String? = null,

    @SerialName("tipoTitstCod")
    val titleStatusTypeCode: String? = null,

    @SerialName("tipoTitstraDes")
    val foreignTitleTypeDescription: String? = null,

    @SerialName("titAccAmm")
    val adminTitleAccess: Long? = null,

    @SerialName("titAccMat")
    val matTitleAccess: Int? = null,

    @SerialName("titAccMatStu")
    val studentMatTitleAccess: Int? = null,

    @SerialName("titoloEquipFlg")
    val equivalentTitleFlag: Long? = null,

    @SerialName("titStraId")
    val foreignTitleId: Long? = null,

    @SerialName("valutatoFlg")
    val evaluatedFlag: Long? = null,

    @SerialName("voto")
    val grade: Float? = null,

    @SerialName("votoAlfanumerico")
    val alphanumericGrade: String? = null,

    @SerialName("votoBase")
    val baseGrade: Int? = null
)

@Serializable
data class Esse3ItalianTitleAttachmentMetadata(
    @SerialName("filename")
    val fileName: String,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("titolo")
    val title: String,

    @SerialName("descrizione")
    val description: String,

    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int,

    @SerialName("aaConsegTit")
    val academicYearAwardedTitle: Int,

    @SerialName("tipoTititCod")
    val titleCategoryCode: String,

    @SerialName("titItId")
    val italianTitleId: Long? = null
)

@Serializable
data class Esse3HandicapDeclarationPersonalData(
    @SerialName("dicHandId")
    val handicapDeclarationId: Int? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    @SerialName("cittadDes")
    val citizenshipDescription: String? = null,

    @SerialName("naziNasc")
    val birthNation: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("luogoNascita")
    val birthPlace: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("nazioneId")
    val nationId: Int? = null,

    @SerialName("naziRes")
    val residenceNation: String? = null,

    @SerialName("comuneId")
    val municipalityId: Int? = null,

    @SerialName("luogoRes")
    val residencePlace: String? = null,

    @SerialName("viaRes")
    val residenceStreet: String? = null,

    @SerialName("numCivRes")
    val residenceStreetNumber: String? = null,

    @SerialName("capRes")
    val residencePostalCode: String? = null,

    @SerialName("telRes")
    val residencePhone: String? = null
)

@Serializable
data class Esse3TeacherPositions(
    @SerialName("struttId")
    val structureId: Long? = null,

    @SerialName("caricaId")
    val positionId: Long? = null,

    @SerialName("caricaDes")
    val positionDescription: String? = null,

    @SerialName("caricaCognome")
    val positionSurname: String? = null,

    @SerialName("caricaNome")
    val positionName: String? = null,

    @SerialName("caricaIdAb")
    val positionAbbreviatedId: Long? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("dataInizioVal")
    val evaluationStartDate: String? = null,

    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    @SerialName("codStruttura")
    val structureCode: String? = null,

    @SerialName("desStruttura")
    val structureDescription: String? = null,

    @SerialName("tipoStruttura")
    val structureType: String? = null
)

@Serializable
data class Esse3SubjectPermission(
    @SerialName("permSogId")
    val authorizedSubjectId: Long? = null,

    @SerialName("dataPres")
    val presenceDate: String? = null,

    @SerialName("numPerm")
    val permitNumber: String? = null,

    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    @SerialName("tipoDepositoDes")
    val depositTypeDescription: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("tipoPermsogCod")
    val authorizedSubjectTypeCode: String? = null,

    @SerialName("tipoPermsogDes")
    val authorizedSubjectTypeDescription: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("numAssicurata")
    val insuredNumber: String? = null,

    @SerialName("motEmisPermsogCod")
    val authorizedSubjectIssuanceReasonCode: String? = null,

    @SerialName("motEmisPermsogDes")
    val authorizedSubjectIssuanceReasonDescription: String? = null,

    @SerialName("statoPermsogCod")
    val authorizedSubjectStateCode: String? = null,

    @SerialName("statoPermsogDes")
    val authorizedSubjectStateDescription: String? = null
)

@Serializable
data class Esse3Career(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailAte")
    val ateEmail: String? = null,

    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("dataImm")
    val matriculationDate: String? = null,

    @SerialName("statiStuDes")
    val studentStatesDescription: String? = null,

    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    @SerialName("numProtocollo")
    val protocolNumber: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("domCtStato")
    val domicileCommitteeState: String? = null,

    @SerialName("statiDomCtDes")
    val committeeApplicationStatesDescription: String? = null,

    @SerialName("aaDes")
    val academicYearDescription: String? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("sedeId")
    val siteId: Int? = null,

    @SerialName("sediDes")
    val sitesDescription: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("lingue")
    val languages: String? = null,

    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("settDes")
    val sectorDescription: String? = null,

    @SerialName("areaCod")
    val areaCode: String? = null,

    @SerialName("areaDes")
    val areaDescription: String? = null,

    @SerialName("areaCodStatMiur")
    val areaMiurStatisticalCode: String? = null,

    @SerialName("sdrCod")
    val siteCode: String? = null,

    @SerialName("sdrDes")
    val siteDescription: String? = null,

    @SerialName("sdrCsaCod")
    val siteCsaCode: Int? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("facCsaCod")
    val facultyCsaCode: String? = null,

    @SerialName("idAb")
    val abbreviatedId: Int? = null,

    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    @SerialName("responsabile")
    val responsible: Esse3PhDProgramDirector? = null,

    @SerialName("tutor")
    val tutor: Esse3TutorData? = null,

    @SerialName("attlauFlg")
    val degreeAwardFlag: Int? = null,

    @SerialName("dataAttlau")
    val degreeAwardDate: String? = null,

    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    @SerialName("profstuCod")
    val studentProfessionCode: String? = null,

    @SerialName("profstuDes")
    val studentProfessionDescription: String? = null,

    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    @SerialName("sospFlg")
    val suspensionFlag: Int? = null,

    @SerialName("p06CdsCod")
    val p06CourseOfStudyCode: String? = null,

    @SerialName("p06CdsDes")
    val p06CourseOfStudyDescription: String? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("dataChiusura")
    val closingDate: String? = null,

    @SerialName("aaImm1")
    val academicYearImm1: Int? = null,

    @SerialName("aaRegId")
    val academicYearRegulationId: Long? = null,

    @SerialName("emailCertificata")
    val certifiedEmail: String? = null
)

@Serializable
data class Esse3RefreshedToken(
    @SerialName("activationUrl")
    val activationUrl: String? = null,

    @SerialName("expiration")
    val expiration: String? = null
)

@Serializable
data class Esse3SpecialNeeds(
    @SerialName("bisognoSpecialeCod")
    val specialNeedCode: String? = null,

    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3HandicapRegulations(
    @SerialName("handNormativaCod")
    val handicapRegulationCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("webFlg")
    val webFlag: Int? = null
)

@Serializable
data class Esse3HandicapDeclarationAttachmentMetadata(
    @SerialName("filename")
    val fileName: String,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("titolo")
    val title: String,

    @SerialName("descrizione")
    val description: String,

    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int,

    @SerialName("tipoHandicap")
    val handicapType: String,

    @SerialName("dataDichiar")
    val declarationDate: String? = null,

    @SerialName("dataIni")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("dicHandId")
    val handicapDeclarationId: Long? = null,

    @SerialName("abilVisWeb")
    val webVisibility: Int
)

@Serializable
data class Esse3NewTeachers(
    @SerialName("docentiRecapiti")
    val lecturersContacts: List<Esse3TeachersContacts> = emptyList(),

    @SerialName("docentiNote")
    val lecturersNotes: List<Esse3TeachersNotes> = emptyList(),

    @SerialName("cariche")
    val positions: List<Esse3TeacherPositions> = emptyList(),

    @SerialName("orario")
    val schedule: List<Esse3TeachersTimetable> = emptyList(),

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("badge")
    val badge: String? = null,

    @SerialName("eMail")
    val email: String? = null,

    @SerialName("emailAte")
    val ateEmail: String? = null,

    @SerialName("emailDocenteLa")
    val lecturerLaEmail: String? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("ruoloDocCod")
    val lecturerRoleCode: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("hyperlink")
    val hyperlink: String? = null,

    @SerialName("dataIniAtt")
    val activityStartDate: String? = null,

    @SerialName("dataFinAtt")
    val activityEndDate: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("p01NaziCodFisc")
    val p01NationFiscalCode: String? = null,

    @SerialName("p01NaziDes")
    val p01NationDescription: String? = null,

    @SerialName("p01NaziNazioneCod")
    val p01NationNationCode: String? = null,

    @SerialName("p01NaziCod")
    val p01NationCode: String? = null,

    @SerialName("p01ComuComuneId")
    val p01MunicipalityCommonId: Long? = null,

    @SerialName("p01ComuCodIstat")
    val p01MunicipalityIstatCode: String? = null,

    @SerialName("p01ComuComuneCod")
    val p01MunicipalityCommonCode: String? = null,

    @SerialName("p01ComuCodIstatMiur")
    val p01MunicipalityMiurIstatCode: String? = null,

    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    @SerialName("p01ProvDes")
    val p01ProvinceDescription: String? = null,

    @SerialName("notePubblicazioni")
    val publicationsNotes: String? = null,

    @SerialName("noteBiografiche")
    val biographicalNotes: String? = null,

    @SerialName("noteCurriculum")
    val curriculumNotes: String? = null,

    @SerialName("noteDocente")
    val lecturerNotes: String? = null,

    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    @SerialName("dataModDoc")
    val documentModificationDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("settDes")
    val sectorDescription: String? = null,

    @SerialName("dipId")
    val departmentId: Long? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("ruoloDocDes")
    val lecturerRoleDescription: String? = null,

    @SerialName("profilo")
    val profile: String? = null,

    @SerialName("docenteAppellativo")
    val lecturerTitle: String? = null,

    @SerialName("dataIniRuolo")
    val roleStartDate: String? = null
)

@Serializable
data class Esse3HighSchoolDiplomaAttachmentMetadata(
    @SerialName("filename")
    val fileName: String,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("titolo")
    val title: String,

    @SerialName("descrizione")
    val description: String,

    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int,

    @SerialName("annoMaturita")
    val highSchoolGraduationYear: Int,

    @SerialName("idDiplomaMiur")
    val miurDiplomaId: Long? = null,

    @SerialName("dataMaturita")
    val highSchoolGraduationDate: String? = null,

    @SerialName("maturId")
    val highSchoolGraduationId: Long? = null
)

@Serializable
data class Esse3PersonalDataAddressesHistory(
    @SerialName("anaperIndStoId")
    val personHistoricalAddressId: Long? = null,

    @SerialName("tipoIndirizCod")
    val addressTypeCode: String? = null,

    @SerialName("tipoIndirizDes")
    val addressTypeDescription: String? = null,

    @SerialName("nazioneId")
    val nationId: Long? = null,

    @SerialName("naziIndCod")
    val addressNationCode: String? = null,

    @SerialName("naziIndDes")
    val addressNationDescription: String? = null,

    @SerialName("naziIndNazioneCod")
    val addressCountryCode: String? = null,

    @SerialName("naziIndCodInt")
    val addressNationInternationalCode: String? = null,

    @SerialName("comuneId")
    val municipalityId: Long? = null,

    @SerialName("comuNascId")
    val birthMunicipalityId: Long? = null,

    @SerialName("comuNascCod")
    val birthMunicipalityCode: String? = null,

    @SerialName("comuNascCodCatastale")
    val birthMunicipalityCadastralCode: String? = null,

    @SerialName("comuNascCodIstatMiur")
    val birthMunicipalityMiurIstatCode: String? = null,

    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    @SerialName("provIndDes")
    val addressProvinceDescription: String? = null,

    @SerialName("fraz")
    val fraction: String? = null,

    @SerialName("citstra")
    val foreignCity: String? = null,

    @SerialName("via")
    val street: String? = null,

    @SerialName("numCiv")
    val streetNumber: String? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("tel")
    val phone: String? = null,

    @SerialName("prefixInternaz")
    val internationalPrefix: String? = null,

    @SerialName("fax")
    val fax: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("aziendaleFlg")
    val companyRelatedFlag: Int? = null,

    @SerialName("ragioneSociale")
    val companyName: String? = null,

    @SerialName("piva")
    val vatNumber: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    @SerialName("codiceSdi")
    val sdiCode: String? = null,

    @SerialName("url")
    val url: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cig")
    val cig: String? = null,

    @SerialName("cup")
    val cup: String? = null,

    @SerialName("ipa")
    val ipa: String? = null,

    @SerialName("splitpayementFlg")
    val splitPaymentFlag: Int? = null
)

@Serializable
data class Esse3StudentsConsents(
    @SerialName("persId")
    val personId: Int? = null,

    @SerialName("tipiConsensoTipoConsensoCod")
    val consentTypesConsentTypeCode: String? = null,

    @SerialName("consensoFlg")
    val consentFlag: Int? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("dataIni")
    val startDate: String? = null,

    @SerialName("procAmmCod")
    val administrativeProcedureCode: String? = null,

    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    @SerialName("vincFlg")
    val winnerFlag: Int? = null,

    @SerialName("abilVisDocFlg")
    val documentVisibilityFlag: Int? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("etichetta")
    val label: String? = null,

    @SerialName("p01AnaperConsensiTipoConsensoCod")
    val p01PersonConsentsConsentTypeCode: String? = null
)

@Serializable
data class Esse3TutorsRulesHeader(
    @SerialName("regTutoriTstId")
    val tutorsTestRegistrationId: Long? = null,

    @SerialName("cod")
    val code: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("etichetta")
    val label: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("dettaglio")
    val detail: List<Esse3TutorsRulesDetail> = emptyList()
)

@Serializable
data class Esse3StudentTypeParameters(
    @SerialName("tipoStuCod")
    val studentTypeCode: String? = null
)

@Serializable
data class Esse3Authorizations(
    @SerialName("autorizzazioneId")
    val authorizationId: Long? = null,

    @SerialName("autorizzazioneCod")
    val authorizationCode: String? = null,

    @SerialName("autorizzazioneDes")
    val authorizationDescription: String? = null,

    @SerialName("provvFlg")
    val provisionalFlag: Int? = null,

    @SerialName("dataAutorizz")
    val authorizationDate: String? = null,

    @SerialName("dataRevoca")
    val revocationDate: String? = null,

    @SerialName("autorizzatoId")
    val authorizedId: Long? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Long? = null,

    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("tipoParCod")
    val paragraphTypeCode: String? = null,

    @SerialName("tipiParDes")
    val paragraphTypesDescription: String? = null,

    @SerialName("nazioneId")
    val nationId: Long? = null,

    @SerialName("naziCod")
    val nationCode: String? = null,

    @SerialName("naziDes")
    val nationDescription: String? = null,

    @SerialName("naziNazioneCod")
    val nationNationCode: String? = null,

    @SerialName("naziCodInt")
    val nationInternationalCode: String? = null,

    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    @SerialName("comuneId")
    val municipalityId: Long? = null,

    @SerialName("comuCod")
    val municipalityCode: String? = null,

    @SerialName("comuCodCatastale")
    val municipalityCadastralCode: String? = null,

    @SerialName("comuCodIstatMiur")
    val municipalityMiurIstatCode: String? = null,

    @SerialName("comuDes")
    val municipalityDescription: String? = null,

    @SerialName("comuSigla")
    val municipalityAbbreviation: String? = null,

    @SerialName("provDes")
    val provinceDescription: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("autExtCod")
    val externalAuthorizationCode: String? = null
)

@Serializable
data class Esse3HighSchoolDiploma(
    @SerialName("id")
    val id: Long? = null,

    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    @SerialName("tipoTitoloDes")
    val titleTypeDescription: String? = null,

    @SerialName("idDiploma")
    val diplomaId: Long? = null,

    @SerialName("annoMaturita")
    val highSchoolGraduationYear: Int? = null,

    @SerialName("dataMaturita")
    val highSchoolGraduationDate: String? = null,

    @SerialName("voto")
    val grade: Int? = null,

    @SerialName("votoMin")
    val minGrade: Int? = null,

    @SerialName("votoMax")
    val maxGrade: Int? = null,

    @SerialName("votoNormal")
    val normalGrade: String? = null,

    @SerialName("lodeFlg")
    val cumLaudeFlag: Int? = null,

    @SerialName("tipiIstCod")
    val institutionTypesCode: String? = null,

    @SerialName("tipiIstDes")
    val institutionTypesDescription: String? = null,

    @SerialName("scuolaSupId")
    val higherSchoolId: Long? = null,

    @SerialName("codScuola")
    val schoolCode: String? = null,

    @SerialName("idScuolaMiur")
    val miurSchoolId: Long? = null,

    @SerialName("scuolaDes")
    val schoolName: String? = null,

    @SerialName("via")
    val street: String? = null,

    @SerialName("numeroCivico")
    val streetNumber: String? = null,

    @SerialName("nazioneConsegCod")
    val deliveryNationCode: String? = null,

    @SerialName("nazioneConsegDes")
    val deliveryNationDescription: String? = null,

    @SerialName("comuCodIstatMiur")
    val municipalityMiurIstatCode: String? = null,

    @SerialName("comuCodCatastale")
    val municipalityCadastralCode: String? = null,

    @SerialName("comuDes")
    val municipalityDescription: String? = null,

    @SerialName("sigla")
    val abbreviation: String? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("scuolaNonStatFlg")
    val nonStatutorySchoolFlag: Int? = null,

    @SerialName("nazioneOrdinamenCod")
    val orderNationCode: String? = null,

    @SerialName("nazioneOrdinamenDes")
    val orderNationDescription: String? = null,

    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("indirizzo")
    val address: String? = null,

    @SerialName("staTitItCod")
    val italianTitleStatusCode: String? = null,

    @SerialName("statiTitDes")
    val titleStatesDescription: String? = null,

    @SerialName("tipoTitstDes")
    val titleStatusTypeDescription: String? = null,

    @SerialName("istStDes")
    val institutionStateDescription: String? = null,

    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    @SerialName("linguaDidIso6391Cod")
    val teachingLanguageIso6391Code: String? = null,

    @SerialName("linguaDidIso6392Cod")
    val teachingLanguageIso6392Code: String? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    @SerialName("codExt")
    val externalCode: String? = null,

    @SerialName("valutatoFlg")
    val evaluatedFlag: Int? = null,

    @SerialName("citstraConseg")
    val deliveryForeignCity: String? = null,

    @SerialName("mediaVoti")
    val gradesAverage: Double? = null
)

@Serializable
data class Esse3PhDSupervisorTutor(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    @SerialName("tipoRefCod")
    val referenceTypeCode: String? = null,

    @SerialName("tipoRefDes")
    val referenceTypeDescription: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    @SerialName("principaleFlg")
    val mainFlag: Int? = null
)

@Serializable
data class Esse3ThesisExtension(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    @SerialName("dataCotutela")
    val cotutorshipDate: String? = null,

    @SerialName("domCotutelaFlg")
    val domicileCotutorshipFlag: Long? = null,

    @SerialName("cotutelaFlg")
    val cotutorshipFlag: Long? = null,

    @SerialName("atestraErasmusCod")
    val foreignTestErasmusCode: String? = null,

    @SerialName("atestraDes")
    val foreignTestDescription: String? = null,

    @SerialName("tipoCotutelaCod")
    val cotutorshipTypeCode: String? = null,

    @SerialName("tipiCotutelaDes")
    val cotutorshipTypesDescription: String? = null,

    @SerialName("mesiProrogaCt")
    val committeeExtensionMonths: Long? = null,

    @SerialName("mesiProrogaCtRic")
    val committeeExtensionMonthsRequest: Long? = null,

    @SerialName("dataRicProrogaCt")
    val extensionRequestCommitteeDate: String? = null,

    @SerialName("motRicProrogaCt")
    val committeeExtensionRequestReason: String? = null,

    @SerialName("mesiDifftesiCtRic")
    val thesisDifferenceCommitteeMonthsRequest: Long? = null,

    @SerialName("dataRicDifftesiCt")
    val thesisDifferenceRequestCommitteeDate: String? = null,

    @SerialName("motRicDifftesiCt")
    val thesisDifferenceCommitteeRequestReason: String? = null
)

@Serializable
data class Esse3TeachersNotes(
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("noteBiografiche")
    val biographicalNotes: String? = null,

    @SerialName("notePubblicazioni")
    val publicationsNotes: String? = null,

    @SerialName("noteCurriculum")
    val curriculumNotes: String? = null,

    @SerialName("noteDocente")
    val lecturerNotes: String? = null
)

@Serializable
data class Esse3GetHandicapDeclarationAttachmentMetadata(
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    @SerialName("dimensione")
    val size: Long? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("filename")
    val fileName: String? = null,

    @SerialName("estensione")
    val extension: String? = null,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int? = null,

    @SerialName("abilVisWeb")
    val webVisibility: Int? = null,

    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3GetIdentityDocumentAttachmentMetadata(
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    @SerialName("dimensione")
    val size: Long? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("filename")
    val fileName: String? = null,

    @SerialName("estensione")
    val extension: String? = null,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int? = null,

    @SerialName("abilVisWeb")
    val webVisibility: Int? = null,

    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null,

    @SerialName("docPersId")
    val personalDocumentId: Long? = null
)

@Serializable
data class Esse3PersonalDocumentAuthorizationMetadata(
    @SerialName("filename")
    val fileName: String,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("titolo")
    val title: String,

    @SerialName("descrizione")
    val description: String,

    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int,

    @SerialName("docIdentTipoCod")
    val identityDocumentTypeCode: String,

    @SerialName("autDocPersId")
    val personalDataDocAuthorizationId: Long? = null,

    @SerialName("abilVisWeb")
    val webVisibility: Int
)

@Serializable
data class Esse3GraduationWaitingParameters(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Long? = null,

    @SerialName("attlauFlg")
    val degreeAwardFlag: Int,

    @SerialName("dataAttlau")
    val degreeAwardDate: String? = null
)

@Serializable
data class Esse3EnrollmentReturn(
    @SerialName("codiceRitorno")
    val returnCode: Int? = null,

    @SerialName("idElenco")
    val listId: Int? = null,

    @SerialName("errori")
    val errors: List<Esse3DettaglioErrore> = emptyList()
)

@Serializable
data class Esse3CareerMinimalData(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailAte")
    val ateEmail: String? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("aaDes")
    val academicYearDescription: String? = null,

    @SerialName("dataImm")
    val matriculationDate: String? = null,

    @SerialName("statiStuDes")
    val studentStatesDescription: String? = null,

    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    @SerialName("numProtocollo")
    val protocolNumber: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    @SerialName("sospFlg")
    val suspensionFlag: Int? = null,

    @SerialName("p06CdsCod")
    val p06CourseOfStudyCode: String? = null,

    @SerialName("p06CdsDes")
    val p06CourseOfStudyDescription: String? = null,

    @SerialName("sedeId")
    val siteId: Int? = null,

    @SerialName("sediDes")
    val sitesDescription: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("facCsaCod")
    val facultyCsaCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Int? = null,

    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    @SerialName("profstuCod")
    val studentProfessionCode: String? = null,

    @SerialName("profstuDes")
    val studentProfessionDescription: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("dataChiusura")
    val closingDate: String? = null,

    @SerialName("matId")
    val matId: Int? = null
)

@Serializable
data class Esse3CanteenBandParameters(
    @SerialName("fasciaMensaId")
    val canteenBandId: Long? = null
)

@Serializable
data class Esse3HandicapDeclarationPut(
    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    @SerialName("percHand")
    val handicapPercentage: Int? = null,

    @SerialName("dataIni")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null
)

@Serializable
data class Esse3PersonTitles(
    @SerialName("persId")
    val personId: String? = null,

    @SerialName("SUP")
    val SUP: List<Esse3HighSchoolDiplomaPerson> = emptyList(),

    @SerialName("TITSTRA")
    val foreignTitle: List<Esse3ForeignTitlePerson> = emptyList(),

    @SerialName("TITIT")
    val italianTitle: List<Esse3ItalianTitlePerson> = emptyList()
)

@Serializable
data class Esse3Coordinator(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    @SerialName("caricaId")
    val positionId: Int? = null,

    @SerialName("firmaId")
    val signatureId: Int? = null,

    @SerialName("caricaDes")
    val positionDescription: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("appellativo")
    val title: String? = null,

    @SerialName("dataInizioVal")
    val evaluationStartDate: String? = null,

    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    @SerialName("docenteId")
    val lecturerId: Int? = null,

    @SerialName("soggEstId")
    val externalSubjectId: Int? = null
)

@Serializable
data class Esse3TitlesInsertion(
    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("matur")
    val highSchoolGraduation: List<Esse3HigherTitlesEnrollment> = emptyList(),

    @SerialName("titIt")
    val italianTitle: List<Esse3ItalianTitlesEnrollment> = emptyList(),

    @SerialName("titStra")
    val foreignTitle: List<Esse3ForeignTitlesEnrollment> = emptyList()
)

@Serializable
data class Esse3HandicapDeclarationType(
    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    @SerialName("tipoHandicapDes")
    val handicapTypeDescription: String? = null
)

@Serializable
data class Esse3ExternalSubjectsConsents(
    @SerialName("soggEstId")
    val externalSubjectId: Int? = null,

    @SerialName("tipiConsensoTipoConsensoCod")
    val consentTypesConsentTypeCode: String? = null,

    @SerialName("consensoFlg")
    val consentFlag: Int? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("dataIni")
    val startDate: String? = null,

    @SerialName("procAmmCod")
    val administrativeProcedureCode: String? = null,

    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    @SerialName("vincFlg")
    val winnerFlag: Int? = null,

    @SerialName("abilVisDocFlg")
    val documentVisibilityFlag: Int? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("etichetta")
    val label: String? = null,

    @SerialName("p01SoggEstConsensiTipoConsensoCod")
    val p01ExternalSubjectConsentsConsentTypeCode: String? = null
)

@Serializable
data class Esse3HigherTitlesEnrollment(
    @SerialName("annoMaturita")
    val highSchoolGraduationYear: Long,

    @SerialName("dataMaturita")
    val highSchoolGraduationDate: String? = null,

    @SerialName("idDiploma")
    val diplomaId: Long? = null,

    @SerialName("tipoDepositoCodSup")
    val higherDepositTypeCode: String? = null,

    @SerialName("dataDepositoTitolo")
    val titleDepositDate: String? = null,

    @SerialName("indirizzo")
    val address: String? = null,

    @SerialName("voto")
    val grade: Float? = null,

    @SerialName("votoAlfa")
    val alphanumericGrade: String? = null,

    @SerialName("anniScolarita")
    val anniScolarita: Long? = null,

    @SerialName("votoMin")
    val minGrade: Long? = null,

    @SerialName("dichiarazValoreFlg")
    val valueDeclarationFlag: Long? = null,

    @SerialName("votoMax")
    val maxGrade: Long? = null,

    @SerialName("identificativoGed")
    val gedIdentifier: String? = null,

    @SerialName("tipoDepositoCodAnnoInt")
    val internationalYearDepositTypeCode: String? = null,

    @SerialName("annoIntegrazione")
    val integrationYear: Long? = null,

    @SerialName("restituitoFlg")
    val returnedFlag: Long? = null,

    @SerialName("dataRestituzione")
    val returnDate: String? = null,

    @SerialName("consolatoId")
    val consulateId: Long? = null,

    @SerialName("richiestaRestitFlg")
    val returnRequestFlag: Long? = null,

    @SerialName("annoIntFlg")
    val integrationYearFlag: Long? = null,

    @SerialName("tipoTitstCod")
    val titleStatusTypeCode: String? = null,

    @SerialName("naziConsCodFis")
    val nationConsFiscalCode: String,

    @SerialName("naziOrdCodfis")
    val orderNationFiscalCode: String,

    @SerialName("miurScuoleCodScuola")
    val miurSchoolsSchoolCode: String? = null,

    @SerialName("lingua1")
    val language1: Long? = null,

    @SerialName("lingua2")
    val language2: Long? = null,

    @SerialName("lingua3")
    val language3: Long? = null,

    @SerialName("tipoTitstDes")
    val titleStatusTypeDescription: String? = null,

    @SerialName("anniIntegrativi")
    val supplementaryYears: Long? = null,

    @SerialName("staTitItCod")
    val italianTitleStatusCode: String,

    @SerialName("lodeFlg")
    val cumLaudeFlag: Long? = null,

    @SerialName("desScuola")
    val schoolDescription: String? = null,

    @SerialName("istStDes")
    val institutionStateDescription: String? = null,

    @SerialName("codExt")
    val externalCode: String? = null,

    @SerialName("citstraConseg")
    val deliveryForeignCity: String? = null
)

@Serializable
data class Esse3TeachersContacts(
    @SerialName("docenteId")
    val lecturerId: Long,

    @SerialName("citt1Cod")
    val citizenship1Code: String? = null,

    @SerialName("citt1Des")
    val citizenship1Description: String? = null,

    @SerialName("citt1NazioneCod")
    val citizenship1CountryCode: String? = null,

    @SerialName("citt1Dataini")
    val citizenship1StartDate: String? = null,

    @SerialName("citt1Datafin")
    val citizenship1EndDate: String? = null,

    @SerialName("citt2Cod")
    val citizenship2Code: String? = null,

    @SerialName("citt2Des")
    val citizenship2Description: String? = null,

    @SerialName("citt2NazioneCod")
    val citizenship2CountryCode: String? = null,

    @SerialName("citt2Dataini")
    val citizenship2StartDate: String? = null,

    @SerialName("citt2Datafin")
    val citizenship2EndDate: String? = null,

    @SerialName("citt3Cod")
    val citizenship3Code: String? = null,

    @SerialName("citt3Des")
    val citizenship3Description: String? = null,

    @SerialName("citt3NazioneCod")
    val citizenship3CountryCode: String? = null,

    @SerialName("citt3Dataini")
    val citizenship3StartDate: String? = null,

    @SerialName("citt3Datafin")
    val citizenship3EndDate: String? = null,

    @SerialName("naziResId")
    val residenceNationId: Long? = null,

    @SerialName("comResId")
    val residenceMunicipalityId: Long? = null,

    @SerialName("naziResCodFis")
    val residenceNationFiscalCode: String? = null,

    @SerialName("naziResDes")
    val residenceNationDescription: String? = null,

    @SerialName("naziResNazioneCod")
    val residenceCountryCode: String? = null,

    @SerialName("naziResCod")
    val residenceNationCode: String? = null,

    @SerialName("comuResCodIstat")
    val residenceMunicipalityIstatCode: String? = null,

    @SerialName("comuResComuneCod")
    val residenceMunicipalityCommonCode: String? = null,

    @SerialName("comuResCodIstatMiur")
    val residenceMunicipalityMiurIstatCode: String? = null,

    @SerialName("comuResDes")
    val residenceMunicipalityDescription: String? = null,

    @SerialName("citstraRes")
    val residenceForeignCity: String? = null,

    @SerialName("comuResSigla")
    val residenceMunicipalityAbbreviation: String? = null,

    @SerialName("provResDes")
    val residenceProvinceDescription: String? = null,

    @SerialName("viaRes")
    val residenceStreet: String? = null,

    @SerialName("numCivRes")
    val residenceStreetNumber: String? = null,

    @SerialName("capRes")
    val residencePostalCode: String? = null,

    @SerialName("telRes")
    val residencePhone: String? = null,

    @SerialName("prefixInternazRes")
    val residenceInternationalPrefix: String? = null,

    @SerialName("nazDomId")
    val domicileNationId: Long? = null,

    @SerialName("comDomId")
    val domicileMunicipalityId: Long? = null,

    @SerialName("naziDomCodFisc")
    val domicileNationFiscalCode: String? = null,

    @SerialName("naziDomDes")
    val domicileNationDescription: String? = null,

    @SerialName("naziDomNazioneCod")
    val domicileCountryCode: String? = null,

    @SerialName("naziDomCod")
    val domicileNationCode: String? = null,

    @SerialName("comuDomCodIstat")
    val domicileMunicipalityIstatCode: String? = null,

    @SerialName("comuDomComuneCod")
    val domicileMunicipalityCommonCode: String? = null,

    @SerialName("comuDomCodIstatMiur")
    val domicileMunicipalityMiurIstatCode: String? = null,

    @SerialName("comuDomDes")
    val domicileMunicipalityDescription: String? = null,

    @SerialName("citstraDom")
    val domicileForeignCity: String? = null,

    @SerialName("comuDomSigla")
    val domicileMunicipalityAbbreviation: String? = null,

    @SerialName("provDomDes")
    val domicileProvinceDescription: String? = null,

    @SerialName("viaDom")
    val domicileStreet: String? = null,

    @SerialName("numCivDom")
    val domicileStreetNumber: String? = null,

    @SerialName("capDom")
    val domicilePostalCode: String? = null,

    @SerialName("telDom")
    val domicilePhone: String? = null,

    @SerialName("prefixInternazDom")
    val domicileInternationalPrefix: String? = null,

    @SerialName("co")
    val co: String? = null,

    @SerialName("fax")
    val fax: String? = null,

    @SerialName("tipoIndirizCod")
    val addressTypeCode: String? = null,

    @SerialName("domComeResFlg")
    val domicileSameAsResidenceFlag: Boolean? = null,

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
data class Esse3ForForeignStudent(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    @SerialName("sdrCod")
    val siteCode: String? = null,

    @SerialName("sdrDes")
    val siteDescription: String? = null,

    @SerialName("sdrNaziCod")
    val siteNationCode: String? = null,

    @SerialName("sdrNaziDes")
    val siteNationDescription: String? = null,

    @SerialName("sdrCittà")
    val siteCity: String? = null,

    @SerialName("via")
    val street: String? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("dataInizioPeriodo")
    val periodStartDate: String? = null,

    @SerialName("dataFinePeriodo")
    val periodEndDate: String? = null,

    @SerialName("maggImpBorsa")
    val scholarshipIncreaseImport: Float? = null,

    @SerialName("numGiorni")
    val daysNumber: Long? = null,

    @SerialName("numGiorniMaggioraz")
    val increaseDaysNumber: Long? = null,

    @SerialName("annoPagamentoId")
    val paymentYearId: Long? = null,

    @SerialName("mesePagamento")
    val paymentMonth: Long? = null
)

@Serializable
data class Esse3ForeignUniversity(
    @SerialName("ateneoStranieroId")
    val foreignUniversityId: Int? = null,

    @SerialName("nazioneId")
    val nationId: Int? = null,

    @SerialName("nazioneCod")
    val nationCode: String? = null,

    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    @SerialName("nazioneCodFisc")
    val nationFiscalCode: String? = null,

    @SerialName("nazioneMiurCod")
    val miurNationCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("citStra")
    val foreignCity: String? = null,

    @SerialName("comuneId")
    val municipalityId: Int? = null,

    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    @SerialName("comuneSigla")
    val municipalityAbbreviation: String? = null,

    @SerialName("comuneCodIstat")
    val municipalityIstatCode: String? = null,

    @SerialName("comuneCap")
    val municipalityPostalCode: String? = null,

    @SerialName("rettoreId")
    val rectorId: Int? = null,

    @SerialName("rettoreCognome")
    val rectorSurname: String? = null,

    @SerialName("rettoreNome")
    val rectorName: String? = null,

    @SerialName("rettoreSesso")
    val rectorGender: String? = null,

    @SerialName("rettoreCodFis")
    val rectorFiscalCode: String? = null,

    @SerialName("rettoreDataNascita")
    val rectorBirthDate: String? = null,

    @SerialName("rettoreTel")
    val rectorPhone: String? = null,

    @SerialName("rettoreCellulare")
    val rectorMobile: String? = null,

    @SerialName("rettoreEmail")
    val rectorEmail: String? = null,

    @SerialName("homePage")
    val homePage: String? = null,

    @SerialName("erasmusCod")
    val erasmusCode: String? = null,

    @SerialName("via")
    val street: String? = null,

    @SerialName("tel")
    val phone: String? = null,

    @SerialName("prefixInternaz")
    val internationalPrefix: String? = null,

    @SerialName("fax")
    val fax: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("nazioneOrdId")
    val orderNationId: Int? = null,

    @SerialName("nazioneOrdCod")
    val orderNationCode: String? = null,

    @SerialName("nazioneOrdDes")
    val orderNationDescription: String? = null,

    @SerialName("nazioneOrdCodFisc")
    val orderNationFiscalCode: String? = null,

    @SerialName("nazioneOrdMiurCod")
    val miurOrderNationCode: String? = null,

    @SerialName("codiceAteneo")
    val universityCode: String? = null,

    @SerialName("codicePic")
    val picCode: String? = null,

    @SerialName("codAteStra")
    val foreignAteCode: String? = null,

    @SerialName("codiceSchac")
    val schacCode: String? = null,

    @SerialName("dtIniVal")
    val initialValidityDate: String? = null,

    @SerialName("dtFinVal")
    val finalValidityDate: String? = null,

    @SerialName("iataCod")
    val iataCode: String? = null
)

@Serializable
data class Esse3HandicapDeclarationCompensatoryMeasures(
    @SerialName("dicHandId")
    val handicapDeclarationId: Long? = null,

    @SerialName("dicHandMisureId")
    val handicapDeclarationMeasuresId: Long? = null,

    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String? = null,

    @SerialName("misuraCompensativaDes")
    val compensatoryMeasureDescription: String? = null,

    @SerialName("desLiberaFlg")
    val freeDescriptionFlag: Int? = null,

    @SerialName("statoMisuraComp")
    val compensatoryMeasureState: String? = null,

    @SerialName("statoMisuraCompDes")
    val compensatoryMeasureStateDescription: String? = null,

    @SerialName("dataIni")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null,

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
data class Esse3HandicapDeclarationStates(
    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    @SerialName("statoDicHandDes")
    val handicapDeclarationStateDescription: String? = null
)

@Serializable
data class Esse3GetAuthorizationAttachmentMetadata(
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    @SerialName("dimensione")
    val size: Long? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("filename")
    val fileName: String? = null,

    @SerialName("estensione")
    val extension: String? = null,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int? = null,

    @SerialName("abilVisWeb")
    val webVisibility: Int? = null,

    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3Institute(
    @SerialName("scuolaSupId")
    val higherSchoolId: Long? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("tipologiaCod")
    val typologyCode: String? = null,

    @SerialName("tipologiaDes")
    val typologyDescription: String? = null,

    @SerialName("via")
    val street: String? = null,

    @SerialName("numeroCivico")
    val streetNumber: String? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("telefono")
    val phone: String? = null,

    @SerialName("fax")
    val fax: String? = null,

    @SerialName("localita")
    val locality: String? = null,

    @SerialName("codMiur")
    val miurCode: String? = null,

    @SerialName("comuneId")
    val municipalityId: Int? = null,

    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    @SerialName("comuneSigla")
    val municipalityAbbreviation: String? = null,

    @SerialName("comuneCodIstat")
    val municipalityIstatCode: String? = null,

    @SerialName("comuneCap")
    val municipalityPostalCode: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("codAteneo")
    val universityCode: String? = null,

    @SerialName("emailMinist")
    val ministryEmail: String? = null,

    @SerialName("codUniverso")
    val universeCode: String? = null,

    @SerialName("istRifId")
    val referenceInstitutionId: Int? = null,

    @SerialName("nuovoIstId")
    val newInstitutionId: Int? = null,

    @SerialName("tipiIstId")
    val institutionTypesId: Int? = null,

    @SerialName("tipiIstDes")
    val institutionTypesDescription: String? = null,

    @SerialName("scuolaNonStatFlg")
    val nonStatutorySchoolFlag: Int? = null,

    @SerialName("distretto")
    val district: String? = null,

    @SerialName("aaIniVal")
    val academicYearStartValidity: Int? = null,

    @SerialName("aaFineVal")
    val academicYearEndValidity: Int? = null,

    @SerialName("idScuolaMiur")
    val miurSchoolId: Int? = null,

    @SerialName("webFlg")
    val webFlag: Int? = null,

    @SerialName("noAggiornaFlg")
    val noUpdateFlag: Int? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("noteCronologia")
    val chronologyNotes: String? = null,

    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    @SerialName("codScuola")
    val schoolCode: String? = null,

    @SerialName("stataleFlg")
    val stateFlag: Int? = null,

    @SerialName("codiceScuolaRiferimento")
    val referenceSchoolCode: String? = null
)

@Serializable
data class Esse3ItalianTitlePerson(
    @SerialName("persId")
    val personId: String? = null,

    @SerialName("aaConsegTitolo")
    val academicYearTitleAward: Int? = null,

    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    @SerialName("appellativoF")
    val femaleTitle: String? = null,

    @SerialName("appellativoM")
    val maleTitle: String? = null,

    @SerialName("baseVoto")
    val baseGrade: Int? = null,

    @SerialName("certAns")
    val certAnswer: Long? = null,

    @SerialName("cfu")
    val credits: Float? = null,

    @SerialName("claAbCod")
    val abbreviatedClassCode: String? = null,

    @SerialName("confInvioOrdineFlg")
    val orderSendingConfirmationFlag: Int? = null,

    @SerialName("dataConsegTitolo")
    val titleDeliveryDate: String? = null,

    @SerialName("dataDomTiro")
    val internshipApplicationDate: String? = null,

    @SerialName("dataFineAttivita")
    val activityEndDate: String? = null,

    @SerialName("dataFinTiro")
    val internshipEndDate: String? = null,

    @SerialName("dataIniAttivita")
    val activityStartDate: String? = null,

    @SerialName("dataIniTiro")
    val internshipStartDate: String? = null,

    @SerialName("dataIscrOrdProf")
    val professionalOrderEnrollmentDate: String? = null,

    @SerialName("desCds")
    val courseOfStudyDescription: String? = null,

    @SerialName("desEstesa")
    val extendedDescription: String? = null,

    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    @SerialName("domRicoTitStraFlg")
    val domicileForeignTitleRecoveryFlag: Int? = null,

    @SerialName("indInvioRichConfTiro")
    val indexSendingInternshipConfirmationRequest: String? = null,

    @SerialName("iscrAlboFlg")
    val registerEnrollmentFlag: Int? = null,

    @SerialName("linguaDes")
    val languageDescription: String? = null,

    @SerialName("lode")
    val cumLaude: Int? = null,

    @SerialName("mediaVoti")
    val gradesAverage: Float? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("numAnniConseguimento")
    val achievementYearsNumber: Int? = null,

    @SerialName("ordine")
    val order: Long? = null,

    @SerialName("ordProfCod")
    val professionalOrderCode: String? = null,

    @SerialName("ordProfId")
    val professionalOrderId: Long? = null,

    @SerialName("percorsoDiStudio")
    val studyPath: String? = null,

    @SerialName("p01CdsAteneiItaCod")
    val p01ItalianUniversitiesCourseOfStudyCode: String? = null,

    @SerialName("p01CdsAteneiItaIstatCod")
    val p01ItalianUniversitiesCourseOfStudyIstatCode: String? = null,

    @SerialName("p03SesTestCod")
    val p03SessionTestCode: String? = null,

    @SerialName("p03SesTestDes")
    val p03SessionTestDescription: String? = null,

    @SerialName("p06AaDes")
    val p06AcademicYearDescription: String? = null,

    @SerialName("p06AteneiDes")
    val p06UniversitiesDescription: String? = null,

    @SerialName("p06AteneiIstatCod")
    val p06UniversitiesIstatCode: String? = null,

    @SerialName("p06CdsCod")
    val p06CourseOfStudyCode: String? = null,

    @SerialName("p06OrdProfDes")
    val p06ProfessionalOrderDescription: String? = null,

    @SerialName("p06SediDes")
    val p06SitesDescription: String? = null,

    @SerialName("p07ClaAbMiurDes")
    val p07AbbreviatedMiurClassDescription: String? = null,

    @SerialName("p07IndAbMiurDes")
    val p07AbbreviatedMiurAddressDescription: String? = null,

    @SerialName("p12DomCtDataDomCt")
    val p12CommitteeApplicationDate: String? = null,

    @SerialName("p12DomCtStato")
    val p12CommitteeApplicationState: String? = null,

    @SerialName("ricoTitStraFlg")
    val foreignTitleRecoveryFlag: Int? = null,

    @SerialName("rifFlg")
    val referenceFlag: Int? = null,

    @SerialName("sedeClaAbId")
    val abbreviatedClassSiteId: Long? = null,

    @SerialName("sedeDes")
    val siteDescription: String? = null,

    @SerialName("sessione")
    val session: String? = null,

    @SerialName("statiDomTiroDes")
    val internshipApplicationStatesDescription: String? = null,

    @SerialName("staTitItCod")
    val italianTitleStatusCode: String? = null,

    @SerialName("statiTitItDes")
    val italianTitleStatesDescription: String? = null,

    @SerialName("stessoAteneoFlg")
    val sameUniversityFlag: Long? = null,

    @SerialName("tipiDepositoDes")
    val depositTypesDescription: String? = null,

    @SerialName("tipiGiudProFinDes")
    val finalProJudgmentTypesDescription: String? = null,

    @SerialName("tipiRicoTitStraDes")
    val foreignTitleRecoveryTypesDescription: String? = null,

    @SerialName("tipiSdrDes")
    val siteTypesDescription: String? = null,

    @SerialName("tipiTirocDes")
    val internshipTypesDescription: String? = null,

    @SerialName("tipiTitItDes")
    val italianTitleTypesDescription: String? = null,

    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    @SerialName("tipoGiudProFinCod")
    val finalProjectJudgmentTypeCode: String? = null,

    @SerialName("tipoRicoTitStraCod")
    val foreignTitleRecoveryTypeCode: String? = null,

    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    @SerialName("tirocinioFlg")
    val internshipFlag: Int? = null,

    @SerialName("titAccAmm")
    val adminTitleAccess: Int? = null,

    @SerialName("titAccMat")
    val matTitleAccess: Int? = null,

    @SerialName("titAccMatStu")
    val studentMatTitleAccess: Long? = null,

    @SerialName("tititCod")
    val titleTypeCode: String? = null,

    @SerialName("titItId")
    val italianTitleId: Long? = null,

    @SerialName("titoloTesi")
    val thesisTitle: String? = null,

    @SerialName("valutatoFlg")
    val evaluatedFlag: Int? = null,

    @SerialName("vDecodeTititCodDes")
    val vDecodeTitleTypeCodeDescription: String? = null,

    @SerialName("voto")
    val grade: Float? = null,

    @SerialName("vStrutSdrCod")
    val vStructureSiteCode: String? = null,

    @SerialName("vStrutSdrDes")
    val vStructureSiteDescription: String? = null,

    @SerialName("vStrutSdrTip")
    val vStructureSiteType: String? = null
)

@Serializable
data class Esse3PutExternalSubject(
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    @SerialName("cognome")
    val surname: String,

    @SerialName("nome")
    val name: String,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("tipoSoggEstCod")
    val externalSubjectTypeCode: String? = null,

    @SerialName("sdrId")
    val siteId: Long? = null,

    @SerialName("naziNascId")
    val birthNationId: Long? = null,

    @SerialName("comNascId")
    val birthMunicipalityId: Long? = null,

    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    @SerialName("tel")
    val phone: String? = null,

    @SerialName("prefixInternaz")
    val internationalPrefix: String? = null,

    @SerialName("fax")
    val fax: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("appellativo")
    val title: String? = null,

    @SerialName("firmaId")
    val signatureId: Long? = null,

    @SerialName("dipId")
    val departmentId: Long? = null,

    @SerialName("nominativoAlt")
    val alternativeFullName: String? = null,

    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    @SerialName("operCellulare")
    val mobileOperator: Long? = null,

    @SerialName("consSmsFlg")
    val consentSmsFlag: Long,

    @SerialName("ateIdAccreditamento")
    val ateAccreditationId: Long? = null,

    @SerialName("dataIniAtt")
    val activityStartDate: String? = null,

    @SerialName("dataFinAtt")
    val activityEndDate: String? = null
)

@Serializable
data class Esse3PersonalDataConsentsHistory(
    @SerialName("tipoConsensoCod")
    val consentTypeCode: String? = null,

    @SerialName("tipiConsensoDes")
    val consentTypesDescription: String? = null,

    @SerialName("stoId")
    val historicalId: Long? = null,

    @SerialName("tipiConsensoEtichetta")
    val consentTypesLabel: String? = null,

    @SerialName("consensoFlg")
    val consentFlag: Int? = null,

    @SerialName("dataIni")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("procAmmCod")
    val administrativeProcedureCode: String? = null,

    @SerialName("procAmmDes")
    val administrativeProcedureDescription: String? = null,

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
data class Esse3Tutors(
    @SerialName("anaperTutoreId")
    val personGuardianId: Long? = null,

    @SerialName("autorizzatoId")
    val authorizedId: Long? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("stato")
    val state: String? = null,

    @SerialName("statoDes")
    val stateDescription: String? = null,

    @SerialName("tipoParCod")
    val paragraphTypeCode: String? = null,

    @SerialName("tipoParDes")
    val paragraphTypeDescription: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Long? = null,

    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("nazioneId")
    val nationId: Int? = null,

    @SerialName("naziCod")
    val nationCode: String? = null,

    @SerialName("naziDes")
    val nationDescription: String? = null,

    @SerialName("naziNazioneCod")
    val nationNationCode: String? = null,

    @SerialName("naziCodInt")
    val nationInternationalCode: String? = null,

    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    @SerialName("comuneId")
    val municipalityId: Long? = null,

    @SerialName("comuCod")
    val municipalityCode: String? = null,

    @SerialName("comuCodCatastale")
    val municipalityCadastralCode: String? = null,

    @SerialName("comuCodIstatMiur")
    val municipalityMiurIstatCode: String? = null,

    @SerialName("comuDes")
    val municipalityDescription: String? = null,

    @SerialName("comuSigla")
    val municipalityAbbreviation: String? = null,

    @SerialName("provDes")
    val provinceDescription: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("autExtCod")
    val externalAuthorizationCode: String? = null
)

@Serializable
data class Esse3PhDCareer(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailAte")
    val ateEmail: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("aaDes")
    val academicYearDescription: String? = null,

    @SerialName("dataImm")
    val matriculationDate: String? = null,

    @SerialName("statiStuDes")
    val studentStatesDescription: String? = null,

    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    @SerialName("sedeId")
    val siteId: Int? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    @SerialName("sospFlg")
    val suspensionFlag: Int? = null,

    @SerialName("sediDes")
    val sitesDescription: String? = null,

    @SerialName("p06CdsCod")
    val p06CourseOfStudyCode: String? = null,

    @SerialName("p06CdsDes")
    val p06CourseOfStudyDescription: String? = null,

    @SerialName("p06CdsordCod")
    val p06CourseOfStudyOrderCode: String? = null,

    @SerialName("p06CdsordDes")
    val p06CourseOfStudyOrderDescription: String? = null,

    @SerialName("p06PdsordCod")
    val p06StudyPlanOrderCode: String? = null,

    @SerialName("p06PdsordDes")
    val p06StudyPlanOrderDescription: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("facCsaCod")
    val facultyCsaCode: String? = null,

    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    @SerialName("profCod")
    val professionCode: String? = null,

    @SerialName("tipiProfstuDes")
    val studentProfessionTypesDescription: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null
)

@Serializable
data class Esse3Tutor(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("autorizzatoId")
    val authorizedId: Long? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("comuneId")
    val municipalityId: Long? = null,

    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    @SerialName("tipoParCod")
    val paragraphTypeCode: String? = null,

    @SerialName("tipoParDes")
    val paragraphTypeDescription: String? = null,

    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Long? = null,

    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    @SerialName("nazioneId")
    val nationId: Long? = null,

    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("autExtCod")
    val externalAuthorizationCode: String? = null,

    @SerialName("regTutoriTstCod")
    val tutorsTestRegistrationCode: String? = null,

    @SerialName("regTutoriTstDes")
    val tutorsTestRegistrationDescription: String? = null,

    @SerialName("regTutoriDettCod")
    val tutorsDetailRegistrationCode: String? = null,

    @SerialName("regTutoriDettDes")
    val tutorsDetailRegistrationDescription: String? = null,

    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    @SerialName("stato")
    val state: String? = null
)

@Serializable
data class Esse3HigherInstituteTypes(
    @SerialName("tipologiaCod")
    val typologyCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    @SerialName("almaCod")
    val almaCode: Int? = null,

    @SerialName("tipoScuolaMiurCod")
    val miurSchoolTypeCode: String? = null
)

@Serializable
data class Esse3PersonalDataExternalCode(
    @SerialName("tipoCodExt")
    val externalTypeCode: String? = null,

    @SerialName("tipoCodExtDes")
    val externalTypeDescription: String? = null,

    @SerialName("tipoCodExtNota")
    val externalTypeNote: String? = null,

    @SerialName("codExt")
    val externalCode: String? = null,

    @SerialName("ateneoId")
    val universityId: Long? = null,

    @SerialName("ateneoIstatCod")
    val universityIstatCode: String? = null,

    @SerialName("ateneoCodeUn")
    val universityUnifiedCode: String? = null,

    @SerialName("ateneoDes")
    val universityDescription: String? = null,

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
data class Esse3ConsentsParameters(
    @SerialName("tipoConsensoCod")
    val consentTypeCode: String,

    @SerialName("consensoFlg")
    val consentFlag: Int,

    @SerialName("dataIni")
    val startDate: String? = null
)

@Serializable
data class Esse3AttachmentsOperationsResult(
    @SerialName("retCode")
    val returnCode: Int? = null,

    @SerialName("retErrMsg")
    val returnErrorMessage: String? = null
)

@Serializable
data class Esse3PhDProgramCareer(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailAte")
    val ateEmail: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("aaDes")
    val academicYearDescription: String? = null,

    @SerialName("dataImm")
    val matriculationDate: String? = null,

    @SerialName("statiStuDes")
    val studentStatesDescription: String? = null,

    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    @SerialName("sedeId")
    val siteId: Int? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    @SerialName("sospFlg")
    val suspensionFlag: Int? = null,

    @SerialName("sediDes")
    val sitesDescription: String? = null,

    @SerialName("p06CdsCod")
    val p06CourseOfStudyCode: String? = null,

    @SerialName("p06CdsDes")
    val p06CourseOfStudyDescription: String? = null,

    @SerialName("p06CdsordCod")
    val p06CourseOfStudyOrderCode: String? = null,

    @SerialName("p06CdsordDes")
    val p06CourseOfStudyOrderDescription: String? = null,

    @SerialName("p06PdsordCod")
    val p06StudyPlanOrderCode: String? = null,

    @SerialName("p06PdsordDes")
    val p06StudyPlanOrderDescription: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("facCsaCod")
    val facultyCsaCode: String? = null,

    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    @SerialName("profCod")
    val professionCode: String? = null,

    @SerialName("tipiProfstuDes")
    val studentProfessionTypesDescription: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("coordinatore")
    val coordinator: List<Esse3Coordinator> = emptyList(),

    @SerialName("supervisoreTutoreDottorato")
    val phdSupervisorTutor: List<Esse3PhDSupervisorTutor> = emptyList(),

    @SerialName("periodoStudioEstero")
    val foreignStudyPeriod: List<Esse3ForForeignStudent> = emptyList(),

    @SerialName("prorogaTesi")
    val thesisExtension: List<Esse3ThesisExtension> = emptyList(),

    @SerialName("sospensioni")
    val suspensions: List<Esse3Suspensions> = emptyList()
)
