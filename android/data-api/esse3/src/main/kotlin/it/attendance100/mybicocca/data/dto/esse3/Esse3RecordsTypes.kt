package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3ImportBatch(
    @SerialName("nomeFileBatch")
    val batchFileName: String? = null,

    @SerialName("batchNum")
    val batchNumber: Int? = null,

    @SerialName("tipo")
    val type: String,

    @SerialName("verbaliInserimento")
    val insertionMinutes: List<Esse3InsertionRecordImport> = emptyList(),

    @SerialName("verbaliAggiornamento")
    val updateMinutes: List<Esse3UpdateRecordImport> = emptyList()
)

@Serializable
data class Esse3BatchRecord(
    @SerialName("batchId")
    val batchId: Long? = null,

    @SerialName("batchNum")
    val batchNumber: Long? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("dataAcq")
    val acquisitionDate: String? = null,

    @SerialName("dataApp")
    val callDate: String? = null
)

@Serializable
data class Esse3BatchWithDetails(
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("tipoGestAppCod")
    val callManagementTypeCode: String? = null,

    @SerialName("statoLotto")
    val batchState: String? = null,

    @SerialName("statoLottoDes")
    val batchStateDescription: String? = null,

    @SerialName("dataApp")
    val callDate: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("docenteCodFis")
    val lecturerFiscalCode: String? = null,

    @SerialName("linguaId")
    val languageId: Long? = null,

    @SerialName("linguaCod")
    val languageCode: String? = null,

    @SerialName("motivoRifirma")
    val resigningReason: String? = null,

    @SerialName("progRifirma")
    val resigningProgram: Int? = null,

    @SerialName("commissione")
    val committee: List<Esse3CommissionBatch> = emptyList(),

    @SerialName("transizioniStato")
    val stateTransitions: List<Esse3BatchTransactionStatus> = emptyList()
)

@Serializable
data class Esse3RecordRoot(
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    @SerialName("verbId")
    val minutesId: Long? = null,

    @SerialName("batchId")
    val batchId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("appId")
    val callId: Long? = null,

    @SerialName("appLogId")
    val callLogId: Long? = null,

    @SerialName("verbNum")
    val minutesNumber: String? = null,

    @SerialName("statoVerbale")
    val minutesState: Int? = null,

    @SerialName("errNum")
    val errorNumber: Int? = null,

    @SerialName("statoWarn")
    val warningState: Int? = null,

    @SerialName("warnNum")
    val warningNumber: Int? = null,

    @SerialName("tipoVerbCod")
    val minutesTypeCode: String? = null,

    @SerialName("lottoCollId")
    val batchCollectionId: Long? = null,

    @SerialName("verbCollId")
    val minutesCollectionId: Long? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("adStuId")
    val studentActivityId: Long? = null,

    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    @SerialName("voto")
    val grade: Int? = null,

    @SerialName("causale")
    val reason: Int? = null,

    @SerialName("dataEsa")
    val graduationDate: String? = null,

    @SerialName("dataApp")
    val callDate: String? = null,

    @SerialName("livelloUscitaLinguaCod")
    val exitLanguageLevelCode: String? = null,

    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    @SerialName("imgId")
    val imageId: Long? = null
)

@Serializable
data class Esse3BatchRecordWithDetails(
    @SerialName("batchId")
    val batchId: Long? = null,

    @SerialName("batchNum")
    val batchNumber: Long? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("dataAcq")
    val acquisitionDate: String? = null,

    @SerialName("dataApp")
    val callDate: String? = null,

    @SerialName("verbali")
    val minutes: List<Esse3RecordWithUploadUrl> = emptyList()
)

@Serializable
data class Esse3AdditionalFieldRecordImport(
    @SerialName("nome")
    val name: String,

    @SerialName("valore")
    val value: String
)

