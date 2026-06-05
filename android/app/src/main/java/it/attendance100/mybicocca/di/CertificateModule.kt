package it.attendance100.mybicocca.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.repository.CertificateRepositoryImpl
import it.attendance100.mybicocca.domain.repository.CertificateRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CertificateModule {

    @Binds
    @Singleton
    abstract fun bindCertificateRepository(impl: CertificateRepositoryImpl): CertificateRepository
}
