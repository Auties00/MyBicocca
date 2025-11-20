package it.attendance100.mybicocca.viewmodel

import android.app.*
import androidx.lifecycle.*
import it.attendance100.mybicocca.model.*
import it.attendance100.mybicocca.repository.*
import kotlinx.coroutines.*
import java.time.*

/**
 * ViewModel per il calendario
 * Gestisce lo stato e la logica di business del calendario
 */
class CalendarViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: CalendarRepository

  // LiveData per gli eventi e gli orari
  val allEvents: LiveData<List<CourseEvent>>
  val allSchedules: LiveData<List<CourseSchedule>>

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
    val database = AppDatabase.getDatabase(application)
    repository = CalendarRepository(
      database.courseEventDao(),
      database.courseScheduleDao()
    )
    allEvents = repository.allEvents
    allSchedules = repository.allSchedules

    // Inizializza con la data corrente
    _selectedDate.value = LocalDate.now()
    _currentMonth.value = YearMonth.now()
  }

  /**
   * Ottiene gli eventi per la data selezionata
   */
  val eventsForSelectedDate: LiveData<List<CourseEvent>> = Transformations.switchMap<LocalDate, List<CourseEvent>>(_selectedDate) { date ->
    repository.getEventsByDate(date)
  }

  /**
   * Ottiene gli eventi per il mese corrente
   */
  val eventsForCurrentMonth: LiveData<List<CourseEvent>> = Transformations.switchMap<YearMonth, List<CourseEvent>>(_currentMonth) { month ->
    val startDate = month.atDay(1)
    val endDate = month.atEndOfMonth()
    repository.getEventsBetweenDates(startDate, endDate)
  }

  /**
   * Seleziona una nuova data
   */
  fun selectDate(date: LocalDate) {
    _selectedDate.value = date
  }

  /**
   * Cambia il mese visualizzato
   */
  fun setCurrentMonth(month: YearMonth) {
    _currentMonth.value = month
  }

  /**
   * Va al mese precedente
   */
  fun previousMonth() {
    _currentMonth.value = _currentMonth.value?.minusMonths(1) ?: YearMonth.now().minusMonths(1)
  }

  /**
   * Va al mese successivo
   */
  fun nextMonth() {
    _currentMonth.value = _currentMonth.value?.plusMonths(1) ?: YearMonth.now().plusMonths(1)
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
      repository.insertEvent(event)
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
      repository.updateEvent(event)
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
      repository.deleteEvent(event)
      _errorMessage.value = null
    } catch (e: Exception) {
      _errorMessage.value = e.message
    } finally {
      _isLoading.value = false
    }
  }

  /**
   * Sincronizza i dati dal server
   */
  fun syncFromRemote() = viewModelScope.launch {
    try {
      _isLoading.value = true
      repository.syncFromRemote()
      _errorMessage.value = null
    } catch (e: Exception) {
      _errorMessage.value = e.message
    } finally {
      _isLoading.value = false
    }
  }

  /**
   * Genera eventi dal calendario ricorrente
   */
  fun generateEventsFromSchedules() = viewModelScope.launch {
    try {
      _isLoading.value = true
      val currentMonth = _currentMonth.value ?: YearMonth.now()
      val startDate = currentMonth.atDay(1)
      val endDate = currentMonth.atEndOfMonth()
      repository.generateEventsFromSchedules(startDate, endDate)
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
