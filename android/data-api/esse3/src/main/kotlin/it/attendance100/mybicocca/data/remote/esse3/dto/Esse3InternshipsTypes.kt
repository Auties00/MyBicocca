package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3InternshipApplicationAttachments(
    /** id della domanda dello studente */
    @SerialName("domTiroId")
    val domicileInternshipId: Long? = null,

    /** id dell'allegato */
    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    /** id della carriera dello studente */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** dimensione dell'allegato */
    @SerialName("dimensione")
    val size: Long? = null,

    /** titolo dell'allegato */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione dell'allegato */
    @SerialName("des")
    val description: String? = null,

    /** nome del file dell'allegato */
    @SerialName("filename")
    val fileName: String? = null,

    /** id del gruppo di appartenenza dell'utente che ha inserito/modificato l'allegato */
    @SerialName("grpId")
    val groupId: Long? = null,

    /** user_name dell'utente che ha inserito/modificato l'allegato */
    @SerialName("userMod")
    val modificationUser: String? = null,

    /** data di modifica dell'allegato */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** user_id dell'utente che ha inserito l'allegato */
    @SerialName("userInsId")
    val insertionUserId: String? = null,

    /** utente collegato è il proprietario dell'allegato */
    @SerialName("isOwner")
    val isOwner: Int? = null,

    /** codice della tipologia di allegato */
    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null
)

