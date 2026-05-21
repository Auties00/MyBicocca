package it.attendance100.mybicocca.data.remote.esse3.api

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.exception.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Esse3RecordsApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3RecordsApiTest::class.java.name)
        private lateinit var userPermissions: Set<Esse3PermissionLevel>
    }

    @BeforeAll
    fun setUp() = runTest {
        logger.info("Logging in to derive user permissions")
        val loginResult = api.auth.login()
        logger.info("Login successful: userId=${loginResult.user.userId}, groupId=${loginResult.user.groupId}")

        val permissions = mutableSetOf(
            Esse3PermissionLevel.ANY,
            Esse3PermissionLevel.AUTHENTICATED_USER,
            Esse3PermissionLevel.fromGroupId(loginResult.user.groupId)
        )
        for (profile in loginResult.profiles) {
            val description = profile.description
            if (description != null) {
                permissions.add(Esse3PermissionLevel.fromProfileName(description))
            }
        }
        userPermissions = permissions
        logger.info("Derived user permissions: $userPermissions")
    }

    @Test
    fun testGetBatches() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetBatches: calling getBatches() with defaults")
            val defaultResult = api.records.getBatches()
            logger.info("testGetBatches: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getBatches() default result should not be null")
            for (item in defaultResult) {
                logger.info("testGetBatches: batchId=${item.batchId}, batchNumber=${item.batchNumber}, activityCode=${item.activityCode}, activityDescription=${item.activityDescription}, courseOfStudyCode=${item.courseOfStudyCode}, acquisitionDate=${item.acquisitionDate}, callDate=${item.callDate}")
            }

            logger.info("testGetBatches: calling getBatches() with pagination start=0, limit=5")
            val paginatedResult = api.records.getBatches(start = 0, limit = 5)
            logger.info("testGetBatches: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            if (defaultResult.isNotEmpty()) {
                val first = defaultResult.first()

                if (first.activityCode != null) {
                    logger.info("testGetBatches: calling getBatches() with activityCode=${first.activityCode}")
                    val filteredByActivity = api.records.getBatches(activityCode = first.activityCode)
                    logger.info("testGetBatches: filtered by activityCode result size=${filteredByActivity.size}")
                    assertNotNull(filteredByActivity, "getBatches() filtered by activityCode should not be null")
                }

                if (first.courseOfStudyCode != null) {
                    logger.info("testGetBatches: calling getBatches() with courseOfStudyCode=${first.courseOfStudyCode}")
                    val filteredByCourse = api.records.getBatches(courseOfStudyCode = first.courseOfStudyCode)
                    logger.info("testGetBatches: filtered by courseOfStudyCode result size=${filteredByCourse.size}")
                    assertNotNull(filteredByCourse, "getBatches() filtered by courseOfStudyCode should not be null")
                }
            }

            logger.info("testGetBatches: calling getBatches() with order=adCod")
            val orderedResult = api.records.getBatches(order = "adCod")
            logger.info("testGetBatches: ordered result size=${orderedResult.size}")
            assertNotNull(orderedResult, "getBatches() with order should not be null")
        } else {
            logger.info("testGetBatches: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.records.getBatches()
            }
            logger.info("testGetBatches: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testImportMinutes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testImportMinutes: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testImportMinutes: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.records.importMinutes(
                    body = it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ImportBatch(
                        type = "INS"
                    )
                )
            }
            logger.info("testImportMinutes: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testUploadMinutesBlob() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testUploadMinutesBlob: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testUploadMinutesBlob: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.records.uploadMinutesBlob(
                    uploadId = 1L,
                    body = kotlinx.serialization.json.JsonObject(emptyMap())
                )
            }
            logger.info("testUploadMinutesBlob: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetBatchById() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetBatchById: fetching batches list to derive a batchId")
            val batches = api.records.getBatches(start = 0, limit = 5)
            logger.info("testGetBatchById: fetched ${batches.size} batches")

            if (batches.isNotEmpty()) {
                val batchId = batches.first().batchId
                assertNotNull(batchId, "batchId should not be null for the first batch")

                logger.info("testGetBatchById: calling getBatch(batchId=$batchId)")
                val result = api.records.getBatch(batchId = batchId)
                logger.info("testGetBatchById: batchId=${result.batchId}, batchNumber=${result.batchNumber}, description=${result.description}, activityCode=${result.activityCode}, activityDescription=${result.activityDescription}, courseOfStudyCode=${result.courseOfStudyCode}, acquisitionDate=${result.acquisitionDate}, callDate=${result.callDate}, minutes size=${result.minutes.size}")
                assertNotNull(result.batchId, "Returned batchId should not be null")

                for (minute in result.minutes) {
                    logger.info("testGetBatchById: minute minutesId=${minute.minutesId}, matricola=${minute.matricola}, grade=${minute.grade}, minutesState=${minute.minutesState}, uploadUrl=${minute.uploadUrl}")
                }
            } else {
                logger.info("testGetBatchById: no batches available, skipping detail test")
            }
        } else {
            logger.info("testGetBatchById: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.records.getBatch(batchId = 1L)
            }
            logger.info("testGetBatchById: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFindBatches() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFindBatches: calling findBatches() with defaults")
            val defaultResult = api.records.findBatches()
            logger.info("testFindBatches: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "findBatches() default result should not be null")
            for (item in defaultResult) {
                logger.info("testFindBatches: lotBatchId=${item.lotBatchId}, activityCode=${item.activityCode}, activityDescription=${item.activityDescription}, batchState=${item.batchState}, batchStateDescription=${item.batchStateDescription}, callDate=${item.callDate}, lecturerName=${item.lecturerName}, lecturerSurname=${item.lecturerSurname}")
            }

            logger.info("testFindBatches: calling findBatches() with pagination start=0, limit=5")
            val paginatedResult = api.records.findBatches(start = 0, limit = 5)
            logger.info("testFindBatches: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            if (defaultResult.isNotEmpty()) {
                val first = defaultResult.first()

                if (first.activityCode != null) {
                    logger.info("testFindBatches: calling findBatches() with activityCode=${first.activityCode}")
                    val filteredByActivity = api.records.findBatches(activityCode = first.activityCode)
                    logger.info("testFindBatches: filtered by activityCode result size=${filteredByActivity.size}")
                    assertNotNull(filteredByActivity, "findBatches() filtered by activityCode should not be null")
                }

                if (first.courseOfStudyCode != null) {
                    logger.info("testFindBatches: calling findBatches() with courseOfStudyCode=${first.courseOfStudyCode}")
                    val filteredByCourse = api.records.findBatches(courseOfStudyCode = first.courseOfStudyCode)
                    logger.info("testFindBatches: filtered by courseOfStudyCode result size=${filteredByCourse.size}")
                    assertNotNull(filteredByCourse, "findBatches() filtered by courseOfStudyCode should not be null")
                }

                if (first.batchState != null) {
                    logger.info("testFindBatches: calling findBatches() with batchState=${first.batchState}")
                    val filteredByState = api.records.findBatches(batchState = first.batchState)
                    logger.info("testFindBatches: filtered by batchState result size=${filteredByState.size}")
                    assertNotNull(filteredByState, "findBatches() filtered by batchState should not be null")
                }

                if (first.callManagementTypeCode != null) {
                    logger.info("testFindBatches: calling findBatches() with callManagementTypeCode=${first.callManagementTypeCode}")
                    val filteredByCallType = api.records.findBatches(callManagementTypeCode = first.callManagementTypeCode)
                    logger.info("testFindBatches: filtered by callManagementTypeCode result size=${filteredByCallType.size}")
                    assertNotNull(filteredByCallType, "findBatches() filtered by callManagementTypeCode should not be null")
                }
            }

            logger.info("testFindBatches: calling findBatches() with studentEnrollmentId=${studentProfile.matricola}")
            val filteredByStudent = api.records.findBatches(studentMatricola = studentProfile.matricola)
            logger.info("testFindBatches: filtered by studentEnrollmentId result size=${filteredByStudent.size}")
            assertNotNull(filteredByStudent, "findBatches() filtered by studentEnrollmentId should not be null")

            if (session.fiscalCode != null) {
                logger.info("testFindBatches: calling findBatches() with studentFiscalCode=${session.fiscalCode}")
                val filteredByFiscalCode = api.records.findBatches(studentFiscalCode = session.fiscalCode)
                logger.info("testFindBatches: filtered by studentFiscalCode result size=${filteredByFiscalCode.size}")
                assertNotNull(filteredByFiscalCode, "findBatches() filtered by studentFiscalCode should not be null")
            }

            logger.info("testFindBatches: calling findBatches() with fields=lottoId,adCod,cdsCod")
            val resultWithFields = api.records.findBatches(
                start = 0,
                limit = 5,
                fields = "lottoId,adCod,cdsCod"
            )
            logger.info("testFindBatches: result with fields size=${resultWithFields.size}")

            logger.info("testFindBatches: calling findBatches() with order=adCod")
            val orderedResult = api.records.findBatches(order = "adCod")
            logger.info("testFindBatches: ordered result size=${orderedResult.size}")
            assertNotNull(orderedResult, "findBatches() with order should not be null")
        } else {
            logger.info("testFindBatches: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.records.findBatches()
            }
            logger.info("testFindBatches: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetBatchByLotId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetBatchByLotId: fetching batches via findBatches() to derive a lotBatchId")
            val batches = api.records.findBatches(start = 0, limit = 5)
            logger.info("testGetBatchByLotId: fetched ${batches.size} batches")

            if (batches.isNotEmpty()) {
                val lotBatchId = batches.first().lotBatchId
                assertNotNull(lotBatchId, "lotBatchId should not be null for the first batch")

                logger.info("testGetBatchByLotId: calling getBatch(lotBatchId=$lotBatchId) with defaults")
                val result = api.records.getBatch(lotBatchId = lotBatchId)
                logger.info("testGetBatchByLotId: result size=${result.size}")
                assertNotNull(result, "getBatch(lotBatchId) result should not be null")
                for (item in result) {
                    logger.info("testGetBatchByLotId: lotBatchId=${item.lotBatchId}, activityCode=${item.activityCode}, activityDescription=${item.activityDescription}, batchState=${item.batchState}, batchStateDescription=${item.batchStateDescription}, callDate=${item.callDate}, lecturerName=${item.lecturerName}, lecturerSurname=${item.lecturerSurname}, committee size=${item.committee.size}, stateTransitions size=${item.stateTransitions.size}")
                }

                logger.info("testGetBatchByLotId: calling getBatch(lotBatchId=$lotBatchId) with fields=lottoId,adCod,statoLotto")
                val resultWithFields = api.records.getBatch(
                    lotBatchId = lotBatchId,
                    fields = "lottoId,adCod,statoLotto"
                )
                logger.info("testGetBatchByLotId: result with fields size=${resultWithFields.size}")

                logger.info("testGetBatchByLotId: calling getBatch(lotBatchId=$lotBatchId) with optionalFields=commissione,transizioniStato")
                val resultWithOptFields = api.records.getBatch(
                    lotBatchId = lotBatchId,
                    optionalFields = "commissione,transizioniStato"
                )
                logger.info("testGetBatchByLotId: result with optionalFields size=${resultWithOptFields.size}")
                for (item in resultWithOptFields) {
                    logger.info("testGetBatchByLotId: committee size=${item.committee.size}, stateTransitions size=${item.stateTransitions.size}")
                    for (member in item.committee) {
                        logger.info("testGetBatchByLotId: committee member lecturerId=${member.lecturerId}, name=${member.name}, surname=${member.surname}, roleCode=${member.roleCode}, roleDescription=${member.roleDescription}")
                    }
                    for (transition in item.stateTransitions) {
                        logger.info("testGetBatchByLotId: stateTransition batchTransactionStateId=${transition.batchTransactionStateId}, oldBatchState=${transition.oldBatchState}, newBatchState=${transition.newBatchState}, insertionDate=${transition.insertionDate}")
                    }
                }
            } else {
                logger.info("testGetBatchByLotId: no batches available via findBatches(), skipping detail test")
            }
        } else {
            logger.info("testGetBatchByLotId: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.records.getBatch(lotBatchId = 1L)
            }
            logger.info("testGetBatchByLotId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetMinutesByBatch() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetMinutesByBatch: fetching batches via findBatches() to derive a lotBatchId")
            val batches = api.records.findBatches(start = 0, limit = 5)
            logger.info("testGetMinutesByBatch: fetched ${batches.size} batches")

            if (batches.isNotEmpty()) {
                val lotBatchId = batches.first().lotBatchId
                assertNotNull(lotBatchId, "lotBatchId should not be null for the first batch")

                logger.info("testGetMinutesByBatch: calling getMinutesByBatch(lotBatchId=$lotBatchId) with defaults")
                val defaultResult = api.records.getMinutesByBatch(lotBatchId = lotBatchId)
                logger.info("testGetMinutesByBatch: default result size=${defaultResult.size}")
                assertNotNull(defaultResult, "getMinutesByBatch() result should not be null")
                for (item in defaultResult) {
                    logger.info("testGetMinutesByBatch: minutesId=${item.minutesId}, lotBatchId=${item.lotBatchId}, matricola=${item.matricola}, name=${item.name}, surname=${item.surname}, activityCode=${item.activityCode}, grade=${item.grade}, minutesState=${item.minutesState}, outcome=${item.outcome}, credits=${item.credits}")
                }

                logger.info("testGetMinutesByBatch: calling getMinutesByBatch(lotBatchId=$lotBatchId) with fields=lottoId,verbId,matricola,voto")
                val resultWithFields = api.records.getMinutesByBatch(
                    lotBatchId = lotBatchId,
                    fields = "lottoId,verbId,matricola,voto"
                )
                logger.info("testGetMinutesByBatch: result with fields size=${resultWithFields.size}")

                logger.info("testGetMinutesByBatch: calling getMinutesByBatch(lotBatchId=$lotBatchId) with optionalFields=modifiche")
                val resultWithOptFields = api.records.getMinutesByBatch(
                    lotBatchId = lotBatchId,
                    optionalFields = "modifiche"
                )
                logger.info("testGetMinutesByBatch: result with optionalFields size=${resultWithOptFields.size}")
                for (item in resultWithOptFields) {
                    logger.info("testGetMinutesByBatch: minutesId=${item.minutesId}, modifications size=${item.modifications.size}")
                    for (mod in item.modifications) {
                        logger.info("testGetMinutesByBatch: modification logOriginCode=${mod.logOriginCode}, oldValue=${mod.oldValue}, newValue=${mod.newValue}, insertionDate=${mod.insertionDate}")
                    }
                }
            } else {
                logger.info("testGetMinutesByBatch: no batches available via findBatches(), skipping test")
            }
        } else {
            logger.info("testGetMinutesByBatch: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.records.getMinutesByBatch(lotBatchId = 1L)
            }
            logger.info("testGetMinutesByBatch: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetMinutes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.STUDENT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetMinutes: fetching minutes via findMinutes() to derive lotBatchId and minutesId")
            val minutes = api.records.findMinutes(start = 0, limit = 5)
            logger.info("testGetMinutes: fetched ${minutes.size} minutes")

            if (minutes.isNotEmpty()) {
                val firstMinute = minutes.first()
                val lotBatchId = firstMinute.lotBatchId
                val minutesId = firstMinute.minutesId
                assertNotNull(lotBatchId, "lotBatchId should not be null for the first minute")
                assertNotNull(minutesId, "minutesId should not be null for the first minute")

                logger.info("testGetMinutes: calling getMinutes(lotBatchId=$lotBatchId, minutesId=$minutesId) with defaults")
                val result = api.records.getMinutes(lotBatchId = lotBatchId, minutesId = minutesId)
                logger.info("testGetMinutes: minutesId=${result.minutesId}, lotBatchId=${result.lotBatchId}, matricola=${result.matricola}, name=${result.name}, surname=${result.surname}, activityCode=${result.activityCode}, activityDescription=${result.activityDescription}, grade=${result.grade}, minutesState=${result.minutesState}, outcome=${result.outcome}, credits=${result.credits}, lecturerName=${result.lecturerName}, lecturerSurname=${result.lecturerSurname}, fiscalCode=${result.fiscalCode}")
                assertNotNull(result.minutesId, "Returned minutesId should not be null")

                logger.info("testGetMinutes: calling getMinutes() with fields=lottoId,verbId,matricola,voto")
                val resultWithFields = api.records.getMinutes(
                    lotBatchId = lotBatchId,
                    minutesId = minutesId,
                    fields = "lottoId,verbId,matricola,voto"
                )
                logger.info("testGetMinutes: result with fields minutesId=${resultWithFields.minutesId}")

                logger.info("testGetMinutes: calling getMinutes() with optionalFields=modifiche")
                val resultWithOptFields = api.records.getMinutes(
                    lotBatchId = lotBatchId,
                    minutesId = minutesId,
                    optionalFields = "modifiche"
                )
                logger.info("testGetMinutes: result with optionalFields minutesId=${resultWithOptFields.minutesId}, modifications size=${resultWithOptFields.modifications.size}")
                for (mod in resultWithOptFields.modifications) {
                    logger.info("testGetMinutes: modification logOriginCode=${mod.logOriginCode}, oldValue=${mod.oldValue}, newValue=${mod.newValue}, insertionDate=${mod.insertionDate}, insertionUserId=${mod.insertionUserId}")
                }
            } else {
                logger.info("testGetMinutes: no minutes available via findMinutes(), skipping detail test")
            }
        } else {
            logger.info("testGetMinutes: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.records.getMinutes(lotBatchId = 1L, minutesId = 1L)
            }
            logger.info("testGetMinutes: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testFindMinutes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.STUDENT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testFindMinutes: calling findMinutes() with defaults")
            val defaultResult = api.records.findMinutes()
            logger.info("testFindMinutes: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "findMinutes() default result should not be null")
            for (item in defaultResult) {
                logger.info("testFindMinutes: minutesId=${item.minutesId}, lotBatchId=${item.lotBatchId}, matricola=${item.matricola}, name=${item.name}, surname=${item.surname}, activityCode=${item.activityCode}, activityDescription=${item.activityDescription}, grade=${item.grade}, minutesState=${item.minutesState}, outcome=${item.outcome}, credits=${item.credits}")
            }

            logger.info("testFindMinutes: calling findMinutes() with pagination start=0, limit=5")
            val paginatedResult = api.records.findMinutes(start = 0, limit = 5)
            logger.info("testFindMinutes: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            logger.info("testFindMinutes: calling findMinutes() with studentEnrollmentId=${studentProfile.matricola}")
            val filteredByEnrollment = api.records.findMinutes(studentMatricola = studentProfile.matricola)
            logger.info("testFindMinutes: filtered by studentEnrollmentId result size=${filteredByEnrollment.size}")
            assertNotNull(filteredByEnrollment, "findMinutes() filtered by studentEnrollmentId should not be null")

            if (session.fiscalCode != null) {
                logger.info("testFindMinutes: calling findMinutes() with studentFiscalCode=${session.fiscalCode}")
                val filteredByFiscalCode = api.records.findMinutes(studentFiscalCode = session.fiscalCode)
                logger.info("testFindMinutes: filtered by studentFiscalCode result size=${filteredByFiscalCode.size}")
                assertNotNull(filteredByFiscalCode, "findMinutes() filtered by studentFiscalCode should not be null")
            }

            if (defaultResult.isNotEmpty()) {
                val first = defaultResult.first()

                if (first.activityCode != null) {
                    logger.info("testFindMinutes: calling findMinutes() with activityCode=${first.activityCode}")
                    val filteredByActivity = api.records.findMinutes(activityCode = first.activityCode)
                    logger.info("testFindMinutes: filtered by activityCode result size=${filteredByActivity.size}")
                    assertNotNull(filteredByActivity, "findMinutes() filtered by activityCode should not be null")
                }

                if (first.courseOfStudyCode != null) {
                    logger.info("testFindMinutes: calling findMinutes() with courseOfStudyCode=${first.courseOfStudyCode}")
                    val filteredByCourse = api.records.findMinutes(courseOfStudyCode = first.courseOfStudyCode)
                    logger.info("testFindMinutes: filtered by courseOfStudyCode result size=${filteredByCourse.size}")
                    assertNotNull(filteredByCourse, "findMinutes() filtered by courseOfStudyCode should not be null")
                }

                if (first.studentActivityCode != null) {
                    logger.info("testFindMinutes: calling findMinutes() with studentActivityCode=${first.studentActivityCode}")
                    val filteredByStudentActivity = api.records.findMinutes(studentActivityCode = first.studentActivityCode)
                    logger.info("testFindMinutes: filtered by studentActivityCode result size=${filteredByStudentActivity.size}")
                    assertNotNull(filteredByStudentActivity, "findMinutes() filtered by studentActivityCode should not be null")
                }

                if (first.courseOfStudyStudentCode != null) {
                    logger.info("testFindMinutes: calling findMinutes() with courseOfStudyStudentCode=${first.courseOfStudyStudentCode}")
                    val filteredByCdsStu = api.records.findMinutes(courseOfStudyStudentCode = first.courseOfStudyStudentCode)
                    logger.info("testFindMinutes: filtered by courseOfStudyStudentCode result size=${filteredByCdsStu.size}")
                    assertNotNull(filteredByCdsStu, "findMinutes() filtered by courseOfStudyStudentCode should not be null")
                }

                if (first.minutesState != null) {
                    logger.info("testFindMinutes: calling findMinutes() with minutesState=${first.minutesState}")
                    val filteredByState = api.records.findMinutes(minutesState = first.minutesState.toString())
                    logger.info("testFindMinutes: filtered by minutesState result size=${filteredByState.size}")
                    assertNotNull(filteredByState, "findMinutes() filtered by minutesState should not be null")
                }
            }

            logger.info("testFindMinutes: calling findMinutes() with order=adCod")
            val orderedResult = api.records.findMinutes(order = "adCod")
            logger.info("testFindMinutes: ordered result size=${orderedResult.size}")
            assertNotNull(orderedResult, "findMinutes() with order should not be null")
        } else {
            logger.info("testFindMinutes: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.records.findMinutes()
            }
            logger.info("testFindMinutes: Esse3Exception thrown as expected")
        }
    }
}
