package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.subscreen.rilevaPresenza.component

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.NoPhotography
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.R

/**
 * Full-screen QR scanner shown above the "Rileva presenza" sheet: an edge-to-edge black
 * dialog hosting the live camera preview under an animated corner-bracket reticle with a
 * sweeping scan line, a bottom hint pill, and a top bar with close and manual-entry buttons;
 * while camera permission is missing, a request body with a grant action takes the preview's
 * place. Reports the first decoded payload (or a manually typed code) exactly once via
 * [onResult] and dismisses; the sheet routes it through the same marking flow as a typed
 * code.
 */
@OptIn(ExperimentalGetImage::class)
@Composable
fun QrScannerScreen(
    onResult: (String) -> Unit,
    onClose: () -> Unit,
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false),
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        var hasPermission by remember { mutableStateOf(context.hasCameraPermission()) }
        var consumed by remember { mutableStateOf(false) }
        var showCodeDialog by remember { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted -> hasPermission = granted }

        DisposableEffect(Unit) {
            if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
            onDispose {}
        }

        val controller = remember(hasPermission) {
            if (!hasPermission) {
                null
            } else {
                LifecycleCameraController(context).apply {
                    setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
                    setImageAnalysisAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        QrAnalyzer { raw ->
                            if (!consumed) {
                                consumed = true
                                onResult(raw)
                            }
                        },
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            if (controller != null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            this.controller = controller
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    },
                )
                DisposableEffect(controller, lifecycleOwner) {
                    controller.bindToLifecycle(lifecycleOwner)
                    onDispose { controller.unbind() }
                }
                ScannerReticle()
                ScannerHint(stringResource(R.string.attendance_qr_hint))
            } else {
                PermissionDenied(onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) })
            }

            ScannerTopBar(
                onManualEntry = { showCodeDialog = true },
                onClose = onClose,
                modifier = Modifier.align(Alignment.TopStart),
            )
        }

        if (showCodeDialog) {
            ManualCodeDialog(
                onSubmit = { code ->
                    showCodeDialog = false
                    if (!consumed) {
                        consumed = true
                        onResult(code)
                    }
                },
                onDismiss = { showCodeDialog = false },
            )
        }
    }
}

/**
 * Manual fallback for QR payloads that won't scan: the typed code goes through the exact same
 * marking flow as a decoded one. Follows the M3 dialog anatomy — hero icon above the (then
 * center-aligned) headline, supporting text, end-aligned text buttons — with container color,
 * extra-large corners and typography coming from AlertDialog itself. The field is the
 * dialog's whole purpose, so it is focused (raising the IME) on entry.
 */
@Composable
private fun ManualCodeDialog(
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val haptic = rememberHapticManager()
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Keyboard, contentDescription = null) },
        title = { Text(stringResource(R.string.attendance_manual_code_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.attendance_manual_code_description))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = { Text(stringResource(R.string.attendance_activity_code)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { if (code.isNotBlank()) onSubmit(code.trim()) },
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = code.isNotBlank(),
                onClick = { haptic.tap(); onSubmit(code.trim()) },
            ) { Text(stringResource(R.string.attendance_record)) }
        },
        dismissButton = {
            TextButton(onClick = { haptic.tap(); onDismiss() }) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}

@OptIn(ExperimentalGetImage::class)
private class QrAnalyzer(private val onQr: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build(),
    )

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                barcodes.firstNotNullOfOrNull { it.rawValue }?.let(onQr)
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}

@Composable
private fun ScannerReticle() {
    val transition = rememberInfiniteTransition(label = "reticle")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sweep",
    )
    val accent = MaterialTheme.colorScheme.primary
    val frame = Color.White

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(264.dp)) {
            val cornerRadius = CornerRadius(40f, 40f)
            val bracket = size.minDimension * 0.22f
            val stroke = 10f

            drawRoundRect(
                color = frame.copy(alpha = 0.35f),
                cornerRadius = cornerRadius,
                style = Stroke(width = 4f),
            )

            val corners = listOf(
                Offset(0f, 0f) to listOf(Offset(0f, bracket), Offset(bracket, 0f)),
                Offset(size.width, 0f) to listOf(Offset(size.width, bracket), Offset(size.width - bracket, 0f)),
                Offset(0f, size.height) to listOf(Offset(0f, size.height - bracket), Offset(bracket, size.height)),
                Offset(size.width, size.height) to listOf(
                    Offset(size.width, size.height - bracket),
                    Offset(size.width - bracket, size.height),
                ),
            )
            corners.forEach { (corner, arms) ->
                arms.forEach { arm ->
                    drawLine(accent, corner, arm, strokeWidth = stroke, cap = StrokeCap.Round)
                }
            }

            val y = bracket + (size.height - 2 * bracket) * sweep
            drawLine(
                color = accent,
                start = Offset(bracket * 0.6f, y),
                end = Offset(size.width - bracket * 0.6f, y),
                strokeWidth = 6f,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun ScannerHint(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 64.dp)
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(percent = 50))
                .padding(horizontal = 20.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun ScannerTopBar(
    onManualEntry: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { haptic.tap(); onClose() },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.45f),
                contentColor = Color.White,
            ),
        ) {
            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.common_close))
        }

        IconButton(
            onClick = { haptic.tap(); onManualEntry() },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.45f),
                contentColor = Color.White,
            ),
        ) {
            Icon(
                Icons.Outlined.Keyboard,
                contentDescription = stringResource(R.string.attendance_manual_code_entry)
            )
        }
    }
}

@Composable
private fun PermissionDenied(onRequest: () -> Unit) {
    val haptic = rememberHapticManager()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.NoPhotography,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.attendance_camera_permission_needed),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = { haptic.tap(); onRequest() }) { Text(stringResource(R.string.attendance_allow_camera)) }
    }
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