@Serializable
data class Esse3RecordWithDetails(
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    @SerialName("verbId")
    val minutesId: Long? = null,

    @SerialName("batchId")
    val batchId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("appId")
    val callId: Long? = null,

    @SerialName("appLogId")
    val callLogId: Long? = null,

    @SerialName("verbNum")
    val minutesNumber: String? = null,

    @SerialName("statoVerbale")
    val minutesState: Int? = null,

    @SerialName("errNum")
    val errorNumber: Int? = null,

    @SerialName("statoWarn")
    val warningState: Int? = null,

    @SerialName("warnNum")
    val warningNumber: Int? = null,

    @SerialName("tipoVerbCod")
    val minutesTypeCode: String? = null,

    @SerialName("lottoCollId")
    val batchCollectionId: Long? = null,

    @SerialName("verbCollId")
    val minutesCollectionId: Long? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("adStuId")
    val studentActivityId: Long? = null,

    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    @SerialName("voto")
    val grade: Int? = null,

    @SerialName("causale")
    val reason: Int? = null,

    @SerialName("dataEsa")
    val graduationDate: String? = null,

    @SerialName("dataApp")
    val callDate: String? = null,

    @SerialName("livelloUscitaLinguaCod")
    val exitLanguageLevelCode: String? = null,

    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    @SerialName("imgId")
    val imageId: Long? = null,

    @SerialName("modifiche")
    val modifications: List<Esse3RecordModificationLog> = emptyList(),

    @SerialName("applistaId")
    val applicationListId: Long? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("adregId")
    val activityRegulationId: Long? = null,

    @SerialName("domandeEsame")
    val examApplications: String? = null,

    @SerialName("errDes")
    val errorDescription: String? = null,

    @SerialName("warnDes")
    val warningDescription: String? = null,

    @SerialName("tipoVerbDes")
    val minutesTypeDescription: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    @SerialName("docenteCodFis")
    val lecturerFiscalCode: String? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("cfu")
    val credits: Float? = null,

    @SerialName("esito")
    val outcome: String? = null
)

@Serializable
data class Esse3InsertionRecordImport(
    @SerialName("matricola")
    val matricola: String,

    @SerialName("dataEsa")
    val graduationDate: String,

    @SerialName("dataApp")
    val callDate: String,

    @SerialName("voto")
    val grade: Int,

    @SerialName("causale")
    val reason: Int? = null,

    @SerialName("blobFileName")
    val blobFileName: String? = null,

    @SerialName("correzioni")
    val corrections: List<Esse3CorrectionRecordImport> = emptyList(),

    @SerialName("campiAggiuntivi")
    val additionalFields: List<Esse3AdditionalFieldRecordImport> = emptyList(),

    @SerialName("adStuCod")
    val studentActivityCode: String,

    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String
)

@Serializable
data class Esse3UpdateRecordImport(
    @SerialName("matricola")
    val matricola: String,

    @SerialName("dataEsa")
    val graduationDate: String,

    @SerialName("dataApp")
    val callDate: String,

    @SerialName("voto")
    val grade: Int,

    @SerialName("causale")
    val reason: Int? = null,

    @SerialName("blobFileName")
    val blobFileName: String? = null,

    @SerialName("correzioni")
    val corrections: List<Esse3CorrectionRecordImport> = emptyList(),

    @SerialName("campiAggiuntivi")
    val additionalFields: List<Esse3AdditionalFieldRecordImport> = emptyList(),

    @SerialName("lottoId")
    val lotBatchId: Long,

    @SerialName("verbId")
    val minutesId: Long,

    @SerialName("verbNum")
    val minutesNumber: String,

    @SerialName("cognome")
    val surname: String,

    @SerialName("nome")
    val name: String,

    @SerialName("adCod")
    val activityCode: String,

    @SerialName("cdsCod")
    val courseOfStudyCode: String,

    @SerialName("adDes")
    val activityDescription: String,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String
)

