package it.attendance100.mybicocca.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.repository.DegreeAwardRepositoryImpl
import it.attendance100.mybicocca.domain.repository.DegreeAwardRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DegreeAwardModule {

    @Binds
    @Singleton
    abstract fun bindDegreeAwardRepository(impl: DegreeAwardRepositoryImpl): DegreeAwardRepository
}
