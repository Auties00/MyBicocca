package it.attendance100.mybicocca.data.repository.demo

import it.attendance100.mybicocca.domain.model.elearning.course.CompletionState
import it.attendance100.mybicocca.domain.model.elearning.course.CourseDetails
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseModule
import it.attendance100.mybicocca.domain.model.elearning.course.CourseSection
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleContent
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleDate
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleType
import java.time.Instant

// TEMPORARY demo course injected into the e-learning list so every content type the audit
// found can be seen rendering in-app, using real data pulled from the dumped courses. Flip
// ENABLED to false (or delete this file + its 3 hooks in ElearningCourseRepositoryImpl) to
// remove it. File URLs are real pluginfile links from the signed-in user's own courses, so the
// in-app viewers actually open them.
object DemoCourse {

    const val ENABLED = true
    const val DEMO_COURSE_ID = 999001

    val enrolled: EnrolledCourse = EnrolledCourse(
        id = CourseId(DEMO_COURSE_ID),
        shortName = "DEMO-CONTENUTI",
        fullName = "★ Demo — Tutti i tipi di contenuto",
        displayName = "★ Demo — Tutti i tipi di contenuto",
        idNumber = null,
        summary = "Corso dimostrativo che raccoglie un esempio reale di ogni tipo di " +
            "attività e risorsa di Moodle, per verificare come vengono visualizzati.",
        courseImageUrl = null,
        format = "topics",
        language = "it",
        categoryId = null,
        progress = 0.35f,
        completed = false,
        completionEnabled = true,
        startDate = null,
        endDate = null,
        lastAccessDate = null,
        isFavourite = true,
        hidden = false,
        deadlines = emptyList(),
    )

    val completion: Map<Int, CompletionState> = mapOf(
        cmDone(11) to completed(11),
        cmDone(21) to completed(21),
        cmDone(31) to completed(31),
    )

    val details: CourseDetails
        get() = CourseDetails(
            enrolled = enrolled,
            sections = sections,
            staff = emptyList(),
            syllabus = null,
        )

