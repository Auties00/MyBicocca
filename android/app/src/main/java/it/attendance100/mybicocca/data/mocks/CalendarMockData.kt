package it.attendance100.mybicocca.data.mocks

import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.utils.*
import java.time.*

/**
 * Mock data completi per il calendario con eventi realistici.
 * Include vari scenari: eventi singoli, 2 sovrapposti, 3+ sovrapposti, inizi simultanei e sfalsati.
 * Dati per questa settimana e la prossima.
 */
object CalendarMockData {

  /**
   * Restituisce una lista completa di eventi per testing
   */
  fun getSampleEvents(): List<CourseEvent> {
    // Usa settimana corrente e prossima per garantire eventi visibili
    val today = LocalDate.now()
    val currentMonth = YearMonth.from(today)
    val nextMonth = currentMonth.plusMonths(1)
    
    // Calcola lunedì di questa settimana e della prossima
    val thisMonday = today.with(java.time.DayOfWeek.MONDAY)
    val nextMonday = thisMonday.plusWeeks(1)
    
    val brandColor = CalendarUtils.BICOCCA_BRAND_COLOR

    return buildList {
      var eventId = CalendarUtils.TEST_EVENT_ID_START

      // ========================================================================
      // QUESTA SETTIMANA - Distribuiti da lunedì a venerdì
      // ========================================================================
      
      // Mattina: evento singolo
      add(CourseEvent(
        id = eventId++,
        courseName = "Programmazione ad Oggetti",
        courseCode = "E3101Q094",
        professor = "Prof. Mario Rossi",
        room = "U7-08",
        building = "Edificio U7",
        startTime = LocalDateTime.of(thisMonday, LocalTime.of(9, 0)),
        endTime = LocalDateTime.of(thisMonday, LocalTime.of(11, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))

      // Pomeriggio: 2 eventi sovrapposti (stesso orario inizio)
      add(CourseEvent(
        id = eventId++,
        courseName = "Basi di Dati",
        courseCode = "E3101Q095",
        professor = "Prof.ssa Laura Bianchi",
        room = "U14-T014",
        building = "Edificio U14",
        startTime = LocalDateTime.of(thisMonday, LocalTime.of(14, 0)),
        endTime = LocalDateTime.of(thisMonday, LocalTime.of(16, 0)),
        eventType = EventType.LAB,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Laboratorio di Programmazione",
        courseCode = "E3101Q110",
        professor = "Prof. Andrea Colombo",
        room = "U14-T015",
        building = "Edificio U14",
        startTime = LocalDateTime.of(thisMonday, LocalTime.of(14, 0)),
        endTime = LocalDateTime.of(thisMonday, LocalTime.of(15, 30)),
        eventType = EventType.LAB,
        color = brandColor
      ))

      // ========================================================================
      // MARTEDÌ - 3 eventi sovrapposti (inizio sfalsato)
      // ========================================================================
      
      add(CourseEvent(
        id = eventId++,
        courseName = "Algoritmi e Strutture Dati",
        courseCode = "E3101Q096",
        professor = "Prof. Giuseppe Verdi",
        room = "U6-18",
        building = "Edificio U6",
        startTime = LocalDateTime.of(thisMonday.plusDays(1), LocalTime.of(10, 0)),
        endTime = LocalDateTime.of(thisMonday.plusDays(1), LocalTime.of(12, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Fisica",
        courseCode = "E3101Q101",
        professor = "Prof.ssa Elena Russo",
        room = "U9-15",
        building = "Edificio U9",
        startTime = LocalDateTime.of(thisMonday.plusDays(1), LocalTime.of(10, 30)),
        endTime = LocalDateTime.of(thisMonday.plusDays(1), LocalTime.of(12, 30)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Matematica Discreta",
        courseCode = "E3101Q111",
        professor = "Prof. Roberto Neri",
        room = "U5-2070",
        building = "Edificio U5",
        startTime = LocalDateTime.of(thisMonday.plusDays(1), LocalTime.of(11, 0)),
        endTime = LocalDateTime.of(thisMonday.plusDays(1), LocalTime.of(13, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))

      // Pomeriggio: evento singolo lungo
      add(CourseEvent(
        id = eventId++,
        courseName = "Progetto Software - Workshop",
        courseCode = "E3101Q112",
        professor = "Prof. Marco Verdi",
        room = "U14-T016",
        building = "Edificio U14",
        startTime = LocalDateTime.of(thisMonday.plusDays(1), LocalTime.of(14, 0)),
        endTime = LocalDateTime.of(thisMonday.plusDays(1), LocalTime.of(18, 0)),
        eventType = EventType.LAB,
        color = brandColor
      ))

      // ========================================================================
      // MERCOLEDÌ - 4 eventi sovrapposti (badge +2)
      // ========================================================================
      
      // 4 eventi sovrapposti nel pomeriggio
      add(CourseEvent(
        id = eventId++,
        courseName = "Ingegneria del Software",
        courseCode = "E3101Q097",
        professor = "Prof. Anna Neri",
        room = "U5-2070",
        building = "Edificio U5",
        startTime = LocalDateTime.of(thisMonday.plusDays(2), LocalTime.of(15, 0)),
        endTime = LocalDateTime.of(thisMonday.plusDays(2), LocalTime.of(17, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Sistemi Operativi",
        courseCode = "E3101Q098",
        professor = "Prof. Luigi Bianchi",
        room = "U6-20",
        building = "Edificio U6",
        startTime = LocalDateTime.of(thisMonday.plusDays(2), LocalTime.of(15, 0)),
        endTime = LocalDateTime.of(thisMonday.plusDays(2), LocalTime.of(16, 30)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Reti di Calcolatori",
        courseCode = "E3101Q100",
        professor = "Prof. Marco Verdi",
        room = "U7-08",
        building = "Edificio U7",
        startTime = LocalDateTime.of(thisMonday.plusDays(2), LocalTime.of(15, 30)),
        endTime = LocalDateTime.of(thisMonday.plusDays(2), LocalTime.of(17, 30)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Sicurezza Informatica",
        courseCode = "E3101Q113",
        professor = "Prof.ssa Carla Ferrari",
        room = "U9-15",
        building = "Edificio U9",
        startTime = LocalDateTime.of(thisMonday.plusDays(2), LocalTime.of(16, 0)),
        endTime = LocalDateTime.of(thisMonday.plusDays(2), LocalTime.of(18, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))

      // ========================================================================
      // GIOVEDÌ - Eventi sparsi e 2 sovrapposti
      // ========================================================================
      
      // Mattina: evento singolo
      add(CourseEvent(
        id = eventId++,
        courseName = "Analisi Matematica II",
        courseCode = "E3101Q114",
        professor = "Prof. Giovanni Bianchi",
        room = "U5-2070",
        building = "Edificio U5",
        startTime = LocalDateTime.of(thisMonday.plusDays(3), LocalTime.of(9, 0)),
        endTime = LocalDateTime.of(thisMonday.plusDays(3), LocalTime.of(11, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))

      // Mezzogiorno: 2 eventi sovrapposti (inizio sfalsato)
      add(CourseEvent(
        id = eventId++,
        courseName = "Ricevimento Studenti",
        courseCode = null,
        professor = "Prof. Mario Rossi",
        room = "U7-3050",
        building = "Edificio U7",
        startTime = LocalDateTime.of(thisMonday.plusDays(3), LocalTime.of(12, 0)),
        endTime = LocalDateTime.of(thisMonday.plusDays(3), LocalTime.of(14, 0)),
        eventType = EventType.OTHER,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Seminario AI",
        courseCode = null,
        professor = "Prof.ssa Elena Russo",
        room = "U9-Aula Magna",
        building = "Edificio U9",
        startTime = LocalDateTime.of(thisMonday.plusDays(3), LocalTime.of(12, 30)),
        endTime = LocalDateTime.of(thisMonday.plusDays(3), LocalTime.of(14, 30)),
        eventType = EventType.OTHER,
        color = brandColor
      ))

      // Pomeriggio: evento singolo
      add(CourseEvent(
        id = eventId++,
        courseName = "Programmazione Web",
        courseCode = "E3101Q102",
        professor = "Prof. Andrea Colombo",
        room = "U14-T016",
        building = "Edificio U14",
        startTime = LocalDateTime.of(thisMonday.plusDays(3), LocalTime.of(16, 0)),
        endTime = LocalDateTime.of(thisMonday.plusDays(3), LocalTime.of(18, 0)),
        eventType = EventType.LAB,
        color = brandColor
      ))

      // ========================================================================
      // VENERDÌ - 5 eventi sovrapposti (badge +3) caso estremo
      // ========================================================================
      
      add(CourseEvent(
        id = eventId++,
        courseName = "Esame Basi di Dati",
        courseCode = "E3101Q095",
        professor = "Prof.ssa Laura Bianchi",
        room = "U5-2070",
        building = "Edificio U5",
        startTime = LocalDateTime.of(thisMonday.plusDays(4), LocalTime.of(9, 0)),
        endTime = LocalDateTime.of(thisMonday.plusDays(4), LocalTime.of(12, 0)),
        eventType = EventType.EXAM,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Esame Fisica",
        courseCode = "E3101Q101",
        professor = "Prof.ssa Elena Russo",
        room = "U9-15",
        building = "Edificio U9",
        startTime = LocalDateTime.of(thisMonday.plusDays(4), LocalTime.of(9, 0)),
        endTime = LocalDateTime.of(thisMonday.plusDays(4), LocalTime.of(11, 0)),
        eventType = EventType.EXAM,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Esame Programmazione",
        courseCode = "E3101Q094",
        professor = "Prof. Mario Rossi",
        room = "U7-08",
        building = "Edificio U7",
        startTime = LocalDateTime.of(thisMonday.plusDays(4), LocalTime.of(9, 30)),
        endTime = LocalDateTime.of(thisMonday.plusDays(4), LocalTime.of(11, 30)),
        eventType = EventType.EXAM,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Test Algoritmi",
        courseCode = "E3101Q096",
        professor = "Prof. Giuseppe Verdi",
        room = "U6-18",
        building = "Edificio U6",
        startTime = LocalDateTime.of(thisMonday.plusDays(4), LocalTime.of(10, 0)),
        endTime = LocalDateTime.of(thisMonday.plusDays(4), LocalTime.of(12, 0)),
        eventType = EventType.EXAM,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Verifica Reti",
        courseCode = "E3101Q100",
        professor = "Prof. Marco Verdi",
        room = "U6-20",
        building = "Edificio U6",
        startTime = LocalDateTime.of(thisMonday.plusDays(4), LocalTime.of(10, 30)),
        endTime = LocalDateTime.of(thisMonday.plusDays(4), LocalTime.of(12, 30)),
        eventType = EventType.EXAM,
        color = brandColor
      ))

      // ========================================================================
      // SABATO - Studio e ricevimenti
      // ========================================================================
      
      add(CourseEvent(
        id = eventId++,
        courseName = "Ricevimento Studenti",
        courseCode = null,
        professor = "Prof. Mario Rossi",
        room = "U7-3050",
        building = "Edificio U7",
        startTime = LocalDateTime.of(thisMonday.plusDays(5), LocalTime.of(10, 0)),
        endTime = LocalDateTime.of(thisMonday.plusDays(5), LocalTime.of(12, 0)),
        eventType = EventType.OTHER,
        color = brandColor
      ))
      
      add(CourseEvent(
        id = eventId++,
        courseName = "Sessione di Studio di Gruppo",
        courseCode = null,
        professor = null,
        room = "Biblioteca",
        building = "Edificio U6",
        startTime = LocalDateTime.of(thisMonday.plusDays(5), LocalTime.of(14, 0)),
        endTime = LocalDateTime.of(thisMonday.plusDays(5), LocalTime.of(17, 0)),
        eventType = EventType.OTHER,
        color = brandColor
      ))

      // ========================================================================
      // DOMENICA - Preparazione esami
      // ========================================================================
      
      add(CourseEvent(
        id = eventId++,
        courseName = "Preparazione Esame Fisica",
        courseCode = null,
        professor = null,
        room = "Studio personale",
        building = null,
        startTime = LocalDateTime.of(thisMonday.plusDays(6), LocalTime.of(15, 0)),
        endTime = LocalDateTime.of(thisMonday.plusDays(6), LocalTime.of(18, 0)),
        eventType = EventType.OTHER,
        color = brandColor
      ))

      // ========================================================================
      // SETTIMANA PROSSIMA - LUNEDÌ
      // ========================================================================
      
      // 3 eventi sovrapposti al mattino (inizio sfalsato)
      add(CourseEvent(
        id = eventId++,
        courseName = "Architettura degli Elaboratori",
        courseCode = "E3101Q115",
        professor = "Prof. Francesco Russo",
        room = "U5-2070",
        building = "Edificio U5",
        startTime = LocalDateTime.of(nextMonday, LocalTime.of(10, 0)),
        endTime = LocalDateTime.of(nextMonday, LocalTime.of(12, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Calcolo delle Probabilità",
        courseCode = "E3101Q116",
        professor = "Prof.ssa Sofia Verdi",
        room = "U9-15",
        building = "Edificio U9",
        startTime = LocalDateTime.of(nextMonday, LocalTime.of(10, 0)),
        endTime = LocalDateTime.of(nextMonday, LocalTime.of(11, 30)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Compilatori",
        courseCode = "E3101Q117",
        professor = "Prof. Luca Neri",
        room = "U7-08",
        building = "Edificio U7",
        startTime = LocalDateTime.of(nextMonday, LocalTime.of(10, 30)),
        endTime = LocalDateTime.of(nextMonday, LocalTime.of(12, 30)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))

      // ========================================================================
      // SETTIMANA PROSSIMA - LUNEDÌ POMERIGGIO (TEST OVERLAP > 2)
      // ========================================================================
      
      // 4 eventi sovrapposti per testare la UI
      add(CourseEvent(
        id = eventId++,
        courseName = "Sistemi Operativi Avanzati",
        courseCode = "TEST001",
        professor = "Prof. Test 1",
        room = "U14-T01",
        building = "Edificio U14",
        startTime = LocalDateTime.of(nextMonday, LocalTime.of(14, 0)),
        endTime = LocalDateTime.of(nextMonday, LocalTime.of(16, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Sicurezza Informatica",
        courseCode = "TEST002",
        professor = "Prof. Test 2",
        room = "U14-T02",
        building = "Edificio U14",
        startTime = LocalDateTime.of(nextMonday, LocalTime.of(14, 0)),
        endTime = LocalDateTime.of(nextMonday, LocalTime.of(16, 0)),
        eventType = EventType.LAB,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Intelligenza Artificiale",
        courseCode = "TEST003",
        professor = "Prof. Test 3",
        room = "U14-T03",
        building = "Edificio U14",
        startTime = LocalDateTime.of(nextMonday, LocalTime.of(14, 30)),
        endTime = LocalDateTime.of(nextMonday, LocalTime.of(16, 30)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "Machine Learning",
        courseCode = "TEST004",
        professor = "Prof. Test 4",
        room = "U14-T04",
        building = "Edificio U14",
        startTime = LocalDateTime.of(nextMonday, LocalTime.of(15, 0)),
        endTime = LocalDateTime.of(nextMonday, LocalTime.of(17, 0)),
        eventType = EventType.LAB,
        color = brandColor
      ))

      // ========================================================================
      // SETTIMANA PROSSIMA - MARTEDÌ
      // ========================================================================
      
      // Eventi singoli sparsi
      add(CourseEvent(
        id = eventId++,
        courseName = "Intelligenza Artificiale",
        courseCode = "E3101Q118",
        professor = "Prof.ssa Elena Russo",
        room = "U14-T014",
        building = "Edificio U14",
        startTime = LocalDateTime.of(nextMonday.plusDays(1), LocalTime.of(9, 0)),
        endTime = LocalDateTime.of(nextMonday.plusDays(1), LocalTime.of(11, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))
      
      add(CourseEvent(
        id = eventId++,
        courseName = "Machine Learning Lab",
        courseCode = "E3101Q119",
        professor = "Prof. Roberto Bianchi",
        room = "U14-T015",
        building = "Edificio U14",
        startTime = LocalDateTime.of(nextMonday.plusDays(1), LocalTime.of(14, 0)),
        endTime = LocalDateTime.of(nextMonday.plusDays(1), LocalTime.of(17, 0)),
        eventType = EventType.LAB,
        color = brandColor
      ))

      // ========================================================================
      // SETTIMANA PROSSIMA - MERCOLEDÌ
      // ========================================================================
      
      // 2 eventi sovrapposti (stesso orario)
      add(CourseEvent(
        id = eventId++,
        courseName = "Cloud Computing",
        courseCode = "E3101Q120",
        professor = "Prof. Marco Verdi",
        room = "U6-20",
        building = "Edificio U6",
        startTime = LocalDateTime.of(nextMonday.plusDays(2), LocalTime.of(15, 0)),
        endTime = LocalDateTime.of(nextMonday.plusDays(2), LocalTime.of(17, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))
      add(CourseEvent(
        id = eventId++,
        courseName = "DevOps Workshop",
        courseCode = "E3101Q121",
        professor = "Prof. Andrea Colombo",
        room = "U14-T016",
        building = "Edificio U14",
        startTime = LocalDateTime.of(nextMonday.plusDays(2), LocalTime.of(15, 0)),
        endTime = LocalDateTime.of(nextMonday.plusDays(2), LocalTime.of(18, 0)),
        eventType = EventType.LAB,
        color = brandColor
      ))

      // ========================================================================
      // SETTIMANA PROSSIMA - GIOVEDÌ E VENERDÌ
      // ========================================================================
      
      add(CourseEvent(
        id = eventId++,
        courseName = "Grafica Computazionale",
        courseCode = "E3101Q122",
        professor = "Prof.ssa Carla Ferrari",
        room = "U9-15",
        building = "Edificio U9",
        startTime = LocalDateTime.of(nextMonday.plusDays(3), LocalTime.of(11, 0)),
        endTime = LocalDateTime.of(nextMonday.plusDays(3), LocalTime.of(13, 0)),
        eventType = EventType.LECTURE,
        color = brandColor
      ))
      
      add(CourseEvent(
        id = eventId++,
        courseName = "Presentazione Progetti",
        courseCode = null,
        professor = "Vari Docenti",
        room = "U9-Aula Magna",
        building = "Edificio U9",
        startTime = LocalDateTime.of(nextMonday.plusDays(4), LocalTime.of(14, 0)),
        endTime = LocalDateTime.of(nextMonday.plusDays(4), LocalTime.of(18, 0)),
        eventType = EventType.OTHER,
        color = brandColor,
        isCancelled = false
      ))
    }
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