@Serializable
data class Esse3InternshipApplicationDetail(
    /** id della domanda di tirocinio dello studente */
    @SerialName("domTiroId")
    val domicileInternshipId: Long? = null,

    /** progressivo della domanda di tirocinio dello studente */
    @SerialName("domTiroPrg")
    val domicileInternshipProgram: Int? = null,

    /** id della carriera dello studente */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** id del corso di studio di iscrizione dello studente */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** anno accademico di iscrizione dello studente */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** id del percorso di studio di iscrizione dello studente */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** id della convenzione */
    @SerialName("sdrId")
    val siteId: Long? = null,

    /** numero di Protocollo della convenzione */
    @SerialName("numProtocollo")
    val protocolNumber: String? = null,

    /** descrizione della convenzione */
    @SerialName("cnvzDes")
    val conventionDescription: String? = null,

    /** id della struttura didattica della convenzione */
    @SerialName("sdrCnvzId")
    val conventionSiteId: Long? = null,

    /** data di inizio convenzione, il formato con cui deve essere definita la data � DD/MM/YYYY */
    @SerialName("dataIniCnvz")
    val conventionStartDate: String? = null,

    /** data di fine convenzione, il formato con cui deve essere definita la data � DD/MM/YYYY */
    @SerialName("dataFinCnvz")
    val conventionEndDate: String? = null,

    /** frequenza assegnata di ufficio */
    @SerialName("defaultFlg")
    val defaultFlag: Int? = null,

    /** titolo opportunità */
    @SerialName("oppTitolo")
    val opportunityTitle: String? = null,

    /** descrizione opportunità */
    @SerialName("oppDes")
    val opportunityDescription: String? = null,

    /** sede opportunità */
    @SerialName("oppSede")
    val opportunitySite: String? = null,

    /** codice del tipo di periodo del tirocionio */
    @SerialName("tipoPeriodoCod")
    val periodTypeCode: String? = null,

    /** descrizione del tipo di periodo del tirocinio */
    @SerialName("tipoPeriodoDes")
    val periodTypeDescription: String? = null,

    /** data di inizio tirocinio, il formato con cui deve essere definita la data � DD/MM/YYYY */
    @SerialName("dataIniTiro")
    val internshipStartDate: String? = null,

    /** data di fine tirocinio, il formato con cui deve essere definita la data � DD/MM/YYYY */
    @SerialName("dataFinTiro")
    val internshipEndDate: String? = null,

    /** orario di lavoro previsto */
    @SerialName("orarioPrevisto")
    val expectedSchedule: String? = null,

    /** cognome del responsabile ufficio */
    @SerialName("respUffCognome")
    val officeResponsibleSurname: String? = null,

    /** nome del responsabile ufficio */
    @SerialName("respUffNome")
    val officeResponsibleName: String? = null,

    /** id del docente tutor */
    @SerialName("tutDoceId")
    val tutorLecturerId: Long? = null,

    /** matricola del docente tutor */
    @SerialName("tutDoceMatricola")
    val tutorLecturerMatricola: String? = null,

    /** cognome del docente tutor */
    @SerialName("tutDoceCognome")
    val tutorLecturerSurname: String? = null,

    /** nome del docente tutor */
    @SerialName("tutDoceNome")
    val tutorLecturerName: String? = null,

    /** id del soggetto esterno del tutor */
    @SerialName("tutSoggId")
    val tutorSubjectId: Long? = null,

    /** codice fiscale del soggetto esterno tutor */
    @SerialName("tutSoggCodFis")
    val tutorSubjectFiscalCode: String? = null,

    /** cognome del soggetto esterno tutor */
    @SerialName("tutSoggCognome")
    val tutorSubjectSurname: String? = null,

    /** nome del soggetto esterno tutor */
    @SerialName("tutSoggNome")
    val tutorSubjectName: String? = null,

    /** matricola del docente delegato del tutor */
    @SerialName("delTutMatricola")
    val tutorStudentIdDelete: String? = null,

    /** cognome del docente delegato del tutor */
    @SerialName("delTutCognome")
    val tutorSurnameDelete: String? = null,

    /** nome del docente delegato del tutor */
    @SerialName("delTutNome")
    val tutorNameDelete: String? = null,

    /** codice tipologia di stage */
    @SerialName("tipoStageCod")
    val stageTypeCode: String? = null,

    /** descrizione del tipo di periodo del tirocinio */
    @SerialName("tipiStageDes")
    val stageTypesDescription: String? = null,

    /** id del settore legato all'area */
    @SerialName("areeSettId")
    val areasSectorsId: Long? = null,

    /** descrizione del settore legato all'area */
    @SerialName("areeSettDes")
    val areasSectorsDescription: String? = null,

    /** descrizione dell'area */
    @SerialName("areaDes")
    val areaDescription: String? = null,

    /** indica se per lo studente è prevista la richiesta di riconoscimento crediti per l´attività di stage. Tale dato è ereditato dalla "tipologia di stage". */
    @SerialName("abilRicCfu")
    val cFURecognitionAuthorization: Int? = null,

    /** abilita la richiesta di valutazione finale */
    @SerialName("abilValfin")
    val finalValidationAuthorization: Int? = null,

    /** data di inizio compilazione della valutazione finale */
    @SerialName("dataIniValfin")
    val finalEvaluationStartDate: String? = null,

    /** data di fine compilazione della valutazione finale */
    @SerialName("dataFinValfin")
    val finalEvaluationEndDate: String? = null,

    /** Numero di giorni prima della fine della conclusione dello stage da cui sarà abilitata la compilazione della valutazione finale. */
    @SerialName("numGgIniValfin")
    val finalEvaluationStartDaysNumber: Int? = null,

    /** Numero di giorni prima della fine della conclusione dello stage entro cui sarà abilitata la compilazione della valutazione finale. Numeri negativi indicheranno che la compilazione è prevista anche oltre la data di fine stage. */
    @SerialName("numGgFinValfin")
    val finalEvaluationEndDaysNumber: Int? = null,

    /** abilita la richiesta di valutazione mid-term */
    @SerialName("abilValmt")
    val midtermValidationAuthorization: Int? = null,

    /** data di inizio compilazione della valutazione mid-term */
    @SerialName("dataIniValmt")
    val mtEvaluationStartDate: String? = null,

    /** data di fine compilazione della valutazione mid-term */
    @SerialName("dataFinValmt")
    val mtEvaluationEndDate: String? = null,

    /** Numero di giorni prima della fine della conclusione dello stage da cui sarà abilitata la compilazione della valutazione mid-term. */
    @SerialName("numGgIniValmt")
    val mtEvaluationStartDaysNumber: Int? = null,

    /** Numero di giorni prima della fine della conclusione dello stage entro cui sarà abilitata la compilazione della valutazione mid-term. Numeri negativi indicheranno che la compilazione è prevista anche oltre la data di fine stage. */
    @SerialName("numGgFinValmt")
    val mtEvaluationEndDaysNumber: Int? = null,

    /** abilita la richiesta di relazione finale */
    @SerialName("abilRelfin")
    val finalRelationAuthorization: Int? = null,

    /** data di inizio compilazione della relazione finale */
    @SerialName("dataIniRelfin")
    val finalRelationStartDate: String? = null,

    /** data di fine compilazione della relazione finale */
    @SerialName("dataFinRelfin")
    val finalRelationEndDate: String? = null,

    /** Numero di giorni prima della fine della conclusione dello stage da cui sarà abilitata la compilazione della relazione finale. */
    @SerialName("numGgIniRelfin")
    val finalRelationStartDaysNumber: Int? = null,

    /** Numero di giorni prima della fine della conclusione dello stage entro cui sarà abilitata la compilazione della relazione finale. Numeri negativi indicheranno che la compilazione è prevista anche oltre la data di fine stage. */
    @SerialName("numGgFinRelfin")
    val finalRelationEndDaysNumber: Int? = null,

    /** codice dello stato del progetto formativo */
    @SerialName("statoPfCod")
    val pfStateCode: String? = null,

    /** descrizione dello stato del progetto formativo */
    @SerialName("pfDes")
    val pfDescription: String? = null,

    /** indica che il progetto formativo è stato accettato dallo studente */
    @SerialName("pfAccStu")
    val studentPfAcceptance: Int? = null,

    /** data di accettazione del progetto formativo da parte dello studente */
    @SerialName("dataPfAccStu")
    val studentPfAcceptanceDate: String? = null,

    /** indica che il progetto formativo è stato accettato dall'azienda */
    @SerialName("pfAccAzienda")
    val companyPfAcceptance: Int? = null,

    /** data di accettazione del progetto formativo da parte dell'azienda */
    @SerialName("dataPfAccAzienda")
    val companyPfAcceptanceDate: String? = null,

    /** accettazione manleva da parte dello studente. */
    @SerialName("accManleva")
    val liabilityWaiverAcceptance: Int? = null,

    /** data di accettazione della manleva da parte dello studente. */
    @SerialName("dataAccManleva")
    val indemnityAcceptanceDate: String? = null,

    /** cognome del responsabile della firma del progetto formativo */
    @SerialName("firmCognome")
    val signatorySurname: String? = null,

    /** nome del responsabile della firma del progetto formativo */
    @SerialName("firmNome")
    val signatoryName: String? = null,

    /** ruolo del responsabile della firma del progetto formativo */
    @SerialName("firmRuolo")
    val signatoryRole: String? = null,

    /** durata in ore del tirocinio */
    @SerialName("durataOre")
    val durationHours: Long? = null,

    /** durata effettiva in giorni del tirocinio */
    @SerialName("durataEffettiva")
    val effectiveDuration: Long? = null,

    /** durata in mesi del tirocinio */
    @SerialName("durataMesi")
    val durationMonths: Long? = null,

    /** durata in settimane del tirocinio */
    @SerialName("durataSett")
    val durationWeeks: Long? = null,

    /** durata in giorni del tirocinio */
    @SerialName("durataGiorni")
    val durationDays: Long? = null,

    /** numero di giorni alla settimana dedicati allo stage */
    @SerialName("numGiorniTiroSett")
    val internshipWeeklyDaysNumber: Long? = null,

    /** codice della fascia di addetti */
    @SerialName("fasciaAddettiStageCod")
    val stageStaffBandCode: String? = null,

    /** descrizione della fascia di addetti */
    @SerialName("fasciaDes")
    val bandDescription: String? = null,

    /** numero max stagisti consentiti */
    @SerialName("numMaxStagisti")
    val maxInternsNumber: Long? = null,

    /** numero esatto di dipendenti/addetti in azienda nel momento dell´attivazione dello stage */
    @SerialName("numAddetti")
    val staffNumber: Long? = null,

    /** numero di tirocinanti in azienda nel momento dell´attivazione del progetto formativo dello studente */
    @SerialName("numTirocinanti")
    val internNumber: Long? = null,

    /** numero di tirocinanti extracurriculari in azienda nel momento dell´attivazione del progetto formativo dello studente */
    @SerialName("numTirocinantiExtcurr")
    val extraCurricularInternNumber: Long? = null,

    /** numero di tirocinanti curriculari seguiti dal tutor */
    @SerialName("numTiroTutCurr")
    val currentTutoredInternshipNumber: Long? = null,

    /** numero di tirocinanti extracurriculari seguiti dal tutor */
    @SerialName("numTiroTutExtcurr")
    val extraCurricularTutoredInternshipNumber: Long? = null,

    /** cognome del contatto amministrativo dell'azienda */
    @SerialName("contattiAmmCognome")
    val adminContactsSurname: String? = null,

    /** nome del contatto amministrativo dell'azienda */
    @SerialName("contattiAmmNome")
    val adminContactsName: String? = null,

    /** id della tipologia di inserimento lavorativo dello studente */
    @SerialName("tipiInsLavId")
    val workInsertionTypesId: Long? = null,

    /** descrizione della tipologia di inserimento lavorativo dello studente */
    @SerialName("insLavDes")
    val workInsertionDescription: String? = null,

    /** descrizione delle attività svolte durante il tirocinio */
    @SerialName("attSvolteDes")
    val activitiesCarriedOutDescription: String? = null,

    /** descrizione delle competenze acquisite durante il tirocinio */
    @SerialName("competenzeAcqDes")
    val acquiredSkillsDescription: String? = null,

    /** contenuti della formazione generale */
    @SerialName("contFormGen")
    val generalTrainingContent: String? = null,

    /** contenuti della formazione specialistica */
    @SerialName("contFormSpec")
    val specificTrainingContent: String? = null,

    /** contenuti della formazione */
    @SerialName("contFormazione")
    val trainingContent: String? = null,

    /** facilitazioni previste dal soggetto ospitante. Per esempio rimborso spese, buono pasto, ecc */
    @SerialName("facilitazioni")
    val facilitations: String? = null,

    /** obiettivi formativi del tirocinio */
    @SerialName("obiettFormDes")
    val trainingObjectiveDescription: String? = null,

    /** competenze attese */
    @SerialName("compAttese")
    val pendingComponents: String? = null,

    /** modalità di verifica dell'apprendimento */
    @SerialName("modVerifApprend")
    val learningVerificationMode: String? = null,

    /** numero di ore di formazione generale */
    @SerialName("numOreFormGen")
    val generalTrainingHoursNumber: Long? = null,

    /** numero di ore di formazione specifica */
    @SerialName("numOreFormSpec")
    val specificTrainingHoursNumber: Long? = null,

    /** obiettivi formativi proposti dal tutor accademico */
    @SerialName("obiettFormDesTut")
    val trainingObjectiveDescriptionTutor: String? = null,

    /** descrizione delle attività svolte nel corso del tirocinio proposte dal tutor accademico */
    @SerialName("attSvolteDesTut")
    val activitiesCarriedOutDescriptionTutor: String? = null,

    /** competenze attese proposte dal tutor accademico */
    @SerialName("compAtteseTut")
    val pendingComponentsTutor: String? = null,

    /** competenze acquisite da parte dello studente nel corso del tirocinio proposte dal tutor accademico */
    @SerialName("competenzeAcqDesTut")
    val acquiredSkillsDescriptionTutor: String? = null,

    /** contenuti della formazione generale proposti dal tutor accademico */
    @SerialName("contFormGenTut")
    val generalTrainingContentTutor: String? = null,

    /** contenuti della formazione specialistica proposti dal tutor accademico */
    @SerialName("contFormSpecTut")
    val specificTrainingContentTutor: String? = null,

    /** contenuti della formazione proposti dal tutor accademico */
    @SerialName("contFormazioneTut")
    val trainingContentTutor: String? = null,

    /** modalità di verifica dell'apprendimento proposta dal tutor accademico */
    @SerialName("modVerifApprendTut")
    val learningVerificationModeTutor: String? = null
)

