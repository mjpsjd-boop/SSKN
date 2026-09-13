package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/** A real-page, notebook-style reader backed by the bundled NCERT chapter PDF. */
@Composable
fun PdfLessonScreen(
    chapter: ChapterInfo,
    onBackClick: () -> Unit,
    onScanPage: (Int) -> Unit,
    onOpenMcqs: (Int) -> Unit,
    onAskAboutPage: (Int) -> Unit
) {
    val context = LocalContext.current
    var pdfFile by remember(chapter.id) { mutableStateOf<File?>(null) }
    var pageCount by remember(chapter.id) { mutableStateOf(0) }
    var currentPage by remember(chapter.id) { mutableStateOf(1) }
    var error by remember(chapter.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(chapter.id) {
        runCatching {
            val assetName = pdfAssetPathForChapter(chapter)
            val cached = File(context.cacheDir, "lesson_${chapter.classLevel}_${chapter.number}.pdf")
            if (!cached.exists() || cached.length() == 0L) {
                context.assets.open(assetName).use { input ->
                    cached.outputStream().use { output -> input.copyTo(output) }
                }
            }
            ParcelFileDescriptor.open(cached, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer -> pageCount = renderer.pageCount }
            }
            pdfFile = cached
        }.onFailure { error = it.message ?: "Unable to open the textbook pages" }
    }

    Column(Modifier.fillMaxSize().background(AppBackground)) {
        Surface(shadowElevation = 3.dp) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(Modifier.weight(1f)) {
                    Text("Class ${chapter.classLevel} Biology", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    Text(
                        "Chapter ${chapter.number}: ${chapter.name}",
                        maxLines = 1,
                        color = SecondaryText
                    )
                }
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Digital notebook", tint = PrimaryBlue, modifier = Modifier.size(22.dp))
            }
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text("NCERT DIGITAL NOTEBOOK", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            Text(
                if (pageCount > 0) "Page $currentPage of $pageCount · original NCERT page image" else "Preparing original NCERT pages…",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onScanPage(currentPage) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("AI scan page", maxLines = 1)
                }
                Button(
                    onClick = { onOpenMcqs(currentPage) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Quiz, contentDescription = null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Page MCQs", maxLines = 1)
                }
            }
        }

        when {
            error != null -> Text("Textbook page unavailable: $error", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
            pdfFile == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Opening your digital notebook…", color = SecondaryText) }
            else -> {
                NotebookPage(
                    file = pdfFile!!,
                    pageNumber = currentPage,
                    onAskAboutPage = { onAskAboutPage(currentPage) },
                    modifier = Modifier.weight(1f)
                )
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { if (currentPage > 1) currentPage-- },
                        enabled = currentPage > 1,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Previous")
                    }
                    Text("$currentPage / $pageCount", fontWeight = FontWeight.Bold, color = SecondaryText)
                    Button(
                        onClick = { if (currentPage < pageCount) currentPage++ },
                        enabled = currentPage < pageCount,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Next")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotebookPage(
    file: File,
    pageNumber: Int,
    onAskAboutPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bitmap by remember(file, pageNumber) { mutableStateOf<Bitmap?>(null) }
    var renderError by remember(file, pageNumber) { mutableStateOf<String?>(null) }

    LaunchedEffect(file, pageNumber) {
        bitmap = null
        renderError = null
        runCatching {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    renderer.openPage(pageNumber - 1).use { page ->
                        val width = 1400
                        val height = (width.toFloat() * page.height / page.width).toInt()
                        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { rendered ->
                            rendered.eraseColor(android.graphics.Color.WHITE)
                            page.render(rendered, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bitmap = rendered
                        }
                    }
                }
            }
        }.onFailure { renderError = it.message ?: "Unable to render this page" }
    }

    Surface(
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("NCERT page $pageNumber", fontWeight = FontWeight.Bold, color = SecondaryText, modifier = Modifier.weight(1f))
                Text("Read • Scan • Practice", style = MaterialTheme.typography.labelSmall, color = PrimaryBlue)
            }
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                when {
                    renderError != null -> Text(renderError!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                    bitmap != null -> Image(
                        bitmap!!.asImageBitmap(),
                        contentDescription = "Original NCERT page $pageNumber",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    else -> Text("Turning to page $pageNumber…", color = SecondaryText)
                }
            }
            Button(
                onClick = onAskAboutPage,
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppBackground, contentColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Ask AI about this page", maxLines = 1)
            }
        }
    }
}

fun pdfAssetPathForChapter(chapter: ChapterInfo): String =
    "textbooks/chapters/class_${chapter.classLevel}_chapter_${chapter.number}.pdf"
