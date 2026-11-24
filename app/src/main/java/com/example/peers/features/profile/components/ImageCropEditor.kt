package com.example.peers.features.profile.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.exifinterface.media.ExifInterface
import com.example.peers.ui.theme.GoogleSans
import com.example.peers.ui.theme.Lato
import com.example.peers.ui.theme.PeersPrimaryIndigo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

/**
 * Loads a bitmap from a content/file Uri, downscaling large photos safely and
 * applying EXIF rotation so camera captures display upright.
 */
private fun loadOrientedBitmap(context: Context, uri: Uri, maxDim: Int = 1400): Bitmap? {
    return runCatching {
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, boundsOpts)
        }
        var sampleSize = 1
        while (boundsOpts.outWidth / sampleSize > maxDim || boundsOpts.outHeight / sampleSize > maxDim) {
            sampleSize *= 2
        }
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val raw = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOpts)
        } ?: return null

        val orientation = context.contentResolver.openInputStream(uri)?.use { stream ->
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return raw
        }
        Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
    }.getOrNull()
}

/**
 * Renders the user-adjusted zoom & pan inside the circular crop viewport to a
 * 384x384 JPEG file (local display) and Base64 data URI (Firestore persistence).
 */
private fun renderCroppedPhoto(
    context: Context,
    source: Bitmap,
    viewportSize: IntSize,
    circleDiameterPx: Float,
    scale: Float,
    offset: Offset,
): Pair<String, String>? {
    if (viewportSize.width <= 0 || viewportSize.height <= 0 || circleDiameterPx <= 0f) return null

    val outSize = 384
    val output = Bitmap.createBitmap(outSize, outSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    canvas.drawColor(android.graphics.Color.WHITE)

    // Base ContentScale.Fit dimensions inside viewport
    val baseScale = min(
        viewportSize.width.toFloat() / source.width.toFloat(),
        viewportSize.height.toFloat() / source.height.toFloat()
    )

    // Map viewport center + circular crop bounds to output square [0..outSize]
    val toOut = outSize / circleDiameterPx
    val matrix = Matrix().apply {
        // Center image at origin
        postTranslate(-source.width / 2f, -source.height / 2f)
        // Apply base fit scale + user zoom scale
        postScale(baseScale * scale, baseScale * scale)
        // Apply user pan offset in viewport coordinates
        postTranslate(offset.x, offset.y)
        // Scale from crop circle size to output size and place at output center
        postScale(toOut, toOut)
        postTranslate(outSize / 2f, outSize / 2f)
    }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    canvas.drawBitmap(source, matrix, paint)

    // Save local file
    val file = File(context.filesDir, "peers_profile_cropped_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        output.compress(Bitmap.CompressFormat.JPEG, 88, out)
    }

    // Encode compact Data URI for Firestore storage
    val baos = ByteArrayOutputStream()
    output.compress(Bitmap.CompressFormat.JPEG, 80, baos)
    val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    val dataUri = "data:image/jpeg;base64,$base64"

    return Uri.fromFile(file).toString() to dataUri
}

/**
 * Interactive Circular Image Crop Editor:
 * - User manually zooms in/out and pans horizontally/vertically (no auto-crop).
 * - Cancel discards changes; Save persists the exact adjusted frame.
 */
@Composable
fun ImageCropEditorDialog(
    imageUri: Uri,
    onCancel: () -> Unit,
    onSave: (localFileUri: String, dataUri: String) -> Unit,
) {
    val context = LocalContext.current
    var loadedBitmap by remember(imageUri) { mutableStateOf<Bitmap?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(imageUri) {
        loadedBitmap = withContext(Dispatchers.IO) {
            loadOrientedBitmap(context, imageUri)
        }
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101218))
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Adjust Profile Photo",
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Drag to reposition and pinch or slide to zoom",
                fontFamily = Lato,
                fontSize = 13.5.sp,
                color = Color.White.copy(alpha = 0.72f)
            )

            Spacer(Modifier.height(16.dp))

            // ── Circular Crop Viewport ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds()
                    .onSizeChanged { viewportSize = it }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(0.6f, 5f)
                            scale = newScale
                            offset = Offset(offset.x + pan.x, offset.y + pan.y)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                loadedBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Selected photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                    )
                }

                // Dimmed surround + Circular frame overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val diameter = size.minDimension * 0.82f
                    val radius = diameter / 2f
                    val circlePath = Path().apply {
                        addOval(
                            androidx.compose.ui.geometry.Rect(
                                left = center.x - radius,
                                top = center.y - radius,
                                right = center.x + radius,
                                bottom = center.y + radius
                            )
                        )
                    }
                    clipPath(circlePath, clipOp = ClipOp.Difference) {
                        drawRect(color = Color.Black.copy(alpha = 0.68f))
                    }
                    drawCircle(
                        color = Color.White,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Zoom Controls (Slider + Buttons) ──────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { scale = (scale - 0.25f).coerceIn(0.6f, 5f) }) {
                    Icon(Icons.Rounded.ZoomOut, contentDescription = "Zoom out", tint = Color.White)
                }
                Slider(
                    value = scale,
                    onValueChange = { scale = it },
                    valueRange = 0.6f..5f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = PeersPrimaryIndigo,
                        activeTrackColor = PeersPrimaryIndigo
                    )
                )
                IconButton(onClick = { scale = (scale + 0.25f).coerceIn(0.6f, 5f) }) {
                    Icon(Icons.Rounded.ZoomIn, contentDescription = "Zoom in", tint = Color.White)
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Cancel / Save Buttons ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "Cancel",
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Button(
                    onClick = {
                        val bmp = loadedBitmap ?: return@Button
                        val diameter = min(viewportSize.width, viewportSize.height) * 0.82f
                        val result = renderCroppedPhoto(
                            context = context,
                            source = bmp,
                            viewportSize = viewportSize,
                            circleDiameterPx = diameter,
                            scale = scale,
                            offset = offset
                        )
                        if (result != null) {
                            onSave(result.first, result.second)
                        }
                    },
                    enabled = loadedBitmap != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo)
                ) {
                    Text(
                        "Save",
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
