package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ChapterInfo
import com.example.ui.theme.AppBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryText
import java.io.File

@Composable
fun PdfLessonScreen(
    chapter: ChapterInfo,
    onBackClick: () -> Unit,
    onScanPage: (Int) -> Unit,
    onOpenMcqs: () -> Unit,
    onAskAboutPage: (Int) -> Unit
) {
    val context = LocalContext.current
    var pdfFile by remember(chapter.id) { mutableStateOf<File?>(null) }
    var pageCount by remember(chapter.id) { mutableStateOf(0) }
    var error by remember(chapter.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(chapter.id) {
        runCatching {
            val assetName = pdfAssetPathForChapter(chapter)
            val cached = File(context.cacheDir, "lesson_${chapter.classLevel}_${chapter.number}.pdf")
            context.assets.open(assetName).use { input -> cached.outputStream().use { output -> input.copyTo(output) } }
            ParcelFileDescriptor.open(cached, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer -> pageCount = renderer.pageCount }
            }
            pdfFile = cached
        }.onFailure { error = it.message ?: "Unable to open the textbook PDF" }
    }

    Column(Modifier.fillMaxSize().background(AppBackground)) {
        Surface(shadowElevation = 3.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Back") }
                Column(Modifier.weight(1f)) {
                    Text("Class ${chapter.classLevel} Biology", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    Text("Chapter ${chapter.number}: ${chapter.name}", maxLines = 1, color = SecondaryText)
                }
                Icon(Icons.Default.MenuBook, "Original PDF lesson", tint = PrimaryBlue, modifier = Modifier.size(22.dp))
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text("Original NCERT PDF pages", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text("${if (pageCount > 0) pageCount else "Loading"} PDF pages · original page images · scanner/AI actions stay page-specific", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onScanPage(1) }, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)) {
                    Icon(Icons.Default.AutoAwesome, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Scan page", maxLines = 1)
                }
                Button(onClick = onOpenMcqs, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)) {
                    Icon(Icons.Default.Quiz, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("MCQs", maxLines = 1)
                }
            }
        }
        when {
            error != null -> Text("PDF unavailable: $error", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
            pdfFile == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Loading original textbook pages…", color = SecondaryText) }
            else -> LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
                items((1..pageCount).toList(), key = { it }) { pageNumber ->
                    PdfPageCard(pdfFile!!, pageNumber, onScanPage, onAskAboutPage)
                }
            }
        }
    }
}

@Composable
private fun PdfPageCard(file: File, pageNumber: Int, onScanPage: (Int) -> Unit, onAskAboutPage: (Int) -> Unit) {
    var bitmap by remember(file, pageNumber) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(file, pageNumber) {
        runCatching {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    renderer.openPage(pageNumber - 1).use { page ->
                        val width = 1200
                        val height = (width.toFloat() * page.height / page.width).toInt()
                        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { rendered ->
                            rendered.eraseColor(android.graphics.Color.WHITE)
                            page.render(rendered, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bitmap = rendered
                        }
                    }
                }
            }
        }
    }
    DisposableEffect(bitmap) { onDispose { bitmap?.takeIf { !it.isRecycled }?.recycle() } }
    Surface(shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("PDF page $pageNumber", Modifier.padding(start = 12.dp, top = 10.dp), fontWeight = FontWeight.Bold, color = SecondaryText)
            bitmap?.let { Image(it.asImageBitmap(), "NCERT page $pageNumber", Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth) }
                ?: Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) { Text("Rendering page…", color = SecondaryText) }
            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onScanPage(pageNumber) }, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)) { Text("Scan page", maxLines = 1) }
                Button(onClick = { onAskAboutPage(pageNumber) }, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)) { Text("Ask about page", maxLines = 1) }
            }
        }
    }
}

fun pdfAssetPathForChapter(chapter: ChapterInfo): String =
    "textbooks/chapters/class_${chapter.classLevel}_chapter_${chapter.number}.pdf"
