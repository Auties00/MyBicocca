package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3PostPlanRule(
    /** numero della regola */
    @SerialName("ordNum")
    val orderNumber: Int = 0,

    /** descrizione libera della regola */
    @SerialName("des")
    val description: String = "",

    /** anno di corso della regola */
    @SerialName("annoCorso")
    val courseYear: Int = 0,

    /** anno di corso di anticipo della regola */
    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    /** tipo di unità di misura della regola. BLK=Blocchi, CFU=Crediti */
    @SerialName("unitaMisura")
    val measurementUnit: String = "",

    /** Tipo di regola da creare nel piano, per la validazione vedere la tabella TIPI_SCE */
    @SerialName("tipoRegola")
    val ruleType: String = "",

    /** massimo valore previsto per il vincolo della regola */
    @SerialName("maxUnt")
    val maxTeachingUnit: Int = 0,

    /** minimo valore previsto per il vincolo della regola */
    @SerialName("minUnt")
    val minTeachingUnit: Int = 0,

    /** indica se la regola è opzionale */
    @SerialName("opzFlg")
    val optionalFlag: Int = 0,

    /** indica se la regola è sovrannumeraria */
    @SerialName("sovranFlg")
    val overrideFlag: Int = 0,

    /** modificatore del TAF della regola. Per la validazione vedere la tabella TIPI_AF */
    @SerialName("taf")
    val taf: String? = null,

    /** codeUn dell'ateneo dove verranno erogate le attività didattiche della regola */
    @SerialName("interateCodeUn")
    val integratedUnifiedCode: String? = null
)

@Serializable
data class Esse3PostPhDPlanActivity(
    /** descrizione dell'attività di tipo dottorato */
    @SerialName("desAd")
    val teachingActivityDescription: String = "",

    /** tipo di insegnamento collegato alla attività figlia di regole di dottorato, la validazione avviene sulla tabella TIPI_INS per le righe con dottorato_flg =1 */
    @SerialName("tipoInsCod")
    val insertionTypeCode: String = "",

    /** Codice catastale della nazione valida alla presentazione del piano */
    @SerialName("codCatNazione")
    val categoryCountryCode: String? = null,

    /** data di partenza dell'attività, obbligatoria per le attività all'estero */
    @SerialName("dataPartenzaPrev")
    val expectedDepartureDate: String? = null,

    /** data di arrivo dell'attività, obbligatoria per le attività all'estero */
    @SerialName("dataArrivoPrev")
    val expectedArrivalDate: String? = null,

    /** Soggetto che eroga l'attività */
    @SerialName("soggettoErogante")
    val providerSubject: String? = null,

    /** destinazione prevista */
    @SerialName("destinazione")
    val destination: String? = null,

    /** link ad eventuali pubblicazioni, previsto solo per le attività di ricerca. I link sono inseriti uno per riga */
    @SerialName("linkPubblicazioni")
    val publicationsLink: List<String> = emptyList()
)

@Serializable
data class Esse3PutInteruniversityPlanOptionalLocation(
    /** indica se attuare il piano una volta sincronizzato */
    @SerialName("attuaPiano")
    val implementPlan: Boolean? = null,

    @SerialName("attivita")
    val activity: List<Esse3PostPlanActivity> = emptyList(),

    @SerialName("pianiInterateneo")
    val interuniversityPlans: List<Esse3PostInteruniversityPlanPortion> = emptyList(),

    @SerialName("attivitaInterateneo")
    val interUniversityActivities: List<Esse3PostInteruniversityPlanActivity> = emptyList(),

    @SerialName("segmentiInterateneo")
    val interuniversitySegments: List<Esse3PostInteruniversityPlanSegment> = emptyList()
)

@Serializable
data class Esse3EntityToUpdate(
    /** tipo di entità da aggiornare */
    @SerialName("entita")
    val entity: String? = null,

    /** id del piano interateneo */
    @SerialName("interateId")
    val integratedId: Int? = null,

    /** id dell'attività del piano (se previsto) */
    @SerialName("itmId")
    val itemId: Int? = null,

    /** stato del piano interateneo */
    @SerialName("staPianoInterateCod")
    val integratedStudyPlanStatusCode: String? = null,

    /** stato dell'attività interateneo (se previsto) */
    @SerialName("staAdInterateCod")
    val integratedTeachingActivityStatusCode: String? = null,

    /** dettaglio dello stato dell'attività interateneo (se previsto) */
    @SerialName("dettstaAdInterateCod")
    val integratedTeachingActivityStatusDetailCode: String? = null,

    /** messaggio di dettaglio */
    @SerialName("msg")
    val message: String? = null
)

@Serializable
data class Esse3PostInteruniversityPlanPortion(
    /** code_un dell'ateneo sede del tratto tratto di piano */
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String = "",

    /** stato del tratto di piano, indica lo stato relativamente alla sincronizzazione con la sede amministrativa */
    @SerialName("staPianoInterateCod")
    val integratedStudyPlanStatusCode: String = "",

    /** id del piano nell'ateneo della ' */
    @SerialName("sedeOpPianoId")
    val operationalSitePlanId: Int = 0,

    /** stato del piano sulla sede definita nel tratto */
    @SerialName("sedeOpStaPianoCod")
    val operationalSitePlanStatusCode: String? = null,

    /** indica la valutazione che deve essere fatta nella sede del tratto rispetto al piano completo; 0 - nessuna approvazione, 1 - approvazione su altro tratto, 2 - approvazione su questo tratto */
    @SerialName("sedeOpValutazione")
    val operationalSiteEvaluation: Int = 0,

    /** messaggio sincronizzazione */
    @SerialName("msgSync")
    val syncMessage: String? = null,

    /** dettaglio messaggio sincronizzazione */
    @SerialName("msgSyncDett")
    val syncMessageDetail: String? = null,

    /** user controllo cognome */
    @SerialName("userControlloCognome")
    val controlUserSurname: String? = null,

    /** user controllo nome */
    @SerialName("userControllonome")
    val controlUserName: String? = null,

    /** user controllo matricola */
    @SerialName("userControlloMatricola")
    val controlUserMatricola: String? = null,

    /** nota controllo */
    @SerialName("notaControllo")
    val controlNote: String? = null,

    /** user valutatore cognome */
    @SerialName("userValutatoreCognome")
    val evaluatorUserSurname: String? = null,

    /** user valutatore nome */
    @SerialName("userValutatorenome")
    val evaluatorUserName: String? = null,

    /** user valutatore matricola */
    @SerialName("userValutatoreMatricola")
    val evaluatorUserMatricola: String? = null,

    /** nota valutatore */
    @SerialName("notaValutatore")
    val evaluatorNote: String? = null,

    /** id del tratto di piano interateneo */
    @SerialName("interateId")
    val integratedId: Int = 0,

    @SerialName("regoleInterateneo")
    val interuniversityRules: List<Esse3PostInteruniversityPlanPortionRule> = emptyList()
)

@Serializable
data class Esse3CourseOrConditionFilter(
    /** lista delle condizioni in AND da applicare alla condizione in OR */
    @SerialName("andConditions")
    val termsAndConditions: List<Esse3CourseAndConditionFilter> = emptyList()
)