@Serializable
data class Esse3CompanyPostOutput(
    /** ID azienda creata */
    @SerialName("enteId")
    val entityId: Long? = null,

    /** ID struttura didattica dell'azienda creata */
    @SerialName("sdrId")
    val siteId: Long? = null
)

@Serializable
data class Esse3InternshipApplicationHeader(
    /** id della domanda di tirocinio */
    @SerialName("domTiroId")
    val domicileInternshipId: Long? = null,

    /** id della carriera dello studente */
    @SerialName("stuId")
    val studentId: Long = 0L,

    /** progressivo della domanda di tirocinio */
    @SerialName("domTiroPrg")
    val domicileInternshipProgram: Long? = null,

    /** anno accademico della domanda di tirocinio */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** anno accademico della domanda nel formato YYYY/YYYY+1 */
    @SerialName("desAa")
    val academicYearDescription: String? = null,

    /** id del corso di studio di iscrizione dello studente */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** anno accademico di iscrizione dello studente */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** id del percorso di studio di iscrizione dello studente */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** tipo di tirocinio */
    @SerialName("tipoTirocCod")
    val internshipTypeCode: String? = null,

    /** descrizione del tipo di tirocinio */
    @SerialName("tipoTirocDes")
    val internshipTypeDescription: String? = null,

    /** descrizione estesa del tipo di tirocinio */
    @SerialName("desEstesa")
    val extendedDescription: String? = null,

    /** stato domanda di tirocinio (PRE => Presentata, CHI => Chiusa, ANN => Annullata, CON => Confermata, RIF => Rifiutata, AVV => Avviato, NAS => Non assegnato) */
    @SerialName("statoDomTiroCod")
    val internshipApplicationStateCode: Esse3InternshipApplicationStateCode? = null,

    /** descrizione dello stato del tirocinio */
    @SerialName("statoDomTiroDes")
    val internshipApplicationStateDescription: String? = null,

    /** candidatura visibile in area web aziendale. Si applica solo alle domande Presentate. Quando la domanda è confermata la candidatura è sempre visibile. */
    @SerialName("candVisEnteFlg")
    val candidateEntityViewFlag: Int? = null,

    /** indica se per lo studente è prevista la richiesta di riconoscimento crediti per l´attività di stage. Tale dato è ereditato dalla "tipologia di stage". */
    @SerialName("abilRicCfu")
    val cFURecognitionAuthorization: Int? = null,

    /** id dell'ente */
    @SerialName("enteId")
    val entityId: Long? = null,

    /** descrizione dell'ente */
    @SerialName("enteDes")
    val entityDescription: String? = null,

    /** codice duns di 9 cifre */
    @SerialName("duns")
    val duns: String? = null,

    /** partita IVA ente */
    @SerialName("piva")
    val vatNumber: String? = null,

    /** codice fiscale ente */
    @SerialName("cf")
    val fiscalCode: String? = null,

    /** id della convenzione */
    @SerialName("sdrId")
    val siteId: Long? = null,

    /** numero di Protocollo della convenzione */
    @SerialName("numProtocollo")
    val protocolNumber: String? = null,

    /** descrizione della convenzione */
    @SerialName("cnvzDes")
    val conventionDescription: String? = null,

    /** id della struttura didattica della convenzione */
    @SerialName("sdrCnvzId")
    val conventionSiteId: Long? = null,

    /** titolo opportunità */
    @SerialName("oppTitolo")
    val opportunityTitle: String? = null,

    /** descrizione opportunità */
    @SerialName("oppDes")
    val opportunityDescription: String? = null
)