    private val sections: List<CourseSection>
        get() = listOf(
            // Labels: a short header label, a rich content label with a real link, plus a
            // section summary that itself carries a clickable textbook link (M10).
            section(
                id = 1,
                number = 1,
                name = "Note ed etichette",
                summary = "<p>Testo consigliato: A. Albanese, A. Leaci, D. Pallara, " +
                    "<a href=\"https://www.unisalento.it/documents/20152/718619/Appunti+Anlisi+I.pdf\">" +
                    "Appunti del corso di Analisi Matematica I</a> (PDF scaricabile).</p>",
                modules = listOf(
                    mod(
                        cmId = 1,
                        name = "Materiale del corso",
                        type = ModuleType.Label,
                        typeLabel = "Etichetta",
                    ),
                    mod(
                        cmId = 2,
                        name = "Dal manuale di Diritto privato, si consiglia…",
                        type = ModuleType.Label,
                        typeLabel = "Etichetta",
                        description = "<p>Dal manuale di Diritto privato si consiglia di " +
                            "studiare/ripassare i seguenti argomenti:</p><ul>" +
                            "<li>Parte IV — Obbligazioni (161-173)</li>" +
                            "<li>Parte V — Contratti in generale (204-230)</li></ul>" +
                            "<p>Riferimento: <a href=\"https://boa.unimib.it/\">ebook (BOA)</a> · " +
                            "Docente: <a href=\"mailto:alessandro.candido@unimib.it\">alessandro.candido@unimib.it</a></p>",
                    ),
                ),
            ),
            // Files and folders: single-file PDF resource, a 2-file resource (folder sheet),
            // a real folder, a page (in-app HTML), and an indented resource.
            section(
                id = 2,
                number = 2,
                name = "File, cartelle e pagine",
                modules = listOf(
                    mod(
                        cmId = 11,
                        name = "Esercitazione 1 — Note",
                        type = ModuleType.Resource,
                        typeLabel = "File",
                        url = "https://elearning.unimib.it/mod/resource/view.php?id=1146092",
                        description = "<p>Esercizi su notazioni e operazioni insiemistiche, " +
                            "intervalli reali, funzioni inverse.</p>",
                        contents = listOf(
                            file(
                                "Lezione01_Note.pdf",
                                "https://elearning.unimib.it/webservice/pluginfile.php/1587663/mod_resource/content/5/Lezione01_Note.pdf?forcedownload=1",
                                "application/pdf",
                                4_213_586,
                            ),
                        ),
                    ),
                    mod(
                        cmId = 12,
                        name = "Materiali aggiuntivi (2 file)",
                        type = ModuleType.Resource,
                        typeLabel = "File",
                        indent = 1,
                        contents = listOf(
                            file(
                                "esempi-I-prova-parziale.pdf",
                                "https://elearning.unimib.it/webservice/pluginfile.php/2020689/mod_resource/content/15/esempi-I-prova-parziale.pdf?forcedownload=1",
                                "application/pdf",
                                512_000,
                            ),
                            file(
                                "parzialeII-gen25.pdf",
                                "https://elearning.unimib.it/webservice/pluginfile.php/2060907/mod_resource/content/16/parzialeII-gen25.pdf?forcedownload=1",
                                "application/pdf",
                                480_000,
                            ),
                        ),
                    ),
                    mod(
                        cmId = 13,
                        name = "Correzioni simulazione primo itinere",
                        type = ModuleType.Folder,
                        typeLabel = "Cartella",
                        description = "<p>Prove corrette: come correggiamo e cosa ci aspettiamo " +
                            "nelle soluzioni.</p>",
                        contents = listOf(
                            file(
                                "Simulazione_Correzione1.pdf",
                                "https://elearning.unimib.it/webservice/pluginfile.php/1624292/mod_folder/content/1/Simulazione_Correzione1.pdf?forcedownload=1",
                                "application/pdf",
                                1_070_150,
                            ),
                            file(
                                "Simulazione_Correzione2.pdf",
                                "https://elearning.unimib.it/webservice/pluginfile.php/1624292/mod_folder/content/1/Simulazione_Correzione2.pdf?forcedownload=1",
                                "application/pdf",
                                980_000,
                            ),
                        ),
                    ),
                    mod(
                        cmId = 14,
                        name = "Tabella corrispondenze argomenti/materiali",
                        type = ModuleType.Page,
                        typeLabel = "Pagina",
                        url = "https://elearning.unimib.it/mod/page/view.php?id=1231083",
                        description = "<p>Pagina HTML renderizzata in-app.</p>",
                        contents = listOf(
                            file(
                                "index.html",
                                "https://elearning.unimib.it/webservice/pluginfile.php/1678713/mod_page/content/index.html?forcedownload=1",
                                null,
                                0,
                            ),
                        ),
                    ),
                ),
            ),
            // Every openable file type, one real example each (audio has no example anywhere
            // in the dumps, so it is omitted). In-app viewers: images, video, HTML, text/code,
            // zip. External hand-off: PDF (default reader) and Office.
            section(
                id = 99,
                number = 99,
                name = "Tutti i tipi di file",
                modules = listOf(
                    mod(101, "Immagini (in-app)", ModuleType.Label, typeLabel = "Etichetta"),
                    fileMod(102, "Immagine PNG", "immagine.png",
                        "https://elearning.unimib.it/webservice/pluginfile.php/1287222/mod_folder/content/9/Tutoraggio%203%20-%2008-04-2022.png?forcedownload=1",
                        "image/png", 556_714),
                    fileMod(103, "Immagine JPG", "foto.jpg",
                        "https://elearning.unimib.it/webservice/pluginfile.php/1271941/mod_resource/content/1/7-3-22.jpg?forcedownload=1",
                        "image/jpeg", 1_001_696),
                    fileMod(104, "GIF animata", "animazione.gif",
                        "https://elearning.unimib.it/webservice/pluginfile.php/1778879/mod_resource/content/3/tutorial/Tutorial%20JFLAP_files/state.gif?forcedownload=1",
                        "image/gif", 106),
                    fileMod(105, "Immagine SVG", "vettoriale.svg",
                        "https://elearning.unimib.it/webservice/pluginfile.php/1287222/mod_folder/content/9/Tutoraggio%201%20-%2025-03-2022.svg?forcedownload=1",
                        "image/svg+xml", 2_115_986),

                    mod(110, "Video (in-app)", ModuleType.Label, typeLabel = "Etichetta"),
                    fileMod(111, "Video MP4", "video.mp4",
                        "https://elearning.unimib.it/webservice/pluginfile.php/1785645/mod_resource/content/2/Intro-2.mp4?forcedownload=1",
                        "video/mp4", 2_926_725),

                    mod(120, "Pagina HTML e archivi (in-app)", ModuleType.Label, typeLabel = "Etichetta"),
                    fileMod(121, "Pagina HTML", "pagina.html",
                        "https://elearning.unimib.it/webservice/pluginfile.php/2077389/mod_page/content/index.html?forcedownload=1",
                        "text/html", 0),
                    fileMod(122, "Archivio ZIP", "archivio.zip",
                        "https://elearning.unimib.it/webservice/pluginfile.php/1366674/mod_resource/content/1/Screenshot%20%28222%29.zip?forcedownload=1",
                        "application/zip", 1_651_391),
                    fileMod(123, "Java Archive (JAR)", "tool.jar",
                        "https://elearning.unimib.it/webservice/pluginfile.php/2091614/mod_page/content/52/CheckPICT_Results.jar?forcedownload=1",
                        "application/java-archive", 8_952),

                    mod(130, "Testo e codice (in-app)", ModuleType.Label, typeLabel = "Etichetta"),
                    fileMod(131, "Testo TXT", "note.txt",
                        "https://elearning.unimib.it/webservice/pluginfile.php/2091676/mod_page/content/28/crypted.txt?forcedownload=1",
                        "text/plain", 256),
                    fileMod(132, "CSV", "dati.csv",
                        "https://elearning.unimib.it/webservice/pluginfile.php/1973215/mod_folder/content/8/EsperienzaComputazionale.csv?forcedownload=1",
                        "text/csv", 1_825),
                    fileMod(133, "JSON", "config.json",
                        "https://elearning.unimib.it/webservice/pluginfile.php/2047812/mod_resource/content/7/input_json.json?forcedownload=1",
                        "application/json", 1_886),
                    fileMod(134, "Codice Java", "Chess.java",
                        "https://elearning.unimib.it/webservice/pluginfile.php/1739263/mod_resource/content/1/Chess.java?forcedownload=1",
                        "text/plain", 2_007),
                    fileMod(135, "Codice Python", "postfissa.py",
                        "https://elearning.unimib.it/webservice/pluginfile.php/2047976/mod_resource/content/5/postfissa.py?forcedownload=1",
                        null, 392),
                    fileMod(136, "Codice C++", "main.cpp",
                        "https://elearning.unimib.it/webservice/pluginfile.php/2044942/mod_resource/content/8/main.cpp?forcedownload=1",
                        "text/plain", 3_560),
                    fileMod(137, "Script SQL", "query.sql",
                        "https://elearning.unimib.it/webservice/pluginfile.php/1903617/mod_folder/content/1/es4_a_mano.sql?forcedownload=1",
                        "text/plain", 3_289),

                    mod(140, "PDF e Office (apertura esterna)", ModuleType.Label, typeLabel = "Etichetta"),
                    fileMod(141, "Documento PDF", "regolamento.pdf",
                        "https://elearning.unimib.it/webservice/pluginfile.php/1395235/mod_resource/content/9/tabella%20riassuntiva%20regolamento%20didattico%20LT%20Informatica%202022-23.pdf?forcedownload=1",
                        "application/pdf", 503_504),
                    fileMod(142, "Word (DOCX)", "documento.docx",
                        "https://elearning.unimib.it/webservice/pluginfile.php/985321/mod_folder/content/10/Multiple-choice%20exam%202%20-%20Solutions.docx?forcedownload=1",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 15_536),
                    fileMod(143, "Word (DOC)", "programma.doc",
                        "https://elearning.unimib.it/webservice/pluginfile.php/1675268/mod_resource/content/1/ProgCorsoPubblicato.doc?forcedownload=1",
                        "application/msword", 19_968),
                    fileMod(144, "Excel (XLSX)", "voti.xlsx",
                        "https://elearning.unimib.it/webservice/pluginfile.php/1457579/mod_resource/content/1/E3101Q132-Algebra%20Lineare%20e%20geometria%20-%2025%20gennaio%202023%20%28LAB%201401%29-valutazioni-pubb.xlsx?forcedownload=1",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 8_636),
                    fileMod(145, "Excel (XLS)", "aule.xls",
                        "https://elearning.unimib.it/webservice/pluginfile.php/1827264/mod_resource/content/1/Suddivisione%20aule.xls?forcedownload=1",
                        "application/vnd.ms-excel", 51_200),
                    fileMod(146, "PowerPoint (PPTX)", "slide.pptx",
                        "https://elearning.unimib.it/webservice/pluginfile.php/851804/mod_resource/content/1/Prima%20pillola%20di%20statistica.pptx?forcedownload=1",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation", 1_036_207),
                    fileMod(147, "PowerPoint (PPT)", "pillola.ppt",
                        "https://elearning.unimib.it/webservice/pluginfile.php/864060/mod_resource/content/1/Seconda%20pillola%20di%20statistica.ppt?forcedownload=1",
                        "application/vnd.ms-powerpoint", 1_097_728),
                    fileMod(148, "PowerPoint show (PPSX)", "SR-Latch.ppsx",
                        "https://elearning.unimib.it/webservice/pluginfile.php/1686946/mod_resource/content/1/SR-Latch.ppsx?forcedownload=1",
                        "application/vnd.openxmlformats-officedocument.presentationml.slideshow", 459_110),
                ),
            ),
            // Video (Kaltura) — description carries a link to the printed handout.
            section(
                id = 3,
                number = 3,
                name = "Video",
                modules = listOf(
                    mod(
                        cmId = 21,
                        name = "AM-2324-T2 · Lezione 01",
                        type = ModuleType.Kalvidres,
                        typeLabel = "Kaltura Video Resource",
                        url = "https://elearning.unimib.it/mod/kalvidres/view.php?id=1145171",
                        description = "<p>Presentazione del corso. Insiemi: definizione, " +
                            "operazioni, prodotto cartesiano. " +
                            "<a href=\"https://elearning.unimib.it/pluginfile.php/1586538/mod_kalvidres/intro/AM-2324-T2_L01.pdf\">" +
                            "Stampato della lezione</a>.</p>",
                    ),
                ),
            ),
            // Graded / interactive activities, with real deadlines parsed by dataId.
            section(
                id = 4,
                number = 4,
                name = "Attività valutate",
                modules = listOf(
                    mod(
                        cmId = 31,
                        name = "Laboratorio 2 — Quiz 1 (feedback immediato)",
                        type = ModuleType.Quiz,
                        typeLabel = "Quiz",
                        url = "https://elearning.unimib.it/mod/quiz/view.php?id=1245184",
                        description = "<p>Argomenti: Logisim, logica combinatoria e " +
                            "sequenziale, finite state machines.</p>",
                    ),
                    mod(
                        cmId = 32,
                        name = "Consegna secondo laboratorio",
                        type = ModuleType.Assign,
                        typeLabel = "Compiti",
                        url = "https://elearning.unimib.it/mod/assign/view.php?id=1508997",
                        description = "<p>Scrivere una grammatica context-free per file in " +
                            "<a href=\"https://www.json.org\">formato JSON</a> e generare il parser.</p>",
                        dates = listOf(
                            date("Aperto:", 1_764_025_200, "allowsubmissionsfromdate"),
                            date("Data limite:", 1_781_474_340, "duedate"),
                        ),
                    ),
                    mod(
                        cmId = 33,
                        name = "Rifiuto del voto (entro lun 23 feb ore 10:00)",
                        type = ModuleType.Choice,
                        typeLabel = "Scelte",
                        url = "https://elearning.unimib.it/mod/choice/view.php?id=1566522",
                        description = "<p>Solo per chi intende RIFIUTARE il voto.</p>",
                        dates = listOf(date("Chiuso:", 1_771_837_200, "timeclose")),
                    ),
                    mod(
                        cmId = 34,
                        name = "Feedback studenti",
                        type = ModuleType.Feedback,
                        typeLabel = "Feedback",
                        url = "https://elearning.unimib.it/mod/feedback/view.php?id=694160",
                        description = "<p>Sondaggio anonimo sul materiale e sull'offerta formativa.</p>",
                        dates = listOf(date("Chiuso:", 1_618_480_800, "timeclose")),
                    ),
                    mod(
                        cmId = 35,
                        name = "Esercizio 12.1 — automi a pila",
                        type = ModuleType.Lesson,
                        typeLabel = "Lezioni",
                        url = "https://elearning.unimib.it/mod/lesson/view.php?id=1453752",
                        description = "<p>Esercizio guidato sugli automi a pila deterministici.</p>",
                    ),
                ),
            ),
            // External / browser-handoff types, now properly labelled. Includes a password-gated
            // (restricted) Webex shown as a locked row.
            section(
                id = 5,
                number = 5,
                name = "Strumenti esterni e altri tipi",
                modules = listOf(
                    mod(
                        cmId = 41,
                        name = "Java (JDK) SE 21 Documentation",
                        type = ModuleType.Url,
                        typeLabel = "URL",
                        url = "https://elearning.unimib.it/mod/url/view.php?id=1135544",
                        description = "<p>Documentazione ufficiale. " +
                            "<a href=\"https://docs.oracle.com/en/java/javase/21\">Versione online</a>.</p>",
                        contents = listOf(
                            ModuleContent(
                                type = "url",
                                fileName = "Java SE 21 Documentation",
                                filePath = null,
                                fileUrl = "https://docs.oracle.com/en/java/javase/21/docs/api/index.html",
                                mimeType = null,
                                sizeBytes = null,
                                timeModified = null,
                            ),
                        ),
                    ),
                    mod(
                        cmId = 42,
                        name = "01 — Chi siete?",
                        type = ModuleType.Wooclap,
                        typeLabel = "Wooclap",
                        url = "https://elearning.unimib.it/mod/wooclap/view.php?id=1143029",
                    ),
                    mod(
                        cmId = 43,
                        name = "Test 1: Cinematica e vettori",
                        type = ModuleType.Lti,
                        typeLabel = "Tool Esterni",
                        url = "https://elearning.unimib.it/mod/lti/view.php?id=1357350",
                    ),
                    mod(
                        cmId = 44,
                        name = "Date orali giugno",
                        type = ModuleType.Webex,
                        typeLabel = "Webex",
                        url = "https://elearning.unimib.it/mod/webexunimib/view.php?id=1113303",
                        description = "<p>Password: M29623</p>",
                        accessible = false,
                        availabilityInfo = "Condizioni per l'accesso: inserisci la password corretta",
                    ),
                    mod(
                        cmId = 45,
                        name = "Discussioni su sistemi operativi (forum anonimo)",
                        type = ModuleType.HsuForum,
                        typeLabel = "Open Forums",
                        url = "https://elearning.unimib.it/mod/hsuforum/view.php?id=1311809",
                        afterLink = "3 messaggi non letti",
                        description = "<p>Forum anonimo per fare domande al tutor.</p>",
                    ),
                    mod(
                        cmId = 46,
                        name = "Forum Laboratorio di Architettura",
                        type = ModuleType.Forum,
                        typeLabel = "Forum",
                        url = "https://elearning.unimib.it/mod/forum/view.php?id=1237228",
                        afterLink = "7 messaggi non letti",
                        description = "<p>Forum per chiarimenti sugli esercizi.</p>",
                    ),
                    mod(
                        cmId = 47,
                        name = "Prenotazione prova orale del 31/01",
                        type = ModuleType.Reservation,
                        typeLabel = "Prenotazioni",
                        url = "https://elearning.unimib.it/mod/reservation/view.php?id=1380085",
                        description = "<p>È ora possibile prenotarsi, fino alle ore 16 del 30/01.</p>",
                    ),
                    mod(
                        cmId = 48,
                        name = "Iscrizione Laboratorio Turno T2",
                        type = ModuleType.ChoiceGroup,
                        typeLabel = "Scelta gruppi",
                        url = "https://elearning.unimib.it/mod/choicegroup/view.php?id=1235777",
                        dates = listOf(
                            date("Aperto:", 1_710_025_200, "timeopen"),
                            date("Chiude:", 1_711_061_940, "timeclose"),
                        ),
                    ),
                    mod(
                        cmId = 49,
                        name = "Registro iscrizioni GitHub [A-L]",
                        type = ModuleType.Database,
                        typeLabel = "Database",
                        url = "https://elearning.unimib.it/mod/data/view.php?id=1382401",
                    ),
                    mod(
                        cmId = 50,
                        name = "Presenze Laboratorio",
                        type = ModuleType.Attendance,
                        typeLabel = "Presenze",
                        url = "https://elearning.unimib.it/mod/attendance/view.php?id=1573361",
                    ),
                    mod(
                        cmId = 51,
                        name = "Agenda ricevimento",
                        type = ModuleType.Scheduler,
                        typeLabel = "Schedulatori",
                        url = "https://elearning.unimib.it/mod/scheduler/view.php?id=1492072",
                    ),
                    mod(
                        cmId = 52,
                        name = "Programmazione Non Lineare (slides + video)",
                        type = ModuleType.Book,
                        typeLabel = "Libri",
                        url = "https://elearning.unimib.it/mod/book/view.php?id=1462066",
                        description = "<p>Programmazione non lineare multivariata, vincolata e non.</p>",
                    ),
                ),
            ),
            // Subsections: two placeholder modules whose real content lives in the child
            // sections below (filtered from the top level, inlined under the placeholders).
            section(
                id = 6,
                number = 6,
                name = "Sottosezioni (mod_subsection)",
                modules = listOf(
                    mod(
                        cmId = 61,
                        name = "PROGRAMMA DELL'INSEGNAMENTO",
                        type = ModuleType.Subsection,
                        typeLabel = "Sottosezione",
                        instanceId = 174,
                        linkedSectionId = 333817,
                    ),
                    mod(
                        cmId = 62,
                        name = "TEMI D'ESAME",
                        type = ModuleType.Subsection,
                        typeLabel = "Sottosezione",
                        instanceId = 1004,
                        linkedSectionId = 340360,
                    ),
                ),
            ),
            // Child sections — component = mod_subsection, hidden from the top level.
            section(
                id = 333817,
                number = 7,
                name = "PROGRAMMA DELL'INSEGNAMENTO",
                component = "mod_subsection",
                itemId = 174,
                modules = listOf(
                    mod(
                        cmId = 71,
                        name = "Programma del corso",
                        type = ModuleType.Label,
                        typeLabel = "Etichetta",
                        description = "<p><strong>1. Strumenti matematici (ripasso)</strong></p><ul>" +
                            "<li>Crescita delle funzioni, notazioni asintotiche</li>" +
                            "<li>Ricorsione e algoritmi ricorsivi</li></ul>" +
                            "<p><strong>2. Programmazione dinamica</strong></p><ul>" +
                            "<li>Sottostruttura ottima, equazioni di ricorrenza</li></ul>" +
                            "<p><strong>3. Metodo greedy, matroidi</strong></p>",
                    ),
                ),
            ),
            section(
                id = 340360,
                number = 8,
                name = "TEMI D'ESAME",
                component = "mod_subsection",
                itemId = 1004,
                modules = listOf(
                    mod(
                        cmId = 72,
                        name = "Esempi prima prova parziale",
                        type = ModuleType.Resource,
                        typeLabel = "File",
                        contents = listOf(
                            file(
                                "esempi-I-prova-parziale.pdf",
                                "https://elearning.unimib.it/webservice/pluginfile.php/2020689/mod_resource/content/15/esempi-I-prova-parziale.pdf?forcedownload=1",
                                "application/pdf",
                                512_000,
                            ),
                        ),
                    ),
                    mod(
                        cmId = 73,
                        name = "Esempio seconda prova parziale",
                        type = ModuleType.Resource,
                        typeLabel = "File",
                        contents = listOf(
                            file(
                                "parzialeII-gen25.pdf",
                                "https://elearning.unimib.it/webservice/pluginfile.php/2060907/mod_resource/content/16/parzialeII-gen25.pdf?forcedownload=1",
                                "application/pdf",
                                480_000,
                            ),
                        ),
                    ),
                ),
            ),
        )

    private fun cmDone(cmId: Int) = cmId
    private fun completed(cmId: Int) = CompletionState(
        cmId = cmId,
        isCompleted = true,
        completedAt = null,
        isManual = false,
        isAutomatic = true,
        isTracked = true,
    )

    private fun mod(
        cmId: Int,
        name: String,
        type: ModuleType,
        typeLabel: String? = null,
        description: String? = null,
        url: String? = null,
        instanceId: Int? = cmId,
        accessible: Boolean = true,
        availabilityInfo: String? = null,
        onCoursePage: Boolean = true,
        indent: Int = 0,
        afterLink: String? = null,
        linkedSectionId: Int? = null,
        contents: List<ModuleContent> = emptyList(),
        dates: List<ModuleDate> = emptyList(),
    ): CourseModule = CourseModule(
        cmId = cmId,
        instanceId = instanceId,
        name = name,
        type = type,
        typeLabel = typeLabel,
        description = description,
        url = url,
        iconUrl = null,
        visible = true,
        accessible = accessible,
        availabilityInfo = availabilityInfo,
        onCoursePage = onCoursePage,
        indent = indent,
        afterLink = afterLink,
        linkedSectionId = linkedSectionId,
        contents = contents,
        completion = null,
        dates = dates,
    )

    private fun file(name: String, url: String, mime: String?, size: Long): ModuleContent =
        ModuleContent(
            type = "file",
            fileName = name,
            filePath = "/",
            fileUrl = url,
            mimeType = mime,
            sizeBytes = size,
            timeModified = null,
        )

    // A single-file resource — opens in the in-app viewer or hands off externally per file type.
    private fun fileMod(cmId: Int, name: String, fileName: String, url: String, mime: String?, size: Long): CourseModule =
        mod(
            cmId = cmId,
            name = name,
            type = ModuleType.Resource,
            typeLabel = "File",
            contents = listOf(file(fileName, url, mime, size)),
        )

    private fun date(label: String, epochSeconds: Long, dataId: String): ModuleDate =
        ModuleDate(label = label, instant = Instant.ofEpochSecond(epochSeconds), dataId = dataId)

    private fun section(
        id: Int,
        number: Int,
        name: String,
        summary: String? = null,
        component: String? = null,
        itemId: Int? = null,
        modules: List<CourseModule>,
    ): CourseSection = CourseSection(
        id = id,
        sectionNumber = number,
        name = name,
        summary = summary,
        visible = true,
        component = component,
        itemId = itemId,
        modules = modules,
    )
}