@Serializable
data class Esse3InteruniversityPlanSegmentWithDetails(
    /** code_un dell'ateneo dove l'attività è offerta */
    @SerialName("sedeOffertaCodeUn")
    val offerSiteUnifiedCode: String = "",

    /** code_un dell'ateneo dove l'attività deve essere sostenuta */
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String? = null,

    /** tipo di segmento, indica se questo deriva dall'offerta se è relativo ad una AD di completamento oppure di intergrazione */
    @SerialName("tipoSegInterateCod")
    val integratedSegmentTypeCode: String = "",

    /** codice UD del segmento nell'ateneo di offerta */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** descrizione UD del segmento nell'ateneo di offerta */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    /** codice tipo credito del segmento nell'ateneo di offerta */
    @SerialName("tipoCreCod")
    val creditTypeCode: String? = null,

    /** descrizione tipo credito del segmento nell'ateneo di offerta */
    @SerialName("tipoCreDes")
    val creditTypeDescription: String? = null,

    /** codice settore del segmento nell'ateneo di offerta */
    @SerialName("settCod")
    val sectorCode: String? = null,

    /** descrizione settore del segmento nell'ateneo di offerta */
    @SerialName("settDes")
    val sectorDescription: String? = null,

    /** id dell'ambito del segmento nell'ateneo di offerta dell'attività */
    @SerialName("ambId")
    val environmentId: Long? = null,

    /** descrizione dell'ambito del segmento nell'ateneo di offerta */
    @SerialName("ambDes")
    val environmentDescription: String? = null,

    /** codice  del TAF del segmento nell'ateneo di offerta */
    @SerialName("tipoAfCod")
    val teachingActivityTypeCode: String? = null,

    /** descrizione del TAF del segmento nell'ateneo di offerta */
    @SerialName("tipoAfDes")
    val teachingActivityTypeDescription: String? = null,

    /** indica se il segmento ha frequenza obbligatoria */
    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int? = null,

    /** ore minime di frequenza previste */
    @SerialName("oreMinFreq")
    val minimumAttendanceHours: Float? = null,

    /** peso del segmento */
    @SerialName("peso")
    val weight: Float = 0f,

    /** peso convalidato nel caso di riconoscimento o convalida */
    @SerialName("pesoConv")
    val conversionWeight: Float? = null,

    /** durata in ore di lezione previste per il segmento */
    @SerialName("durata")
    val duration: Float? = null,

    /** durata in ore di studio individuale  previste per il segmento */
    @SerialName("durataStudioIndividuale")
    val individualStudyDuration: Float? = null,

    /** id della carriera dello studente */
    @SerialName("stuId")
    val studentId: Long = 0L,

    /** progressivo del piano di studio per la carriera dello studente */
    @SerialName("pianoId")
    val planId: Int = 0,

    /** id della riga del piano alla quale si collegano le informazioni dell'interateneo */
    @SerialName("itmId")
    val itemId: Int = 0,

    /** id del modulo nell'ateneo di offerta dell'attività */
    @SerialName("udId")
    val teachingUnitId: Long = 0L,

    /** id del segmento nell'ateneo di offerta dell'attività */
    @SerialName("segId")
    val segmentId: Long = 0L
)

@Serializable
data class Esse3InteruniversityPlanActivityResult(
    /** data di acquisizione della frequenza nell'ateneo di sostenimento dell'attività */
    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    /** anno di frequenza dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    /** data di superamento nell'ateneo di sostenimento dell'attività */
    @SerialName("dataSup")
    val supDate: String? = null,

    /** anno di superamento dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("aaSupId")
    val academicYearSupervisorId: Int? = null,

    /** modalità di valutazione dell'attività */
    @SerialName("modValCod")
    val evaluationModeCode: String? = null,

    /** voto assegnato all'attività */
    @SerialName("voto")
    val grade: Float? = null,

    /** lode */
    @SerialName("lode")
    val cumLaude: Int? = null,

    /** codice tipo di giudizio */
    @SerialName("tipoGiudCod")
    val judgmentTypeCode: String? = null,

    /** descrizione tipo di giudizio */
    @SerialName("tipoGiudDes")
    val judgmentTypeDescription: String? = null,

    @SerialName("ricId")
    val searchId: Int = 0,

    /** tipo di riconoscimento nel caso di riconoscimento nella sede operativa, valido solo se ric_id IN (1,2) */
    @SerialName("tipoRicCod")
    val requestTypeCode: String? = null,

    /** livello della lingua */
    @SerialName("livelloLinguaCod")
    val languageLevelCode: String? = null,

    /** codice della lingua in iso6392 */
    @SerialName("linguaCod")
    val languageCode: String? = null,

    /** codice tipo svoltigemnto esame */
    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null
)

@Serializable
data class Esse3StudyPlanActivity(
    /** id della carriera dello studente */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** progressivo del piano di studio per la carriera dello studente */
    @SerialName("pianoId")
    val planId: Int? = null,

    /** progressivo della regola a cui fa riferimento l'attività interno del piano di studio */
    @SerialName("scePianoId")
    val choicePlanId: Int? = null,

    /** progressivo dell'attività all'interno del piano di studio */
    @SerialName("itmId")
    val itemId: Int? = null,

    /** id del tratto di carriera a cui si riferisce il piano di studio */
    @SerialName("matId")
    val matId: Long? = null,

    /** id dello schema di piano utilizzato per la compilazione del piano standard */
    @SerialName("schemaId")
    val schemaId: Long? = null,

    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** id univoco dell'attività dalle regole di scelta scelta durante la compilazione del piano */
    @SerialName("sceltaAdId")
    val teachingActivityChoiceId: Int? = null,

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    /** id univoco dell'attività del libretto. Viene valorizzato se l'attività deriva da tesoretto oppure è stata preselezionata durante la compilazione del piano */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** id univoco dell'attività del libretto. viene valorizzato quando il piano risulta approvato e attuato ed la riga di piano è collegata al libretto */
    @SerialName("adsceAttId")
    val choiceActivityId: Long? = null,

    /** per le regole da gruppo e da gruppo libero da OD, indica se le attività sono state selezionate (1) oppure no (0), per tutte le altre regole vale 1 */
    @SerialName("sceltaFlg")
    val choiceFlag: Int? = null,

    /** indica se l'attività didattica è derivante da tesoretto */
    @SerialName("tesorettoFlg")
    val treasureFlag: Int? = null,

    /** riferimento al padre del raggruppamento nel caso di raggruppamenti. il padre del raggruppamento ha ragId=itmId i figli invece puntano all'itmId del padre */
    @SerialName("ragId")
    val groupId: Int? = null,

    /** id univoco le raggruppamento presente in offerta, viene valorizzato solo nel padre del raggruppamento */
    @SerialName("adragoffId")
    val activityRaggruppamentoOfferId: Long? = null,

    /** codice del tipo di raggruppamento */
    @SerialName("tipoRagCod")
    val groupingTypeCode: String? = null,

    /** codice dell'attività didattica presente nel libretto dello studente */
    @SerialName("adLibCod")
    val activityTranscriptCode: String? = null,

    /** descrizione dell'attività didattica presente nel libretto dello studente */
    @SerialName("adLibDes")
    val activityTranscriptDescription: String? = null,

    /** peso effettivo dell'attività didattica dello studente */
    @SerialName("peso")
    val weight: Float? = null,

    /** peso dell'attività didattica che viene visualizzato durante la compilazione del piano */
    @SerialName("pesoAdVis")
    val teachingActivityVisibleWeight: Float? = null,

    /** codice iso_6352 della lingua prevista nel blocco di scelta che contiene l'attività selezionata */
    @SerialName("linguaSceCod")
    val chosenLanguageCode: String? = null,

    /** numero della lingua previsto nel blocco di scelta che contiene l'attività selezionata */
    @SerialName("linguaSceNum")
    val chosenLanguageNumber: Long? = null,

    @SerialName("udSelezionate")
    val selectedTeachingUnits: List<Esse3StudyPlanTeachingUnit> = emptyList(),

    /** indica se l'attività è conteggiabile per quanto riguarda il controllo di posti durante la compilazione del piano */
    @SerialName("conteggiabileFlg")
    val countableFlag: Int? = null,

    /** indica se l'attività deve essere inserita a libretto tramite delibera */
    @SerialName("deliberaFlg")
    val resolutionFlag: Int? = null,

    /** Stato della attività relativamente all`attuazione -1 => Attività relativa a delibera ANNULLATA, in questo caso l`AD non viene mai attuata; 0 => attività NON attuabile, viene usata per le attività che hanno una delibera pendente o in tutti quei casi in cui l'attività non deve essere attauta; 1 => attività Attuabile, indica la normale attività del piano viene presa in considerazione dall''attuazione ed attuata secondo le normali logiche; */
    @SerialName("statoAttuazione")
    val implementationState: Int? = null,

    /** codice semestre di effettiva erogazione scelto dallo studente in fase di compilazione del piano (se la regola nelle regole di scelta ha assegnazionePostiFlg=1) */
    @SerialName("partEffCod")
    val effectivePartialCode: String? = null,

    /** id della lingua di erogazione della didattica scelta dallo studente in fase di compilazione del piano (se la regola nelle regole di scelta ha assegnazionePostiFlg=1) */
    @SerialName("linguaDidId")
    val teachingLanguageId: String? = null,

    /** codice della lingua di erogazione della didattica scelta dallo studente in fase di compilazione del piano (se la regola nelle regole di scelta ha assegnazionePostiFlg=1) */
    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    /** descrizione della lingua di erogazione della didattica scelta dallo studente in fase di compilazione del piano (se la regola nelle regole di scelta ha assegnazionePostiFlg=1) */
    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    /** codice del tipo di erogazione della didattica scelta dallo studente in fase di compilazione del piano (se la regola nelle regole di scelta ha assegnazionePostiFlg=1) */
    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    /** descrizione del tipo di erogazione della didattica scelta dallo studente in fase di compilazione del piano (se la regola nelle regole di scelta ha assegnazionePostiFlg=1) */
    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    /** codice del gruppo di logistica scelto dallo studente in fase di compilazione del piano (se la regola nelle regole di scelta ha assegnazionePostiFlg=1) */
    @SerialName("grpAdlogCod")
    val teachingActivityLogGroupCode: String? = null,

    /** descrizione del gruppo di logistica scelto dallo studente in fase di compilazione del piano (se la regola nelle regole di scelta ha assegnazionePostiFlg=1) */
    @SerialName("grpAdlogDes")
    val teachingActivityLogGroupDescription: String? = null,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey? = null,

    /** anno di corso della regola (zero indica anno di corso generico) */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** anno di corso di anticipo della regola */
    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    @SerialName("infoDottorati")
    val phdInfo: Esse3PhDStudyPlanActivityInfo? = null
)