@Serializable
data class Esse3CompanyData(
    /** id dell'azienda */
    @SerialName("aziendaId")
    val companyId: Long? = null,

    /** codice dell'azienda */
    @SerialName("aziendaCod")
    val companyCode: String? = null,

    /** descrizione dell'azienda */
    @SerialName("aziendaDes")
    val companyDescription: String? = null,

    /** codice fiscale azienda */
    @SerialName("cf")
    val fiscalCode: String? = null,

    /** partita IVA azienda */
    @SerialName("piva")
    val vatNumber: String? = null,

    /** partita IVA di gruppo */
    @SerialName("pivaGruppo")
    val groupVatNumber: String? = null,

    /** codice duns di 9 cifre */
    @SerialName("duns")
    val duns: String? = null,

    /** codice della tipologia dell'azienda */
    @SerialName("tipoAziendaCod")
    val companyTypeCode: String? = null,

    /** descrizione della tipologia dell'azienda */
    @SerialName("tipoAziendaDes")
    val companyTypeDescription: String? = null,

    /** codice del settore dell'azienda */
    @SerialName("settAziendaCod")
    val companySectorCode: String? = null,

    /** descrizione del settore dell'azienda */
    @SerialName("settAziendaDes")
    val companySectorDescription: String? = null,

    /** Indica se l´azienda è privata o pubblica */
    @SerialName("privatoFlg")
    val privateFlag: Int? = null,

    /** url del sito dell'azienda */
    @SerialName("link")
    val link: String? = null,

    /** id della struttura di appartenenza */
    @SerialName("sdrId")
    val siteId: Long? = null,

    /** codice della tipologia di struttura responsabile */
    @SerialName("tipoSdrCod")
    val siteTypeCode: String? = null,

    /** descrizione della tipologia della struttura responsabile */
    @SerialName("tipoSdrDes")
    val siteTypeDescription: String? = null,

    /** codice della struttura responsabile */
    @SerialName("sdrCod")
    val siteCode: String? = null,

    /** descrizione della struttura responsabile */
    @SerialName("sdrDes")
    val siteDescription: String? = null,

    /** codice dello stato azienda */
    @SerialName("statoAziendaCod")
    val companyStateCode: String? = null,

    /** descrizione dello stato azienda */
    @SerialName("statoAziendaDes")
    val companyStateDescription: String? = null,

    /** indica chi ha inserito i dati dell'azienda */
    @SerialName("provenienza")
    val origin: String? = null,

    /** codice della fascia di dipendenti */
    @SerialName("fasciaDipCod")
    val departmentBandCode: String? = null,

    /** descrizione della fascia di dipendenti */
    @SerialName("fasciaDipDes")
    val departmentBandDescription: String? = null,

    /** Indica se l´azienda è un'associazione di categoria o un gruppo */
    @SerialName("assCatFlg")
    val associationCategoryFlag: Int? = null,

    /** id dell'associazione di categoria alla quale l'azienda è affiliata */
    @SerialName("assCatAziendaId")
    val companyCategoryAssociationId: Long? = null,

    /** codice dell'associazione di categoria alla quale l'azienda è affiliata */
    @SerialName("assCatAziendaCod")
    val companyCategoryAssociationCode: String? = null,

    /** descrizione dell'associazione di categoria alla quale l'azienda è affiliata */
    @SerialName("assCatAziendaDes")
    val companyCategoryAssociationDescription: String? = null,

    /** descrizione estesa dell'azienda */
    @SerialName("desEstesa")
    val extendedDescription: String? = null,

    /** Indica se è stata data la liberatoria per la privacy */
    @SerialName("autPrivacyFlg")
    val privacyAuthorizationFlag: Int? = null,

    /** Gruppo di appartenenza */
    @SerialName("gruppoAppart")
    val belongingGroup: String? = null,

    /** Codice associativo */
    @SerialName("codiceAssociativo")
    val associativeCode: String? = null,

    /** Fatturato */
    @SerialName("fatturato")
    val invoiced: String? = null,

    /** Identificativo del settore dell'attività economica */
    @SerialName("settAtecoId")
    val atecoSectorId: Long? = null,

    /** codice del settore dell'attività economica */
    @SerialName("settAtecoCod")
    val atecoSectorCode: String? = null,

    /** descrizione del settore dell'attività economica */
    @SerialName("settAtecoDes")
    val atecoSectorDescription: String? = null,

    /** Codice profilo azienda */
    @SerialName("profiloAziendaId")
    val companyProfileId: Long? = null,

    /** descrizione profilo azienda */
    @SerialName("profiloAziendaDes")
    val companyProfileDescription: String? = null,

    /** id dell'attività economica */
    @SerialName("catAtecoId")
    val atecoCategoryId: Long? = null,

    /** descrizione dell'attività economica */
    @SerialName("catAtecoDes")
    val atecoCategoryDescription: String? = null,

    /** id del profilo dei permessi */
    @SerialName("profiloPermessiId")
    val permissionsProfileId: Long? = null,

    /** descrizione del profilo dei permessi */
    @SerialName("profiloPermessiDes")
    val permissionsProfileDescription: String? = null,

    /** id del profilo permessi padre da cui si eredita */
    @SerialName("profiloPermessiPadreId")
    val permissionsProfileParentId: Long? = null,

    /** Se il flag è ON l'azienda genererà opportunità in evidenza (cioè in cima alla lista nella pagine delle opportunità per gli studenti) */
    @SerialName("oppEvidenzaFlg")
    val opportunityHighlightFlag: Int? = null,

    /** Codice azienda del CRM esterno */
    @SerialName("crmCod")
    val crmCode: String? = null,

    /** associazione imprenditoriale */
    @SerialName("associazioneImprenditoriale")
    val businessAssociation: String? = null,

    /** Codice univoco di vincolo di inizio degli stage. */
    @SerialName("vincIniStageCod")
    val initialStageWinnerCode: String? = null,

    /** descrizione del vincolo di inizio degli stage. */
    @SerialName("vincIniStageDes")
    val initialStageWinnerDescription: String? = null,

    /** Id della risorsa dell´ufficio stage (memorizzato come soggetto esterno PTA) che funge da responsabile della gestione dell´azienda. */
    @SerialName("respPtaId")
    val ptaResponsibleId: Long? = null,

    /** Cognome della risorsa dell´ufficio stage (memorizzato come soggetto esterno PTA) che funge da responsabile della gestione dell´azienda. */
    @SerialName("respPtaCognome")
    val ptaResponsibleSurname: String? = null,

    /** Nome della risorsa dell´ufficio stage (memorizzato come soggetto esterno PTA) che funge da responsabile della gestione dell´azienda. */
    @SerialName("respPtaNome")
    val ptaResponsibleName: String? = null,

    /** Se vale 1 la replica verso CRM è avvenuta con successo */
    @SerialName("crmSyncFlg")
    val crmSyncFlag: Int? = null,

    /** Identificativo della registrazione azienda che ha dato origine all´azienda. */
    @SerialName("regAziendaId")
    val companyRegistrationId: Long? = null,

    /** Annotazioni */
    @SerialName("notaInterna")
    val internalNote: String? = null,

    /** Campo descrittivo in cui caricare i prodotti/servizi/brands gestiti dall´azienda. */
    @SerialName("prodotti")
    val products: String? = null,

    /** Lingue di lavoro usate dall´azienda. */
    @SerialName("lingueLavoro")
    val workLanguages: String? = null,

    /** Lingue di lavoro usate dal gruppo. */
    @SerialName("lingueLavoroGruppo")
    val workLanguagesGroup: String? = null,

    /** Codice della tipologia di selezione aziendale. */
    @SerialName("tipoSelCod")
    val selectionTypeCode: String? = null,

    /** descrizione della tipologia di selezione aziendale. */
    @SerialName("tipoSelDes")
    val selectionTypeDescription: String? = null,

    /** Codice del contratto CCNL. */
    @SerialName("contrAziendaCod")
    val companyContractCode: String? = null,

    /** descrizione del contratto CCNL. */
    @SerialName("contrAziendaDes")
    val companyContractDescription: String? = null,

    /** Indica se l´azienda è in convenzione ESSE3PA. */
    @SerialName("convenzEsse3Pa")
    val conventionEsse3Pa: Int? = null,

    /** Data di inizio della convenzione ESSE3PA */
    @SerialName("dataIniConvenzEsse3Pa")
    val esse3PaConventionStartDate: String? = null,

    /** Data di fine della convenzione ESSE3PA */
    @SerialName("dataFinConvenzEsse3Pa")
    val esse3PaConventionEndDate: String? = null,

    /** Identificativo scheda aggiuntiva dati azienda. */
    @SerialName("schedaAccId")
    val accessCardId: Long? = null,

    /** Codice della scheda aggiuntiva dati azienda. */
    @SerialName("schedaAccCod")
    val accessCardCode: String? = null,

    /** descrizione della scheda aggiuntiva dati azienda. */
    @SerialName("schedaAccDes")
    val accessCardDescription: String? = null,

    /** Annotazione inserita dall'azienda in fase di registrazione. */
    @SerialName("notaAzienda")
    val companyNote: String? = null,

    /** Codice Aeroporto. */
    @SerialName("iataCod")
    val iataCode: String? = null,

    /** Nome dell'Aeroporto */
    @SerialName("nomeAeroporto")
    val airportName: String? = null,

    /** Indica se l´azienda ha una convenzione attiva. */
    @SerialName("hasValidCnvz")
    val hasValidConvention: Int? = null,

    /** Indica se l´azienda ha un'opportunità attiva. */
    @SerialName("hasValidOpportunita")
    val hasValidOpportunity: Int? = null
)

