package it.attendance100.mybicocca

import it.attendance100.mybicocca.domain.datasource.UserDataSource
import it.attendance100.mybicocca.domain.model.CareerStats
import it.attendance100.mybicocca.domain.model.Exam
import it.attendance100.mybicocca.domain.model.GradePoint
import it.attendance100.mybicocca.domain.model.User
import javax.inject.Inject

/**
 * Mock user source
 * Provides static test data for user profile and career stats
 */
class MockUserDataSource @Inject constructor() : UserDataSource {
    override suspend fun getUser(): User = user
    override suspend fun getCareerStats(): CareerStats = careerStats

    companion object {
        private val user = User(
            name = "Lorenzo Angelo",
            surname = "Lupi",
            matricola = "909369",
            course = "Informatica",
            year = "3",
            email = "l.lupi3@campus.unimib.it"
        )

        private val careerStats = CareerStats(
            mediaAritmetica = 27.5f,
            mediaPonderata = 28.2f,
            esamiSostenuti = 18,
            esamiTotali = 24,
            cfuAcquisiti = 144,
            cfuTotali = 180,
            grades = listOf(
                GradePoint(28f, "2023-01-01", "Analisi 1", "12", false),
                GradePoint(30f, "2023-02-01", "Programmazione 1", "12", false),
                GradePoint(26f, "2023-06-01", "Architettura", "6", false),
                GradePoint(31f, "2023-07-01", "Basi di Dati", "9", true)
            ),
            passedExams = listOf(
                Exam("Analisi 1", "12", "28", "2023-01-01", "S", false),
                Exam("Programmazione 1", "12", "30", "2023-02-01", "S", false),
                Exam("Architettura", "6", "26", "2023-06-01", "S", false),
                Exam("Basi di Dati", "9", "30L", "2023-07-01", "S", true)
            ),
            remainingExams = listOf(
                Exam("Sistemi Operativi", "9", null, null, "F", false),
                Exam("Reti", "6", null, null, "F", false)
            )
        )
    }
}