@Serializable
data class Esse3PutInteruniversityPlanAdmissionLocation(
    /** indica se attuare il piano una volta inserito (valido solo se lo stato è A) */
    @SerialName("attuaFlg")
    val implementationFlag: Boolean? = null,

    /** code_un dell'ateneo sede del tratto di piano */
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String? = null,

    /** nuovo stato del piano da inserire nella sede amministrativa */
    @SerialName("stato")
    val state: String = "",

    /** nota inserita dall'utente approvatore del piano */
    @SerialName("notaControllo")
    val controlNote: String? = null,

    /** cognome dell'utente approvatore del piano */
    @SerialName("userControlloCognome")
    val controlUserSurname: String? = null,

    /** nome dell'utente approvatore del piano */
    @SerialName("userControlloNome")
    val controlUserName: String? = null,

    /** matricola dell'utente approvatore del piano */
    @SerialName("userControlloMatricola")
    val controlUserMatricola: String? = null,

    /** nota inserita dall'utente valutatore del piano */
    @SerialName("notaValutatore")
    val evaluatorNote: String? = null,

    /** cognome dell'utente valutatore del piano */
    @SerialName("userValutatoreCognome")
    val evaluatorUserSurname: String? = null,

    /** nome dell'utente valutatore del piano */
    @SerialName("userValutatoreNome")
    val evaluatorUserName: String? = null,

    /** matricola dell'utente valutatore del piano */
    @SerialName("userValutatoreMatricola")
    val evaluatorUserMatricola: String? = null
)

@Serializable
data class Esse3PostPlanBody(
    /** tipo di piano da inserire, I=Individuale */
    @SerialName("tipo")
    val type: String = "",

    /** stato del piano, A=Approvato, P=Proposto */
    @SerialName("stato")
    val state: String = "",

    /** indica se attuare il piano una volta inserito (valido solo se lo stato è A) */
    @SerialName("attuaFlg")
    val implementationFlag: Boolean = false,

    /** indica se annullare il piano precedente valido prima di inserire il nuovo piano */
    @SerialName("annullaPianoValidoFlg")
    val cancelValidPlanFlag: Boolean = false,

    /** indica come deve essere verificato il piano creato (se con il RAD o L'OFFF), se non valorizzato viene usato OFFF */
    @SerialName("tipoOrigModTaf")
    val tafModificationOriginType: String? = null,

    /** codice del percorso scelto per il piano, se null vale il percorso corrente dello studente */
    @SerialName("pdsSceCod")
    val studyPlanChoiceCode: String? = null,

    /** anno di offerta al quale applicare la condizione sui corsi di studio */
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    /** anno di coorte dello studente */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    /** tipo di approvazione piano */
    @SerialName("tipoRegsce")
    val choiceRegulationType: Int = 0,

    @SerialName("interateneo")
    val interuniversity: Esse3PostInteruniversityPlan? = null,

    /** Lista delle regole da inserire nel piano */
    @SerialName("regole")
    val rules: List<Esse3PostPlanRule> = emptyList(),

    /** Lista delle attività da inserire nel piano */
    @SerialName("attivita")
    val activity: List<Esse3PostPlanActivity> = emptyList()
)

@Serializable
data class Esse3FiltroCdsInterateBody(
    /** anno di offerta al quale applicare la condizione sui corsi di studio */
    @SerialName("aaOffId")
    val academicYearOfferId: Int = 0,

    @SerialName("filtroCds")
    val courseOfStudyFilter: Esse3CourseOrConditionsFilter
)

@Serializable
data class Esse3InteruniversityPlanPortionRule(
    /** code_un dell'ateneo sede del tratto di piano dove le attività della regola devono essere sostenute */
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String = "",

    /** tipo della regola prevista nel tratto del piano */
    @SerialName("tipoRegolaInterateneo")
    val interuniversityRuleType: String = "",

    /** indica se la regola deve essere visualizzata in area studente nell'ateneo collegato al tratto */
    @SerialName("viewStudente")
    val studentView: Int = 0,

    /** indica se la regola deve essere visualizzata in area docente nell'ateneo collegato al tratto */
    @SerialName("viewDocente")
    val lecturerView: Int = 0,

    /** indica se la regola deve essere visualizzata nella maschera dei piani dek client nell'ateneo collegato al tratto */
    @SerialName("viewSegr")
    val secretariatView: Int = 0
)

@Serializable
data class Esse3IntegratedCoursesFilterResponse(
    /** codice del corso di studio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di studio */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** id del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** indica se il cds è a settori */
    @SerialName("cdsSettFlg")
    val courseOfStudySectorFlag: Int? = null,

    /** indica se il cds è ante riforma DM 509/99 */
    @SerialName("cdsRifFlg")
    val courseOfStudyReferenceFlag: Int? = null,

    /** id del corso di studio */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice del dipartimento di afferenza del cds */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** id del dipartimento di afferenza del cds */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** codice del tipo corso di afferenza del cds */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** id del tipo corso di afferenza del cds */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null
)

@Serializable
data class Esse3InteruniversityPlan(
    /** code_un della sede amministrativa nel caso questo piano sia un piano interateneo da inserire nella sede operativa */
    @SerialName("sedeAmmCodeUn")
    val adminSiteUnifiedCode: String = ""
)

@Serializable
data class Esse3PostPlanActivity(
    /** id dell'attività all'interno del piano */
    @SerialName("itmId")
    val itemId: Int = 0,

    /** id dell'attività padre nel caso di AD Raggruppate */
    @SerialName("itmPadreId")
    val parentItemId: Int? = null,

    /** id dell' itm_id del piano che ha generato la convalida parziale, popolato solo nel caso la riga sia relativa alla attività integrativa di una convalida. */
    @SerialName("genConvItmId")
    val generateConventionItemId: Int? = null,

    /** id della regola che contiene l'attività */
    @SerialName("ordNum")
    val orderNumber: Int? = null,

    /** Nel caso non sia valorizzato l'ordNum della regola è richiesto il valore di anno di corso */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** Nel caso non sia valorizzato l'ordNum della regola è possibile valorizzare il valore di anno di corso di anticipo */
    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    /** nel caso non sia valorizzato l'ordNum della regola è possibile indicare se l'attività è sovrannumeraria */
    @SerialName("sovranFlg")
    val overrideFlag: Int? = null,

    /** codice corso di studio di erogazione dell'attività */
    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    /** ordinamento di corso di studio di erogazione dell'attività */
    @SerialName("ordAdId")
    val orderTeachingActivityId: Int? = null,

    /** codice percorso di studio di erogazione dell'attività */
    @SerialName("pdsAdCod")
    val studyPlanTeachingActivityCode: String? = null,

    /** anno di offerta di erogazione dell'attività */
    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    /** codice dell'attività */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** id della riga di libretto che contiene l'attività se questa è già presente a libretto */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** indica se la riga è un tesoretto, se non passato il default è a zero */
    @SerialName("tesorettoFlg")
    val treasureFlag: Int? = null,

    /** tipo di insegnamento collegato alla attività padre di regole di dottorato, la validazione avviene sulla tabella TIPI_INS per le righe con dottorato_flg =1. L'attività deve essere offerta e padre di raggruppamento */
    @SerialName("tipoInsAdPadreDottorato")
    val phdParentTeachingActivityInsertionType: String? = null,

    @SerialName("dottorati")
    val phdPrograms: Esse3PostPhDPlanActivity? = null
)

