package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import it.attendance100.mybicocca.data.dto.esse3.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * API for questionnaire operations.
 *
 * Provides access to:
 * - List pending questionnaires
 * - Fill questionnaires
 * - Submit questionnaire pages
 * - View questionnaire summary
 */
class Esse3QuestionnaireApi(
    client: HttpClient
) : Esse3AbstractApi(client) {
    /**
     * Gets the list of pending questionnaires.
     *
     * @return List of pending questionnaires
     */
    suspend fun getPendingQuestionnaires(): List<Esse3Questionnaire> {
        val doc = executeGet(
            "/auth/studente/Questionari/QuestListaQuest.do",
            mapOf("menu_opened_cod" to "menu_link-navbox_studenti_Questionari")
        )
        return parseQuestionnaires(doc)
    }

    /**
     * Gets course evaluation questionnaires from libretto.
     *
     * @return List of course evaluations with questionnaire links
     */
    suspend fun getCourseEvaluations(): List<Esse3CourseEvaluation> {
        val doc = executeGet(
            "/auth/studente/Questionari/QValutazioneDidattica.do",
            mapOf("menu_opened_cod" to "menu_link-navbox_studenti_Questionari")
        )
        return parseCourseEvaluations(doc)
    }

    /**
     * Starts filling a questionnaire.
     *
     * @param questionnaire the questionnaire to start filling
     * @return The first page of the questionnaire
     */
    suspend fun startQuestionnaire(questionnaire: Esse3Questionnaire): Esse3QuestionnairePage? {
        val doc = executeGet(
            "/auth/studente/Questionari/QuestCompila.do",
            mapOf(
                "p_quest_id" to questionnaire.id.toString(),
                "p_quest_comp_id" to questionnaire.compositionId.toString()
            )
        )
        return parseQuestionnairePage(doc)
    }

    /**
     * Gets a specific questionnaire page.
     *
     * @param navigation The navigation info containing IDs
     * @return The questionnaire page
     */
    suspend fun getQuestionnairePage(
        navigation: Esse3QuestionnaireNavigation
    ): Esse3QuestionnairePage? {
        val doc = executeGet(
            "/auth/studente/Questionari/QuestCompila.do",
            navigation.toFormFields()
        )
        return parseQuestionnairePage(doc)
    }

    /**
     * Submits answers for a questionnaire page and moves to next page.
     *
     * @param navigation The navigation info
     * @param answers Map of field names to answer values
     * @return The next page or null if questionnaire is complete
     */
    suspend fun submitPage(
        navigation: Esse3QuestionnaireNavigation,
        answers: Map<String, String>
    ): Esse3QuestionnairePage? {
        val formFields = navigation.toFormFields().toMutableMap()
        formFields.putAll(answers)
        formFields["form_id_QuestForm"] = "QuestForm"
        formFields["sbmAvanti"] = ""

        val doc = executePost(
            "/auth/studente/Questionari/QuestCompilaSubmit.do",
            formFields
        )

        // Check if we're on a summary page or next page
        if (doc.selectFirst("div.quest-summary, .questionnaire-complete") != null) {
            return null // Questionnaire complete
        }

        return parseQuestionnairePage(doc)
    }

    /**
     * Goes back to the previous page in a questionnaire.
     *
     * @param navigation The navigation info
     * @return The previous page
     */
    suspend fun previousPage(
        navigation: Esse3QuestionnaireNavigation
    ): Esse3QuestionnairePage? {
        val formFields = navigation.toFormFields().toMutableMap()
        formFields["form_id_QuestForm"] = "QuestForm"
        formFields["sbmIndietro"] = ""

        val doc = executePost(
            "/auth/studente/Questionari/QuestCompilaSubmit.do",
            formFields
        )
        return parseQuestionnairePage(doc)
    }

    /**
     * Gets the questionnaire summary after completion.
     *
     * @param questId The questionnaire ID
     * @param questCompId The questionnaire composition ID
     * @return The summary document
     */
    suspend fun getQuestionnaireSummary(
        questId: Long,
        questCompId: Long
    ): Document {
        return executeGet(
            "/auth/studente/Questionari/QuestRiepilogo.do",
            mapOf(
                "p_quest_id" to questId.toString(),
                "p_quest_comp_id" to questCompId.toString()
            )
        )
    }

    /**
     * Extracts navigation info from a questionnaire page.
     *
     * @param doc The questionnaire page document
     * @return Navigation info or null if not found
     */
    fun extractNavigation(doc: Document): Esse3QuestionnaireNavigation? {
        val form = doc.selectFirst("form[action*=QuestCompila]") ?: return null
        val fields = form.extractHiddenFields()

        val questId = fields["p_quest_id"]?.toLongOrNull() ?: return null
        val questCompId = fields["p_quest_comp_id"]?.toLongOrNull() ?: return null
        val pageId = fields["p_pagina_id"]?.toLongOrNull() ?: 0L

        return Esse3QuestionnaireNavigation(
            questId = questId,
            questCompId = questCompId,
            pageId = pageId,
            userCompId = fields["p_user_comp_id"],
            questConfigId = fields["p_quest_config_id"],
            redirectUrl = fields["page_redirect"],
            eventoCompCod = fields["p_evento_comp_cod"],
            adsceId = fields["adsce_id"]?.toLongOrNull()
        )
    }

    private fun parseQuestionnaires(doc: Document): List<Esse3Questionnaire> {
        val questionnaires = mutableListOf<Esse3Questionnaire>()

        val table = doc.selectFirst("table.table-1")
        if (table != null) {
            val rows = table.select("tbody tr, tr:has(td)")
            for (row in rows) {
                val cells = row.select("td")
                if (cells.size >= 2) {
                    // Extract IDs from link
                    val link = row.selectFirst("a[href*=p_quest_id]")?.attr("href") ?: ""
                    val questIdMatch = "p_quest_id=(\\d+)".toRegex().find(link)
                    val questCompIdMatch = "p_quest_comp_id=(\\d+)".toRegex().find(link)

                    val questId = questIdMatch?.groupValues?.get(1)?.toLongOrNull() ?: continue
                    val questCompId = questCompIdMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L

                    val title = cells[0].text().cleanText()
                    val statusText = cells.getOrNull(1)?.text()?.cleanText() ?: ""
                    val status = Esse3QuestionnaireStatus.fromString(statusText)

                    // Check if required (usually marked with asterisk or specific text)
                    val required = cells[0].text().contains("*") ||
                            row.text().contains("obbligatorio", ignoreCase = true)

                    // Extract course code and name from title or dedicated columns
                    val titleText = title
                    val codeMatch = "\\[([A-Z0-9]+)\\]".toRegex().find(titleText)
                        ?: "^([A-Z0-9]+)\\s*-".toRegex().find(titleText)
                    val courseCode = codeMatch?.groupValues?.get(1)
                        ?: cells.getOrNull(2)?.text()?.cleanText()?.takeIf { it.matches("[A-Z0-9]+".toRegex()) }

                    val courseName = when {
                        codeMatch != null -> titleText.replace("\\[.*?\\]".toRegex(), "")
                            .replace("^[A-Z0-9]+\\s*-\\s*".toRegex(), "").trim()
                        else -> cells.getOrNull(3)?.text()?.cleanText()
                    }?.takeIf { it.isNotBlank() && it != title }

                    // Extract professor from dedicated column or from title
                    val professor = cells.getOrNull(4)?.text()?.cleanText()?.takeIf { it.isNotBlank() }
                        ?: "(?:Docente|Prof)[.:]?\\s*(.+?)(?:\\s{2,}|$)".toRegex()
                            .find(row.text())?.groupValues?.get(1)?.trim()

                    questionnaires.add(
                        Esse3Questionnaire(
                            id = questId,
                            compositionId = questCompId,
                            title = title,
                            status = status,
                            required = required,
                            courseCode = courseCode,
                            courseName = courseName,
                            professor = professor
                        )
                    )
                }
            }
        }

        return questionnaires
    }

    private fun parseCourseEvaluations(doc: Document): List<Esse3CourseEvaluation> {
        val evaluations = mutableListOf<Esse3CourseEvaluation>()

        // This endpoint may have a different structure than ValDid table
        // Table columns vary: some show [code] courseName, professor, activity type
        val table = doc.selectFirst("table.table-1")
        if (table != null) {
            val rows = table.select("tbody tr, tr:has(td)")
            for (row in rows) {
                val cells = row.select("td")
                if (cells.size >= 2) {
                    val courseText = cells[0].text().cleanText()

                    // Try to extract code from [CODE] format or CODE - format
                    val bracketMatch = "\\[([A-Z0-9]+)\\]".toRegex().find(courseText)
                    val dashMatch = "^([A-Z0-9]+)\\s*-\\s*(.+)$".toRegex().find(courseText)

                    val code = bracketMatch?.groupValues?.get(1)
                        ?: dashMatch?.groupValues?.get(1)
                        ?: courseText.substringBefore(" ")
                    val name = when {
                        bracketMatch != null -> courseText.replace("\\[.*?\\]".toRegex(), "").trim()
                        dashMatch != null -> dashMatch.groupValues[2]
                        else -> courseText
                    }

                    val link = row.selectFirst("a[href*=Quest]")?.attr("href")

                    evaluations.add(
                        Esse3CourseEvaluation(
                            courseCode = code,
                            courseName = name,
                            courseYear = 0, // Not available in this endpoint
                            credits = 0, // Not available in this endpoint
                            academicYear = null, // Not available in this endpoint
                            status = null, // Not available in this endpoint
                            isRecognized = false, // Not available in this endpoint
                            questionnaireUrl = link?.let { "$BASE_URL$it" }
                        )
                    )
                }
            }
        }

        return evaluations
    }

    private fun parseQuestionnairePage(doc: Document): Esse3QuestionnairePage? {
        val form = doc.selectFirst("form[action*=QuestCompila]") ?: return null
        val fields = form.extractHiddenFields()

        val pageId = fields["p_pagina_id"]?.toLongOrNull() ?: 0L
        val pageNumber = doc.selectFirst("span.page-number, .quest-page-num")?.text()
            ?.let { "\\d+".toRegex().find(it)?.value?.toIntOrNull() } ?: 1

        val title = doc.selectFirst("h2.quest-title, div.quest-header h2")?.text()?.cleanText()

        val questions = mutableListOf<Esse3Question>()
        val questionElements = form.select("div.quest-question, div.question, fieldset.question")

        for ((index, element) in questionElements.withIndex()) {
            val question = parseQuestion(element, index)
            if (question != null) {
                questions.add(question)
            }
        }

        // If no structured questions found, try parsing from table/list format
        if (questions.isEmpty()) {
            questions.addAll(parseQuestionsFromTable(form))
        }

        return Esse3QuestionnairePage(
            id = pageId,
            pageNumber = pageNumber,
            title = title,
            questions = questions
        )
    }

    private fun parseQuestion(element: Element, index: Int): Esse3Question? {
        val text = element.selectFirst("label, .question-text, legend")?.text()?.cleanText()
            ?: element.ownText().cleanText()
        if (text.isBlank()) return null

        val required = element.selectFirst(".required, .obbligatorio") != null ||
                text.contains("*")

        // Determine question type based on input elements
        val radios = element.select("input[type=radio]")
        val checkboxes = element.select("input[type=checkbox]")
        val textInput = element.selectFirst("input[type=text], textarea")
        val select = element.selectFirst("select")

        return when {
            radios.isNotEmpty() -> {
                val fieldName = radios.first()?.attr("name") ?: "q_$index"
                val options = radios.map { radio ->
                    Esse3QuestionOption(
                        id = radio.attr("value").toLongOrNull(),
                        value = radio.attr("value"),
                        text = radio.parent()?.text()?.cleanText()
                            ?: radio.nextElementSibling()?.text()?.cleanText()
                            ?: radio.attr("value")
                    )
                }
                val selected = radios.find { it.hasAttr("checked") }?.attr("value")

                Esse3Question.SingleChoice(
                    id = index.toLong(),
                    text = text.replace("*", "").trim(),
                    required = required,
                    fieldName = fieldName,
                    options = options,
                    selectedValue = selected
                )
            }

            checkboxes.isNotEmpty() -> {
                val fieldName = checkboxes.first()?.attr("name") ?: "q_$index"
                val options = checkboxes.map { checkbox ->
                    Esse3QuestionOption(
                        id = checkbox.attr("value").toLongOrNull(),
                        value = checkbox.attr("value"),
                        text = checkbox.parent()?.text()?.cleanText()
                            ?: checkbox.nextElementSibling()?.text()?.cleanText()
                            ?: checkbox.attr("value")
                    )
                }
                val selected = checkboxes.filter { it.hasAttr("checked") }
                    .map { it.attr("value") }

                Esse3Question.MultipleChoice(
                    id = index.toLong(),
                    text = text.replace("*", "").trim(),
                    required = required,
                    fieldName = fieldName,
                    options = options,
                    selectedValues = selected
                )
            }

            select != null -> {
                val fieldName = select.attr("name")
                val options = select.select("option").map { option ->
                    Esse3QuestionOption(
                        id = option.attr("value").toLongOrNull(),
                        value = option.attr("value"),
                        text = option.text().cleanText()
                    )
                }
                val selected = select.selectFirst("option[selected]")?.attr("value")

                Esse3Question.SingleChoice(
                    id = index.toLong(),
                    text = text.replace("*", "").trim(),
                    required = required,
                    fieldName = fieldName,
                    options = options,
                    selectedValue = selected
                )
            }

            textInput != null -> {
                val fieldName = textInput.attr("name")
                val maxLength = textInput.attr("maxlength").toIntOrNull()
                val currentValue = textInput.attr("value").takeIf { it.isNotBlank() }
                    ?: textInput.text().takeIf { it.isNotBlank() }

                Esse3Question.FreeText(
                    id = index.toLong(),
                    text = text.replace("*", "").trim(),
                    required = required,
                    fieldName = fieldName,
                    maxLength = maxLength,
                    currentValue = currentValue
                )
            }

            else -> null
        }
    }

    private fun parseQuestionsFromTable(form: Element): List<Esse3Question> {
        val questions = mutableListOf<Esse3Question>()

        // Look for rating scale tables (common in course evaluations)
        val ratingTable = form.selectFirst("table.rating-scale, table.valutazione")
        if (ratingTable != null) {
            val rows = ratingTable.select("tr:has(td)")
            for ((index, row) in rows.withIndex()) {
                val questionText = row.selectFirst("td:first-child")?.text()?.cleanText() ?: continue
                val radios = row.select("input[type=radio]")

                if (radios.isNotEmpty()) {
                    val fieldName = radios.first()?.attr("name") ?: "rating_$index"
                    val values = radios.map { it.attr("value").toIntOrNull() ?: 0 }
                    val minValue = values.minOrNull() ?: 1
                    val maxValue = values.maxOrNull() ?: 5
                    val selected = radios.find { it.hasAttr("checked") }?.attr("value")?.toIntOrNull()

                    questions.add(
                        Esse3Question.Rating(
                            id = index.toLong(),
                            text = questionText,
                            required = row.text().contains("*"),
                            fieldName = fieldName,
                            minValue = minValue,
                            maxValue = maxValue,
                            selectedValue = selected
                        )
                    )
                }
            }
        }

        return questions
    }
}
