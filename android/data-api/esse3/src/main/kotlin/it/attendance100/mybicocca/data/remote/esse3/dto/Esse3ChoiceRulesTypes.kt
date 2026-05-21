package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3ChoiceCondition(
    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** tipo di condizione, in valore viene decodificato sui campi valDes* ( A => Attività didattiche; B => Blocchi della stessa regola; P => provenienza dal primo livello (3-2); R => regola di scelta; S => condizione SQL) */
    @SerialName("tipoCondCod")
    val conditionTypeCode: Esse3ConditionTypeCode? = null,

    /** id del valore selezionato */
    @SerialName("valSelId")
    val selectedValueId: Long? = null,

    /** codice del valore selezionato */
    @SerialName("valSelCod")
    val selectedValueCode: String? = null,

    /** descrizione del valore selezionato */
    @SerialName("valSelDes")
    val selectedValueDescription: String? = null,

    /** flag che consente di negare la condizione */
    @SerialName("notFlg")
    val noteFlag: Int? = null,

    /** id univoco della condizione della regola di scelta di un eventuale sistema esterno */
    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3RulePropertyAND(
    /** id univoco del della regola AND */
    @SerialName("regpropAndId")
    val andProposalRuleId: Long? = null,

    /** id univoco del della regola OR */
    @SerialName("regpropOrId")
    val orProposalRuleId: Long? = null,

    /** id univoco del vincolo di propedeuticità */
    @SerialName("regpropVincId")
    val proposalRuleWinnerId: Long? = null,

    /** id univoco del regolamento di propedeuticità */
    @SerialName("regpropId")
    val proposalRuleId: Long? = null,

    /** per poter soddisfare la regola occorre che venga rispettato il tipo di vincolo indicato (NUM_AD => Numero di Attività Formative da superare (tra quelle possibili); TUTTE_AD => vanno superate tutte le attività formative elencate; PESO => Peso minimo valido) */
    @SerialName("tipoVincoloRegola")
    val ruleConstraintType: Esse3RuleConstraintType? = null,

    /** nel caso il tipoVincoloRegola sia NUM_AF rappresenta il numero minimo di AD da superare */
    @SerialName("numAd")
    val teachingActivityNumber: Int? = null,

    /** nel caso il tipoVincoloRegola sia PESO rappresenta il numero minimo CFU da superare */
    @SerialName("peso")
    val weight: Float? = null,

    /** indica come gli elementi figli della regola vanno considerati (1 => OR; 2 => AND) */
    @SerialName("raggruppamentoFigli")
    val childrenGrouping: Int? = null,

    @SerialName("figli")
    val children: List<Esse3RulePropertyChild> = emptyList()
)

@Serializable
data class Esse3RulePropertyChild(
    /** id univoco del figlio della regola */
    @SerialName("regpropPropId")
    val proposalRuleProposalId: Long? = null,

    /** id univoco del figlio della regola */
    @SerialName("regpropAndId")
    val andProposalRuleId: Long? = null,

    /** tipo di elemento vincolato (AC => anno di corso, AD => Attività didattica, SET => Settore Scientifico Disciplinare) */
    @SerialName("tipoElemento")
    val elementType: Esse3ElementType? = null,

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    /** anno di corso nel caso in cui il tipoElemento sia AC */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** codice del SSD nel caso in cui il tipoElemento sia SET */
    @SerialName("settCod")
    val sectorCode: String? = null
)

@Serializable
data class Esse3ChoiceRuleBlockCondition(
    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** tipo di condizione, in valore viene decodificato sui campi valDes* ( A => Attività didattiche; B => Blocchi della stessa regola; P => provenienza dal primo livello (3-2); R => regola di scelta; S => condizione SQL) */
    @SerialName("tipoCondCod")
    val conditionTypeCode: Esse3ConditionTypeCode? = null,

    /** id del valore selezionato */
    @SerialName("valSelId")
    val selectedValueId: Long? = null,

    /** codice del valore selezionato */
    @SerialName("valSelCod")
    val selectedValueCode: String? = null,

    /** descrizione del valore selezionato */
    @SerialName("valSelDes")
    val selectedValueDescription: String? = null,

    /** flag che consente di negare la condizione */
    @SerialName("notFlg")
    val noteFlag: Int? = null,

    /** id univoco della condizione della regola di scelta di un eventuale sistema esterno */
    @SerialName("extId")
    val externalId: Long? = null,

    /** id progressivo della condizione all'interno della regola di scelta */
    @SerialName("condBlkId")
    val conditionBlockId: Long? = null,

    /** id progressivo all'interno della regola di scelta del numero del blocco */
    @SerialName("blkId")
    val blockId: Int? = null,

    /** indica se la condizione è stata inserita dall'utete (manualeFlg=1) oppure dal sistema (manualeFlg=0) */
    @SerialName("manualeFlg")
    val manualFlag: Int? = null
)

@Serializable
data class Esse3ChoiceRuleWithSchemas(
    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** id progressivo all'interno del regolamento di scelta per la regola */
    @SerialName("sceId")
    val choiceRuleId: Int? = null,

    /** ordine di visualizzazione della regola all'interno dello schema di piano */
    @SerialName("ordNum")
    val orderNumber: Int? = null,

    /** descrizione della regola di scelta */
    @SerialName("des")
    val description: String? = null,

    /** descrizione della regola di scelta in inglese (se presente) */
    @SerialName("desEng")
    val descriptionEnglish: String? = null,

    /** id del percorso collegato alla regola di scelta (codice e descrizione si trovano sullo schema di piano) */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** codice del percorso collegato allo schema di piano */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** id dell'alternativa di part-time collegataalla regola di scelta (codice e descrizione si trovano sullo schema di piano) */
    @SerialName("aptId")
    val aptId: Long? = null,

    /** codice dell'alternativa di part-time collegata allo schema di piano */
    @SerialName("aptCod")
    val aptCode: String? = null,

    /** anno di corso della regola (zero indica anno di corso generico) */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** anno di corso di anticipo della regola */
    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    /** tipo di regola di scelta ( O => Obbligatoria; F => Da Elenco; T => Da Elenco Libero da OD; G => Gruppo; W => Gruppo Libero da OD; S => Libera da OD; V => Vincolo; D => Regola per Dottorandi) */
    @SerialName("tipSce")
    val choiceType: Esse3ChoiceType? = null,

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

    /** id della regola vincolo a cui la regola fa riferimento (vinId=sceId) */
    @SerialName("vinId")
    val constraintId: Int? = null,

    /** livello del vincolo, valorizzato solo per le regole vincolo */
    @SerialName("livello")
    val level: Int? = null,

    /** modificatore del TAF della regola di scelta */
    @SerialName("modTAF")
    val tafMode: Esse3TafMode? = null,

    /** flag per l'abilitazione al sostegno */
    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    /** flag per le regole opzionali */
    @SerialName("opzFlg")
    val optionalFlag: Int? = null,

    /** flag per le regole sovrannumerarie */
    @SerialName("sovranFlg")
    val overrideFlag: Int? = null,

    /** flag per l'abilitiazione del tesoretto sulla regola */
    @SerialName("tesorettoFlg")
    val treasureFlag: Int? = null,

    /** peso della rosa ristretta associato alla regola */
    @SerialName("pesoRosaRist")
    val restrictedListWeight: Long? = null,

    /** flag per l'abilitazione dell'assegnazione posti nella regola di scelta */
    @SerialName("assegnazionePostiFlg")
    val seatAssignmentFlag: Int? = null,

    /** flag per la generazione di una delibera per le AD nella regola */
    @SerialName("deliberaFlg")
    val resolutionFlag: Int? = null,

    /** flag per azzerare il peso delle attività della regola nell'attuazione */
    @SerialName("azzeraCfuFlg")
    val resetCreditsFlag: Int? = null,

    /** flag per la selezione dei parametri della logistica durante la compilazione del piano */
    @SerialName("parametriLogisticaFlg")
    val logisticsParametersFlag: Int? = null,

    /** Nota opzionale da visualizzare prima delle attività da scegliere */
    @SerialName("notaPre")
    val preNote: String? = null,

    /** Nota opzionale da visualizzare prima delle attività da scegliere in inglese (se presente) */
    @SerialName("notaPreEng")
    val preNoteEnglish: String? = null,

    /** Flag di visibilità per la descrizione della nota_pre del regolamento nell'area pubblica */
    @SerialName("notaPreVisPubblFlg")
    val preNotePublicVisibleFlag: Int? = null,

    /** Nota opzionale da visualizzare dopo le attività da scegliere */
    @SerialName("notaPost")
    val postNote: String? = null,

    /** Nota opzionale da visualizzare dopo le attività da scegliere in inglese (se presente) */
    @SerialName("notaPostEng")
    val postNoteEnglish: String? = null,

    /** Flag di visibilità per la descrizione della nota_post del regolamento nell'area pubblica */
    @SerialName("notaPostVisPubblFlg")
    val postNotePublicVisibleFlag: Int? = null,

    /** id univoco della regola di scelta di un eventuale sistema esterno */
    @SerialName("extId")
    val externalId: Long? = null,

    /** codice univoco della regola di scelta di un eventuale sistema esterno */
    @SerialName("extCod")
    val externalCode: String? = null,

    @SerialName("schemi")
    val schemas: List<Esse3PlanSchemaChoiceRule> = emptyList()
)

@Serializable
data class Esse3RulePropertyOR(
    /** id univoco del della regola OR */
    @SerialName("regpropOrId")
    val orProposalRuleId: Long? = null,

    /** id univoco del vincolo di propedeuticità */
    @SerialName("regpropVincId")
    val proposalRuleWinnerId: Long? = null,

    /** id univoco del regolamento di propedeuticità */
    @SerialName("regpropId")
    val proposalRuleId: Long? = null,

    /** descrizione della regola in OR */
    @SerialName("des")
    val description: String? = null,

    @SerialName("regolePropAND")
    val andProposalRules: List<Esse3RulePropertyAND> = emptyList()
)

@Serializable
data class Esse3ChoiceRegulationWindow(
    /** id della finestra del regolamento di scelta */
    @SerialName("finregsceId")
    val finalRegulationChoiceId: Long? = null,

    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** tipo di finestra di ricompilazione */
    @SerialName("tipoFinestra")
    val windowType: String? = null,

    /** data di inizio validità dello schema (DD/MM/YYYY), se nulla lo schema risulta sempre valido */
    @SerialName("dataInizio")
    val startDate: String? = null,

    /** data di fine validità dello schema (DD/MM/YYYY), se nulla lo schema risulta sempre valido */
    @SerialName("dataFine")
    val endDate: String? = null,

    /** id univoco della finestra del regolamento di scelta di un eventuale sistema esterno */
    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3PrerequisitesRegulationWithConstraints(
    /** id univoco del regolamento di propedeuticità */
    @SerialName("regpropId")
    val proposalRuleId: Long? = null,

    /** id univoco del dipartimento su cui è definito il regolamento di propedeuticità */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice del dipartimento su cui è definito il regolamento di propedeuticità */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** codice del tipo di corso su cui è definito il regolamento di propedeuticità */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** id univoco del corso di studio su cui è definito il regolamento di propedeuticità */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio su cui è definito il regolamento di propedeuticità */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di studio su cui è definito il regolamento di propedeuticità */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** id univoco dell'ordinamento di corso di studio su cui è definito il regolamento di propedeuticità */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** anno di coorte su cui è definito il regolamento di propedeuticità */
    @SerialName("coorte")
    val cohort: Int? = null,

    /** anno di revisione su cui è definito il regolamento di propedeuticità */
    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    /** stato del regolamento di propedeuticità */
    @SerialName("stato")
    val state: Esse3State2? = null,

    /** id univoco del regolamento di propedeuticità di un eventuale sistema esterno */
    @SerialName("extId")
    val externalId: Long? = null,

    @SerialName("vincoli")
    val constraints: List<Esse3PrerequisiteConstraint> = emptyList()
)

@Serializable
data class Esse3StudyPlanHeader(
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
    val externalCode: String? = null
)

@Serializable
data class Esse3ChoiceRuleCondition(
    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** tipo di condizione, in valore viene decodificato sui campi valDes* ( A => Attività didattiche; B => Blocchi della stessa regola; P => provenienza dal primo livello (3-2); R => regola di scelta; S => condizione SQL) */
    @SerialName("tipoCondCod")
    val conditionTypeCode: Esse3ConditionTypeCode? = null,

    /** id del valore selezionato */
    @SerialName("valSelId")
    val selectedValueId: Long? = null,

    /** codice del valore selezionato */
    @SerialName("valSelCod")
    val selectedValueCode: String? = null,

    /** descrizione del valore selezionato */
    @SerialName("valSelDes")
    val selectedValueDescription: String? = null,

    /** flag che consente di negare la condizione */
    @SerialName("notFlg")
    val noteFlag: Int? = null,

    /** id univoco della condizione della regola di scelta di un eventuale sistema esterno */
    @SerialName("extId")
    val externalId: Long? = null,

    /** id progressivo della condizione all'interno della regola di scelta */
    @SerialName("condSceId")
    val conditionChoiceId: Long? = null
)

@Serializable
data class Esse3ChoiceRule(
    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** id progressivo all'interno del regolamento di scelta per la regola */
    @SerialName("sceId")
    val choiceRuleId: Int? = null,

    /** ordine di visualizzazione della regola all'interno dello schema di piano */
    @SerialName("ordNum")
    val orderNumber: Int? = null,

    /** descrizione della regola di scelta */
    @SerialName("des")
    val description: String? = null,

    /** descrizione della regola di scelta in inglese (se presente) */
    @SerialName("desEng")
    val descriptionEnglish: String? = null,

    /** id del percorso collegato alla regola di scelta (codice e descrizione si trovano sullo schema di piano) */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** codice del percorso collegato allo schema di piano */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** id dell'alternativa di part-time collegataalla regola di scelta (codice e descrizione si trovano sullo schema di piano) */
    @SerialName("aptId")
    val aptId: Long? = null,

    /** codice dell'alternativa di part-time collegata allo schema di piano */
    @SerialName("aptCod")
    val aptCode: String? = null,

    /** anno di corso della regola (zero indica anno di corso generico) */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** anno di corso di anticipo della regola */
    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    /** tipo di regola di scelta ( O => Obbligatoria; F => Da Elenco; T => Da Elenco Libero da OD; G => Gruppo; W => Gruppo Libero da OD; S => Libera da OD; V => Vincolo; D => Regola per Dottorandi) */
    @SerialName("tipSce")
    val choiceType: Esse3ChoiceType? = null,

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

    /** id della regola vincolo a cui la regola fa riferimento (vinId=sceId) */
    @SerialName("vinId")
    val constraintId: Int? = null,

    /** livello del vincolo, valorizzato solo per le regole vincolo */
    @SerialName("livello")
    val level: Int? = null,

    /** modificatore del TAF della regola di scelta */
    @SerialName("modTAF")
    val tafMode: Esse3TafMode? = null,

    /** flag per l'abilitazione al sostegno */
    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    /** flag per le regole opzionali */
    @SerialName("opzFlg")
    val optionalFlag: Int? = null,

    /** flag per le regole sovrannumerarie */
    @SerialName("sovranFlg")
    val overrideFlag: Int? = null,

    /** flag per l'abilitiazione del tesoretto sulla regola */
    @SerialName("tesorettoFlg")
    val treasureFlag: Int? = null,

    /** peso della rosa ristretta associato alla regola */
    @SerialName("pesoRosaRist")
    val restrictedListWeight: Long? = null,

    /** flag per l'abilitazione dell'assegnazione posti nella regola di scelta */
    @SerialName("assegnazionePostiFlg")
    val seatAssignmentFlag: Int? = null,

    /** flag per la generazione di una delibera per le AD nella regola */
    @SerialName("deliberaFlg")
    val resolutionFlag: Int? = null,

    /** flag per azzerare il peso delle attività della regola nell'attuazione */
    @SerialName("azzeraCfuFlg")
    val resetCreditsFlag: Int? = null,

    /** flag per la selezione dei parametri della logistica durante la compilazione del piano */
    @SerialName("parametriLogisticaFlg")
    val logisticsParametersFlag: Int? = null,

    /** Nota opzionale da visualizzare prima delle attività da scegliere */
    @SerialName("notaPre")
    val preNote: String? = null,

    /** Nota opzionale da visualizzare prima delle attività da scegliere in inglese (se presente) */
    @SerialName("notaPreEng")
    val preNoteEnglish: String? = null,

    /** Flag di visibilità per la descrizione della nota_pre del regolamento nell'area pubblica */
    @SerialName("notaPreVisPubblFlg")
    val preNotePublicVisibleFlag: Int? = null,

    /** Nota opzionale da visualizzare dopo le attività da scegliere */
    @SerialName("notaPost")
    val postNote: String? = null,

    /** Nota opzionale da visualizzare dopo le attività da scegliere in inglese (se presente) */
    @SerialName("notaPostEng")
    val postNoteEnglish: String? = null,

    /** Flag di visibilità per la descrizione della nota_post del regolamento nell'area pubblica */
    @SerialName("notaPostVisPubblFlg")
    val postNotePublicVisibleFlag: Int? = null,

    /** id univoco della regola di scelta di un eventuale sistema esterno */
    @SerialName("extId")
    val externalId: Long? = null,

    /** codice univoco della regola di scelta di un eventuale sistema esterno */
    @SerialName("extCod")
    val externalCode: String? = null
)

@Serializable
data class Esse3StudyPlanHeaderPerActivity(
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

    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** id univoco dell'attività dalle regole di scelta scelta durante la compilazione del piano */
    @SerialName("sceltaAdId")
    val teachingActivityChoiceId: Int? = null
)

@Serializable
data class Esse3ChoiceRuleSchemaWithDetails(
    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** id progressivo all'interno del regolamento di scelta per la regola */
    @SerialName("sceId")
    val choiceRuleId: Int? = null,

    /** ordine di visualizzazione della regola all'interno dello schema di piano */
    @SerialName("ordNum")
    val orderNumber: Int? = null,

    /** descrizione della regola di scelta */
    @SerialName("des")
    val description: String? = null,

    /** descrizione della regola di scelta in inglese (se presente) */
    @SerialName("desEng")
    val descriptionEnglish: String? = null,

    /** id del percorso collegato alla regola di scelta (codice e descrizione si trovano sullo schema di piano) */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** codice del percorso collegato allo schema di piano */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** id dell'alternativa di part-time collegataalla regola di scelta (codice e descrizione si trovano sullo schema di piano) */
    @SerialName("aptId")
    val aptId: Long? = null,

    /** codice dell'alternativa di part-time collegata allo schema di piano */
    @SerialName("aptCod")
    val aptCode: String? = null,

    /** anno di corso della regola (zero indica anno di corso generico) */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** anno di corso di anticipo della regola */
    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    /** tipo di regola di scelta ( O => Obbligatoria; F => Da Elenco; T => Da Elenco Libero da OD; G => Gruppo; W => Gruppo Libero da OD; S => Libera da OD; V => Vincolo; D => Regola per Dottorandi) */
    @SerialName("tipSce")
    val choiceType: Esse3ChoiceType? = null,

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

    /** id della regola vincolo a cui la regola fa riferimento (vinId=sceId) */
    @SerialName("vinId")
    val constraintId: Int? = null,

    /** livello del vincolo, valorizzato solo per le regole vincolo */
    @SerialName("livello")
    val level: Int? = null,

    /** modificatore del TAF della regola di scelta */
    @SerialName("modTAF")
    val tafMode: Esse3TafMode? = null,

    /** flag per l'abilitazione al sostegno */
    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    /** flag per le regole opzionali */
    @SerialName("opzFlg")
    val optionalFlag: Int? = null,

    /** flag per le regole sovrannumerarie */
    @SerialName("sovranFlg")
    val overrideFlag: Int? = null,

    /** flag per l'abilitiazione del tesoretto sulla regola */
    @SerialName("tesorettoFlg")
    val treasureFlag: Int? = null,

    /** peso della rosa ristretta associato alla regola */
    @SerialName("pesoRosaRist")
    val restrictedListWeight: Long? = null,

    /** flag per l'abilitazione dell'assegnazione posti nella regola di scelta */
    @SerialName("assegnazionePostiFlg")
    val seatAssignmentFlag: Int? = null,

    /** flag per la generazione di una delibera per le AD nella regola */
    @SerialName("deliberaFlg")
    val resolutionFlag: Int? = null,

    /** flag per azzerare il peso delle attività della regola nell'attuazione */
    @SerialName("azzeraCfuFlg")
    val resetCreditsFlag: Int? = null,

    /** flag per la selezione dei parametri della logistica durante la compilazione del piano */
    @SerialName("parametriLogisticaFlg")
    val logisticsParametersFlag: Int? = null,

    /** Nota opzionale da visualizzare prima delle attività da scegliere */
    @SerialName("notaPre")
    val preNote: String? = null,

    /** Nota opzionale da visualizzare prima delle attività da scegliere in inglese (se presente) */
    @SerialName("notaPreEng")
    val preNoteEnglish: String? = null,

    /** Flag di visibilità per la descrizione della nota_pre del regolamento nell'area pubblica */
    @SerialName("notaPreVisPubblFlg")
    val preNotePublicVisibleFlag: Int? = null,

    /** Nota opzionale da visualizzare dopo le attività da scegliere */
    @SerialName("notaPost")
    val postNote: String? = null,

    /** Nota opzionale da visualizzare dopo le attività da scegliere in inglese (se presente) */
    @SerialName("notaPostEng")
    val postNoteEnglish: String? = null,

    /** Flag di visibilità per la descrizione della nota_post del regolamento nell'area pubblica */
    @SerialName("notaPostVisPubblFlg")
    val postNotePublicVisibleFlag: Int? = null,

    /** id univoco della regola di scelta di un eventuale sistema esterno */
    @SerialName("extId")
    val externalId: Long? = null,

    /** codice univoco della regola di scelta di un eventuale sistema esterno */
    @SerialName("extCod")
    val externalCode: String? = null,

    /** id univoco dello schema di piano */
    @SerialName("schemaId")
    val schemaId: Long? = null,

    /** codice dello schema di piano, univoco in base al regolamento di scelta dove è stato definito */
    @SerialName("schemaCod")
    val schemaCode: String? = null,

    /** id dello slot di part-time collegato alla regola */
    @SerialName("ptSlotId")
    val ptSlotId: Long? = null,

    /** codice dello slot di part-time collegato alla regola */
    @SerialName("ptSlotCod")
    val ptSlotCode: String? = null,

    /** descrizione dello slot di part-time collegato alla regola */
    @SerialName("ptSlotDes")
    val ptSlotDescription: String? = null,

    @SerialName("blocchi")
    val blocks: List<Esse3ChoiceRuleBlock> = emptyList(),

    @SerialName("condizioni")
    val conditions: List<Esse3ChoiceRuleCondition> = emptyList(),

    @SerialName("filtriOD")
    val orFilters: List<Esse3ChoiceRuleOrFilter> = emptyList()
)

@Serializable
data class Esse3StudentsStatistics(
    /** Numero di piani standard validi. (Stati A, P, V). */
    @SerialName("numPianiStandardValidi")
    val validStandardPlansNumber: Int? = null,

    /** Numero di piani individuali validi. (Stati A, P, V). */
    @SerialName("numPianiIndividualiValidi")
    val validIndividualPlansNumber: Int? = null,

    /** Numero di piani totali. */
    @SerialName("numPiani")
    val planNumber: Int? = null
)

@Serializable
data class Esse3PlansLinkedToActivities(
    /** codice del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica */
    @SerialName("cdsAdDes")
    val courseOfStudyTeachingActivityDescription: String? = null,

    /** anno di ordinamento dell'attività didattica */
    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Int? = null,

    /** anno di offerta dell'attività didattica */
    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    /** codice percorso di studio di erogazione dell'attività */
    @SerialName("pdsAdCod")
    val studyPlanTeachingActivityCode: String? = null,

    /** descrizione percorso di studio di erogazione dell'attività */
    @SerialName("pdsAdDes")
    val studyPlanTeachingActivityDescription: String? = null,

    /** codice attività didattica */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione attività didattica */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** id univoco della ad inserita nella regola di scelta */
    @SerialName("sceltaAdId")
    val teachingActivityChoiceId: Long? = null,

    /** id della scelta */
    @SerialName("sceId")
    val choiceRuleId: Int? = null,

    /** Identificativo del blocco di scelta che contiene l'attività didattica */
    @SerialName("blkId")
    val blockId: Int? = null,

    /** conteggio piani collegati alla attività */
    @SerialName("conteggio")
    val count: Int? = null
)

@Serializable
data class Esse3StudyPlanHeaderPerRule(
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

    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null
)

@Serializable
data class Esse3PrerequisitesRegulation(
    /** id univoco del regolamento di propedeuticità */
    @SerialName("regpropId")
    val proposalRuleId: Long? = null,

    /** id univoco del dipartimento su cui è definito il regolamento di propedeuticità */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice del dipartimento su cui è definito il regolamento di propedeuticità */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** codice del tipo di corso su cui è definito il regolamento di propedeuticità */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** id univoco del corso di studio su cui è definito il regolamento di propedeuticità */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio su cui è definito il regolamento di propedeuticità */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di studio su cui è definito il regolamento di propedeuticità */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** id univoco dell'ordinamento di corso di studio su cui è definito il regolamento di propedeuticità */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** anno di coorte su cui è definito il regolamento di propedeuticità */
    @SerialName("coorte")
    val cohort: Int? = null,

    /** anno di revisione su cui è definito il regolamento di propedeuticità */
    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    /** stato del regolamento di propedeuticità */
    @SerialName("stato")
    val state: Esse3State2? = null,

    /** id univoco del regolamento di propedeuticità di un eventuale sistema esterno */
    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3PlansCountsFilters(
    /** Visualizzazione nella response del conteggio schemi */
    @SerialName("conteggiSchemi")
    val schemaCounts: Boolean? = null,

    /** Visualizzazione nella response del conteggio regole */
    @SerialName("conteggiRegole")
    val ruleCounts: Boolean? = null,

    /** Visualizzazione nella response del conteggio attività */
    @SerialName("conteggiAttivita")
    val activityCounts: Boolean? = null,

    /** Visualizzazione nella response del conteggio finestre */
    @SerialName("conteggiFinestre")
    val windowCounts: Boolean? = null,

    /** codice dello schema scelto dallo studente */
    @SerialName("filtroSchemaCod")
    val schemaFilterCode: String? = null,

    /** numero di compilazione della regola nel piano */
    @SerialName("filtroRegolaOrdNum")
    val ruleOrderNumberFilter: Int? = null,

    /** codice dell'attività didattica */
    @SerialName("filtroADCod")
    val teachingActivityFilterCode: String? = null,

    /** codice del corso dell'attività didattica */
    @SerialName("filtroADCdsCod")
    val teachingActivityCourseOfStudyFilterCode: String? = null,

    /** anno di ordinamento del corso di studio di erogazione dell'attività didattica */
    @SerialName("filtroADAaOrdId")
    val teachingActivityAcademicYearOrderFilterId: Int? = null,

    /** codice del percorso di erogazione dell'attività didattica */
    @SerialName("filtroADPdsCod")
    val teachingActivityStudyPlanFilterCode: String? = null,

    /** anno di offerta dell'attività */
    @SerialName("filtroADAaOffId")
    val teachingActivityAcademicYearOfferFilterId: Int? = null,

    /** codice della finestra del regolamento di scelta */
    @SerialName("filtroFinregsceCod")
    val finalRegulationChoiceFilterCode: String? = null
)

@Serializable
data class Esse3PlansLinkedToRule(
    /** Numero di ordine della scelta */
    @SerialName("ordNum")
    val orderNumber: Int? = null,

    /** id scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** id della scelta */
    @SerialName("sceId")
    val choiceRuleId: Int? = null,

    /** descrizione della regola */
    @SerialName("sceDes")
    val choiceDescription: String? = null,

    /** tipo regola */
    @SerialName("tipSce")
    val choiceType: String? = null,

    /** descrizione tipo regola */
    @SerialName("tipSceDes")
    val choiceTypeDescription: String? = null,

    /** conteggio piani collegati alla regola */
    @SerialName("conteggio")
    val count: Int? = null
)

@Serializable
data class Esse3TeachingUnitChoiceRuleRegulation(
    /** id progressivo della regola sulle UD */
    @SerialName("regUdId")
    val teachingUnitRegistrationId: Long? = null,

    /** id progressivo all'interno della regola di scelta del numero del blocco */
    @SerialName("blkId")
    val blockId: Int? = null,

    /** id univoco della ad inserita nella regola di scelta */
    @SerialName("sceltaAdId")
    val teachingActivityChoiceId: Long? = null,

    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** tipo di unità didattica */
    @SerialName("tipoUdCod")
    val teachingUnitTypeCode: String? = null,

    /** tipo di unità didattica del vincolo 1 */
    @SerialName("tipo1")
    val type1: String? = null,

    /** unità di misura del vincolo 1 */
    @SerialName("um1")
    val measurementUnit1: Esse3MeasurementUnit1? = null,

    /** quantità da selezionare del vincolo 1 */
    @SerialName("qta1")
    val quantity1: Int? = null,

    /** tipo di unità didattica del vincolo 2 */
    @SerialName("tipo2")
    val type2: String? = null,

    /** unità di misura del vincolo 2 */
    @SerialName("um2")
    val measurementUnit2: Esse3MeasurementUnit1? = null,

    /** quantità da selezionare del vincolo 2 */
    @SerialName("qta2")
    val quantity2: Int? = null,

    /** tipo di unità didattica del vincolo 3 */
    @SerialName("tipo3")
    val type3: String? = null,

    /** unità di misura del vincolo 3 */
    @SerialName("um3")
    val measurementUnit3: Esse3MeasurementUnit1? = null,

    /** quantità da selezionare del vincolo 3 */
    @SerialName("qta3")
    val quantity3: Int? = null,

    @SerialName("moduli")
    val modules: List<Esse3TeachingUnitChoiceRule> = emptyList()
)

@Serializable
data class Esse3ChoiceRegulationWithSchemas(
    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** id univoco del dipartimento su cui è definito il regolamento di scelta */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice del dipartimento su cui è definito il regolamento di scelta */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** codice del tipo di corso su cui è definito il regolamento di scelta */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** id univoco del corso di studio su cui è definito il regolamento di scelta */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio su cui è definito il regolamento di scelta */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di studio su cui è definito il regolamento di scelta */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** id univoco dell'ordinamento di corso di studio su cui è definito il regolamento di scelta */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** anno di coorte su cui è definito il regolamento di scelta */
    @SerialName("coorte")
    val cohort: Int? = null,

    /** anno di revisione su cui è definito il regolamento di scelta */
    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    /** stato del regolamento di scelta */
    @SerialName("stato")
    val state: Esse3State2? = null,

    /** data di ultima modifica del regolamento, nel caso il regolamento sia attivo coincide con la data di ultima modifica di tutte le entità sottostanti */
    @SerialName("dataUltimaModifica")
    val lastModificationDate: String? = null,

    /** data di inserimento del regolamento */
    @SerialName("dataInserimento")
    val insertionDate: String? = null,

    @SerialName("finestreDiCompilazione")
    val compilationWindows: List<Esse3ChoiceRegulationWindow> = emptyList(),

    @SerialName("schemiDiPiano")
    val planSchemas: List<Esse3PlanSchema> = emptyList()
)

@Serializable
data class Esse3ActivityChoiceRule(
    /** id univoco della ad inserita nella regola di scelta */
    @SerialName("sceltaAdId")
    val teachingActivityChoiceId: Long? = null,

    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** nel caso l'attività sia ad UD configurabili visualizza la descrizione delle regola sulla selezione delle UD (calcolata dal sistema) */
    @SerialName("regUdDesAuto")
    val teachingUnitAutoDescription: String? = null,

    /** nel caso l'attività sia ad UD configurabili visualizza la descrizione delle regola sulla selezione delle UD (inserita dall'utente) */
    @SerialName("regUdDesUte")
    val teachingUnitUserDescription: String? = null,

    /** id progressivo all'interno della regola di scelta del numero del blocco */
    @SerialName("blkId")
    val blockId: Int? = null,

    /** peso associato all'attività didattica */
    @SerialName("peso")
    val weight: Float? = null,

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    @SerialName("regoleUD")
    val teachingUnitRules: List<Esse3TeachingUnitChoiceRuleRegulation> = emptyList()
)

@Serializable
data class Esse3ChoiceRegulation(
    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** id univoco del dipartimento su cui è definito il regolamento di scelta */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice del dipartimento su cui è definito il regolamento di scelta */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** codice del tipo di corso su cui è definito il regolamento di scelta */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** id univoco del corso di studio su cui è definito il regolamento di scelta */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio su cui è definito il regolamento di scelta */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di studio su cui è definito il regolamento di scelta */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** id univoco dell'ordinamento di corso di studio su cui è definito il regolamento di scelta */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** anno di coorte su cui è definito il regolamento di scelta */
    @SerialName("coorte")
    val cohort: Int? = null,

    /** anno di revisione su cui è definito il regolamento di scelta */
    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    /** stato del regolamento di scelta */
    @SerialName("stato")
    val state: Esse3State2? = null,

    /** data di ultima modifica del regolamento, nel caso il regolamento sia attivo coincide con la data di ultima modifica di tutte le entità sottostanti */
    @SerialName("dataUltimaModifica")
    val lastModificationDate: String? = null,

    /** data di inserimento del regolamento */
    @SerialName("dataInserimento")
    val insertionDate: String? = null
)

@Serializable
data class Esse3ContextualizedTeachingUnitKey(
    @SerialName("chiaveAdContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey,

    /** chiave del'unità (modulo) dell'attività didattica */
    @SerialName("udId")
    val teachingUnitId: Long = 0L,

    /** codice del'unità (modulo) dell'attività didattica */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** descrizione del'unità (modulo) dell'attività didattica */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    /** descrizione del'unità (modulo) dell'attività didattica in lingua inglese */
    @SerialName("udDesEng")
    val teachingUnitDescriptionEnglish: String? = null
)

@Serializable
data class Esse3ChoiceRuleAndFilter(
    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** id progressivo all'interno della regola */
    @SerialName("orId")
    val orId: Int? = null,

    /** id progressivo all'interno del filtro OR */
    @SerialName("andId")
    val andId: Int? = null,

    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** codice tipo di filtro in AND, in valore selezionato va decodificato sui campi valSel* */
    @SerialName("tipoFiltroCod")
    val filterTypeCode: String? = null,

    /** descrizione tipo di filtro in AND, in valore selezionato va decodificato sui campi valSel* */
    @SerialName("tipoFiltroDes")
    val filterTypeDescription: String? = null,

    /** id del valore selezionato */
    @SerialName("valSelId")
    val selectedValueId: Long? = null,

    /** codice del valore selezionato */
    @SerialName("valSelCod")
    val selectedValueCode: String? = null,

    /** descrizione del valore selezionato */
    @SerialName("valSelDes")
    val selectedValueDescription: String? = null,

    /** flag che consente di negare la filtro */
    @SerialName("notFlg")
    val noteFlag: Int? = null,

    /** id univoco della fintro in AND della regola di scelta di un eventuale sistema esterno */
    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3PrerequisiteConstraint(
    /** id univoco del vincolo di propedeuticità */
    @SerialName("regpropVincId")
    val proposalRuleWinnerId: Long? = null,

    /** id univoco del regolamento di propedeuticità */
    @SerialName("regpropId")
    val proposalRuleId: Long? = null,

    /** tipo di vincolo dell'elemento del vincolo di propedeuticità (AC => anno di corso, AD => Attività didattica, SET => Settore Scientifico Disciplinare) */
    @SerialName("tipoVincolo")
    val constraintType: Esse3ElementType? = null,

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    /** anno di corso nel caso in cui il tipoVicolo sia AC */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** codice del SSD nel caso in cui il tipoVincolo sia SET */
    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("regolePropOR")
    val orProposalRules: List<Esse3RulePropertyOR> = emptyList(),

    /** id univoco del vincolo di propedeuticità di un eventuale sistema esterno */
    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3ChoiceRuleBlock(
    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** id progressivo all'interno della regola di scelta del numero del blocco */
    @SerialName("blkId")
    val blockId: Int? = null,

    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** id della lingua collegata al blocco */
    @SerialName("linguaId")
    val languageId: Long? = null,

    /** codice iso_6352 della lingua collegata al blocco */
    @SerialName("linguaCod")
    val languageCode: String? = null,

    /** numero della lingua */
    @SerialName("linguaNum")
    val languageNumber: Long? = null,

    /** flag per indicare se il blocco va incluso nel piano statutario */
    @SerialName("defFlg")
    val definitionFlag: Int? = null,

    /** flag per il controllo sull'anno di offerta per la visualizzazione durante la compilazione del piano */
    @SerialName("ctrlFlg")
    val controlFlag: Int? = null,

    /** anno di offerta di erogazione delle attività del blocco */
    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    @SerialName("attivita")
    val activity: List<Esse3ActivityChoiceRule> = emptyList(),

    @SerialName("condizioni")
    val conditions: List<Esse3ChoiceRuleBlockCondition> = emptyList(),

    /** id univoco del blocco della regola di scelta di un eventuale sistema esterno */
    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3PlansLinkedToSchema(
    /** codice schema */
    @SerialName("schemaCod")
    val schemaCode: String? = null,

    /** descrizione schema */
    @SerialName("schemaDes")
    val schemaDescription: String? = null,

    /** id schema */
    @SerialName("schemaId")
    val schemaId: Long? = null,

    /** conteggio piani collegati allo schema */
    @SerialName("conteggio")
    val count: Int? = null
)

@Serializable
data class Esse3ChoiceRuleOrFilter(
    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** id progressivo all'interno della regola */
    @SerialName("orId")
    val orId: Int? = null,

    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** descrizione del filtro in or */
    @SerialName("des")
    val description: String? = null,

    @SerialName("filtriAND")
    val andFilters: List<Esse3ChoiceRuleAndFilter> = emptyList(),

    /** id univoco della fintro in OR della regola di scelta di un eventuale sistema esterno */
    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3TeachingUnitChoiceRule(
    /** id progressivo della ud rispetto alla regUd */
    @SerialName("udId")
    val teachingUnitId: Long? = null,

    /** id progressivo della regola sulle UD */
    @SerialName("regUdId")
    val teachingUnitRegistrationId: Long? = null,

    /** id progressivo all'interno della regola di scelta del numero del blocco */
    @SerialName("blkId")
    val blockId: Int? = null,

    /** id univoco della ad inserita nella regola di scelta */
    @SerialName("sceltaAdId")
    val teachingActivityChoiceId: Long? = null,

    /** flag che indica se l'UD deve essere statutaria */
    @SerialName("statutFlg")
    val statuteFlag: Int? = null,

    /** flag che indica se l'UD deve essere obbligatoria */
    @SerialName("obbligFlg")
    val mandatoryFlag: Int? = null,

    @SerialName("chiaveUDContestualizzata")
    val contextualizedTeachingUnitKey: Esse3ContextualizedTeachingUnitKey? = null,

    /** id univoco del blocco della regola di scelta di un eventuale sistema esterno */
    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3ChoiceRegulationWithCounts(
    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** id univoco del dipartimento su cui è definito il regolamento di scelta */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice del dipartimento su cui è definito il regolamento di scelta */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** codice del tipo di corso su cui è definito il regolamento di scelta */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** id univoco del corso di studio su cui è definito il regolamento di scelta */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio su cui è definito il regolamento di scelta */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di studio su cui è definito il regolamento di scelta */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** id univoco dell'ordinamento di corso di studio su cui è definito il regolamento di scelta */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** anno di coorte su cui è definito il regolamento di scelta */
    @SerialName("coorte")
    val cohort: Int? = null,

    /** anno di revisione su cui è definito il regolamento di scelta */
    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    /** stato del regolamento di scelta */
    @SerialName("stato")
    val state: Esse3State2? = null,

    /** data di ultima modifica del regolamento, nel caso il regolamento sia attivo coincide con la data di ultima modifica di tutte le entità sottostanti */
    @SerialName("dataUltimaModifica")
    val lastModificationDate: String? = null,

    /** data di inserimento del regolamento */
    @SerialName("dataInserimento")
    val insertionDate: String? = null,

    @SerialName("schemi")
    val schemas: List<Esse3PlansLinkedToSchema> = emptyList(),

    @SerialName("regole")
    val rules: List<Esse3PlansLinkedToRule> = emptyList(),

    @SerialName("attivita")
    val activity: List<Esse3PlansLinkedToActivities> = emptyList(),

    @SerialName("finestre")
    val windows: List<Esse3PlansLinkedToWindow> = emptyList()
)

@Serializable
data class Esse3ChoiceRuleWithDetails(
    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** id progressivo all'interno del regolamento di scelta per la regola */
    @SerialName("sceId")
    val choiceRuleId: Int? = null,

    /** ordine di visualizzazione della regola all'interno dello schema di piano */
    @SerialName("ordNum")
    val orderNumber: Int? = null,

    /** descrizione della regola di scelta */
    @SerialName("des")
    val description: String? = null,

    /** descrizione della regola di scelta in inglese (se presente) */
    @SerialName("desEng")
    val descriptionEnglish: String? = null,

    /** id del percorso collegato alla regola di scelta (codice e descrizione si trovano sullo schema di piano) */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** codice del percorso collegato allo schema di piano */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** id dell'alternativa di part-time collegataalla regola di scelta (codice e descrizione si trovano sullo schema di piano) */
    @SerialName("aptId")
    val aptId: Long? = null,

    /** codice dell'alternativa di part-time collegata allo schema di piano */
    @SerialName("aptCod")
    val aptCode: String? = null,

    /** anno di corso della regola (zero indica anno di corso generico) */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** anno di corso di anticipo della regola */
    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    /** tipo di regola di scelta ( O => Obbligatoria; F => Da Elenco; T => Da Elenco Libero da OD; G => Gruppo; W => Gruppo Libero da OD; S => Libera da OD; V => Vincolo; D => Regola per Dottorandi) */
    @SerialName("tipSce")
    val choiceType: Esse3ChoiceType? = null,

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

    /** id della regola vincolo a cui la regola fa riferimento (vinId=sceId) */
    @SerialName("vinId")
    val constraintId: Int? = null,

    /** livello del vincolo, valorizzato solo per le regole vincolo */
    @SerialName("livello")
    val level: Int? = null,

    /** modificatore del TAF della regola di scelta */
    @SerialName("modTAF")
    val tafMode: Esse3TafMode? = null,

    /** flag per l'abilitazione al sostegno */
    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    /** flag per le regole opzionali */
    @SerialName("opzFlg")
    val optionalFlag: Int? = null,

    /** flag per le regole sovrannumerarie */
    @SerialName("sovranFlg")
    val overrideFlag: Int? = null,

    /** flag per l'abilitiazione del tesoretto sulla regola */
    @SerialName("tesorettoFlg")
    val treasureFlag: Int? = null,

    /** peso della rosa ristretta associato alla regola */
    @SerialName("pesoRosaRist")
    val restrictedListWeight: Long? = null,

    /** flag per l'abilitazione dell'assegnazione posti nella regola di scelta */
    @SerialName("assegnazionePostiFlg")
    val seatAssignmentFlag: Int? = null,

    /** flag per la generazione di una delibera per le AD nella regola */
    @SerialName("deliberaFlg")
    val resolutionFlag: Int? = null,

    /** flag per azzerare il peso delle attività della regola nell'attuazione */
    @SerialName("azzeraCfuFlg")
    val resetCreditsFlag: Int? = null,

    /** flag per la selezione dei parametri della logistica durante la compilazione del piano */
    @SerialName("parametriLogisticaFlg")
    val logisticsParametersFlag: Int? = null,

    /** Nota opzionale da visualizzare prima delle attività da scegliere */
    @SerialName("notaPre")
    val preNote: String? = null,

    /** Nota opzionale da visualizzare prima delle attività da scegliere in inglese (se presente) */
    @SerialName("notaPreEng")
    val preNoteEnglish: String? = null,

    /** Flag di visibilità per la descrizione della nota_pre del regolamento nell'area pubblica */
    @SerialName("notaPreVisPubblFlg")
    val preNotePublicVisibleFlag: Int? = null,

    /** Nota opzionale da visualizzare dopo le attività da scegliere */
    @SerialName("notaPost")
    val postNote: String? = null,

    /** Nota opzionale da visualizzare dopo le attività da scegliere in inglese (se presente) */
    @SerialName("notaPostEng")
    val postNoteEnglish: String? = null,

    /** Flag di visibilità per la descrizione della nota_post del regolamento nell'area pubblica */
    @SerialName("notaPostVisPubblFlg")
    val postNotePublicVisibleFlag: Int? = null,

    /** id univoco della regola di scelta di un eventuale sistema esterno */
    @SerialName("extId")
    val externalId: Long? = null,

    /** codice univoco della regola di scelta di un eventuale sistema esterno */
    @SerialName("extCod")
    val externalCode: String? = null,

    @SerialName("schemi")
    val schemas: List<Esse3PlanSchemaChoiceRule> = emptyList(),

    @SerialName("blocchi")
    val blocks: List<Esse3ChoiceRuleBlock> = emptyList(),

    @SerialName("condizioni")
    val conditions: List<Esse3ChoiceRuleCondition> = emptyList(),

    @SerialName("filtriOD")
    val orFilters: List<Esse3ChoiceRuleOrFilter> = emptyList()
)

@Serializable
data class Esse3PlansLinkedToWindow(
    /** codice finestra */
    @SerialName("finregsceCod")
    val finalRegulationChoiceCode: String? = null,

    /** descrizione finestra */
    @SerialName("finregsceDes")
    val finalRegulationChoiceDescription: String? = null,

    /** id finestra regolamento scelta */
    @SerialName("finregsceId")
    val finalRegulationChoiceId: Long? = null,

    /** conteggio piani collegatia lla finestra */
    @SerialName("conteggio")
    val count: Int? = null
)

@Serializable
data class Esse3PlanSchema(
    /** id univoco dello schema di piano */
    @SerialName("schemaId")
    val schemaId: Long? = null,

    /** codice dello schema di piano, univoco in base al regolamento di scelta dove è stato definito */
    @SerialName("schemaCod")
    val schemaCode: String? = null,

    /** descrizione dello schema di piano */
    @SerialName("schemaDes")
    val schemaDescription: String? = null,

    /** descrizione dello schema di piano in inglese (se presente) */
    @SerialName("schemaDesEng")
    val schemaDescriptionEnglish: String? = null,

    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** id del percorso collegato allo schema di piano */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** codice del percorso collegato allo schema di piano */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** descrizione del percorso collegato allo schema di piano */
    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    /** tipo di approvazione dello schema di piano ( 0 => piano standard ad appovazione automatica 1 => piano standard senza approvazione automatica 2 => piano standard con approvazione automatica se conforme al regolamento di scelta */
    @SerialName("tipoApprovazione")
    val approvalType: Int? = null,

    /** flag che permette di filtrare la regola di scelta in base all'anno di corso dello studente 0 => nessun controllo 1 => anni di corso minori o uguali a quello dello studente */
    @SerialName("gestAnnoCorsoFlg")
    val manageCourseYearFlag: Int? = null,

    /** flag che indica se lo schema di piano è statutatio per i parametri indicati */
    @SerialName("statutarioFlg")
    val statutoryFlag: Int? = null,

    /** data di inizio validità dello schema (DD/MM/YYYY), se nulla lo schema risulta sempre valido */
    @SerialName("dataInizioVal")
    val evaluationStartDate: String? = null,

    /** data di fine validità dello schema (DD/MM/YYYY), se nulla lo schema risulta sempre valido */
    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    /** id della condizione SQL per restringere l'accesso allo schema di piano */
    @SerialName("condId")
    val conditionId: Long? = null,

    /** codice della condizione SQL per restringere l'accesso allo schema di piano */
    @SerialName("condCod")
    val conditionCode: String? = null,

    /** descrizione della condizione SQL per restringere l'accesso allo schema di piano */
    @SerialName("condDes")
    val conditionDescription: String? = null,

    /** id dell'orientamento collegato allo schema di piano */
    @SerialName("orientId")
    val orientationId: Long? = null,

    /** codice dell'orientamento collegato allo schema di piano */
    @SerialName("orientCod")
    val orientationCode: String? = null,

    /** descrizione dell'orientamento collegato allo schema di piano */
    @SerialName("orientDes")
    val orientationDescription: String? = null,

    /** codice del prifilo studente collegato allo schema di piano */
    @SerialName("profCod")
    val professionCode: String? = null,

    /** descrizione del prifilo studente collegato allo schema di piano */
    @SerialName("profDes")
    val professionDescription: String? = null,

    /** id dell'alternativa di part-time collegata allo schema di piano */
    @SerialName("aptId")
    val aptId: Long? = null,

    /** codice dell'alternativa di part-time collegata allo schema di piano */
    @SerialName("aptCod")
    val aptCode: String? = null,

    /** descrizione dell'alternativa di part-time collegata allo schema di piano */
    @SerialName("aptDes")
    val aptDescription: String? = null,

    /** consente di visualizzare lo schema di piano nell'area pubblica */
    @SerialName("webViewFlg")
    val webViewFlag: Int? = null
)

@Serializable
data class Esse3PlanSchemaWithDetails(
    /** id univoco dello schema di piano */
    @SerialName("schemaId")
    val schemaId: Long? = null,

    /** codice dello schema di piano, univoco in base al regolamento di scelta dove è stato definito */
    @SerialName("schemaCod")
    val schemaCode: String? = null,

    /** descrizione dello schema di piano */
    @SerialName("schemaDes")
    val schemaDescription: String? = null,

    /** descrizione dello schema di piano in inglese (se presente) */
    @SerialName("schemaDesEng")
    val schemaDescriptionEnglish: String? = null,

    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** id del percorso collegato allo schema di piano */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** codice del percorso collegato allo schema di piano */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** descrizione del percorso collegato allo schema di piano */
    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    /** tipo di approvazione dello schema di piano ( 0 => piano standard ad appovazione automatica 1 => piano standard senza approvazione automatica 2 => piano standard con approvazione automatica se conforme al regolamento di scelta */
    @SerialName("tipoApprovazione")
    val approvalType: Int? = null,

    /** flag che permette di filtrare la regola di scelta in base all'anno di corso dello studente 0 => nessun controllo 1 => anni di corso minori o uguali a quello dello studente */
    @SerialName("gestAnnoCorsoFlg")
    val manageCourseYearFlag: Int? = null,

    /** flag che indica se lo schema di piano è statutatio per i parametri indicati */
    @SerialName("statutarioFlg")
    val statutoryFlag: Int? = null,

    /** data di inizio validità dello schema (DD/MM/YYYY), se nulla lo schema risulta sempre valido */
    @SerialName("dataInizioVal")
    val evaluationStartDate: String? = null,

    /** data di fine validità dello schema (DD/MM/YYYY), se nulla lo schema risulta sempre valido */
    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    /** id della condizione SQL per restringere l'accesso allo schema di piano */
    @SerialName("condId")
    val conditionId: Long? = null,

    /** codice della condizione SQL per restringere l'accesso allo schema di piano */
    @SerialName("condCod")
    val conditionCode: String? = null,

    /** descrizione della condizione SQL per restringere l'accesso allo schema di piano */
    @SerialName("condDes")
    val conditionDescription: String? = null,

    /** id dell'orientamento collegato allo schema di piano */
    @SerialName("orientId")
    val orientationId: Long? = null,

    /** codice dell'orientamento collegato allo schema di piano */
    @SerialName("orientCod")
    val orientationCode: String? = null,

    /** descrizione dell'orientamento collegato allo schema di piano */
    @SerialName("orientDes")
    val orientationDescription: String? = null,

    /** codice del prifilo studente collegato allo schema di piano */
    @SerialName("profCod")
    val professionCode: String? = null,

    /** descrizione del prifilo studente collegato allo schema di piano */
    @SerialName("profDes")
    val professionDescription: String? = null,

    /** id dell'alternativa di part-time collegata allo schema di piano */
    @SerialName("aptId")
    val aptId: Long? = null,

    /** codice dell'alternativa di part-time collegata allo schema di piano */
    @SerialName("aptCod")
    val aptCode: String? = null,

    /** descrizione dell'alternativa di part-time collegata allo schema di piano */
    @SerialName("aptDes")
    val aptDescription: String? = null,

    /** consente di visualizzare lo schema di piano nell'area pubblica */
    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    @SerialName("regoleDiScelta")
    val choiceRules: List<Esse3ChoiceRuleSchemaWithDetails> = emptyList()
)

@Serializable
data class Esse3PlanSchemaChoiceRule(
    /** id univoco della regola di scelta */
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    /** id univoco dello schema di piano */
    @SerialName("schemaId")
    val schemaId: Long? = null,

    /** codice dello schema di piano, univoco in base al regolamento di scelta dove è stato definito */
    @SerialName("schemaCod")
    val schemaCode: String? = null,

    /** ordine di visualizzazione della regola di scelta nello schema di piano */
    @SerialName("ordNum")
    val orderNumber: Int? = null,

    /** id dello slot di part-time collegato alla regola */
    @SerialName("ptSlotId")
    val ptSlotId: Long? = null,

    /** codice dello slot di part-time collegato alla regola */
    @SerialName("ptSlotCod")
    val ptSlotCode: String? = null,

    /** descrizione dello slot di part-time collegato alla regola */
    @SerialName("ptSlotDes")
    val ptSlotDescription: String? = null
)

@Serializable
data class Esse3UpdateChoiceRegulation(
    /** stato del regolamento di scelta */
    @SerialName("stato")
    val state: Esse3QuestionStateCode? = null
)

@Serializable
data class Esse3ChoiceRegulationWithDetails(
    /** id univoco di definizione del regolamento di scelta */
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    /** id univoco del dipartimento su cui è definito il regolamento di scelta */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice del dipartimento su cui è definito il regolamento di scelta */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** codice del tipo di corso su cui è definito il regolamento di scelta */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** id univoco del corso di studio su cui è definito il regolamento di scelta */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio su cui è definito il regolamento di scelta */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di studio su cui è definito il regolamento di scelta */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** id univoco dell'ordinamento di corso di studio su cui è definito il regolamento di scelta */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** anno di coorte su cui è definito il regolamento di scelta */
    @SerialName("coorte")
    val cohort: Int? = null,

    /** anno di revisione su cui è definito il regolamento di scelta */
    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    /** stato del regolamento di scelta */
    @SerialName("stato")
    val state: Esse3State2? = null,

    /** data di ultima modifica del regolamento, nel caso il regolamento sia attivo coincide con la data di ultima modifica di tutte le entità sottostanti */
    @SerialName("dataUltimaModifica")
    val lastModificationDate: String? = null,

    /** data di inserimento del regolamento */
    @SerialName("dataInserimento")
    val insertionDate: String? = null,

    @SerialName("finesteDiCompilazione")
    val compilationWindows: List<Esse3ChoiceRegulationWindow> = emptyList(),

    @SerialName("schemiDiPiano")
    val planSchemas: List<Esse3PlanSchemaWithDetails> = emptyList()
)
