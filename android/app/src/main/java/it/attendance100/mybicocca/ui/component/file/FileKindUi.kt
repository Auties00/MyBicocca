package it.attendance100.mybicocca.ui.component.file

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Per-kind icon shared by the in-app/external open chooser and the "Apertura file" settings
 * screen, so both depict a file type the same way.
 */
internal fun FileKind.openChooserIcon(): ImageVector = when (this) {
    FileKind.Pdf -> Icons.Outlined.PictureAsPdf
    FileKind.Image -> Icons.Outlined.Image
    FileKind.Video -> Icons.Outlined.Movie
    FileKind.Audio -> Icons.Outlined.MusicNote
    FileKind.Html -> Icons.AutoMirrored.Outlined.Article
    FileKind.Text -> Icons.Outlined.Description
    FileKind.Zip -> Icons.Outlined.FolderZip
    else -> Icons.AutoMirrored.Outlined.InsertDriveFile
}

/**
 * Per-kind human label paired with [openChooserIcon] in the chooser hero chip and the
 * file-associations settings rows.
 */
internal fun FileKind.openChooserLabel(): String = when (this) {
    FileKind.Pdf -> "file_kind_pdf"
    FileKind.Image -> "file_kind_image"
    FileKind.Video -> "file_kind_video"
    FileKind.Audio -> "file_kind_audio"
    FileKind.Html -> "file_kind_html"
    FileKind.Text -> "file_kind_text"
    FileKind.Zip -> "file_kind_zip"
    else -> "file_kind_generic"
}
