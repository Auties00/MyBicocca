package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import it.attendance100.mybicocca.data.common.exception.HtmlParsingException
import it.attendance100.mybicocca.data.common.util.cleanText
import it.attendance100.mybicocca.data.common.util.extractQueryParamAsLong
import it.attendance100.mybicocca.data.common.util.parseTable
import it.attendance100.mybicocca.data.dto.esse3.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.FormElement

/**
 * API for questionnaire operations.
 *
 * Provides access to:
 * - Course evaluation questionnaires (teaching quality surveys)
 * - Questionnaire page navigation and submission
 */
class Esse3QuestionnaireApi(
    client: HttpClient
) : Esse3AbstractApi(client) {
    companion object {
        private const val VALDID_ENTRYPOINT = "/auth/studente/QuestAdLibrettoValDid.do"
        private const val VALDID_MENU_COD = "menu_link-navbox_questionari_Questionari_generici"
        private const val WRAPPER_VALDID_ENTRYPOINT = "/auth/questionari/QuestionariWrapperAdLibrettoValDid.do"
        private const val QUESTIONNAIRE_SUBMIT_ENTRYPOINT = "/questionari/QuestionariPaginaSubmitNew.do"
        private const val NEW_QUESTIONNAIRE_ENTRYPOINT = "/questionari/QuestionariWrapperCompilaNew.do"
        private val QUESTION_REGEX = "quest_container_domanda_(\\d+)".toRegex()
    }

    /**
     * Gets the list of courses available for teaching evaluation.
     *
     * This returns courses from the student's academic record that can be evaluated
     * through the ValDid (Valutazione Didattica) questionnaire system.
     *
     * @return List of courses with their evaluation status
     */
    suspend fun getEvaluationCourses(): List<Esse3EvaluationCourse> {
        val doc = executeGet(VALDID_ENTRYPOINT, mapOf("menu_opened_cod" to VALDID_MENU_COD))

        val table = doc.selectFirst("#quest_table_quest_valutazione")
            ?: throw HtmlParsingException("Cannot get evaluation courses: missing 'quest_table_quest_valutazione' table")

        return table.parseTable().map { row ->
            val year = row.getTextAsOrThrow("anno di corso") { it.toIntOrNull() }

            val (courseCode, courseName) = parseCourseCodeAndName(row.getTextOrThrow("attività didattiche"))

            val credits = row.getTextAsOrThrow("peso in crediti") { it.toIntOrNull() }

            val academicYear = row.getTextOrThrow("aa freq.")

            val questionnaireCell = row.getElementOrNull("q.val.")
            val questionnaireLink = questionnaireCell?.selectFirst("a")
            val status = parseEvaluationCourseStatus(questionnaireLink)

            Esse3EvaluationCourse(
                courseCode = courseCode,
                courseName = courseName,
                year = year,
                credits = credits,
                academicYear = academicYear,
                status = status
            )
        }.toList()
    }

    /**
     * Gets the evaluation partitions for a specific course.
     *
     * A course may have multiple partitions (different teachers, class sections, etc.)
     * that need to be evaluated separately.
     *
     * @param course The course to get partitions for (must have Pending or Completed status)
     * @return List of partitions with their evaluation status
     * @throws IllegalArgumentException if the course has no questionnaire available
     */
    suspend fun getEvaluationPartitions(course: Esse3EvaluationCourse): List<Esse3EvaluationPartition> {
        if(course.status is Esse3EvaluationCourseStatus.NotAvailable) {
            return emptyList()
        }

        val adsceId = when (val status = course.status) {
            is Esse3EvaluationCourseStatus.Pending -> status.activityId
            is Esse3EvaluationCourseStatus.Completed -> status.activityId
        }

        val doc = executeGet(
            WRAPPER_VALDID_ENTRYPOINT,
            mapOf("evento_comp_cod" to "EV_VAL_DID", "adsce_id" to adsceId.toString())
        )

        val table = doc.selectFirst("table[id*=tabellaPartizioni]")
            ?: throw HtmlParsingException("Cannot get evaluation partitions: missing partitions table")

        return table.parseTable().map { row ->
            val unitName = row.getTextOrThrow("unità didattica")
            val teacher = row.getTextOrThrow("docente")
            val activityType = row.getTextOrThrow("tipo attività")
            val partition = row.getTextOrThrow("partizione")

            val questionnaire = row.getElementOrThrow("questionario")
            val questionnaireLink = questionnaire.selectFirst("a")
            val status = parseEvaluationPartitionStatus(questionnaireLink)

            Esse3EvaluationPartition(
                unitName = unitName,
                teacher = teacher,
                activityType = activityType,
                partition = partition,
                status = status
            )
        }.toList()
    }

    /**
     * Starts a questionnaire for a specific partition.
     *
     * @param partition The partition to start the questionnaire for
     * @return The first page of the questionnaire
     */
    suspend fun startQuestionnaire(partition: Esse3EvaluationPartition): Esse3QuestionnairePage? {
        if(partition.status !is Esse3EvaluationPartitionStatus.Pending) {
            return null
        }

        val wrapperDoc = executeGet(partition.status.questionnaireUrl)
        val startForm = wrapperDoc.selectFirst("form#quest_form_compilazioni1") as? FormElement
            ?: return null

        val formFields = startForm.formData()
            .associate { it.key() to it.value() }
        val pageDoc = executePost(NEW_QUESTIONNAIRE_ENTRYPOINT, formFields)
        return parseQuestionnairePage(pageDoc)
    }

    /**
     * Submits answers for a questionnaire page and navigates to the next page.
     *
     * @param page The current questionnaire page
     * @param answers Map of field names to answer values
     * @return The result of the submission (next page, completed, or validation error)
     */
    suspend fun submitPage(
        page: Esse3QuestionnairePage,
        answers: Map<String, String>
    ): Esse3QuestionnaireSubmitResult {
        val formFields = page.navigation.toFormFields().toMutableMap()
        formFields.putAll(answers)
        formFields["form_id_quest_form_questionario_pagina"] = "quest_form_questionario_pagina"
        formFields["sbmSuccessivo"] = ""

        val doc = executePost(QUESTIONNAIRE_SUBMIT_ENTRYPOINT, formFields)

        val errorMessage = doc.selectFirst(".alert-danger, .errore, #error")?.text()?.cleanText()
        if (errorMessage != null && errorMessage.isNotBlank()) {
            return Esse3QuestionnaireSubmitResult.ValidationError(errorMessage)
        }

        if (doc.selectFirst("form[action*=QuestionariRiepilogo]") != null || doc.selectFirst("#quest-toolbarEsci") != null || doc.title().contains("Riepilogo", ignoreCase = true)) {
            return Esse3QuestionnaireSubmitResult.Completed
        }

        return Esse3QuestionnaireSubmitResult.NextPage(parseQuestionnairePage(doc))
    }

    /**
     * Navigates to the previous page in a questionnaire.
     *
     * @param page The current questionnaire page
     * @return The previous page
     * @throws HtmlParsingException if navigation back is not possible
     */
    suspend fun previousPage(page: Esse3QuestionnairePage): Esse3QuestionnairePage {
        if (!page.canGoBack) {
            throw HtmlParsingException("Cannot navigate back: this is the first page")
        }

        val formFields = page.navigation.toFormFields().toMutableMap()
        formFields["form_id_quest_form_questionario_pagina"] = "quest_form_questionario_pagina"
        formFields["sbmPrecedente"] = ""

        val doc = executePost(QUESTIONNAIRE_SUBMIT_ENTRYPOINT, formFields)
        return parseQuestionnairePage(doc)
    }

    /**
     * Exits from a questionnaire without completing it.
     *
     * @param page The current questionnaire page
     */
    suspend fun exitQuestionnaire(page: Esse3QuestionnairePage) {
        val formFields = page.navigation.toFormFields().toMutableMap()
        formFields["form_id_quest_form_questionario_pagina"] = "quest_form_questionario_pagina"
        formFields["sbmEsci"] = ""

        executePost(QUESTIONNAIRE_SUBMIT_ENTRYPOINT, formFields)
    }

    private fun parseCourseCodeAndName(text: String): Pair<String, String> {
        val dashIndex = text.indexOf(" - ")
        return if (dashIndex != -1) {
            text.take(dashIndex).trim() to text.substring(dashIndex + 3).trim()
        } else {
            text to text
        }
    }

    private fun parseEvaluationCourseStatus(link: Element?): Esse3EvaluationCourseStatus {
        if (link == null) {
            return Esse3EvaluationCourseStatus.NotAvailable
        }

        val href = link.attr("href")
        val activityId = extractQueryParamAsLong(href, "adsce_id")
            ?: return Esse3EvaluationCourseStatus.NotAvailable

        val imgAlt = link.selectFirst("img")
            ?.attr("alt")
            ?: ""
        return if(imgAlt.contains("Questionario compilato", ignoreCase = true)) {
            Esse3EvaluationCourseStatus.Completed(activityId)
        } else {
            Esse3EvaluationCourseStatus.Pending(activityId)
        }
    }

    private fun parseEvaluationPartitionStatus(link: Element?): Esse3EvaluationPartitionStatus {
        if (link == null) {
            return Esse3EvaluationPartitionStatus.NotAvailable
        }

        val href = link.attr("href")
        if(href.isBlank()) {
            return Esse3EvaluationPartitionStatus.NotAvailable
        }

        val imgAlt = link.selectFirst("img")
            ?.attr("alt")
            ?: ""
        return if(imgAlt.contains("Tutti i questionari compilati", ignoreCase = true)) {
            Esse3EvaluationPartitionStatus.Completed
        } else {
            Esse3EvaluationPartitionStatus.Pending(href)
        }
    }

    private fun parseQuestionnairePage(doc: Document): Esse3QuestionnairePage {
        val form = doc.selectFirst("form[action*=QuestionariPagina]")
            ?: throw HtmlParsingException("Cannot parse questionnaire page: missing form")

        val navigation = extractNavigation(form)
        val title = doc.selectFirst("h2.pagetitle_title")?.text()?.cleanText()
        val sectionTitle = form.selectFirst(".header-h3 h3, .header-h4 h4")?.text()?.cleanText()

        val questions = mutableListOf<Esse3Question>()
        val questionContainers = form.select("fieldset[id^=quest_container_domanda_]")

        for (container in questionContainers) {
            val question = parseQuestion(container)
            if (question != null) {
                questions.add(question)
            }
        }

        val requiredField = form.selectFirst("input[name=lista_obbligatorie]")?.attr("value") ?: ""
        val requiredIds = requiredField.split("|")
            .mapNotNull { it.trim().toLongOrNull() }

        val canGoBack = form.selectFirst("input[name=sbmPrecedente]") != null
        val canGoForward = form.selectFirst("input[name=sbmSuccessivo]") != null

        return Esse3QuestionnairePage(
            navigation = navigation,
            title = title,
            sectionTitle = sectionTitle,
            questions = questions,
            requiredQuestionIds = requiredIds,
            canGoBack = canGoBack,
            canGoForward = canGoForward
        )
    }

    private fun extractNavigation(form: Element): Esse3QuestionnaireNavigation {
        val fields = form.extractHiddenFields()

        val questId = fields["p_quest_id"]?.toLongOrNull()
            ?: throw HtmlParsingException("Cannot extract navigation: missing p_quest_id")
        val questCompId = fields["p_quest_comp_id"]?.toLongOrNull()
            ?: throw HtmlParsingException("Cannot extract navigation: missing p_quest_comp_id")
        val pageId = fields["p_pagina_id"]?.toLongOrNull()
            ?: throw HtmlParsingException("Cannot extract navigation: missing p_pagina_id")

        return Esse3QuestionnaireNavigation(
            questId = questId,
            questCompId = questCompId,
            pageId = pageId,
            userCompId = fields["p_user_comp_id"] ?: "",
            questConfigId = fields["p_quest_config_id"] ?: "",
            redirectUrl = fields["page_redirect"] ?: "",
            eventoCompCod = fields["p_evento_comp_cod"] ?: "",
            adsceId = fields["adsce_id"]?.toLongOrNull() ?: 0L
        )
    }

    private fun parseQuestion(container: Element): Esse3Question? {
        val idMatch = QUESTION_REGEX.find(container.id())
        val questionId = idMatch?.groupValues?.get(1)?.toLongOrNull() ?: return null

        val text = container.selectFirst("legend:not(.no-title)")?.text()?.cleanText()
            ?: container.selectFirst(".header-h4 h4 b")?.text()?.cleanText()
            ?: container.selectFirst("label")?.text()?.cleanText()
            ?: return null

        val cleanText = text.removeSuffix("*").trim()
        val required = text.endsWith("*") ||
                container.selectFirst("[data-required=true], [aria-required=true]") != null

        val radios = container.select("input[type=radio]")
        val checkboxes = container.select("input[type=checkbox]")
        val textarea = container.selectFirst("textarea")
        val textInput = container.selectFirst("input[type=text]")
        val select = container.selectFirst("select")

        return when {
            radios.isNotEmpty() -> {
                val fieldName = radios.first()?.attr("name") ?: return null
                val options = radios.map { radio ->
                    val optionText = radio.parent()?.text()?.cleanText()
                        ?: radio.nextSibling()?.toString()?.cleanText()
                        ?: radio.attr("value")
                    Esse3QuestionOption(
                        value = radio.attr("value"),
                        text = optionText
                    )
                }
                val selectedValue = radios.find { it.hasAttr("checked") }?.attr("value")

                Esse3Question.SingleChoice(
                    id = questionId,
                    fieldName = fieldName,
                    text = cleanText,
                    required = required,
                    options = options,
                    selectedValue = selectedValue
                )
            }

            checkboxes.isNotEmpty() -> {
                val fieldName = checkboxes.first()?.attr("name") ?: return null
                val options = checkboxes.map { checkbox ->
                    val optionText = checkbox.parent()?.text()?.cleanText()
                        ?: checkbox.nextSibling()?.toString()?.cleanText()
                        ?: checkbox.attr("value")
                    Esse3QuestionOption(
                        value = checkbox.attr("value"),
                        text = optionText
                    )
                }
                val selectedValues = checkboxes
                    .filter { it.hasAttr("checked") }
                    .map { it.attr("value") }

                Esse3Question.MultipleChoice(
                    id = questionId,
                    fieldName = fieldName,
                    text = cleanText,
                    required = required,
                    options = options,
                    selectedValues = selectedValues
                )
            }

            select != null -> {
                val fieldName = select.attr("name")
                val options = select.select("option").map { option ->
                    Esse3QuestionOption(
                        value = option.attr("value"),
                        text = option.text().cleanText()
                    )
                }
                val selectedValue = select.selectFirst("option[selected]")?.attr("value")

                Esse3Question.SingleChoice(
                    id = questionId,
                    fieldName = fieldName,
                    text = cleanText,
                    required = required,
                    options = options,
                    selectedValue = selectedValue
                )
            }

            textarea != null -> {
                val fieldName = textarea.attr("name")
                val maxLength = textarea.attr("maxlength").toIntOrNull()
                val currentValue = textarea.text().takeIf { it.isNotBlank() }

                Esse3Question.FreeText(
                    id = questionId,
                    fieldName = fieldName,
                    text = cleanText,
                    required = required,
                    maxLength = maxLength,
                    currentValue = currentValue
                )
            }

            textInput != null -> {
                val fieldName = textInput.attr("name")
                val maxLength = textInput.attr("maxlength").toIntOrNull()
                val currentValue = textInput.attr("value").takeIf { it.isNotBlank() }

                Esse3Question.FreeText(
                    id = questionId,
                    fieldName = fieldName,
                    text = cleanText,
                    required = required,
                    maxLength = maxLength,
                    currentValue = currentValue
                )
            }

            else -> null
        }
    }
}
