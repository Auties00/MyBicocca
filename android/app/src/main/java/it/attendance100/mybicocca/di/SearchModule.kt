package it.attendance100.mybicocca.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.ai.MlKitSearchAssistant
import it.attendance100.mybicocca.data.speech.SpeechToTextSelector
import it.attendance100.mybicocca.domain.repository.SearchAssistant
import it.attendance100.mybicocca.domain.repository.SpeechToText
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchModule {

    // Single self-gating impl: it reports Unavailable on devices without Gemini Nano.
    @Binds
    @Singleton
    abstract fun bindSearchAssistant(impl: MlKitSearchAssistant): SearchAssistant

    @Binds
    @Singleton
    abstract fun bindSpeechToText(impl: SpeechToTextSelector): SpeechToText
}
