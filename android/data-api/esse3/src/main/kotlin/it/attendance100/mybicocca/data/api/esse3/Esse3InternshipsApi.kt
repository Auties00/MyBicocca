package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerPortion
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyAgreementsData
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyContactData
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyData
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyLocationsData
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyPostInput
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyPostOutput
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyPutInput
import it.attendance100.mybicocca.data.dto.esse3.Esse3GenericAttachmentInsertMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3InternshipApplicationAttachments
import it.attendance100.mybicocca.data.dto.esse3.Esse3InternshipApplicationDetail
import it.attendance100.mybicocca.data.dto.esse3.Esse3InternshipApplicationHeader
import it.attendance100.mybicocca.data.dto.esse3.Esse3InternshipApplicationQuestionnaireData
import it.attendance100.mybicocca.data.dto.esse3.Esse3OpportunityData
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudentInternshipEligibilityData
import it.attendance100.mybicocca.data.dto.esse3.Esse3TrainingProject
import kotlinx.serialization.json.Json

class Esse3InternshipsApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/tirocini-service-v1") {

    /**
     * matricole per cui è possibile avere una domanda di tirocinio
     *
     * @param matricola codice della matricola dello studente
     * @param courseOfStudyStudentId id del corso di studio di appartenenza dello studente
     * @param courseOfStudyStudentCode codice del corso di studio di appartenenza dello studente
     * @param academicYearOrderStudentId anno di ordinamento del regolamento del piano
     * @param studyPlanStudentId id del percorso di studio di appartenenza dello studente
     * @param studyPlanStudentCode codice del percorso di studio di appartenenza dello studente
     * @param optionalFields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param start utilizzato insieme a `limit` per indicare la paginazione sui record; `start` indica il numero del primo record da caricare (se non indicato viene utilizzato 0)
     * @param limit utilizzato insieme a `start` per indicare la paginazione sui record, `limit` indica il numero di record da recuperare (a partire da `start`); se non indicato viene utilizzato 50, i valori consentiti vanno da 0 a 100
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getCareerSegments(
        matricola: String? = null,
        courseOfStudyStudentId: Long? = null,
        courseOfStudyStudentCode: String? = null,
        academicYearOrderStudentId: Int? = null,
        studyPlanStudentId: Long? = null,
        studyPlanStudentCode: String? = null,
        optionalFields: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3CareerPortion> {
        return executeJsonGetList<Esse3CareerPortion>("/tirocini", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT)) {
            matricola?.let { parameter("matricola", it) }
            courseOfStudyStudentId?.let { parameter("cdsStuId", it) }
            courseOfStudyStudentCode?.let { parameter("cdsStuCod", it) }
            academicYearOrderStudentId?.let { parameter("aaOrdStuId", it) }
            studyPlanStudentId?.let { parameter("pdsStuId", it) }
            studyPlanStudentCode?.let { parameter("pdsStuCod", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * esegue l'inserimento dell'azienda e restituisce l'ID azienda e l'ID della struttura didattica
     *
     * @param body dati azienda
     */
    suspend fun saveCompany(
        body: Esse3CompanyPostInput
    ): Esse3CompanyPostOutput {
        return executeJsonPost<Esse3CompanyPostOutput>("/tirocini/azienda", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * esegue la modifica dell'azienda
     *
     * @param companyId Identificativo dell'azienda
     * @param body dati azienda
     */
    suspend fun updateCompany(
        companyId: Long,
        body: Esse3CompanyPutInput
    ) {
        val response = executePut("/tirocini/azienda/${companyId}") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * esegue la eliminazione dell'azienda
     *
     * @param companyId Identificativo dell'azienda
     */
    suspend fun deleteCompany(
        companyId: Long
    ) {
        val response = executeDelete("/tirocini/azienda/${companyId}")
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupera la lista dei contatti per una data azienda
     *
     * @param companyId Identificativo dell'azienda
     * @param surname cognome del contatto
     * @param name nome del contatto
     * @param matFiscalCode Matricola/Codice Fiscale del contatto.
     * @param activeFlag Indica se il contatto è attivo.
     * @param role Ruolo del contatto.
     */
    suspend fun getCompanyContacts(
        companyId: Long,
        surname: String? = null,
        name: String? = null,
        matFiscalCode: String? = null,
        activeFlag: Int? = null,
        role: String? = null
    ): List<Esse3CompanyContactData> {
        return executeJsonGetList<Esse3CompanyContactData>("/tirocini/azienda/${companyId}/contatti", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            surname?.let { parameter("cognome", it) }
            name?.let { parameter("nome", it) }
            matFiscalCode?.let { parameter("matCodfis", it) }
            activeFlag?.let { parameter("attivoFlg", it) }
            role?.let { parameter("ruolo", it) }
        }
    }

    /**
     * recupera la lista delle convenzioni per una data azienda
     *
     * @param companyId Identificativo dell'azienda
     * @param conventionSiteDescription Descrizione della convenzione.
     * @param conventionStateCode codice dello stato della convenzione ('P-Proposta', 'I-istituita', 'X-Cessata', 'R-Rifiutata').
     * @param startDate Data di inizio convenzione (dd/mm/yyyy).
     * @param endDate Data di fine convenzione. E' possibile inserire anche l'ora (dd/mm/yyyy hh:mm:ss).
     * @param durationYears Durata in anni della convenzione
     * @param academicYearId Anno accademico di validità della convenzione.
     * @param defaultFlag Dice se è la convenzione di default per una certa azienda (ente_est). Utilizzata per gestire le convenzioni fittizie legate a opportunità per le quali non serve la convenzione e per gestire i PF associati a convenzioni uninominali.
     */
    suspend fun getCompanyConventions(
        companyId: Long,
        conventionSiteDescription: String? = null,
        conventionStateCode: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        durationYears: Int? = null,
        academicYearId: Int? = null,
        defaultFlag: Int? = null
    ): List<Esse3CompanyAgreementsData> {
        return executeJsonGetList<Esse3CompanyAgreementsData>("/tirocini/azienda/${companyId}/convenzioni", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            conventionSiteDescription?.let { parameter("sdrCnvzDes", it) }
            conventionStateCode?.let { parameter("statoCnvzCod", it) }
            startDate?.let { parameter("dataInizio", it) }
            endDate?.let { parameter("dataFine", it) }
            durationYears?.let { parameter("durataAnni", it) }
            academicYearId?.let { parameter("aaId", it) }
            defaultFlag?.let { parameter("defaultFlg", it) }
        }
    }

    /**
     * recupera la lista delle sedi per una data azienda
     *
     * @param companyId Identificativo dell'azienda
     * @param companySiteDescription descrizione della sede.
     * @param siteTypeCode codice del tipo di sede ('LEG-Sede Legale', 'OPE-Sede Operativa', 'PER-Periferica')
     * @param city Citta della sede.
     * @param nationId id della nazione
     * @param deactivate Sede disattivata.
     */
    suspend fun getCompanySites(
        companyId: Long,
        companySiteDescription: String? = null,
        siteTypeCode: String? = null,
        city: String? = null,
        nationId: Long? = null,
        deactivate: Int? = null
    ): List<Esse3CompanyLocationsData> {
        return executeJsonGetList<Esse3CompanyLocationsData>("/tirocini/azienda/${companyId}/sedi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            companySiteDescription?.let { parameter("sedeAziendaDes", it) }
            siteTypeCode?.let { parameter("tipoSedeCod", it) }
            city?.let { parameter("citta", it) }
            nationId?.let { parameter("nazioneId", it) }
            deactivate?.let { parameter("disattiva", it) }
        }
    }

    /**
     * recupera la lista delle aziende utilizzabili per i tirocini
     *
     * @param companyCode codice dell'azienda
     * @param company descrizione dell'azienda
     * @param fiscalCode codice fiscale azienda
     * @param vatNumber partita iva azienda
     * @param groupVatNumber partita iva di gruppo
     * @param companyTypeCode codice della tipologia dell'azienda
     * @param companyStateCode codice dello stato azienda (A-Accreditato, B-Bozza, BL-Blacklist, C-Cessato, P-Proposta di accredito, R-Accredito rifiutato)
     * @param duns codice duns di 9 cifre
     * @param hasValidConvention Indica se l´azienda ha una convenzione attiva.
     * @param hasValidOpportunity Indica se l´azienda ha una opportunità attiva.
     */
    suspend fun getInternshipCompanies(
        companyCode: String? = null,
        company: String? = null,
        fiscalCode: String? = null,
        vatNumber: String? = null,
        groupVatNumber: String? = null,
        companyTypeCode: String? = null,
        companyStateCode: String? = null,
        duns: String? = null,
        hasValidConvention: Int? = null,
        hasValidOpportunity: Int? = null
    ): List<Esse3CompanyData> {
        return executeJsonGetList<Esse3CompanyData>("/tirocini/aziende", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            companyCode?.let { parameter("aziendaCod", it) }
            company?.let { parameter("azienda", it) }
            fiscalCode?.let { parameter("cf", it) }
            vatNumber?.let { parameter("piva", it) }
            groupVatNumber?.let { parameter("pivaGruppo", it) }
            companyTypeCode?.let { parameter("tipoAziendaCod", it) }
            companyStateCode?.let { parameter("statoAziendaCod", it) }
            duns?.let { parameter("duns", it) }
            hasValidConvention?.let { parameter("hasValidCnvz", it) }
            hasValidOpportunity?.let { parameter("hasValidOpportunita", it) }
        }
    }

    /**
     * Effettua il controllo che uno studente sia eligibile per il tipo di stage passato tramite matricola, codice fiscale ed altri parametri
     *
     * @param fiscalCode codice fiscale
     * @param languageCode codice lingua
     * @param matricola codice della matricola dello studente
     * @param serviceType tipo del servizio per cui si richiede il controllo
     * @param internshipStartDate data di inizio tirocinio
     */
    suspend fun checkStudentInternshipEligibility(
        fiscalCode: String,
        languageCode: List<String>,
        matricola: String? = null,
        serviceType: List<String>? = null,
        internshipStartDate: String? = null
    ): List<Esse3StudentInternshipEligibilityData> {
        return executeJsonGetList<Esse3StudentInternshipEligibilityData>("/tirocini/checkEligibilitaStageStu", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            matricola?.let { parameter("matricola", it) }
            parameter("codFisc", fiscalCode)
            serviceType?.let { parameter("tipoServizio", it) }
            internshipStartDate?.let { parameter("dataIniTiro", it) }
            parameter("codLingua", languageCode)
        }
    }

    /**
     * esegue l’inserimento del tirocinio; se non esiste viene creato, se esiste viene sostituito integralmente con cancellazione e reinserimento.
     *
     * @param body dati tirocinio
     */
    suspend fun importInternship(
        body: Esse3TrainingProject
    ) {
        val response = executePut("/tirocini/import") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupera la lista delle opportunità disponibili in base al filtro applicato
     *
     * @param area codice della tipologia del tipo di settore
     * @param disciplinaryAreaId id area di afferenza valida per una determinata convenzione
     * @param geographicAreaCode codice dell'area geografica
     * @param company descrizione dell'azienda
     * @param campaignId id della campagna
     * @param atecoCategoryId id dell'attività economica
     * @param protectedCategoryFlag indica se filtrare per categorie protette o no
     * @param enrollmentEndDateTo data di fine iscrizione a partire a
     * @param enrollmentEndDateFrom data di fine iscrizione a partire da
     * @param enrollmentStartDateTo data di inizio iscrizione a partire a
     * @param enrollmentStartDateFrom data di inizio iscrizione a partire da
     * @param description descrizione opportunità
     * @param entityId id dell'ente esterno
     * @param excludeOpportunityCampaign indica se filtrare le opportunità legate a campagne
     * @param nation Italia=0 /Estero = 1/Tutte = 2
     * @param nationId id della nazione
     * @param provinceCode codice della provincia
     * @param requiredCodeFlag indica se filtrare le opportunità con una condizione sql associata
     * @param requiredObjective testo di un requisito da ricercare
     * @param sectorDisciplinaryAreaId Identificativo del settore legato all'area
     * @param atecoSectorId Identificativo del settore dell'attività economica
     * @param sector id dell'attività economica dell'ente sterno
     * @param text testo da ricercare nel titolo e descrizione dell'opportunità o ragione sociale dell'ente esterno
     * @param internshipTypeCode codice della tipologia del tirocinio
     * @param title testo da ricercare nel titolo dell'opportunità
     * @param expiredOpportunitiesVisible indica se filtrare le opportunità scadute
     * @param durationFrom durata in mesi a partire da
     * @param durationTo durata in mesi fino a
     * @param internshipStartDateFrom data di inizio tirocinio a partire da
     * @param internshipStartDateTo data di inizio tirocinio fino a
     */
    suspend fun getInternshipOpportunities(
        area: String? = null,
        disciplinaryAreaId: Long? = null,
        geographicAreaCode: String? = null,
        company: String? = null,
        campaignId: Long? = null,
        atecoCategoryId: Long? = null,
        protectedCategoryFlag: Int? = null,
        enrollmentEndDateTo: String? = null,
        enrollmentEndDateFrom: String? = null,
        enrollmentStartDateTo: String? = null,
        enrollmentStartDateFrom: String? = null,
        description: String? = null,
        entityId: Long? = null,
        excludeOpportunityCampaign: Int? = null,
        nation: Int? = null,
        nationId: Long? = null,
        provinceCode: String? = null,
        requiredCodeFlag: Int? = null,
        requiredObjective: String? = null,
        sectorDisciplinaryAreaId: Long? = null,
        atecoSectorId: Long? = null,
        sector: Long? = null,
        text: String? = null,
        internshipTypeCode: String? = null,
        title: String? = null,
        expiredOpportunitiesVisible: Int? = null,
        durationFrom: Int? = null,
        durationTo: Int? = null,
        internshipStartDateFrom: String? = null,
        internshipStartDateTo: String? = null
    ): List<Esse3OpportunityData> {
        return executeJsonGetList<Esse3OpportunityData>("/tirocini/opportunita", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            area?.let { parameter("area", it) }
            disciplinaryAreaId?.let { parameter("areaDiscId", it) }
            geographicAreaCode?.let { parameter("areaGeograficaCod", it) }
            company?.let { parameter("azienda", it) }
            campaignId?.let { parameter("campagnaId", it) }
            atecoCategoryId?.let { parameter("catAtecoId", it) }
            protectedCategoryFlag?.let { parameter("catProtettaFlg", it) }
            enrollmentEndDateTo?.let { parameter("dataFinIscrA", it) }
            enrollmentEndDateFrom?.let { parameter("dataFinIscrDa", it) }
            enrollmentStartDateTo?.let { parameter("dataIniIscrA", it) }
            enrollmentStartDateFrom?.let { parameter("dataIniIscrDa", it) }
            description?.let { parameter("descr", it) }
            entityId?.let { parameter("enteId", it) }
            excludeOpportunityCampaign?.let { parameter("esclOppCamp", it) }
            nation?.let { parameter("nazione", it) }
            nationId?.let { parameter("nazioneId", it) }
            provinceCode?.let { parameter("provCod", it) }
            requiredCodeFlag?.let { parameter("reqCodFlg", it) }
            requiredObjective?.let { parameter("reqObiett", it) }
            sectorDisciplinaryAreaId?.let { parameter("settAreaDiscId", it) }
            atecoSectorId?.let { parameter("settAtecoId", it) }
            sector?.let { parameter("settore", it) }
            text?.let { parameter("testo", it) }
            internshipTypeCode?.let { parameter("tipoTirocCod", it) }
            title?.let { parameter("title", it) }
            expiredOpportunitiesVisible?.let { parameter("visOppScadute", it) }
            durationFrom?.let { parameter("durataDa", it) }
            durationTo?.let { parameter("durataA", it) }
            internshipStartDateFrom?.let { parameter("dataIniTiroDa", it) }
            internshipStartDateTo?.let { parameter("dataIniTiroA", it) }
        }
    }

    /**
     * inserimento metadati allegato
     *
     * @param studentId id della carriera dello studente
     * @param body Oggetto che contiene i metadati dell'allegato da inserire
     */
    suspend fun postInternshipApplicationAttachmentMetadata(
        studentId: Long,
        body: Esse3GenericAttachmentInsertMetadata
    ) {
        val response = executePost("/tirocini/${studentId}/allegati/") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ensureSuccess(response, setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupera il blob dell'allegato richiesto
     *
     * @param studentId id della carriera dello studente
     * @param attachmentId id di upload per caricare un allegato
     */
    suspend fun getAttachmentContent(
        studentId: Long,
        attachmentId: Long
    ): ByteReadChannel {
        return executeStreamGet("/tirocini/${studentId}/allegati/${attachmentId}/blob", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    /**
     * recupera le testate della domanda di tirocinio di uno studente
     *
     * @param studentId id della carriera dello studente
     * @param internshipApplicationStateCode codice dello stato della domanda di tirocinio piano (PRE => Presentata, CHI => Chiusa, ANN => Annullata, CON => Confermata, RIF => Rifiutata, AVV => Avviato, NAS => Non assegnato). Se non viene passato vengono recuperate le domande in stato PRE, CON e AVV.
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getStudentInternshipApplicationHeaders(
        studentId: Long,
        internshipApplicationStateCode: List<String>? = null,
        order: String? = null,
        fields: String? = null
    ): List<Esse3InternshipApplicationHeader> {
        return executeJsonGetList<Esse3InternshipApplicationHeader>("/tirocini/${studentId}/domande", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            internshipApplicationStateCode?.let { parameter("statoDomTiroCod", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * recupera il dettaglio della domanda di tirocinio di uno studente
     *
     * @param studentId id della carriera dello studente
     * @param domicileInternshipId id della testata della domanda di tirocinio dello studente
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     */
    suspend fun getStudentInternshipApplication(
        studentId: Long,
        domicileInternshipId: Long,
        fields: String? = null
    ): Esse3InternshipApplicationDetail {
        return executeJsonGet<Esse3InternshipApplicationDetail>("/tirocini/${studentId}/domande/${domicileInternshipId}", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
        }
    }

    /**
     * recupera gli allegati associati alla domanda di tirocinio di uno studente
     *
     * @param studentId id della carriera dello studente
     * @param domicileInternshipId id della testata della domanda di tirocinio dello studente
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     */
    suspend fun getStudentInternshipApplicationAttachments(
        studentId: Long,
        domicileInternshipId: Long,
        fields: String? = null,
        filter: String? = null
    ): List<Esse3InternshipApplicationAttachments> {
        return executeJsonGetList<Esse3InternshipApplicationAttachments>("/tirocini/${studentId}/domande/${domicileInternshipId}/allegati", setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    /**
     * recupera il dettaglio della scheda di valutazione compilata da uno studente
     *
     * @param studentId id della carriera dello studente
     * @param domicileInternshipId id della testata della domanda di tirocinio dello studente
     * @param questionTypeCode codice del tipo di valutazione
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     */
    suspend fun getStudentInternshipEvaluation(
        studentId: Long,
        domicileInternshipId: Long,
        questionTypeCode: List<String>? = null,
        order: String? = null
    ): List<Esse3InternshipApplicationQuestionnaireData> {
        return executeJsonGetList<Esse3InternshipApplicationQuestionnaireData>("/tirocini/${studentId}/valutazioni/${domicileInternshipId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            questionTypeCode?.let { parameter("tipoQuestCod", it) }
            order?.let { parameter("order", it) }
        }
    }
}
