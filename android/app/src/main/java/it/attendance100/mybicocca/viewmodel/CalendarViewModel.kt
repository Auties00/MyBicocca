package it.attendance100.mybicocca.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.domain.usecase.*
import kotlinx.coroutines.*
import java.time.*
import javax.inject.*


/**
 * ViewModel per il calendario.
 * Gestisce lo stato e la logica di business del calendario.
 * Usa Hilt per Dependency Injection e Use Cases per business logic.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
  private val getEventsForDateUseCase: GetEventsForDateUseCase,
  private val getEventsForMonthUseCase: GetEventsForMonthUseCase,
  private val syncCalendarUseCase: SyncCalendarUseCase,
  private val insertEventUseCase: InsertEventUseCase,
  private val updateEventUseCase: UpdateEventUseCase,
  private val deleteEventUseCase: DeleteEventUseCase,
) : ViewModel() {

  // Stato del calendario
  private val _selectedDate = MutableLiveData<LocalDate>()
  val selectedDate: LiveData<LocalDate> = _selectedDate

  private val _currentMonth = MutableLiveData<YearMonth>()
  val currentMonth: LiveData<YearMonth> = _currentMonth

  private val _isLoading = MutableLiveData<Boolean>()
  val isLoading: LiveData<Boolean> = _isLoading

  private val _errorMessage = MutableLiveData<String?>()
  val errorMessage: LiveData<String?> = _errorMessage

  init {
    // Inizializza con la data corrente
    _selectedDate.value = LocalDate.now()
    _currentMonth.value = YearMonth.now()

    // Carica i dati iniziali
    loadInitialData()
  }

  /**
   * Carica i dati iniziali (funziona sia con Mock che con API reale)
   */
  private fun loadInitialData() = viewModelScope.launch {
    try {
      _isLoading.value = true
      // Sincronizza dal server (o carica dati mock)
      syncCalendarUseCase()
      _errorMessage.value = null
    } catch (e: Exception) {
      _errorMessage.value = e.message
    } finally {
      _isLoading.value = false
    }
  }

  /**
   * Ottiene gli eventi per la data selezionata
   */
  val eventsForSelectedDate: LiveData<List<CourseEvent>> = _selectedDate.switchMap { date ->
    getEventsForDateUseCase(date)
  }

  /**
   * Ottiene gli eventi per il mese corrente
   */
  val eventsForCurrentMonth: LiveData<List<CourseEvent>> = _currentMonth.switchMap { month ->
    getEventsForMonthUseCase(month)
  }

  /**
   * Seleziona una nuova data e aggiorna il mese corrente se necessario
   */
  fun selectDate(date: LocalDate) {
    _selectedDate.value = date
    // Sincronizza currentMonth solo se la data è in un mese diverso
    val dateMonth = YearMonth.from(date)
    if (_currentMonth.value != dateMonth) {
      _currentMonth.value = dateMonth
    }
  }

  /**
   * Cambia il mese visualizzato
   */
  fun setCurrentMonth(month: YearMonth) {
    _currentMonth.value = month
  }

  /**
   * Va al mese precedente e aggiorna la data selezionata
   */
  fun previousMonth() {
    val current = _currentMonth.value ?: YearMonth.now()
    val newMonth = current.minusMonths(1)
    _currentMonth.value = newMonth

    // Mantieni lo stesso giorno del mese, o l'ultimo se non esiste
    val currentDay = _selectedDate.value?.dayOfMonth ?: 1
    val newDay = minOf(currentDay, newMonth.lengthOfMonth())
    _selectedDate.value = newMonth.atDay(newDay)
  }

  /**
   * Va al mese successivo e aggiorna la data selezionata
   */
  fun nextMonth() {
    val current = _currentMonth.value ?: YearMonth.now()
    val newMonth = current.plusMonths(1)
    _currentMonth.value = newMonth

    // Mantieni lo stesso giorno del mese, o l'ultimo se non esiste
    val currentDay = _selectedDate.value?.dayOfMonth ?: 1
    val newDay = minOf(currentDay, newMonth.lengthOfMonth())
    _selectedDate.value = newMonth.atDay(newDay)
  }

  /**
   * Va alla settimana precedente
   */
  fun previousWeek() {
    val current = _selectedDate.value ?: return
    val newDate = current.minusWeeks(1)
    _selectedDate.value = newDate
    // Aggiorna currentMonth solo se la data è in un mese diverso
    val newMonth = YearMonth.from(newDate)
    if (_currentMonth.value != newMonth) {
      _currentMonth.value = newMonth
    }
  }

  /**
   * Va alla settimana successiva
   */
  fun nextWeek() {
    val current = _selectedDate.value ?: return
    val newDate = current.plusWeeks(1)
    _selectedDate.value = newDate
    // Aggiorna currentMonth solo se la data è in un mese diverso
    val newMonth = YearMonth.from(newDate)
    if (_currentMonth.value != newMonth) {
      _currentMonth.value = newMonth
    }
  }

  /**
   * Torna al mese corrente
   */
  fun goToToday() {
    _currentMonth.value = YearMonth.now()
    _selectedDate.value = LocalDate.now()
  }

  /**
   * Inserisce un nuovo evento
   */
  fun insertEvent(event: CourseEvent) = viewModelScope.launch {
    try {
      _isLoading.value = true
      insertEventUseCase(event)
      _errorMessage.value = null
    } catch (e: Exception) {
      _errorMessage.value = e.message
    } finally {
      _isLoading.value = false
    }
  }

  /**
   * Aggiorna un evento esistente
   */
  fun updateEvent(event: CourseEvent) = viewModelScope.launch {
    try {
      _isLoading.value = true
      updateEventUseCase(event)
      _errorMessage.value = null
    } catch (e: Exception) {
      _errorMessage.value = e.message
    } finally {
      _isLoading.value = false
    }
  }

  /**
   * Elimina un evento
   */
  fun deleteEvent(event: CourseEvent) = viewModelScope.launch {
    try {
      _isLoading.value = true
      deleteEventUseCase(event)
      _errorMessage.value = null
    } catch (e: Exception) {
      _errorMessage.value = e.message
    } finally {
      _isLoading.value = false
    }
  }

  /**
   * Sincronizza i dati dal server (funziona sia con Mock che con API reale)
   */
  fun syncFromRemote() = viewModelScope.launch {
    try {
      _isLoading.value = true
      syncCalendarUseCase()
      _errorMessage.value = null
    } catch (e: Exception) {
      _errorMessage.value = e.message
    } finally {
      _isLoading.value = false
    }
  }

  /**
   * Pulisce il messaggio di errore
   */
  fun clearError() {
    _errorMessage.value = null
  }
}