@Serializable
data class Esse3PostInteruniversityPlanActivity(
    /** code_un dell'ateneo sede del tratto di piano dove l'attività deve essere sostenute */
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String = "",

    /** stato della sincronizzazione dell'attività sulla sede indicata nel sostenimento */
    @SerialName("staAdInterateCod")
    val integratedTeachingActivityStatusCode: String = "",

    /** dettaglio dello stato della sincronizzazione dell'attività sulla sede indicata nel sostenimento */
    @SerialName("dettstaAdInterateCod")
    val integratedTeachingActivityStatusDetailCode: String? = null,

    /** id della riga di libretto prevista nell'ateneo di sostenimento della attività */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** ad_id dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** cds_id di ergoazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("cdsAdId")
    val courseOfStudyTeachingActivityId: Long? = null,

    /** codice del corso di erogazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    /** descrizione del corso di erogazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("cdsAdDes")
    val courseOfStudyTeachingActivityDescription: String? = null,

    /** anno di ordinamento di ergoazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Int? = null,

    /** pds_id di ergoazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("pdsAdId")
    val studyPlanTeachingActivityId: Long? = null,

    /** codice del percorso di erogazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("pdsAdCod")
    val studyPlanTeachingActivityCode: String? = null,

    /** descrizione del percorso di erogazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("pdsAdDes")
    val studyPlanTeachingActivityDescription: String? = null,

    /** anno di offerta di ergoazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    /** punteggio positivo minimo per il superamento dell'attività */
    @SerialName("minPuntiEsa")
    val minGraduationPoints: Int? = null,

    /** punteggio positivo massimo per il superamento dell'attività */
    @SerialName("maxPuntiEsa")
    val maxGraduationPoints: Int? = null,

    /** tipo di azione da effettuare sul riconoscimento, valido solo se ric_id = 2; INTEGR - Integrazione, COMPL - Completamento */
    @SerialName("tipoAzioneRicCod")
    val researchActionTypeCode: String? = null,

    /** id della riga di libretto nel caso di attività intergrativa e/o di completamento */
    @SerialName("adsceIntComplId")
    val choiceInternalComplementaryId: Long? = null,

    /** codice dell'attività integrativa e/o di completamento nel caso di riconoscimenti o convalide nella sede operativa */
    @SerialName("adIntComplCod")
    val internalComplementaryActivityCode: String? = null,

    /** descrizione dell'attività integrativa e/o di completamento nel caso di riconoscimenti o convalide nella sede operativa */
    @SerialName("adIntComplDes")
    val internalComplementaryActivityDescription: String? = null,

    /** nel caso di attività di dottorato il campo viene popolato con il valore posizionale dei flag presenti nella tipi_ins. I valori dei flag sono i seguenti (dottorati_flg,ricerca_flg,missione_fle,periodo_estero_flg,azienda_flg) */
    @SerialName("dottTipoInsFlags")
    val phdEnrollmentTypeFlags: String? = null,

    @SerialName("esito")
    val outcome: Esse3InteruniversityPlanActivityResult? = null,

    @SerialName("esitoIntCompl")
    val integratedCompletionOutcome: Esse3InteruniversityPlanActivityResult? = null,

    /** id della riga del piano alla quale si collegano le informazioni dell'interateneo */
    @SerialName("itmId")
    val itemId: Int = 0,

    /** messaggio sincronizzazione */
    @SerialName("msgSync")
    val syncMessage: String? = null
)

@Serializable
data class Esse3PostInteruniversityPlan(
    /** code_un della sede amministrativa nel caso questo piano sia un piano interateneo da inserire nella sede operativa */
    @SerialName("sedeAmmCodeUn")
    val adminSiteUnifiedCode: String = "",

    @SerialName("pianiInterateneo")
    val interuniversityPlans: List<Esse3PostInteruniversityPlanPortion> = emptyList(),

    @SerialName("attivitaInterateneo")
    val interUniversityActivities: List<Esse3PostInteruniversityPlanActivity> = emptyList(),

    @SerialName("segmentiInterateneo")
    val interuniversitySegments: List<Esse3PostInteruniversityPlanSegment> = emptyList()
)

@Serializable
data class Esse3PhDStudyPlanActivityInfo(
    /** soggetto che eroga l'attività */
    @SerialName("soggettoErogante")
    val providerSubject: String? = null,

    /** luogo di erogazione dell'attività */
    @SerialName("destinazione")
    val destination: String? = null,

    /** data di partenza prevista */
    @SerialName("dataPartenza")
    val departureDate: String? = null,

    /** data di arrivo prevista */
    @SerialName("dataArrivo")
    val arrivalDate: String? = null,

    /** note sull'attività da parte dello studente */
    @SerialName("noteAd")
    val teachingActivityNotes: String? = null,

    /** indica che l'attività richiede una missione */
    @SerialName("missioneFlg")
    val missionFlag: Int? = null,

    /** indica che l'attività è di ricerca */
    @SerialName("ricercaFlg")
    val searchFlag: Int? = null,

    /** descrizione dell'attività inserita dal dottorando */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** codice tipo di insergnamento a cui si riferisce l'attività */
    @SerialName("tipoInsCod")
    val insertionTypeCode: String? = null,

    /** Descrizione tipo di insergnamento a cui si riferisce l'attività */
    @SerialName("tipoInsDes")
    val insertionTypeDescription: String? = null
)

@Serializable
data class Esse3PutIntegratedRecognitionPlanRejectionResponse(
    @SerialName("attivita")
    val activity: Esse3PutInteruniversityRecognitionPlanActivity? = null,

    /** motivo dello scarto */
    @SerialName("errMsg")
    val errorMessage: String? = null,

    /** codice di errore dello scarto */
    @SerialName("errCode")
    val errorCode: Int? = null
)

@Serializable
data class Esse3PostInteruniversityPlanPortionRule(
    /** code_un dell'ateneo sede del tratto di piano dove le attività della regola devono essere sostenute */
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String = "",

    /** tipo della regola prevista nel tratto del piano */
    @SerialName("tipoRegolaInterateneo")
    val interuniversityRuleType: String = "",

    /** indica se la regola deve essere visualizzata in area studente nell'ateneo collegato al tratto */
    @SerialName("viewStudente")
    val studentView: Int = 0,

    /** indica se la regola deve essere visualizzata in area docente nell'ateneo collegato al tratto */
    @SerialName("viewDocente")
    val lecturerView: Int = 0,

    /** indica se la regola deve essere visualizzata nella maschera dei piani dek client nell'ateneo collegato al tratto */
    @SerialName("viewSegr")
    val secretariatView: Int = 0,

    /** ordNum della regola del piano che è prevista per il tratto di piano con sede nell'ateneo del tratto. */
    @SerialName("ordNum")
    val orderNumber: Int = 0
)

@Serializable
data class Esse3StudyPlansStatistics(
    /** Tipo di piano S = Standard I = Individuale */
    @SerialName("tipPiano")
    val planType: String? = null,

    /** Flag che indica se il piano è stato inserito come Statutario. 1 = statutario, 0 non statutarie */
    @SerialName("statuFlg")
    val statusFlag: Int? = null,

    /** tipo di approvazione piano, per i piani standard copia il piano p08_regsce_schemi.tipo_regsce 0 = Piano standard con approvazione automatica 1 = Piano standard senza approvazione automatica 2 = Piano standard con approvazione automatica se il piano è conforme alle regole di percorso 3 = Piano Standard interateneo senza approvazione automatica, il piano è approvabile solo dalla sede amministrativa 4 = Piano Standard interateneo senza approvazione automatica, il piano è approvabile dalla sede amministrativa e dalla ultima sede operativa (quella che eroga l`anno maggiore) */
    @SerialName("tipoRegsce")
    val choiceRegulationType: Int? = null,

    /** numero di piani */
    @SerialName("num")
    val number: Int? = null,

    /** Stato del piano */
    @SerialName("staPianoCod")
    val studyPlanStatusCode: String? = null,

    /** Descrizione dello stato del piano */
    @SerialName("staPianoDes")
    val studyPlanStatusDescription: String? = null
)

