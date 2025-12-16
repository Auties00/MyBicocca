package it.attendance100.mybicocca

import it.attendance100.mybicocca.data.local.entity.CourseEvent
import it.attendance100.mybicocca.data.local.entity.CourseSchedule
import it.attendance100.mybicocca.data.local.entity.EventType
import it.attendance100.mybicocca.domain.datasource.CalendarDataSource
import it.attendance100.mybicocca.util.CalendarUtils
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock calendar source
 * Provides test data for development
 */
@Singleton
class MockCalendarDataSource @Inject constructor() : CalendarDataSource {

    override suspend fun getEventsForMonth(month: YearMonth): List<CourseEvent> {
        // Simulates network latency
        delay(300)

        val startDate = month.atDay(1)
        val endDate = month.atEndOfMonth()

        return sampleEvents.filter { event ->
            val eventDate = event.startTime.toLocalDate()
            !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate)
        }
    }

    override suspend fun getEventsForDate(date: LocalDate): List<CourseEvent> {
        // Simulates network latency
        delay(300)

        return sampleEvents.filter { event ->
            event.startTime.toLocalDate() == date
        }
    }

    override suspend fun syncEvents(): Boolean {
        // Simulates server synchronization
        delay(1000)

        return true
    }

    companion object {
        private val thisMonday: LocalDate by lazy { LocalDate.now().with(DayOfWeek.MONDAY) }
        private val nextMonday: LocalDate by lazy { thisMonday.plusWeeks(1) }

        private fun event(
            id: Long,
            name: String,
            code: String? = null,
            prof: String? = null,
            room: String,
            building: String? = null,
            day: LocalDate,
            start: LocalTime,
            end: LocalTime,
            type: EventType = EventType.LECTURE
        ) = CourseEvent(
            id,
            name,
            code,
            prof,
            room,
            building,
            LocalDateTime.of(day, start),
            LocalDateTime.of(day, end),
            type,
            CalendarUtils.BICOCCA_BRAND_COLOR
        )

        private val sampleEvents: List<CourseEvent> by lazy {
            listOf(
                // LUNEDÌ QUESTA SETTIMANA
                event(
                    1000,
                    "Programmazione ad Oggetti",
                    "E3101Q094",
                    "Prof. Mario Rossi",
                    "U7-08",
                    "Edificio U7",
                    thisMonday,
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0)
                ),
                event(
                    1001,
                    "Basi di Dati",
                    "E3101Q095",
                    "Prof.ssa Laura Bianchi",
                    "U14-T014",
                    "Edificio U14",
                    thisMonday,
                    LocalTime.of(14, 0),
                    LocalTime.of(16, 0),
                    EventType.LAB
                ),
                event(
                    1002,
                    "Laboratorio di Programmazione",
                    "E3101Q110",
                    "Prof. Andrea Colombo",
                    "U14-T015",
                    "Edificio U14",
                    thisMonday,
                    LocalTime.of(14, 0),
                    LocalTime.of(15, 30),
                    EventType.LAB
                ),
                // MARTEDÌ - 3 eventi sovrapposti
                event(
                    1003,
                    "Algoritmi e Strutture Dati",
                    "E3101Q096",
                    "Prof. Giuseppe Verdi",
                    "U6-18",
                    "Edificio U6",
                    thisMonday.plusDays(1),
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0)
                ),
                event(
                    1004,
                    "Fisica",
                    "E3101Q101",
                    "Prof.ssa Elena Russo",
                    "U9-15",
                    "Edificio U9",
                    thisMonday.plusDays(1),
                    LocalTime.of(10, 30),
                    LocalTime.of(12, 30)
                ),
                event(
                    1005,
                    "Matematica Discreta",
                    "E3101Q111",
                    "Prof. Roberto Neri",
                    "U5-2070",
                    "Edificio U5",
                    thisMonday.plusDays(1),
                    LocalTime.of(11, 0),
                    LocalTime.of(13, 0)
                ),
                event(
                    1006,
                    "Progetto Software - Workshop",
                    "E3101Q112",
                    "Prof. Marco Verdi",
                    "U14-T016",
                    "Edificio U14",
                    thisMonday.plusDays(1),
                    LocalTime.of(14, 0),
                    LocalTime.of(18, 0),
                    EventType.LAB
                ),
                // MERCOLEDÌ - 4 eventi sovrapposti
                event(
                    1007,
                    "Ingegneria del Software",
                    "E3101Q097",
                    "Prof. Anna Neri",
                    "U5-2070",
                    "Edificio U5",
                    thisMonday.plusDays(2),
                    LocalTime.of(15, 0),
                    LocalTime.of(17, 0)
                ),
                event(
                    1008,
                    "Sistemi Operativi",
                    "E3101Q098",
                    "Prof. Luigi Bianchi",
                    "U6-20",
                    "Edificio U6",
                    thisMonday.plusDays(2),
                    LocalTime.of(15, 0),
                    LocalTime.of(16, 30)
                ),
                event(
                    1009,
                    "Reti di Calcolatori",
                    "E3101Q100",
                    "Prof. Marco Verdi",
                    "U7-08",
                    "Edificio U7",
                    thisMonday.plusDays(2),
                    LocalTime.of(15, 30),
                    LocalTime.of(17, 30)
                ),
                event(
                    1010,
                    "Sicurezza Informatica",
                    "E3101Q113",
                    "Prof.ssa Carla Ferrari",
                    "U9-15",
                    "Edificio U9",
                    thisMonday.plusDays(2),
                    LocalTime.of(16, 0),
                    LocalTime.of(18, 0)
                ),
                // GIOVEDÌ
                event(
                    1011,
                    "Analisi Matematica II",
                    "E3101Q114",
                    "Prof. Giovanni Bianchi",
                    "U5-2070",
                    "Edificio U5",
                    thisMonday.plusDays(3),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0)
                ),
                event(
                    1012,
                    "Ricevimento Studenti",
                    null,
                    "Prof. Mario Rossi",
                    "U7-3050",
                    "Edificio U7",
                    thisMonday.plusDays(3),
                    LocalTime.of(12, 0),
                    LocalTime.of(14, 0),
                    EventType.OTHER
                ),
                event(
                    1013,
                    "Seminario AI",
                    null,
                    "Prof.ssa Elena Russo",
                    "U9-Aula Magna",
                    "Edificio U9",
                    thisMonday.plusDays(3),
                    LocalTime.of(12, 30),
                    LocalTime.of(14, 30),
                    EventType.OTHER
                ),
                event(
                    1014,
                    "Programmazione Web",
                    "E3101Q102",
                    "Prof. Andrea Colombo",
                    "U14-T016",
                    "Edificio U14",
                    thisMonday.plusDays(3),
                    LocalTime.of(16, 0),
                    LocalTime.of(18, 0),
                    EventType.LAB
                ),
                // VENERDÌ - 5 eventi sovrapposti (esami)
                event(
                    1015,
                    "Esame Basi di Dati",
                    "E3101Q095",
                    "Prof.ssa Laura Bianchi",
                    "U5-2070",
                    "Edificio U5",
                    thisMonday.plusDays(4),
                    LocalTime.of(9, 0),
                    LocalTime.of(12, 0),
                    EventType.EXAM
                ),
                event(
                    1016,
                    "Esame Fisica",
                    "E3101Q101",
                    "Prof.ssa Elena Russo",
                    "U9-15",
                    "Edificio U9",
                    thisMonday.plusDays(4),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0),
                    EventType.EXAM
                ),
                event(
                    1017,
                    "Esame Programmazione",
                    "E3101Q094",
                    "Prof. Mario Rossi",
                    "U7-08",
                    "Edificio U7",
                    thisMonday.plusDays(4),
                    LocalTime.of(9, 30),
                    LocalTime.of(11, 30),
                    EventType.EXAM
                ),
                event(
                    1018,
                    "Test Algoritmi",
                    "E3101Q096",
                    "Prof. Giuseppe Verdi",
                    "U6-18",
                    "Edificio U6",
                    thisMonday.plusDays(4),
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0),
                    EventType.EXAM
                ),
                event(
                    1019,
                    "Verifica Reti",
                    "E3101Q100",
                    "Prof. Marco Verdi",
                    "U6-20",
                    "Edificio U6",
                    thisMonday.plusDays(4),
                    LocalTime.of(10, 30),
                    LocalTime.of(12, 30),
                    EventType.EXAM
                ),
                // SABATO
                event(
                    1020,
                    "Ricevimento Studenti",
                    null,
                    "Prof. Mario Rossi",
                    "U7-3050",
                    "Edificio U7",
                    thisMonday.plusDays(5),
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0),
                    EventType.OTHER
                ),
                event(
                    1021,
                    "Sessione di Studio di Gruppo",
                    null,
                    null,
                    "Biblioteca",
                    "Edificio U6",
                    thisMonday.plusDays(5),
                    LocalTime.of(14, 0),
                    LocalTime.of(17, 0),
                    EventType.OTHER
                ),
                // DOMENICA
                event(
                    1022,
                    "Preparazione Esame Fisica",
                    null,
                    null,
                    "Studio personale",
                    null,
                    thisMonday.plusDays(6),
                    LocalTime.of(15, 0),
                    LocalTime.of(18, 0),
                    EventType.OTHER
                ),
                // LUNEDÌ PROSSIMA SETTIMANA - mattina
                event(
                    1023,
                    "Architettura degli Elaboratori",
                    "E3101Q115",
                    "Prof. Francesco Russo",
                    "U5-2070",
                    "Edificio U5",
                    nextMonday,
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0)
                ),
                event(
                    1024,
                    "Calcolo delle Probabilità",
                    "E3101Q116",
                    "Prof.ssa Sofia Verdi",
                    "U9-15",
                    "Edificio U9",
                    nextMonday,
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 30),
                    EventType.LAB
                ),
                event(
                    1025,
                    "Compilatori",
                    "E3101Q117",
                    "Prof. Luca Neri",
                    "U7-08",
                    "Edificio U7",
                    nextMonday,
                    LocalTime.of(10, 30),
                    LocalTime.of(12, 30)
                ),
                // LUNEDÌ PROSSIMA SETTIMANA - pomeriggio (4 sovrapposti)
                event(
                    1026,
                    "Sistemi Operativi Avanzati",
                    "TEST001",
                    "Prof. Test 1",
                    "U14-T01",
                    "Edificio U14",
                    nextMonday,
                    LocalTime.of(14, 0),
                    LocalTime.of(16, 0)
                ),
                event(
                    1027,
                    "Sicurezza Informatica",
                    "TEST002",
                    "Prof. Test 2",
                    "U14-T02",
                    "Edificio U14",
                    nextMonday,
                    LocalTime.of(14, 0),
                    LocalTime.of(16, 0),
                    EventType.LAB
                ),
                event(
                    1028,
                    "Intelligenza Artificiale",
                    "TEST003",
                    "Prof. Test 3",
                    "U14-T03",
                    "Edificio U14",
                    nextMonday,
                    LocalTime.of(14, 30),
                    LocalTime.of(16, 30)
                ),
                event(
                    1029,
                    "Machine Learning",
                    "TEST004",
                    "Prof. Test 4",
                    "U14-T04",
                    "Edificio U14",
                    nextMonday,
                    LocalTime.of(15, 0),
                    LocalTime.of(17, 0),
                    EventType.LAB
                ),
                // MARTEDÌ PROSSIMA SETTIMANA
                event(
                    1030,
                    "Intelligenza Artificiale",
                    "E3101Q118",
                    "Prof.ssa Elena Russo",
                    "U14-T014",
                    "Edificio U14",
                    nextMonday.plusDays(1),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0)
                ),
                event(
                    1031,
                    "Machine Learning Lab",
                    "E3101Q119",
                    "Prof. Roberto Bianchi",
                    "U14-T015",
                    "Edificio U14",
                    nextMonday.plusDays(1),
                    LocalTime.of(14, 0),
                    LocalTime.of(17, 0),
                    EventType.LAB
                ),
                // MERCOLEDÌ PROSSIMA SETTIMANA
                event(
                    1032,
                    "Cloud Computing",
                    "E3101Q120",
                    "Prof. Marco Verdi",
                    "U6-20",
                    "Edificio U6",
                    nextMonday.plusDays(2),
                    LocalTime.of(15, 0),
                    LocalTime.of(17, 0)
                ),
                event(
                    1033,
                    "DevOps Workshop",
                    "E3101Q121",
                    "Prof. Andrea Colombo",
                    "U14-T016",
                    "Edificio U14",
                    nextMonday.plusDays(2),
                    LocalTime.of(15, 0),
                    LocalTime.of(18, 0),
                    EventType.LAB
                ),
                // GIOVEDÌ E VENERDÌ PROSSIMA SETTIMANA
                event(
                    1034,
                    "Grafica Computazionale",
                    "E3101Q122",
                    "Prof.ssa Carla Ferrari",
                    "U9-15",
                    "Edificio U9",
                    nextMonday.plusDays(3),
                    LocalTime.of(11, 0),
                    LocalTime.of(13, 0)
                ),
                event(
                    1035,
                    "Presentazione Progetti",
                    null,
                    "Vari Docenti",
                    "U9-Aula Magna",
                    "Edificio U9",
                    nextMonday.plusDays(4),
                    LocalTime.of(14, 0),
                    LocalTime.of(18, 0),
                    EventType.OTHER
                ),
            )
        }

        private val sampleSchedules: List<CourseSchedule> = run {
            val month = YearMonth.now()
            val validFrom = month.atDay(1)
            val validTo = month.plusMonths(3).atEndOfMonth()

            listOf(
                CourseSchedule(
                    1,
                    "Programmazione ad Oggetti",
                    "E3101Q094",
                    "Prof. Mario Rossi",
                    "U7-08",
                    "Edificio U7",
                    DayOfWeek.MONDAY,
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0),
                    EventType.LECTURE,
                    validFrom,
                    validTo,
                    CalendarUtils.BICOCCA_BRAND_COLOR
                ),
                CourseSchedule(
                    2,
                    "Basi di Dati",
                    "E3101Q095",
                    "Prof.ssa Laura Bianchi",
                    "U14-T014",
                    "Edificio U14",
                    DayOfWeek.TUESDAY,
                    LocalTime.of(14, 0),
                    LocalTime.of(16, 0),
                    EventType.LAB,
                    validFrom,
                    validTo,
                    CalendarUtils.BICOCCA_BRAND_COLOR
                ),
                CourseSchedule(
                    3,
                    "Algoritmi e Strutture Dati",
                    "E3101Q096",
                    "Prof. Giuseppe Verdi",
                    "U6-18",
                    "Edificio U6",
                    DayOfWeek.WEDNESDAY,
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0),
                    EventType.LECTURE,
                    validFrom,
                    validTo,
                    CalendarUtils.BICOCCA_BRAND_COLOR
                ),
            )
        }
    }
}