@Serializable
data class Esse3CompanyContactData(
    /** id dell'azienda */
    @SerialName("aziendaId")
    val companyId: Long? = null,

    /** id del contatto */
    @SerialName("contattoAziendaId")
    val companyContactId: Long? = null,

    /** id del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** id del soggetto esterno */
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    /** Appellativo del contatto */
    @SerialName("appellativo")
    val title: String? = null,

    /** Cognome del contatto. */
    @SerialName("cognome")
    val surname: String? = null,

    /** Nome del contatto. */
    @SerialName("nome")
    val name: String? = null,

    /** Sesso del contatto. */
    @SerialName("sesso")
    val gender: String? = null,

    /** Matricola/Codice Fiscale del contatto. */
    @SerialName("matCodfis")
    val matFiscalCode: String? = null,

    /** Email del contatto. */
    @SerialName("email")
    val email: String? = null,

    /** Telefono del contatto. */
    @SerialName("tel")
    val phone: String? = null,

    /** Indica se il contatto è attivo. */
    @SerialName("attivoFlg")
    val activeFlag: Int? = null,

    /** Ruolo del contatto. */
    @SerialName("ruolo")
    val role: String? = null,

    /** Data di nascita Contatto */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** Nazione di nascita del contatto. */
    @SerialName("nazioneNascita")
    val birthNation: String? = null,

    /** Comune di nascita del contatto. */
    @SerialName("comuneNascita")
    val birthMunicipality: String? = null,

    /** Provincia di nascita del contatto. */
    @SerialName("siglaNasc")
    val birthAbbreviation: String? = null,

    /** Matricola del contatto. */
    @SerialName("matricola")
    val matricola: String? = null,

    /** Codice azienda del CRM esterno */
    @SerialName("crmCod")
    val crmCode: String? = null,

    /** Cellulare del contatto. */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** Identificativo Lingua in cui ricevere informazioni. */
    @SerialName("linguaInfoId")
    val languageInfoId: Long? = null,

    /** descrizione della lingua in cui ricevere informazioni. */
    @SerialName("linguaInfoDes")
    val languageInfoDescription: String? = null,

    /** Regione di iscrizione all'albo degli psicologi. Usato solo per i contatti che sono Tutor Psicologi. */
    @SerialName("regioneAlbo")
    val registerRegion: String? = null,

    /** Numero di iscrizione all'albo degli psicologi. Usato solo per i contatti che sono Tutor Psicologi. */
    @SerialName("numIscrAlbo")
    val registerEnrollmentNumber: String? = null,

    /** Data di iscrizione all'albo degli psicologi. Usato solo per i contatti che sono Tutor Psicologi. */
    @SerialName("dataIscrAlbo")
    val enrollmentRegisterDate: String? = null
)

@Serializable
data class Esse3StudentInternshipEligibilityData(
    /** matricola dello studente */
    @SerialName("matricola")
    val matricola: String? = null,

    /** tipo del servizio da controllare */
    @SerialName("tipo_servizio")
    val serviceType: String? = null,

    /** codice dell'esito dell'operazione di controllo */
    @SerialName("esito_cod")
    val outcomeCode: String? = null,

    /** descrizione dell'esito dell'operazione di controllo */
    @SerialName("esito_des")
    val outcomeDescription: String? = null
)

@Serializable
data class Esse3CompanyPutInput(
    @SerialName("cod")
    val code: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("statoEnteCod")
    val entityStateCode: String? = null,

    @SerialName("settAtecoId")
    val atecoSectorId: Long? = null,

    @SerialName("catAtecoId")
    val atecoCategoryId: Long? = null,

    @SerialName("duns")
    val duns: String? = null,

    @SerialName("crmCod")
    val crmCode: String? = null,

    @SerialName("fatturato")
    val invoiced: String? = null,

    @SerialName("link")
    val link: String? = null,

    @SerialName("user")
    val user: String? = null
)

@Serializable
data class Esse3CompanyLocationsData(
    /** id della sede */
    @SerialName("sedeAziendaId")
    val companySiteId: Long? = null,

    /** id dell'azienda */
    @SerialName("aziendaId")
    val companyId: Long? = null,

    /** descrizione della sede. */
    @SerialName("sedeAziendaDes")
    val companySiteDescription: String? = null,

    /** codice tipo sede */
    @SerialName("tipoSedeCod")
    val siteTypeCode: String? = null,

    /** descrizione tipo sede */
    @SerialName("tipiSedeDes")
    val siteTypesDescription: String? = null,

    /** Via dell'indirizzo della sede */
    @SerialName("via")
    val street: String? = null,

    /** Cap dell'indirizzo della sede */
    @SerialName("cap")
    val postalCode: String? = null,

    /** Citta della sede */
    @SerialName("citta")
    val city: String? = null,

    /** Sigla della provincia della sede */
    @SerialName("provSigla")
    val provinceAbbreviation: String? = null,

    /** id della nazione */
    @SerialName("nazioneId")
    val nationId: Long? = null,

    /** descrizione della nazione */
    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    /** numero telefonico sede */
    @SerialName("sedeNumTel")
    val sitePhoneNumber: String? = null,

    /** numero fax sede */
    @SerialName("sedeFax")
    val siteFax: String? = null,

    /** Indirizzo completo della sede */
    @SerialName("indirizzoCompleto")
    val fullAddress: String? = null,

    /** Visibilità in area web studente dell'indirizzo di posta elettronica associato alla sede. */
    @SerialName("emailVisWeb")
    val webVisibleEmail: Int? = null,

    /** Indirizzo di posta elettronica associato alla sede. */
    @SerialName("email")
    val email: String? = null,

    /** Sede disattivata. */
    @SerialName("disattiva")
    val deactivate: Int? = null,

    /** Codice Aeroporto Sede. */
    @SerialName("iataCod")
    val iataCode: String? = null,

    /** Nome dell'Aeroporto della sede */
    @SerialName("nomeAeroporto")
    val airportName: String? = null
)

