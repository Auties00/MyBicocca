package it.attendance100.mybicocca.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.speech.SpeechToTextSelector
import it.attendance100.mybicocca.domain.repository.SpeechToText
import javax.inject.Singleton

/**
 * Binds the speech-to-text contract behind search dictation to the selector that picks the
 * on-device ML Kit recognizer when its model is available and the platform recognizer otherwise.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SearchModule {

    @Binds
    @Singleton
    abstract fun bindSpeechToText(impl: SpeechToTextSelector): SpeechToText
}
