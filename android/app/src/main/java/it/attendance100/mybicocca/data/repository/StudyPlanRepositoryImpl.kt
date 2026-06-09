package it.attendance100.mybicocca.data.repository

import io.ktor.utils.io.jvm.javaio.toInputStream
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.mapper.calendar.normalizeSubjectName
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffAcademicYear
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffStudyProgram
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffStudyProgramSubject
import it.attendance100.mybicocca.data.remote.esse3.api.Esse3PlansApi
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ChoiceRegulation
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ChoiceRuleSchemaWithDetails
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ChoiceType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PlanSchema
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PlanType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PostPlanActivity
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PostPlanBody
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3State3
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudyPath
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudyPlan
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudyPlanActivity
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudyPlanHeader
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachingUnitType
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.ChoiceConstraintUnit
import it.attendance100.mybicocca.domain.model.studyplan.EditableCourse
import it.attendance100.mybicocca.domain.model.studyplan.EditableRule
import it.attendance100.mybicocca.domain.model.studyplan.PlanApprovalType
import it.attendance100.mybicocca.domain.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.domain.model.studyplan.Semester
import it.attendance100.mybicocca.domain.model.studyplan.StudyPath
import it.attendance100.mybicocca.domain.model.studyplan.StudyPathFacet
import it.attendance100.mybicocca.domain.model.studyplan.StudyPathOption
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlan
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlanCourse
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlanType
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyPlanRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val easyStaffApi: EasyStaffApi,
) : StudyPlanRepository {

    private val activitiesCache = ConcurrentHashMap<CareerId, CachedActivities>()
    private val programCache = ConcurrentHashMap<CareerId, CachedProgram>()
    private val plannedCache = ConcurrentHashMap<CareerId, CachedPlanned>()
    private val mutexes = ConcurrentHashMap<CareerId, Mutex>()

    override suspend fun getPlannedCoursesForActiveCareer(careerId: CareerId): List<PlannedCourse> {
        plannedCache[careerId]?.takeIf { it.isFresh() }?.let { return it.courses }
        val mutex = mutexes.computeIfAbsent(careerId) { Mutex() }
        return mutex.withLock {
            plannedCache[careerId]?.takeIf { it.isFresh() }?.let { return@withLock it.courses }
            val career = activeCareer(careerId) ?: return@withLock emptyList()
            val result = coroutineScope {
                val activitiesDeferred = async { loadActivitiesUnlocked(careerId) }
                val programDeferred = async { loadProgramUnlocked(careerId, career) }
                val activities = activitiesDeferred.await()
                val program = programDeferred.await() ?: return@coroutineScope emptyList()
                if (activities.isEmpty()) emptyList() else intersect(activities, program)
            }
            plannedCache[careerId] = CachedPlanned(courses = result, cachedAtMs = nowMs())
            result
        }
    }

    override suspend fun getStudyPlan(careerId: CareerId): StudyPlan? {
        val plansApi = sessionManager.esse3().plans
        val header = plansApi.getStudentPlanHeaders(studentId = careerId.value).pickBest() ?: return null
        val planId = header.planId?.toLong() ?: return null
        // ALL: the default response omits choiceFlag/courseYear/weight, which the page needs.
        val plan = plansApi.getStudentPlan(careerId.value, planId, optionalFields = "ALL")
        return StudyPlan(
            planId = planId,
            studentId = careerId.value,
            type = when (header.planType) {
                Esse3PlanType.Standard -> StudyPlanType.Standard
                Esse3PlanType.Individual -> StudyPlanType.Individual
                else -> StudyPlanType.Unknown
            },
            statusDescription = header.stateDescription,
            lastUpdated = header.lastStateChangeDate.parseEsse3Date(),
            choiceRegulationId = header.choiceRegulationId,
            schemaId = header.schemaId?.toLong(),
            courses = plan.activity.mapNotNull { it.toStudyPlanCourse() },
        )
    }

    override suspend fun getStudyPath(careerId: CareerId): StudyPath? = coroutineScope {
        val esse3 = sessionManager.esse3()
        // Path descriptions on the header (pdsSceDes, pdsDes, aptDes, ...) are optional
        // fields Esse3 omits by default.
        val header = esse3.plans
            .getStudentPlanHeaders(studentId = careerId.value, optionalFields = "ALL")
            .pickBest()

        // Only standard plans (tipoPiano = S) hang off a choice regulation with selectable
        // schemas. Individual plans have no percorso/orientamento choice. With no plan at
        // all, the active regulation for the career's course + cohort still allows a
        // first compilation.
        val regulationId: Long?
        val courseOfStudyId: Long?
        val academicYearOrderId: Long?
        if (header != null) {
            regulationId = header.choiceRegulationId
            courseOfStudyId = header.courseOfStudyStudentId
            academicYearOrderId = header.academicYearOrderStudentId
        } else {
            val regulation = findRegulationForCareer(careerId) ?: return@coroutineScope null
            regulationId = regulation.choiceRegulationId
            courseOfStudyId = regulation.courseOfStudyId
            academicYearOrderId = regulation.academicYearOrderId
        }

        // Fetch schemas, windows and the percorso catalog concurrently; all non-fatal —
        // a path with no selectable alternatives still renders read-only.
        val schemasDeferred = async {
            if (regulationId == null) emptyList()
            else runCatching {
                // ALL: pds/orient/prof/apt/cond descriptions are optional fields.
                esse3.choiceRules.getStudyPlanSchemas(regulationId, optionalFields = "ALL")
            }.getOrDefault(emptyList())
        }
        val windowOpenDeferred = async {
            if (regulationId == null) false
            else runCatching { isPlanEditingOpen(regulationId) }.getOrDefault(false)
        }
        // Public percorso catalog of the course/ordinamento: fills names the schema
        // response leaves blank, plus the teaching languages. Purely cosmetic.
        val catalogDeferred = async {
            if (courseOfStudyId == null || academicYearOrderId == null) emptyMap()
            else runCatching {
                esse3.structure.getPaths(courseOfStudyId, academicYearOrderId.toInt())
                    .associateBy { it.studyPlanCode }
            }.getOrDefault(emptyMap())
        }
        val schemas = schemasDeferred.await()
        val windowOpen = windowOpenDeferred.await()
        val catalog = catalogDeferred.await()

        val currentSchema = header?.schemaId?.let { id -> schemas.firstOrNull { it.schemaId == id } }

        // Percorso and part-time live on the plan header; orientamento and profilo are
        // only carried on the schema, so read them from the student's current schema.
        val percorso = facet(
            code = header?.studyPlanChoiceCode ?: header?.studyPlanStudentCode,
            description = header?.studyPlanChoiceDescription ?: header?.studyPlanDescription
                ?: currentSchema?.studyPlanDescription,
        )
        val orientamento = facet(currentSchema?.orientationCode, currentSchema?.orientationDescription)
        val profilo = facet(currentSchema?.professionCode, currentSchema?.professionDescription)
        val partTime = facet(
            code = header?.aptCode ?: currentSchema?.aptCode,
            description = header?.aptDescription ?: currentSchema?.aptDescription,
        )

        // Choosable today = exposed to students AND inside the schema's validity range.
        // The current schema stays listed even when hidden/expired, so the student can
        // re-confirm their own percorso. De-duplicate by path tuple — two schemas that
        // differ only by approval flavour (e.g. GGG vs GGG-A) are the same choice.
        val today = LocalDate.now()
        val currentTuple = currentSchema?.let(::pathTuple)
        val options = schemas
            .filter { (it.webViewFlag != 0 && it.isValidOn(today)) || pathTuple(it) == currentTuple }
            .distinctBy { pathTuple(it) }
            .map { it.toOption(isCurrent = pathTuple(it) == currentTuple, catalog = catalog) }

        StudyPath(
            percorso = percorso,
            orientamento = orientamento,
            profilo = profilo,
            partTime = partTime,
            choiceRegulationId = regulationId,
            currentSchemaId = options.firstOrNull { it.isCurrent }?.schemaId,
            options = options,
            editingOpen = windowOpen,
            choiceAvailable = windowOpen && options.size > 1,
        )
    }

    // With no plan there's no header pointing at the regulation — resolve it from the
    // career's course and cohort instead. Several active regulations pick the newest
    // ordinamento/revision; none found means compilation simply isn't offered.
    private suspend fun findRegulationForCareer(careerId: CareerId): Esse3ChoiceRegulation? {
        val career = activeCareer(careerId) ?: return null
        if (career.programId <= 0L) return null
        val regulations = runCatching {
            sessionManager.esse3().choiceRules.getChoiceRegulations(
                courseOfStudyId = career.programId,
                cohort = career.academicYearEnrollmentId,
            )
        }.getOrDefault(emptyList())
        return regulations
            .filter { it.choiceRegulationId != null }
            .maxByOrNull { (it.academicYearOrderId ?: 0L) * 10_000 + (it.academicYearRevisionId ?: 0) }
    }

    private suspend fun isPlanEditingOpen(choiceRegulationId: Long): Boolean {
        val windows = sessionManager.esse3().choiceRules.getChoiceRegulationWindows(choiceRegulationId)
        if (windows.isEmpty()) return false
        val today = LocalDate.now()
        return windows.any { window ->
            val start = window.startDate.parseEsse3Date()
            val end = window.endDate.parseEsse3Date()
            (start == null || !today.isBefore(start)) && (end == null || !today.isAfter(end))
        }
    }

    override suspend fun getStudyPlanPrint(careerId: CareerId, planId: Long): ByteArray =
        withContext(Dispatchers.IO) {
            sessionManager.esse3().plans.getPlanPrint(careerId.value, planId)
                .toInputStream()
                .use { it.readBytes() }
        }

    // Esse3 returns the whole choice tree; only activities the student actually has in
    // plan (choiceFlag != 0) become courses.
    private fun Esse3StudyPlanActivity.toStudyPlanCourse(): StudyPlanCourse? {
        if (choiceFlag == 0) return null
        val courseId = activityChoiceId
            ?: choiceActivityId
            ?: teachingActivityChoiceId?.toLong()
            ?: return null
        val name = activityTranscriptDescription
            ?: contextualizedTeachingActivityKey?.activityDescription
            ?: return null
        return StudyPlanCourse(
            id = courseId,
            name = name,
            code = activityTranscriptCode ?: contextualizedTeachingActivityKey?.activityCode,
            credits = weight ?: 0f,
            year = courseYear?.let(::StudyYear) ?: StudyYear.Unknown,
        )
    }

    // Path identity of a schema: the tuple that distinguishes a real choice. Schemas
    // sharing this tuple are duplicates (typically approval-flavour variants).
    private fun pathTuple(schema: Esse3PlanSchema): List<String?> = listOf(
        schema.studyPlanCode,
        schema.orientationCode?.takeIf { it.isNotBlank() },
        schema.professionCode?.takeIf { it.isNotBlank() },
        schema.aptCode?.takeIf { it.isNotBlank() },
    )

    private fun Esse3PlanSchema.toOption(
        isCurrent: Boolean,
        catalog: Map<String?, Esse3StudyPath>,
    ): StudyPathOption {
        val catalogEntry = studyPlanCode?.let(catalog::get)
        return StudyPathOption(
            schemaId = schemaId ?: 0L,
            schemaCode = schemaCode,
            schemaDescription = schemaDescription,
            percorso = facet(
                code = studyPlanCode,
                description = studyPlanDescription ?: catalogEntry?.studyPlanDescription,
            ),
            orientamento = facet(orientationCode, orientationDescription),
            profilo = facet(professionCode, professionDescription),
            partTime = facet(aptCode, aptDescription),
            isCurrent = isCurrent,
            approval = when (approvalType) {
                0 -> PlanApprovalType.Automatic
                1 -> PlanApprovalType.Manual
                2 -> PlanApprovalType.AutomaticIfCompliant
                else -> PlanApprovalType.Unknown
            },
            conditionNote = conditionDescription?.takeIf { it.isNotBlank() },
            languages = catalogEntry?.teachingLanguages.orEmpty().mapNotNull { lang ->
                lang.teachingLanguageDescription?.takeIf { it.isNotBlank() }
                    ?: lang.teachingLanguageCode?.takeIf { it.isNotBlank() }
            },
        )
    }

    // Schemas carry their own validity window, independent of the compilation windows.
    private fun Esse3PlanSchema.isValidOn(date: LocalDate): Boolean {
        val start = evaluationStartDate.parseEsse3Date()
        val end = evaluationEndDate.parseEsse3Date()
        return (start == null || !date.isBefore(start)) && (end == null || !date.isAfter(end))
    }

    // A facet exists only when at least one of code/description carries text.
    private fun facet(code: String?, description: String?): StudyPathFacet? {
        val cleanCode = code?.takeIf { it.isNotBlank() }
        val cleanDesc = description?.takeIf { it.isNotBlank() }
        return if (cleanCode == null && cleanDesc == null) null
        else StudyPathFacet(code = cleanCode, description = cleanDesc)
    }

    override suspend fun getStudyPlanDraft(
        careerId: CareerId,
        planId: Long?,
        choiceRegulationId: Long,
        schemaId: Long,
    ): List<EditableRule> = coroutineScope {
        val esse3 = sessionManager.esse3()
        val schemaDeferred = async {
            esse3.choiceRules.getStudyPlanSchemaWithDetails(choiceRegulationId, schemaId)
        }
        val existingActivities = if (planId != null && planId > 0) {
            esse3.plans.getStudentPlan(careerId.value, planId).activity
        } else {
            emptyList()
        }
        val schema = schemaDeferred.await()

        // An activity can appear under multiple rules; once selected somewhere it must
        // not auto-select again elsewhere.
        val assignedActivityCodes = mutableSetOf<String>()
        schema.choiceRules
            .mapNotNull { it.toEditableRule(existingActivities, assignedActivityCodes) }
            .sortedWith(compareBy({ it.courseYear }, { it.orderNumber }))
    }

    override suspend fun submitStudyPlan(
        careerId: CareerId,
        rules: List<EditableRule>,
        chosenPath: StudyPathOption?,
    ) {
        val selected = rules.flatMap { rule ->
            rule.courses.filter { it.isSelected }.map { course -> rule to course }
        }
        val activities = selected.mapIndexed { index, (rule, course) ->
            Esse3PostPlanActivity(
                itemId = index + 1,
                orderNumber = rule.orderNumber,
                activityCode = course.code,
                courseOfStudyTeachingActivityCode = course.courseOfStudyCode,
                studyPlanTeachingActivityCode = course.studyPlanCode,
                academicYearOfferActivityId = course.academicYearOfferId,
            )
        }
        // Standard plan, proposed state; replaces the currently valid plan. The schema's
        // approval flavour rides on tipoRegsce; pdsSceCod records the percorso choice
        // only when compiling against a non-current schema — null keeps the server on
        // the student's current percorso.
        val body = Esse3PostPlanBody(
            type = "S",
            state = "P",
            implementationFlag = false,
            cancelValidPlanFlag = true,
            studyPlanChoiceCode = chosenPath?.takeIf { !it.isCurrent }?.percorso?.code,
            choiceRegulationType = when (chosenPath?.approval) {
                PlanApprovalType.Manual -> 1
                PlanApprovalType.AutomaticIfCompliant -> 2
                else -> 0
            },
            activity = activities,
        )
        sessionManager.esse3().plans.postStudentPlan(careerId.value, body)
    }

    private fun Esse3ChoiceRuleSchemaWithDetails.toEditableRule(
        existingActivities: List<Esse3StudyPlanActivity>,
        assignedActivityCodes: MutableSet<String>,
    ): EditableRule? {
        val ruleChoiceId = choiceId ?: return null
        val isMandatory = choiceType == Esse3ChoiceType.Obligatory
        // BLK rules constrain how many picks are made, not how many CFU they weigh.
        // Blocks map 1:1 onto activities here, so a block pick is an activity pick.
        // Mandatory rules have nothing to pick, so they always read in CFU even when
        // the schema marks them BLK.
        val unit = when {
            isMandatory -> ChoiceConstraintUnit.Credits
            teachingUnitType == Esse3TeachingUnitType.Block -> ChoiceConstraintUnit.Activities
            else -> ChoiceConstraintUnit.Credits
        }
        val maxUnits = maxTeachingUnit

        var currentUnits = 0f
        val courses = blocks.flatMap { block ->
            block.activity.mapNotNull { activity ->
                val key = activity.contextualizedTeachingActivityKey ?: return@mapNotNull null
                val activityCode = key.activityCode ?: return@mapNotNull null
                val weight = activity.weight ?: 0f
                val unitWeight = when (unit) {
                    ChoiceConstraintUnit.Credits -> weight
                    ChoiceConstraintUnit.Activities -> 1f
                }

                // Mandatory rules pre-select everything; otherwise pre-select what the
                // existing plan already contains, while it still fits the rule's cap.
                var isSelected = isMandatory
                if (!isSelected && activityCode !in assignedActivityCodes) {
                    val isInExistingPlan = existingActivities.any { existing ->
                        existing.contextualizedTeachingActivityKey?.activityCode == activityCode
                    }
                    if (isInExistingPlan && (maxUnits == null || currentUnits + unitWeight <= maxUnits)) {
                        isSelected = true
                    }
                }
                if (isSelected) {
                    assignedActivityCodes.add(activityCode)
                    if (!isMandatory) currentUnits += unitWeight
                }

                EditableCourse(
                    choiceId = activity.teachingActivityChoiceId ?: return@mapNotNull null,
                    code = activityCode,
                    name = key.activityDescription ?: activityCode,
                    credits = weight,
                    courseOfStudyCode = key.courseOfStudyCode,
                    studyPlanCode = key.studyPlanCode,
                    academicYearOfferId = key.academicYearOfferId.toInt(),
                    isSelected = isSelected,
                    isMandatory = isMandatory,
                    isInitialSelected = isSelected,
                )
            }
        }
        if (courses.isEmpty()) return null

        return EditableRule(
            choiceId = ruleChoiceId,
            orderNumber = orderNumber ?: 0,
            description = description.orEmpty(),
            courseYear = courseYear ?: 0,
            typeDescription = choiceTypeDescription.orEmpty(),
            isMandatoryRule = isMandatory,
            unit = unit,
            minUnits = minTeachingUnit,
            maxUnits = maxTeachingUnit,
            isOptional = optionalFlag == 1,
            courses = courses,
            preNote = preNote,
            postNote = postNote,
        )
    }

    private suspend fun loadActivitiesUnlocked(careerId: CareerId): List<Esse3StudyPlanActivity> {
        activitiesCache[careerId]?.takeIf { it.isFresh() }?.let { return it.activities }
        val esse3 = sessionManager.esse3()
        val plan = runCatching { fetchEsse3Plan(esse3.plans, careerId) }.getOrNull() ?: return emptyList()
        val activities = plan.activity
        activitiesCache[careerId] = CachedActivities(activities = activities, cachedAtMs = nowMs())
        return activities
    }

    private suspend fun loadProgramUnlocked(careerId: CareerId, career: Career): EasyStaffStudyProgram? {
        programCache[careerId]?.takeIf { it.isFresh() }?.let { return it.program }
        val program = runCatching { fetchEasyStaffProgram(career) }.getOrNull() ?: return null
        programCache[careerId] = CachedProgram(program = program, cachedAtMs = nowMs())
        return program
    }

    private suspend fun fetchEsse3Plan(
        plansApi: Esse3PlansApi,
        careerId: CareerId,
    ): Esse3StudyPlan? {
        val headers = runCatching {
            plansApi.getStudentPlanHeaders(studentId = careerId.value)
        }.getOrDefault(emptyList())
        val header = headers.pickBest() ?: return null
        val planId = header.planId?.toLong() ?: return null
        return runCatching { plansApi.getStudentPlan(careerId.value, planId) }.getOrNull()
    }

    private suspend fun fetchEasyStaffProgram(career: Career): EasyStaffStudyProgram? {
        val code = career.easyStaffProgramCode ?: return null
        val academicYear = currentAcademicYear()
        val programs = runCatching {
            easyStaffApi.core.getStudyPrograms(academicYear)
        }.getOrDefault(emptyList())
        return programs.firstOrNull { it.code == code }
    }

    private fun intersect(
        activities: List<Esse3StudyPlanActivity>,
        program: EasyStaffStudyProgram,
    ): List<PlannedCourse> {
        val subjects = program.years.flatMap { it.subjects }
        val byCode: Map<String, EasyStaffStudyProgramSubject> = subjects.associateBy { it.code }
        val byName: Map<String, EasyStaffStudyProgramSubject> =
            subjects.associateBy { normalizeSubjectName(it.name) }
        // EasyStaff period codes ("S1"/"S2") keyed by their opaque period id.
        val semesterByPeriodId: Map<String, Semester> = program.teachingPeriods.associate {
            it.id to when (it.code.uppercase()) {
                "S1" -> Semester.First
                "S2" -> Semester.Second
                else -> Semester.Unknown
            }
        }
        return activities.mapNotNull { activity ->
            val activityName = activity.activityTranscriptDescription ?: return@mapNotNull null
            val activityCode = activity.activityTranscriptCode
            val subject = (activityCode?.let(byCode::get))
                ?: byName[normalizeSubjectName(activityName)]
                ?: return@mapNotNull null
            PlannedCourse(
                easyStaffSubjectId = subject.id,
                easyStaffSubjectCode = subject.code,
                activityCode = activityCode,
                name = subject.name.ifBlank { activityName },
                normalizedName = normalizeSubjectName(activityName),
                teacherName = subject.teacherName,
                periodId = subject.periodId,
                semester = semesterByPeriodId[subject.periodId] ?: Semester.Unknown,
                studyYear = activity.courseYear?.let(::StudyYear) ?: StudyYear.Unknown,
                cfu = activity.weight?.toInt(),
            )
        }.distinctBy { it.easyStaffSubjectCode }
    }

    private fun activeCareer(careerId: CareerId): Career? {
        val account = sessionManager.activeAccount.value ?: return null
        return account.academic.careers.firstOrNull { it.id == careerId }
    }

    private fun CachedActivities.isFresh(): Boolean = nowMs() - cachedAtMs <= CACHE_TTL_MS
    private fun CachedProgram.isFresh(): Boolean = nowMs() - cachedAtMs <= CACHE_TTL_MS
    private fun CachedPlanned.isFresh(): Boolean = nowMs() - cachedAtMs <= CACHE_TTL_MS
    private fun nowMs(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()

    private data class CachedActivities(val activities: List<Esse3StudyPlanActivity>, val cachedAtMs: Long)
    private data class CachedProgram(val program: EasyStaffStudyProgram, val cachedAtMs: Long)
    private data class CachedPlanned(val courses: List<PlannedCourse>, val cachedAtMs: Long)

    private companion object {
        const val CACHE_TTL_MS = 30L * 60_000L
    }
}

private fun List<Esse3StudyPlanHeader>.pickBest(): Esse3StudyPlanHeader? {
    val approved = firstOrNull { it.state is Esse3State3.Approved }
    return approved ?: maxByOrNull { it.planId ?: Int.MIN_VALUE }
}

private val Esse3HeaderDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

// Esse3 emits DD/MM/YYYY, sometimes followed by a time component — keep the date part.
private fun String?.parseEsse3Date(): LocalDate? = this?.take(10)?.let {
    runCatching { LocalDate.parse(it, Esse3HeaderDateFormat) }.getOrNull()
}

private fun currentAcademicYear(): EasyStaffAcademicYear {
    val today = LocalDate.now()
    val startYear = if (today.monthValue >= 9) today.year else today.year - 1
    return EasyStaffAcademicYear(startYear = startYear)
}
