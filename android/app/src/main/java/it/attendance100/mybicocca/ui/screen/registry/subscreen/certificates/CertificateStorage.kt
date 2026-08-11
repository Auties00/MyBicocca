package it.attendance100.mybicocca.ui.screen.registry.subscreen.certificates

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.domain.model.document.Certificate
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Storage abstraction for certificate files on disk. Handles checking cached status,
 * resolving cache file locations, and persisting downloaded PDF bytes without leaking
 * Android [Context] into the ViewModel layer.
 */
@Singleton
class CertificateStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun isDownloaded(certificate: Certificate): Boolean {
        return isCertificateDownloaded(context, certificate)
    }

    fun getFile(certificate: Certificate): File {
        return certificateFile(context, certificate)
    }

    suspend fun write(certificate: Certificate, bytes: ByteArray): File {
        return writeCertificate(context, certificate, bytes)
    }
}
