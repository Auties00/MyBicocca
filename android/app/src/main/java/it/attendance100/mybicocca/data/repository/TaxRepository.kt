package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.IseeDao
import it.attendance100.mybicocca.data.database.dao.TaxDao
import it.attendance100.mybicocca.data.datasource.tax.Esse3TaxDataSource
import it.attendance100.mybicocca.data.model.isee.IseeDeclaration
import it.attendance100.mybicocca.data.model.tax.Invoice
import it.attendance100.mybicocca.data.model.tax.TaxCharge
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaxRepository @Inject constructor(
    private val dataSource: Esse3TaxDataSource,
    private val taxDao: TaxDao,
    private val iseeDao: IseeDao,
) {
    // Charges & Invoices
    fun observeCharges(careerId: Long): Flow<List<TaxCharge>> = taxDao.observeCharges(careerId)

    fun observeInvoices(careerId: Long): Flow<List<Invoice>> = taxDao.observeInvoices(careerId)

    suspend fun refreshCharges(studentId: Long, careerId: Long): Result<Unit> = runCatching {
        val charges = dataSource.getStudentCharges(studentId)
        taxDao.upsertAllCharges(charges)
    }

    suspend fun refreshInvoices(personId: Long, careerId: Long): Result<Unit> = runCatching {
        val invoices = dataSource.getInvoices(personId, careerId)
        taxDao.upsertAllInvoices(invoices)
    }

    // ISEE
    fun observeLatestIsee(): Flow<IseeDeclaration?> = iseeDao.observeLatest()

    fun observeIseeByYear(academicYear: Long): Flow<IseeDeclaration?> = iseeDao.observeByYear(academicYear)

    suspend fun refreshIsee(academicYearId: Long): Result<Unit> = runCatching {
        val declarations = dataSource.getIseeDeclarations(academicYearId)
        iseeDao.upsertAll(declarations)
    }
}
