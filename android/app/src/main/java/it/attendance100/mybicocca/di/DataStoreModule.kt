package it.attendance100.mybicocca.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mybicocca_settings",
)

private val Context.libraryCrowdingHistoryDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "library_crowding_history",
)

/**
 * Provides the app's Preferences DataStores: the shared `mybicocca_settings` store and the
 * dedicated `library_crowding_history` store. File-level property delegates enforce DataStore's
 * one-instance-per-file rule at runtime.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    const val LIBRARY_CROWDING_HISTORY_DATA_STORE = "library_crowding_history"

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.settingsDataStore

    @Provides
    @Singleton
    @Named(LIBRARY_CROWDING_HISTORY_DATA_STORE)
    fun provideLibraryCrowdingHistoryDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.libraryCrowdingHistoryDataStore
}