@Serializable
data class Esse3Batch(
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("tipoGestAppCod")
    val callManagementTypeCode: String? = null,

    @SerialName("statoLotto")
    val batchState: String? = null,

    @SerialName("statoLottoDes")
    val batchStateDescription: String? = null,

    @SerialName("dataApp")
    val callDate: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("docenteCodFis")
    val lecturerFiscalCode: String? = null,

    @SerialName("linguaId")
    val languageId: Long? = null,

    @SerialName("linguaCod")
    val languageCode: String? = null,

    @SerialName("motivoRifirma")
    val resigningReason: String? = null,

    @SerialName("progRifirma")
    val resigningProgram: Int? = null
)

@Serializable
data class Esse3CorrectionRecordImport(
    @SerialName("campo")
    val field: String,

    @SerialName("valoreVecchio")
    val oldValue: String,

    @SerialName("valoreNuovo")
    val newValue: String,

    @SerialName("dataModifica")
    val modificationDate: String,

    @SerialName("utente")
    val user: String
)

@Serializable
data class Esse3CommissionBatch(
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("ruoloCod")
    val roleCode: String? = null,

    @SerialName("ruoloDes")
    val roleDescription: String? = null,

    @SerialName("ordineVisNum")
    val orderVisibleNumber: Int? = null
)

@Serializable
data class Esse3BatchTransactionStatus(
    @SerialName("lottoTransStatoId")
    val batchTransactionStateId: Long? = null,

    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    @SerialName("statoLottoOld")
    val oldBatchState: String? = null,

    @SerialName("statoLottoNew")
    val newBatchState: String? = null,

    @SerialName("statoLottoOldDes")
    val oldBatchStateDescription: String? = null,

    @SerialName("statoLottoNewDes")
    val newBatchStateDescription: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null
)

@Serializable
data class Esse3ResultRecordImport(
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    @SerialName("verbId")
    val minutesId: Long? = null,

    @SerialName("batchId")
    val batchId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("appId")
    val callId: Long? = null,

    @SerialName("appLogId")
    val callLogId: Long? = null,

    @SerialName("verbNum")
    val minutesNumber: String? = null,

    @SerialName("statoVerbale")
    val minutesState: Int? = null,

    @SerialName("errNum")
    val errorNumber: Int? = null,

    @SerialName("statoWarn")
    val warningState: Int? = null,

    @SerialName("warnNum")
    val warningNumber: Int? = null,

    @SerialName("tipoVerbCod")
    val minutesTypeCode: String? = null,

    @SerialName("lottoCollId")
    val batchCollectionId: Long? = null,

    @SerialName("verbCollId")
    val minutesCollectionId: Long? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("adStuId")
    val studentActivityId: Long? = null,

    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    @SerialName("voto")
    val grade: Int? = null,

    @SerialName("causale")
    val reason: Int? = null,

    @SerialName("dataEsa")
    val graduationDate: String? = null,

    @SerialName("dataApp")
    val callDate: String? = null,

    @SerialName("livelloUscitaLinguaCod")
    val exitLanguageLevelCode: String? = null,

    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    @SerialName("imgId")
    val imageId: Long? = null,

    @SerialName("risultatoImport")
    val importResult: Boolean? = null,

    @SerialName("risultatoImportErrMsg")
    val importResultErrorMessage: String? = null
)

@Serializable
data class Esse3RecordWithUploadUrl(
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    @SerialName("verbId")
    val minutesId: Long? = null,

    @SerialName("batchId")
    val batchId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("appId")
    val callId: Long? = null,

    @SerialName("appLogId")
    val callLogId: Long? = null,

    @SerialName("verbNum")
    val minutesNumber: String? = null,

    @SerialName("statoVerbale")
    val minutesState: Int? = null,

    @SerialName("errNum")
    val errorNumber: Int? = null,

    @SerialName("statoWarn")
    val warningState: Int? = null,

    @SerialName("warnNum")
    val warningNumber: Int? = null,

    @SerialName("tipoVerbCod")
    val minutesTypeCode: String? = null,

    @SerialName("lottoCollId")
    val batchCollectionId: Long? = null,

    @SerialName("verbCollId")
    val minutesCollectionId: Long? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("adStuId")
    val studentActivityId: Long? = null,

    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    @SerialName("voto")
    val grade: Int? = null,

    @SerialName("causale")
    val reason: Int? = null,

    @SerialName("dataEsa")
    val graduationDate: String? = null,

    @SerialName("dataApp")
    val callDate: String? = null,

    @SerialName("livelloUscitaLinguaCod")
    val exitLanguageLevelCode: String? = null,

    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    @SerialName("imgId")
    val imageId: Long? = null,

    @SerialName("uploadUrl")
    val uploadUrl: String? = null
)

