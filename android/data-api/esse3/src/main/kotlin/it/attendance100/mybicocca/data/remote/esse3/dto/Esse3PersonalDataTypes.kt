package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3GetEnrollmentNumberAttachmentMetadata(
    /** codice tipo associativa allegato */
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    /** identificativo allegato */
    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    /** dimensione allegato in byte */
    @SerialName("dimensione")
    val size: Long? = null,

    /** titolo allegato */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione allegato */
    @SerialName("des")
    val description: String? = null,

    /** nome file */
    @SerialName("filename")
    val fileName: String? = null,

    /** estensione file */
    @SerialName("estensione")
    val extension: String? = null,

    /** autore allegato */
    @SerialName("autore")
    val author: String? = null,

    /** data inserimento allegato */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data ultima modifica allegato */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** codice tipologia allegato */
    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    /** flag che indica se l'allegato risulta validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int? = null,

    /** flag che indica se l'allegato risulta visibile da web o meno */
    @SerialName("abilVisWeb")
    val webVisibility: Int? = null,

    /** flag che indica se l'allegato risulta stampabile o meno */
    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3AcademicYearLookup(
    /** anno accademico */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** descrizione anno accademico */
    @SerialName("des")
    val description: String? = null,

    /** Data di inizio */
    @SerialName("dataInizio")
    val startDate: String? = null,

    /** Data di fine */
    @SerialName("dataFine")
    val endDate: String? = null
)

@Serializable
data class Esse3ExternalSubjectsConsentsReplica(
    /** Identificativo soggetto esterno. */
    @SerialName("soggEstId")
    val externalSubjectId: Int? = null,

    /** Codice tipo consenso richiesto. */
    @SerialName("tipoConsensoCod")
    val consentTypeCode: String? = null,

    /** Descrizione in lingua tipo consenso. */
    @SerialName("tipoConsensoDes")
    val consentTypeDescription: String? = null,

    /** Etichetta da visualizzare on-line. */
    @SerialName("tipiConsensoEtichetta")
    val consentTypesLabel: String? = null,

    /** Indica se il consenso è stato dato. */
    @SerialName("consensoFlg")
    val consentFlag: Int? = null,

    /** Data inizio consenso o negazione del consenso. */
    @SerialName("dataIni")
    val startDate: String? = null,

    /** Processo amministrativo in cui è stato modificato il consenso. */
    @SerialName("procAmmCod")
    val administrativeProcedureCode: String? = null,

    /** Descrizione Processo amministrativo in cui è stato modificato il consenso. */
    @SerialName("procAmmDes")
    val administrativeProcedureDescription: String? = null,

    /** id utente di inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** id utente di ultima  modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null
)

@Serializable
data class Esse3PersonalDocument(
    /** Identificativo documento persona */
    @SerialName("docPersId")
    val personalDocumentId: Long? = null,

    /** Codice tipo documento identificativo */
    @SerialName("docIdentTipoCod")
    val identityDocumentTypeCode: String? = null,

    /** Descrizione tipo documento identificativo */
    @SerialName("docIdentTipoDes")
    val identityDocumentTypeDescription: String? = null,

    /** Numero documento */
    @SerialName("num")
    val number: String? = null,

    /** Data rilascio documento */
    @SerialName("dataRilascio")
    val releaseDate: String? = null,

    /** Data scadenza documento */
    @SerialName("dataScadenza")
    val expirationDate: String? = null,

    /** Ente rilascio documento */
    @SerialName("enteRilascio")
    val issuingEntity: String? = null,

    /** Stato documento persona */
    @SerialName("statoDocPers")
    val personalDocumentState: String? = null,

    /** Descrizione stato documento persona */
    @SerialName("statoDocPersDes")
    val personalDocumentStateDescription: String? = null,

    /** Identificativo nazione emissione */
    @SerialName("nazioneEmissioneId")
    val issuanceNationId: Long? = null,

    /** Codice fiscale nazione emissione */
    @SerialName("nazioneEmissioneCodFis")
    val issuanceNationFiscalCode: String? = null,

    /** Descrizione nazione emissione */
    @SerialName("nazioneEmissioneDes")
    val issuanceNationDescription: String? = null,

    /** Codice nazione emissione */
    @SerialName("nazioneEmissioneNazioneCod")
    val issuanceNationCode: String? = null,

    /** Codice internazionale nazione emissione */
    @SerialName("nazioneEmissioneCodInt")
    val issuanceNationInternationalCode: String? = null,

    /** Identificativo comune emissione */
    @SerialName("comuneEmissioneId")
    val issuanceMunicipalityId: Long? = null,

    /** Codice comune emissione */
    @SerialName("comuneEmissioneCod")
    val issuanceMunicipalityCode: String? = null,

    /** Codice catastale comune emissione */
    @SerialName("comuneEmissioneCodCatastale")
    val issuanceMunicipalityCadastralCode: String? = null,

    /** Codice ISTAT/MIUR comune emissione */
    @SerialName("comuneEmissioneIstatMiur")
    val issuanceMunicipalityMiurIstat: String? = null,

    /** Descrizione comune emissione */
    @SerialName("comuneEmissioneDes")
    val issuanceMunicipalityDescription: String? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null
)

@Serializable
data class Esse3PersonCommonRegistry(
    /** id univoco che consente di individuare la persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** id univoco che consente di individuare la persona */
    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    /** Codice identificativo dell'anagrafica */
    @SerialName("persCod")
    val personCode: String? = null,

    /** cognome della persona */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome della persona */
    @SerialName("nome")
    val name: String? = null,

    /** Nominativo del genitore */
    @SerialName("patronimico")
    val patronymic: String? = null,

    /** data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** identificativo comune nascita */
    @SerialName("comuNascId")
    val birthMunicipalityId: Long? = null,

    /** codice comune nascita */
    @SerialName("comuNascCod")
    val birthMunicipalityCode: String? = null,

    /** codice catastale del comune di nascita */
    @SerialName("comuNascCodCatastale")
    val birthMunicipalityCadastralCode: String? = null,

    /** codice istat comune nascita */
    @SerialName("comuNascCodIstatMiur")
    val birthMunicipalityMiurIstatCode: String? = null,

    /** denominazione del comune di nascita */
    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    /** identificativo nazione nascita */
    @SerialName("naziNascId")
    val birthNationId: Long? = null,

    /** codice nazione nascita */
    @SerialName("naziNascCod")
    val birthNationCode: String? = null,

    /** denominazione del luogo di nascita se nazione diversa da italia */
    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    /** sigla della provincia di nascita */
    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    /** denominazione della provincia di nascita */
    @SerialName("provNascDes")
    val birthProvinceDescription: String? = null,

    /** codice della nazione di nascita */
    @SerialName("naziNascNazioneCod")
    val birthCountryCode: String? = null,

    /** denominazione della nazione di nascita */
    @SerialName("naziNascDes")
    val birthNationDescription: String? = null,

    /** codice 3 numeri della nazione di nascita */
    @SerialName("naziNascCodInt")
    val birthNationInternationalCode: String? = null,

    /** codice fiscale della personae */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** codice di errore nel controllo del CF */
    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Int? = null,

    /** sesso della persona */
    @SerialName("sesso")
    val gender: String? = null,

    /** id univoco che consente di individuare l'account utente */
    @SerialName("userId")
    val userId: String? = null,

    /** id univoco che consente di individuare la foto associata alla  persona */
    @SerialName("fotoId")
    val photoId: Long? = null,

    /** codice catastale della nazione di residenza */
    @SerialName("naziResCod")
    val residenceNationCode: String? = null,

    /** nazione di residenza */
    @SerialName("naziResDes")
    val residenceNationDescription: String? = null,

    /** codice ISTAT del comune di residenza */
    @SerialName("comuResCod")
    val residenceMunicipalityCode: String? = null,

    /** comune di residenza */
    @SerialName("comuResDes")
    val residenceMunicipalityDescription: String? = null,

    /** sigla della provincia  di residenza */
    @SerialName("comuResSigla")
    val residenceMunicipalityAbbreviation: String? = null,

    /** via di residenza */
    @SerialName("viaRes")
    val residenceStreet: String? = null,

    /** numero civico di residenza */
    @SerialName("numCivRes")
    val residenceStreetNumber: String? = null,

    /** CAP di residenza */
    @SerialName("capRes")
    val residencePostalCode: String? = null,

    /** Telefono di residenza */
    @SerialName("telRes")
    val residencePhone: String? = null,

    /** codice catastale della nazione di domicilio */
    @SerialName("naziDomCod")
    val domicileNationCode: String? = null,

    /** nazione di domicilio */
    @SerialName("naziDomDes")
    val domicileNationDescription: String? = null,

    /** codice ISTAT del comune di domicilio */
    @SerialName("comuDomCod")
    val domicileMunicipalityCode: String? = null,

    /** comune di domicilio */
    @SerialName("comuDomDes")
    val domicileMunicipalityDescription: String? = null,

    /** sigla della provincia  di domicilio */
    @SerialName("comuDomSigla")
    val domicileMunicipalityAbbreviation: String? = null,

    /** via di domicilio */
    @SerialName("viaDom")
    val domicileStreet: String? = null,

    /** numero civico di domicilio */
    @SerialName("numCivDom")
    val domicileStreetNumber: String? = null,

    /** CAP di domicilio */
    @SerialName("capDom")
    val domicilePostalCode: String? = null,

    /** Telefono di domicilio */
    @SerialName("telDom")
    val domicilePhone: String? = null,

    /** email personale */
    @SerialName("email")
    val email: String? = null,

    /** email di ateneo */
    @SerialName("emailAte")
    val universityEmail: String? = null,

    /** data di inserimento (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data di modifica (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Codice Cittadinanza */
    @SerialName("codCittadinanza")
    val citizenshipCode: String? = null,

    /** Descrizione cittadinanza */
    @SerialName("desCittadinanza")
    val citizenshipDescription: String? = null,

    /** numero di cellulare */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** Indicatore di Permesso di soggiorno scaduto */
    @SerialName("permsogScadutoFlg")
    val authorizedSubjectExpiredFlag: Long? = null,

    /** Indicatore di Permesso di soggiorno caricato */
    @SerialName("presenzaPermSogFlg")
    val authorizedSubjectPresenceFlag: Long? = null,

    /** Data scadenza dell'ultimo Permesso di soggiorno caricato (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("permsogDataScad")
    val authorizedSubjectExpirationDate: String? = null,

    /** usato per inserimento iscrizioni pregresso, indica se il dato è certificato dall'operatore di segreteria. 0 - inserite dallo studente, 1- certificato dall'utente di segreteria. */
    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    /** ID della nazione di residenza. */
    @SerialName("naziResId")
    val residenceNationId: Long? = null,

    /** Codice della nazione di residenza. */
    @SerialName("naziResNazioneCod")
    val residenceCountryCode: String? = null,

    /** Codice internazionale della nazione di residenza. */
    @SerialName("naziResCodInt")
    val residenceNationInternationalCode: String? = null,

    /** ID del comune di residenza. */
    @SerialName("comuResId")
    val residenceMunicipalityId: Long? = null,

    /** Codice catastale del comune di residenza. */
    @SerialName("comuResCodCatastale")
    val residenceMunicipalityCadastralCode: String? = null,

    /** Codice ISTAT/MIUR del comune di residenza. */
    @SerialName("comuResCodIstatMiur")
    val residenceMunicipalityMiurIstatCode: String? = null,

    /** Descrizione della provincia di residenza. */
    @SerialName("provResDes")
    val residenceProvinceDescription: String? = null,

    /** Città straniera di residenza. */
    @SerialName("citstraRes")
    val residenceForeignCity: String? = null,

    /** Frazione di residenza. */
    @SerialName("frazRes")
    val residenceFraction: String? = null,

    /** Prefisso telefonico internazionale della nazione di residenza. */
    @SerialName("prefixInternazRes")
    val residenceInternationalPrefix: String? = null,

    /** Data inizio validità residenza. */
    @SerialName("dataIniValRes")
    val residenceEvaluationStartDate: String? = null,

    /** Flag che indica se il domicilio coincide con la residenza. */
    @SerialName("domComeResFlg")
    val domicileSameAsResidenceFlag: Int? = null,

    /** ID della nazione di domicilio. */
    @SerialName("naziDomId")
    val domicileNationId: Int? = null,

    /** Codice della nazione di domicilio. */
    @SerialName("naziDomNazioneCod")
    val domicileCountryCode: String? = null,

    /** Codice internazionale della nazione di domicilio. */
    @SerialName("naziDomCodInt")
    val domicileNationInternationalCode: String? = null,

    /** ID del comune di domicilio. */
    @SerialName("comuDomId")
    val domicileMunicipalityId: Long? = null,

    /** Codice catastale del comune di domicilio. */
    @SerialName("comuDomCodCatastale")
    val domicileMunicipalityCadastralCode: String? = null,

    /** Codice ISTAT/MIUR del comune di domicilio. */
    @SerialName("comuDomCodIstatMiur")
    val domicileMunicipalityMiurIstatCode: String? = null,

    /** Città straniera di domicilio. */
    @SerialName("citstraDom")
    val domicileForeignCity: String? = null,

    /** Frazione di domicilio. */
    @SerialName("frazDom")
    val domicileFraction: String? = null,

    /** Prefisso telefonico internazionale della nazione di domicilio. */
    @SerialName("prefixInternazDom")
    val domicileInternationalPrefix: String? = null,

    /** Indicazione del presso. */
    @SerialName("cO")
    val co: String? = null,

    /** Data di inizio validità del domicilio. */
    @SerialName("dataIniValDom")
    val domicileEvaluationStartDate: String? = null,

    /** Viene valorizzato solo nel caso in cui sia stato introdotto un domicilio valido (a livello di tempo). */
    @SerialName("tipoIndirizCod")
    val addressTypeCode: String? = null,

    /** Codice del recapito della tasse. */
    @SerialName("recapitoTasse")
    val taxesContact: String? = null,

    /** Indica la tipologia di indirizzo selezionato per il recapito badge. */
    @SerialName("recapitoBadge")
    val badgeContact: String? = null,

    /** Numero fax. */
    @SerialName("fax")
    val fax: String? = null,

    /** Email studente certificata. */
    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    /** Codice della nazione della prima cittadinanza. */
    @SerialName("citt1NazioneCod")
    val citizenship1CountryCode: String? = null,

    /** Data di inizio validità della prima cittadinanza. */
    @SerialName("citt1Dataini")
    val citizenship1StartDate: String? = null,

    /** Data di fine validità della prima cittadinanza. */
    @SerialName("citt1Datafin")
    val citizenship1EndDate: String? = null,

    /** Codice della seconda cittadinanza. */
    @SerialName("citt2Cod")
    val citizenship2Code: String? = null,

    /** Descrizione della seconda cittadinanza. */
    @SerialName("citt2Des")
    val citizenship2Description: String? = null,

    /** Codice della nazione della seconda cittadinanza. */
    @SerialName("citt2NazioneCod")
    val citizenship2CountryCode: String? = null,

    /** Data di inizio validità della seconda cittadinanza. */
    @SerialName("citt2Dataini")
    val citizenship2StartDate: String? = null,

    /** Data di fine validità della seconda cittadinanza. */
    @SerialName("citt2Datafin")
    val citizenship2EndDate: String? = null,

    /** Codice della terza cittadinanza. */
    @SerialName("citt3Cod")
    val citizenship3Code: String? = null,

    /** Descrizione della terza cittadinanza. */
    @SerialName("citt3Des")
    val citizenship3Description: String? = null,

    /** Codice della nazione della terza cittadinanza. */
    @SerialName("citt3NazioneCod")
    val citizenship3CountryCode: String? = null,

    /** Data di inizio validità della terza cittadinanza. */
    @SerialName("citt3Dataini")
    val citizenship3StartDate: String? = null,

    /** Data di fine validità della terza cittadinanza. */
    @SerialName("citt3Datafin")
    val citizenship3EndDate: String? = null,

    /** Codice internazionale della nazione di cittadinanza principale. */
    @SerialName("naziCittadCodInt")
    val citizenshipNationInternationalCode: String? = null,

    /** Descrizione della nazione di cittadinanza principale. */
    @SerialName("naziCittadDes")
    val citizenshipNationDescription: String? = null,

    /** Prefisso del numero di cellulare. */
    @SerialName("prefixCell")
    val mobilePrefix: String? = null,

    /** Consenso per il trattamento dei dati personali. */
    @SerialName("consDpFlg")
    val consentDpFlag: Int? = null,

    /** Flag di consenso per la diffusione dei dati personali. */
    @SerialName("consDiffDpFlg")
    val consentDifferentDpFlag: Int? = null,

    /** Consenso alla notifica tramite messaggistica SMS. */
    @SerialName("consSmsFlg")
    val consentSmsFlag: Int? = null,

    /** Consenso alla comunicazione dei dati personali. */
    @SerialName("consComunicDpFlg")
    val consentCommunicationDpFlag: Int? = null,

    /** Consenso invio dei dati all'ente regionale. */
    @SerialName("consComunicErFlg")
    val consentCommunicationErFlag: Int? = null,

    /** Flag che indica se lo studente appartiene al clero. */
    @SerialName("religiosoFlg")
    val religiousFlag: Int? = null,

    /** Indica se la persona è deceduta. */
    @SerialName("decedutoFlg")
    val deceasedFlag: Int? = null,

    /** Codice esterno di identificazione della persona. */
    @SerialName("extPersCod")
    val externalPersonCode: String? = null,

    /** Nota persona. */
    @SerialName("notaPers")
    val personalNote: String? = null,

    /** Descrizione della professione. */
    @SerialName("professione")
    val profession: String? = null,

    /** Codice dello stato civile. */
    @SerialName("statoCivileCod")
    val maritalStatusCode: String? = null,

    /** Descrizione dello stato civile. */
    @SerialName("statoCivileDes")
    val maritalStatusDescription: String? = null,

    /** Nome di una persona da contattare in caso di emergenza. */
    @SerialName("emergNome")
    val emergencyName: String? = null,

    /** Cognome di una persona da contattare in caso di emergenza. */
    @SerialName("emergCognome")
    val emergencySurname: String? = null,

    /** Telefono di una persona da contattare in caso di emergenza. */
    @SerialName("emergTel")
    val emergencyPhone: String? = null,

    /** Prefisso telefonico internazionale di una persona da contattare in caso di emergenza.. */
    @SerialName("emergPrefixInternaz")
    val emergencyInternationalPrefix: String? = null,

    /** Indirizzo e-mail di una persona da contattare in caso di emergenza. */
    @SerialName("emergEmail")
    val emergencyEmail: String? = null,

    /** Rapporto con persona da contattare in caso di emergenza. */
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
    /** ID della persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** Progressivo di dichiarazione dell´handicap inoltrata dallo studente. */
    @SerialName("dichiarId")
    val declarationId: Long? = null,

    /** Tipo di handicap */
    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    /** Descrizione Tipo di handicap */
    @SerialName("tipiHandicapDes")
    val handicapTypesDescription: String? = null,

    /** Numero compreso tra 0 e 100 che riporta la percentuale di handicap dello studente. */
    @SerialName("percHand")
    val handicapPercentage: Int? = null,

    /** Data della dichiarazione */
    @SerialName("dataDichiar")
    val declarationDate: String? = null,

    /** Codice dello stato della dichiarazione di handicap. */
    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    /** Descrizione dello stato della dichiarazione di handicap. */
    @SerialName("statiDicHandDes")
    val handicapDeclarationStatesDescription: String? = null,

    /** Data di inizio validità dello stato attribuito alla dichiarazione di handicap. */
    @SerialName("dataIniStato")
    val stateStartDate: String? = null,

    /** Indica se la persona richiede assistenza e/o servizi di tutorato specializzato. */
    @SerialName("tutoratoFlg")
    val tutoringFlag: Int? = null,

    /** Indica l´autorizzazione della persona a farsi contattare direttamente al recapito indicato per l´offerta di assistenza. */
    @SerialName("autTutorFlg")
    val tutorAuthorizationFlag: Int? = null,

    /** Data di inizio invalidità. */
    @SerialName("dataIni")
    val startDate: String? = null,

    /** Data fine invalidità. */
    @SerialName("dataFine")
    val endDate: String? = null,

    /** Anno accademico di inizio competenza. */
    @SerialName("aaIdCompIni")
    val academicYearComponentStartId: Long? = null,

    /** Anno accademico di fine competenza. */
    @SerialName("aaIdCompFine")
    val academicYearComponentEndId: Long? = null,

    /** Nota. */
    @SerialName("nota")
    val note: String? = null,

    /** Consenso al trattamento dei dati sensibili. */
    @SerialName("consDsFlg")
    val consentDsFlag: Int? = null,

    /** Codice normative associabili alle dichiarazioni di handicap. */
    @SerialName("handNormativaCod")
    val handicapRegulationCode: String? = null,

    /** Descrizione delle normative associabili alle dichiarazioni di handicap. */
    @SerialName("p01HandNormativaDes")
    val p01HandicapRegulationDescription: String? = null,

    /** Indica se il profilo BES è completo. */
    @SerialName("besCheckFlg")
    val besCheckFlag: Int? = null,

    /** ID dichiarazione di invalidità */
    @SerialName("dicHandId")
    val handicapDeclarationId: Int? = null,

    /** ID dichiarazione di invalidità */
    @SerialName("misureComp")
    val compensatoryMeasures: Int? = null
)

@Serializable
data class Esse3PhoneParameters(
    /** numero di telefono */
    @SerialName("numTelefono")
    val phoneNumber: String? = null,

    /** prefisso */
    @SerialName("prefix")
    val prefix: String? = null
)

@Serializable
data class Esse3HandicapDeclarationToValidate(
    /** userId attivo collegato al docente */
    @SerialName("userId")
    val userId: String? = null,

    /** ID user. */
    @SerialName("id")
    val id: Int? = null,

    /** ID dichiarazione di invalidità */
    @SerialName("dicHandId")
    val handicapDeclarationId: Int? = null,

    /** id univoco che consente di individuare la persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** cognome dell'utente */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome dell'utente */
    @SerialName("nome")
    val name: String? = null,

    /** codice fiscale dell'utente */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Data di nascita del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** Tipo di handicap */
    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    /** Descrizione Tipo di handicap */
    @SerialName("tipoHandicapDes")
    val handicapTypeDescription: String? = null,

    /** Numero compreso tra 0 e 100 che riporta la percentuale di handicap dello studente. */
    @SerialName("percHand")
    val handicapPercentage: Int? = null,

    /** Codice dello stato della dichiarazione di handicap. */
    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    /** Descrizione dello stato della dichiarazione di handicap. */
    @SerialName("statoDicHandDes")
    val handicapDeclarationStateDescription: String? = null,

    /** Data di inizio invalidità. */
    @SerialName("dataIni")
    val startDate: String? = null,

    /** Data fine invalidità. */
    @SerialName("dataFine")
    val endDate: String? = null,

    /** Anno accademico di inizio competenza. */
    @SerialName("aaIdCompIni")
    val academicYearComponentStartId: Long? = null,

    /** Anno accademico di fine competenza. */
    @SerialName("aaIdCompFine")
    val academicYearComponentEndId: Long? = null,

    /** Data della dichiarazione */
    @SerialName("dataDichiar")
    val declarationDate: String? = null,

    /** Indica l´autorizzazione della persona a farsi contattare direttamente al recapito indicato per l´offerta di assistenza. */
    @SerialName("autTutorFlg")
    val tutorAuthorizationFlag: Int? = null,

    /** Indica se la persona richiede assistenza e/o servizi di tutorato specializzato. */
    @SerialName("tutoratoFlg")
    val tutoringFlag: Int? = null,

    /** id allegato tabella p17_dic_hand_all */
    @SerialName("allegati")
    val attachments: Int? = null,

    /** id misure compensative tabella p01_dic_hand_misure_comp */
    @SerialName("misureComp")
    val compensatoryMeasures: Int? = null,

    /** id bisogni speciali P01_DIC_HAND_BISOGNI_SPECIALI */
    @SerialName("bes")
    val bes: Int? = null,

    /** Nota. */
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
    val modificationDate: String? = null,

    /** Data di fine validità. */
    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    /** Indica se il record è abilitato pper la valutazione. */
    @SerialName("abilFlg")
    val authorizationFlag: Int? = null
)

@Serializable
data class Esse3HighSchoolGradeRange(
    /** Anno di inizio validità. */
    @SerialName("annoDa")
    val yearFrom: Int? = null,

    /** Anno di fine validità. */
    @SerialName("annoA")
    val yearTo: Int? = null,

    /** Voto minimo titolo di scuola superiore. */
    @SerialName("votoMin")
    val minGrade: Int? = null,

    /** Voto massimo titolo di scuola superiore. */
    @SerialName("votoMax")
    val maxGrade: Int? = null,

    /** Lode abilitata. */
    @SerialName("lodeFlg")
    val cumLaudeFlag: Int? = null
)

@Serializable
data class Esse3AttachmentMetadataInfo(
    /** nome del file */
    @SerialName("filename")
    val fileName: String = "",

    /** autore del file */
    @SerialName("autore")
    val author: String? = null,

    /** titolo del file */
    @SerialName("titolo")
    val title: String = "",

    /** descrizione libera */
    @SerialName("descrizione")
    val description: String = "",

    /** tipologia dell'allegato relativo all'entità p17_tipologia_allegati */
    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    /** flag che indica se l'allegato deve già risultare validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int = 0
)

@Serializable
data class Esse3MobileParameter(
    /** cellulare */
    @SerialName("cellulare")
    val mobilePhone: String? = null
)

@Serializable
data class Esse3ExtendedPerson(
    /** id univoco che consente di individuare la persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** cognome della persona */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome della persona */
    @SerialName("nome")
    val name: String? = null,

    /** data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** codice fiscale della personae */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** sesso della persona */
    @SerialName("sesso")
    val gender: String? = null,

    /** id univoco che consente di individuare la foto associata alla  persona */
    @SerialName("fotoId")
    val photoId: Long? = null,

    /** email personale */
    @SerialName("email")
    val email: String? = null,

    /** email di ateneo */
    @SerialName("emailAte")
    val universityEmail: String? = null,

    /** numero di cellulare */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** cognome della persona normalizzato ( di accenti e letterer strane) */
    @SerialName("cognomeNormalizzato")
    val normalizedSurname: String? = null,

    /** nome della persona normalizzato ( di accenti e letterer strane) */
    @SerialName("nomeNormalizzato")
    val normalizedName: String? = null
)

@Serializable
data class Esse3ForeignTitlesEnrollment(
    /** Data di conseguimento del titolo */
    @SerialName("dataConsegTitolo")
    val titleDeliveryDate: String? = null,

    /** codice Tipo di titolo straniero ad esempio  MS Master BA Bachelor LIC Licence etc */
    @SerialName("tipoTitstCod")
    val titleStatusTypeCode: String? = null,

    /** Elenco codifica atenei stranieri */
    @SerialName("ateneoStranieroErasmusCod")
    val foreignUniversityErasmusCode: String? = null,

    /** Descrizione del Corso di studio straniero */
    @SerialName("cdsStraniero")
    val foreignCourseOfStudy: String? = null,

    /** Durata legale in anni del corso */
    @SerialName("durataAnni")
    val durationYears: Long? = null,

    /** Voto conseguito I campi Voto e Voto_Alfanumerico sono mutuamente esclusivi */
    @SerialName("voto")
    val grade: Float? = null,

    /** base del voto */
    @SerialName("votoBase")
    val baseGrade: Long? = null,

    /** Flag che indica se è stata conseguita la lode. 0 se non c’è il voto */
    @SerialName("lode")
    val cumLaude: Long? = null,

    /** Voto conseguito espresso in lettere. */
    @SerialName("votoAlfanumerico")
    val alphanumericGrade: String? = null,

    /** Flag che Attesta se è stata depositata la Dichiarazione di valore del titolo di studio */
    @SerialName("dichiarazValoreFlg")
    val valueDeclarationFlag: Long? = null,

    /** Indica in quale forma è stato depositato il titolo. Per esempio O originale, CS copia , AUT autocertificazione CA copia autenticata S sostitutivo A in ateneo REG registrazione atto CER certificato. */
    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    /** Appellativo femminile del titolo */
    @SerialName("appellativoF")
    val femaleTitle: String? = null,

    /** Appellativo maschile del titolo */
    @SerialName("appellativoM")
    val maleTitle: String? = null,

    /** Descrizione libera titolo di studio straniero */
    @SerialName("desTitolo")
    val titleDescription: String? = null,

    /** Stato del  titolo- C Conseguito, I In ipotesi */
    @SerialName("staTitStraCod")
    val foreignTitleStatusCode: String = "",

    /** Identificativo della nazione in cui è stato conseguito il titolo */
    @SerialName("naziCodFis")
    val nationFiscalCode: String? = null,

    /** Anno Accademico di conseguimento del titolo */
    @SerialName("aaConsegId")
    val academicYearAwardId: Long? = null,

    /** città straniera di conseguimento */
    @SerialName("citstraConseg")
    val deliveryForeignCity: String? = null,

    /** Voto minimo */
    @SerialName("votoMin")
    val minGrade: Float? = null,

    /** Voto convertito */
    @SerialName("votoConvertito")
    val convertedGrade: Float? = null,

    /** Voto minimo convertito */
    @SerialName("votoMinConvertito")
    val convertedMinGrade: Float? = null,

    /** Voto base convertito */
    @SerialName("votoBaseConvertito")
    val convertedBaseGrade: Float? = null,

    /** Tipo dichiarazione di valore */
    @SerialName("tipoDicValCod")
    val valueDeclarationTypeCode: String? = null,

    /** Identificativo della nazione in cui è stato conseguito il titolo */
    @SerialName("naziOrdCodFis")
    val orderNationFiscalCode: String? = null,

    /** nota legata al titolo */
    @SerialName("nota")
    val note: String? = null,

    /** descrizione libera dell ateneo di conseguimento */
    @SerialName("desAteneo")
    val universityDescription: String? = null
)

@Serializable
data class Esse3HigherSchoolTitleType(
    /** Codice MIUR del tipo di titolo superiore. */
    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    /** Codice della tipologia del titolo superiore. */
    @SerialName("tipologiaCod")
    val typologyCode: String? = null,

    /** Descrizione della tipologia del titolo di studio di scuola superiore conseguito. */
    @SerialName("des")
    val description: String? = null,

    /** Flag attributo di sistema. */
    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    /** Identificativo diploma MIUR. */
    @SerialName("idDiploma")
    val diplomaId: Int? = null,

    /** Codice ISTAT Alma Laurea. */
    @SerialName("almaCod")
    val almaCode: Int? = null,

    /** Indicatore di gestione anno integrativo. */
    @SerialName("annoIntFlg")
    val integrationYearFlag: Int? = null,

    /** Abilitazione visualizzazione on-line nella gestione dei titoli di accesso e dell'anagrafica titoli. */
    @SerialName("abilVisFlg")
    val visibilityFlag: Int? = null,

    /** Descrizione MIUR della tipologia del titolo di studio di scuola superiore conseguito. */
    @SerialName("miurDes")
    val miurDescription: String? = null,

    /** Tipo scuola codificato dal Ministero. */
    @SerialName("tipoScuolaCod")
    val schoolTypeCode: String? = null,

    /** Identificativo tipo istituto. */
    @SerialName("idTipoIst")
    val institutionTypeId: Int? = null,

    /** Descrizione tipologia istituto. */
    @SerialName("descTipo")
    val typeDescription: String? = null,

    /** Identificativo macro tipologia diploma. */
    @SerialName("idMacroTipo")
    val macroTypeId: Int? = null,

    /** Descrizione macro tipologia diploma. */
    @SerialName("descMacroTipo")
    val macroTypeDescription: String? = null,

    /** Descrizione tipologia del titolo di studio di scuola superiore conseguito. */
    @SerialName("desEng")
    val descriptionEnglish: String? = null
)

@Serializable
data class Esse3ExternalEntitiesReplica(
    /** Identificativo soggetto esterno. */
    @SerialName("soggEstId")
    val externalSubjectId: Int? = null,

    /** Identificativo ente. */
    @SerialName("enteId")
    val entityId: Int? = null,

    /** descrizione dell ente esterno */
    @SerialName("des")
    val description: String? = null,

    /** Codice ente esterno. */
    @SerialName("cod")
    val code: String? = null,

    /** direttore ente esterno. */
    @SerialName("direttore")
    val director: String? = null,

    /** Codice tipo ente richiesto. */
    @SerialName("tipoEnteCod")
    val entityTypeCode: String? = null,

    /** Descrizione tipo ente. */
    @SerialName("tipoEnteDes")
    val entityTypeDescription: String? = null,

    /** Codice settore ente richiesto. */
    @SerialName("settEnteCod")
    val entitySectorCode: String? = null,

    /** Descrizione settore ente. */
    @SerialName("settEnteDes")
    val entitySectorDescription: String? = null,

    /** Indica se l'ente è privato. */
    @SerialName("privatoFlg")
    val privateFlag: Int? = null,

    /** link all ente */
    @SerialName("link")
    val link: String? = null,

    /** Identificativo ente. */
    @SerialName("sdrId")
    val siteId: Int? = null,

    /** codice della struttura didattica responsabile */
    @SerialName("strutSdrCod")
    val structureSiteCode: String? = null,

    /** descrizione della struttura didattica responsabile */
    @SerialName("strutSdrDes")
    val structureSiteDescription: String? = null,

    /** descrizione della tipologia della struttura didattica */
    @SerialName("strutSdrTip")
    val structureSiteType: String? = null,

    /** descrizione del tipo struttura didattica responsabile */
    @SerialName("tipiSdrDes")
    val siteTypesDescription: String? = null,

    /** codice dello stato dell ente */
    @SerialName("statoEnteCod")
    val entityStateCode: String? = null,

    /** Descrizione dello stato dell ente. */
    @SerialName("statoEnteDes")
    val entityStateDescription: String? = null,

    /** codice fascia numero dipendenti */
    @SerialName("fasciaDipCod")
    val departmentBandCode: String? = null,

    /** descrizione  fascia dipendenti */
    @SerialName("fasciaDipDes")
    val departmentBandDescription: String? = null,

    /** secrizio ne ateneo straniero */
    @SerialName("desAtestra")
    val foreignTestDescription: String? = null,

    /** Indica se è stato dato il consenso per la privacy */
    @SerialName("autPrivacyFlg")
    val privacyAuthorizationFlag: Int? = null,

    /** id utente di inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** id utente di ultima  modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** gruppoo di appartenenza */
    @SerialName("gruppoAppart")
    val belongingGroup: String? = null,

    /** codice associativo */
    @SerialName("codiceAssociativo")
    val associativeCode: String? = null,

    /** fatturato */
    @SerialName("fatturato")
    val invoiced: String? = null,

    /** settore ateco id. */
    @SerialName("settAtecoId")
    val atecoSectorId: Int? = null,

    /** codice settore ateco */
    @SerialName("settAtecoCod")
    val atecoSectorCode: String? = null,

    /** descrizione del settore Ateco */
    @SerialName("settAtecoDes")
    val atecoSectorDescription: String? = null,

    /** id profilo aziendale */
    @SerialName("profiloAziId")
    val companyProfileId: Int? = null,

    /** descrizione profilo aziendale */
    @SerialName("profiloAziDes")
    val companyProfileDescription: String? = null,

    /** id codice Ateco */
    @SerialName("codAtecoId")
    val atecoCodeId: Int? = null,

    /** descrizione codice ateco */
    @SerialName("codAtecoDes")
    val atecoCodeDescription: String? = null,

    /** codice duns di 9 cifre */
    @SerialName("duns")
    val duns: String? = null,

    /** flag per generare opportunità */
    @SerialName("genOppEvidFlg")
    val generateOpportunityEvidenceFlag: Int? = null,

    /** codice crm */
    @SerialName("crmCod")
    val crmCode: String? = null,

    /** associazione imprenditorile */
    @SerialName("associazioneInprenditoriale")
    val businessAssociation: String? = null,

    /** flag che indica sincronizzazione avvenuta con crm */
    @SerialName("crmSyncFlg")
    val crmSyncFlag: Int? = null,

    /** Identificativo della registrazione azienda che ha dato origine all´azienda. */
    @SerialName("regAziId")
    val companyRegistrationId: Int? = null,

    /** nota */
    @SerialName("nota")
    val note: String? = null,

    /** prodotti */
    @SerialName("prodotti")
    val products: String? = null,

    /** lingue lavoro */
    @SerialName("lingueLavoro")
    val workLanguages: String? = null,

    /** lingue lavoro gruppo */
    @SerialName("lingueLavoroGruppo")
    val workLanguagesGroup: String? = null,

    /** nota inserita dall azienda */
    @SerialName("notaAzi")
    val companyNote: String? = null,

    /** Indirizzo email del responsabile della protezione dei dati */
    @SerialName("responsabileProtdatiEmail")
    val dataProtectionResponsibleEmail: String? = null,

    @SerialName("sedi")
    val sites: List<Esse3ExternalEntitiesLocationsReplica> = emptyList()
)

@Serializable
data class Esse3PersonPhotoAttachmentMetadata(
    /** Nome del file con estensione (formati validi: jpg, jpeg, bmp, png). */
    @SerialName("filename")
    val fileName: String = "",

    /** Flag che indica se la foto risulta già validato o meno. */
    @SerialName("fotoValidata")
    val validatedPhoto: Int? = null
)

@Serializable
data class Esse3PersonalDataConsents(
    /** Codice tipo consenso */
    @SerialName("tipoConsensoCod")
    val consentTypeCode: String? = null,

    /** Descrizione tipo consenso */
    @SerialName("tipiConsensoDes")
    val consentTypesDescription: String? = null,

    /** Etichetta tipo consenso */
    @SerialName("tipiConsensoEtichetta")
    val consentTypesLabel: String? = null,

    /** Flag consenso */
    @SerialName("consensoFlg")
    val consentFlag: Int? = null,

    /** Data inizio validità consenso */
    @SerialName("dataIni")
    val startDate: String? = null,

    /** Codice procedura amministrativa */
    @SerialName("procAmmCod")
    val administrativeProcedureCode: String? = null,

    /** Descrizione procedura amministrativa */
    @SerialName("procAmmDes")
    val administrativeProcedureDescription: String? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("storicoConsensi")
    val consentHistory: List<Esse3PersonalDataConsentsHistory> = emptyList()
)

@Serializable
data class Esse3ForeignTitleType(
    /** Codice Tipo di Titoli Straniero. */
    @SerialName("tipoTitst_cod")
    val titleStatusTypeCode: String? = null,

    /** Descrizione Tipo di Titoli Straniero. */
    @SerialName("des")
    val description: String? = null,

    /** Indica se il titolo è di livello universitario (U) o di livello di scuola superiore (S). */
    @SerialName("livelloCod")
    val levelCode: String? = null,

    /** Flag attributo di sistema. */
    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    /** Indica se il tipo titolo è utilizzabile nei criteri di filtro di recupero dei CV su WEBESSE3. */
    @SerialName("visTrovacvFlg")
    val cvSearchVisibleFlag: Int? = null,

    /** Codice esterno tipo titolo. */
    @SerialName("extCod")
    val externalCode: String? = null
)

@Serializable
data class Esse3CareerClosureParameters(
    /** codice motivo stato studente */
    @SerialName("motStastuCod")
    val statusReasonCode: String = "",

    /** data di chiusura della carriera */
    @SerialName("dataChiusura")
    val closingDate: String = ""
)

@Serializable
data class Esse3PhDProgramDirector(
    /** nome del responsabile */
    @SerialName("respNome")
    val responsibleName: String? = null,

    /** cognome del responsabile */
    @SerialName("respCognome")
    val responsibleSurname: String? = null,

    /** codice fiscale del responsabile */
    @SerialName("respCodFis")
    val responsibleFiscalCode: String? = null,

    /** data di nascita del responsabile */
    @SerialName("respDataNascita")
    val responsibleBirthDate: String? = null,

    /** luogo di nascita del responsabile */
    @SerialName("respLuogoNascita")
    val responsibleBirthPlace: String? = null,

    /** matricola del responsabile */
    @SerialName("respMatricola")
    val responsibleMatricola: String? = null,

    /** id U-Gov del responsabile */
    @SerialName("respIdAb")
    val responsibleAbbreviatedId: String? = null,

    /** carica del responsabile */
    @SerialName("respDesCarica")
    val responsiblePositionDescription: String? = null
)

@Serializable
data class Esse3PostCompensatoryMeasuresHandicapDeclarationParameters(
    /** stato della misura compensativa */
    @SerialName("statoMisuraComp")
    val compensatoryMeasureState: String? = null,

    /** data di inizio invalidità. */
    @SerialName("misuraDataIni")
    val measureStartDate: String? = null,

    /** data fine invalidità. */
    @SerialName("misuraDataFine")
    val measureEndDate: String? = null,

    /** codice della misura compensativa richiesta */
    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String? = null,

    /** Descrizione libera misura compensativa. */
    @SerialName("desLiberaMisura")
    val freeMeasureDescription: String? = null
)

@Serializable
data class Esse3CareerParameters(
    /** numero protocollo */
    @SerialName("numProtocollo")
    val protocolNumber: String = ""
)

@Serializable
data class Esse3AcademicYearRegistrationHandicapDeclaration(
    /** anno accademico */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** descrizione anno accademico */
    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3AuthorizedPersonalDocument(
    /** ID numerico univoco documento dell'autorizzato. */
    @SerialName("autDocPersId")
    val personalDataDocAuthorizationId: Long? = null,

    /** ID numerico univoco dell'autorizzato. */
    @SerialName("autorizzatoId")
    val authorizedId: Long? = null,

    /** Codice tipo di documento d'identità. */
    @SerialName("docIdentTipoCod")
    val identityDocumentTypeCode: String? = null,

    /** Descrizione tipo di documento d'identità. */
    @SerialName("docIdentTipoDes")
    val identityDocumentTypeDescription: String? = null,

    /** Numero del documento di identità. */
    @SerialName("num")
    val number: String? = null,

    /** Data di rilascio del documento. */
    @SerialName("dataRilascio")
    val releaseDate: String? = null,

    /** Data di scadenza del documento. */
    @SerialName("dataScadenza")
    val expirationDate: String? = null,

    /** Ente di rilascio del documento. */
    @SerialName("enteRilascio")
    val issuingEntity: String? = null,

    /** Codice stato documento personale. */
    @SerialName("statoDocPers")
    val personalDocumentState: String? = null,

    /** Nazione di emissione del documento di identità. */
    @SerialName("nazioneEmissioneId")
    val issuanceNationId: Int? = null,

    /** Codice di 4 cifre Lettera + 3 numeri: Znnn - la laettera è sempre una Z. Nel caso dell'Italia questo attributo non è istanziato. */
    @SerialName("nazioneEmissioneCodFis")
    val issuanceNationFiscalCode: String? = null,

    /** Descrizione Nazione di emissione del documento di identità. */
    @SerialName("nazioneEmissioneDes")
    val issuanceNationDescription: String? = null,

    /** Comune di emissione del documento di identità. */
    @SerialName("comuneEmissioneId")
    val issuanceMunicipalityId: Long? = null,

    /** Codice di 4 cifre (Lettera + 3 numeri) che è utilizzato nel codice fiscale per indicare il comune di nascita. */
    @SerialName("comuneEmissioneCodFis")
    val issuanceMunicipalityFiscalCode: String? = null,

    /** Descrizione comune di emissione del documento di identità. */
    @SerialName("comuneEmissioneDes")
    val issuanceMunicipalityDescription: String? = null,

    /** Città straniera di emissione del documento di identità. */
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
    val description: String = "",

    @SerialName("zipFileName")
    val zipFileName: String = ""
)

@Serializable
data class Esse3Suspensions(
    /** id univoco che consente di individuare la carriera */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** ID del Corso di Studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Anno di ordinamento */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** ID del percorso di studio */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** ID iscrizione studente */
    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    /** id univoco che consente di individuare la carriera */
    @SerialName("stuStaStuId")
    val studentStatusStudentId: Long? = null,

    /** Codice dello stato dello studente. */
    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    /** Descrizione dello stato dello studente */
    @SerialName("staStuDes")
    val studentStatusDescription: String? = null,

    /** Motivo dello stato dello studente. */
    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    /** Descrizione motivo dello stato dello studente. */
    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    /** AA in cui lo studente ha assunto lo stato indicato. */
    @SerialName("aaInizioId")
    val academicYearStartId: String? = null,

    /** AA in cui lo studente ha assunto lo stato indicato. */
    @SerialName("aaFineId")
    val academicYearEndId: String? = null,

    /** Data di inizio sospensione della carriera. */
    @SerialName("dataIniSosp")
    val suspensionStartDate: String? = null,

    /** Data di fine sospensione della carriera. */
    @SerialName("dataFineSosp")
    val suspensionEndDate: String? = null
)

@Serializable
data class Esse3HandicapDeclarationStatesLookup(
    /** codice stato dichiarazione di invalidità */
    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    /** descrizione stato dichiarazione di invalidità */
    @SerialName("des")
    val description: String? = null,

    /** flag che specifica se visibile dalla commisione */
    @SerialName("abilVisCommissioneFlg")
    val commissionVisibilityFlag: Long? = null
)

@Serializable
data class Esse3ForeignTitleValidationDeclaration(
    /** Codice tipologia dichirazione di valore di un titolo straniero. */
    @SerialName("tipoDicValCod")
    val valueDeclarationTypeCode: String? = null,

    /** Descrizione tipologia dichirazione di valore di un titolo straniero. */
    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3PersonalDataBankDetails(
    /** Codice tipo dati banca */
    @SerialName("tipoDatiBancaCod")
    val bankDataTypeCode: String? = null,

    /** Descrizione tipo dati banca */
    @SerialName("tipiDatiBancaDes")
    val bankDataTypesDescription: String? = null,

    /** Codice tipo rimborso pagamento */
    @SerialName("tipoRimbPagCod")
    val paymentRefundTypeCode: String? = null,

    /** Descrizione tipo rimborso pagamento */
    @SerialName("tipiRimbPagDes")
    val paymentRefundTypesDescription: String? = null,

    /** Descrizione banca */
    @SerialName("bancaDes")
    val bankDescription: String? = null,

    /** Intestatario conto corrente */
    @SerialName("ccIntestatario")
    val currentAccountHolder: String? = null,

    /** Codice fiscale intestatario conto */
    @SerialName("ccIntestatarioCf")
    val currentAccountHolderFiscalCode: String? = null,

    /** Codice IBAN */
    @SerialName("ibanCod")
    val ibanCode: String? = null,

    /** Numero di conto */
    @SerialName("nConto")
    val accountNumber: String? = null,

    /** Identificativo nazione */
    @SerialName("nazioneId")
    val nationId: Long? = null,

    /** Codice nazione */
    @SerialName("naziCod")
    val nationCode: String? = null,

    /** Codice fiscale nazione */
    @SerialName("naziCodFis")
    val nationFiscalCode: String? = null,

    /** Descrizione nazione */
    @SerialName("naziDes")
    val nationDescription: String? = null,

    /** Codice SWIFT */
    @SerialName("swiftCod")
    val swiftCode: String? = null,

    /** Modalità accesso dati */
    @SerialName("modaccDati")
    val dataAccessMode: Long? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null
)

@Serializable
data class Esse3ForeignTitle(
    /** ID del titolo straniero */
    @SerialName("titStraId")
    val foreignTitleId: Long? = null,

    /** Descrizione tipo titolo straniero */
    @SerialName("tipoTitstraDes")
    val foreignTitleTypeDescription: String? = null,

    /** Anno accademico di consegna */
    @SerialName("aaConsegId")
    val academicYearAwardId: Int? = null,

    /** Data consegna titolo */
    @SerialName("dataConsegTitolo")
    val titleDeliveryDate: String? = null,

    /** ID ateneo straniero */
    @SerialName("ateneoStranieroId")
    val foreignUniversityId: Long? = null,

    /** Codice ateneo straniero */
    @SerialName("atestraCod")
    val foreignTestCode: String? = null,

    /** Nome ateneo straniero */
    @SerialName("atestraDes")
    val foreignTestDescription: String? = null,

    /** Codice nazione */
    @SerialName("nazioneCod")
    val nationCode: String? = null,

    /** Nome nazione */
    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    /** Voto conseguito */
    @SerialName("voto")
    val grade: Double? = null,

    /** Base del voto */
    @SerialName("votoBase")
    val baseGrade: Int? = null,

    /** Voto alfanumerico */
    @SerialName("votoAlfanumerico")
    val alphanumericGrade: String? = null,

    /** Corso di studio straniero */
    @SerialName("cdsStraniero")
    val foreignCourseOfStudy: String? = null,

    /** Durata in anni del corso */
    @SerialName("durataAnni")
    val durationYears: Int? = null,

    /** Stato titolo straniero */
    @SerialName("staTitStraCod")
    val foreignTitleStatusCode: String? = null,

    /** Descrizione stato titolo */
    @SerialName("statiTitDes")
    val titleStatesDescription: String? = null,

    /** Flag dichiarazione valore */
    @SerialName("dichiarazValoreFlg")
    val valueDeclarationFlag: Int? = null,

    /** Flag titolo equipollente */
    @SerialName("titoloEquipFlg")
    val equivalentTitleFlag: Int? = null,

    /** Codice tipo deposito */
    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    /** Descrizione tipo deposito */
    @SerialName("tipoDepositoDes")
    val depositTypeDescription: String? = null,

    /** Descrizione titolo */
    @SerialName("desTitolo")
    val titleDescription: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** ID lingua didattica */
    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    /** Codice lingua ISO 639-1 */
    @SerialName("linguaDidIso6391Cod")
    val teachingLanguageIso6391Code: String? = null,

    /** Codice lingua ISO 639-2 */
    @SerialName("linguaDidIso6392Cod")
    val teachingLanguageIso6392Code: String? = null,

    /** Descrizione lingua didattica */
    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    /** Flag laurea entro DN */
    @SerialName("lauEntroDnFlg")
    val graduationWithinDeadlineFlag: Int? = null,

    /** Flag lode */
    @SerialName("lode")
    val cumLaude: Int? = null,

    /** Flag valutato */
    @SerialName("valutatoFlg")
    val evaluatedFlag: Int? = null,

    /** Città consegna titolo */
    @SerialName("citstraConseg")
    val deliveryForeignCity: String? = null,

    /** Voto minimo */
    @SerialName("votoMin")
    val minGrade: Double? = null,

    /** Voto convertito */
    @SerialName("votoConvertito")
    val convertedGrade: Double? = null,

    /** Voto minimo convertito */
    @SerialName("votoMinConvertito")
    val convertedMinGrade: Double? = null,

    /** Voto base convertito */
    @SerialName("votoBaseConvertito")
    val convertedBaseGrade: Double? = null,

    /** ID nazione ordinamento */
    @SerialName("nazioneOrdId")
    val orderNationId: Int? = null,

    /** Codice nazione ordinamento */
    @SerialName("nazioneOrdCod")
    val orderNationCode: String? = null,

    /** Descrizione nazione ordinamento */
    @SerialName("nazioneOrdDes")
    val orderNationDescription: String? = null,

    /** Note aggiuntive */
    @SerialName("nota")
    val note: String? = null,

    /** Codice tipo dichiarazione valore */
    @SerialName("tipoDicValCod")
    val valueDeclarationTypeCode: String? = null,

    /** Descrizione tipo dichiarazione valore */
    @SerialName("tipoDicValDes")
    val valueDeclarationTypeDescription: String? = null
)

@Serializable
data class Esse3ExternalEntitiesLocationsReplica(
    /** Identificativo soggetto esterno. */
    @SerialName("soggEstId")
    val externalSubjectId: Int? = null,

    /** Identificativosede ente esterno. */
    @SerialName("sediEntiEstId")
    val externalEntitiesSitesId: Int? = null,

    /** Identificativo ente. */
    @SerialName("enteId")
    val entityId: Int? = null,

    /** Codice tipo sede richiesto. */
    @SerialName("tipoSedeCod")
    val siteTypeCode: String? = null,

    /** Descrizione tipo sede. */
    @SerialName("tipoSedeDes")
    val siteTypeDescription: String? = null,

    /** Descrizione della sede. */
    @SerialName("des")
    val description: String? = null,

    /** via. */
    @SerialName("via")
    val street: String? = null,

    /** cap. */
    @SerialName("cap")
    val postalCode: String? = null,

    /** Codice fiscale. */
    @SerialName("cF")
    val fiscalCode: String? = null,

    /** partita iva. */
    @SerialName("piva")
    val vatNumber: String? = null,

    /** città straniera. */
    @SerialName("citstra")
    val foreignCity: String? = null,

    /** Identificativo del comune. */
    @SerialName("comuneId")
    val municipalityId: Int? = null,

    /** codice dell comune. */
    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    /** codice catastale del comune. */
    @SerialName("comuneCodCatastale")
    val municipalityCadastralCode: String? = null,

    /** codice istat del comune per miur. */
    @SerialName("comuneCodIstatMiur")
    val municipalityMiurIstatCode: String? = null,

    /** descrixzione del commune. */
    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    /** sigla della provincia. */
    @SerialName("comuneSigla")
    val municipalityAbbreviation: String? = null,

    /** descrizione della provincia. */
    @SerialName("provDes")
    val provinceDescription: String? = null,

    /** Identificativo della nazione. */
    @SerialName("nazioneId")
    val nationId: Int? = null,

    /** codice della nazione. */
    @SerialName("nazieNascCod")
    val birthNationRefCode: String? = null,

    /** codice ans . */
    @SerialName("naziNascNazioneCod")
    val birthCountryCode: String? = null,

    /** codice fdella nazione. */
    @SerialName("naziNascCodInt")
    val birthNationInternationalCode: String? = null,

    /** numero di telefono. */
    @SerialName("numTel")
    val phoneNumber: String? = null,

    /** prefisso internazionale. */
    @SerialName("prefixInternaz")
    val internationalPrefix: String? = null,

    /** fax. */
    @SerialName("fax")
    val fax: String? = null,

    /** codice sede. */
    @SerialName("codSede")
    val siteCode: String? = null,

    /** email. */
    @SerialName("email")
    val email: String? = null,

    /** Identifica se la mail è visibile da web */
    @SerialName("emailVisWeb")
    val webVisibleEmail: Int? = null,

    /** codice areoporto. */
    @SerialName("iataCod")
    val iataCode: String? = null,

    /** sede disattivata */
    @SerialName("disattivaFlg")
    val deactivateFlag: Int? = null,

    /** partita iva del gruppo. */
    @SerialName("pivaGruppo")
    val groupVatNumber: String? = null,

    /** codice sdi. */
    @SerialName("codiceSdi")
    val sdiCode: String? = null,

    /** frazione. */
    @SerialName("fraz")
    val fraction: String? = null,

    /** email certificata. */
    @SerialName("emailCertigficata")
    val certifiedEmail: String? = null,

    /** codice identificativo di gara */
    @SerialName("cig")
    val cig: String? = null,

    /** codice unico di progetto. */
    @SerialName("cup")
    val cup: String? = null,

    /** ipa anagrafica di riferimento per la fatturazione elettronica. */
    @SerialName("ipa")
    val ipa: String? = null,

    /** indica se è attivo lo Split Payement, l'ente pubblico si occupa di saldare il debito IVA direttamente all'Erario. */
    @SerialName("splitpayementFlg")
    val splitPaymentFlag: Int? = null
)

@Serializable
data class Esse3ItalianTitlesEnrollment(
    /** Data di conseguimento del titolo */
    @SerialName("dataConsegTitolo")
    val titleDeliveryDate: String? = null,

    /** Indica in quale forma è stato depositato il titolo. Per esempio o originale, CS copia , AUT autocertificazione CA copia autenticata S sostitutivo A in ateneo REG registrazione atto CER certificato. */
    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    /** Corso di studio dell´Ateneo esterno per il quale è stato conseguito il titolo */
    @SerialName("cdsAteId")
    val courseOfStudyAteId: Long? = null,

    /** Descrive l´eventuale indirizzo dello studente */
    @SerialName("percorsoDiStudio")
    val studyPath: String? = null,

    /** Voto conseguito */
    @SerialName("voto")
    val grade: Float? = null,

    /** base del voto */
    @SerialName("baseVoto")
    val baseGrade: Long? = null,

    /** Flag che indica se è stata conseguita la lode. 0 se non c’è il voto */
    @SerialName("lode")
    val cumLaude: Long? = null,

    /** Flag che indica se il titolo è stato ottenuto nello stesso ateneo */
    @SerialName("stessoAteneoFlg")
    val sameUniversityFlag: Long? = null,

    /** Codice ISTAT dell’Ateneo di conseguimento */
    @SerialName("ateneiIstatCod")
    val universitiesIstatCode: String? = null,

    /** Codice MIUR dell’Ateneo di conseguimento */
    @SerialName("ateneiCodUn")
    val universitiesUnifiedCode: String? = null,

    /** Tipo titolo italiano */
    @SerialName("idTipoLaurea")
    val degreeTypeId: String? = null,

    /** sessione di conseguimento titolo */
    @SerialName("sessione")
    val session: String? = null,

    /** Flag che indica se è stata ottenuta l´iscrizione all´albo */
    @SerialName("iscrAlboFlg")
    val registerEnrollmentFlag: Long? = null,

    /** Codice ISTAT dell corso di studio di conseguimento */
    @SerialName("cdsAteneiItaIstatCod")
    val italianUniversitiesCourseOfStudyIstatCode: String? = null,

    /** Appellativo femminile del titolo italiano */
    @SerialName("appellativoF")
    val femaleTitle: String? = null,

    /** Appellativo maschile del titolo italiano */
    @SerialName("appellativoM")
    val maleTitle: String? = null,

    /** Anno Accademico di conseguimento del titolo */
    @SerialName("aaConsegTitolo")
    val academicYearTitleAward: Long? = null,

    /** Flag che indica se è stata ottenuta l´abilitazione al sostegno */
    @SerialName("abilFlg")
    val authorizationFlag: Long? = null,

    /** Stato del  titolo- C Conseguito, I In ipotesi */
    @SerialName("staTitItCod")
    val italianTitleStatusCode: String = "",

    /** Numero d´ordine della classe di abilitazione */
    @SerialName("ordine")
    val order: Long? = null,

    /** Note libere per il titolo */
    @SerialName("nota")
    val note: String? = null,

    /** Titolo della tesi */
    @SerialName("titoloTesi")
    val thesisTitle: String? = null,

    /** Flag che Indica che la persona ha inoltrato la domanda di riconoscimento di titolo straniero */
    @SerialName("domRicoStraFlg")
    val domicileForeignRecoveryFlag: Long? = null,

    /** Flag che Indica che il titolo è stato conseguito per riconoscimento di titolo straniero */
    @SerialName("ricoTitStraFlg")
    val foreignTitleRecoveryFlag: Long? = null,

    /** Data di inizio del tirocinio */
    @SerialName("dataIniTiro")
    val internshipStartDate: String? = null,

    /** Data di fine del tirocinio */
    @SerialName("dataFinTiro")
    val internshipEndDate: String? = null,

    /** Data di iscrizione all´ordine professionale */
    @SerialName("dataIscrOrdProf")
    val professionalOrderEnrollmentDate: String? = null,

    /** Descrizion e libera della sede di conseguimento titolo, utilizzata nel caso in cui questa non sia codificata */
    @SerialName("desSede")
    val siteDescription: String? = null,

    /** Flag che Indica che lo studente si è laureato entro la durata normale del corso di studio. */
    @SerialName("lauEntroDnFlg")
    val graduationWithinDeadlineFlag: Long? = null,

    /** Numero di CFU associati al titolo. Tale campo viene utilizzato solo per titoli di tipo MSC */
    @SerialName("cfu")
    val credits: Float? = null,

    /** Giudizio libero. Utilizzato per i dottorati di ricerca, in cui la commissione non utilizza giudizi codificati per valutare il conseguimento del titolo di dottorato */
    @SerialName("giudizioFinDes")
    val finalJudgmentDescription: String? = null,

    /** Flag che Indica se con il conseguimento dell'esame di stato è stato conseguito anche il tirocinio. */
    @SerialName("tirocinioFlg")
    val internshipFlag: Long? = null,

    /** Descrizione libera del corso di studio */
    @SerialName("desCds")
    val courseOfStudyDescription: String? = null,

    /** Descrizione struttura di conseguimento del titolo di studio */
    @SerialName("sdrConsegDes")
    val deliverySiteDescription: String? = null,

    /** Codice nazione struttura di conseguimento del titolo. */
    @SerialName("sdrConsegNazioneFisCod")
    val deliverySiteFiscalNationCode: String? = null,

    /** Città straniera della struttura di conseguimento del titolo. */
    @SerialName("sdrConsegCitstra")
    val deliverySiteForeignCity: String? = null,

    /** codice catastale del comune della struttura di conseguimento del titolo. */
    @SerialName("sdrConsegComuneCod")
    val deliverySiteMunicipalityCode: String? = null,

    /** Via della struttura di conseguimento del titolo. */
    @SerialName("sdrConsegVia")
    val deliverySiteStreet: String? = null,

    /** Numero civico della struttura di conseguimento del titolo. */
    @SerialName("sdrConsegNumCiv")
    val deliverySiteStreetNumber: String? = null,

    /** CAP della struttura di conseguimento del titolo. */
    @SerialName("sdrConsegCap")
    val deliverySitePostalCode: String? = null,

    /** Dettaglio tipo titolo */
    @SerialName("tititCod")
    val titleTypeCode: String? = null,

    /** Data di scadenza del titolo */
    @SerialName("dataScadenza")
    val expirationDate: String? = null
)

@Serializable
data class Esse3Authorized(
    /** Identificativo autorizzato */
    @SerialName("autorizzatoId")
    val authorizedId: Long? = null,

    /** Nome */
    @SerialName("nome")
    val name: String? = null,

    /** Cognome */
    @SerialName("cognome")
    val surname: String? = null,

    /** Codice fiscale */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** codice di errore nel controllo del CF */
    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Long? = null,

    /** Flag certificato */
    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    /** Sesso */
    @SerialName("sesso")
    val gender: String? = null,

    /** Data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** Tipo parentela codice */
    @SerialName("tipoParCod")
    val paragraphTypeCode: String? = null,

    /** Descrizione tipo parentela */
    @SerialName("tipiParDes")
    val paragraphTypesDescription: String? = null,

    /** Identificativo nazione */
    @SerialName("nazioneId")
    val nationId: Long? = null,

    /** Codice nazione */
    @SerialName("naziCod")
    val nationCode: String? = null,

    /** Descrizione nazione */
    @SerialName("naziDes")
    val nationDescription: String? = null,

    /** Codice ISO nazione */
    @SerialName("naziNazioneCod")
    val nationNationCode: String? = null,

    /** Codice internazionale nazione */
    @SerialName("naziCodInt")
    val nationInternationalCode: String? = null,

    /** Città o stato nascita */
    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    /** Identificativo comune */
    @SerialName("comuneId")
    val municipalityId: Long? = null,

    /** Codice comune */
    @SerialName("comuCod")
    val municipalityCode: String? = null,

    /** Codice catastale comune */
    @SerialName("comuCodCatastale")
    val municipalityCadastralCode: String? = null,

    /** Codice ISTAT/MIUR comune */
    @SerialName("comuCodIstatMiur")
    val municipalityMiurIstatCode: String? = null,

    /** Descrizione comune */
    @SerialName("comuDes")
    val municipalityDescription: String? = null,

    /** Sigla comune */
    @SerialName("comuSigla")
    val municipalityAbbreviation: String? = null,

    /** Descrizione provincia */
    @SerialName("provDes")
    val provinceDescription: String? = null,

    /** Email */
    @SerialName("email")
    val email: String? = null,

    /** PEC */
    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Numero cellulare */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** Codice esterno autorizzato */
    @SerialName("autExtCod")
    val externalAuthorizationCode: String? = null
)

@Serializable
data class Esse3HighSchoolDiplomaPerson(
    /** Identificativo univoco della persona */
    @SerialName("persId")
    val personId: String? = null,

    /** Codice di 4 cifre (Lettera + 3 numeri) che è utilizzato nel codice fiscale per indicare il comune di nascita. */
    @SerialName("aaIntScuolaComuCod")
    val academicYearInternalSchoolMunicipalityCode: String? = null,

    @SerialName("aaIntScuolaComuDes")
    val academicYearInternalSchoolMunicipalityDescription: String? = null,

    /** Sigla provincia */
    @SerialName("aaIntScuolaComuSigla")
    val academicYearInternalSchoolMunicipalityAbbreviation: String? = null,

    /** Descrizione dell'istituto */
    @SerialName("aaIntScuolaDes")
    val academicYearInternalSchoolDescription: String? = null,

    /** Un'alternativa tra le seguenti: Nessun diploma depositato Diploma originale depositato Autocertificazione presentata Fotocopia Fotocopia autenticata Copia dell'attestazione del titolo di laurea con elenco degli esami svolti */
    @SerialName("aaIntTipiDepDes")
    val academicYearInternalDepositTypesDescription: String? = null,

    /** Numero di anni integrativi da conseguire in Italia, nel caso di conseguimento di titolo straniero. */
    @SerialName("anniIntegrativi")
    val supplementaryYears: Int? = null,

    /** Anni di scolarità frequentati. Da usare ed abilitare solo per gli studenti che non si sono diplomati in Italia. */
    @SerialName("anniScolarita")
    val anniScolarita: Int? = null,

    /** Anno in cui  stato conseguito lanno di integrazione. */
    @SerialName("annoIntegrazione")
    val integrationYear: Int? = null,

    /** Flag che indica se per laccesso all'Università  necessario il conseguimento dellanno integrativo. */
    @SerialName("annoIntFlg")
    val integrationYearFlag: Int? = null,

    /** Anno in cui  stata conseguita la maturità. Coincide con lanno solare relativo alla DATA di conseguimento del diploma. Per esempio, se lo studente ha conseguito il diploma nellanno scolastico 1996/97, lanno di diploma  il 1997. */
    @SerialName("annoMaturita")
    val highSchoolGraduationYear: Int? = null,

    @SerialName("certAns")
    val certAnswer: Int? = null,

    /** Città straniera di conseguimento del titolo. */
    @SerialName("citstraConseg")
    val deliveryForeignCity: String? = null,

    /** Codice titolo sistema esterno. */
    @SerialName("codExt")
    val externalCode: String? = null,

    /** Data di deposito del titolo. */
    @SerialName("dataDepositoTitolo")
    val titleDepositDate: String? = null,

    /** Data di maturità. */
    @SerialName("dataMaturita")
    val highSchoolGraduationDate: String? = null,

    /** Data di restituzione del titolo. */
    @SerialName("dataRestituzione")
    val returnDate: String? = null,

    /** Descrizione libera della scuola di conseguimento del titolo superiore, nel caso in cui questa non sia codificata. */
    @SerialName("desScuola")
    val schoolDescription: String? = null,

    /** Descrizione libera della scuola superiore in cui  stato frequentato l'anno integrativo, nel caso in cui questa non sia codificata. */
    @SerialName("desScuolaAnnoInt")
    val schoolInternationalYearDescription: String? = null,

    /** Il codice attributo dal sistema informativo del Ministero dell' Istruzione. */
    @SerialName("aaIntScuolaCodMiur")
    val academicYearInternalSchoolMiurCode: String? = null,

    /** Codice meccanografico della scuola. */
    @SerialName("aaIntScuolaCodScuola")
    val academicYearInternalSchoolCode: String? = null,

    /** Flag che indica se  stata presentata la dichiarazione di valore. Vale solo per maturità straniere. Nel caso la persona sia in possesso di un titolo di studio straniero, questo deve essere presentato direttamente alla Segreteria Studenti, tradotto, legalizzato ed accompagnato dalla DICHIARAZIONE DI VALORE, cio da una dichiarazione della rappresentanza consolare italiana, da cui risulti; - che nel Paese nel quale  stato conseguito, il titolo  valido per l accesso a corsi di studio analoghi a quelli per i quali viene chiesta liscrizione universitaria in Italia - il sistema di valutazione locale e la scala di valori cui si riferisce il voto o il giudizio del titolo stesso. */
    @SerialName("dichiarazValoreFlg")
    val valueDeclarationFlag: Int? = null,

    /** Identificativo univoco dei titoli di scuola superiore della persona. */
    @SerialName("id")
    val id: Int? = null,

    /** Link al sistema di gestione elettronica dei documenti. */
    @SerialName("identificativoGed")
    val gedIdentifier: String? = null,

    /** Indirizzo del titolo. */
    @SerialName("indirizzo")
    val address: String? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    /** Lode. */
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

    /** Numero di raccomandata per restituzione titolo. */
    @SerialName("raccomandataNum")
    val registeredMailNumber: String? = null,

    /** Flag che indica se il titolo  stato restituito o meno. */
    @SerialName("restituitoFlg")
    val returnedFlag: Int? = null,

    /** Tipo restituzione. */
    @SerialName("restituitoTipo")
    val returnedType: String? = null,

    /** Flag che indica se lo studente ha richiesto la restituzione delloriginale del diploma. */
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

    /** Stato del il tipo titolo: C: Conseguito, I: In ipotesi */
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

    /** Indica in quale forma  stato depositato il titolo relativo all'anno integrativo: originale, fotocopia, autocertificazione. */
    @SerialName("tipoDepositoAnnoIntCod")
    val internationalYearDepositTypeCode: String? = null,

    /** TIPO DOCUMENTO CONSEGNATO: N --> Nessun diploma depositato O --> Diploma originale depositato AUT --> Autocertificazione presentata F --> Fotocopia FAUT --> Fotocopia autenticata LAUESA --> Copia dellattestazione del titolo di laurea con elenco degli esami svolti */
    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    /** Codice MIUR del tipo di titolo superiore */
    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    /** Codice Tipo di Titoli Straniero. Utilizzato sia per la decodifica del tipo di titoli stranieri che del titpo di corsi stranieri. Esempi: Magister, Bachelor, Master, Licence */
    @SerialName("tipoTitstCod")
    val titleStatusTypeCode: String? = null,

    @SerialName("titAccAmm")
    val adminTitleAccess: Long? = null,

    @SerialName("titAccMat")
    val matTitleAccess: Long? = null,

    @SerialName("titAccMatStu")
    val studentMatTitleAccess: Long? = null,

    /** Indica se il titolo è stato valutato. */
    @SerialName("valutatoFlg")
    val evaluatedFlag: Long? = null,

    /** Voto del diploma. */
    @SerialName("voto")
    val grade: Float? = null,

    /** Voto alfanumerico del diploma. Da usare ed abilitare solo per gli studenti che non si sono diplomati in Italia. */
    @SerialName("votoAlfa")
    val alphanumericGrade: String? = null,

    /** Voto massimo di maturit. Se Anno_Maturita > 1998 allora Voto_Max = 100 se Anno_Maturita < 1999 allora Voto_Max = 60 */
    @SerialName("votoMax")
    val maxGrade: Float? = null,

    /** Voto minimo di maturit. Se Anno_Maturita > 1998 allora Voto_Min = 60 se Anno_Maturita < 1999 allora  Voto_Min = 36 */
    @SerialName("votoMin")
    val minGrade: Float? = null
)

@Serializable
data class Esse3ExemptionTypeParameters(
    /** Codice tipologia esonero */
    @SerialName("tipoEsoCod")
    val exemptionTypeCode: String? = null,

    /** Data di iscrizione */
    @SerialName("dataIscr")
    val enrollmentDate: String? = null
)

@Serializable
data class Esse3AuthorizedPerson(
    /** ID numerico univoco della persona. */
    @SerialName("persId")
    val personId: Long? = null,

    /** Identificativo autorizzato. */
    @SerialName("autorizzatoId")
    val authorizedId: Long? = null,

    /** Nome della persona autorizzata */
    @SerialName("nome")
    val name: String? = null,

    /** Cognome della persona autorizzata */
    @SerialName("cognome")
    val surname: String? = null,

    /** Codice fiscale della persona autorizzata */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** M  --> maschio, F  --> femmina */
    @SerialName("sesso")
    val gender: String? = null,

    /** Data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** ID del comune di nascita della persona autorizzata */
    @SerialName("comuneId")
    val municipalityId: Long? = null,

    /** Descrizione del comune di nascita della persona autorizzata */
    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    /** Vincolo di parentela con chi delega: Padre, Madre, Fratello, Sorella, Moglie, Marito, Nessuna parentela, ... */
    @SerialName("tipoParCod")
    val paragraphTypeCode: String? = null,

    /** Descrizione del vincolo di parentela */
    @SerialName("tipoParDes")
    val paragraphTypeDescription: String? = null,

    /** Codice di errore nel controllo del CF */
    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Long? = null,

    /** Indica se i dati anagrafici sono stati certificati. */
    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    /** ID Nazione di nascita della persona autorizzata */
    @SerialName("nazioneId")
    val nationId: Long? = null,

    /** Descrizione nazione di nascita della persona autorizzata */
    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    /** Città straniera di nascita. */
    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    /** Indirizzo email. */
    @SerialName("email")
    val email: String? = null,

    /** Indirizzo email Certificata (PEC). */
    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    /** Numero di cellulare. */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** Codice esterno autorizzato. */
    @SerialName("autExtCod")
    val externalAuthorizationCode: String? = null,

    @SerialName("documentiIdentita")
    val identityDocuments: List<Esse3AuthorizedPersonalDocument> = emptyList()
)

@Serializable
data class Esse3PostSpecialNeedsHandicapDeclarationParameters(
    /** ID dichiarazione di invalidità */
    @SerialName("dicHandId")
    val handicapDeclarationId: Int? = null,

    /** codice bisogno speciale */
    @SerialName("bisognoSpecialeCod")
    val specialNeedCode: String? = null
)

@Serializable
data class Esse3CompensatoryMeasures(
    /** Codice misura compensativa. */
    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String? = null,

    /** Descrizione misura compensativa. */
    @SerialName("des")
    val description: String? = null,

    /** Nota della misura compensativa */
    @SerialName("nota")
    val note: String? = null,

    /** Abilitazione visualizzazione on-line misura compensativa. */
    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    /** Abilitazione inserimento descrizione libera misura compensativa. */
    @SerialName("desLiberaFlg")
    val freeDescriptionFlag: Int? = null
)

@Serializable
data class Esse3Person(
    /** id univoco che consente di individuare la persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** id univoco che consente di individuare la persona */
    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    /** Codice identificativo dell'anagrafica */
    @SerialName("persCod")
    val personCode: String? = null,

    /** cognome della persona */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome della persona */
    @SerialName("nome")
    val name: String? = null,

    /** Nominativo del genitore */
    @SerialName("patronimico")
    val patronymic: String? = null,

    /** data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** identificativo comune nascita */
    @SerialName("comuNascId")
    val birthMunicipalityId: Long? = null,

    /** codice comune nascita */
    @SerialName("comuNascCod")
    val birthMunicipalityCode: String? = null,

    /** codice catastale del comune di nascita */
    @SerialName("comuNascCodCatastale")
    val birthMunicipalityCadastralCode: String? = null,

    /** codice istat comune nascita */
    @SerialName("comuNascCodIstatMiur")
    val birthMunicipalityMiurIstatCode: String? = null,

    /** denominazione del comune di nascita */
    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    /** identificativo nazione nascita */
    @SerialName("naziNascId")
    val birthNationId: Long? = null,

    /** codice nazione nascita */
    @SerialName("naziNascCod")
    val birthNationCode: String? = null,

    /** denominazione del luogo di nascita se nazione diversa da italia */
    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    /** sigla della provincia di nascita */
    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    /** denominazione della provincia di nascita */
    @SerialName("provNascDes")
    val birthProvinceDescription: String? = null,

    /** codice della nazione di nascita */
    @SerialName("naziNascNazioneCod")
    val birthCountryCode: String? = null,

    /** denominazione della nazione di nascita */
    @SerialName("naziNascDes")
    val birthNationDescription: String? = null,

    /** codice 3 numeri della nazione di nascita */
    @SerialName("naziNascCodInt")
    val birthNationInternationalCode: String? = null,

    /** codice fiscale della personae */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** codice di errore nel controllo del CF */
    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Int? = null,

    /** sesso della persona */
    @SerialName("sesso")
    val gender: String? = null,

    /** id univoco che consente di individuare l'account utente */
    @SerialName("userId")
    val userId: String? = null,

    /** id univoco che consente di individuare la foto associata alla  persona */
    @SerialName("fotoId")
    val photoId: Long? = null,

    /** codice catastale della nazione di residenza */
    @SerialName("naziResCod")
    val residenceNationCode: String? = null,

    /** nazione di residenza */
    @SerialName("naziResDes")
    val residenceNationDescription: String? = null,

    /** codice ISTAT del comune di residenza */
    @SerialName("comuResCod")
    val residenceMunicipalityCode: String? = null,

    /** comune di residenza */
    @SerialName("comuResDes")
    val residenceMunicipalityDescription: String? = null,

    /** sigla della provincia  di residenza */
    @SerialName("comuResSigla")
    val residenceMunicipalityAbbreviation: String? = null,

    /** via di residenza */
    @SerialName("viaRes")
    val residenceStreet: String? = null,

    /** numero civico di residenza */
    @SerialName("numCivRes")
    val residenceStreetNumber: String? = null,

    /** CAP di residenza */
    @SerialName("capRes")
    val residencePostalCode: String? = null,

    /** Telefono di residenza */
    @SerialName("telRes")
    val residencePhone: String? = null,

    /** codice catastale della nazione di domicilio */
    @SerialName("naziDomCod")
    val domicileNationCode: String? = null,

    /** nazione di domicilio */
    @SerialName("naziDomDes")
    val domicileNationDescription: String? = null,

    /** codice ISTAT del comune di domicilio */
    @SerialName("comuDomCod")
    val domicileMunicipalityCode: String? = null,

    /** comune di domicilio */
    @SerialName("comuDomDes")
    val domicileMunicipalityDescription: String? = null,

    /** sigla della provincia  di domicilio */
    @SerialName("comuDomSigla")
    val domicileMunicipalityAbbreviation: String? = null,

    /** via di domicilio */
    @SerialName("viaDom")
    val domicileStreet: String? = null,

    /** numero civico di domicilio */
    @SerialName("numCivDom")
    val domicileStreetNumber: String? = null,

    /** CAP di domicilio */
    @SerialName("capDom")
    val domicilePostalCode: String? = null,

    /** Telefono di domicilio */
    @SerialName("telDom")
    val domicilePhone: String? = null,

    /** email personale */
    @SerialName("email")
    val email: String? = null,

    /** email di ateneo */
    @SerialName("emailAte")
    val universityEmail: String? = null,

    /** data di inserimento (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data di modifica (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Codice Cittadinanza */
    @SerialName("codCittadinanza")
    val citizenshipCode: String? = null,

    /** Descrizione cittadinanza */
    @SerialName("desCittadinanza")
    val citizenshipDescription: String? = null,

    /** numero di cellulare */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** Indicatore di Permesso di soggiorno scaduto */
    @SerialName("permsogScadutoFlg")
    val authorizedSubjectExpiredFlag: Long? = null,

    /** Indicatore di Permesso di soggiorno caricato */
    @SerialName("presenzaPermSogFlg")
    val authorizedSubjectPresenceFlag: Long? = null,

    /** Data scadenza dell'ultimo Permesso di soggiorno caricato (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("permsogDataScad")
    val authorizedSubjectExpirationDate: String? = null,

    /** usato per inserimento iscrizioni pregresso, indica se il dato è certificato dall'operatore di segreteria. 0 - inserite dallo studente, 1- certificato dall'utente di segreteria. */
    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    /** ID della nazione di residenza. */
    @SerialName("naziResId")
    val residenceNationId: Long? = null,

    /** Codice della nazione di residenza. */
    @SerialName("naziResNazioneCod")
    val residenceCountryCode: String? = null,

    /** Codice internazionale della nazione di residenza. */
    @SerialName("naziResCodInt")
    val residenceNationInternationalCode: String? = null,

    /** ID del comune di residenza. */
    @SerialName("comuResId")
    val residenceMunicipalityId: Long? = null,

    /** Codice catastale del comune di residenza. */
    @SerialName("comuResCodCatastale")
    val residenceMunicipalityCadastralCode: String? = null,

    /** Codice ISTAT/MIUR del comune di residenza. */
    @SerialName("comuResCodIstatMiur")
    val residenceMunicipalityMiurIstatCode: String? = null,

    /** Descrizione della provincia di residenza. */
    @SerialName("provResDes")
    val residenceProvinceDescription: String? = null,

    /** Città straniera di residenza. */
    @SerialName("citstraRes")
    val residenceForeignCity: String? = null,

    /** Frazione di residenza. */
    @SerialName("frazRes")
    val residenceFraction: String? = null,

    /** Prefisso telefonico internazionale della nazione di residenza. */
    @SerialName("prefixInternazRes")
    val residenceInternationalPrefix: String? = null,

    /** Data inizio validità residenza. */
    @SerialName("dataIniValRes")
    val residenceEvaluationStartDate: String? = null,

    /** Flag che indica se il domicilio coincide con la residenza. */
    @SerialName("domComeResFlg")
    val domicileSameAsResidenceFlag: Int? = null,

    /** ID della nazione di domicilio. */
    @SerialName("naziDomId")
    val domicileNationId: Int? = null,

    /** Codice della nazione di domicilio. */
    @SerialName("naziDomNazioneCod")
    val domicileCountryCode: String? = null,

    /** Codice internazionale della nazione di domicilio. */
    @SerialName("naziDomCodInt")
    val domicileNationInternationalCode: String? = null,

    /** ID del comune di domicilio. */
    @SerialName("comuDomId")
    val domicileMunicipalityId: Long? = null,

    /** Codice catastale del comune di domicilio. */
    @SerialName("comuDomCodCatastale")
    val domicileMunicipalityCadastralCode: String? = null,

    /** Codice ISTAT/MIUR del comune di domicilio. */
    @SerialName("comuDomCodIstatMiur")
    val domicileMunicipalityMiurIstatCode: String? = null,

    /** Città straniera di domicilio. */
    @SerialName("citstraDom")
    val domicileForeignCity: String? = null,

    /** Frazione di domicilio. */
    @SerialName("frazDom")
    val domicileFraction: String? = null,

    /** Prefisso telefonico internazionale della nazione di domicilio. */
    @SerialName("prefixInternazDom")
    val domicileInternationalPrefix: String? = null,

    /** Indicazione del presso. */
    @SerialName("cO")
    val co: String? = null,

    /** Data di inizio validità del domicilio. */
    @SerialName("dataIniValDom")
    val domicileEvaluationStartDate: String? = null,

    /** Viene valorizzato solo nel caso in cui sia stato introdotto un domicilio valido (a livello di tempo). */
    @SerialName("tipoIndirizCod")
    val addressTypeCode: String? = null,

    /** Codice del recapito della tasse. */
    @SerialName("recapitoTasse")
    val taxesContact: String? = null,

    /** Indica la tipologia di indirizzo selezionato per il recapito badge. */
    @SerialName("recapitoBadge")
    val badgeContact: String? = null,

    /** Numero fax. */
    @SerialName("fax")
    val fax: String? = null,

    /** Email studente certificata. */
    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    /** Codice della nazione della prima cittadinanza. */
    @SerialName("citt1NazioneCod")
    val citizenship1CountryCode: String? = null,

    /** Data di inizio validità della prima cittadinanza. */
    @SerialName("citt1Dataini")
    val citizenship1StartDate: String? = null,

    /** Data di fine validità della prima cittadinanza. */
    @SerialName("citt1Datafin")
    val citizenship1EndDate: String? = null,

    /** Codice della seconda cittadinanza. */
    @SerialName("citt2Cod")
    val citizenship2Code: String? = null,

    /** Descrizione della seconda cittadinanza. */
    @SerialName("citt2Des")
    val citizenship2Description: String? = null,

    /** Codice della nazione della seconda cittadinanza. */
    @SerialName("citt2NazioneCod")
    val citizenship2CountryCode: String? = null,

    /** Data di inizio validità della seconda cittadinanza. */
    @SerialName("citt2Dataini")
    val citizenship2StartDate: String? = null,

    /** Data di fine validità della seconda cittadinanza. */
    @SerialName("citt2Datafin")
    val citizenship2EndDate: String? = null,

    /** Codice della terza cittadinanza. */
    @SerialName("citt3Cod")
    val citizenship3Code: String? = null,

    /** Descrizione della terza cittadinanza. */
    @SerialName("citt3Des")
    val citizenship3Description: String? = null,

    /** Codice della nazione della terza cittadinanza. */
    @SerialName("citt3NazioneCod")
    val citizenship3CountryCode: String? = null,

    /** Data di inizio validità della terza cittadinanza. */
    @SerialName("citt3Dataini")
    val citizenship3StartDate: String? = null,

    /** Data di fine validità della terza cittadinanza. */
    @SerialName("citt3Datafin")
    val citizenship3EndDate: String? = null,

    /** Codice internazionale della nazione di cittadinanza principale. */
    @SerialName("naziCittadCodInt")
    val citizenshipNationInternationalCode: String? = null,

    /** Descrizione della nazione di cittadinanza principale. */
    @SerialName("naziCittadDes")
    val citizenshipNationDescription: String? = null,

    /** Prefisso del numero di cellulare. */
    @SerialName("prefixCell")
    val mobilePrefix: String? = null,

    /** Consenso per il trattamento dei dati personali. */
    @SerialName("consDpFlg")
    val consentDpFlag: Int? = null,

    /** Flag di consenso per la diffusione dei dati personali. */
    @SerialName("consDiffDpFlg")
    val consentDifferentDpFlag: Int? = null,

    /** Consenso alla notifica tramite messaggistica SMS. */
    @SerialName("consSmsFlg")
    val consentSmsFlag: Int? = null,

    /** Consenso alla comunicazione dei dati personali. */
    @SerialName("consComunicDpFlg")
    val consentCommunicationDpFlag: Int? = null,

    /** Consenso invio dei dati all'ente regionale. */
    @SerialName("consComunicErFlg")
    val consentCommunicationErFlag: Int? = null,

    /** Flag che indica se lo studente appartiene al clero. */
    @SerialName("religiosoFlg")
    val religiousFlag: Int? = null,

    /** Indica se la persona è deceduta. */
    @SerialName("decedutoFlg")
    val deceasedFlag: Int? = null,

    /** Codice esterno di identificazione della persona. */
    @SerialName("extPersCod")
    val externalPersonCode: String? = null,

    /** Nota persona. */
    @SerialName("notaPers")
    val personalNote: String? = null,

    /** Descrizione della professione. */
    @SerialName("professione")
    val profession: String? = null,

    /** Codice dello stato civile. */
    @SerialName("statoCivileCod")
    val maritalStatusCode: String? = null,

    /** Descrizione dello stato civile. */
    @SerialName("statoCivileDes")
    val maritalStatusDescription: String? = null,

    /** Nome di una persona da contattare in caso di emergenza. */
    @SerialName("emergNome")
    val emergencyName: String? = null,

    /** Cognome di una persona da contattare in caso di emergenza. */
    @SerialName("emergCognome")
    val emergencySurname: String? = null,

    /** Telefono di una persona da contattare in caso di emergenza. */
    @SerialName("emergTel")
    val emergencyPhone: String? = null,

    /** Prefisso telefonico internazionale di una persona da contattare in caso di emergenza.. */
    @SerialName("emergPrefixInternaz")
    val emergencyInternationalPrefix: String? = null,

    /** Indirizzo e-mail di una persona da contattare in caso di emergenza. */
    @SerialName("emergEmail")
    val emergencyEmail: String? = null,

    /** Rapporto con persona da contattare in caso di emergenza. */
    @SerialName("emergRapporto")
    val emergencyRelationship: String? = null
)

@Serializable
data class Esse3HandicapDeclarationPatch(
    /** imposta la validazione dell'allegato (1 true, 0 false) */
    @SerialName("validoFlg")
    val validFlag: Long? = null
)

@Serializable
data class Esse3HandicapDeclarationSpecialNeeds(
    /** ID dichiarazione di invalidità */
    @SerialName("dicHandId")
    val handicapDeclarationId: Int? = null,

    /** userId attivo collegato al docente */
    @SerialName("userId")
    val userId: String? = null,

    /** Tipo di handicap */
    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    /** Numero compreso tra 0 e 100 che riporta la percentuale di handicap dello studente. */
    @SerialName("percHand")
    val handicapPercentage: Int? = null,

    /** Data della dichiarazione */
    @SerialName("dataDichiar")
    val declarationDate: String? = null,

    /** Codice dello stato della dichiarazione di handicap. */
    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    /** ID bisogno speciale legato alla dichiarazione di invalidità. */
    @SerialName("dicHandBisId")
    val handicapDeclarationBisId: Int? = null,

    /** codice bisogno speciale */
    @SerialName("bisognoSpecialeCod")
    val specialNeedCode: String? = null,

    /** descrizione bisogno speciale */
    @SerialName("bisognoSpecialeDes")
    val specialNeedDescription: String? = null,

    /** Abilita visibilità del bisogno nella lista appelli on-line. */
    @SerialName("abilVisOnLine")
    val onlineVisibility: Int? = null
)

@Serializable
data class Esse3AllItalianTitles(
    /** ID del titolo */
    @SerialName("titItId")
    val italianTitleId: Long? = null,

    /** Data di consegna del titolo */
    @SerialName("dataConsegTitolo")
    val titleDeliveryDate: String? = null,

    /** Codice del tipo di deposito */
    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    /** Descrizione del tipo di deposito */
    @SerialName("tipoDepositoDes")
    val depositTypeDescription: String? = null,

    /** Voto conseguito */
    @SerialName("voto")
    val grade: Double? = null,

    /** Base del voto */
    @SerialName("baseVoto")
    val baseGrade: Int? = null,

    /** Flag lode */
    @SerialName("lode")
    val cumLaude: Int? = null,

    /** ID dell'ateneo */
    @SerialName("ateneoId")
    val universityId: Long? = null,

    /** Codice ISTAT dell'ateneo */
    @SerialName("ateneoIstatCod")
    val universityIstatCode: String? = null,

    /** Codice università MIUR */
    @SerialName("ateneoCodeUn")
    val universityUnifiedCode: String? = null,

    /** Nome dell'ateneo */
    @SerialName("ateneoDes")
    val universityDescription: String? = null,

    /** Codice comune */
    @SerialName("comuCod")
    val municipalityCode: String? = null,

    /** Codice catastale del comune */
    @SerialName("comuCodCatastale")
    val municipalityCadastralCode: String? = null,

    /** Codice ISTAT MIUR del comune */
    @SerialName("comuCodIstatMiur")
    val municipalityMiurIstatCode: String? = null,

    /** Nome del comune */
    @SerialName("comuDes")
    val municipalityDescription: String? = null,

    /** Sigla del comune */
    @SerialName("comuSigla")
    val municipalityAbbreviation: String? = null,

    /** Anno accademico di consegna del titolo */
    @SerialName("aaConsegTitolo")
    val academicYearTitleAward: Int? = null,

    /** Codice tipo titolo */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    /** Descrizione */
    @SerialName("des")
    val description: String? = null,

    /** Codice stato titolo */
    @SerialName("staTitItCod")
    val italianTitleStatusCode: String? = null,

    /** Descrizione stato titolo */
    @SerialName("statiTitDes")
    val titleStatesDescription: String? = null,

    /** Codice titolo */
    @SerialName("tititCod")
    val titleTypeCode: String? = null,

    /** Descrizione titolo */
    @SerialName("tititDes")
    val titleTypeDescription: String? = null,

    /** Flag laurea entro DN */
    @SerialName("lauEntroDnFlg")
    val graduationWithinDeadlineFlag: Int? = null,

    /** Codice corso di studio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** Descrizione corso di studio */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** Descrizione sede */
    @SerialName("desSede")
    val siteDescription: String? = null,

    /** Percorso di studio */
    @SerialName("percorsoDiStudio")
    val studyPath: String? = null,

    /** ID lingua didattica */
    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    /** Codice lingua ISO 639-1 */
    @SerialName("linguaDidIso6391Cod")
    val teachingLanguageIso6391Code: String? = null,

    /** Codice lingua ISO 639-2 */
    @SerialName("linguaDidIso6392Cod")
    val teachingLanguageIso6392Code: String? = null,

    /** Descrizione lingua didattica */
    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    /** Giudizio finale */
    @SerialName("giudizioFinDes")
    val finalJudgmentDescription: String? = null,

    /** Flag tirocinio */
    @SerialName("tirocinioFlg")
    val internshipFlag: Int? = null,

    /** Data inizio attività */
    @SerialName("dataIniAttivita")
    val activityStartDate: String? = null,

    /** Data fine attività */
    @SerialName("dataFineAttivita")
    val activityEndDate: String? = null,

    /** Descrizione estesa */
    @SerialName("desEstesa")
    val extendedDescription: String? = null,

    /** Numero di anni per il conseguimento del titolo */
    @SerialName("numAnniConseguimento")
    val achievementYearsNumber: Int? = null,

    /** Media dei voti */
    @SerialName("mediaVoti")
    val gradesAverage: Double? = null,

    /** Flag valutato */
    @SerialName("valutatoFlg")
    val evaluatedFlag: Int? = null,

    /** Data scadenza */
    @SerialName("dataScadenza")
    val expirationDate: String? = null,

    /** Note aggiuntive */
    @SerialName("nota")
    val note: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null
)

@Serializable
data class Esse3CareerGDPR(
    /** id univoco che consente di individuare la persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** cognome della persona */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome della persona */
    @SerialName("nome")
    val name: String? = null,

    /** data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** sesso della persona */
    @SerialName("sesso")
    val gender: String? = null,

    /** id univoco che consente di individuare l'account utente */
    @SerialName("userId")
    val userId: String? = null,

    /** codice fiscale della personae */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** email personale dello studente */
    @SerialName("email")
    val email: String? = null,

    /** indirizzo email assegnato dall'ateneo allo studente */
    @SerialName("emailAte")
    val universityEmail: String? = null,

    /** sigla che identifica lo stato della carriera */
    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    /** sigla che identifica il motivo dello stato della carriera */
    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    /** anno di immatricolazione */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** data di immatricolazione */
    @SerialName("dataImm")
    val matriculationDate: String? = null,

    /** descrizione dello stato della carriera */
    @SerialName("statiStuDes")
    val studentStatesDescription: String? = null,

    /** descrizione del motivo della stato della carriera */
    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    /** numero protocollo */
    @SerialName("numProtocollo")
    val protocolNumber: String? = null,

    /** data di inserimento (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data di modifica (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** stato domanda di conseguiimento titolo */
    @SerialName("domCtStato")
    val domicileCommitteeState: String? = null,

    /** descrizione stato domanda di conseguiimento titolo */
    @SerialName("statiDomCtDes")
    val committeeApplicationStatesDescription: String? = null,

    /** descrizione anno accademico */
    @SerialName("aaDes")
    val academicYearDescription: String? = null,

    /** anno iscrizione */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    /** matricola */
    @SerialName("matricola")
    val matricola: String? = null,

    /** id della sede */
    @SerialName("sedeId")
    val siteId: Int? = null,

    /** descrizione della sede */
    @SerialName("sediDes")
    val sitesDescription: String? = null,

    /** anno di corso */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** descrizione lingua */
    @SerialName("lingue")
    val languages: String? = null,

    /** data iscrizione */
    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    /** codice del settore */
    @SerialName("settCod")
    val sectorCode: String? = null,

    /** descrizione del settore */
    @SerialName("settDes")
    val sectorDescription: String? = null,

    /** codice dell'area */
    @SerialName("areaCod")
    val areaCode: String? = null,

    /** descrizione dell'area */
    @SerialName("areaDes")
    val areaDescription: String? = null,

    /** codice usato nelle statistiche del MIUR */
    @SerialName("areaCodStatMiur")
    val areaMiurStatisticalCode: String? = null,

    /** codice struttura didattica */
    @SerialName("sdrCod")
    val siteCode: String? = null,

    /** descrizione struttura didattica */
    @SerialName("sdrDes")
    val siteDescription: String? = null,

    /** Identificativo della struttura didattica responsabile */
    @SerialName("sdrCsaCod")
    val siteCsaCode: Int? = null,

    /** codice facoltà */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione facoltà */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** codice csa della facoltà */
    @SerialName("facCsaCod")
    val facultyCsaCode: String? = null,

    /** identificativo U-gov */
    @SerialName("idAb")
    val abbreviatedId: Int? = null,

    /** Codice esterno carriera */
    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    @SerialName("responsabile")
    val responsible: Esse3PhDProgramDirector? = null,

    @SerialName("tutor")
    val tutor: Esse3TutorData? = null,

    /** Flag che indica se lo studente è iscritto in attesa di laurea. */
    @SerialName("attlauFlg")
    val degreeAwardFlag: Int? = null,

    /** data attesa di laurea */
    @SerialName("dataAttlau")
    val degreeAwardDate: String? = null,

    /** identificativo della tipologia di categoria amministrativa */
    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    /** descrizione della tipologia di categoria amministrativa */
    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    /** Profilo studente */
    @SerialName("profstuCod")
    val studentProfessionCode: String? = null,

    /** descrizione profilo studente */
    @SerialName("profstuDes")
    val studentProfessionDescription: String? = null,

    /** Indica lo stato della posizione della matricola. I valori di sistema sono:  A =  Attivo, S = Sospeso, I = Ipotesi */
    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    /** Causale dello stato della matricola. */
    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    /** Tipo di iscrizione all´anno di corso specificato: IC = In Corso, FC = Fuori Corso, RI = Ripetente */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** Flag che indica se l´iscrizione è part-time (1) oppure full-time (0). */
    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    /** Flag che indica se nell´anno dell´iscrizione lo studente era sospeso e quindi se l´iscrizione era fittizia. */
    @SerialName("sospFlg")
    val suspensionFlag: Int? = null,

    /** Codice mnemonico del corso di studio */
    @SerialName("p06CdsCod")
    val p06CourseOfStudyCode: String? = null,

    /** Descrizione del corso di studio */
    @SerialName("p06CdsDes")
    val p06CourseOfStudyDescription: String? = null,

    /** Identificativo carriera */
    @SerialName("matId")
    val matId: Long? = null,

    /** Anno di ordinamento */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** ID del percorso di studio */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** ID iscrizione studente */
    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    /** identificativo corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** id univoco che consente di individuare la carriera */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** data di chiusura della carriera */
    @SerialName("dataChiusura")
    val closingDate: String? = null,

    /** anno accademico di inizio carriera */
    @SerialName("aaImm1")
    val academicYearImm1: Int? = null,

    /** Anno Accademico Regolamenti (Coorte) */
    @SerialName("aaRegId")
    val academicYearRegulationId: Long? = null,

    /** Indirizzo e-mail certificato (PEC). */
    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    /** nome alias */
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
    /** Identificativo del corso. */
    @SerialName("cdsAteId")
    val courseOfStudyAteId: String? = null,

    /** identificativo Ateneo. */
    @SerialName("ateneoId")
    val universityId: Int? = null,

    /** Codice ISTAT dell'Ateneo. */
    @SerialName("ateneiIstatCod")
    val universitiesIstatCode: String? = null,

    /** Descrizione Ateneo. */
    @SerialName("ateneiDes")
    val universitiesDescription: String? = null,

    /** Codice università MIUR. */
    @SerialName("ateneiCodeUn")
    val universitiesUnifiedCode: String? = null,

    /** Codice Erasmus dell'Ateneo. */
    @SerialName("ateneiErasmusCod")
    val universitiesErasmusCode: String? = null,

    /** codice del tipo di corso di studio */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** Codice ISTAT del corso. */
    @SerialName("istatCod")
    val istatCode: String? = null,

    /** Descrizione del corso. */
    @SerialName("des")
    val description: String? = null,

    /** Anno di disattivazione del corso oppure NULL se il corso è ancora attivo. */
    @SerialName("aaDisattivazione")
    val academicYearDeactivation: Int? = null,

    /** Durata legale in anni del corso. */
    @SerialName("durataAnni")
    val durationYears: Int? = null,

    /** Indica se il corso è post (0) o ante riforma (1). */
    @SerialName("riformaFlg")
    val reformFlag: Int? = null,

    /** Codice corso. */
    @SerialName("cod")
    val code: String? = null,

    /** Indica se il corso viene gestito a settori. */
    @SerialName("settFlg")
    val sectorFlag: Int? = null,

    /** Indica se il corso è generico. */
    @SerialName("genericoFlg")
    val genericFlag: Int? = null,

    /** Codice Off. F. Ministeriale del Corso di Studio dell'Ateneo. */
    @SerialName("codicione")
    val bigCode: String? = null,

    /** Identificativo univoco della Normativa di riferimento per la riforma universitaria. */
    @SerialName("normId")
    val normId: Int? = null,

    /** Codice breve alfanumerico della Normativa. */
    @SerialName("normativaCod")
    val regulationCode: String? = null,

    /** Descrizione estesa della Normativa. */
    @SerialName("normativaDes")
    val regulationDescription: String? = null,

    /** Note della Normativa. */
    @SerialName("normativaNote")
    val regulationNotes: String? = null,

    /** Flag attributo di sistema. */
    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    /** Campo note. */
    @SerialName("note")
    val notes: String? = null,

    @SerialName("classe")
    val `class`: List<Esse3StudyCourseClass> = emptyList()
)

@Serializable
data class Esse3TutorData(
    /** cognome del tutor */
    @SerialName("cognomeTutor")
    val tutorSurname: String? = null,

    /** nome del tutor */
    @SerialName("nomeTutor")
    val tutorName: String? = null,

    /** id del tutor */
    @SerialName("docenteIdTutor")
    val lecturerTutorId: Int? = null,

    /** id del tutor se è un soggetto esterno */
    @SerialName("soggEstIdTutor")
    val externalSubjectTutorId: Int? = null,

    /** id address book */
    @SerialName("idAbTutor")
    val tutorAbbreviatedId: Int? = null,

    /** matricola del tutor */
    @SerialName("matricolaTutor")
    val tutorMatricola: String? = null
)

@Serializable
data class Esse3StudyCourseClass(
    /** Identificativo del corso. */
    @SerialName("cdsAteId")
    val courseOfStudyAteId: String? = null,

    /** Codice della classe del corso di studio. */
    @SerialName("cod")
    val code: String? = null,

    /** Descrizione della classe del corso di studio. */
    @SerialName("des")
    val description: String? = null,

    /** Identificativo dell'area. */
    @SerialName("claAreaId")
    val classAreaId: Long? = null,

    /** Codice ISCED-F 1. */
    @SerialName("iscedF1")
    val iscedF1: String? = null,

    /** Codice ISCED-F 2. */
    @SerialName("iscedF2")
    val iscedF2: String? = null,

    /** Codice ISCED-F 3. */
    @SerialName("iscedF3")
    val iscedF3: String? = null
)

@Serializable
data class Esse3PersonalDataContacts(
    /** Identificativo contatto */
    @SerialName("contattoId")
    val contactId: Long? = null,

    /** Codice tipo contatto */
    @SerialName("tipoContattoCod")
    val contactTypeCode: String? = null,

    /** Descrizione tipo contatto */
    @SerialName("tipoContattoDes")
    val contactTypeDescription: String? = null,

    /** Numero d'ordine */
    @SerialName("ordNum")
    val orderNumber: Long? = null,

    /** Valore contatto */
    @SerialName("valore")
    val value: String? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null
)

@Serializable
data class Esse3PutCompensatoryMeasuresHandicapDeclarationParameters(
    /** stato della misura compensativa */
    @SerialName("statoMisuraComp")
    val compensatoryMeasureState: String? = null,

    /** data di inizio invalidità. */
    @SerialName("misuraDataIni")
    val measureStartDate: String? = null,

    /** data fine invalidità. */
    @SerialName("misuraDataFine")
    val measureEndDate: String? = null
)

@Serializable
data class Esse3PersonalDataAddresses(
    /** Identificativo indirizzo */
    @SerialName("anaperIndId")
    val personAddressId: Long? = null,

    /** Codice tipo indirizzo */
    @SerialName("tipoIndirizCod")
    val addressTypeCode: String? = null,

    /** Descrizione tipo indirizzo */
    @SerialName("tipoIndirizDes")
    val addressTypeDescription: String? = null,

    /** Identificativo nazione */
    @SerialName("nazioneId")
    val nationId: Long? = null,

    /** Codice fisico nazione indirizzo */
    @SerialName("naziIndCod")
    val addressNationCode: String? = null,

    /** Descrizione nazione indirizzo */
    @SerialName("naziIndDes")
    val addressNationDescription: String? = null,

    /** Codice ISO nazione indirizzo */
    @SerialName("naziIndNazioneCod")
    val addressCountryCode: String? = null,

    /** Codice internazionale nazione indirizzo */
    @SerialName("naziIndCodInt")
    val addressNationInternationalCode: String? = null,

    /** Identificativo comune */
    @SerialName("comuneId")
    val municipalityId: Long? = null,

    /** Identificativo comune nascita */
    @SerialName("comuNascId")
    val birthMunicipalityId: Long? = null,

    /** Codice comune nascita */
    @SerialName("comuNascCod")
    val birthMunicipalityCode: String? = null,

    /** Codice catastale comune nascita */
    @SerialName("comuNascCodCatastale")
    val birthMunicipalityCadastralCode: String? = null,

    /** Codice ISTAT/MIUR comune nascita */
    @SerialName("comuNascCodIstatMiur")
    val birthMunicipalityMiurIstatCode: String? = null,

    /** Descrizione comune nascita */
    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    /** Sigla comune nascita */
    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    /** Descrizione provincia indirizzo */
    @SerialName("provIndDes")
    val addressProvinceDescription: String? = null,

    /** Frazione */
    @SerialName("fraz")
    val fraction: String? = null,

    /** Città o strada */
    @SerialName("citstra")
    val foreignCity: String? = null,

    /** Nome via */
    @SerialName("via")
    val street: String? = null,

    /** Numero civico */
    @SerialName("numCiv")
    val streetNumber: String? = null,

    /** CAP */
    @SerialName("cap")
    val postalCode: String? = null,

    /** Numero telefono */
    @SerialName("tel")
    val phone: String? = null,

    /** Prefisso internazionale */
    @SerialName("prefixInternaz")
    val internationalPrefix: String? = null,

    /** Fax */
    @SerialName("fax")
    val fax: String? = null,

    /** Numero cellulare */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** Email */
    @SerialName("email")
    val email: String? = null,

    /** Data inizio validità */
    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Flag aziendale */
    @SerialName("aziendaleFlg")
    val companyRelatedFlag: Int? = null,

    /** Ragione sociale */
    @SerialName("ragioneSociale")
    val companyName: String? = null,

    /** Partita IVA */
    @SerialName("piva")
    val vatNumber: String? = null,

    /** Codice fiscale */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** PEC */
    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    /** Codice SDI */
    @SerialName("codiceSdi")
    val sdiCode: String? = null,

    /** URL */
    @SerialName("url")
    val url: String? = null,

    /** Cognome persona */
    @SerialName("cognome")
    val surname: String? = null,

    /** Nome persona */
    @SerialName("nome")
    val name: String? = null,

    /** Codice CIG */
    @SerialName("cig")
    val cig: String? = null,

    /** Codice CUP */
    @SerialName("cup")
    val cup: String? = null,

    /** Codice IPA */
    @SerialName("ipa")
    val ipa: String? = null,

    /** Flag split payment */
    @SerialName("splitpayementFlg")
    val splitPaymentFlag: Int? = null,

    @SerialName("indirizziStorico")
    val historicalAddresses: List<Esse3PersonalDataAddressesHistory> = emptyList()
)

@Serializable
data class Esse3ValidationFlag(
    /** flag che rappresenta la validazone della foto. 1 foto valida, altrimenti 0. */
    @SerialName("validaFlg")
    val validationFlag: Int? = null
)

@Serializable
data class Esse3PersonGDPR(
    /** id univoco che consente di individuare la persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** id univoco che consente di individuare la persona */
    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    /** Codice identificativo dell'anagrafica */
    @SerialName("persCod")
    val personCode: String? = null,

    /** cognome della persona */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome della persona */
    @SerialName("nome")
    val name: String? = null,

    /** Nominativo del genitore */
    @SerialName("patronimico")
    val patronymic: String? = null,

    /** data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** identificativo comune nascita */
    @SerialName("comuNascId")
    val birthMunicipalityId: Long? = null,

    /** codice comune nascita */
    @SerialName("comuNascCod")
    val birthMunicipalityCode: String? = null,

    /** codice catastale del comune di nascita */
    @SerialName("comuNascCodCatastale")
    val birthMunicipalityCadastralCode: String? = null,

    /** codice istat comune nascita */
    @SerialName("comuNascCodIstatMiur")
    val birthMunicipalityMiurIstatCode: String? = null,

    /** denominazione del comune di nascita */
    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    /** identificativo nazione nascita */
    @SerialName("naziNascId")
    val birthNationId: Long? = null,

    /** codice nazione nascita */
    @SerialName("naziNascCod")
    val birthNationCode: String? = null,

    /** denominazione del luogo di nascita se nazione diversa da italia */
    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    /** sigla della provincia di nascita */
    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    /** denominazione della provincia di nascita */
    @SerialName("provNascDes")
    val birthProvinceDescription: String? = null,

    /** codice della nazione di nascita */
    @SerialName("naziNascNazioneCod")
    val birthCountryCode: String? = null,

    /** denominazione della nazione di nascita */
    @SerialName("naziNascDes")
    val birthNationDescription: String? = null,

    /** codice 3 numeri della nazione di nascita */
    @SerialName("naziNascCodInt")
    val birthNationInternationalCode: String? = null,

    /** codice fiscale della personae */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** codice di errore nel controllo del CF */
    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Int? = null,

    /** sesso della persona */
    @SerialName("sesso")
    val gender: String? = null,

    /** id univoco che consente di individuare l'account utente */
    @SerialName("userId")
    val userId: String? = null,

    /** id univoco che consente di individuare la foto associata alla  persona */
    @SerialName("fotoId")
    val photoId: Long? = null,

    /** codice catastale della nazione di residenza */
    @SerialName("naziResCod")
    val residenceNationCode: String? = null,

    /** nazione di residenza */
    @SerialName("naziResDes")
    val residenceNationDescription: String? = null,

    /** codice ISTAT del comune di residenza */
    @SerialName("comuResCod")
    val residenceMunicipalityCode: String? = null,

    /** comune di residenza */
    @SerialName("comuResDes")
    val residenceMunicipalityDescription: String? = null,

    /** sigla della provincia  di residenza */
    @SerialName("comuResSigla")
    val residenceMunicipalityAbbreviation: String? = null,

    /** via di residenza */
    @SerialName("viaRes")
    val residenceStreet: String? = null,

    /** numero civico di residenza */
    @SerialName("numCivRes")
    val residenceStreetNumber: String? = null,

    /** CAP di residenza */
    @SerialName("capRes")
    val residencePostalCode: String? = null,

    /** Telefono di residenza */
    @SerialName("telRes")
    val residencePhone: String? = null,

    /** codice catastale della nazione di domicilio */
    @SerialName("naziDomCod")
    val domicileNationCode: String? = null,

    /** nazione di domicilio */
    @SerialName("naziDomDes")
    val domicileNationDescription: String? = null,

    /** codice ISTAT del comune di domicilio */
    @SerialName("comuDomCod")
    val domicileMunicipalityCode: String? = null,

    /** comune di domicilio */
    @SerialName("comuDomDes")
    val domicileMunicipalityDescription: String? = null,

    /** sigla della provincia  di domicilio */
    @SerialName("comuDomSigla")
    val domicileMunicipalityAbbreviation: String? = null,

    /** via di domicilio */
    @SerialName("viaDom")
    val domicileStreet: String? = null,

    /** numero civico di domicilio */
    @SerialName("numCivDom")
    val domicileStreetNumber: String? = null,

    /** CAP di domicilio */
    @SerialName("capDom")
    val domicilePostalCode: String? = null,

    /** Telefono di domicilio */
    @SerialName("telDom")
    val domicilePhone: String? = null,

    /** email personale */
    @SerialName("email")
    val email: String? = null,

    /** email di ateneo */
    @SerialName("emailAte")
    val universityEmail: String? = null,

    /** data di inserimento (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data di modifica (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Codice Cittadinanza */
    @SerialName("codCittadinanza")
    val citizenshipCode: String? = null,

    /** Descrizione cittadinanza */
    @SerialName("desCittadinanza")
    val citizenshipDescription: String? = null,

    /** numero di cellulare */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** Indicatore di Permesso di soggiorno scaduto */
    @SerialName("permsogScadutoFlg")
    val authorizedSubjectExpiredFlag: Long? = null,

    /** Indicatore di Permesso di soggiorno caricato */
    @SerialName("presenzaPermSogFlg")
    val authorizedSubjectPresenceFlag: Long? = null,

    /** Data scadenza dell'ultimo Permesso di soggiorno caricato (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("permsogDataScad")
    val authorizedSubjectExpirationDate: String? = null,

    /** usato per inserimento iscrizioni pregresso, indica se il dato è certificato dall'operatore di segreteria. 0 - inserite dallo studente, 1- certificato dall'utente di segreteria. */
    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    /** ID della nazione di residenza. */
    @SerialName("naziResId")
    val residenceNationId: Long? = null,

    /** Codice della nazione di residenza. */
    @SerialName("naziResNazioneCod")
    val residenceCountryCode: String? = null,

    /** Codice internazionale della nazione di residenza. */
    @SerialName("naziResCodInt")
    val residenceNationInternationalCode: String? = null,

    /** ID del comune di residenza. */
    @SerialName("comuResId")
    val residenceMunicipalityId: Long? = null,

    /** Codice catastale del comune di residenza. */
    @SerialName("comuResCodCatastale")
    val residenceMunicipalityCadastralCode: String? = null,

    /** Codice ISTAT/MIUR del comune di residenza. */
    @SerialName("comuResCodIstatMiur")
    val residenceMunicipalityMiurIstatCode: String? = null,

    /** Descrizione della provincia di residenza. */
    @SerialName("provResDes")
    val residenceProvinceDescription: String? = null,

    /** Città straniera di residenza. */
    @SerialName("citstraRes")
    val residenceForeignCity: String? = null,

    /** Frazione di residenza. */
    @SerialName("frazRes")
    val residenceFraction: String? = null,

    /** Prefisso telefonico internazionale della nazione di residenza. */
    @SerialName("prefixInternazRes")
    val residenceInternationalPrefix: String? = null,

    /** Data inizio validità residenza. */
    @SerialName("dataIniValRes")
    val residenceEvaluationStartDate: String? = null,

    /** Flag che indica se il domicilio coincide con la residenza. */
    @SerialName("domComeResFlg")
    val domicileSameAsResidenceFlag: Int? = null,

    /** ID della nazione di domicilio. */
    @SerialName("naziDomId")
    val domicileNationId: Int? = null,

    /** Codice della nazione di domicilio. */
    @SerialName("naziDomNazioneCod")
    val domicileCountryCode: String? = null,

    /** Codice internazionale della nazione di domicilio. */
    @SerialName("naziDomCodInt")
    val domicileNationInternationalCode: String? = null,

    /** ID del comune di domicilio. */
    @SerialName("comuDomId")
    val domicileMunicipalityId: Long? = null,

    /** Codice catastale del comune di domicilio. */
    @SerialName("comuDomCodCatastale")
    val domicileMunicipalityCadastralCode: String? = null,

    /** Codice ISTAT/MIUR del comune di domicilio. */
    @SerialName("comuDomCodIstatMiur")
    val domicileMunicipalityMiurIstatCode: String? = null,

    /** Città straniera di domicilio. */
    @SerialName("citstraDom")
    val domicileForeignCity: String? = null,

    /** Frazione di domicilio. */
    @SerialName("frazDom")
    val domicileFraction: String? = null,

    /** Prefisso telefonico internazionale della nazione di domicilio. */
    @SerialName("prefixInternazDom")
    val domicileInternationalPrefix: String? = null,

    /** Indicazione del presso. */
    @SerialName("cO")
    val co: String? = null,

    /** Data di inizio validità del domicilio. */
    @SerialName("dataIniValDom")
    val domicileEvaluationStartDate: String? = null,

    /** Viene valorizzato solo nel caso in cui sia stato introdotto un domicilio valido (a livello di tempo). */
    @SerialName("tipoIndirizCod")
    val addressTypeCode: String? = null,

    /** Codice del recapito della tasse. */
    @SerialName("recapitoTasse")
    val taxesContact: String? = null,

    /** Indica la tipologia di indirizzo selezionato per il recapito badge. */
    @SerialName("recapitoBadge")
    val badgeContact: String? = null,

    /** Numero fax. */
    @SerialName("fax")
    val fax: String? = null,

    /** Email studente certificata. */
    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    /** Codice della nazione della prima cittadinanza. */
    @SerialName("citt1NazioneCod")
    val citizenship1CountryCode: String? = null,

    /** Data di inizio validità della prima cittadinanza. */
    @SerialName("citt1Dataini")
    val citizenship1StartDate: String? = null,

    /** Data di fine validità della prima cittadinanza. */
    @SerialName("citt1Datafin")
    val citizenship1EndDate: String? = null,

    /** Codice della seconda cittadinanza. */
    @SerialName("citt2Cod")
    val citizenship2Code: String? = null,

    /** Descrizione della seconda cittadinanza. */
    @SerialName("citt2Des")
    val citizenship2Description: String? = null,

    /** Codice della nazione della seconda cittadinanza. */
    @SerialName("citt2NazioneCod")
    val citizenship2CountryCode: String? = null,

    /** Data di inizio validità della seconda cittadinanza. */
    @SerialName("citt2Dataini")
    val citizenship2StartDate: String? = null,

    /** Data di fine validità della seconda cittadinanza. */
    @SerialName("citt2Datafin")
    val citizenship2EndDate: String? = null,

    /** Codice della terza cittadinanza. */
    @SerialName("citt3Cod")
    val citizenship3Code: String? = null,

    /** Descrizione della terza cittadinanza. */
    @SerialName("citt3Des")
    val citizenship3Description: String? = null,

    /** Codice della nazione della terza cittadinanza. */
    @SerialName("citt3NazioneCod")
    val citizenship3CountryCode: String? = null,

    /** Data di inizio validità della terza cittadinanza. */
    @SerialName("citt3Dataini")
    val citizenship3StartDate: String? = null,

    /** Data di fine validità della terza cittadinanza. */
    @SerialName("citt3Datafin")
    val citizenship3EndDate: String? = null,

    /** Codice internazionale della nazione di cittadinanza principale. */
    @SerialName("naziCittadCodInt")
    val citizenshipNationInternationalCode: String? = null,

    /** Descrizione della nazione di cittadinanza principale. */
    @SerialName("naziCittadDes")
    val citizenshipNationDescription: String? = null,

    /** Prefisso del numero di cellulare. */
    @SerialName("prefixCell")
    val mobilePrefix: String? = null,

    /** Consenso per il trattamento dei dati personali. */
    @SerialName("consDpFlg")
    val consentDpFlag: Int? = null,

    /** Flag di consenso per la diffusione dei dati personali. */
    @SerialName("consDiffDpFlg")
    val consentDifferentDpFlag: Int? = null,

    /** Consenso alla notifica tramite messaggistica SMS. */
    @SerialName("consSmsFlg")
    val consentSmsFlag: Int? = null,

    /** Consenso alla comunicazione dei dati personali. */
    @SerialName("consComunicDpFlg")
    val consentCommunicationDpFlag: Int? = null,

    /** Consenso invio dei dati all'ente regionale. */
    @SerialName("consComunicErFlg")
    val consentCommunicationErFlag: Int? = null,

    /** Flag che indica se lo studente appartiene al clero. */
    @SerialName("religiosoFlg")
    val religiousFlag: Int? = null,

    /** Indica se la persona è deceduta. */
    @SerialName("decedutoFlg")
    val deceasedFlag: Int? = null,

    /** Codice esterno di identificazione della persona. */
    @SerialName("extPersCod")
    val externalPersonCode: String? = null,

    /** Nota persona. */
    @SerialName("notaPers")
    val personalNote: String? = null,

    /** Descrizione della professione. */
    @SerialName("professione")
    val profession: String? = null,

    /** Codice dello stato civile. */
    @SerialName("statoCivileCod")
    val maritalStatusCode: String? = null,

    /** Descrizione dello stato civile. */
    @SerialName("statoCivileDes")
    val maritalStatusDescription: String? = null,

    /** Nome di una persona da contattare in caso di emergenza. */
    @SerialName("emergNome")
    val emergencyName: String? = null,

    /** Cognome di una persona da contattare in caso di emergenza. */
    @SerialName("emergCognome")
    val emergencySurname: String? = null,

    /** Telefono di una persona da contattare in caso di emergenza. */
    @SerialName("emergTel")
    val emergencyPhone: String? = null,

    /** Prefisso telefonico internazionale di una persona da contattare in caso di emergenza.. */
    @SerialName("emergPrefixInternaz")
    val emergencyInternationalPrefix: String? = null,

    /** Indirizzo e-mail di una persona da contattare in caso di emergenza. */
    @SerialName("emergEmail")
    val emergencyEmail: String? = null,

    /** Rapporto con persona da contattare in caso di emergenza. */
    @SerialName("emergRapporto")
    val emergencyRelationship: String? = null,

    /** nome alias */
    @SerialName("nomeAlias")
    val aliasName: String? = null
)

@Serializable
data class Esse3ExternalSubjectReplica(
    /** Identificativo soggetto esterno. */
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    /** ID numerico univoco del dipartimento */
    @SerialName("dipId")
    val departmentId: Long? = null,

    /** Descrizione del dipartimento. */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** Cognome. */
    @SerialName("cognome")
    val surname: String? = null,

    /** appellativo. */
    @SerialName("appellativo")
    val title: String? = null,

    /** Nome. */
    @SerialName("nome")
    val name: String? = null,

    /** Codice fiscale. */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Genere. */
    @SerialName("sesso")
    val gender: String? = null,

    /** Data di nascita. */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** Codice tipologia soggetto esterno. */
    @SerialName("tipoSoggEstCod")
    val externalSubjectTypeCode: String? = null,

    /** Data inizio attività */
    @SerialName("dataIniAtt")
    val activityStartDate: String? = null,

    /** Data fine attività */
    @SerialName("dataFinAtt")
    val activityEndDate: String? = null,

    /** id dell sdr */
    @SerialName("sdrId")
    val siteId: Long? = null,

    /** Indirizzo di posta elettronica del soggetto esterno, ossia visulizzato nel web e stampato nei vari documenti (es. Guida studente) */
    @SerialName("email")
    val email: String? = null,

    /** codice della struttura didattica responsabile */
    @SerialName("strutSdrCod")
    val structureSiteCode: String? = null,

    /** descrizione della struttura didattica responsabile */
    @SerialName("strutSdrDes")
    val structureSiteDescription: String? = null,

    /** descrizione della tipologia della struttura didattica */
    @SerialName("strutSdrTip")
    val structureSiteType: String? = null,

    /** id della nazione di nazita */
    @SerialName("naziNascId")
    val birthNationId: Long? = null,

    /** codice nazione di nascita */
    @SerialName("naziNascCod")
    val birthNationCode: String? = null,

    /** descrizione nazione di nascita */
    @SerialName("naziNascDes")
    val birthNationDescription: String? = null,

    /** codice   nazione di nascita */
    @SerialName("naziNascCodInt")
    val birthNationInternationalCode: String? = null,

    /** id comune nascita */
    @SerialName("comuNascId")
    val birthMunicipalityId: Long? = null,

    /** codice comune di nascita */
    @SerialName("comuNascCod")
    val birthMunicipalityCode: String? = null,

    /** codice comune catastale di nascita */
    @SerialName("comuNascCodCatastale")
    val birthMunicipalityCadastralCode: String? = null,

    /** codice comune di nascita istat miur */
    @SerialName("comuNascCodIstatMiur")
    val birthMunicipalityMiurIstatCode: String? = null,

    /** descrizione comune di nascita */
    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    /** sigla provincia di nascita */
    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    /** descrizione provincia di nascita */
    @SerialName("provNascDes")
    val birthProvinceDescription: String? = null,

    /** descrizione citta straniera  di nascita */
    @SerialName("cistraNasc")
    val birthForeignCitizenship: String? = null,

    /** codice cittadinanza */
    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    /** descrizione cittadinanza */
    @SerialName("cittadDes")
    val citizenshipDescription: String? = null,

    /** numero di telefono */
    @SerialName("tel")
    val phone: String? = null,

    /** prefisso internazionale */
    @SerialName("prefixInternaz")
    val internationalPrefix: String? = null,

    /** fax */
    @SerialName("fax")
    val fax: String? = null,

    /** cellulare */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** operatore cellulare */
    @SerialName("operCellulare")
    val mobileOperator: Long? = null,

    /** operator cellulare */
    @SerialName("operCellulareDes")
    val mobileOperatorDescription: String? = null,

    /** id utente di inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** id utente di ultima  modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** modalita inserimento dati */
    @SerialName("modInsDati")
    val dataInsertionMode: Long? = null,

    /** descrizione tipo soggetto esterno. */
    @SerialName("tipiSoggEstDes")
    val externalSubjectTypesDescription: String? = null,

    /** pers ate flg */
    @SerialName("persAteFlg")
    val atePersonFlag: Long? = null,

    /** descrizione del tipo struttura didattica responsabile */
    @SerialName("tipiSdrDes")
    val siteTypesDescription: String? = null,

    /** Firma. */
    @SerialName("firmaId")
    val signatureId: Long? = null,

    /** nominativo  alterrnativo del soggetto esterno */
    @SerialName("nominativoAlt")
    val alternativeFullName: String? = null,

    /** Id address book della persona in ugov. */
    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    /** consenso alla notifica tramite sms */
    @SerialName("consSmsFlg")
    val consentSmsFlag: Long? = null,

    /** id ateneo che detien le credenziali del soggetto esterno */
    @SerialName("ateIdAccreditamento")
    val ateAccreditationId: Long? = null,

    /** codice istat dell'ateneo */
    @SerialName("istatCod")
    val istatCode: String? = null,

    /** descrizione dell'ateneo */
    @SerialName("ateneoDes")
    val universityDescription: String? = null,

    @SerialName("consensiSoggEsterni")
    val consentsExternalSubjects: List<Esse3ExternalSubjectsConsentsReplica> = emptyList(),

    @SerialName("entiSoggEsterni")
    val externalSubjectsEntities: List<Esse3ExternalEntitiesReplica> = emptyList()
)

@Serializable
data class Esse3AuthorizationAttachmentMetadata(
    /** nome del file */
    @SerialName("filename")
    val fileName: String = "",

    /** autore del file */
    @SerialName("autore")
    val author: String? = null,

    /** titolo del file */
    @SerialName("titolo")
    val title: String = "",

    /** descrizione libera */
    @SerialName("descrizione")
    val description: String = "",

    /** tipologia dell'allegato relativo all'entità p17_tipologia_allegati */
    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    /** flag che indica se l'allegato deve già risultare validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int = 0,

    /** flag che indica se l'allegato deve già risultare visibile da web o meno */
    @SerialName("abilVisWeb")
    val webVisibility: Int = 0
)

@Serializable
data class Esse3HandicapDeclarationAll(
    /** ID dichiarazione di invalidità */
    @SerialName("dicHandId")
    val handicapDeclarationId: Int? = null,

    /** userId attivo collegato al docente */
    @SerialName("userId")
    val userId: String? = null,

    /** nome dell'utente */
    @SerialName("nome")
    val name: String? = null,

    /** cognome dell'utente */
    @SerialName("cognome")
    val surname: String? = null,

    /** identificativo allegato */
    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    /** titolo del file */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione dell'allegato */
    @SerialName("des")
    val description: String? = null,

    /** estensione del file allegato */
    @SerialName("estensione")
    val extension: String? = null,

    /** flag che indica se l'allegato deve già risultare validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int? = null,

    /** valore dell'ENUM ALLEGATI_TYPE collegata al tipo di allegato */
    @SerialName("allegatoTypeCod")
    val attachmentCategoryCode: String? = null,

    /** nome del file */
    @SerialName("filename")
    val fileName: String? = null
)

@Serializable
data class Esse3GetGenericAttachmentMetadata(
    /** codice tipo associativa allegato */
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    /** identificativo allegato */
    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    /** dimensione allegato in byte */
    @SerialName("dimensione")
    val size: Long? = null,

    /** titolo allegato */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione allegato */
    @SerialName("des")
    val description: String? = null,

    /** nome file */
    @SerialName("filename")
    val fileName: String? = null,

    /** estensione file */
    @SerialName("estensione")
    val extension: String? = null,

    /** autore allegato */
    @SerialName("autore")
    val author: String? = null,

    /** data inserimento allegato */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data ultima modifica allegato */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** codice tipologia allegato */
    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null
)

@Serializable
data class Esse3TutorsRulesDetail(
    /** Identificativo dettaglio regole di richiesta tutori. */
    @SerialName("regTutoriDettId")
    val tutorsDetailRegistrationId: Long? = null,

    /** Identificativo dettaglio regole di richiesta tutori. */
    @SerialName("regTutoriTstId")
    val tutorsTestRegistrationId: Long? = null,

    /** Codice dettaglio regole di richiesta tutori. */
    @SerialName("cod")
    val code: String? = null,

    /** Descrizione dettaglio regole di richiesta tutori. */
    @SerialName("des")
    val description: String? = null,

    /** Etichetta dettaglio regole di richiesta tutori */
    @SerialName("etichetta")
    val label: String? = null,

    /** Nota dettaglio regole di richiesta tutori */
    @SerialName("nota")
    val note: String? = null,

    /** Tipo parentela regola di richiesta tutori */
    @SerialName("tipoParCod")
    val paragraphTypeCode: String? = null,

    /** Descrizione tipo parentela regola di richiesta tutori */
    @SerialName("tipoParDes")
    val paragraphTypeDescription: String? = null,

    /** N. minimo componenti del tipo parentela nella regola di richiesta tutori */
    @SerialName("nMin")
    val minNumber: Int? = null,

    /** N. massimo componenti del tipo parentela nella regola di richiesta tutori */
    @SerialName("nMax")
    val maxNumber: Int? = null,

    /** Abilita visibilità del dettaglio di richiesta tutori. */
    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    /** Utente di inserimento. */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Utente di ultima modifica. */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data di inserimento. */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Data di ultima modifica. */
    @SerialName("dataMod")
    val modificationDate: String? = null
)

@Serializable
data class Esse3IdentityDocumentAttachmentMetadata(
    /** nome del file */
    @SerialName("filename")
    val fileName: String = "",

    /** autore del file */
    @SerialName("autore")
    val author: String? = null,

    /** titolo del file */
    @SerialName("titolo")
    val title: String = "",

    /** descrizione libera */
    @SerialName("descrizione")
    val description: String = "",

    /** tipologia dell'allegato relativo all'entità p17_tipologia_allegati */
    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    /** flag che indica se l'allegato deve già risultare validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int = 0,

    /** codice tipo di documento d'identità (CI - Carta Identità, PAT - Patente, PAS - Passaporto) */
    @SerialName("docIdentTipoCod")
    val identityDocumentTypeCode: String = "",

    /** identificativo documento identità */
    @SerialName("docPersId")
    val personalDocumentId: Long? = null,

    /** flag che indica se l'allegato deve già risultare visibile da web o meno */
    @SerialName("abilVisWeb")
    val webVisibility: Int = 0
)

@Serializable
data class Esse3PersonCompensatoryMeasuresEvaluation(
    @SerialName("dicHandId")
    val handicapDeclarationId: Long? = null,

    /** id della persona a cui si riferisce la misura compensativa */
    @SerialName("persId")
    val personId: Long? = null,

    /** percentuale di handicap */
    @SerialName("percHand")
    val handicapPercentage: Int? = null,

    /** data di dichiarazione dell'handicap */
    @SerialName("dataDichiar")
    val declarationDate: String? = null,

    /** stato della dichiarazione di handicap */
    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    /** id della misura compensativa dello studente per il bisogno speciale */
    @SerialName("dicHandMisureId")
    val handicapDeclarationMeasuresId: Long? = null,

    /** codice della misura compensativa richeista */
    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String? = null,

    /** descrizione libera della miusura compensativa */
    @SerialName("misuraCompensativaDes")
    val compensatoryMeasureDescription: String? = null,

    @SerialName("desLiberaFlg")
    val freeDescriptionFlag: Int? = null,

    /** indica se la misura compensativa è visibile da web */
    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    /** stato della misura compensativa */
    @SerialName("statoMisuraComp")
    val compensatoryMeasureState: String? = null,

    /** descrizione stato della misura compensativa */
    @SerialName("statoMisuraCompDes")
    val compensatoryMeasureStateDescription: String? = null,

    /** data di inizio validità della misura compensativa */
    @SerialName("misuraDataIni")
    val measureStartDate: String? = null,

    /** data di fine validità della misura compensativa */
    @SerialName("misuraDataFine")
    val measureEndDate: String? = null
)

@Serializable
data class Esse3TeachersTimetable(
    /** chiave dell'orario docente */
    @SerialName("docenteOrarioId")
    val lecturerScheduleId: Long = 0L,

    /** chiave del docente */
    @SerialName("docenteId")
    val lecturerId: Long = 0L,

    /** giorno della settimana */
    @SerialName("giorno")
    val day: Long? = null,

    /** giorno della settimana */
    @SerialName("giornoDes")
    val dayDescription: String? = null,

    /** ora inizio dell'appuntemento. */
    @SerialName("oraInizio")
    val startTime: String? = null,

    /** ora fine dell'appuntemento. */
    @SerialName("oraFine")
    val endTime: String? = null,

    /** luogo dell'appuntamento */
    @SerialName("desLuogo")
    val placeDescription: String? = null,

    /** nota collegata all'appuntamento */
    @SerialName("nota")
    val note: String? = null
)

@Serializable
data class Esse3RelationshipTypes(
    /** Codice tipo parentela. */
    @SerialName("tipoParCod")
    val paragraphTypeCode: String? = null,

    /** Descrizione tipo parentela. */
    @SerialName("des")
    val description: String? = null,

    /** Indica se il record è di sistema oppure no: nel primo caso, il record NON è cancellabile, mentre nel secondo sì. */
    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    /** Indica la percentuale di reddito da considerare in fase di autocertificazione, per questo grado di parentela. */
    @SerialName("percPesoReddito")
    val incomeWeightPercentage: Int? = null,

    /** Indica la visibità del tipo di parentela nei processi online. */
    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null
)

@Serializable
data class Esse3GetItalianTitleAttachmentMetadata(
    /** codice tipo associativa allegato */
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    /** identificativo allegato */
    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    /** dimensione allegato in byte */
    @SerialName("dimensione")
    val size: Long? = null,

    /** titolo allegato */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione allegato */
    @SerialName("des")
    val description: String? = null,

    /** nome file */
    @SerialName("filename")
    val fileName: String? = null,

    /** estensione file */
    @SerialName("estensione")
    val extension: String? = null,

    /** autore allegato */
    @SerialName("autore")
    val author: String? = null,

    /** data inserimento allegato */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data ultima modifica allegato */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** codice tipologia allegato */
    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    /** flag che indica se l'allegato risulta validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int? = null,

    /** flag che indica se l'allegato risulta stampabile o meno */
    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3PersonalDataHistory(
    /** Identificativo anagrafica storico */
    @SerialName("anaperStoId")
    val personHistoricalId: Long? = null,

    /** Nome */
    @SerialName("nome")
    val name: String? = null,

    /** Cognome */
    @SerialName("cognome")
    val surname: String? = null,

    /** Patronimico */
    @SerialName("patronimico")
    val patronymic: String? = null,

    /** Nome alias */
    @SerialName("nomeAlias")
    val aliasName: String? = null,

    /** Codice fiscale */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** Sesso */
    @SerialName("sesso")
    val gender: String? = null,

    /** Identificativo nazione di nascita */
    @SerialName("naziNascId")
    val birthNationId: Long? = null,

    /** Codice nazione di nascita */
    @SerialName("naziNascCod")
    val birthNationCode: String? = null,

    /** Descrizione nazione di nascita */
    @SerialName("naziNascDes")
    val birthNationDescription: String? = null,

    /** Codice nazione (ISO) nascita */
    @SerialName("naziNascNazioneCod")
    val birthCountryCode: String? = null,

    /** Codice internazionale nazione di nascita */
    @SerialName("naziNascCodInt")
    val birthNationInternationalCode: String? = null,

    /** Identificativo comune di nascita */
    @SerialName("comuNascId")
    val birthMunicipalityId: Long? = null,

    /** Codice comune di nascita */
    @SerialName("comuNascCod")
    val birthMunicipalityCode: String? = null,

    /** Codice catastale comune di nascita */
    @SerialName("comuNascCodCatastale")
    val birthMunicipalityCadastralCode: String? = null,

    /** Codice ISTAT/MIUR comune di nascita */
    @SerialName("comuNascCodIstatMiur")
    val birthMunicipalityMiurIstatCode: String? = null,

    /** Descrizione comune di nascita */
    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    /** Sigla provincia comune di nascita */
    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    /** Provincia di nascita */
    @SerialName("provNascDes")
    val birthProvinceDescription: String? = null,

    /** Codice cittadinanza 1 */
    @SerialName("citt1Cod")
    val citizenship1Code: String? = null,

    /** Descrizione cittadinanza 1 */
    @SerialName("citt1Des")
    val citizenship1Description: String? = null,

    /** Codice nazione cittadinanza 1 */
    @SerialName("citt1NazioneCod")
    val citizenship1CountryCode: String? = null,

    /** Codice cittadinanza 2 */
    @SerialName("citt2Cod")
    val citizenship2Code: String? = null,

    /** Descrizione cittadinanza 2 */
    @SerialName("citt2Des")
    val citizenship2Description: String? = null,

    /** Codice nazione cittadinanza 2 */
    @SerialName("citt2NazioneCod")
    val citizenship2CountryCode: String? = null,

    /** Codice cittadinanza 3 */
    @SerialName("citt3Cod")
    val citizenship3Code: String? = null,

    /** Descrizione cittadinanza 3 */
    @SerialName("citt3Des")
    val citizenship3Description: String? = null,

    /** Codice nazione cittadinanza 3 */
    @SerialName("citt3NazioneCod")
    val citizenship3CountryCode: String? = null,

    /** Flag consenso differente dati personali */
    @SerialName("consDiffDpFlg")
    val consentDifferentDpFlag: Int? = null,

    /** Flag consenso comunicazioni dati personali */
    @SerialName("consComunicDpFlg")
    val consentCommunicationDpFlag: Int? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null
)

@Serializable
data class Esse3EnrollmentNumberAttachmentMetadata(
    /** nome del file */
    @SerialName("filename")
    val fileName: String = "",

    /** autore del file */
    @SerialName("autore")
    val author: String? = null,

    /** titolo del file */
    @SerialName("titolo")
    val title: String = "",

    /** descrizione libera */
    @SerialName("descrizione")
    val description: String = "",

    /** tipologia dell'allegato relativo all'entità p17_tipologia_allegati */
    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    /** flag che indica se l'allegato deve già risultare validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int = 0,

    /** flag che indica se l'allegato deve già risultare visibile da web o meno */
    @SerialName("abilVisWeb")
    val webVisibility: Int = 0
)

@Serializable
data class Esse3BankDetails(
    /** id univoco che consente di individuare la persona */
    @SerialName("persId")
    val personId: Long = 0L,

    /** Tipologia di dato bancario (RIMB rimborso o PAG pagamento) */
    @SerialName("tipoDatiBancaCod")
    val bankDataTypeCode: String? = null,

    /** descrizione Tipologia di dato bancario */
    @SerialName("tipiDatiBancaDes")
    val bankDataTypesDescription: String? = null,

    /** Tipo rimborso tasse usato come default per la persona (PF Posta file MAV MAV PD Posta diretto BD Banca diretto POS Bancomat/Carta di credito RBB Rimborso tramite bonifico RBD Rimborso bancario diretto CC Carta di credito on-line RBP Rimborso tramite banco posta BB  Pagamento tramite bonifico bancario BORSA Borsa di studio RID Rapporto Interbancario Diretto.) */
    @SerialName("tipoRimbPagCod")
    val paymentRefundTypeCode: String? = null,

    /** descrizione  Tipo rimborso tasse */
    @SerialName("tipiRimbPagDes")
    val paymentRefundTypesDescription: String? = null,

    /** Descrizione della banca per il dato bancario inserito */
    @SerialName("bancaDes")
    val bankDescription: String? = null,

    /** conto corrente di rimborso. */
    @SerialName("ccIntestatario")
    val currentAccountHolder: String? = null,

    /** Codice fiscale intestatario del conto corrente di rimborso. */
    @SerialName("ccIntestatarioCf")
    val currentAccountHolderFiscalCode: String? = null,

    /** Codice IBAN (coordinate bancarie internazionali) del debitore per addebito automatico. */
    @SerialName("ibanCod")
    val ibanCode: String? = null,

    /** Numero del conto corrente straniero. */
    @SerialName("numConto")
    val accountNumber: String? = null,

    /** ID numerico univoco della nazione. */
    @SerialName("nazioneId")
    val nationId: Int? = null,

    /** Codice nazione. */
    @SerialName("naziCod")
    val nationCode: String? = null,

    /** Descrizione nazione. */
    @SerialName("naziDes")
    val nationDescription: String? = null,

    /** Codice fiscale nazione. */
    @SerialName("naziCodFis")
    val nationFiscalCode: String? = null,

    /** Codice BIC (SWIFT) del conto corrente straniero. */
    @SerialName("swiftCod")
    val swiftCode: String? = null
)

@Serializable
data class Esse3GetPersonalDocumentAuthorizationMetadata(
    /** codice tipo associativa allegato */
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    /** identificativo allegato */
    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    /** dimensione allegato in byte */
    @SerialName("dimensione")
    val size: Long? = null,

    /** titolo allegato */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione allegato */
    @SerialName("des")
    val description: String? = null,

    /** nome file */
    @SerialName("filename")
    val fileName: String? = null,

    /** estensione file */
    @SerialName("estensione")
    val extension: String? = null,

    /** autore allegato */
    @SerialName("autore")
    val author: String? = null,

    /** data inserimento allegato */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data ultima modifica allegato */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** codice tipologia allegato */
    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    /** flag che indica se l'allegato risulta validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int? = null,

    /** flag che indica se l'allegato risulta visibile da web o meno */
    @SerialName("abilVisWeb")
    val webVisibility: Int? = null,

    /** flag che indica se l'allegato risulta stampabile o meno */
    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3ForeignTitleAttachmentMetadata(
    /** nome del file */
    @SerialName("filename")
    val fileName: String = "",

    /** autore del file */
    @SerialName("autore")
    val author: String? = null,

    /** titolo del file */
    @SerialName("titolo")
    val title: String = "",

    /** descrizione libera */
    @SerialName("descrizione")
    val description: String = "",

    /** tipologia dell'allegato relativo all'entità p17_tipologia_allegati */
    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    /** flag che indica se l'allegato deve già risultare validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int = 0,

    /** Anno Accademico di conseguimento del titolo straniero */
    @SerialName("aaConsegTit")
    val academicYearAwardedTitle: Int = 0,

    /** codice tipo titolo straniero */
    @SerialName("tipoTitstCod")
    val titleStatusTypeCode: String? = null,

    /** identificativo del titolo straniero */
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
    /** id della misura compensativa dello studente per il bisogno speciale */
    @SerialName("dicHandMisureId")
    val handicapDeclarationMeasuresId: Long? = null,

    /** id della persona a cui si riferisce la misura compensativa */
    @SerialName("persId")
    val personId: Long? = null,

    /** codice della tipologia di handicap a cui fa riferimento la misura compensativa */
    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    /** percentuale di handicap */
    @SerialName("percHand")
    val handicapPercentage: Int? = null,

    /** data di dichiarazione dell'handicap */
    @SerialName("dataDicharaz")
    val declarationDate: String? = null,

    /** indica se lo stato della dichiarazione è valido ai fini dei processi che utilizzano la dichiarazione stessa */
    @SerialName("statoDicHandValidoFlg")
    val validHandicapDeclarationStateFlag: Int? = null,

    /** stato della dichiarazione di handicap */
    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    /** codice della misura compensativa richeista */
    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String? = null,

    /** descrizione libera della miusura compensativa */
    @SerialName("misuraCompensativaDes")
    val compensatoryMeasureDescription: String? = null,

    /** indica se la descrizione è personalizzata per il singolo studente */
    @SerialName("misuraDesLiberaFlg")
    val freeMeasureDescriptionFlag: Int? = null,

    /** indica se la misura compensativa è visibile da web */
    @SerialName("misuraVisWebFlg")
    val webVisibleMeasureFlag: Int? = null,

    /** stato della misura compensativa */
    @SerialName("statoMisuraComp")
    val compensatoryMeasureState: String? = null,

    /** data di inizio validità della misura compensativa */
    @SerialName("misuraDataIni")
    val measureStartDate: String? = null,

    /** data di fine validità della misura compensativa */
    @SerialName("misuraDataFine")
    val measureEndDate: String? = null,

    /** data di inizio invalidità. */
    @SerialName("dichHandDataIni")
    val handicapDeclarationStartDate: String? = null,

    /** data fine invalidità. */
    @SerialName("dichHandDataFine")
    val handicapDeclarationEndDate: String? = null
)

@Serializable
data class Esse3GetHighSchoolDiplomaAttachmentMetadata(
    /** codice tipo associativa allegato */
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    /** identificativo allegato */
    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    /** dimensione allegato in byte */
    @SerialName("dimensione")
    val size: Long? = null,

    /** titolo allegato */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione allegato */
    @SerialName("des")
    val description: String? = null,

    /** nome file */
    @SerialName("filename")
    val fileName: String? = null,

    /** estensione file */
    @SerialName("estensione")
    val extension: String? = null,

    /** autore allegato */
    @SerialName("autore")
    val author: String? = null,

    /** data inserimento allegato */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data ultima modifica allegato */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** codice tipologia allegato */
    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    /** flag che indica se l'allegato risulta validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int? = null,

    /** flag che indica se l'allegato risulta stampabile o meno */
    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3ExternalSubject(
    /** Identificativo soggetto esterno. */
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    /** identificativo U-gov. */
    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    /** Cognome. */
    @SerialName("cognome")
    val surname: String? = null,

    /** Nome. */
    @SerialName("nome")
    val name: String? = null,

    /** Codice fiscale. */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Genere. */
    @SerialName("sesso")
    val gender: String? = null,

    /** Data di nascita. */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** Codice tipologia soggetto esterno. */
    @SerialName("tipoSoggEstCod")
    val externalSubjectTypeCode: String? = null,

    /** Descrizione tipologia soggetto esterno. */
    @SerialName("tipoSoggEstDes")
    val externalSubjectTypeDescription: String? = null,

    /** Data inizio attività */
    @SerialName("dataIniAtt")
    val activityStartDate: String? = null,

    /** Data fine attività */
    @SerialName("dataFinAtt")
    val activityEndDate: String? = null,

    /** appellativo del soggetto esterno */
    @SerialName("appellativo")
    val title: String? = null,

    /** Indirizzo di posta elettronica del soggetto esterno, ossia visulizzato nel web e stampato nei vari documenti (es. Guida studente) */
    @SerialName("email")
    val email: String? = null,

    /** descrizione della struttura didattica responsabile */
    @SerialName("strutturaDidattResp")
    val didacticResponsibleStructure: String? = null,

    /** descrizione del dipartimento di appartenenza */
    @SerialName("dipartimento")
    val department: String? = null
)

@Serializable
data class Esse3AnnualEnrollment(
    /** identificativo persona. */
    @SerialName("persId")
    val personId: Long? = null,

    /** identificativo studente. */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** identificativo corso di studio. */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** identificativo anno ordinamento. */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** identificativo percorso di studio. */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** identificativo iscizione. */
    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    /** identificativo carriera. */
    @SerialName("matId")
    val matId: Long? = null,

    /** matricola. */
    @SerialName("matricola")
    val matricola: String? = null,

    /** stato matricola. */
    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    /** identificativo anno di regolamento. */
    @SerialName("aaRegId")
    val academicYearRegulationId: Long? = null,

    /** identificativo anno iscrizione. */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Long? = null,

    /** data iscrizione. */
    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    /** codice facoltà. */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione facoltà. */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** codice corso di studio. */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione corso di studio. */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** codice tipologia corso. */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** descrizione tipologia corso. */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** codice indirizzo. */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** descrizione indirizzo. */
    @SerialName("indirizzoDes")
    val addressDescription: String? = null,

    /** codice orientamento. */
    @SerialName("ordinamentoCod")
    val studyOrderCode: String? = null,

    /** descrizione orientamento. */
    @SerialName("ordinamentoDes")
    val studyOrderDescription: String? = null,

    /** durata corso in anni. */
    @SerialName("durataCorso")
    val courseDuration: Int? = null,

    /** valore minimo di crediti o annualità che deveno essere ottenuti per poter conseguire il titolo. */
    @SerialName("valoreMin")
    val minimumValue: String? = null,

    /** anno di corso. */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** anni fuori corso. */
    @SerialName("anniFc")
    val fcYears: Int? = null,

    /** codice tipologia iscrizione. */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** descrizione tipologia iscrizione. */
    @SerialName("tipoIscrDes")
    val enrollmentTypeDescription: String? = null,

    /** codice stato iscrizione annuale A = ATTIVA, X = ANNULLATA, S = SOSPESA. */
    @SerialName("staIscrCod")
    val enrollmentStatusCode: String? = null,

    /** motivo stato iscrizione. */
    @SerialName("motStaiscrCod")
    val enrollmentStatusReasonCode: String? = null,

    /** flag che indica se l'iscrizione è condizionata (per esempio dal superamento di uno sbarramento). */
    @SerialName("condFlg")
    val conditionFlag: Int? = null,

    /** flag che indica se l'iscrizione annuale è stata EFFETTUATA (0) o RICOSTRUITA a posteriori tramite ricognizione oppure tramite inserimento delle iscrizioni pregresse nel caso di un trasferimento in ingresso (1). */
    @SerialName("ricFlg")
    val searchFlag: Int? = null,

    /** flag che indica se lo studente è iscritto in attesa di laurea. */
    @SerialName("attlauFlg")
    val degreeAwardFlag: Int? = null,

    /** codice ateneo. */
    @SerialName("ateneoCod")
    val universityCode: String? = null,

    /** descrizione ateneo. */
    @SerialName("ateneoDes")
    val universityDescription: String? = null,

    /** sigla ateneo. */
    @SerialName("ateneoSiglaUniv")
    val universityAbbreviation: String? = null,

    /** identificativo sede. */
    @SerialName("sedeId")
    val siteId: Long? = null,

    /** descrizione sede. */
    @SerialName("sedeDes")
    val siteDescription: String? = null,

    /** codice lingua didattica. */
    @SerialName("linguaDid")
    val teachingLanguage: String? = null,

    /** codice normativa. */
    @SerialName("normCod")
    val normCode: String? = null,

    /** data inserimento. */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data modifica. */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** anno ultima iscrizione. */
    @SerialName("aaUltimaIscr")
    val academicYearLastEnrollment: Int? = null,

    /** codice orientamento. */
    @SerialName("orientCod")
    val orientationCode: String? = null,

    /** descrizione orientamento. */
    @SerialName("orientDes")
    val orientationDescription: String? = null,

    /** codice classe MURST. */
    @SerialName("claMurstCod")
    val classMurstCode: String? = null,

    /** descrizione classe MURST. */
    @SerialName("claMurstDes")
    val classMurstDescription: String? = null,

    /** codice classe ateneo. */
    @SerialName("claAteneoCod")
    val classUniversityCode: String? = null,

    /** descrizione classe ateneo. */
    @SerialName("claAteneoDes")
    val classUniversityDescription: String? = null,

    /** flag che indica se l'iscrizione è part-time (1) oppure full-time (0). */
    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    /** numero di CFU scelti per il part time in fase di iscrizione. */
    @SerialName("ptCfu")
    val ptCredits: Int? = null,

    /** identificativo alternativa part-ime scelta dallo studente. */
    @SerialName("aptId")
    val aptId: Long? = null,

    /** numero di CFU extra scelti dallo studente durante l'anno accademico. */
    @SerialName("ptCfuExtra")
    val ptExtraCredits: Int? = null,

    /** flag che impedisce ulteriori modifiche alla scelta del part-time per la particolare iscrizione annuale. */
    @SerialName("ptBloccatoFlg")
    val ptBlockedFlag: Int? = null,

    /** slot alternativa di part-tme. */
    @SerialName("ptSlotId")
    val ptSlotId: Long? = null,

    /** fascia di reddito attribuita allo studente. */
    @SerialName("fasciaId")
    val bandId: Long? = null,

    /** flag che indica se nell'anno dell'iscrizione lo studente era sospeso e quindi se l'iscrizione era fittizia. */
    @SerialName("sospFlg")
    val suspensionFlag: Int? = null,

    /** Causale di sospensione. */
    @SerialName("motSospCod")
    val suspensionReasonCode: String? = null,

    /** Gruppo di rateizzazione, coincide con il numero di rate. */
    @SerialName("tipoGruppoId")
    val groupTypeId: Long? = null,

    /** identificativo fascia di merito. */
    @SerialName("fasMeritoId")
    val meritBandId: Long? = null,

    /** identificativo regola fascia di merito, applicata per il calcolo della fascia di merito. */
    @SerialName("regFasId")
    val bandRegistrationId: Long? = null,

    /** data in cui è stata calcolata la fascia di merito. */
    @SerialName("dtCalcMerito")
    val meritCalculationDate: String? = null,

    /** note derivanti dal calcolo del merito. */
    @SerialName("notaMerito")
    val meritNote: String? = null,

    /** commenti relativi all'iscrizione annuale dello studente. */
    @SerialName("notaIscr")
    val enrollmentNote: String? = null,

    /** flag che indica se la nazione di provenienza dello studente appartiene al gruppo dei Paesi Paricolarmente Poveri in base alla classificazione fornita dal decreto ministeriale emanato ogni anno. */
    @SerialName("povFlg")
    val povFlag: Int? = null,

    /** flag che indica se la nazione di provenienza dello studente appartiene alla Unione Europea. */
    @SerialName("ueFlg")
    val ueFlag: Int? = null,

    /** identificativo nazione provenienza. */
    @SerialName("nazioneProvId")
    val provinceNationId: Long? = null,

    /** identificativo fascia mensa. */
    @SerialName("fasciaMensaId")
    val canteenBandId: Long? = null,

    /** codice tipologia handicap. */
    @SerialName("codTipoHandicap")
    val handicapTypeCode: String? = null,

    /** descrizione tipologia handicap. */
    @SerialName("desTipoHandicap")
    val handicapTypeDescription: String? = null,

    /** percentuale handicap */
    @SerialName("percHandicap")
    val disabilityPercentage: Float? = null,

    /** codice tipo posto riservato per dottorandi di ricerca OSB - Ordinario senza borsa, OCB - Ordinario con borsa, SOP - Soprannumerario. */
    @SerialName("tipoPostoRisCod")
    val reservedSeatTypeCode: String? = null,

    /** descrizione tipo posto riservato per dottorandi di ricerca. */
    @SerialName("tipoPostoRiservato")
    val reservedSeatType: String? = null,

    /** codice classe iscrizione. */
    @SerialName("codiceClasseIscrizione")
    val enrollmentClassCode: String? = null,

    /** codice tipologia didattica. */
    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    /** descrizione tipologia didattica. */
    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    /** codice tipologia esonero. */
    @SerialName("tipoEsoCod")
    val exemptionTypeCode: String? = null,

    /** descrizione tipologia esonero. */
    @SerialName("tipoEsoDes")
    val exemptionTypeDescription: String? = null,

    /** data di inizio del contratto di specializzazione. */
    @SerialName("dataIniContratto")
    val contractStartDate: String? = null,

    /** identificativo orientamento percorso di studio. */
    @SerialName("orientId")
    val orientationId: Long? = null,

    /** fascia di reddito dichiarata dallo studente. */
    @SerialName("fasciaDichiarId")
    val bandDeclarationId: Long? = null,

    /** identificativo polo. */
    @SerialName("poloId")
    val poleId: Long? = null,

    /** indica se lo studente è un rifugiato politico. */
    @SerialName("rifPolFlg")
    val policyReferenceFlag: Int? = null,

    /** data attesa laurea. */
    @SerialName("dataAttlau")
    val degreeAwardDate: String? = null,

    /** codice tipologia studente. */
    @SerialName("tipoStuCod")
    val studentTypeCode: String? = null,

    /** codice tipologia studente. */
    @SerialName("tipoStuDes")
    val studentTypeDescription: String? = null,

    /** usato per inserimento iscrizioni pregresso, indica se il dato è certificato dall'operatore di segreteria. 0 - inserite dallo studente, 1- certificato dall'utente di segreteria. */
    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null
)

@Serializable
data class Esse3HandicapTypesLookup(
    /** Codice tipo handicap. */
    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    /** Descrizione tipo handicap. */
    @SerialName("des")
    val description: String? = null,

    /** Indica che la tipologia di handicap non abilita l'inserimento di percentuali. */
    @SerialName("disabilPercFlg")
    val disabilityPercentageFlag: Int? = null,

    /** Indica che la tipologia di handicap è una disabilità (invalidità riconosciuta ASL). */
    @SerialName("disabilitaFlg")
    val disabilityFlag: Int? = null,

    /** Indica che la tipologia di handicap è una invalidità legge 104 (riconosciuta da INPS). */
    @SerialName("flg104")
    val law104Flag: Int? = null,

    /** Indica l'ordine in cui devono essere presentate le tipologie di invalidità da web. Se 0 non viene visualizzato, se 1 è quello preselezionato. */
    @SerialName("ordWeb")
    val orderWeb: Int? = null
)

@Serializable
data class Esse3HandicapDeclarationReplica(
    /** Identificativo dichiarazione */
    @SerialName("dichiarId")
    val declarationId: Long? = null,

    /** Tipo handicap */
    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    /** Descrizione tipo handicap */
    @SerialName("tipoHandicapDes")
    val handicapTypeDescription: String? = null,

    /** Flag disabilità percentuale */
    @SerialName("disabilPercFlg")
    val disabilityPercentageFlag: Int? = null,

    /** Flag disabilità */
    @SerialName("disabilitaFlg")
    val disabilityFlag: Int? = null,

    /** Flag inv. 104 */
    @SerialName("inv104Flg")
    val law104InvitationFlag: Int? = null,

    /** Percentuale handicap */
    @SerialName("percHand")
    val handicapPercentage: Int? = null,

    /** Data dichiarazione */
    @SerialName("dataDichiar")
    val declarationDate: String? = null,

    /** Stato dichiarazione handicap */
    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    /** Descrizione stato dichiarazione handicap */
    @SerialName("statoDicHandDes")
    val handicapDeclarationStateDescription: String? = null,

    /** Data inizio stato */
    @SerialName("dataIniStato")
    val stateStartDate: String? = null,

    /** Flag tutorato */
    @SerialName("tutoratoFlg")
    val tutoringFlag: Int? = null,

    /** Flag autorizzazione tutor */
    @SerialName("autTutorFlg")
    val tutorAuthorizationFlag: Int? = null,

    /** Data inizio */
    @SerialName("dataIni")
    val startDate: String? = null,

    /** Data fine */
    @SerialName("dataFine")
    val endDate: String? = null,

    /** Identificativo anno accademico inizio */
    @SerialName("aaIdCompIni")
    val academicYearComponentStartId: Int? = null,

    /** Identificativo anno accademico fine */
    @SerialName("aaIdCompFine")
    val academicYearComponentEndId: Int? = null,

    /** Note aggiuntive */
    @SerialName("nota")
    val note: String? = null,

    /** Flag consenso DS */
    @SerialName("consDsFlg")
    val consentDsFlag: Int? = null,

    /** Codice normativa handicap */
    @SerialName("handNormativaCod")
    val handicapRegulationCode: String? = null,

    /** Descrizione normativa handicap */
    @SerialName("handNormativaDes")
    val handicapRegulationDescription: String? = null,

    /** Flag BES check */
    @SerialName("besCheckFlg")
    val besCheckFlag: Int? = null,

    /** Identificativo dichiarazione handicap */
    @SerialName("dicHandId")
    val handicapDeclarationId: Long? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("misureComp")
    val compensatoryMeasures: List<Esse3HandicapDeclarationCompensatoryMeasures> = emptyList()
)

@Serializable
data class Esse3University(
    /** id univoco ateneo. */
    @SerialName("ateneoId")
    val universityId: Int? = null,

    /** Codice ISTAT dell'ateneo. */
    @SerialName("istatCod")
    val istatCode: String? = null,

    /** Descrizione. */
    @SerialName("des")
    val description: String? = null,

    /** Indirizzo. */
    @SerialName("via")
    val street: String? = null,

    /** CAP. */
    @SerialName("cap")
    val postalCode: String? = null,

    /** Città. */
    @SerialName("citta")
    val city: String? = null,

    /** Provincia. */
    @SerialName("prov")
    val province: String? = null,

    /** Codice fiscale dell'Ateneo. */
    @SerialName("cf")
    val fiscalCode: String? = null,

    /** Partita Iva dell'Ateneo. */
    @SerialName("piva")
    val vatNumber: String? = null,

    /** Descrizione MAV1. */
    @SerialName("desMav1")
    val mav1Description: String? = null,

    /** Descrizione MAV2. */
    @SerialName("desMav2")
    val mav2Description: String? = null,

    /** Descrizione MAV3. */
    @SerialName("desMav3")
    val mav3Description: String? = null,

    /** Descrizione MAV4. */
    @SerialName("desMav4")
    val mav4Description: String? = null,

    /** Prefisso ateneo per Alma Laurea. */
    @SerialName("almaPref")
    val almaPrefix: String? = null,

    /** Descrizione usata nella stampa del promemoria per pagamenti di tipo Banca diretto. */
    @SerialName("desBd1")
    val bd1Description: String? = null,

    /** Codice università MIUR. */
    @SerialName("codeUn")
    val unifiedCode: String? = null,

    /** ID numerico unico del comune. */
    @SerialName("comuneId")
    val municipalityId: Int? = null,

    /** Descrizione del comune. */
    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    /** Codice usato nel codice fiscale per identificare il comune di nascita. */
    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    /** Sigla provincia. */
    @SerialName("comuneSigla")
    val municipalityAbbreviation: String? = null,

    /** Codice ISTAT del comune. */
    @SerialName("comuneCodIstat")
    val municipalityIstatCode: String? = null,

    /** CAP. */
    @SerialName("comuneCap")
    val municipalityPostalCode: String? = null,

    /** Data dell'ultima elaborazione della funzione di generazione FILE ASCII per integrazione con CSA. */
    @SerialName("csaUltElab")
    val csaLastProcessing: String? = null,

    /** Url del sito web della guida studente dell'Ateneo, esterno ad Esse3.. */
    @SerialName("urlGuidaWeb")
    val webGuideUrl: String? = null,

    /** Codice Erasmus dell'Ateneo. */
    @SerialName("erasmusCod")
    val erasmusCode: String? = null,

    /** Nome del prodotto. */
    @SerialName("prodotto")
    val product: String? = null,

    /** Flag web. */
    @SerialName("webFlg")
    val webFlag: Int? = null,

    /** Indica se il record è di sistema. */
    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    /** Codice della Tipologia di istituzione universitaria. */
    @SerialName("tipoUnivCod")
    val universityTypeCode: String? = null,

    /** Descrizione della Tipologia di Istituzione Universitaria. */
    @SerialName("tipiUnivDes")
    val universityTypesDescription: String? = null,

    /** Eventuali ulteriori descrizioni sulla tipologia di Istituzione Universitaria. */
    @SerialName("tipiUnivNote")
    val universityTypesNotes: String? = null,

    /** Note ateneo. */
    @SerialName("note")
    val notes: String? = null,

    /** Codice del Contratto Istituzionale dell'Ateneo. */
    @SerialName("icNumber")
    val icNumber: String? = null,

    /** Sigla o acronimo dell'Ateneo, generalmente in uso (anche non ufficiale).. */
    @SerialName("siglaUniv")
    val universityAbbreviation: String? = null,

    /** Telefono. */
    @SerialName("telefono")
    val phone: String? = null,

    /** Fax. */
    @SerialName("fax")
    val fax: String? = null,

    /** ID numerico univoco della nazione. */
    @SerialName("nazioneId")
    val nationId: Int? = null,

    /** Codice nazione. */
    @SerialName("nazioneCod")
    val nationCode: String? = null,

    /** Descrizione nazione. */
    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    /** Codice fiscale nazione. */
    @SerialName("nazioneCodFisc")
    val nationFiscalCode: String? = null,

    /** Codice MIUR nazione. */
    @SerialName("nazioneNazioneCod")
    val nationNationCode: String? = null,

    /** DES_CERT */
    @SerialName("desCert")
    val certificateDescription: String? = null,

    /** caso genitivo della DES_CERT, indica l'appartenza (DI). */
    @SerialName("desCertGenit")
    val parentsCertificateDescription: String? = null,

    /** caso locativo della DES_CERT (PRESSO). */
    @SerialName("desCertLocat")
    val locationCertificateDescription: String? = null,

    /** caso vocativo della DES_CERT (DOVE). */
    @SerialName("desCertVocat")
    val vocationCertificateDescription: String? = null,

    /** Indirizzo e-mail. */
    @SerialName("email")
    val email: String? = null,

    /** Identificativo dell'intermediario per click-lavoro. */
    @SerialName("cvIdIntermediario")
    val cvIntermediaryId: String? = null,

    /** Indirizzo e-mail principale dell'intermediario per click-lavoro. */
    @SerialName("cvEmail")
    val cvEmail: String? = null,

    /** Codice IPA d'Ateneo. */
    @SerialName("ipaCod")
    val ipaCode: String? = null,

    /** Codice AOO d'Ateneo. */
    @SerialName("aooCod")
    val officeCode: String? = null,

    /** Identificativo issuer BESTR. */
    @SerialName("bestrCod")
    val bestPracticeCode: String? = null,

    /** Indirizzo e-mail certificato (PEC). */
    @SerialName("emailCertificata")
    val certifiedEmail: String? = null
)

@Serializable
data class Esse3GetForeignTitleAttachmentMetadata(
    /** codice tipo associativa allegato */
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    /** identificativo allegato */
    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    /** dimensione allegato in byte */
    @SerialName("dimensione")
    val size: Long? = null,

    /** titolo allegato */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione allegato */
    @SerialName("des")
    val description: String? = null,

    /** nome file */
    @SerialName("filename")
    val fileName: String? = null,

    /** estensione file */
    @SerialName("estensione")
    val extension: String? = null,

    /** autore allegato */
    @SerialName("autore")
    val author: String? = null,

    /** data inserimento allegato */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data ultima modifica allegato */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** codice tipologia allegato */
    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    /** flag che indica se l'allegato risulta validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int? = null,

    /** flag che indica se l'allegato risulta stampabile o meno */
    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3ForeignTitlePerson(
    /** Identificativo univoco della persona */
    @SerialName("persId")
    val personId: String? = null,

    /** Anno di conseguimento titolo straniero. */
    @SerialName("aaConsegId")
    val academicYearAwardId: Long? = null,

    /** Appellativo femminile del titolo straniero. */
    @SerialName("appellativoF")
    val femaleTitle: String? = null,

    /** Appellativo maschile del titolo straniero. */
    @SerialName("appellativoM")
    val maleTitle: String? = null,

    /** Identificativo dellAteneo di equipollenza del titolo. */
    @SerialName("ateneoEquipId")
    val universityEquivalentId: Long? = null,

    /** Corso di studio di equipollenza del titolo. */
    @SerialName("cdsItEquipId")
    val courseOfStudyItalianEquivalentId: Long? = null,

    /** Descrizione del Corso di studio straniero. */
    @SerialName("cdsStraniero")
    val foreignCourseOfStudy: String? = null,

    /** Città straniera di conseguimento del titolo. */
    @SerialName("citstraConseg")
    val deliveryForeignCity: String? = null,

    /** Codice titolo sistema esterno. */
    @SerialName("codExt")
    val externalCode: String? = null,

    /** Data conseguimento titolo straniero. */
    @SerialName("dataConsegTitolo")
    val titleDeliveryDate: String? = null,

    /** Descrizione libera dell'ateneo. */
    @SerialName("desAteneo")
    val universityDescription: String? = null,

    /** Descrizione generica titolo di studio straniero */
    @SerialName("desTitolo")
    val titleDescription: String? = null,

    /** Attesta se è stata depositata la Dichiarazione di valore del titolo di studio. */
    @SerialName("dichiarazValoreFlg")
    val valueDeclarationFlag: Int? = null,

    /** Durata legale in anni del corso. */
    @SerialName("durataAnni")
    val durationYears: Long? = null,

    /** Link al sistema di gestione elettronica dei documenti. */
    @SerialName("identificativoGed")
    val gedIdentifier: String? = null,

    /** Indica che lo studente si è laureato entro la durata normale del corso di studio. */
    @SerialName("lauEntroDnFlg")
    val graduationWithinDeadlineFlag: Int? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    /** Flag che indica se  stata conseguita la lode. */
    @SerialName("lode")
    val cumLaude: Int? = null,

    /** Descrizione del percorso di equipollenza del titolo. */
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

    /** Codice tipo di struttura didattica responsabile. */
    @SerialName("sdrTip")
    val siteType: String? = null,

    @SerialName("statiTitItDes")
    val italianTitleStatesDescription: String? = null,

    /** Stato del il tipo titolo: C = Conseguito, I = In ipotesi */
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

    /** TIPO DOCUMENTO CONSEGNATO: N --> Nessun diploma depositato O --> Diploma originale depositato AUT --> Autocertificazione presentata F --> Fotocopia FAUT --> Fotocopia autenticata LAUESA --> Copia dellattestazione del titolo di laurea con elenco degli esami svolti */
    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    /** Tipo Ente esterno convenzionato con uno o più corsi di studio */
    @SerialName("tipoEnteCod")
    val entityTypeCode: String? = null,

    /** Codice Tipo di Titoli Straniero. */
    @SerialName("tipoTitstCod")
    val titleStatusTypeCode: String? = null,

    /** Descrizione libera tipo titolo universitario straniero. */
    @SerialName("tipoTitstraDes")
    val foreignTitleTypeDescription: String? = null,

    @SerialName("titAccAmm")
    val adminTitleAccess: Long? = null,

    @SerialName("titAccMat")
    val matTitleAccess: Int? = null,

    @SerialName("titAccMatStu")
    val studentMatTitleAccess: Int? = null,

    /** Indica se il titolo straniero è stato riconosciuto nell Ateneo. */
    @SerialName("titoloEquipFlg")
    val equivalentTitleFlag: Long? = null,

    /** Identificativo univoco del titolo universitario stranierio */
    @SerialName("titStraId")
    val foreignTitleId: Long? = null,

    /** Indica se il titolo universitario straniero è stato valutato. */
    @SerialName("valutatoFlg")
    val evaluatedFlag: Long? = null,

    /** Voto conseguito (I campi Voto e Voto_Alfanumerico sono mutuamente esclusivi). */
    @SerialName("voto")
    val grade: Float? = null,

    /** Voto conseguito espresso in lettere. (Voto e Voto_Alfanumerico sono mutuamente esclusivi). */
    @SerialName("votoAlfanumerico")
    val alphanumericGrade: String? = null,

    /** È attivato se e soltanto se viene indicato il Voto numerico e rappresenta il voto massimo raggiungibile. */
    @SerialName("votoBase")
    val baseGrade: Int? = null
)

@Serializable
data class Esse3ItalianTitleAttachmentMetadata(
    /** nome del file */
    @SerialName("filename")
    val fileName: String = "",

    /** autore del file */
    @SerialName("autore")
    val author: String? = null,

    /** titolo del file */
    @SerialName("titolo")
    val title: String = "",

    /** descrizione libera */
    @SerialName("descrizione")
    val description: String = "",

    /** tipologia dell'allegato relativo all'entità p17_tipologia_allegati */
    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    /** flag che indica se l'allegato deve già risultare validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int = 0,

    /** Anno Accademico di conseguimento del titolo */
    @SerialName("aaConsegTit")
    val academicYearAwardedTitle: Int = 0,

    /** codice tipo titolo italiano */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String = "",

    /** identificativo del titolo */
    @SerialName("titItId")
    val italianTitleId: Long? = null
)

@Serializable
data class Esse3HandicapDeclarationPersonalData(
    /** ID dichiarazione di invalidità */
    @SerialName("dicHandId")
    val handicapDeclarationId: Int? = null,

    /** userId attivo collegato al docente */
    @SerialName("userId")
    val userId: String? = null,

    /** id univoco che consente di individuare la persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** cognome dell'utente */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome dell'utente */
    @SerialName("nome")
    val name: String? = null,

    /** sesso del docente */
    @SerialName("sesso")
    val gender: String? = null,

    /** Data di nascita del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** Cittadinanza. */
    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    /** descrizione cittadinanza */
    @SerialName("cittadDes")
    val citizenshipDescription: String? = null,

    /** denominazione della nazione di nascita */
    @SerialName("naziNasc")
    val birthNation: String? = null,

    /** codice fiscale dell'utente */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** comune o città estera di nascita */
    @SerialName("luogoNascita")
    val birthPlace: String? = null,

    /** email personale */
    @SerialName("email")
    val email: String? = null,

    /** cellulare del docente */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** ID numerico univoco della nazione. */
    @SerialName("nazioneId")
    val nationId: Int? = null,

    /** nazione di residenza */
    @SerialName("naziRes")
    val residenceNation: String? = null,

    /** ID numerico unico del comune. */
    @SerialName("comuneId")
    val municipalityId: Int? = null,

    /** comune o città estera di nascita */
    @SerialName("luogoRes")
    val residencePlace: String? = null,

    /** via di residenza */
    @SerialName("viaRes")
    val residenceStreet: String? = null,

    /** numero civico di residenza */
    @SerialName("numCivRes")
    val residenceStreetNumber: String? = null,

    /** CAP di residenza */
    @SerialName("capRes")
    val residencePostalCode: String? = null,

    /** Telefono di residenza */
    @SerialName("telRes")
    val residencePhone: String? = null
)

@Serializable
data class Esse3TeacherPositions(
    /** identificativo struttura. */
    @SerialName("struttId")
    val structureId: Long? = null,

    /** identificativo carrica. */
    @SerialName("caricaId")
    val positionId: Long? = null,

    /** descrizione carica. */
    @SerialName("caricaDes")
    val positionDescription: String? = null,

    /** cognome del responsabile. */
    @SerialName("caricaCognome")
    val positionSurname: String? = null,

    /** nome el responsabile. */
    @SerialName("caricaNome")
    val positionName: String? = null,

    /** identificativo U-gov del responsabile. */
    @SerialName("caricaIdAb")
    val positionAbbreviatedId: Long? = null,

    /** identificativo docente. */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** data inizio validità. */
    @SerialName("dataInizioVal")
    val evaluationStartDate: String? = null,

    /** data fine validità. */
    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    /** codice struttura. */
    @SerialName("codStruttura")
    val structureCode: String? = null,

    /** descrizione struttura. */
    @SerialName("desStruttura")
    val structureDescription: String? = null,

    /** tipologia struttura. */
    @SerialName("tipoStruttura")
    val structureType: String? = null
)

@Serializable
data class Esse3SubjectPermission(
    /** Identificativo permesso soggetto */
    @SerialName("permSogId")
    val authorizedSubjectId: Long? = null,

    /** Data presentazione */
    @SerialName("dataPres")
    val presenceDate: String? = null,

    /** Numero permesso */
    @SerialName("numPerm")
    val permitNumber: String? = null,

    /** Data inizio validità */
    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    /** Data fine validità */
    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    /** Codice tipo deposito */
    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    /** Descrizione tipo deposito */
    @SerialName("tipoDepositoDes")
    val depositTypeDescription: String? = null,

    /** Note aggiuntive */
    @SerialName("nota")
    val note: String? = null,

    /** Codice tipo permesso soggetto */
    @SerialName("tipoPermsogCod")
    val authorizedSubjectTypeCode: String? = null,

    /** Descrizione tipo permesso soggetto */
    @SerialName("tipoPermsogDes")
    val authorizedSubjectTypeDescription: String? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Numero assicurata */
    @SerialName("numAssicurata")
    val insuredNumber: String? = null,

    /** Codice motivo emissione permesso soggetto */
    @SerialName("motEmisPermsogCod")
    val authorizedSubjectIssuanceReasonCode: String? = null,

    /** Descrizione motivo emissione permesso soggetto */
    @SerialName("motEmisPermsogDes")
    val authorizedSubjectIssuanceReasonDescription: String? = null,

    /** Codice stato permesso soggetto */
    @SerialName("statoPermsogCod")
    val authorizedSubjectStateCode: String? = null,

    /** Descrizione stato permesso soggetto */
    @SerialName("statoPermsogDes")
    val authorizedSubjectStateDescription: String? = null
)

@Serializable
data class Esse3Career(
    /** id univoco che consente di individuare la persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** cognome della persona */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome della persona */
    @SerialName("nome")
    val name: String? = null,

    /** data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** sesso della persona */
    @SerialName("sesso")
    val gender: String? = null,

    /** id univoco che consente di individuare l'account utente */
    @SerialName("userId")
    val userId: String? = null,

    /** codice fiscale della personae */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** email personale dello studente */
    @SerialName("email")
    val email: String? = null,

    /** indirizzo email assegnato dall'ateneo allo studente */
    @SerialName("emailAte")
    val universityEmail: String? = null,

    /** sigla che identifica lo stato della carriera */
    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    /** sigla che identifica il motivo dello stato della carriera */
    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    /** anno di immatricolazione */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** data di immatricolazione */
    @SerialName("dataImm")
    val matriculationDate: String? = null,

    /** descrizione dello stato della carriera */
    @SerialName("statiStuDes")
    val studentStatesDescription: String? = null,

    /** descrizione del motivo della stato della carriera */
    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    /** numero protocollo */
    @SerialName("numProtocollo")
    val protocolNumber: String? = null,

    /** data di inserimento (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data di modifica (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** stato domanda di conseguiimento titolo */
    @SerialName("domCtStato")
    val domicileCommitteeState: String? = null,

    /** descrizione stato domanda di conseguiimento titolo */
    @SerialName("statiDomCtDes")
    val committeeApplicationStatesDescription: String? = null,

    /** descrizione anno accademico */
    @SerialName("aaDes")
    val academicYearDescription: String? = null,

    /** anno iscrizione */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    /** matricola */
    @SerialName("matricola")
    val matricola: String? = null,

    /** id della sede */
    @SerialName("sedeId")
    val siteId: Int? = null,

    /** descrizione della sede */
    @SerialName("sediDes")
    val sitesDescription: String? = null,

    /** anno di corso */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** descrizione lingua */
    @SerialName("lingue")
    val languages: String? = null,

    /** data iscrizione */
    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    /** codice del settore */
    @SerialName("settCod")
    val sectorCode: String? = null,

    /** descrizione del settore */
    @SerialName("settDes")
    val sectorDescription: String? = null,

    /** codice dell'area */
    @SerialName("areaCod")
    val areaCode: String? = null,

    /** descrizione dell'area */
    @SerialName("areaDes")
    val areaDescription: String? = null,

    /** codice usato nelle statistiche del MIUR */
    @SerialName("areaCodStatMiur")
    val areaMiurStatisticalCode: String? = null,

    /** codice struttura didattica */
    @SerialName("sdrCod")
    val siteCode: String? = null,

    /** descrizione struttura didattica */
    @SerialName("sdrDes")
    val siteDescription: String? = null,

    /** Identificativo della struttura didattica responsabile */
    @SerialName("sdrCsaCod")
    val siteCsaCode: Int? = null,

    /** codice facoltà */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione facoltà */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** codice csa della facoltà */
    @SerialName("facCsaCod")
    val facultyCsaCode: String? = null,

    /** identificativo U-gov */
    @SerialName("idAb")
    val abbreviatedId: Int? = null,

    /** Codice esterno carriera */
    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    @SerialName("responsabile")
    val responsible: Esse3PhDProgramDirector? = null,

    @SerialName("tutor")
    val tutor: Esse3TutorData? = null,

    /** Flag che indica se lo studente è iscritto in attesa di laurea. */
    @SerialName("attlauFlg")
    val degreeAwardFlag: Int? = null,

    /** data attesa di laurea */
    @SerialName("dataAttlau")
    val degreeAwardDate: String? = null,

    /** identificativo della tipologia di categoria amministrativa */
    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    /** descrizione della tipologia di categoria amministrativa */
    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    /** Profilo studente */
    @SerialName("profstuCod")
    val studentProfessionCode: String? = null,

    /** descrizione profilo studente */
    @SerialName("profstuDes")
    val studentProfessionDescription: String? = null,

    /** Indica lo stato della posizione della matricola. I valori di sistema sono:  A =  Attivo, S = Sospeso, I = Ipotesi */
    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    /** Causale dello stato della matricola. */
    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    /** Tipo di iscrizione all´anno di corso specificato: IC = In Corso, FC = Fuori Corso, RI = Ripetente */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** Flag che indica se l´iscrizione è part-time (1) oppure full-time (0). */
    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    /** Flag che indica se nell´anno dell´iscrizione lo studente era sospeso e quindi se l´iscrizione era fittizia. */
    @SerialName("sospFlg")
    val suspensionFlag: Int? = null,

    /** Codice mnemonico del corso di studio */
    @SerialName("p06CdsCod")
    val p06CourseOfStudyCode: String? = null,

    /** Descrizione del corso di studio */
    @SerialName("p06CdsDes")
    val p06CourseOfStudyDescription: String? = null,

    /** Identificativo carriera */
    @SerialName("matId")
    val matId: Long? = null,

    /** Anno di ordinamento */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** ID del percorso di studio */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** ID iscrizione studente */
    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    /** identificativo corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** id univoco che consente di individuare la carriera */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** data di chiusura della carriera */
    @SerialName("dataChiusura")
    val closingDate: String? = null,

    /** anno accademico di inizio carriera */
    @SerialName("aaImm1")
    val academicYearImm1: Int? = null,

    /** Anno Accademico Regolamenti (Coorte) */
    @SerialName("aaRegId")
    val academicYearRegulationId: Long? = null,

    /** Indirizzo e-mail certificato (PEC). */
    @SerialName("emailCertificata")
    val certifiedEmail: String? = null
)

@Serializable
data class Esse3RefreshedToken(
    /** Url attivazione a cui viene concatenato il token di attivazione . */
    @SerialName("activationUrl")
    val activationUrl: String? = null,

    /** Data di scadenza del token. */
    @SerialName("expiration")
    val expiration: String? = null
)

@Serializable
data class Esse3SpecialNeeds(
    /** codice bisogno speciale */
    @SerialName("bisognoSpecialeCod")
    val specialNeedCode: String? = null,

    /** descrizione bisogno speciale */
    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3HandicapRegulations(
    /** Codice normativa tipologia di handicap. */
    @SerialName("handNormativaCod")
    val handicapRegulationCode: String? = null,

    /** Descrizione normativa handicap. */
    @SerialName("des")
    val description: String? = null,

    /** Attivazione visualizzazione del dato dalle liste on-line. */
    @SerialName("webFlg")
    val webFlag: Int? = null
)

@Serializable
data class Esse3HandicapDeclarationAttachmentMetadata(
    /** nome del file */
    @SerialName("filename")
    val fileName: String = "",

    /** autore del file */
    @SerialName("autore")
    val author: String? = null,

    /** titolo del file */
    @SerialName("titolo")
    val title: String = "",

    /** descrizione libera */
    @SerialName("descrizione")
    val description: String = "",

    /** tipologia dell'allegato relativo all'entità p17_tipologia_allegati */
    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    /** flag che indica se l'allegato deve già risultare validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int = 0,

    /** tipo di handicap */
    @SerialName("tipoHandicap")
    val handicapType: String = "",

    /** data della dichiarazione */
    @SerialName("dataDichiar")
    val declarationDate: String? = null,

    /** data di inizio invalidità */
    @SerialName("dataIni")
    val startDate: String? = null,

    /** data di fine invalidità */
    @SerialName("dataFine")
    val endDate: String? = null,

    /** identificativo dichiarazione invalidità */
    @SerialName("dicHandId")
    val handicapDeclarationId: Long? = null,

    /** flag che indica se l'allegato deve già risultare visibile da web o meno */
    @SerialName("abilVisWeb")
    val webVisibility: Int = 0
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

    /** chiave del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** matricola del docente */
    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    /** cognome del docente */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** nome del docente */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** userId attivo collegato al docente */
    @SerialName("userId")
    val userId: String? = null,

    /** settore del docente */
    @SerialName("settCod")
    val sectorCode: String? = null,

    /** numero badge */
    @SerialName("badge")
    val badge: String? = null,

    /** email del docente */
    @SerialName("eMail")
    val email: String? = null,

    /** Indirizzo e-mail assegnato dall'ateneo. */
    @SerialName("emailAte")
    val universityEmail: String? = null,

    /** email di riferimento per i docenti L.A. */
    @SerialName("emailDocenteLa")
    val lecturerLaEmail: String? = null,

    /** ID della struttura di appartenenza del docente */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice della struttura di appartenenza del docente */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione della struttura di appartenenza del docente */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** ruolo del docente */
    @SerialName("ruoloDocCod")
    val lecturerRoleCode: String? = null,

    /** codice fiscale del docente */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** cellulare del docente */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** hyperlink del docente */
    @SerialName("hyperlink")
    val hyperlink: String? = null,

    /** Data inizio del attività del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIniAtt")
    val activityStartDate: String? = null,

    /** Data fine del attività del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFinAtt")
    val activityEndDate: String? = null,

    /** sesso del docente */
    @SerialName("sesso")
    val gender: String? = null,

    /** Data di nascita del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** codice fiscale della nazione di nascita */
    @SerialName("p01NaziCodFisc")
    val p01NationFiscalCode: String? = null,

    /** descrizione della nazione di nascita */
    @SerialName("p01NaziDes")
    val p01NationDescription: String? = null,

    /** codice  iso della nazione di nascita */
    @SerialName("p01NaziNazioneCod")
    val p01NationNationCode: String? = null,

    /** codice  della nazione di nascita */
    @SerialName("p01NaziCod")
    val p01NationCode: String? = null,

    /** ID del comune di nascita */
    @SerialName("p01ComuComuneId")
    val p01MunicipalityCommonId: Long? = null,

    /** codice  istat del comune di nascita */
    @SerialName("p01ComuCodIstat")
    val p01MunicipalityIstatCode: String? = null,

    /** codice  del comune di nascita */
    @SerialName("p01ComuComuneCod")
    val p01MunicipalityCommonCode: String? = null,

    /** codice  istat miur del comune di nascita */
    @SerialName("p01ComuCodIstatMiur")
    val p01MunicipalityMiurIstatCode: String? = null,

    /** descrizione del comune di nascita */
    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    /** città straniera di nascita */
    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    /** sigla provincia di nascita */
    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    /** descrizione provincia di nascita */
    @SerialName("p01ProvDes")
    val p01ProvinceDescription: String? = null,

    /** note pubblicazioni */
    @SerialName("notePubblicazioni")
    val publicationsNotes: String? = null,

    /** note biografiche */
    @SerialName("noteBiografiche")
    val biographicalNotes: String? = null,

    /** note curriculum */
    @SerialName("noteCurriculum")
    val curriculumNotes: String? = null,

    /** note docente */
    @SerialName("noteDocente")
    val lecturerNotes: String? = null,

    /** ID_AB del docente */
    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    /** Data di modifica del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataModDoc")
    val documentModificationDate: String? = null,

    /** Data di modifica del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Data di inserimento del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Descrizione del settore */
    @SerialName("settDes")
    val sectorDescription: String? = null,

    /** Identificativo del dipartimento */
    @SerialName("dipId")
    val departmentId: Long? = null,

    /** Codice del dipartimento */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** Descrizione del dipartimento */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** Descrizione del ruolo del docente */
    @SerialName("ruoloDocDes")
    val lecturerRoleDescription: String? = null,

    /** Profilo docente */
    @SerialName("profilo")
    val profile: String? = null,

    /** appellativo del docente */
    @SerialName("docenteAppellativo")
    val lecturerTitle: String? = null,

    /** Data inizio ruolo docente attuale. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIniRuolo")
    val roleStartDate: String? = null
)

@Serializable
data class Esse3HighSchoolDiplomaAttachmentMetadata(
    /** nome del file */
    @SerialName("filename")
    val fileName: String = "",

    /** autore del file */
    @SerialName("autore")
    val author: String? = null,

    /** titolo del file */
    @SerialName("titolo")
    val title: String = "",

    /** descrizione libera */
    @SerialName("descrizione")
    val description: String = "",

    /** tipologia dell'allegato relativo all'entità p17_tipologia_allegati */
    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    /** flag che indica se l'allegato deve già risultare validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int = 0,

    /** anno di maturità, coincide con l'anno solare della data di conseguimento del diploma. Per esempio, anno scolastico 2019/2020, l'anno di diploma è 2020 */
    @SerialName("annoMaturita")
    val highSchoolGraduationYear: Int = 0,

    /** identificativo diploma MIUR */
    @SerialName("idDiplomaMiur")
    val miurDiplomaId: Long? = null,

    /** data di maturità */
    @SerialName("dataMaturita")
    val highSchoolGraduationDate: String? = null,

    /** identificativo della maturità */
    @SerialName("maturId")
    val highSchoolGraduationId: Long? = null
)

@Serializable
data class Esse3PersonalDataAddressesHistory(
    /** Identificativo storico indirizzo */
    @SerialName("anaperIndStoId")
    val personHistoricalAddressId: Long? = null,

    /** Codice tipo indirizzo */
    @SerialName("tipoIndirizCod")
    val addressTypeCode: String? = null,

    /** Descrizione tipo indirizzo */
    @SerialName("tipoIndirizDes")
    val addressTypeDescription: String? = null,

    /** Identificativo nazione */
    @SerialName("nazioneId")
    val nationId: Long? = null,

    /** Codice fisico nazione indirizzo */
    @SerialName("naziIndCod")
    val addressNationCode: String? = null,

    /** Descrizione nazione indirizzo */
    @SerialName("naziIndDes")
    val addressNationDescription: String? = null,

    /** Codice ISO nazione indirizzo */
    @SerialName("naziIndNazioneCod")
    val addressCountryCode: String? = null,

    /** Codice internazionale nazione indirizzo */
    @SerialName("naziIndCodInt")
    val addressNationInternationalCode: String? = null,

    /** Identificativo comune */
    @SerialName("comuneId")
    val municipalityId: Long? = null,

    /** Identificativo comune nascita */
    @SerialName("comuNascId")
    val birthMunicipalityId: Long? = null,

    /** Codice comune nascita */
    @SerialName("comuNascCod")
    val birthMunicipalityCode: String? = null,

    /** Codice catastale comune nascita */
    @SerialName("comuNascCodCatastale")
    val birthMunicipalityCadastralCode: String? = null,

    /** Codice ISTAT/MIUR comune nascita */
    @SerialName("comuNascCodIstatMiur")
    val birthMunicipalityMiurIstatCode: String? = null,

    /** Descrizione comune nascita */
    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    /** Sigla comune nascita */
    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    /** Descrizione provincia indirizzo */
    @SerialName("provIndDes")
    val addressProvinceDescription: String? = null,

    /** Frazione */
    @SerialName("fraz")
    val fraction: String? = null,

    /** Città o strada */
    @SerialName("citstra")
    val foreignCity: String? = null,

    /** Nome via */
    @SerialName("via")
    val street: String? = null,

    /** Numero civico */
    @SerialName("numCiv")
    val streetNumber: String? = null,

    /** CAP */
    @SerialName("cap")
    val postalCode: String? = null,

    /** Numero telefono */
    @SerialName("tel")
    val phone: String? = null,

    /** Prefisso internazionale */
    @SerialName("prefixInternaz")
    val internationalPrefix: String? = null,

    /** Fax */
    @SerialName("fax")
    val fax: String? = null,

    /** Numero cellulare */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** Email */
    @SerialName("email")
    val email: String? = null,

    /** Data inizio validità */
    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    /** Data fine validità */
    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Flag aziendale */
    @SerialName("aziendaleFlg")
    val companyRelatedFlag: Int? = null,

    /** Ragione sociale */
    @SerialName("ragioneSociale")
    val companyName: String? = null,

    /** Partita IVA */
    @SerialName("piva")
    val vatNumber: String? = null,

    /** Codice fiscale */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** PEC */
    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    /** Codice SDI */
    @SerialName("codiceSdi")
    val sdiCode: String? = null,

    /** URL */
    @SerialName("url")
    val url: String? = null,

    /** Cognome persona */
    @SerialName("cognome")
    val surname: String? = null,

    /** Nome persona */
    @SerialName("nome")
    val name: String? = null,

    /** Codice CIG */
    @SerialName("cig")
    val cig: String? = null,

    /** Codice CUP */
    @SerialName("cup")
    val cup: String? = null,

    /** Codice IPA */
    @SerialName("ipa")
    val ipa: String? = null,

    /** Flag split payment */
    @SerialName("splitpayementFlg")
    val splitPaymentFlag: Int? = null
)

@Serializable
data class Esse3StudentsConsents(
    /** Identificativo anagrafica. */
    @SerialName("persId")
    val personId: Int? = null,

    /** Codice tipo consenso richiesto. */
    @SerialName("tipiConsensoTipoConsensoCod")
    val consentTypesConsentTypeCode: String? = null,

    /** Indica se il consenso è stato dato. */
    @SerialName("consensoFlg")
    val consentFlag: Int? = null,

    /** Descrizione in lingua tipo consenso. */
    @SerialName("des")
    val description: String? = null,

    /** Data inizio consenso o negazione del consenso. */
    @SerialName("dataIni")
    val startDate: String? = null,

    /** Processo amministrativo in cui è stato modificato il consenso. */
    @SerialName("procAmmCod")
    val administrativeProcedureCode: String? = null,

    /** Indica attivazione richiesta nei processi online. */
    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    /** Il rilascio del consenso è vincolante al fine del completamento del processo. */
    @SerialName("vincFlg")
    val winnerFlag: Int? = null,

    /** Abilita la visualizzazione del link alla documentazione nel processo online. */
    @SerialName("abilVisDocFlg")
    val documentVisibilityFlag: Int? = null,

    /** Nota in lingua. */
    @SerialName("nota")
    val note: String? = null,

    /** Etichetta da visualizzare on-line. */
    @SerialName("etichetta")
    val label: String? = null,

    /** Codice tipo consenso. */
    @SerialName("p01AnaperConsensiTipoConsensoCod")
    val p01PersonConsentsConsentTypeCode: String? = null
)

@Serializable
data class Esse3TutorsRulesHeader(
    /** Identificativo testata regole di richiesta tutori. */
    @SerialName("regTutoriTstId")
    val tutorsTestRegistrationId: Long? = null,

    /** Codice testata regole di richiesta tutori. */
    @SerialName("cod")
    val code: String? = null,

    /** Descrizione testata regole di richiesta tutori. */
    @SerialName("des")
    val description: String? = null,

    /** Etichetta testata regole di richiesta tutori */
    @SerialName("etichetta")
    val label: String? = null,

    /** Nota testata regole di richiesta tutori */
    @SerialName("nota")
    val note: String? = null,

    /** Abilita visibilità della testata di richiesta tutori. */
    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    /** Utente di inserimento. */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Utente di ultima modifica. */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data di inserimento. */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Data di ultima modifica. */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("dettaglio")
    val detail: List<Esse3TutorsRulesDetail> = emptyList()
)

@Serializable
data class Esse3StudentTypeParameters(
    /** Codice tipologia studente */
    @SerialName("tipoStuCod")
    val studentTypeCode: String? = null
)

@Serializable
data class Esse3Authorizations(
    /** Identificativo autorizzazione */
    @SerialName("autorizzazioneId")
    val authorizationId: Long? = null,

    /** Codice autorizzazione */
    @SerialName("autorizzazioneCod")
    val authorizationCode: String? = null,

    /** Descrizione autorizzazione */
    @SerialName("autorizzazioneDes")
    val authorizationDescription: String? = null,

    /** Flag provvisorio */
    @SerialName("provvFlg")
    val provisionalFlag: Int? = null,

    /** Data autorizzazione */
    @SerialName("dataAutorizz")
    val authorizationDate: String? = null,

    /** Data revoca autorizzazione */
    @SerialName("dataRevoca")
    val revocationDate: String? = null,

    /** Identificativo */
    @SerialName("autorizzatoId")
    val authorizedId: Long? = null,

    /** Nome */
    @SerialName("nome")
    val name: String? = null,

    /** Cognome */
    @SerialName("cognome")
    val surname: String? = null,

    /** Codice fiscale */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** codice di errore nel controllo del CF */
    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Long? = null,

    /** Flag certificato */
    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    /** Sesso */
    @SerialName("sesso")
    val gender: String? = null,

    /** Data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** Tipo parentela codice */
    @SerialName("tipoParCod")
    val paragraphTypeCode: String? = null,

    /** Descrizione tipo parentela */
    @SerialName("tipiParDes")
    val paragraphTypesDescription: String? = null,

    /** Identificativo nazione */
    @SerialName("nazioneId")
    val nationId: Long? = null,

    /** Codice nazione */
    @SerialName("naziCod")
    val nationCode: String? = null,

    /** Descrizione nazione */
    @SerialName("naziDes")
    val nationDescription: String? = null,

    /** Codice ISO nazione */
    @SerialName("naziNazioneCod")
    val nationNationCode: String? = null,

    /** Codice internazionale nazione */
    @SerialName("naziCodInt")
    val nationInternationalCode: String? = null,

    /** Città o stato nascita */
    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    /** Identificativo comune */
    @SerialName("comuneId")
    val municipalityId: Long? = null,

    /** Codice comune */
    @SerialName("comuCod")
    val municipalityCode: String? = null,

    /** Codice catastale comune */
    @SerialName("comuCodCatastale")
    val municipalityCadastralCode: String? = null,

    /** Codice ISTAT/MIUR comune */
    @SerialName("comuCodIstatMiur")
    val municipalityMiurIstatCode: String? = null,

    /** Descrizione comune */
    @SerialName("comuDes")
    val municipalityDescription: String? = null,

    /** Sigla comune */
    @SerialName("comuSigla")
    val municipalityAbbreviation: String? = null,

    /** Descrizione provincia */
    @SerialName("provDes")
    val provinceDescription: String? = null,

    /** Email */
    @SerialName("email")
    val email: String? = null,

    /** PEC */
    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    /** Numero cellulare */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** Codice esterno autorizzato */
    @SerialName("autExtCod")
    val externalAuthorizationCode: String? = null
)

@Serializable
data class Esse3HighSchoolDiploma(
    /** Identificativo record maturità */
    @SerialName("id")
    val id: Long? = null,

    /** Codice tipo titolo */
    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    /** Descrizione tipo titolo */
    @SerialName("tipoTitoloDes")
    val titleTypeDescription: String? = null,

    /** Identificativo diploma */
    @SerialName("idDiploma")
    val diplomaId: Long? = null,

    /** Anno maturità */
    @SerialName("annoMaturita")
    val highSchoolGraduationYear: Int? = null,

    /** Data conseguimento maturità */
    @SerialName("dataMaturita")
    val highSchoolGraduationDate: String? = null,

    /** Voto ottenuto */
    @SerialName("voto")
    val grade: Int? = null,

    /** Voto minimo */
    @SerialName("votoMin")
    val minGrade: Int? = null,

    /** Voto massimo */
    @SerialName("votoMax")
    val maxGrade: Int? = null,

    /** Voto normalizzato */
    @SerialName("votoNormal")
    val normalGrade: String? = null,

    /** Flag lode */
    @SerialName("lodeFlg")
    val cumLaudeFlag: Int? = null,

    /** Codice tipo istituto */
    @SerialName("tipiIstCod")
    val institutionTypesCode: String? = null,

    /** Descrizione tipo istituto */
    @SerialName("tipiIstDes")
    val institutionTypesDescription: String? = null,

    /** Identificativo scuola superiore */
    @SerialName("scuolaSupId")
    val higherSchoolId: Long? = null,

    /** Codice scuola */
    @SerialName("codScuola")
    val schoolCode: String? = null,

    /** Identificativo MIUR scuola */
    @SerialName("idScuolaMiur")
    val miurSchoolId: Long? = null,

    /** Descrizione scuola */
    @SerialName("scuolaDes")
    val schoolName: String? = null,

    /** Via scuola */
    @SerialName("via")
    val street: String? = null,

    /** Numero civico */
    @SerialName("numeroCivico")
    val streetNumber: String? = null,

    /** Codice nazione consegna diploma */
    @SerialName("nazioneConsegCod")
    val deliveryNationCode: String? = null,

    /** Descrizione nazione consegna diploma */
    @SerialName("nazioneConsegDes")
    val deliveryNationDescription: String? = null,

    /** Codice ISTAT/MIUR comune scuola */
    @SerialName("comuCodIstatMiur")
    val municipalityMiurIstatCode: String? = null,

    /** Codice catastale comune scuola */
    @SerialName("comuCodCatastale")
    val municipalityCadastralCode: String? = null,

    /** Descrizione comune scuola */
    @SerialName("comuDes")
    val municipalityDescription: String? = null,

    /** Sigla comune scuola */
    @SerialName("sigla")
    val abbreviation: String? = null,

    /** CAP scuola */
    @SerialName("cap")
    val postalCode: String? = null,

    /** Flag scuola non statale */
    @SerialName("scuolaNonStatFlg")
    val nonStatutorySchoolFlag: Int? = null,

    /** Codice nazione ordinamento */
    @SerialName("nazioneOrdinamenCod")
    val orderNationCode: String? = null,

    /** Descrizione nazione ordinamento */
    @SerialName("nazioneOrdinamenDes")
    val orderNationDescription: String? = null,

    /** Tipo deposito codice */
    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    /** Identificativo allegato */
    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    /** Note */
    @SerialName("nota")
    val note: String? = null,

    /** Indirizzo scuola */
    @SerialName("indirizzo")
    val address: String? = null,

    /** Stato titolo codice */
    @SerialName("staTitItCod")
    val italianTitleStatusCode: String? = null,

    /** Stato titolo descrizione */
    @SerialName("statiTitDes")
    val titleStatesDescription: String? = null,

    /** Tipo titolo esteso */
    @SerialName("tipoTitstDes")
    val titleStatusTypeDescription: String? = null,

    /** Istituto stato descrizione */
    @SerialName("istStDes")
    val institutionStateDescription: String? = null,

    /** Identificativo lingua didattica */
    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    /** Codice ISO639-1 lingua */
    @SerialName("linguaDidIso6391Cod")
    val teachingLanguageIso6391Code: String? = null,

    /** Codice ISO639-2 lingua */
    @SerialName("linguaDidIso6392Cod")
    val teachingLanguageIso6392Code: String? = null,

    /** Descrizione lingua */
    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    /** Codice esterno */
    @SerialName("codExt")
    val externalCode: String? = null,

    /** Flag valutato */
    @SerialName("valutatoFlg")
    val evaluatedFlag: Int? = null,

    /** Città consegna diploma */
    @SerialName("citstraConseg")
    val deliveryForeignCity: String? = null,

    /** Media voti */
    @SerialName("mediaVoti")
    val gradesAverage: Double? = null
)

@Serializable
data class Esse3PhDSupervisorTutor(
    /** id univoco che consente di individuare la carriera */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** ID del Corso di Studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Anno di ordinamento */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** ID del percorso di studio */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** ID iscrizione studente */
    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    /** Codice tipi di referente dei dottorandi. */
    @SerialName("tipoRefCod")
    val referenceTypeCode: String? = null,

    /** Descrizione tipi di referente dei dottorandi. */
    @SerialName("tipoRefDes")
    val referenceTypeDescription: String? = null,

    /** cognome */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome */
    @SerialName("nome")
    val name: String? = null,

    /** Codice fiscale */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** matricola */
    @SerialName("matricola")
    val matricola: String? = null,

    /** data di nascita */
    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    /** data di nascita */
    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    /** Indica se è il referente principale. */
    @SerialName("principaleFlg")
    val mainFlag: Int? = null
)

@Serializable
data class Esse3ThesisExtension(
    /** id univoco che consente di individuare la carriera */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** ID del Corso di Studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Anno di ordinamento */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** ID del percorso di studio */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** ID iscrizione studente */
    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    /** Data di riferimento della cotutela. */
    @SerialName("dataCotutela")
    val cotutorshipDate: String? = null,

    /** Se = 1, indica che lo studente ha richiesto la cotutela di tesi. */
    @SerialName("domCotutelaFlg")
    val domicileCotutorshipFlag: Long? = null,

    /** Se = 1, indica che lo studente ha ottenuto la cotutela di tesi. */
    @SerialName("cotutelaFlg")
    val cotutorshipFlag: Long? = null,

    /** Codice Erasmus dell´Ateneo */
    @SerialName("atestraErasmusCod")
    val foreignTestErasmusCode: String? = null,

    /** Descrizione Ateneo Straniero */
    @SerialName("atestraDes")
    val foreignTestDescription: String? = null,

    /** Codice tipo di cotutela-> Ingresso, Uscita. */
    @SerialName("tipoCotutelaCod")
    val cotutorshipTypeCode: String? = null,

    /** Descrizione codice tipo di cotutela */
    @SerialName("tipiCotutelaDes")
    val cotutorshipTypesDescription: String? = null,

    /** Mesi di proroga per il conseguimento titolo del dottorato. */
    @SerialName("mesiProrogaCt")
    val committeeExtensionMonths: Long? = null,

    /** Mesi di proroga per il conseguimento titolo del dottorato richiesti dallo studente. */
    @SerialName("mesiProrogaCtRic")
    val committeeExtensionMonthsRequest: Long? = null,

    /** Data di richiesta della proroga di conseguimento titolo da parte dello studente. */
    @SerialName("dataRicProrogaCt")
    val extensionRequestCommitteeDate: String? = null,

    /** Motivazione della richiesta di differimento tesi di conseguimento titolo da parte dello studente. */
    @SerialName("motRicProrogaCt")
    val committeeExtensionRequestReason: String? = null,

    /** Mesi di differimento tesi per il conseguimento titolo del dottorato richiesti dallo studente. */
    @SerialName("mesiDifftesiCtRic")
    val thesisDifferenceCommitteeMonthsRequest: Long? = null,

    /** Data di richiesta del differimento tesi di conseguimento titolo da parte dello studente. */
    @SerialName("dataRicDifftesiCt")
    val thesisDifferenceRequestCommitteeDate: String? = null,

    /** Motivazione della richiesta della proroga di conseguimento titolo da parte dello studente. */
    @SerialName("motRicDifftesiCt")
    val thesisDifferenceCommitteeRequestReason: String? = null
)

@Serializable
data class Esse3TeachersNotes(
    /** chiave del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** Note biografiche del docente. */
    @SerialName("noteBiografiche")
    val biographicalNotes: String? = null,

    /** Note sulle pubblicazioni del docente. */
    @SerialName("notePubblicazioni")
    val publicationsNotes: String? = null,

    /** Note sul curriculum accademico del docente. */
    @SerialName("noteCurriculum")
    val curriculumNotes: String? = null,

    /** Note libere relative al docente. */
    @SerialName("noteDocente")
    val lecturerNotes: String? = null
)

@Serializable
data class Esse3GetHandicapDeclarationAttachmentMetadata(
    /** codice tipo associativa allegato */
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    /** identificativo allegato */
    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    /** dimensione allegato in byte */
    @SerialName("dimensione")
    val size: Long? = null,

    /** titolo allegato */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione allegato */
    @SerialName("des")
    val description: String? = null,

    /** nome file */
    @SerialName("filename")
    val fileName: String? = null,

    /** estensione file */
    @SerialName("estensione")
    val extension: String? = null,

    /** autore allegato */
    @SerialName("autore")
    val author: String? = null,

    /** data inserimento allegato */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data ultima modifica allegato */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** codice tipologia allegato */
    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    /** flag che indica se l'allegato risulta validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int? = null,

    /** flag che indica se l'allegato risulta visibile da web o meno */
    @SerialName("abilVisWeb")
    val webVisibility: Int? = null,

    /** flag che indica se l'allegato risulta stampabile o meno */
    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3GetIdentityDocumentAttachmentMetadata(
    /** codice tipo associativa allegato */
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    /** identificativo allegato */
    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    /** dimensione allegato in byte */
    @SerialName("dimensione")
    val size: Long? = null,

    /** titolo allegato */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione allegato */
    @SerialName("des")
    val description: String? = null,

    /** nome file */
    @SerialName("filename")
    val fileName: String? = null,

    /** estensione file */
    @SerialName("estensione")
    val extension: String? = null,

    /** autore allegato */
    @SerialName("autore")
    val author: String? = null,

    /** data inserimento allegato */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data ultima modifica allegato */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** codice tipologia allegato */
    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    /** flag che indica se l'allegato risulta validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int? = null,

    /** flag che indica se l'allegato risulta visibile da web o meno */
    @SerialName("abilVisWeb")
    val webVisibility: Int? = null,

    /** flag che indica se l'allegato risulta stampabile o meno */
    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null,

    /** identificativo documento identità */
    @SerialName("docPersId")
    val personalDocumentId: Long? = null
)

@Serializable
data class Esse3PersonalDocumentAuthorizationMetadata(
    /** nome del file */
    @SerialName("filename")
    val fileName: String = "",

    /** autore del file */
    @SerialName("autore")
    val author: String? = null,

    /** titolo del file */
    @SerialName("titolo")
    val title: String = "",

    /** descrizione libera */
    @SerialName("descrizione")
    val description: String = "",

    /** tipologia dell'allegato relativo all'entità p17_tipologia_allegati */
    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    /** flag che indica se l'allegato deve già risultare validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int = 0,

    /** codice tipo di documento d'identità (CI - Carta Identità, PAT - Patente, PAS - Passaporto) */
    @SerialName("docIdentTipoCod")
    val identityDocumentTypeCode: String = "",

    /** identificativo documento identità autorizzato */
    @SerialName("autDocPersId")
    val personalDataDocAuthorizationId: Long? = null,

    /** flag che indica se l'allegato deve già risultare visibile da web o meno */
    @SerialName("abilVisWeb")
    val webVisibility: Int = 0
)

@Serializable
data class Esse3GraduationWaitingParameters(
    /** id univoco che consente di individuare la carriera */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** matricola dello studente */
    @SerialName("matricola")
    val matricola: String? = null,

    /** anno iscrizione */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Long? = null,

    /** check attesa di laurea */
    @SerialName("attlauFlg")
    val degreeAwardFlag: Int = 0,

    /** data attesa di laurea */
    @SerialName("dataAttlau")
    val degreeAwardDate: String? = null
)

@Serializable
data class Esse3EnrollmentReturn(
    /** codice di ritorno */
    @SerialName("codiceRitorno")
    val returnCode: Int? = null,

    /** identificativo dell elenco generato dalla elaborazione */
    @SerialName("idElenco")
    val listId: Int? = null,

    @SerialName("errori")
    val errors: List<Esse3DettaglioErrore> = emptyList()
)

@Serializable
data class Esse3CareerMinimalData(
    /** id univoco che consente di individuare la persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** cognome della persona */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome della persona */
    @SerialName("nome")
    val name: String? = null,

    /** data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** sesso della persona */
    @SerialName("sesso")
    val gender: String? = null,

    /** id univoco che consente di individuare l'account utente */
    @SerialName("userId")
    val userId: String? = null,

    /** codice fiscale della personae */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** email personale dello studente */
    @SerialName("email")
    val email: String? = null,

    /** indirizzo email assegnato dall'ateneo allo studente */
    @SerialName("emailAte")
    val universityEmail: String? = null,

    /** id univoco che consente di individuare la carriera */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** sigla che identifica lo stato della carriera */
    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    /** sigla che identifica il motivo dello stato della carriera */
    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    /** anno di immatricolazione */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** descrizione anno accademico */
    @SerialName("aaDes")
    val academicYearDescription: String? = null,

    /** data di immatricolazione */
    @SerialName("dataImm")
    val matriculationDate: String? = null,

    /** descrizione dello stato della carriera */
    @SerialName("statiStuDes")
    val studentStatesDescription: String? = null,

    /** descrizione del motivo della stato della carriera */
    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    /** numero protocollo */
    @SerialName("numProtocollo")
    val protocolNumber: String? = null,

    /** data di inserimento (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data di modifica (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Codice esterno carriera */
    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    /** anno iscrizione */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    /** data iscrizione */
    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    /** matricola */
    @SerialName("matricola")
    val matricola: String? = null,

    /** Indica lo stato della posizione della matricola. I valori di sistema sono:  A =  Attivo, S = Sospeso, I = Ipotesi */
    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    /** Causale dello stato della matricola. */
    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    /** Tipo di iscrizione all´anno di corso specificato: IC = In Corso, FC = Fuori Corso, RI = Ripetente */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** Flag che indica se l´iscrizione è part-time (1) oppure full-time (0). */
    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    /** Flag che indica se nell´anno dell´iscrizione lo studente era sospeso e quindi se l´iscrizione era fittizia. */
    @SerialName("sospFlg")
    val suspensionFlag: Int? = null,

    /** Codice mnemonico del corso di studio */
    @SerialName("p06CdsCod")
    val p06CourseOfStudyCode: String? = null,

    /** Descrizione del corso di studio */
    @SerialName("p06CdsDes")
    val p06CourseOfStudyDescription: String? = null,

    /** id della sede */
    @SerialName("sedeId")
    val siteId: Int? = null,

    /** descrizione della sede */
    @SerialName("sediDes")
    val sitesDescription: String? = null,

    /** anno di corso */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** codice facoltà */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione facoltà */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** codice csa della facoltà */
    @SerialName("facCsaCod")
    val facultyCsaCode: String? = null,

    /** identificativo corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Int? = null,

    /** identificativo della tipologia di categoria amministrativa */
    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    /** descrizione della tipologia di categoria amministrativa */
    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    /** Profilo studente */
    @SerialName("profstuCod")
    val studentProfessionCode: String? = null,

    /** descrizione profilo studente */
    @SerialName("profstuDes")
    val studentProfessionDescription: String? = null,

    /** tipologia corso di studio */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** data di chiusura della carriera */
    @SerialName("dataChiusura")
    val closingDate: String? = null,

    /** id matricola */
    @SerialName("matId")
    val matId: Int? = null
)

@Serializable
data class Esse3CanteenBandParameters(
    /** identificativo fascia mensa */
    @SerialName("fasciaMensaId")
    val canteenBandId: Long? = null
)

@Serializable
data class Esse3HandicapDeclarationPut(
    /** Codice dello stato della dichiarazione di handicap (P - Presentata, C - Confermata, A - Annullata, B - Bozza). */
    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    /** Numero compreso tra 0 e 100 che riporta la percentuale di handicap dello studente. */
    @SerialName("percHand")
    val handicapPercentage: Int? = null,

    /** Data di inizio invalidità. */
    @SerialName("dataIni")
    val startDate: String? = null,

    /** Data fine invalidità. */
    @SerialName("dataFine")
    val endDate: String? = null
)

@Serializable
data class Esse3PersonTitles(
    /** Identificativo univoco della persona */
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
    /** id univoco che consente di individuare la carriera */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** ID del Corso di Studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Anno di ordinamento */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** ID del percorso di studio */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** ID iscrizione studente */
    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    /** Identificativo del tipo carica */
    @SerialName("caricaId")
    val positionId: Int? = null,

    /** Identificativo della firma */
    @SerialName("firmaId")
    val signatureId: Int? = null,

    /** Descrizione della carica */
    @SerialName("caricaDes")
    val positionDescription: String? = null,

    /** nome */
    @SerialName("nome")
    val name: String? = null,

    /** cognome */
    @SerialName("cognome")
    val surname: String? = null,

    /** Appellativo soggetto esterno / docente */
    @SerialName("appellativo")
    val title: String? = null,

    /** data inizio valenza */
    @SerialName("dataInizioVal")
    val evaluationStartDate: String? = null,

    /** data fine valenza */
    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    /** id docente */
    @SerialName("docenteId")
    val lecturerId: Int? = null,

    /** id soggetto esterno */
    @SerialName("soggEstId")
    val externalSubjectId: Int? = null
)

@Serializable
data class Esse3TitlesInsertion(
    /** codice fiscale della persona */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Numero di matricola da assegnare allo studente */
    @SerialName("matricola")
    val matricola: String? = null,

    /** user id */
    @SerialName("userId")
    val userId: String? = null,

    /** identificativo della persona */
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
    /** Tipo di handicap */
    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    /** Descrizione tipo di handicap */
    @SerialName("tipoHandicapDes")
    val handicapTypeDescription: String? = null
)

@Serializable
data class Esse3ExternalSubjectsConsents(
    /** Identificativo soggetto esterno. */
    @SerialName("soggEstId")
    val externalSubjectId: Int? = null,

    /** Codice tipo consenso richiesto. */
    @SerialName("tipiConsensoTipoConsensoCod")
    val consentTypesConsentTypeCode: String? = null,

    /** Indica se il consenso è stato dato. */
    @SerialName("consensoFlg")
    val consentFlag: Int? = null,

    /** Descrizione in lingua tipo consenso. */
    @SerialName("des")
    val description: String? = null,

    /** Data inizio consenso o negazione del consenso. */
    @SerialName("dataIni")
    val startDate: String? = null,

    /** Processo amministrativo in cui è stato modificato il consenso. */
    @SerialName("procAmmCod")
    val administrativeProcedureCode: String? = null,

    /** Indica attivazione richiesta nei processi online. */
    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    /** Il rilascio del consenso è vincolante al fine del completamento del processo. */
    @SerialName("vincFlg")
    val winnerFlag: Int? = null,

    /** Abilita la visualizzazione del link alla documentazione nel processo online. */
    @SerialName("abilVisDocFlg")
    val documentVisibilityFlag: Int? = null,

    /** Nota in lingua. */
    @SerialName("nota")
    val note: String? = null,

    /** Etichetta da visualizzare on-line. */
    @SerialName("etichetta")
    val label: String? = null,

    /** Codice tipo consenso. */
    @SerialName("p01SoggEstConsensiTipoConsensoCod")
    val p01ExternalSubjectConsentsConsentTypeCode: String? = null
)

@Serializable
data class Esse3HigherTitlesEnrollment(
    /** Anno in cui è stata conseguita la maturità.Coincide con l'anno solare relativo alla DATA di conseguimento del diploma */
    @SerialName("annoMaturita")
    val highSchoolGraduationYear: Long = 0L,

    /** data di superamento dell esame di maturità */
    @SerialName("dataMaturita")
    val highSchoolGraduationDate: String? = null,

    /** Tipo di titolo */
    @SerialName("idDiploma")
    val diplomaId: Long? = null,

    /** Indica in quale forma è stato depositato il titolo. Per esempio o originale, CS copia , AUT autocertificazione CA copia autenticata S sostitutivo A in ateneo REG registrazione atto CER certificato. */
    @SerialName("tipoDepositoCodSup")
    val higherDepositTypeCode: String? = null,

    /** Data di deposito del titolo */
    @SerialName("dataDepositoTitolo")
    val titleDepositDate: String? = null,

    /** Indirizzo del titolo */
    @SerialName("indirizzo")
    val address: String? = null,

    /** Voto del diploma */
    @SerialName("voto")
    val grade: Float? = null,

    /** Voto alfanumerico del diploma. Da usare ed abilitare solo per gli studenti che non si sono diplomati in Italia */
    @SerialName("votoAlfa")
    val alphanumericGrade: String? = null,

    /** Anni di scolarità frequentati Da usare ed abilitare solo per gli studenti che non si sono diplomati in Italia */
    @SerialName("anniScolarita")
    val anniScolarita: Long? = null,

    /** Voto minimo di maturità. Se Anno_Maturita > 1998 allora Voto_Min = 60 se Anno_Maturita < 1999 allora  Voto_Min = 36 */
    @SerialName("votoMin")
    val minGrade: Long? = null,

    /** Flag che indica se è stata presentata la dichiarazione di valore. Vale solo per maturità straniere. */
    @SerialName("dichiarazValoreFlg")
    val valueDeclarationFlag: Long? = null,

    /** Voto massimo di maturità. Se Anno_Maturita > 1998 allora Voto_Max = 100 se Anno_Maturita < 1999 allora Voto_Max = 60 */
    @SerialName("votoMax")
    val maxGrade: Long? = null,

    /** Link al sistema di gestione elettronica dei documenti. */
    @SerialName("identificativoGed")
    val gedIdentifier: String? = null,

    /** Indica in quale forma è stato depositato il titolo integrativo. Per esempio o originale, CS copia , AUT autocertificazione CA copia autenticata S sostitutivo A in ateneo REG registrazione atto CER certificato. */
    @SerialName("tipoDepositoCodAnnoInt")
    val internationalYearDepositTypeCode: String? = null,

    /** Anno in cui è stato conseguito l´anno di integrazione */
    @SerialName("annoIntegrazione")
    val integrationYear: Long? = null,

    /** Flag che indica  se il titolo è stato restituito o meno */
    @SerialName("restituitoFlg")
    val returnedFlag: Long? = null,

    /** Data di restituzione del titolo */
    @SerialName("dataRestituzione")
    val returnDate: String? = null,

    /** ID numerico unico del consolato che ha prodotto la dichiarazione di valore del titolo */
    @SerialName("consolatoId")
    val consulateId: Long? = null,

    /** Flag che indica se lo studente ha richiesto la restituzione dell´originale del diploma */
    @SerialName("richiestaRestitFlg")
    val returnRequestFlag: Long? = null,

    /** Flag che indica se per l´accesso all' Università è necessario il conseguimento dell'anno integrativo */
    @SerialName("annoIntFlg")
    val integrationYearFlag: Long? = null,

    /** codice Tipo di titolo straniero ad esempio  MS Master BA Bachelor LIC Licence GCSE General Certificate of Secondary Education */
    @SerialName("tipoTitstCod")
    val titleStatusTypeCode: String? = null,

    /** Identificativo della nazione in cui è stato conseguito il titolo */
    @SerialName("naziConsCodFis")
    val nationConsFiscalCode: String = "",

    /** Identificativo della nazione di ordinamento del titolo */
    @SerialName("naziOrdCodfis")
    val orderNationFiscalCode: String = "",

    /** Elenco codifica scuole superiori con codice miur es  MITF050004 */
    @SerialName("miurScuoleCodScuola")
    val miurSchoolsSchoolCode: String? = null,

    /** Prima lingua del titolo */
    @SerialName("lingua1")
    val language1: Long? = null,

    /** Seconda lingua del titolo */
    @SerialName("lingua2")
    val language2: Long? = null,

    /** Terza lingua del titolo */
    @SerialName("lingua3")
    val language3: Long? = null,

    /** Descrizione libera del tipo di titolo straniero */
    @SerialName("tipoTitstDes")
    val titleStatusTypeDescription: String? = null,

    /** Numero di anni integrativi da conseguire in Italia, nel caso di conseguimento di titolo straniero */
    @SerialName("anniIntegrativi")
    val supplementaryYears: Long? = null,

    /** Stato del il tipo titolo- C Conseguito, I In ipotesi */
    @SerialName("staTitItCod")
    val italianTitleStatusCode: String = "",

    /** lode */
    @SerialName("lodeFlg")
    val cumLaudeFlag: Long? = null,

    /** Descrizione della scuola di conseguimento titolo */
    @SerialName("desScuola")
    val schoolDescription: String? = null,

    /** Descrizione dell'istituto straniero */
    @SerialName("istStDes")
    val institutionStateDescription: String? = null,

    /** codice titolo sistema esterno */
    @SerialName("codExt")
    val externalCode: String? = null,

    /** città straniera di conseguimento */
    @SerialName("citstraConseg")
    val deliveryForeignCity: String? = null
)

@Serializable
data class Esse3TeachersContacts(
    /** chiave del docente */
    @SerialName("docenteId")
    val lecturerId: Long = 0L,

    /** codice della cittadinanza */
    @SerialName("citt1Cod")
    val citizenship1Code: String? = null,

    /** descrizione della cittadinanza */
    @SerialName("citt1Des")
    val citizenship1Description: String? = null,

    /** codice nazione della cittadinanza */
    @SerialName("citt1NazioneCod")
    val citizenship1CountryCode: String? = null,

    /** Data inizio cittadinanza */
    @SerialName("citt1Dataini")
    val citizenship1StartDate: String? = null,

    /** Data fine cittadinanza */
    @SerialName("citt1Datafin")
    val citizenship1EndDate: String? = null,

    /** codice della cittadinanza */
    @SerialName("citt2Cod")
    val citizenship2Code: String? = null,

    /** descrizione della cittadinanza */
    @SerialName("citt2Des")
    val citizenship2Description: String? = null,

    /** codice nazione della cittadinanza */
    @SerialName("citt2NazioneCod")
    val citizenship2CountryCode: String? = null,

    /** Data inizio cittadinanza */
    @SerialName("citt2Dataini")
    val citizenship2StartDate: String? = null,

    /** Data fine cittadinanza */
    @SerialName("citt2Datafin")
    val citizenship2EndDate: String? = null,

    /** codice della cittadinanza */
    @SerialName("citt3Cod")
    val citizenship3Code: String? = null,

    /** descrizione della cittadinanza */
    @SerialName("citt3Des")
    val citizenship3Description: String? = null,

    /** codice nazione della cittadinanza */
    @SerialName("citt3NazioneCod")
    val citizenship3CountryCode: String? = null,

    /** Data inizio cittadinanza */
    @SerialName("citt3Dataini")
    val citizenship3StartDate: String? = null,

    /** Data fine cittadinanza */
    @SerialName("citt3Datafin")
    val citizenship3EndDate: String? = null,

    /** id nazione residenza */
    @SerialName("naziResId")
    val residenceNationId: Long? = null,

    /** id comune residenza */
    @SerialName("comResId")
    val residenceMunicipalityId: Long? = null,

    /** codice fiscale nazione residenza */
    @SerialName("naziResCodFis")
    val residenceNationFiscalCode: String? = null,

    /** descrizione nazione residenza */
    @SerialName("naziResDes")
    val residenceNationDescription: String? = null,

    /** codice  nazione residenza */
    @SerialName("naziResNazioneCod")
    val residenceCountryCode: String? = null,

    /** dcodice nazione residenza */
    @SerialName("naziResCod")
    val residenceNationCode: String? = null,

    /** codice istat comune residenza */
    @SerialName("comuResCodIstat")
    val residenceMunicipalityIstatCode: String? = null,

    /** codice  comune residenza */
    @SerialName("comuResComuneCod")
    val residenceMunicipalityCommonCode: String? = null,

    /** codice istat miur comune residenza */
    @SerialName("comuResCodIstatMiur")
    val residenceMunicipalityMiurIstatCode: String? = null,

    /** descrizione  comune residenza */
    @SerialName("comuResDes")
    val residenceMunicipalityDescription: String? = null,

    /** citta straniera residenza */
    @SerialName("citstraRes")
    val residenceForeignCity: String? = null,

    /** sigla provincia residenza */
    @SerialName("comuResSigla")
    val residenceMunicipalityAbbreviation: String? = null,

    /** descrizione provincia residenza */
    @SerialName("provResDes")
    val residenceProvinceDescription: String? = null,

    /** descrizione via residenza */
    @SerialName("viaRes")
    val residenceStreet: String? = null,

    /** numero civico residenza */
    @SerialName("numCivRes")
    val residenceStreetNumber: String? = null,

    /** cap residenza */
    @SerialName("capRes")
    val residencePostalCode: String? = null,

    /** telefono residenza */
    @SerialName("telRes")
    val residencePhone: String? = null,

    /** prefisso internazionale telefono residenza */
    @SerialName("prefixInternazRes")
    val residenceInternationalPrefix: String? = null,

    /** id nazione domicilio */
    @SerialName("nazDomId")
    val domicileNationId: Long? = null,

    /** id comune domicilio */
    @SerialName("comDomId")
    val domicileMunicipalityId: Long? = null,

    /** codice fiscale nazione domicilio */
    @SerialName("naziDomCodFisc")
    val domicileNationFiscalCode: String? = null,

    /** descrizione nazione domicilio */
    @SerialName("naziDomDes")
    val domicileNationDescription: String? = null,

    /** codice nazione domicilio */
    @SerialName("naziDomNazioneCod")
    val domicileCountryCode: String? = null,

    /** codice nazione domicilio */
    @SerialName("naziDomCod")
    val domicileNationCode: String? = null,

    /** codice istat comune domicilio */
    @SerialName("comuDomCodIstat")
    val domicileMunicipalityIstatCode: String? = null,

    /** codice comune domicilio */
    @SerialName("comuDomComuneCod")
    val domicileMunicipalityCommonCode: String? = null,

    /** codice istat miur comune domicilio */
    @SerialName("comuDomCodIstatMiur")
    val domicileMunicipalityMiurIstatCode: String? = null,

    /** descrizione comune di domicilio */
    @SerialName("comuDomDes")
    val domicileMunicipalityDescription: String? = null,

    /** domicilio citta straniera */
    @SerialName("citstraDom")
    val domicileForeignCity: String? = null,

    /** sigla comune di domicilio */
    @SerialName("comuDomSigla")
    val domicileMunicipalityAbbreviation: String? = null,

    /** descrizione provincia domicilio */
    @SerialName("provDomDes")
    val domicileProvinceDescription: String? = null,

    /** via domicilio */
    @SerialName("viaDom")
    val domicileStreet: String? = null,

    /** numero civico domicilio */
    @SerialName("numCivDom")
    val domicileStreetNumber: String? = null,

    /** CAP domicilio */
    @SerialName("capDom")
    val domicilePostalCode: String? = null,

    /** telefono domicilio */
    @SerialName("telDom")
    val domicilePhone: String? = null,

    /** prefisso internazionale domicilio */
    @SerialName("prefixInternazDom")
    val domicileInternationalPrefix: String? = null,

    /** presso */
    @SerialName("co")
    val co: String? = null,

    /** fax */
    @SerialName("fax")
    val fax: String? = null,

    /** codice tipo indirizzo */
    @SerialName("tipoIndirizCod")
    val addressTypeCode: String? = null,

    /** flag domicilio come residenza */
    @SerialName("domComeResFlg")
    val domicileSameAsResidenceFlag: Boolean? = null,

    /** id dell'user di inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** id dell'user di modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null
)

@Serializable
data class Esse3ForForeignStudent(
    /** id univoco che consente di individuare la carriera */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** ID del Corso di Studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Anno di ordinamento */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** ID del percorso di studio */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** ID iscrizione studente */
    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    /** Codice struttura didattica, aggiornato tramite trigger con il codice della relativa tabella della struttura. */
    @SerialName("sdrCod")
    val siteCode: String? = null,

    /** Descrizione del codice della struttura didattica */
    @SerialName("sdrDes")
    val siteDescription: String? = null,

    /** Codice nazione */
    @SerialName("sdrNaziCod")
    val siteNationCode: String? = null,

    /** Descrizione nazione */
    @SerialName("sdrNaziDes")
    val siteNationDescription: String? = null,

    /** Città della struttura didattica */
    @SerialName("sdrCittà")
    val siteCity: String? = null,

    /** via */
    @SerialName("via")
    val street: String? = null,

    /** cap */
    @SerialName("cap")
    val postalCode: String? = null,

    /** Data di inizio del periodo di studio all´estero. */
    @SerialName("dataInizioPeriodo")
    val periodStartDate: String? = null,

    /** Data di fine del periodo di studio all´estero. */
    @SerialName("dataFinePeriodo")
    val periodEndDate: String? = null,

    /** Maggiorazione dell´importo della borsa per la permanenza all´estero. */
    @SerialName("maggImpBorsa")
    val scholarshipIncreaseImport: Float? = null,

    /** Numero di giorni di permanenza all´estero. */
    @SerialName("numGiorni")
    val daysNumber: Long? = null,

    /** Numero di giorni maggiorazione */
    @SerialName("numGiorniMaggioraz")
    val increaseDaysNumber: Long? = null,

    /** Anno di pagamento del soggiorno all'estero. */
    @SerialName("annoPagamentoId")
    val paymentYearId: Long? = null,

    /** Mese/rata di pagamento del soggiorno all'estero. */
    @SerialName("mesePagamento")
    val paymentMonth: Long? = null
)

@Serializable
data class Esse3ForeignUniversity(
    /** ID numerico univoco dell'ateneo straniero. */
    @SerialName("ateneoStranieroId")
    val foreignUniversityId: Int? = null,

    /** ID nazione. */
    @SerialName("nazioneId")
    val nationId: Int? = null,

    /** Codice della nazione. */
    @SerialName("nazioneCod")
    val nationCode: String? = null,

    /** Descrizione della nazione. */
    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    /** Codice fiscale della nazione. */
    @SerialName("nazioneCodFisc")
    val nationFiscalCode: String? = null,

    /** Codice MIUR della nazione. */
    @SerialName("nazioneMiurCod")
    val miurNationCode: String? = null,

    /** Descrizione ateneo. */
    @SerialName("des")
    val description: String? = null,

    /** Città sede dell'ateneo. */
    @SerialName("citStra")
    val foreignCity: String? = null,

    /** ID numerico unico del comune. */
    @SerialName("comuneId")
    val municipalityId: Int? = null,

    /** Descrizione del comune. */
    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    /** Codice usato nel codice fiscale per identificare il comune di nascita. */
    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    /** Sigla provincia. */
    @SerialName("comuneSigla")
    val municipalityAbbreviation: String? = null,

    /** Codice ISTAT del comune. */
    @SerialName("comuneCodIstat")
    val municipalityIstatCode: String? = null,

    /** CAP. */
    @SerialName("comuneCap")
    val municipalityPostalCode: String? = null,

    /** ID numerico unico del rettore. */
    @SerialName("rettoreId")
    val rectorId: Int? = null,

    /** Cognome del rettore. */
    @SerialName("rettoreCognome")
    val rectorSurname: String? = null,

    /** Nome del rettore. */
    @SerialName("rettoreNome")
    val rectorName: String? = null,

    /** Genere del rettore. */
    @SerialName("rettoreSesso")
    val rectorGender: String? = null,

    /** Codice fiscale del rettore. */
    @SerialName("rettoreCodFis")
    val rectorFiscalCode: String? = null,

    /** Data di nascita del rettore. */
    @SerialName("rettoreDataNascita")
    val rectorBirthDate: String? = null,

    /** Numero di telefono del rettore. */
    @SerialName("rettoreTel")
    val rectorPhone: String? = null,

    /** Numero di cellulare del rettore. */
    @SerialName("rettoreCellulare")
    val rectorMobile: String? = null,

    /** Indirizzo e-mail del rettore. */
    @SerialName("rettoreEmail")
    val rectorEmail: String? = null,

    /** Home page sito WEB. */
    @SerialName("homePage")
    val homePage: String? = null,

    /** Codice Erasmus dell'Ateneo. */
    @SerialName("erasmusCod")
    val erasmusCode: String? = null,

    /** Indirizzo. */
    @SerialName("via")
    val street: String? = null,

    /** Numero di telefono. */
    @SerialName("tel")
    val phone: String? = null,

    /** Prefisso, viene usato solo per nazione di residenza/domicilio diversa da Italia. */
    @SerialName("prefixInternaz")
    val internationalPrefix: String? = null,

    /** Fax. */
    @SerialName("fax")
    val fax: String? = null,

    /** Indirizzo e-mail. */
    @SerialName("email")
    val email: String? = null,

    /** ID della nazione di ordinamento dell'ateneo. */
    @SerialName("nazioneOrdId")
    val orderNationId: Int? = null,

    /** Codice della nazione di ordinamento. */
    @SerialName("nazioneOrdCod")
    val orderNationCode: String? = null,

    /** Descrizione della nazione di ordinamento. */
    @SerialName("nazioneOrdDes")
    val orderNationDescription: String? = null,

    /** Codice fiscale della nazione di ordinamento. */
    @SerialName("nazioneOrdCodFisc")
    val orderNationFiscalCode: String? = null,

    /** Codice MIUR della nazione di ordinamento. */
    @SerialName("nazioneOrdMiurCod")
    val miurOrderNationCode: String? = null,

    /** Codice dell'Ateneo. */
    @SerialName("codiceAteneo")
    val universityCode: String? = null,

    /** Codice PIC (personal identification code) dell'ateneo. */
    @SerialName("codicePic")
    val picCode: String? = null,

    /** Codice MIUR dell'Ateneo. */
    @SerialName("codAteStra")
    val foreignAteCode: String? = null,

    /** Codice europeo utilizzato per identificare un ateneo. */
    @SerialName("codiceSchac")
    val schacCode: String? = null,

    /** Data di inizio validità di questo ateneo straniero. */
    @SerialName("dtIniVal")
    val initialValidityDate: String? = null,

    /** Data di fine validità di questo ateneo straniero. */
    @SerialName("dtFinVal")
    val finalValidityDate: String? = null,

    /** Codice aeroporto. */
    @SerialName("iataCod")
    val iataCode: String? = null
)

@Serializable
data class Esse3HandicapDeclarationCompensatoryMeasures(
    /** Identificativo dichiarazione handicap */
    @SerialName("dicHandId")
    val handicapDeclarationId: Long? = null,

    /** Identificativo misura compensativa */
    @SerialName("dicHandMisureId")
    val handicapDeclarationMeasuresId: Long? = null,

    /** Codice misura compensativa */
    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String? = null,

    /** Descrizione misura compensativa */
    @SerialName("misuraCompensativaDes")
    val compensatoryMeasureDescription: String? = null,

    /** Flag descrizione libera */
    @SerialName("desLiberaFlg")
    val freeDescriptionFlag: Int? = null,

    /** Stato misura compensativa */
    @SerialName("statoMisuraComp")
    val compensatoryMeasureState: String? = null,

    /** Descrizione stato misura compensativa */
    @SerialName("statoMisuraCompDes")
    val compensatoryMeasureStateDescription: String? = null,

    /** Data inizio */
    @SerialName("dataIni")
    val startDate: String? = null,

    /** Data fine */
    @SerialName("dataFine")
    val endDate: String? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null
)

@Serializable
data class Esse3HandicapDeclarationStates(
    /** Codice dello stato della dichiarazione di handicap (P - Presentata, C - Confermata, A - Annullata, B - Bozza). */
    @SerialName("statoDicHand")
    val handicapDeclarationState: String? = null,

    /** Descrizione dello stato della dichiarazione di handicap. */
    @SerialName("statoDicHandDes")
    val handicapDeclarationStateDescription: String? = null
)

@Serializable
data class Esse3GetAuthorizationAttachmentMetadata(
    /** codice tipo associativa allegato */
    @SerialName("tipoAssAllegato")
    val attachmentAssociationType: String? = null,

    /** identificativo allegato */
    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    /** dimensione allegato in byte */
    @SerialName("dimensione")
    val size: Long? = null,

    /** titolo allegato */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione allegato */
    @SerialName("des")
    val description: String? = null,

    /** nome file */
    @SerialName("filename")
    val fileName: String? = null,

    /** estensione file */
    @SerialName("estensione")
    val extension: String? = null,

    /** autore allegato */
    @SerialName("autore")
    val author: String? = null,

    /** data inserimento allegato */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data ultima modifica allegato */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** codice tipologia allegato */
    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    /** flag che indica se l'allegato risulta validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int? = null,

    /** flag che indica se l'allegato risulta visibile da web o meno */
    @SerialName("abilVisWeb")
    val webVisibility: Int? = null,

    /** flag che indica se l'allegato risulta stampabile o meno */
    @SerialName("abilStampaAllegatiFlg")
    val attachmentsPrintAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3Institute(
    /** Identificativo della scuola. */
    @SerialName("scuolaSupId")
    val higherSchoolId: Long? = null,

    /** Descrizione scuola. */
    @SerialName("des")
    val description: String? = null,

    /** Codice della tipologia della scuola. */
    @SerialName("tipologiaCod")
    val typologyCode: String? = null,

    /** Descrizione tipologia scuola. */
    @SerialName("tipologiaDes")
    val typologyDescription: String? = null,

    /** Indirizzo. */
    @SerialName("via")
    val street: String? = null,

    /** Numero civico */
    @SerialName("numeroCivico")
    val streetNumber: String? = null,

    /** CAP. */
    @SerialName("cap")
    val postalCode: String? = null,

    /** Numero di telefono. */
    @SerialName("telefono")
    val phone: String? = null,

    /** Numero fax. */
    @SerialName("fax")
    val fax: String? = null,

    /** Località. */
    @SerialName("localita")
    val locality: String? = null,

    /** Il codice attributo dal sistema informativo del Ministero dell´Istruzione. */
    @SerialName("codMiur")
    val miurCode: String? = null,

    /** Identificativo comune. */
    @SerialName("comuneId")
    val municipalityId: Int? = null,

    /** Descrizione comune. */
    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    /** Codice usato nel codice fiscale per identificare il comune di nascita. */
    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    /** Sigla provincia. */
    @SerialName("comuneSigla")
    val municipalityAbbreviation: String? = null,

    /** Codice ISTAT del comune. */
    @SerialName("comuneCodIstat")
    val municipalityIstatCode: String? = null,

    /** CAP. */
    @SerialName("comuneCap")
    val municipalityPostalCode: String? = null,

    /** Indirizzo di posta elettronica. */
    @SerialName("email")
    val email: String? = null,

    /** Vecchio codice della scuola utilizzato dall´ateneo. */
    @SerialName("codAteneo")
    val universityCode: String? = null,

    /** Indirizzo di posta elettronica ministeriale. */
    @SerialName("emailMinist")
    val ministryEmail: String? = null,

    /** Identificativo della scuola utilizzato dal MURST per passare le informazioni relative alla pre-iscrizione. */
    @SerialName("codUniverso")
    val universeCode: String? = null,

    /** Riporta il codice dell´istituto principale - presente solo per scuole statali. */
    @SerialName("istRifId")
    val referenceInstitutionId: Int? = null,

    /** Riporta il codice del nuovo istituto nel caso di scuole superiori chiuse. */
    @SerialName("nuovoIstId")
    val newInstitutionId: Int? = null,

    /** 1 --> CORSO SERALE, 2 --> ISTITUTO PRINCIPALE, 3 --> ISTITUZIONE EDUCAT., 4 --> SCUOLA COORDINATA, 5 --> SEZIONE STACCATA */
    @SerialName("tipiIstId")
    val institutionTypesId: Int? = null,

    /** Descrizione tipologia indicata dal ministero per le scuole statali. */
    @SerialName("tipiIstDes")
    val institutionTypesDescription: String? = null,

    /** Flag che indica se si tratta di una scuola statale o meno. */
    @SerialName("scuolaNonStatFlg")
    val nonStatutorySchoolFlag: Int? = null,

    /** Distretto */
    @SerialName("distretto")
    val district: String? = null,

    /** Anno inizio validità scuola. */
    @SerialName("aaIniVal")
    val academicYearStartValidity: Int? = null,

    /** Anno fine validità scuola. */
    @SerialName("aaFineVal")
    val academicYearEndValidity: Int? = null,

    /** Identificativo scuola MIUR. */
    @SerialName("idScuolaMiur")
    val miurSchoolId: Int? = null,

    /** Indica la visibilità da web di questo istituto. */
    @SerialName("webFlg")
    val webFlag: Int? = null,

    /** Indica se la scuola superiore deve essere esclusa dall'aggiornamento dei dati anagrafici. */
    @SerialName("noAggiornaFlg")
    val noUpdateFlag: Int? = null,

    /** Campo note. */
    @SerialName("note")
    val notes: String? = null,

    /** Campo note che tiene traccia di eventuali interventi di riunificazione avvenuti. */
    @SerialName("noteCronologia")
    val chronologyNotes: String? = null,

    /** Indica se l'inserimento dell'Ateneo è certificato dalle procedure di sistema o dalla migrazione nuova di UNIBASE. */
    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    /** Codice meccanografico della scuola. */
    @SerialName("codScuola")
    val schoolCode: String? = null,

    /** statale */
    @SerialName("stataleFlg")
    val stateFlag: Int? = null,

    /** Codice della scuola principale nel caso di sezioni staccate di un istituto ed è valorizzato solo per le scuole statali. */
    @SerialName("codiceScuolaRiferimento")
    val referenceSchoolCode: String? = null
)

@Serializable
data class Esse3ItalianTitlePerson(
    /** Identificativo univoco della persona */
    @SerialName("persId")
    val personId: String? = null,

    /** Anno Accademico di conseguimento del titolo. */
    @SerialName("aaConsegTitolo")
    val academicYearTitleAward: Int? = null,

    /** Flag che indica se  stata ottenuta labilitazione al sostegno. */
    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    /** Appellativo femminile del titolo italiano. */
    @SerialName("appellativoF")
    val femaleTitle: String? = null,

    /** Appellativo maschile del titolo italiano. */
    @SerialName("appellativoM")
    val maleTitle: String? = null,

    /** Base del voto. */
    @SerialName("baseVoto")
    val baseGrade: Int? = null,

    @SerialName("certAns")
    val certAnswer: Long? = null,

    /** Numero di CFU associati al titolo. */
    @SerialName("cfu")
    val credits: Float? = null,

    /** Codice Classe di Abilitazione SSIS, valorizzato se il livello di dettaglio della graduatoria  uno dei seguentI: CLAAB, CLAAB o SEDE. */
    @SerialName("claAbCod")
    val abbreviatedClassCode: String? = null,

    /** Indica se lo studente autorizza linvio automatico della conferma del titolo all'ordine. Valido solamente per Abilitazioni professionali. */
    @SerialName("confInvioOrdineFlg")
    val orderSendingConfirmationFlag: Int? = null,

    /** Data di conseguimento del titolo. */
    @SerialName("dataConsegTitolo")
    val titleDeliveryDate: String? = null,

    /** Data domanda di tirocinio */
    @SerialName("dataDomTiro")
    val internshipApplicationDate: String? = null,

    /** Data fine attivita. */
    @SerialName("dataFineAttivita")
    val activityEndDate: String? = null,

    /** Data di fine del tirocinio. */
    @SerialName("dataFinTiro")
    val internshipEndDate: String? = null,

    /** Data inizio attivita. */
    @SerialName("dataIniAttivita")
    val activityStartDate: String? = null,

    /** Data di inizio del tirocinio. */
    @SerialName("dataIniTiro")
    val internshipStartDate: String? = null,

    /** Data di iscrizione allordine professionale. */
    @SerialName("dataIscrOrdProf")
    val professionalOrderEnrollmentDate: String? = null,

    /** Descrizione libera del corso, utilizzata nel caso in cui questo non sia codificato. */
    @SerialName("desCds")
    val courseOfStudyDescription: String? = null,

    /** Descrizione estesa dellente. Utilizzato per presentare l'ente. */
    @SerialName("desEstesa")
    val extendedDescription: String? = null,

    /** Identificativo della domanda di conseguimento titolo. */
    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    /** Flag domanda di riconoscimento di titolo straniero. Indica che la persona ha inoltrato la domanda di riconoscimento di titolo straniero. */
    @SerialName("domRicoTitStraFlg")
    val domicileForeignTitleRecoveryFlag: Int? = null,

    /** Ente a cui  stata richiesta la conferma del tirocinio: A - Ateneo, S - Struttura didattica responsabile. */
    @SerialName("indInvioRichConfTiro")
    val indexSendingInternshipConfirmationRequest: String? = null,

    /** Flag che indica se  stata ottenuta liscrizione all'albo. */
    @SerialName("iscrAlboFlg")
    val registerEnrollmentFlag: Int? = null,

    @SerialName("linguaDes")
    val languageDescription: String? = null,

    /** Flag che indica se  stata conseguita la lode. */
    @SerialName("lode")
    val cumLaude: Int? = null,

    /** Media dei voti. */
    @SerialName("mediaVoti")
    val gradesAverage: Float? = null,

    /** Note libere per il titolo */
    @SerialName("nota")
    val note: String? = null,

    /** Numero di anni impiegati per il conseguimento del titolo. */
    @SerialName("numAnniConseguimento")
    val achievementYearsNumber: Int? = null,

    /** Numero dordine della classe di abilitazione. */
    @SerialName("ordine")
    val order: Long? = null,

    /** Codice Ordine/Collegio professionale */
    @SerialName("ordProfCod")
    val professionalOrderCode: String? = null,

    /** Identificativo Ordine/Collegio professionale. */
    @SerialName("ordProfId")
    val professionalOrderId: Long? = null,

    /** Descrive leventuale indirizzo dello studente. */
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

    /** Data domanda conseguimento titolo. */
    @SerialName("p12DomCtDataDomCt")
    val p12CommitteeApplicationDate: String? = null,

    /** Stato domanda conseguimento titolo. */
    @SerialName("p12DomCtStato")
    val p12CommitteeApplicationState: String? = null,

    /** Flag di riconoscimento di titolo straniero. Indica che il titolo  stato conseguito per riconoscimento di titolo straniero. */
    @SerialName("ricoTitStraFlg")
    val foreignTitleRecoveryFlag: Int? = null,

    /** Indica se il CDS  stato istituito in accordo con la riforma e quindi pu essere associato ad una classe di laurea. 0 = NUOVO: abilita associazione con classe 1 = VECCHIO: disabilita associazione con classe */
    @SerialName("rifFlg")
    val referenceFlag: Int? = null,

    /** Identificativo numerico univoco della sede della clase di abilitazione per le SSIS. */
    @SerialName("sedeClaAbId")
    val abbreviatedClassSiteId: Long? = null,

    @SerialName("sedeDes")
    val siteDescription: String? = null,

    /** Sessione di conseguimento titolo. */
    @SerialName("sessione")
    val session: String? = null,

    @SerialName("statiDomTiroDes")
    val internshipApplicationStatesDescription: String? = null,

    /** Stato del il tipo titolo: C = Conseguito, I = In ipotesi */
    @SerialName("staTitItCod")
    val italianTitleStatusCode: String? = null,

    @SerialName("statiTitItDes")
    val italianTitleStatesDescription: String? = null,

    /** Flag che indica se il titolo  stato ottenuto nello stesso ateneo. */
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

    /** TIPO DOCUMENTO CONSEGNATO: N --> Nessun diploma depositato O --> Diploma originale depositato AUT --> Autocertificazione presentata F --> Fotocopia FAUT --> Fotocopia autenticata LAUESA --> Copia dellattestazione del titolo di laurea con elenco degli esami svolti */
    @SerialName("tipoDepositoCod")
    val depositTypeCode: String? = null,

    /** Codice Tipo Giudizio Prova Finale. Es. Insufficiente, Sufficiente, Buono. */
    @SerialName("tipoGiudProFinCod")
    val finalProjectJudgmentTypeCode: String? = null,

    /** Codice tipo di riconoscimento titolo straniero. */
    @SerialName("tipoRicoTitStraCod")
    val foreignTitleRecoveryTypeCode: String? = null,

    /** Codice Tipo di Titolo Italiano. Tabella di decodifica. */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    /** Indica se con il conseguimento dellesame di stato  stato conseguito anche il tirocinio. */
    @SerialName("tirocinioFlg")
    val internshipFlag: Int? = null,

    @SerialName("titAccAmm")
    val adminTitleAccess: Int? = null,

    @SerialName("titAccMat")
    val matTitleAccess: Int? = null,

    @SerialName("titAccMatStu")
    val studentMatTitleAccess: Long? = null,

    /** Codice di dettaglio del titolo. */
    @SerialName("tititCod")
    val titleTypeCode: String? = null,

    /** Identificativo univoco del titolo */
    @SerialName("titItId")
    val italianTitleId: Long? = null,

    /** Titolo tesi. */
    @SerialName("titoloTesi")
    val thesisTitle: String? = null,

    /** Indica se il titolo  stato valutato. */
    @SerialName("valutatoFlg")
    val evaluatedFlag: Int? = null,

    @SerialName("vDecodeTititCodDes")
    val vDecodeTitleTypeCodeDescription: String? = null,

    /** Voto conseguito. */
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
    /** Identificativo soggetto esterno. */
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    /** Cognome. */
    @SerialName("cognome")
    val surname: String = "",

    /** Nome. */
    @SerialName("nome")
    val name: String = "",

    /** Codice Fiscale. */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Genere (M o F). */
    @SerialName("sesso")
    val gender: String? = null,

    /** Data di nascita. */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** Tipo soggetto esterno. */
    @SerialName("tipoSoggEstCod")
    val externalSubjectTypeCode: String? = null,

    /** Struttura didattica responsabile. */
    @SerialName("sdrId")
    val siteId: Long? = null,

    /** Nazione di nascita. */
    @SerialName("naziNascId")
    val birthNationId: Long? = null,

    /** Comune di nascita. */
    @SerialName("comNascId")
    val birthMunicipalityId: Long? = null,

    /** Città straniera di nascita. */
    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    /** Cittadinanza. */
    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    /** Numero di Telefono. */
    @SerialName("tel")
    val phone: String? = null,

    /** Prefisso internazionale. */
    @SerialName("prefixInternaz")
    val internationalPrefix: String? = null,

    /** Fax. */
    @SerialName("fax")
    val fax: String? = null,

    /** Cellulare. */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** Indirizzo email. */
    @SerialName("email")
    val email: String? = null,

    /** Appellativo. */
    @SerialName("appellativo")
    val title: String? = null,

    /** Firma. */
    @SerialName("firmaId")
    val signatureId: Long? = null,

    /** Identificativo dipartimento. */
    @SerialName("dipId")
    val departmentId: Long? = null,

    /** Nominativo alternativo. */
    @SerialName("nominativoAlt")
    val alternativeFullName: String? = null,

    /** Identificativo esterno anagrafica. */
    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    /** Operatore cellulare. */
    @SerialName("operCellulare")
    val mobileOperator: Long? = null,

    /** Consenso invio SMS. */
    @SerialName("consSmsFlg")
    val consentSmsFlag: Long = 0L,

    /** Ateneo che detiene le credenziali del tipo soggetto esterno PA (pubblica amministrazione). */
    @SerialName("ateIdAccreditamento")
    val ateAccreditationId: Long? = null,

    /** Data inizio attività. */
    @SerialName("dataIniAtt")
    val activityStartDate: String? = null,

    /** Data fine attività. */
    @SerialName("dataFinAtt")
    val activityEndDate: String? = null
)

@Serializable
data class Esse3PersonalDataConsentsHistory(
    /** Codice tipo consenso */
    @SerialName("tipoConsensoCod")
    val consentTypeCode: String? = null,

    /** Descrizione tipo consenso */
    @SerialName("tipiConsensoDes")
    val consentTypesDescription: String? = null,

    /** Identificativo storico consenso */
    @SerialName("stoId")
    val historicalId: Long? = null,

    /** Etichetta tipo consenso */
    @SerialName("tipiConsensoEtichetta")
    val consentTypesLabel: String? = null,

    /** Flag consenso */
    @SerialName("consensoFlg")
    val consentFlag: Int? = null,

    /** Data inizio validità consenso */
    @SerialName("dataIni")
    val startDate: String? = null,

    /** Data fine validità consenso */
    @SerialName("dataFine")
    val endDate: String? = null,

    /** Codice procedura amministrativa */
    @SerialName("procAmmCod")
    val administrativeProcedureCode: String? = null,

    /** Descrizione procedura amministrativa */
    @SerialName("procAmmDes")
    val administrativeProcedureDescription: String? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null
)

@Serializable
data class Esse3Tutors(
    /** Identificativo tutore */
    @SerialName("anaperTutoreId")
    val personGuardianId: Long? = null,

    /** Identificativo autorizzato */
    @SerialName("autorizzatoId")
    val authorizedId: Long? = null,

    /** Note sul tutore */
    @SerialName("nota")
    val note: String? = null,

    /** Data inizio validità */
    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    /** Data fine validità */
    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data inserimento record */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Data ultima modifica record */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Stato record */
    @SerialName("stato")
    val state: String? = null,

    /** Descrizione stato */
    @SerialName("statoDes")
    val stateDescription: String? = null,

    /** Tipo parentela codice */
    @SerialName("tipoParCod")
    val paragraphTypeCode: String? = null,

    /** Tipo parentela descrizione */
    @SerialName("tipoParDes")
    val paragraphTypeDescription: String? = null,

    /** Nome tutore */
    @SerialName("nome")
    val name: String? = null,

    /** Cognome tutore */
    @SerialName("cognome")
    val surname: String? = null,

    /** Codice fiscale tutore */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** codice di errore nel controllo del CF */
    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Long? = null,

    /** Flag certificato */
    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    /** Sesso tutore */
    @SerialName("sesso")
    val gender: String? = null,

    /** Data nascita tutore */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** Identificativo nazione */
    @SerialName("nazioneId")
    val nationId: Int? = null,

    /** Codice nazione */
    @SerialName("naziCod")
    val nationCode: String? = null,

    /** Descrizione nazione */
    @SerialName("naziDes")
    val nationDescription: String? = null,

    /** Codice ISO nazione */
    @SerialName("naziNazioneCod")
    val nationNationCode: String? = null,

    /** Codice internazionale nazione */
    @SerialName("naziCodInt")
    val nationInternationalCode: String? = null,

    /** Città o stato di nascita */
    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    /** Identificativo comune */
    @SerialName("comuneId")
    val municipalityId: Long? = null,

    /** Codice comune */
    @SerialName("comuCod")
    val municipalityCode: String? = null,

    /** Codice catastale comune */
    @SerialName("comuCodCatastale")
    val municipalityCadastralCode: String? = null,

    /** Codice ISTAT/MIUR comune */
    @SerialName("comuCodIstatMiur")
    val municipalityMiurIstatCode: String? = null,

    /** Descrizione comune */
    @SerialName("comuDes")
    val municipalityDescription: String? = null,

    /** Sigla comune */
    @SerialName("comuSigla")
    val municipalityAbbreviation: String? = null,

    /** Provincia */
    @SerialName("provDes")
    val provinceDescription: String? = null,

    /** Email */
    @SerialName("email")
    val email: String? = null,

    /** PEC */
    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

    /** Numero cellulare */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** Codice esterno autorizzato */
    @SerialName("autExtCod")
    val externalAuthorizationCode: String? = null
)

@Serializable
data class Esse3PhDCareer(
    /** id univoco che consente di individuare la persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** cognome della persona */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome della persona */
    @SerialName("nome")
    val name: String? = null,

    /** data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** sesso della persona */
    @SerialName("sesso")
    val gender: String? = null,

    /** id univoco che consente di individuare l'account utente */
    @SerialName("userId")
    val userId: String? = null,

    /** codice fiscale della personae */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** email personale dello studente */
    @SerialName("email")
    val email: String? = null,

    /** indirizzo email assegnato dall'ateneo allo studente */
    @SerialName("emailAte")
    val universityEmail: String? = null,

    /** Anno di ordinamento */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** ID del percorso di studio */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** ID iscrizione studente */
    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    /** identificativo corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** id univoco che consente di individuare la carriera */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** sigla che identifica lo stato della carriera */
    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    /** sigla che identifica il motivo dello stato della carriera */
    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    /** anno di immatricolazione */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** descrizione anno accademico */
    @SerialName("aaDes")
    val academicYearDescription: String? = null,

    /** data di immatricolazione */
    @SerialName("dataImm")
    val matriculationDate: String? = null,

    /** descrizione dello stato della carriera */
    @SerialName("statiStuDes")
    val studentStatesDescription: String? = null,

    /** descrizione del motivo della stato della carriera */
    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    /** data di inserimento (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data di modifica (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Codice esterno carriera */
    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    /** anno iscrizione */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    /** data iscrizione */
    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    /** matricola */
    @SerialName("matricola")
    val matricola: String? = null,

    /** Indica lo stato della posizione della matricola. I valori di sistema sono:  A =  Attivo, S = Sospeso, I = Ipotesi */
    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    /** Causale dello stato della matricola. */
    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    /** id della sede */
    @SerialName("sedeId")
    val siteId: Int? = null,

    /** Tipo di iscrizione all´anno di corso specificato: IC = In Corso, FC = Fuori Corso, RI = Ripetente */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** Flag che indica se l´iscrizione è part-time (1) oppure full-time (0). */
    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    /** Flag che indica se nell´anno dell´iscrizione lo studente era sospeso e quindi se l´iscrizione era fittizia. */
    @SerialName("sospFlg")
    val suspensionFlag: Int? = null,

    /** descrizione della sede */
    @SerialName("sediDes")
    val sitesDescription: String? = null,

    /** Codice mnemonico del corso di studio */
    @SerialName("p06CdsCod")
    val p06CourseOfStudyCode: String? = null,

    /** Descrizione del corso di studio */
    @SerialName("p06CdsDes")
    val p06CourseOfStudyDescription: String? = null,

    /** Codice mnemonico dell ordinamento del corso di studio */
    @SerialName("p06CdsordCod")
    val p06CourseOfStudyOrderCode: String? = null,

    /** Descrizione dell ordinamento del corso di studio */
    @SerialName("p06CdsordDes")
    val p06CourseOfStudyOrderDescription: String? = null,

    /** Codice mnemonico dell ordinamento del percorso di studio */
    @SerialName("p06PdsordCod")
    val p06StudyPlanOrderCode: String? = null,

    /** Descrizione dell ordinamento percorso di studio */
    @SerialName("p06PdsordDes")
    val p06StudyPlanOrderDescription: String? = null,

    /** anno di corso */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** codice facoltà */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione facoltà */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** codice csa della facoltà */
    @SerialName("facCsaCod")
    val facultyCsaCode: String? = null,

    /** identificativo della tipologia di categoria amministrativa */
    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    /** descrizione della tipologia di categoria amministrativa */
    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    /** Profilo studente */
    @SerialName("profCod")
    val professionCode: String? = null,

    /** descrizione profilo studente */
    @SerialName("tipiProfstuDes")
    val studentProfessionTypesDescription: String? = null,

    /** codice tipologia corso */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null
)

@Serializable
data class Esse3Tutor(
    /** ID della persona che delega */
    @SerialName("persId")
    val personId: Long? = null,

    /** Identificativo autorizzato. */
    @SerialName("autorizzatoId")
    val authorizedId: Long? = null,

    /** Nome della persona autorizzata */
    @SerialName("nome")
    val name: String? = null,

    /** Cognome della persona autorizzata */
    @SerialName("cognome")
    val surname: String? = null,

    /** Codice fiscale della persona autorizzata */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** M --> maschio, F --> femmina */
    @SerialName("sesso")
    val gender: String? = null,

    /** Data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** ID del comune di nascita della persona autorizzata */
    @SerialName("comuneId")
    val municipalityId: Long? = null,

    /** Descrizione comeune di nascita */
    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    /** Vincolo di parentela con chi delega: Padre, Madre, Fratello, Sorella, Moglie, Marito, Nessuna parentela,... */
    @SerialName("tipoParCod")
    val paragraphTypeCode: String? = null,

    /** Descrizione del vincolo di parentela */
    @SerialName("tipoParDes")
    val paragraphTypeDescription: String? = null,

    /** Codice di errore nel controllo del CF */
    @SerialName("contrCfCod")
    val contractFiscalCodeCode: Long? = null,

    /** Indica se i dati anagrafici sono stati certificati. */
    @SerialName("certificatoFlg")
    val certifiedFlag: Int? = null,

    /** ID Nazione di nascita della persona autorizzata */
    @SerialName("nazioneId")
    val nationId: Long? = null,

    /** Descrizione nazione di nascita */
    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    /** Città straniera di nascita. */
    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    /** Indirizzo email. */
    @SerialName("email")
    val email: String? = null,

    /** Indirizzo email Certificata (PEC). */
    @SerialName("emailCertificata")
    val certifiedEmail: String? = null,

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
    val modificationDate: String? = null,

    /** Numero di cellulare. */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** Codice esterno autorizzato. */
    @SerialName("autExtCod")
    val externalAuthorizationCode: String? = null,

    /** Codice testata regole di richiesta tutori. */
    @SerialName("regTutoriTstCod")
    val tutorsTestRegistrationCode: String? = null,

    /** Descrizione testata regole di richiesta tutori. */
    @SerialName("regTutoriTstDes")
    val tutorsTestRegistrationDescription: String? = null,

    /** Codice dettaglio regole di richiesta tutori. */
    @SerialName("regTutoriDettCod")
    val tutorsDetailRegistrationCode: String? = null,

    /** Descrizione dettaglio regole di richiesta tutori. */
    @SerialName("regTutoriDettDes")
    val tutorsDetailRegistrationDescription: String? = null,

    /** Data inizio validità associazione anagrafica studente / tutore. */
    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    /** Data fine validità associazione anagrafica studente / tutore. */
    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    /** Stato tutore. A - Attivo, B - Bozza, X - Annullato */
    @SerialName("stato")
    val state: String? = null
)

@Serializable
data class Esse3HigherInstituteTypes(
    /** Codice della tipologia. */
    @SerialName("tipologiaCod")
    val typologyCode: String? = null,

    /** Descrizione. */
    @SerialName("des")
    val description: String? = null,

    /** Indica se il record è di sistema. */
    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    /** Codice Alma Laurea. */
    @SerialName("almaCod")
    val almaCode: Int? = null,

    /** Codice tipologia di scuola MIUR. */
    @SerialName("tipoScuolaMiurCod")
    val miurSchoolTypeCode: String? = null
)

@Serializable
data class Esse3PersonalDataExternalCode(
    /** Codice tipo codice esterno */
    @SerialName("tipoCodExt")
    val externalTypeCode: String? = null,

    /** Descrizione tipo codice esterno */
    @SerialName("tipoCodExtDes")
    val externalTypeDescription: String? = null,

    /** Note tipo codice esterno */
    @SerialName("tipoCodExtNota")
    val externalTypeNote: String? = null,

    /** Codice esterno */
    @SerialName("codExt")
    val externalCode: String? = null,

    /** Identificativo ateneo */
    @SerialName("ateneoId")
    val universityId: Long? = null,

    /** Codice ISTAT ateneo */
    @SerialName("ateneoIstatCod")
    val universityIstatCode: String? = null,

    /** Codice universitario ateneo */
    @SerialName("ateneoCodeUn")
    val universityUnifiedCode: String? = null,

    /** Descrizione ateneo */
    @SerialName("ateneoDes")
    val universityDescription: String? = null,

    /** Utente inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente modifica */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data modifica */
    @SerialName("dataMod")
    val modificationDate: String? = null
)

@Serializable
data class Esse3ConsentsParameters(
    /** Codice tipo consenso richiesto. */
    @SerialName("tipoConsensoCod")
    val consentTypeCode: String = "",

    /** Indica se il consenso è stato dato. */
    @SerialName("consensoFlg")
    val consentFlag: Int = 0,

    /** Data inizio consenso o negazione del consenso. */
    @SerialName("dataIni")
    val startDate: String? = null
)

@Serializable
data class Esse3AttachmentsOperationsResult(
    /** codice di ritorno */
    @SerialName("retCode")
    val returnCode: Int? = null,

    /** descrizione dell'errore */
    @SerialName("retErrMsg")
    val returnErrorMessage: String? = null
)

@Serializable
data class Esse3PhDProgramCareer(
    /** id univoco che consente di individuare la persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** cognome della persona */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome della persona */
    @SerialName("nome")
    val name: String? = null,

    /** data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** sesso della persona */
    @SerialName("sesso")
    val gender: String? = null,

    /** id univoco che consente di individuare l'account utente */
    @SerialName("userId")
    val userId: String? = null,

    /** codice fiscale della personae */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** email personale dello studente */
    @SerialName("email")
    val email: String? = null,

    /** indirizzo email assegnato dall'ateneo allo studente */
    @SerialName("emailAte")
    val universityEmail: String? = null,

    /** Anno di ordinamento */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** ID del percorso di studio */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** ID iscrizione studente */
    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    /** identificativo corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** id univoco che consente di individuare la carriera */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** sigla che identifica lo stato della carriera */
    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    /** sigla che identifica il motivo dello stato della carriera */
    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    /** anno di immatricolazione */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** descrizione anno accademico */
    @SerialName("aaDes")
    val academicYearDescription: String? = null,

    /** data di immatricolazione */
    @SerialName("dataImm")
    val matriculationDate: String? = null,

    /** descrizione dello stato della carriera */
    @SerialName("statiStuDes")
    val studentStatesDescription: String? = null,

    /** descrizione del motivo della stato della carriera */
    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    /** data di inserimento (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data di modifica (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Codice esterno carriera */
    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    /** anno iscrizione */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    /** data iscrizione */
    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    /** matricola */
    @SerialName("matricola")
    val matricola: String? = null,

    /** Indica lo stato della posizione della matricola. I valori di sistema sono:  A =  Attivo, S = Sospeso, I = Ipotesi */
    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    /** Causale dello stato della matricola. */
    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    /** id della sede */
    @SerialName("sedeId")
    val siteId: Int? = null,

    /** Tipo di iscrizione all´anno di corso specificato: IC = In Corso, FC = Fuori Corso, RI = Ripetente */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** Flag che indica se l´iscrizione è part-time (1) oppure full-time (0). */
    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    /** Flag che indica se nell´anno dell´iscrizione lo studente era sospeso e quindi se l´iscrizione era fittizia. */
    @SerialName("sospFlg")
    val suspensionFlag: Int? = null,

    /** descrizione della sede */
    @SerialName("sediDes")
    val sitesDescription: String? = null,

    /** Codice mnemonico del corso di studio */
    @SerialName("p06CdsCod")
    val p06CourseOfStudyCode: String? = null,

    /** Descrizione del corso di studio */
    @SerialName("p06CdsDes")
    val p06CourseOfStudyDescription: String? = null,

    /** Codice mnemonico dell ordinamento del corso di studio */
    @SerialName("p06CdsordCod")
    val p06CourseOfStudyOrderCode: String? = null,

    /** Descrizione dell ordinamento del corso di studio */
    @SerialName("p06CdsordDes")
    val p06CourseOfStudyOrderDescription: String? = null,

    /** Codice mnemonico dell ordinamento del percorso di studio */
    @SerialName("p06PdsordCod")
    val p06StudyPlanOrderCode: String? = null,

    /** Descrizione dell ordinamento percorso di studio */
    @SerialName("p06PdsordDes")
    val p06StudyPlanOrderDescription: String? = null,

    /** anno di corso */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** codice facoltà */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione facoltà */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** codice csa della facoltà */
    @SerialName("facCsaCod")
    val facultyCsaCode: String? = null,

    /** identificativo della tipologia di categoria amministrativa */
    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    /** descrizione della tipologia di categoria amministrativa */
    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    /** Profilo studente */
    @SerialName("profCod")
    val professionCode: String? = null,

    /** descrizione profilo studente */
    @SerialName("tipiProfstuDes")
    val studentProfessionTypesDescription: String? = null,

    /** codice tipologia corso */
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