@Serializable
data class Esse3InteruniversityPlanActivity(
    /** code_un dell'ateneo sede del tratto di piano dove l'attività deve essere sostenute */
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String = "",

    /** stato della sincronizzazione dell'attività sulla sede indicata nel sostenimento */
    @SerialName("staAdInterateCod")
    val integratedTeachingActivityStatusCode: String = "",

    /** dettaglio dello stato della sincronizzazione dell'attività sulla sede indicata nel sostenimento */
    @SerialName("dettstaAdInterateCod")
    val integratedTeachingActivityStatusDetailCode: String? = null,

    /** id della riga di libretto prevista nell'ateneo di sostenimento della attività */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** ad_id dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** cds_id di ergoazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("cdsAdId")
    val courseOfStudyTeachingActivityId: Long? = null,

    /** codice del corso di erogazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    /** descrizione del corso di erogazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("cdsAdDes")
    val courseOfStudyTeachingActivityDescription: String? = null,

    /** anno di ordinamento di ergoazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Int? = null,

    /** pds_id di ergoazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("pdsAdId")
    val studyPlanTeachingActivityId: Long? = null,

    /** codice del percorso di erogazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("pdsAdCod")
    val studyPlanTeachingActivityCode: String? = null,

    /** descrizione del percorso di erogazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("pdsAdDes")
    val studyPlanTeachingActivityDescription: String? = null,

    /** anno di offerta di ergoazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    /** punteggio positivo minimo per il superamento dell'attività */
    @SerialName("minPuntiEsa")
    val minGraduationPoints: Int? = null,

    /** punteggio positivo massimo per il superamento dell'attività */
    @SerialName("maxPuntiEsa")
    val maxGraduationPoints: Int? = null,

    /** tipo di azione da effettuare sul riconoscimento, valido solo se ric_id = 2; INTEGR - Integrazione, COMPL - Completamento */
    @SerialName("tipoAzioneRicCod")
    val researchActionTypeCode: String? = null,

    /** id della riga di libretto nel caso di attività intergrativa e/o di completamento */
    @SerialName("adsceIntComplId")
    val choiceInternalComplementaryId: Long? = null,

    /** codice dell'attività integrativa e/o di completamento nel caso di riconoscimenti o convalide nella sede operativa */
    @SerialName("adIntComplCod")
    val internalComplementaryActivityCode: String? = null,

    /** descrizione dell'attività integrativa e/o di completamento nel caso di riconoscimenti o convalide nella sede operativa */
    @SerialName("adIntComplDes")
    val internalComplementaryActivityDescription: String? = null,

    /** nel caso di attività di dottorato il campo viene popolato con il valore posizionale dei flag presenti nella tipi_ins. I valori dei flag sono i seguenti (dottorati_flg,ricerca_flg,missione_fle,periodo_estero_flg,azienda_flg) */
    @SerialName("dottTipoInsFlags")
    val phdEnrollmentTypeFlags: String? = null,

    @SerialName("esito")
    val outcome: Esse3InteruniversityPlanActivityResult? = null,

    @SerialName("esitoIntCompl")
    val integratedCompletionOutcome: Esse3InteruniversityPlanActivityResult? = null
)

@Serializable
data class Esse3InteruniversityPlanPortionWithDetails(
    /** code_un dell'ateneo sede del tratto tratto di piano */
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String = "",

    /** stato del tratto di piano, indica lo stato relativamente alla sincronizzazione con la sede amministrativa */
    @SerialName("staPianoInterateCod")
    val integratedStudyPlanStatusCode: String = "",

    /** id del piano nell'ateneo della ' */
    @SerialName("sedeOpPianoId")
    val operationalSitePlanId: Int = 0,

    /** stato del piano sulla sede definita nel tratto */
    @SerialName("sedeOpStaPianoCod")
    val operationalSitePlanStatusCode: String? = null,

    /** indica la valutazione che deve essere fatta nella sede del tratto rispetto al piano completo; 0 - nessuna approvazione, 1 - approvazione su altro tratto, 2 - approvazione su questo tratto */
    @SerialName("sedeOpValutazione")
    val operationalSiteEvaluation: Int = 0,

    /** messaggio sincronizzazione */
    @SerialName("msgSync")
    val syncMessage: String? = null,

    /** dettaglio messaggio sincronizzazione */
    @SerialName("msgSyncDett")
    val syncMessageDetail: String? = null,

    /** user controllo cognome */
    @SerialName("userControlloCognome")
    val controlUserSurname: String? = null,

    /** user controllo nome */
    @SerialName("userControllonome")
    val controlUserName: String? = null,

    /** user controllo matricola */
    @SerialName("userControlloMatricola")
    val controlUserMatricola: String? = null,

    /** nota controllo */
    @SerialName("notaControllo")
    val controlNote: String? = null,

    /** user valutatore cognome */
    @SerialName("userValutatoreCognome")
    val evaluatorUserSurname: String? = null,

    /** user valutatore nome */
    @SerialName("userValutatorenome")
    val evaluatorUserName: String? = null,

    /** user valutatore matricola */
    @SerialName("userValutatoreMatricola")
    val evaluatorUserMatricola: String? = null,

    /** nota valutatore */
    @SerialName("notaValutatore")
    val evaluatorNote: String? = null,

    /** id della carriera dello studente */
    @SerialName("stuId")
    val studentId: Long = 0L,

    /** progressivo del piano di studio per la carriera dello studente */
    @SerialName("pianoId")
    val planId: Int = 0,

    /** id del tratto di piano interateneo */
    @SerialName("interateId")
    val integratedId: Int = 0,

    @SerialName("regoleInterateneo")
    val interuniversityRules: List<Esse3InteruniversityPlanPortionRuleWithDetails> = emptyList()
)

@Serializable
data class Esse3PutInteruniversityPlanOptionalLocationResponse(
    @SerialName("piano")
    val plan: Esse3StudyPlan? = null,

    @SerialName("entitaDaAggiornare")
    val entityToUpdate: List<Esse3EntityToUpdate> = emptyList()
)

@Serializable
data class Esse3InteruniversityPlanSegment(
    /** code_un dell'ateneo dove l'attività è offerta */
    @SerialName("sedeOffertaCodeUn")
    val offerSiteUnifiedCode: String = "",

    /** code_un dell'ateneo dove l'attività deve essere sostenuta */
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String? = null,

    /** tipo di segmento, indica se questo deriva dall'offerta se è relativo ad una AD di completamento oppure di intergrazione */
    @SerialName("tipoSegInterateCod")
    val integratedSegmentTypeCode: String = "",

    /** codice UD del segmento nell'ateneo di offerta */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** descrizione UD del segmento nell'ateneo di offerta */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    /** codice tipo credito del segmento nell'ateneo di offerta */
    @SerialName("tipoCreCod")
    val creditTypeCode: String? = null,

    /** descrizione tipo credito del segmento nell'ateneo di offerta */
    @SerialName("tipoCreDes")
    val creditTypeDescription: String? = null,

    /** codice settore del segmento nell'ateneo di offerta */
    @SerialName("settCod")
    val sectorCode: String? = null,

    /** descrizione settore del segmento nell'ateneo di offerta */
    @SerialName("settDes")
    val sectorDescription: String? = null,

    /** id dell'ambito del segmento nell'ateneo di offerta dell'attività */
    @SerialName("ambId")
    val environmentId: Long? = null,

    /** descrizione dell'ambito del segmento nell'ateneo di offerta */
    @SerialName("ambDes")
    val environmentDescription: String? = null,

    /** codice  del TAF del segmento nell'ateneo di offerta */
    @SerialName("tipoAfCod")
    val teachingActivityTypeCode: String? = null,

    /** descrizione del TAF del segmento nell'ateneo di offerta */
    @SerialName("tipoAfDes")
    val teachingActivityTypeDescription: String? = null,

    /** indica se il segmento ha frequenza obbligatoria */
    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int? = null,

    /** ore minime di frequenza previste */
    @SerialName("oreMinFreq")
    val minimumAttendanceHours: Float? = null,

    /** peso del segmento */
    @SerialName("peso")
    val weight: Float = 0f,

    /** peso convalidato nel caso di riconoscimento o convalida */
    @SerialName("pesoConv")
    val conversionWeight: Float? = null,

    /** durata in ore di lezione previste per il segmento */
    @SerialName("durata")
    val duration: Float? = null,

    /** durata in ore di studio individuale  previste per il segmento */
    @SerialName("durataStudioIndividuale")
    val individualStudyDuration: Float? = null
)

