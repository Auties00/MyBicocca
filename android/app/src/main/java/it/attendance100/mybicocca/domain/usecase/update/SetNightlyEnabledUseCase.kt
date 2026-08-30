package it.attendance100.mybicocca.domain.usecase.update

import it.attendance100.mybicocca.domain.repository.UpdateRepository
import javax.inject.Inject

class SetNightlyEnabledUseCase @Inject constructor(
    private val repository: UpdateRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setNightlyEnabled(enabled)
}
