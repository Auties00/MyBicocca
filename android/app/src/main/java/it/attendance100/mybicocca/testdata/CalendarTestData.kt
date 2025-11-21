package it.attendance100.mybicocca.testdata

import it.attendance100.mybicocca.data.entities.*
import java.time.*

/**
 * Utility per generare dati di test per il calendario.
 * Utile per sviluppo e testing.
 */
object CalendarTestData {

  /**
   * Restituisce una lista di eventi di esempio
   */
  fun getSampleEvents(): List<CourseEvent> {
    val today = LocalDate.now()
    val brandColor = CalendarUtils.BICOCCA_BRAND_COLOR

    return listOf(
      CourseEvent(
        id = CalendarUtils.TEST_EVENT_ID_START,
        courseName = "Programmazione ad Oggetti",
        courseCode = "E3101Q094",
        professor = "Prof. Mario Rossi",
        room = "U7-08",
        building = "Edificio U7",
        startTime = LocalDateTime.of(today, LocalTime.of(9, 0)),
        endTime = LocalDateTime.of(today, LocalTime.of(11, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ),
      CourseEvent(
        id = CalendarUtils.TEST_EVENT_ID_START + 1,
        courseName = "Basi di Dati",
        courseCode = "E3101Q095",
        professor = "Prof.ssa Laura Bianchi",
        room = "U14-T014",
        building = "Edificio U14",
        startTime = LocalDateTime.of(today, LocalTime.of(14, 0)),
        endTime = LocalDateTime.of(today, LocalTime.of(16, 0)),
        eventType = EventType.LAB,
        color = brandColor
      ),
      CourseEvent(
        id = CalendarUtils.TEST_EVENT_ID_START + 2,
        courseName = "Algoritmi e Strutture Dati",
        courseCode = "E3101Q096",
        professor = "Prof. Giuseppe Verdi",
        room = "U6-18",
        building = "Edificio U6",
        startTime = LocalDateTime.of(today.plusDays(1), LocalTime.of(10, 0)),
        endTime = LocalDateTime.of(today.plusDays(1), LocalTime.of(12, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ),
      CourseEvent(
        id = CalendarUtils.TEST_EVENT_ID_START + 3,
        courseName = "Ingegneria del Software",
        courseCode = "E3101Q097",
        professor = "Prof. Anna Neri",
        room = "U5-2070",
        building = "Edificio U5",
        startTime = LocalDateTime.of(today.plusDays(2), LocalTime.of(9, 0)),
        endTime = LocalDateTime.of(today.plusDays(2), LocalTime.of(11, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ),
      CourseEvent(
        id = CalendarUtils.TEST_EVENT_ID_START + 4,
        courseName = "Sistemi Operativi - Lab",
        courseCode = "E3101Q098",
        professor = "Prof. Luigi Bianchi",
        room = "U14-T015",
        building = "Edificio U14",
        startTime = LocalDateTime.of(today.plusDays(2), LocalTime.of(14, 0)),
        endTime = LocalDateTime.of(today.plusDays(2), LocalTime.of(17, 0)),
        eventType = EventType.LAB,
        color = brandColor
      ),
      CourseEvent(
        id = CalendarUtils.TEST_EVENT_ID_START + 5,
        courseName = "Esame Analisi Matematica",
        courseCode = "E3101Q099",
        professor = "Prof. Carlo Ferrari",
        room = "U5-2070",
        building = "Edificio U5",
        startTime = LocalDateTime.of(today.plusDays(7), LocalTime.of(9, 0)),
        endTime = LocalDateTime.of(today.plusDays(7), LocalTime.of(12, 0)),
        eventType = EventType.EXAM,
        color = brandColor
      ),
      CourseEvent(
        id = CalendarUtils.TEST_EVENT_ID_START + 6,
        courseName = "Ricevimento Studenti",
        courseCode = null,
        professor = "Prof. Mario Rossi",
        room = "U7-3050",
        building = "Edificio U7",
        startTime = LocalDateTime.of(today.plusDays(3), LocalTime.of(15, 0)),
        endTime = LocalDateTime.of(today.plusDays(3), LocalTime.of(17, 0)),
        eventType = EventType.OFFICE_HOURS,
        color = brandColor
      ),
      CourseEvent(
        id = CalendarUtils.TEST_EVENT_ID_START + 7,
        courseName = "Reti di Calcolatori",
        courseCode = "E3101Q100",
        professor = "Prof. Marco Verdi",
        room = "U6-20",
        building = "Edificio U6",
        startTime = LocalDateTime.of(today.minusDays(3), LocalTime.of(11, 0)),
        endTime = LocalDateTime.of(today.minusDays(3), LocalTime.of(13, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ),
      CourseEvent(
        id = CalendarUtils.TEST_EVENT_ID_START + 8,
        courseName = "Fisica",
        courseCode = "E3101Q101",
        professor = "Prof.ssa Elena Russo",
        room = "U9-15",
        building = "Edificio U9",
        startTime = LocalDateTime.of(today.minusDays(2), LocalTime.of(9, 0)),
        endTime = LocalDateTime.of(today.minusDays(2), LocalTime.of(11, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ),
      CourseEvent(
        id = CalendarUtils.TEST_EVENT_ID_START + 9,
        courseName = "Programmazione Web",
        courseCode = "E3101Q102",
        professor = "Prof. Andrea Colombo",
        room = "U14-T016",
        building = "Edificio U14",
        startTime = LocalDateTime.of(today.minusDays(1), LocalTime.of(14, 0)),
        endTime = LocalDateTime.of(today.minusDays(1), LocalTime.of(16, 0)),
        eventType = EventType.LAB,
        color = brandColor
      )
    )
  }

  /**
   * Restituisce una lista di orari ricorrenti di esempio
   */
  fun getSampleSchedules(): List<CourseSchedule> {
    val currentMonth = YearMonth.now()
    val brandColor = CalendarUtils.BICOCCA_BRAND_COLOR

    return listOf(
      CourseSchedule(
        courseName = "Programmazione ad Oggetti",
        courseCode = "E3101Q094",
        professor = "Prof. Mario Rossi",
        room = "U7-08",
        building = "Edificio U7",
        dayOfWeek = DayOfWeek.MONDAY,
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(11, 0),
        eventType = EventType.LECTURE,
        validFrom = currentMonth.atDay(1),
        validTo = currentMonth.plusMonths(3).atEndOfMonth(),
        color = brandColor
      ),
      CourseSchedule(
        courseName = "Basi di Dati",
        courseCode = "E3101Q095",
        professor = "Prof.ssa Laura Bianchi",
        room = "U14-T014",
        building = "Edificio U14",
        dayOfWeek = DayOfWeek.TUESDAY,
        startTime = LocalTime.of(14, 0),
        endTime = LocalTime.of(16, 0),
        eventType = EventType.LAB,
        validFrom = currentMonth.atDay(1),
        validTo = currentMonth.plusMonths(3).atEndOfMonth(),
        color = brandColor
      ),
      CourseSchedule(
        courseName = "Algoritmi e Strutture Dati",
        courseCode = "E3101Q096",
        professor = "Prof. Giuseppe Verdi",
        room = "U6-18",
        building = "Edificio U6",
        dayOfWeek = DayOfWeek.WEDNESDAY,
        startTime = LocalTime.of(10, 0),
        endTime = LocalTime.of(12, 0),
        eventType = EventType.LECTURE,
        validFrom = currentMonth.atDay(1),
        validTo = currentMonth.plusMonths(3).atEndOfMonth(),
        color = brandColor
      )
    )
  }
}