@Serializable
data class Esse3CourseAndConditionFilter(
    /** tipo di filtro da utilizzare, DIP=Codice Dipartimento, TC=Codice Tipo Corso, CDS=Codice Corso di studio */
    @SerialName("filtroCod")
    val filterCode: String = "",

    /** valore del filtro da utilizzare */
    @SerialName("valore")
    val value: String = "",

    /** flag che indica se negare la condizione indicata */
    @SerialName("notFlg")
    val noteFlag: Int = 0
)

@Serializable
data class Esse3InteruniversityPlanActivityWithDetails(
    /** code_un dell'ateneo sede del tratto di piano dove l'attività deve essere sostenute */
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String = "",

    /** stato della sincronizzazione dell'attività sulla sede indicata nel sostenimento */
    @SerialName("staAdInterateCod")
    val integratedTeachingActivityStatusCode: String = "",

    /** dettaglio dello stato della sincronizzazione dell'attività sulla sede indicata nel sostenimento */
    @SerialName("dettstaAdInterateCod")
    val integratedTeachingActivityStatusDetailCode: String? = null,

    /** id della riga di libretto prevista nell'ateneo di sostenimento della attività */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** ad_id dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** cds_id di ergoazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("cdsAdId")
    val courseOfStudyTeachingActivityId: Long? = null,

    /** codice del corso di erogazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    /** descrizione del corso di erogazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("cdsAdDes")
    val courseOfStudyTeachingActivityDescription: String? = null,

    /** anno di ordinamento di ergoazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Int? = null,

    /** pds_id di ergoazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("pdsAdId")
    val studyPlanTeachingActivityId: Long? = null,

    /** codice del percorso di erogazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("pdsAdCod")
    val studyPlanTeachingActivityCode: String? = null,

    /** descrizione del percorso di erogazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("pdsAdDes")
    val studyPlanTeachingActivityDescription: String? = null,

    /** anno di offerta di ergoazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    /** punteggio positivo minimo per il superamento dell'attività */
    @SerialName("minPuntiEsa")
    val minGraduationPoints: Int? = null,

    /** punteggio positivo massimo per il superamento dell'attività */
    @SerialName("maxPuntiEsa")
    val maxGraduationPoints: Int? = null,

    /** tipo di azione da effettuare sul riconoscimento, valido solo se ric_id = 2; INTEGR - Integrazione, COMPL - Completamento */
    @SerialName("tipoAzioneRicCod")
    val researchActionTypeCode: String? = null,

    /** id della riga di libretto nel caso di attività intergrativa e/o di completamento */
    @SerialName("adsceIntComplId")
    val choiceInternalComplementaryId: Long? = null,

    /** codice dell'attività integrativa e/o di completamento nel caso di riconoscimenti o convalide nella sede operativa */
    @SerialName("adIntComplCod")
    val internalComplementaryActivityCode: String? = null,

    /** descrizione dell'attività integrativa e/o di completamento nel caso di riconoscimenti o convalide nella sede operativa */
    @SerialName("adIntComplDes")
    val internalComplementaryActivityDescription: String? = null,

    /** nel caso di attività di dottorato il campo viene popolato con il valore posizionale dei flag presenti nella tipi_ins. I valori dei flag sono i seguenti (dottorati_flg,ricerca_flg,missione_fle,periodo_estero_flg,azienda_flg) */
    @SerialName("dottTipoInsFlags")
    val phdEnrollmentTypeFlags: String? = null,

    @SerialName("esito")
    val outcome: Esse3InteruniversityPlanActivityResult? = null,

    @SerialName("esitoIntCompl")
    val integratedCompletionOutcome: Esse3InteruniversityPlanActivityResult? = null,

    /** id della carriera dello studente */
    @SerialName("stuId")
    val studentId: Long = 0L,

    /** progressivo del piano di studio per la carriera dello studente */
    @SerialName("pianoId")
    val planId: Int = 0,

    /** id della riga del piano alla quale si collegano le informazioni dell'interateneo */
    @SerialName("itmId")
    val itemId: Int = 0
)

@Serializable
data class Esse3PostInteruniversityPlanSegment(
    /** code_un dell'ateneo dove l'attività è offerta */
    @SerialName("sedeOffertaCodeUn")
    val offerSiteUnifiedCode: String = "",

    /** code_un dell'ateneo dove l'attività deve essere sostenuta */
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String? = null,

    /** tipo di segmento, indica se questo deriva dall'offerta se è relativo ad una AD di completamento oppure di intergrazione */
    @SerialName("tipoSegInterateCod")
    val integratedSegmentTypeCode: String = "",

    /** codice UD del segmento nell'ateneo di offerta */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** descrizione UD del segmento nell'ateneo di offerta */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    /** codice tipo credito del segmento nell'ateneo di offerta */
    @SerialName("tipoCreCod")
    val creditTypeCode: String? = null,

    /** descrizione tipo credito del segmento nell'ateneo di offerta */
    @SerialName("tipoCreDes")
    val creditTypeDescription: String? = null,

    /** codice settore del segmento nell'ateneo di offerta */
    @SerialName("settCod")
    val sectorCode: String? = null,

    /** descrizione settore del segmento nell'ateneo di offerta */
    @SerialName("settDes")
    val sectorDescription: String? = null,

    /** id dell'ambito del segmento nell'ateneo di offerta dell'attività */
    @SerialName("ambId")
    val environmentId: Long? = null,

    /** descrizione dell'ambito del segmento nell'ateneo di offerta */
    @SerialName("ambDes")
    val environmentDescription: String? = null,

    /** codice  del TAF del segmento nell'ateneo di offerta */
    @SerialName("tipoAfCod")
    val teachingActivityTypeCode: String? = null,

    /** descrizione del TAF del segmento nell'ateneo di offerta */
    @SerialName("tipoAfDes")
    val teachingActivityTypeDescription: String? = null,

    /** indica se il segmento ha frequenza obbligatoria */
    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int? = null,

    /** ore minime di frequenza previste */
    @SerialName("oreMinFreq")
    val minimumAttendanceHours: Float? = null,

    /** peso del segmento */
    @SerialName("peso")
    val weight: Float = 0f,

    /** peso convalidato nel caso di riconoscimento o convalida */
    @SerialName("pesoConv")
    val conversionWeight: Float? = null,

    /** durata in ore di lezione previste per il segmento */
    @SerialName("durata")
    val duration: Float? = null,

    /** durata in ore di studio individuale  previste per il segmento */
    @SerialName("durataStudioIndividuale")
    val individualStudyDuration: Float? = null,

    /** id della riga del piano alla quale si collegano le informazioni dell'interateneo */
    @SerialName("itmId")
    val itemId: Int = 0,

    /** id del modulo nell'ateneo di offerta dell'attività */
    @SerialName("udId")
    val teachingUnitId: Long? = null,

    /** id del segmento nell'ateneo di offerta dell'attività */
    @SerialName("segId")
    val segmentId: Long? = null
)

@Serializable
data class Esse3PutInteruniversityRecognitionPlanBody(
    /** indica se attuare il piano una volta inserito (valido solo se lo stato è A) */
    @SerialName("attuaFlg")
    val implementationFlag: Boolean = false,

    /** code_un dell'ateneo dove l'attività deve essere sostenuta */
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String = "",

    @SerialName("attivita")
    val activity: List<Esse3PutInteruniversityRecognitionPlanActivity> = emptyList()
)

@Serializable
data class Esse3InteruniversityPlanWithDetails(
    /** code_un della sede amministrativa nel caso questo piano sia un piano interateneo da inserire nella sede operativa */
    @SerialName("sedeAmmCodeUn")
    val adminSiteUnifiedCode: String = "",

    /** id della carriera dello studente */
    @SerialName("stuId")
    val studentId: Long = 0L,

    /** progressivo del piano di studio per la carriera dello studente */
    @SerialName("pianoId")
    val planId: Int = 0,

    @SerialName("pianiInterateneo")
    val interuniversityPlans: List<Esse3InteruniversityPlanPortionWithDetails> = emptyList(),

    @SerialName("attivitaInterateneo")
    val interUniversityActivities: List<Esse3InteruniversityPlanActivityWithDetails> = emptyList(),

    @SerialName("segmentiInterateneo")
    val interuniversitySegments: List<Esse3InteruniversityPlanSegmentWithDetails> = emptyList()
)

