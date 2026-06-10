package it.attendance100.mybicocca.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.attendance100.mybicocca.data.local.credentials.CredentialsStore
import javax.inject.Named
import javax.inject.Singleton

/**
 * Provides the encrypted SharedPreferences file backing the credentials store: keys and values
 * encrypted with AES256 under a keystore-backed master key. The named binding keeps it from
 * ever being confused with a plain SharedPreferences injection.
 */
@Module
@InstallIn(SingletonComponent::class)
object CredentialsModule {

    @Provides
    @Singleton
    @Named(CredentialsStore.EncryptedPrefsName)
    fun provideEncryptedPrefs(@ApplicationContext context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private const val ENCRYPTED_FILE = "mybicocca_credentials"
}