@Serializable
data class Esse3InternshipApplicationQuestionnaireData(
    /** id della carriera dello studente */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** id della domanda dello studente */
    @SerialName("domTiroId")
    val domicileInternshipId: Long? = null,

    /** matricola dello studente, è il numero assegnato dalle segreterie allo studente per identificarlo nel sistema, una matricola potrebbe non identificare univocamente un libretto collegato ad un tratto di carriera */
    @SerialName("matricola")
    val matricola: String? = null,

    /** cognome dello studente */
    @SerialName("cognomeStu")
    val studentSurname: String? = null,

    /** nome dello studente */
    @SerialName("nomeStu")
    val studentName: String? = null,

    /** tipologia valutazione */
    @SerialName("tipoQuestCod")
    val questionTypeCode: String? = null,

    /** flag che indica il completamento della compilazione */
    @SerialName("completoFlg")
    val completeFlag: Int? = null,

    /** data di inserimento valutazione */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data di fine compilazione valutazione */
    @SerialName("dataFineComp")
    val completionEndDate: String? = null,

    /** codice della tipologia di compilatore della valutazione */
    @SerialName("compCod")
    val componentCode: String? = null,

    /** codice della tipologia del destinatario della valutazione */
    @SerialName("destCod")
    val destinationCode: String? = null,

    /** ordine di visualizzazione del singolo elemento della valutazione */
    @SerialName("ordVis")
    val orderVisible: Int? = null,

    /** id del questionario utilizzato per la valutazione */
    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    /** id della compilazione del questionario */
    @SerialName("questCompId")
    val questionComponentId: Long? = null,

    /** codice del questionario */
    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    /** codice dello stato del questionario */
    @SerialName("statoQuestCod")
    val questionStateCode: String? = null,

    /** descrizione del questionario */
    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    /** Note del questionario */
    @SerialName("questionarioNote")
    val questionnaireNote: String? = null,

    /** codice del contesto del questionario */
    @SerialName("questContCod")
    val questionContentCode: String? = null,

    /** descrizione del contesto del questionario */
    @SerialName("questContDes")
    val questionContentDescription: String? = null,

    /** data di inserimento questionario */
    @SerialName("questDataIns")
    val questionInsertionDate: String? = null,

    /** data di modifica questionario */
    @SerialName("questDataMod")
    val questionModificationDate: String? = null,

    /** id della domanda */
    @SerialName("quesitoId")
    val questionId: Long? = null,

    /** codice dell'elemento */
    @SerialName("elemCod")
    val elementCode: String? = null,

    /** id della domanda padre */
    @SerialName("parentQuesitoId")
    val parentQuestionId: Long? = null,

    /** codice della tipologia del formato della domanda */
    @SerialName("tipoFormatoCod")
    val formatTypeCode: String? = null,

    /** descrizione della tipologia del formato della domanda */
    @SerialName("tipoFormatoDes")
    val formatTypeDescription: String? = null,

    /** punteggio del quesito */
    @SerialName("quesitoPunteggio")
    val questionScore: Float? = null,

    /** flag che indica se la compilazione è obbligatoria */
    @SerialName("obbligatorioFlg")
    val mandatoryFlag: Int? = null,

    /** note relative alle domande */
    @SerialName("quesitoNote")
    val questionNote: String? = null,

    /** codice del tag */
    @SerialName("tagCod")
    val tagCode: String? = null,

    /** codice della categoria */
    @SerialName("categCod")
    val categoryCode: String? = null,

    /** descrizione elemento */
    @SerialName("elemDes")
    val elementDescription: String? = null,

    /** codice del tipo elemento */
    @SerialName("tipoElemCod")
    val elementTypeCode: String? = null,

    /** nota relativa all'elemento */
    @SerialName("elemNota")
    val elementNote: String? = null,

    /** codice del contesto del quesito */
    @SerialName("quesitoContCod")
    val questionItemContentCode: String? = null,

    /** id della risposta */
    @SerialName("rispostaId")
    val answerId: Long? = null,

    /** testo libero */
    @SerialName("testoLibero")
    val freeText: String? = null,

    /** punteggio della risposta */
    @SerialName("rispostaPunteggio")
    val answerScore: Float? = null,

    /** data di inserimento questionario */
    @SerialName("rispostaDataIns")
    val answerInsertionDate: String? = null,

    /** data di modifica questionario */
    @SerialName("rispostaDataMod")
    val answerModificationDate: String? = null
)

@Serializable
data class Esse3CompanyPostInput(
    @SerialName("tipoEnteCod")
    val entityTypeCode: String? = null,

    @SerialName("provenienza")
    val origin: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("statoEnteCod")
    val entityStateCode: String? = null,

    @SerialName("settAtecoId")
    val atecoSectorId: Long? = null,

    @SerialName("catAtecoId")
    val atecoCategoryId: Long? = null,

    @SerialName("duns")
    val duns: String? = null,

    @SerialName("crmCod")
    val crmCode: String? = null,

    @SerialName("fatturato")
    val invoiced: String? = null,

    @SerialName("link")
    val link: String? = null,

    @SerialName("user")
    val user: String? = null,

    @SerialName("cod")
    val code: String? = null
)