@Serializable
data class Esse3StudyPlan(
    /** id della carriera dello studente */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** progressivo del piano di studio per la carriera dello studente */
    @SerialName("pianoId")
    val planId: Int? = null,

    /** stato dello studente */
    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    /** motivo dello stato dello studente */
    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    /** stato della matricola */
    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    /** motivo dello stato della matricola */
    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    /** id del tratto di carriera a cui si riferisce il piano di studio */
    @SerialName("matId")
    val matId: Long? = null,

    /** id del regolamento di scelta collegato al piano, vale solo per i piani standard */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** id dello schema di piano utilizzato per la compilazione del piano standard */
    @SerialName("schemaId")
    val schemaId: Long? = null,

    /** id della finestra di compilazione in cui è stato compilato il piano */
    @SerialName("finregsceId")
    val finalRegulationChoiceId: Long? = null,

    /** stato del piano (B => Bozza, P => Proposto, V => in Valutuazione, A => Approvato, R => Respinto, X => Annullato) */
    @SerialName("stato")
    val state: Esse3State3? = null,

    /** descrizione dello stato del piano */
    @SerialName("statoDes")
    val stateDescription: String? = null,

    /** data di ultima variazione dello stato del piano */
    @SerialName("dataUltimaVarStato")
    val lastStateChangeDate: String? = null,

    /** tipo di piano (S => Standard, I => Individuale) */
    @SerialName("tipoPiano")
    val planType: Esse3PlanType? = null,

    /** indica se il piano è statutario oppure no */
    @SerialName("statutarioFlg")
    val statutoryFlag: Int? = null,

    /** anno di coorte su cui è definito il regolamento di scelta (nel caso di piani standard) */
    @SerialName("coorte")
    val cohort: Int? = null,

    /** anno di revisione su cui è definito il regolamento di scelta */
    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    /** id del corso di studio di iscrizione dello studente al momento della  compilazione del piano */
    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    /** codice del corso di iscrizione dello studente al momento della compilazione del piano */
    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    /** descrizione del corso di iscrizione dello studente al momento della compilazione del piano */
    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    /** id del corso di studio di iscrizione dello studente al momento della  compilazione del piano */
    @SerialName("aaOrdStuId")
    val academicYearOrderStudentId: Long? = null,

    /** id del percorso di iscrizione dello studente al momento della compilazione del piano */
    @SerialName("pdsStuId")
    val studyPlanStudentId: Long? = null,

    /** codice del percorso di iscrizione dello studente al momento della compilazione del piano */
    @SerialName("pdsStuCod")
    val studyPlanStudentCode: String? = null,

    /** descrizione del percorso di iscrizione dello studente al momento della compilazione del piano */
    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    /** id del percorso di compilazione del piano (coincide con quello dello schema di piano) */
    @SerialName("pdsSceId")
    val studyPlanChoiceId: Long? = null,

    /** codice del percorso di compilazione del piano (coincide con quello dello schema di piano) */
    @SerialName("pdsSceCod")
    val studyPlanChoiceCode: String? = null,

    /** descrizione del percorso di compilazione del piano (coincide con quello dello schema di piano) */
    @SerialName("pdsSceDes")
    val studyPlanChoiceDescription: String? = null,

    /** anno di definizione del piano */
    @SerialName("aaDefId")
    val academicYearDefinitionId: Int? = null,

    /** anno di iscrizione dello studente nella queale è stata eseguita la compilazione del piano standard */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    /** id dell'alternativa di part time utilizzata per la compilazione del piano */
    @SerialName("aptId")
    val aptId: Long? = null,

    /** codice dell'alternativa di part time utilizzata per la compilazione del piano */
    @SerialName("aptCod")
    val aptCode: String? = null,

    /** descrizione dell'alternativa di part time utilizzata per la compilazione del piano */
    @SerialName("aptDes")
    val aptDescription: String? = null,

    /** anno di ultima attuazione del piano */
    @SerialName("aaUltimaAttuazioneId")
    val academicYearLastImplementationId: Int? = null,

    /** data di ultima attuazione del piano */
    @SerialName("dataUltimaAttuazione")
    val lastImplementationDate: String? = null,

    /** id dell'utente che ha eseguito l'ultima attuazione del piano */
    @SerialName("userUltimaAttuazione")
    val lastImplementationUser: String? = null,

    /** utente che ha effettuato il controllo del piano nel caso di approvazione */
    @SerialName("userControllo")
    val controlUser: String? = null,

    /** nota che ha inserito l'utente per la procedura di controllo piani */
    @SerialName("notaControllo")
    val controlNote: String? = null,

    /** note sul piano inserite dal sistema in seguito a procedure automatiche */
    @SerialName("noteSistema")
    val systemNotes: String? = null,

    /** note sul piano inserite dall'utente */
    @SerialName("noteUtente")
    val userNotes: String? = null,

    /** codice del piano nel sistema esterno */
    @SerialName("extCod")
    val externalCode: String? = null,

    @SerialName("regole")
    val rules: List<Esse3StudyPlanChoice> = emptyList(),

    @SerialName("attivita")
    val activity: List<Esse3StudyPlanActivity> = emptyList(),

    @SerialName("interateneo")
    val interuniversity: Esse3InteruniversityPlanWithDetails? = null
)

@Serializable
data class Esse3InteruniversityPlanPortionRuleWithDetails(
    /** code_un dell'ateneo sede del tratto di piano dove le attività della regola devono essere sostenute */
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String = "",

    /** tipo della regola prevista nel tratto del piano */
    @SerialName("tipoRegolaInterateneo")
    val interuniversityRuleType: String = "",

    /** indica se la regola deve essere visualizzata in area studente nell'ateneo collegato al tratto */
    @SerialName("viewStudente")
    val studentView: Int = 0,

    /** indica se la regola deve essere visualizzata in area docente nell'ateneo collegato al tratto */
    @SerialName("viewDocente")
    val lecturerView: Int = 0,

    /** indica se la regola deve essere visualizzata nella maschera dei piani dek client nell'ateneo collegato al tratto */
    @SerialName("viewSegr")
    val secretariatView: Int = 0,

    /** id della carriera dello studente */
    @SerialName("stuId")
    val studentId: Long = 0L,

    /** progressivo del piano di studio per la carriera dello studente */
    @SerialName("pianoId")
    val planId: Int = 0,

    /** progressivo del tratto di piano sulla sede operativa indicata */
    @SerialName("interateId")
    val integratedId: Int = 0,

    /** id della regola del piano che è prevista per il tratto di piano con sede nell'ateneo del tratto. */
    @SerialName("scePianoId")
    val choicePlanId: Long = 0L
)

@Serializable
data class Esse3CourseOrConditionsFilter(
    /** lista delle condizioni in OR da applicare */
    @SerialName("orConditions")
    val orConditions: List<Esse3CourseOrConditionFilter> = emptyList()
)

@Serializable
data class Esse3StudyPlanTeachingUnit(
    /** id della carriera dello studente */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** progressivo del piano di studio per la carriera dello studente */
    @SerialName("pianoId")
    val planId: Int? = null,

    /** progressivo dell'attività all'interno del piano di studio */
    @SerialName("itmId")
    val itemId: Int? = null,

    /** id del tratto di carriera a cui si riferisce il piano di studio */
    @SerialName("matId")
    val matId: Long? = null,

    /** id dello schema di piano utilizzato per la compilazione del piano standard */
    @SerialName("schemaId")
    val schemaId: Long? = null,

    /** progressivo della regola a cui fa riferimento l'attività interno del piano di studio */
    @SerialName("scePianoId")
    val choicePlanId: Int? = null,

    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** id univoco dell'attività dalle regole di scelta scelta durante la compilazione del piano */
    @SerialName("sceltaAdId")
    val teachingActivityChoiceId: Int? = null,

    /** id dell'unità didattica scelta per l'attività didattica selezionata */
    @SerialName("udId")
    val teachingUnitId: Int? = null,

    /** codice dell'unità didattica scelta per l'attività didattica selezionata */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** codice dell'unità didattica scelta per l'attività didattica selezionata */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    /** peso  dell'unità didattica selezionata */
    @SerialName("peso")
    val weight: Float? = null
)

