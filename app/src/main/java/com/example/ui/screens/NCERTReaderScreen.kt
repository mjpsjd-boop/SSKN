package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.ImportanceBadge
import com.example.ui.components.SSKNTopBar
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NCERTReaderScreen(
    page: NCERTPageData,
    neetLens: NEETLensAnalysis?,
    isLensLoading: Boolean,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onBackClick: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onStartPractice: (count: Int) -> Unit,
    onStartAIDrill: () -> Unit = {},
    onAskAI: (String) -> Unit,
    chatMessages: List<ChatMessage>,
    isChatLoading: Boolean,
    pagePYQs: List<Question>,
    onScanPage: () -> Unit = {},
    onQuestionFromPoint: (ImportantPoint) -> Unit = {},
    scanningStage: String? = null,
    aiErrorMessage: String? = null,
    aiMode: PracticeMode = PracticeMode.NCERT_STRICT,
    onToggleAIMode: () -> Unit = {},
    diagramAnalysis: DiagramAnalysisResult? = null,
    isDiagramAnalysisLoading: Boolean = false,
    onAnalyzeDiagram: () -> Unit = {},
    conceptComparison: ConceptComparison? = null,
    isComparingConcepts: Boolean = false,
    onCompareConcepts: (String, String) -> Unit = { _, _ -> },
    onClearComparison: () -> Unit = {},
    summary: PageSummary? = null,
    isSummaryLoading: Boolean = false,
    onGenerateSummary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var fontSizeSp by remember { mutableStateOf(15) }
    var isFocusMode by remember { mutableStateOf(false) }
    var activeSheet by remember { mutableStateOf<ReaderSheetType?>(null) }
    var selectedHighlightPoint by remember { mutableStateOf<ImportantPoint?>(null) }
    var showNeetLensPanel by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val highlightSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val scrollState = rememberScrollState()
    var horizontalDragAccumulator by remember { mutableStateOf(0f) }

    Scaffold(
        topBar = {
            if (!isFocusMode) {
                SSKNTopBar(
                    title = "NCERT Page ${page.pageNumber}",
                    onBackClick = onBackClick,
                    onBookmarkClick = onToggleBookmark,
                    isBookmarked = isBookmarked,
                    actions = {
                        IconButton(
                            onClick = {
                                fontSizeSp = if (fontSizeSp < 19) fontSizeSp + 2 else 13
                            },
                            modifier = Modifier.testTag("reader_font_size_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "Font size",
                                tint = SecondaryText
                            )
                        }
                        IconButton(
                            onClick = { isFocusMode = true },
                            modifier = Modifier.testTag("reader_focus_mode_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Focus Mode",
                                tint = SecondaryText
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!isFocusMode) {
                ReaderBottomDock(
                    onOpenScan = { activeSheet = ReaderSheetType.PAGE_ANALYSIS },
                    onOpenImportant = { activeSheet = ReaderSheetType.IMPORTANT_POINTS },
                    onOpenPractice = { activeSheet = ReaderSheetType.PRACTICE_CONFIG },
                    onOpenAskAI = { activeSheet = ReaderSheetType.ASK_AI },
                    onOpenPYQs = { activeSheet = ReaderSheetType.PAGE_PYQS },
                    pyqCount = pagePYQs.size
                )
            }
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("ncert_reader_screen")
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppBackground)
                .pointerInput(page.pageNumber) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (horizontalDragAccumulator < -90f) {
                                onNextPage()
                            } else if (horizontalDragAccumulator > 90f) {
                                onPreviousPage()
                            }
                            horizontalDragAccumulator = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            horizontalDragAccumulator += dragAmount
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Focus Mode Exit Bar
                if (isFocusMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Focus Mode · Page ${page.pageNumber}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryText
                        )
                        Button(
                            onClick = { isFocusMode = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VeryLightBlue,
                                contentColor = PrimaryBlue
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FullscreenExit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Page Header Metadata
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CLASS ${page.classLevel} BIOLOGY · CH ${page.chapterNumber}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = page.chapterName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VeryLightBlue)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Page ${page.pageNumber}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Switcher Tab (NCERT Reader vs AI MCQ Drill)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("NCERT Page", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onStartAIDrill() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AIPurple,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI MCQ Drill", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = SecondaryText)
                        }
                    }
                }

                // AI Mode Selector & Quick Action Pills
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = if (aiMode == PracticeMode.NCERT_STRICT) Color(0xFFF0FDF4) else Color(0xFFF5F3FF),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (aiMode == PracticeMode.NCERT_STRICT) SuccessGreen.copy(alpha = 0.5f) else AIPurple.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleAIMode() }
                        .testTag("ai_mode_toggle_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (aiMode == PracticeMode.NCERT_STRICT) Icons.Default.CheckCircle else Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (aiMode == PracticeMode.NCERT_STRICT) SuccessGreen else AIPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (aiMode == PracticeMode.NCERT_STRICT) "AI MODE: NCERT STRICT" else "AI MODE: NEET EXAM FOCUS",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (aiMode == PracticeMode.NCERT_STRICT) SuccessGreen else AIPurple
                                )
                                Text(
                                    text = if (aiMode == PracticeMode.NCERT_STRICT) "Grounded strictly in NCERT textbook statements" else "Includes high-yield exam traps & examiner patterns",
                                    fontSize = 10.sp,
                                    color = SecondaryText
                                )
                            }
                        }
                        Surface(
                            color = if (aiMode == PracticeMode.NCERT_STRICT) SuccessGreen else AIPurple,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "SWITCH",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // AI Action Quick Launch Bar
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssistChip(
                        onClick = {
                            onGenerateSummary()
                            activeSheet = ReaderSheetType.SUMMARY_30_SEC
                        },
                        label = { Text("⚡ 30-Sec Summary", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    AssistChip(
                        onClick = { activeSheet = ReaderSheetType.COMPARE_CONCEPTS },
                        label = { Text("⚖️ Compare Concepts", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    if (page.diagramTitle != null) {
                        AssistChip(
                            onClick = {
                                onAnalyzeDiagram()
                                activeSheet = ReaderSheetType.DIAGRAM_ANALYSIS
                            },
                            label = { Text("🔬 Diagram AI", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                    AssistChip(
                        onClick = { activeSheet = ReaderSheetType.ASK_AI },
                        label = { Text("💬 Ask Page AI", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Prominent SCAN THIS PAGE Banner / Animated Scan State
                if (isLensLoading) {
                    Surface(
                        color = Color(0xFFF5F3FF),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AIPurple.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = AIPurple,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = scanningStage ?: "Analyzing your NCERT page...",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AIPurple
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                color = AIPurple,
                                trackColor = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { activeSheet = ReaderSheetType.PAGE_ANALYSIS; onScanPage() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AIPurple,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reader_scan_page_btn"),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SCAN THIS PAGE (NEET LENS)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // NEET Lens Highlights & Insights Panel (Collapsible)
                if (false && neetLens != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showNeetLensPanel = !showNeetLensPanel },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = WarningOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "NEET LENS ANALYSIS",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MidnightNavy,
                                        letterSpacing = 0.6.sp
                                    )
                                }
                                Icon(
                                    imageVector = if (showNeetLensPanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = SecondaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (showNeetLensPanel) {
                                Spacer(modifier = Modifier.height(10.dp))

                                // Must Remember
                                if (neetLens.mustRemember.isNotEmpty()) {
                                    Text(
                                        text = "MUST REMEMBER",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorRed,
                                        letterSpacing = 0.5.sp
                                    )
                                    neetLens.mustRemember.take(3).forEach { mr ->
                                        Text(
                                            text = "• $mr",
                                            fontSize = 12.sp,
                                            color = PrimaryText,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                // NEET Focus
                                Text(
                                    text = "NEET FOCUS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = neetLens.neetFocus,
                                    fontSize = 12.sp,
                                    color = PrimaryText,
                                    lineHeight = 16.sp
                                )

                                // Key Terms & Common Confusions
                                if (neetLens.keyTerms.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "KEY TERMS: " + neetLens.keyTerms.take(5).joinToString(", "),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SecondaryText
                                    )
                                }
                                if (neetLens.commonConfusions.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "COMMON CONFUSIONS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        letterSpacing = 0.5.sp
                                    )
                                    neetLens.commonConfusions.take(2).forEach { cc ->
                                        Text(
                                            text = "⚠️ $cc",
                                            fontSize = 11.5.sp,
                                            color = MidnightNavy,
                                            lineHeight = 15.sp,
                                            modifier = Modifier.padding(vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section Title Banner
                Text(
                    text = page.sectionTitle,
                    fontSize = (fontSizeSp + 3).sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryText,
                    lineHeight = (fontSizeSp + 8).sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Digital Textbook Body Card
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Authorized Edition Tag
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NCERT TEXT (AUTHORIZED)",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryText,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = page.contentVersion,
                                fontSize = 9.5.sp,
                                color = SecondaryText
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderSubtle.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Text content rendered line by line with highlights on important points
                        val lines = page.content.split("\n\n")
                        lines.forEach { paragraph ->
                            if (paragraph.isNotBlank()) {
                                val matchedKeyPoint = page.keyPoints.find { kp ->
                                    kp.sourceLine != null && paragraph.contains(kp.sourceLine.take(25))
                                }

                                if (matchedKeyPoint != null) {
                                    val bg = when (matchedKeyPoint.importance) {
                                        ImportanceRanking.CRITICAL -> Color(0xFFFEF9C3) // rich soft yellow
                                        ImportanceRanking.HIGH -> Color(0xFFDBEAFE) // soft light blue
                                        else -> Color(0xFFF1F5F9) // subtle slate
                                    }
                                    val borderColor = when (matchedKeyPoint.importance) {
                                        ImportanceRanking.CRITICAL -> Color(0xFFEAB308)
                                        ImportanceRanking.HIGH -> Color(0xFF3B82F6)
                                        else -> Color(0xFF94A3B8)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bg)
                                            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
                                            .clickable { selectedHighlightPoint = matchedKeyPoint }
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                ImportanceBadge(ranking = matchedKeyPoint.importance)
                                                Text(
                                                    text = "Tap for Questions & AI ↗",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryBlue
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = paragraph,
                                                fontSize = fontSizeSp.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MidnightNavy,
                                                lineHeight = (fontSizeSp * 1.55).sp
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = paragraph,
                                        fontSize = fontSizeSp.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = PrimaryText,
                                        lineHeight = (fontSizeSp * 1.55).sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }

                // Diagram / Illustration Card if available
                if (page.diagramTitle != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = SurfaceCard,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = page.diagramTitle,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text(
                                        text = page.diagramDescription ?: "",
                                        fontSize = 12.5.sp,
                                        color = PrimaryText,
                                        lineHeight = 17.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Caption: ${page.diagramCaption ?: ""}",
                                        fontSize = 11.5.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = SecondaryText
                                    )
                                }
                            }

                            // 4 Required Diagram Buttons
                            Spacer(modifier = Modifier.height(12.dp))
                            // Compact, book-first action grid: one action per cell prevents text/icon collisions.
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { onAskAI("Explain this diagram '${page.diagramTitle}': ${page.diagramDescription}"); activeSheet = ReaderSheetType.ASK_AI },
                                        shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                    ) { Text("Explain", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                    Button(
                                        onClick = { onAnalyzeDiagram(); activeSheet = ReaderSheetType.DIAGRAM_ANALYSIS },
                                        shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = AIPurple),
                                        modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                    ) { Text("Diagram analysis", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = onStartAIDrill, shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue), modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                    ) { Text("MCQs", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                    OutlinedButton(
                                        onClick = { activeSheet = ReaderSheetType.ASK_AI }, shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                    ) { Text("Ask about page", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                                }
                            }
                        }
                    }
                }

                // Previous / Next Page Navigation Bar
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onPreviousPage,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.testTag("reader_prev_page_btn")
                    ) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prev Page", fontSize = 13.sp)
                    }

                    Text(
                        text = "Page ${page.pageNumber}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryText
                    )

                    Button(
                        onClick = onNextPage,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("reader_next_page_btn")
                    ) {
                        Text("Next Page", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }

    // Modal Bottom Sheets for the 4 Reader Action Docks
    if (activeSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = sheetState,
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            when (activeSheet) {
                ReaderSheetType.PAGE_ANALYSIS -> {
                    PageAnalysisSheetContent(
                        page = page,
                        analysis = neetLens,
                        isLoading = isLensLoading,
                        scanningStage = scanningStage,
                        errorMessage = aiErrorMessage,
                        onScan = onScanPage,
                        onClose = { activeSheet = null },
                        onGenerateMCQs = {
                            activeSheet = null
                            onStartAIDrill()
                        }
                    )
                }
                ReaderSheetType.IMPORTANT_POINTS -> {
                    ImportantPointsSheetContent(
                        page = page,
                        neetLens = neetLens,
                        isLoading = isLensLoading,
                        onPracticeFromPoint = { onStartPractice(5) }
                    )
                }
                ReaderSheetType.PRACTICE_CONFIG -> {
                    PracticeConfigSheetContent(
                        pageNumber = page.pageNumber,
                        onSelectOption = { count ->
                            activeSheet = null
                            onStartPractice(count)
                        },
                        onStartAIDrill = {
                            activeSheet = null
                            onStartAIDrill()
                        }
                    )
                }
                ReaderSheetType.ASK_AI -> {
                    AskAISheetContent(
                        pageNumber = page.pageNumber,
                        chapterName = page.chapterName,
                        chatMessages = chatMessages,
                        isLoading = isChatLoading,
                        aiMode = aiMode,
                        onToggleAIMode = onToggleAIMode,
                        onSendMessage = onAskAI
                    )
                }
                ReaderSheetType.PAGE_PYQS -> {
                    PagePYQsSheetContent(
                        pageNumber = page.pageNumber,
                        pyqs = pagePYQs,
                        onPracticeAll = {
                            activeSheet = null
                            onStartPractice(pagePYQs.size.coerceAtLeast(5))
                        }
                    )
                }
                ReaderSheetType.DIAGRAM_ANALYSIS -> {
                    DiagramAnalysisSheetContent(
                        pageNumber = page.pageNumber,
                        diagramTitle = page.diagramTitle ?: "NCERT Diagram",
                        analysis = diagramAnalysis,
                        isLoading = isDiagramAnalysisLoading,
                        onRetry = onAnalyzeDiagram,
                        onPracticeQuestions = {
                            activeSheet = null
                            onStartAIDrill()
                        }
                    )
                }
                ReaderSheetType.COMPARE_CONCEPTS -> {
                    ConceptComparisonSheetContent(
                        comparison = conceptComparison,
                        isLoading = isComparingConcepts,
                        onCompare = onCompareConcepts,
                        onClear = onClearComparison
                    )
                }
                ReaderSheetType.SUMMARY_30_SEC -> {
                    Summary30SecSheetContent(
                        pageNumber = page.pageNumber,
                        chapterName = page.chapterName,
                        summary = summary,
                        isLoading = isSummaryLoading,
                        onRegenerate = onGenerateSummary
                    )
                }
                null -> {}
            }
        }
    }

    // Modal Bottom Sheet for Point-Level Highlight Tap
    if (selectedHighlightPoint != null) {
        val pt = selectedHighlightPoint!!
        ModalBottomSheet(
            onDismissRequest = { selectedHighlightPoint = null },
            sheetState = highlightSheetState,
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            HighlightPointDetailSheetContent(
                point = pt,
                pageNumber = page.pageNumber,
                onQuestionFromPoint = {
                    val target = pt
                    selectedHighlightPoint = null
                    onQuestionFromPoint(target)
                },
                onExplain = {
                    val target = pt
                    selectedHighlightPoint = null
                    onAskAI("Explain this NCERT line and its underlying biological mechanism simply: ${target.text}")
                    activeSheet = ReaderSheetType.ASK_AI
                },
                onWhyImportant = {
                    val target = pt
                    selectedHighlightPoint = null
                    onAskAI("Why is this line high-yield for NEET UG: ${target.text}")
                    activeSheet = ReaderSheetType.ASK_AI
                },
                onAskAI = {
                    selectedHighlightPoint = null
                    activeSheet = ReaderSheetType.ASK_AI
                },
                onViewPYQs = {
                    selectedHighlightPoint = null
                    activeSheet = ReaderSheetType.PAGE_PYQS
                }
            )
        }
    }
}

@Composable
private fun PageAnalysisSheetContent(
    page: NCERTPageData,
    analysis: NEETLensAnalysis?,
    isLoading: Boolean,
    scanningStage: String?,
    errorMessage: String?,
    onScan: () -> Unit,
    onClose: () -> Unit,
    onGenerateMCQs: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .navigationBarsPadding()
            .testTag("page_analysis_sheet")
    ) {
        Text("PAGE ANALYSIS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = PrimaryText, letterSpacing = 0.8.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Page ${page.pageNumber} · ${page.chapterName}", fontSize = 12.sp, color = SecondaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = AIPurple)
                Spacer(modifier = Modifier.width(10.dp))
                Text(scanningStage ?: "Scanning page…", fontSize = 12.5.sp, color = AIPurple, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Reading NCERT content and finding question potential…", fontSize = 11.5.sp, color = SecondaryText)
        } else if (errorMessage != null) {
            Text(errorMessage, fontSize = 12.5.sp, color = ErrorRed, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onScan, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Text("Retry", fontWeight = FontWeight.Bold)
                }
                Button(onClick = onClose, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = SecondaryText)) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        } else if (analysis == null) {
            Text("Scan only this NCERT page to extract grounded important points and question potential.", fontSize = 12.5.sp, color = PrimaryText, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onScan, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = AIPurple)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan This Page", fontWeight = FontWeight.Bold)
            }
        } else {
            AnalysisSection("MOST IMPORTANT", analysis.mustRemember)
            AnalysisSection("NEET FOCUS", listOf(analysis.neetFocus))
            AnalysisSection("KEY FACTS", analysis.keyNCERTFacts)
            AnalysisSection("QUESTION POTENTIAL", listOf(analysis.questionPotential))
            if (!analysis.keyTerms.isNullOrEmpty()) AnalysisSection("KEY TERMS", analysis.keyTerms)
            if (!analysis.diagramFocus.isNullOrBlank()) AnalysisSection("DIAGRAM FOCUS", listOf(analysis.diagramFocus!!))
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onGenerateMCQs, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate MCQs From This Page", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AnalysisSection(title: String, values: List<String>) {
    if (values.isEmpty()) return
    Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue, letterSpacing = 0.6.sp)
    values.take(4).forEach { value ->
        Text("• $value", fontSize = 12.sp, color = PrimaryText, lineHeight = 17.sp, modifier = Modifier.padding(top = 3.dp))
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun HighlightPointDetailSheetContent(
    point: ImportantPoint,
    pageNumber: Int,
    onQuestionFromPoint: () -> Unit,
    onExplain: () -> Unit,
    onWhyImportant: () -> Unit,
    onAskAI: () -> Unit,
    onViewPYQs: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("highlight_point_detail_sheet")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImportanceBadge(ranking = point.importance)
            Text(
                text = "NCERT Page $pageNumber",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryText
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quoted Point
        Surface(
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "“${point.text}”",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Why it matters in NEET
        Text(
            text = "WHY IT MATTERS IN NEET",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue,
            letterSpacing = 0.6.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = point.reason,
            fontSize = 12.5.sp,
            color = PrimaryText,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Question Potential
        Text(
            text = "QUESTION POTENTIAL",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = WarningOrange,
            letterSpacing = 0.6.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = point.questionType,
            fontSize = 12.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Primary Action: Question From This Point
        Button(
            onClick = onQuestionFromPoint,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("point_question_btn"),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(imageVector = Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("QUESTION FROM THIS POINT", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Secondary Action Row: Explain, Why Important, PYQs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onExplain,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("Explain", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onWhyImportant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1.2f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("Why Tested?", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onViewPYQs,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("View PYQs", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

enum class ReaderSheetType {
    PAGE_ANALYSIS,
    IMPORTANT_POINTS,
    PRACTICE_CONFIG,
    ASK_AI,
    PAGE_PYQS,
    DIAGRAM_ANALYSIS,
    COMPARE_CONCEPTS,
    SUMMARY_30_SEC
}

@Composable
private fun ReaderBottomDock(
    onOpenScan: () -> Unit,
    onOpenImportant: () -> Unit,
    onOpenPractice: () -> Unit,
    onOpenAskAI: () -> Unit,
    onOpenPYQs: () -> Unit,
    pyqCount: Int
) {
    Surface(
        color = SurfaceCard,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("reader_bottom_dock")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockButton(
                icon = Icons.Default.AutoAwesome,
                iconTint = AIPurple,
                title = "Scan Page",
                onClick = onOpenScan,
                testTag = "dock_btn_scan_page"
            )
            DockButton(
                icon = Icons.Default.Bolt,
                iconTint = WarningOrange,
                title = "Important",
                onClick = onOpenImportant,
                testTag = "dock_btn_important"
            )
            DockButton(
                icon = Icons.Default.Quiz,
                iconTint = PrimaryBlue,
                title = "Practice",
                onClick = onOpenPractice,
                testTag = "dock_btn_practice"
            )
            DockButton(
                icon = Icons.Default.AutoAwesome,
                iconTint = AIPurple,
                title = "Ask AI",
                onClick = onOpenAskAI,
                testTag = "dock_btn_ask_ai"
            )
            DockButton(
                icon = Icons.Default.Verified,
                iconTint = SuccessGreen,
                title = if (pyqCount > 0) "PYQs ($pyqCount)" else "PYQs",
                onClick = onOpenPYQs,
                testTag = "dock_btn_pyqs"
            )
        }
    }
}

@Composable
private fun DockButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryText
        )
    }
}

@Composable
private fun ImportantPointsSheetContent(
    page: NCERTPageData,
    neetLens: NEETLensAnalysis?,
    isLoading: Boolean,
    onPracticeFromPoint: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("important_points_sheet"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = WarningOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NEET Lens & High-Yield Points",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }
                VerificationBadge(sourceType = SourceType.AI_GENERATED, customText = "SSKN LENS")
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // NEET Lens Summary Card
        item {
            Surface(
                color = VeryLightBlue,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SoftBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "EXAM FOCUS FOR PAGE ${page.pageNumber}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = neetLens?.neetFocus ?: "Focus heavily on exact scientific names, dates, and definitions verbatim from this page.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MidnightNavy,
                        lineHeight = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "HIGH-YIELD POINTS ON THIS PAGE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryText,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // List of Important Points with ImportanceRanking Badges
        items(page.keyPoints) { kp ->
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ImportanceBadge(ranking = kp.importance)
                        Text(
                            text = kp.questionType,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryText
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = kp.text,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Why it matters: ${kp.reason}",
                        fontSize = 12.sp,
                        color = SecondaryText,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onPracticeFromPoint,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VeryLightBlue,
                            contentColor = PrimaryBlue
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Practice This Point",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeConfigSheetContent(
    pageNumber: Int,
    onSelectOption: (Int) -> Unit,
    onStartAIDrill: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .testTag("practice_config_sheet")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Quiz,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Practice Page $pageNumber",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Every question is strictly mapped to NCERT lines & verified NEET PYQs.",
            fontSize = 12.5.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(18.dp))

        // AI Dynamic Drill Button (Gemini 3.5)
        Surface(
            color = AILight,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, AIPurple.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStartAIDrill() }
                .testTag("reader_ai_dynamic_drill_btn")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AIPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡ AI MCQ Drill (Gemini 3.5)",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightNavy
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Instant feedback, distractor breakdown & line explanations",
                        fontSize = 11.5.sp,
                        color = SecondaryText
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = AIPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        PracticeOptionButton(
            title = "Quick Drill (5 Questions)",
            subtitle = "Direct NCERT facts & assertion-reason",
            onClick = { onSelectOption(5) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        PracticeOptionButton(
            title = "Standard Page Mastery (10 Questions)",
            subtitle = "Comprehensive coverage of all points on page $pageNumber",
            onClick = { onSelectOption(10) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        PracticeOptionButton(
            title = "Deep NEET Drill (15 Questions)",
            subtitle = "Rigorous single correct, statement & trap questions",
            onClick = { onSelectOption(15) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PracticeOptionButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        color = VeryLightBlue,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SoftBlue),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = SecondaryText
                )
            }
        }
    }
}

@Composable
private fun AskAISheetContent(
    pageNumber: Int,
    chapterName: String,
    chatMessages: List<ChatMessage>,
    isLoading: Boolean,
    aiMode: PracticeMode = PracticeMode.NCERT_STRICT,
    onToggleAIMode: () -> Unit = {},
    onSendMessage: (String) -> Unit
) {
    var queryText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(16.dp)
            .testTag("ask_ai_sheet")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AIPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ask This Page",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (aiMode == PracticeMode.NCERT_STRICT) Color(0xFFDCFCE7) else Color(0xFFF3E8FF),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onToggleAIMode() }
                ) {
                    Text(
                        text = if (aiMode == PracticeMode.NCERT_STRICT) "NCERT STRICT" else "NEET FOCUS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (aiMode == PracticeMode.NCERT_STRICT) SuccessGreen else AIPurple,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AILight)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "PAGE $pageNumber",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AIPurple
                    )
                }
            }
        }

        // Quick prompt chips
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PromptChip("Explain Simply") { onSendMessage("Explain the core concept on this page simply in 3 points.") }
            PromptChip("Simplify Line") { onSendMessage("Simplify the most difficult sentence on this page in plain English.") }
            PromptChip("Why in NEET?") { onSendMessage("Why is page $pageNumber high-yield for NEET UG? What does NTA look for?") }
            PromptChip("What to Memorize?") { onSendMessage("What exact facts, names, or numbers must I memorize from this page?") }
            PromptChip("What to Understand?") { onSendMessage("What conceptual mechanisms on this page require deep understanding rather than rote memorization?") }
            PromptChip("Common Confusions") { onSendMessage("What are common student confusions and examiner traps on this page?") }
            PromptChip("Make 3 MCQs") { onSendMessage("Generate 3 strictly NCERT-based NEET MCQs from this page with explanations.") }
            PromptChip("Assertion-Reason") { onSendMessage("Create 1 tricky Assertion-Reason question strictly from this page's statements.") }
            PromptChip("30-Sec Summary") { onSendMessage("Give me a 30-second rapid bullet summary of page $pageNumber.") }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = false
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ask any biology doubt strictly grounded on Page $pageNumber.\nEvery answer clearly distinguishes [NCERT BASED] vs [NEET EXAM FOCUS].",
                            fontSize = 12.sp,
                            color = SecondaryText,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            items(chatMessages) { msg ->
                val isUser = msg.isUser
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isUser) PrimaryBlue else Color(0xFFF1F5F9))
                            .padding(12.dp)
                    ) {
                        if (!isUser && msg.sourceContext != null) {
                            Surface(
                                color = if (msg.sourceContext.contains("NCERT")) Color(0xFFDCFCE7) else Color(0xFFF3E8FF),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = msg.sourceContext,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (msg.sourceContext.contains("NCERT")) SuccessGreen else AIPurple,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = msg.text,
                            fontSize = 13.sp,
                            color = if (isUser) Color.White else PrimaryText,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AIPurple,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analyzing NCERT Page $pageNumber with Gemini...",
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    }
                }
            }
        }

        // Input field
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                placeholder = { Text("Ask about page $pageNumber...", fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ask_ai_input"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (queryText.isNotBlank()) {
                        onSendMessage(queryText)
                        queryText = ""
                    }
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AIPurple)
                    .testTag("ask_ai_send_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun PromptChip(text: String, onClick: () -> Unit) {
    Surface(
        color = AILight,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AIPurple.copy(alpha = 0.2f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = AIPurple,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun PagePYQsSheetContent(
    pageNumber: Int,
    pyqs: List<Question>,
    onPracticeAll: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .testTag("page_pyqs_sheet"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PYQs From Page $pageNumber",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }
                VerificationBadge(sourceType = SourceType.VERIFIED_PYQ)
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (pyqs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No direct verified PYQs currently indexed for Page $pageNumber.\nUse 'Practice' to solve NCERT-strictly generated MCQs.",
                        fontSize = 13.sp,
                        color = SecondaryText,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            items(pyqs) { pyq ->
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${pyq.pyqExam} · ${pyq.pyqPaperCode ?: "Official"}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                            Text(
                                text = "Q.${pyq.pyqQuestionNumber ?: 1}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryText
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pyq.questionText,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryText,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Correct Answer: ${pyq.options.getOrNull(pyq.correctAnswerIndex)?.text ?: ""}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onPracticeAll,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Practice These in Interactive Quiz Mode")
                }
            }
        }
    }
}

@Composable
private fun DiagramAnalysisSheetContent(
    pageNumber: Int,
    diagramTitle: String,
    analysis: DiagramAnalysisResult?,
    isLoading: Boolean,
    onRetry: () -> Unit,
    onPracticeQuestions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(18.dp)
            .testTag("diagram_analysis_sheet")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AIPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NCERT Diagram AI Analysis",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy
                )
            }
            Surface(
                color = AILight,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "PAGE $pageNumber",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = AIPurple,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = diagramTitle,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryBlue
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AIPurple, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Analyzing anatomical structures & NTA NEET patterns...",
                        fontSize = 12.5.sp,
                        color = SecondaryText
                    )
                }
            }
        } else if (analysis != null) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        color = VeryLightBlue,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SoftBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "WHAT THIS DIAGRAM REPRESENTS",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = analysis.representation,
                                fontSize = 13.sp,
                                color = MidnightNavy,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                if (analysis.keyStructures.isNotEmpty()) {
                    item {
                        Text(
                            text = "KEY STRUCTURES & LABELS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            analysis.keyStructures.forEach { label ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("• ", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = label,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PrimaryText
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "BIOLOGICAL PROCESS / SEQUENCE",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = analysis.processOrSequence,
                                fontSize = 12.5.sp,
                                color = PrimaryText,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                if (analysis.potentialQuestions.isNotEmpty()) {
                    item {
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚠️ POTENTIAL NEET QUESTIONS & TESTED ANGLES",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ErrorRed,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    analysis.potentialQuestions.forEach { q ->
                                        Row(verticalAlignment = Alignment.Top) {
                                            Text("• ", color = ErrorRed, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = q,
                                                fontSize = 12.sp,
                                                color = MidnightNavy,
                                                lineHeight = 17.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = onPracticeQuestions,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Practice AI Diagram MCQs")
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Detailed NCERT-grounded diagram analysis will appear here.",
                        fontSize = 12.5.sp,
                        color = SecondaryText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onRetry) {
                        Text("Analyze This Diagram")
                    }
                }
            }
        }
    }
}

@Composable
private fun ConceptComparisonSheetContent(
    comparison: ConceptComparison?,
    isLoading: Boolean,
    onCompare: (String, String) -> Unit,
    onClear: () -> Unit
) {
    var conceptA by remember { mutableStateOf("") }
    var conceptB by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 580.dp)
            .padding(18.dp)
            .testTag("concept_comparison_sheet")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CompareArrows,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Biology Concept Comparator",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy
                )
            }
            if (comparison != null) {
                TextButton(onClick = onClear) {
                    Text("Clear", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = conceptA,
                onValueChange = { conceptA = it },
                label = { Text("Concept A", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = conceptB,
                onValueChange = { conceptB = it },
                label = { Text("Concept B", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick suggestions row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssistChip(
                onClick = {
                    conceptA = "Gram-positive bacteria"
                    conceptB = "Gram-negative bacteria"
                    onCompare(conceptA, conceptB)
                },
                label = { Text("Gram+ vs Gram-", fontSize = 10.5.sp) }
            )
            AssistChip(
                onClick = {
                    conceptA = "Rough ER"
                    conceptB = "Smooth ER"
                    onCompare(conceptA, conceptB)
                },
                label = { Text("RER vs SER", fontSize = 10.5.sp) }
            )
            AssistChip(
                onClick = {
                    conceptA = "Prokaryotic Cell"
                    conceptB = "Eukaryotic Cell"
                    onCompare(conceptA, conceptB)
                },
                label = { Text("Prokaryote vs Eukaryote", fontSize = 10.5.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (conceptA.isNotBlank() && conceptB.isNotBlank()) {
                    onCompare(conceptA.trim(), conceptB.trim())
                }
            },
            enabled = conceptA.isNotBlank() && conceptB.isNotBlank() && !isLoading,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Comparing with NCERT Precision...")
            } else {
                Text("Compare Concepts")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (comparison != null) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Surface(
                        color = VeryLightBlue,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SoftBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "DEFINITION DIFFERENCE",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = comparison.definitionDifference,
                                fontSize = 12.5.sp,
                                color = MidnightNavy,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                if (comparison.processDifferences.isNotEmpty()) {
                    item {
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "PROCESS & MECHANISM DIFFERENCES",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightNavy,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    comparison.processDifferences.forEach { proc ->
                                        Row(verticalAlignment = Alignment.Top) {
                                            Text("• ", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = proc,
                                                fontSize = 12.sp,
                                                color = PrimaryText,
                                                lineHeight = 17.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (comparison.keyDistinctions.isNotEmpty()) {
                    item {
                        Text(
                            text = "NCERT DISTINCTION POINTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            comparison.keyDistinctions.forEach { dist ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("• ", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = dist,
                                        fontSize = 12.sp,
                                        color = PrimaryText,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⚠️ NEET EXAM TIP & HIGH YIELD TRAP",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = comparison.neetExamTip,
                                fontSize = 12.sp,
                                color = MidnightNavy,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Summary30SecSheetContent(
    pageNumber: Int,
    chapterName: String,
    summary: PageSummary?,
    isLoading: Boolean,
    onRegenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(18.dp)
            .testTag("summary_30_sec_sheet")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = WarningOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "30-Second Rapid Summary",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy
                )
            }
            Surface(
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "PAGE $pageNumber",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarningOrange,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = WarningOrange, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Synthesizing 30-second rapid NCERT review...",
                        fontSize = 12.5.sp,
                        color = SecondaryText
                    )
                }
            }
        } else if (summary != null) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        color = VeryLightBlue,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SoftBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "CORE IDEA",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = summary.coreIdea,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MidnightNavy,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                if (summary.mustRememberFacts.isNotEmpty()) {
                    item {
                        Text(
                            text = "MUST-REMEMBER FACTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            summary.mustRememberFacts.forEach { fact ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("✓ ", color = SuccessGreen, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = fact,
                                        fontSize = 12.5.sp,
                                        color = PrimaryText,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }

                if (summary.keyTerminology.isNotEmpty()) {
                    item {
                        Text(
                            text = "KEY TERMINOLOGY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            summary.keyTerminology.forEach { term ->
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = term,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (summary.trapOrConfusion != null) {
                    item {
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚠️ NEET TRAP TO WATCH OUT FOR",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ErrorRed
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = summary.trapOrConfusion,
                                    fontSize = 12.sp,
                                    color = MidnightNavy,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Generate a rapid 30-second review of Page $pageNumber.",
                        fontSize = 12.5.sp,
                        color = SecondaryText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onRegenerate) {
                        Text("Generate 30-Sec Summary")
                    }
                }
            }
        }
    }
}