@Serializable
data class Esse3TrainingProject(
    /** Id del progetto formativo su AlmaLaurea */
    @SerialName("idTirocinio")
    val internshipId: Long? = null,

    /** Chiave identificativa della carriera studente */
    @SerialName("idCarriera")
    val careerId: Int? = null,

    /** Codifica AlmaLaurea del tipo tirocinio */
    @SerialName("idTipoTirocinio")
    val internshipTypeId: Int? = null,

    /** Tipo tirocinio */
    @SerialName("tipoTirocinioDesc")
    val internshipDescriptionType: String? = null,

    /** Codice fiscale tirocinante */
    @SerialName("codiceFiscale")
    val fiscalCode: String? = null,

    /** Numero di CFU associati al titolo. */
    @SerialName("cfu")
    val credits: Float? = null,

    /** Data inizio progetto formativo */
    @SerialName("dataInizioProgettoFormativo")
    val trainingProjectStartDate: String? = null,

    /** Data fine progetto formativo */
    @SerialName("dataFineProgettoFormativo")
    val trainingProjectEndDate: String? = null,

    /** Matricola studente */
    @SerialName("matricola")
    val matricola: String? = null,

    /** Nome tirocinante */
    @SerialName("nome")
    val name: String? = null,

    /** Cognome tirocinante */
    @SerialName("cognome")
    val surname: String? = null,

    /** Codifica titolo di studio */
    @SerialName("titoloDiStudioLauDip")
    val degreeGraduationDepartment: Int? = null,

    /** Descrizione titolo di studio */
    @SerialName("titoloDiStudioLauDipDesc")
    val degreeGraduationDepartmentDescription: String? = null,

    /** Classe di laurea aggregata */
    @SerialName("titoloDiStudioClasseAggr")
    val degreeClassAggregate: String? = null,

    /** Codice ministeriale classe laurea */
    @SerialName("titoloDiStudioClasseCodMinisteriale")
    val ministerialDegreeClassCode: String? = null,

    /** Descrizione classe laurea */
    @SerialName("titoloDiStudioClasseDesc")
    val degreeClassDescription: String? = null,

    /** Corso di laurea */
    @SerialName("titoloDiStudioCorso")
    val degreeCourseTitle: String? = null,

    /** Codice interno titolo di studio */
    @SerialName("titoloDiStudioCodInterno")
    val internalDegreeCode: String? = null,

    /** Codice MUR del corso */
    @SerialName("codicione")
    val bigCode: String? = null,

    /** Codice facoltà */
    @SerialName("idFacolta")
    val facultyId: Int? = null,

    /** Nome facoltà */
    @SerialName("facolta")
    val faculty: String? = null,

    /** Anno di corso dello studente */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** Azienda ospitante */
    @SerialName("nomeAzienda")
    val companyName: String? = null,

    /** Partita IVA azienda */
    @SerialName("partitaIvaAzienda")
    val companyVatNumber: String? = null,

    /** Codice fiscale azienda */
    @SerialName("codFiscAzienda")
    val companyFiscalCode: String? = null,

    /** Settore aziendale */
    @SerialName("settoreAziendale")
    val businessSector: String? = null,

    /** Codice ATECO */
    @SerialName("codiceAteco")
    val atecoCode: String? = null,

    /** Descrizione codice ATECO */
    @SerialName("codiceAtecoDesc")
    val atecoCodeDescription: String? = null,

    /** Reparto/ufficio tirocinio */
    @SerialName("stabilimentoRepartoUfficio")
    val plantDepartmentOffice: String? = null,

    /** Stato sede legale */
    @SerialName("sedeLegaleStato")
    val legalSeatState: Int? = null,

    /** Descrizione stato sede legale */
    @SerialName("sedeLegaleStatoDesc")
    val legalSeatStateDescription: String? = null,

    /** Provincia sede legale */
    @SerialName("sedeLegaleProv")
    val legalSeatProvince: Int? = null,

    /** Provincia sede legale */
    @SerialName("sedeLegaleProvDesc")
    val legalSeatProvinceDescription: String? = null,

    /** Comune sede legale */
    @SerialName("sedeLegaleComuneCodice")
    val legalSeatMunicipalityCode: Int? = null,

    /** Descrizione comune sede legale */
    @SerialName("sedeLegaleComuneDesc")
    val legalSeatMunicipalityDescription: String? = null,

    /** Indirizzo sede legale */
    @SerialName("sedeLegaleIndirizzo")
    val legalSeatAddress: String? = null,

    /** CAP sede legale */
    @SerialName("sedeLegaleCap")
    val legalSeatPostalCode: String? = null,

    /** Stato sede operativa */
    @SerialName("sedeOperativaStato")
    val operationalSeatState: Int? = null,

    /** Descrizione stato sede operativa */
    @SerialName("sedeOperativaStatoDesc")
    val operationalSeatStateDescription: String? = null,

    /** Provincia sede operativa */
    @SerialName("sedeOperativaProv")
    val operationalSeatProvince: Int? = null,

    /** Provincia sede operativa */
    @SerialName("sedeOperativaProvDesc")
    val operationalSeatProvinceDescription: String? = null,

    /** Comune sede operativa */
    @SerialName("sedeOperativaComuneCodice")
    val operationalSeatMunicipalityCode: Int? = null,

    /** Descrizione comune sede operativa */
    @SerialName("sedeOperativaComuneDesc")
    val operationalSeatMunicipalityDescription: String? = null,

    /** Indirizzo sede operativa */
    @SerialName("sedeOperativaIndirizzo")
    val operationalSeatAddress: String? = null,

    /** CAP sede operativa */
    @SerialName("sedeOperativaCap")
    val operationalSeatPostalCode: String? = null,

    /** Nome tutor aziendale */
    @SerialName("tutorAziendaleNome")
    val companyTutorName: String? = null,

    /** Cognome tutor aziendale */
    @SerialName("tutorAziendaleCognome")
    val companyTutorSurname: String? = null,

    /** Email tutor aziendale */
    @SerialName("tutorAziendaleEmail")
    val companyTutorEmail: String? = null,

    /** Ruolo tutor aziendale */
    @SerialName("tutorAziendaleRuolo")
    val companyTutorRole: String? = null,

    /** Competenze tutor aziendale */
    @SerialName("tutorAziendaleCompetenze")
    val companyTutorSkills: String? = null,

    /** Inquadramento tutor aziendale */
    @SerialName("tutorAziendaleInquadramento")
    val companyTutorPosition: String? = null,

    /** Nome tutor accademico */
    @SerialName("tutorAccademicoNome")
    val academicTutorName: String? = null,

    /** Cognome tutor accademico */
    @SerialName("tutorAccademicoCognome")
    val academicTutorSurname: String? = null,

    /** Email tutor accademico */
    @SerialName("tutorAccademicoEmail")
    val academicTutorEmail: String? = null,

    /** Dipartimento tutor accademico */
    @SerialName("tutorAccademicoDipartimento")
    val academicTutorDepartment: String? = null,

    /** Inquadramento tutor accademico */
    @SerialName("tutorAccademicoInquadramento")
    val academicTutorPosition: String? = null,

    /** Durata del tirocinio */
    @SerialName("tirocinioDurata")
    val internshipDuration: String? = null,

    /** Unità durata tirocinio */
    @SerialName("tirocinioDurataTipo")
    val internshipDurationType: String? = null,

    /** Titolo del tirocinio */
    @SerialName("oggettoTirocinio")
    val internshipObject: String? = null,

    /** Obiettivi formativi */
    @SerialName("obiettiviFormativi")
    val trainingObjectives: String? = null,

    /** Attività previste */
    @SerialName("attivita")
    val activity: String? = null
)