@Serializable
data class Esse3PutInteruniversityRecognitionPlanActivity(
    /** codice dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("adCod")
    val activityCode: String = "",

    /** descrizione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("adDes")
    val activityDescription: String = "",

    /** codice del corso di erogazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String = "",

    /** anno di ordinamento di ergoazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Int = 0,

    /** codice del percorso di erogazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("pdsAdCod")
    val studyPlanTeachingActivityCode: String = "",

    /** anno di offerta di ergoazione dell'attività prevista nell'ateneo di sostenimento della attività */
    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int = 0,

    /** progressivo dell'attività all'interno del piano di studio */
    @SerialName("itmId")
    val itemId: Int? = null,

    /** azione da prevedere sull'attività indicata */
    @SerialName("azioneCod")
    val actionCode: String = "",

    /** tipo di azione da effettuare sul riconoscimento, valido solo se ric_id = 2; INTEGR - Integrazione, COMPL - Completamento */
    @SerialName("tipoAzioneRicCod")
    val researchActionTypeCode: String? = null,

    /** punteggio positivo minimo per il superamento dell'attività */
    @SerialName("minPuntiEsa")
    val minGraduationPoints: Int? = null,

    /** punteggio positivo massimo per il superamento dell'attività */
    @SerialName("maxPuntiEsa")
    val maxGraduationPoints: Int? = null,

    /** null */
    @SerialName("adsceIntComplId")
    val choiceInternalComplementaryId: Long? = null,

    /** codice dell'attività integrativa e/o di completamento nel caso di riconoscimenti o convalide nella sede operativa */
    @SerialName("adIntComplCod")
    val internalComplementaryActivityCode: String? = null,

    /** descrizione dell'attività integrativa e/o di completamento nel caso di riconoscimenti o convalide nella sede operativa */
    @SerialName("adIntComplDes")
    val internalComplementaryActivityDescription: String? = null,

    /** stato di sincronizzazione della attività tra la sede amministrativa e quella operativa */
    @SerialName("staAdInterateCod")
    val integratedTeachingActivityStatusCode: String? = null,

    /** dettaglio stato di sincronizzazione della attività tra la sede amministrativa e quella operativa */
    @SerialName("dettstaAdInterateCod")
    val integratedTeachingActivityStatusDetailCode: String? = null,

    @SerialName("esito")
    val outcome: Esse3InteruniversityPlanActivityResult? = null,

    @SerialName("esitoIntCompl")
    val integratedCompletionOutcome: Esse3InteruniversityPlanActivityResult? = null,

    @SerialName("segmenti")
    val segments: List<Esse3PostInteruniversityPlanSegment> = emptyList()
)

@Serializable
data class Esse3InteruniversityPlanPortion(
    /** code_un dell'ateneo sede del tratto tratto di piano */
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String = "",

    /** stato del tratto di piano, indica lo stato relativamente alla sincronizzazione con la sede amministrativa */
    @SerialName("staPianoInterateCod")
    val integratedStudyPlanStatusCode: String = "",

    /** id del piano nell'ateneo della ' */
    @SerialName("sedeOpPianoId")
    val operationalSitePlanId: Int = 0,

    /** stato del piano sulla sede definita nel tratto */
    @SerialName("sedeOpStaPianoCod")
    val operationalSitePlanStatusCode: String? = null,

    /** indica la valutazione che deve essere fatta nella sede del tratto rispetto al piano completo; 0 - nessuna approvazione, 1 - approvazione su altro tratto, 2 - approvazione su questo tratto */
    @SerialName("sedeOpValutazione")
    val operationalSiteEvaluation: Int = 0,

    /** messaggio sincronizzazione */
    @SerialName("msgSync")
    val syncMessage: String? = null,

    /** dettaglio messaggio sincronizzazione */
    @SerialName("msgSyncDett")
    val syncMessageDetail: String? = null,

    /** user controllo cognome */
    @SerialName("userControlloCognome")
    val controlUserSurname: String? = null,

    /** user controllo nome */
    @SerialName("userControllonome")
    val controlUserName: String? = null,

    /** user controllo matricola */
    @SerialName("userControlloMatricola")
    val controlUserMatricola: String? = null,

    /** nota controllo */
    @SerialName("notaControllo")
    val controlNote: String? = null,

    /** user valutatore cognome */
    @SerialName("userValutatoreCognome")
    val evaluatorUserSurname: String? = null,

    /** user valutatore nome */
    @SerialName("userValutatorenome")
    val evaluatorUserName: String? = null,

    /** user valutatore matricola */
    @SerialName("userValutatoreMatricola")
    val evaluatorUserMatricola: String? = null,

    /** nota valutatore */
    @SerialName("notaValutatore")
    val evaluatorNote: String? = null
)

@Serializable
data class Esse3PutIntegratedRecognitionPlanResponse(
    /** indica l'esito dell'operazione 1 - operazione effettuata con successo, 0 - operazione effettuata con degli scarti */
    @SerialName("stato")
    val state: Int? = null,

    @SerialName("pianoDiStudio")
    val studyPlan: Esse3StudyPlan? = null,

    @SerialName("scarti")
    val discards: List<Esse3PutIntegratedRecognitionPlanRejectionResponse> = emptyList()
)

@Serializable
data class Esse3StudyPlanChoice(
    /** id della carriera dello studente */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** progressivo del piano di studio per la carriera dello studente */
    @SerialName("pianoId")
    val planId: Int? = null,

    /** id del tratto di carriera a cui si riferisce il piano di studio */
    @SerialName("matId")
    val matId: Long? = null,

    /** id dello schema di piano utilizzato per la compilazione del piano standard */
    @SerialName("schemaId")
    val schemaId: Long? = null,

    /** progressivo della regola a cui fa riferimento l'attività interno del piano di studio */
    @SerialName("scePianoId")
    val choicePlanId: Int? = null,

    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** numero di compilazione della regola nel piano */
    @SerialName("ordNum")
    val orderNumber: Int? = null,

    /** descrizione della regola di scelta */
    @SerialName("des")
    val description: String? = null,

    /** id dello slot di part-time selezionato per la regola (valido solo se il piano ha un aptId valorizzato) */
    @SerialName("ptSlotId")
    val ptSlotId: Long? = null,

    /** codice dello slot di part-time selezionato per la regola (valido solo se il piano ha un aptId valorizzato) */
    @SerialName("ptSlotCod")
    val ptSlotCode: String? = null,

    /** descrizione dello slot di part-time selezionato per la regola (valido solo se il piano ha un aptId valorizzato) */
    @SerialName("ptSlotDes")
    val ptSlotDescription: String? = null,

    /** anno di corso della regola (zero indica anno di corso generico) */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** anno di corso di anticipo della regola */
    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    /** tipo di regola di scelta ( O => Obbligatoria; F => Da Elenco; T => Da Elenco Libero da OD; G => Gruppo; W => Gruppo Libero da OD; S => Libera da OD; V => Vincolo; D => Regola per Dottorati; E	=> Dottorati Interateneo; I	=> Elenco Interateneo; R => Libera OD Interateneo) */
    @SerialName("tipSce")
    val choiceType: Esse3ChoiceType2? = null,

    /** descrizione della tipologia di regola di scelta */
    @SerialName("tipSceDes")
    val choiceTypeDescription: String? = null,

    /** tipo di unità di misura */
    @SerialName("tipUnt")
    val teachingUnitType: Esse3TeachingUnitType? = null,

    /** valore minimo del vincolo */
    @SerialName("minUnt")
    val minTeachingUnit: Float? = null,

    /** valore massimo del vincolo */
    @SerialName("maxUnt")
    val maxTeachingUnit: Float? = null,

    /** modificatore del TAF della regola di scelta */
    @SerialName("modTAF")
    val tafMode: Esse3TafMode? = null,

    /** flag per le regole opzionali */
    @SerialName("opzFlg")
    val optionalFlag: Int? = null,

    /** flag per l'abilitiazione del tesoretto sulla regola */
    @SerialName("tesorettoFlg")
    val treasureFlag: Int? = null,

    /** flag per le regole sovrannumerarie */
    @SerialName("sovranFlg")
    val overrideFlag: Int? = null,

    /** flag per azzerare il peso delle attività della regola nell'attuazione */
    @SerialName("azzeraCfuFlg")
    val resetCreditsFlag: Int? = null,

    /** flag per l'abilitazione al sostegno */
    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    /** Rappresenta la regola di sistema utilizzata nei piani individuali per caricare le attività didattiche da maschera quando non sono ancora associate ad una regola specifica definita dall'utente. Nei piani standard le regole di sistema NON devono essere presenti, nei piani individuali deve essere presente una ed una sola regola di con il valore del flag alzato per ogni singolo piano. */
    @SerialName("regolaSistemaFlg")
    val systemRuleFlag: Int? = null,

    /** codice univoco della regola di scelta di un eventuale sistema esterno */
    @SerialName("extCod")
    val externalCode: String? = null
)
