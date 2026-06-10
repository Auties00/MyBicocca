package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.local.settings.FileOpenPreferenceStore
import it.attendance100.mybicocca.domain.model.settings.FileOpenChoice
import it.attendance100.mybicocca.domain.repository.FileOpenPreferenceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Delegates to [FileOpenPreferenceStore], the DataStore wrapper owning the persisted keys. */
@Singleton
class FileOpenPreferenceRepositoryImpl @Inject constructor(
    private val store: FileOpenPreferenceStore,
) : FileOpenPreferenceRepository {

    override fun observeChoices(): Flow<Map<String, FileOpenChoice>> = store.choices

    override suspend fun setChoice(kind: String, choice: FileOpenChoice) = store.setChoice(kind, choice)

    override suspend fun clearChoice(kind: String) = store.clearChoice(kind)
}