@Serializable
data class Esse3OpportunityData(
    /** titolo dell'opportunità ricercata */
    @SerialName("titolo")
    val title: String? = null,

    /** azienda */
    @SerialName("azienda")
    val company: String? = null,

    /** data di inizio iscrizione */
    @SerialName("dataIniIscr")
    val enrollmentStartDate: String? = null,

    /** data di fine iscrizione */
    @SerialName("dataFinIscr")
    val enrollmentEndDate: String? = null,

    /** codice tipo tirocinio */
    @SerialName("tipoTirocCod")
    val internshipTypeCode: String? = null,

    /** descrizione tipo tirocinio */
    @SerialName("tipo")
    val type: String? = null,

    /** descrizione pubblica tipo tirocinio */
    @SerialName("tirocDesPub")
    val publicInternshipDescription: String? = null,

    /** descrizione attività che il tirocinante sarà tenuto a svolgere */
    @SerialName("descrizione")
    val description: String? = null,

    /** flag che indica la visibilità web dell'opportunità */
    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    /** flag che indica la presenza o meno di una condizione sql che va verificata in fase di iscrizione al tirocinio */
    @SerialName("reqCodFlg")
    val requiredCodeFlag: Int? = null,

    /** descrizione della sede */
    @SerialName("p06SediEntiEstDes")
    val p06ExternalEntitiesSitesDescription: String? = null,

    /** codice tipo sede */
    @SerialName("tipoSedeCod")
    val siteTypeCode: String? = null,

    /** descrizione tipo sede */
    @SerialName("tipoSedeDes")
    val siteTypeDescription: String? = null,

    /** descrizione nazione */
    @SerialName("p01NaziDes")
    val p01NationDescription: String? = null,

    /** codice provincia */
    @SerialName("p01ComuSigla")
    val p01MunicipalityAbbreviation: String? = null,

    /** descrizione comune */
    @SerialName("p01ComuDes")
    val p01MunicipalityDescription: String? = null,

    /** codice provincia */
    @SerialName("p06SediEntiEstCap")
    val p06ExternalEntitiesSitesPostalCode: String? = null,

    /** città straniera */
    @SerialName("p06SediEntiEstCitstra")
    val p06ExternalEntitiesSitesForeignCity: String? = null,

    /** Via */
    @SerialName("p06SediEntiEstVia")
    val p06ExternalEntitiesSitesStreet: String? = null,

    /** Prefisso Internazionale */
    @SerialName("sedePrefixInternaz")
    val siteInternationalPrefix: String? = null,

    /** numero telefonico sede */
    @SerialName("sedeNumTel")
    val sitePhoneNumber: String? = null,

    /** numero fax sede */
    @SerialName("sedeFax")
    val siteFax: String? = null,

    /** codice della tipologia di settore */
    @SerialName("area")
    val area: String? = null,

    /** codice dell'area geografica */
    @SerialName("areaGeograficaCod")
    val geographicAreaCode: String? = null,

    /** id dell'attività economica dell'ente esterno */
    @SerialName("settore")
    val sector: Long? = null,

    /** indica se filtrare per categorie protette o no */
    @SerialName("categProtetteFlg")
    val protectedCategoriesFlag: Int? = null,

    /** id della nazione */
    @SerialName("nazioneId")
    val nationId: Long? = null,

    /** Identificativo del settore dell'attività economica */
    @SerialName("settAtecoId")
    val atecoSectorId: Long? = null,

    /** id dell'attività economica */
    @SerialName("catAtecoId")
    val atecoCategoryId: Long? = null,

    /** id della campagna */
    @SerialName("campagnaId")
    val campaignId: Long? = null,

    /** id dell'ente esterno */
    @SerialName("enteId")
    val entityId: Long? = null,

    /** data di inizio tirocinio, il formato con cui deve essere definita la data � DD/MM/YYYY */
    @SerialName("dataIniTiro")
    val internshipStartDate: String? = null,

    /** durata in mesi del tirocinio */
    @SerialName("durataMesi")
    val durationMonths: Long? = null
)

@Serializable
data class Esse3CompanyAgreementsData(
    /** Identificativo della convenzione */
    @SerialName("sdrCnvzId")
    val conventionSiteId: Long? = null,

    /** descrizione della convenzione. */
    @SerialName("sdrCnvzDes")
    val conventionSiteDescription: String? = null,

    /** Identificativo della struttura didattica responsabile della convenzione */
    @SerialName("sdrAziendaId")
    val siteCompanyId: Long? = null,

    /** Identificativo dell'azienda responsabile della convenzione */
    @SerialName("aziendaId")
    val companyId: Long? = null,

    /** codice dell'azienda responsabile della convenzione */
    @SerialName("aziendaCod")
    val companyCode: String? = null,

    /** descrizione dell'azienda responsabile della convenzione */
    @SerialName("aziendaDes")
    val companyDescription: String? = null,

    /** codice dello stato dell'azienda responsabile della convenzione */
    @SerialName("statoAziendaCod")
    val companyStateCode: String? = null,

    /** codice dello stato della convenzione */
    @SerialName("statoCnvzCod")
    val conventionStateCode: String? = null,

    /** descrizione dello stato della convenzione */
    @SerialName("statoCnvzDes")
    val conventionStateDescription: String? = null,

    /** Codice del tipo di ruolo della struttura didattica della convenzione (codici di sistema non modificabili dall´utente) */
    @SerialName("ruoloCnvzCod")
    val conventionRoleCode: String? = null,

    /** descrizione del tipo di ruolo della struttura didattica della convenzione (codici di sistema non modificabili dall´utente) */
    @SerialName("ruoloCnvzDes")
    val conventionRoleDescription: String? = null,

    /** Data di inizio convenzione. */
    @SerialName("dataInizio")
    val startDate: String? = null,

    /** Data di fine convenzione. */
    @SerialName("dataFine")
    val endDate: String? = null,

    /** Data di deposito convenzione. */
    @SerialName("dataDeposito")
    val depositDate: String? = null,

    /** Data di invio convenzione. */
    @SerialName("dataInvio")
    val sendingDate: String? = null,

    /** Data di restituzione convenzione. */
    @SerialName("dataRestituzione")
    val returnDate: String? = null,

    /** Note e restrizioni della convenzione */
    @SerialName("note")
    val notes: String? = null,

    /** Identificativo del gruppo di livelli */
    @SerialName("grpId")
    val groupId: Long? = null,

    /** Codice del livello della struttura didattica della convenzione */
    @SerialName("livCod")
    val levelCode: String? = null,

    /** descrizione del livello della struttura didattica della convenzione */
    @SerialName("livDes")
    val levelDescription: String? = null,

    /** Descrizione del gruppo di livelli */
    @SerialName("grpLivDes")
    val groupLevelDescription: String? = null,

    /** Durata in anni della convenzione */
    @SerialName("durataAnni")
    val durationYears: Int? = null,

    /** Indica se per la convenzione c'è il tacito rinnovo. */
    @SerialName("tacitoRinnovoFlg")
    val tacitRenewalFlag: Int? = null,

    /** Numero di protocollo assegnato alla convenzione. */
    @SerialName("numProtocollo")
    val protocolNumber: String? = null,

    /** Indica se è una convenzione quadro. */
    @SerialName("quadroFlg")
    val frameworkFlag: Int? = null,

    /** Anno accademico di validità della convenzione. */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** Testo libero inseribile per la convenzione. */
    @SerialName("testoLiberoCnvz")
    val freeTextConvention: String? = null,

    /** Indirizzo di spedizione per la convenzione. */
    @SerialName("indirizzoSpedizione")
    val shippingAddress: String? = null,

    /** Dice se è la convenzione di default per una certa azienda (ente_est). Utilizzata per gestire le convenzioni fittizie legate a opportunità per le quali non serve la convenzione e per gestire i PF associati a convenzioni uninominali. */
    @SerialName("defaultFlg")
    val defaultFlag: Int? = null,

    /** Identificativo univoco della convenzione da stampare. */
    @SerialName("stampaCnvzId")
    val conventionPrintId: Long? = null,

    /** Codice della stampa della convenzione */
    @SerialName("docCod")
    val documentCode: String? = null,

    /** descrizione della stampa della convenzione */
    @SerialName("des")
    val description: String? = null,

    /** Codice della tipologia di bollo */
    @SerialName("tipoBolloCnvzCod")
    val conventionStampTypeCode: String? = null,

    /** descrizione della tipologia di bollo */
    @SerialName("tipoBolloCnvzDes")
    val conventionStampTypeDescription: String? = null
)
