package it.attendance100.mybicocca.di

import dagger.*
import dagger.hilt.*
import dagger.hilt.components.*

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {
  // Bindings moved to RepositoryModule
}