@Serializable
data class Esse3RecordModificationLog(
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    @SerialName("verbId")
    val minutesId: Long? = null,

    @SerialName("verbOrigLogId")
    val originalLogMinutesId: Long? = null,

    @SerialName("verbOrigLogDes")
    val originalLogMinutesDescription: String? = null,

    @SerialName("origineLogCod")
    val logOriginCode: String? = null,

    @SerialName("valoreVecchio")
    val oldValue: String? = null,

    @SerialName("valoreNuovo")
    val newValue: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null
)

@Serializable
data class Esse3RecordImportRoot(
    @SerialName("matricola")
    val matricola: String,

    @SerialName("dataEsa")
    val graduationDate: String,

    @SerialName("dataApp")
    val callDate: String,

    @SerialName("voto")
    val grade: Int,

    @SerialName("causale")
    val reason: Int? = null,

    @SerialName("blobFileName")
    val blobFileName: String? = null,

    @SerialName("correzioni")
    val corrections: List<Esse3CorrectionRecordImport> = emptyList(),

    @SerialName("campiAggiuntivi")
    val additionalFields: List<Esse3AdditionalFieldRecordImport> = emptyList()
)

@Serializable
data class Esse3RecordsImportResponse(
    @SerialName("esito")
    val outcome: Boolean? = null,

    @SerialName("batchCorretti")
    val correctBatches: List<Esse3BatchRecordWithDetails> = emptyList(),

    @SerialName("verbaliErrati")
    val erroneousMinutes: List<Esse3ResultRecordImport> = emptyList(),

    @SerialName("logElaborazione")
    val processingLog: List<String> = emptyList()
)

@Serializable
data class Esse3Record(
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    @SerialName("verbId")
    val minutesId: Long? = null,

    @SerialName("batchId")
    val batchId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("appId")
    val callId: Long? = null,

    @SerialName("appLogId")
    val callLogId: Long? = null,

    @SerialName("verbNum")
    val minutesNumber: String? = null,

    @SerialName("statoVerbale")
    val minutesState: Int? = null,

    @SerialName("errNum")
    val errorNumber: Int? = null,

    @SerialName("statoWarn")
    val warningState: Int? = null,

    @SerialName("warnNum")
    val warningNumber: Int? = null,

    @SerialName("tipoVerbCod")
    val minutesTypeCode: String? = null,

    @SerialName("lottoCollId")
    val batchCollectionId: Long? = null,

    @SerialName("verbCollId")
    val minutesCollectionId: Long? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("adStuId")
    val studentActivityId: Long? = null,

    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    @SerialName("voto")
    val grade: Int? = null,

    @SerialName("causale")
    val reason: Int? = null,

    @SerialName("dataEsa")
    val graduationDate: String? = null,

    @SerialName("dataApp")
    val callDate: String? = null,

    @SerialName("livelloUscitaLinguaCod")
    val exitLanguageLevelCode: String? = null,

    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    @SerialName("imgId")
    val imageId: Long? = null,

    @SerialName("esito")
    val outcome: String? = null,

    @SerialName("cfu")
    val credits: Float? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("docenteCodFis")
    val lecturerFiscalCode: String? = null,

    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("tipoVerbDes")
    val minutesTypeDescription: String? = null,

    @SerialName("warnDes")
    val warningDescription: String? = null,

    @SerialName("errDes")
    val errorDescription: String? = null,

    @SerialName("domandeEsame")
    val examApplications: String? = null,

    @SerialName("adregId")
    val activityRegulationId: Long? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("applistaId")
    val applicationListId: Long? = null
)
