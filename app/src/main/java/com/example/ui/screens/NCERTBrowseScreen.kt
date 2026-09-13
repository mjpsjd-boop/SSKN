package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterInfo
import com.example.data.model.NCERTPageData
import com.example.data.model.UserProgress
import com.example.ui.components.ImportanceBadge
import com.example.ui.components.SSKNTopBar
import com.example.ui.theme.*

@Composable
fun NCERTBrowseScreen(
    selectedClass: Int,
    onSelectClass: (Int) -> Unit,
    chapters: List<ChapterInfo>,
    onOpenChapter: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("ncert_browse_screen")
    ) {
        SSKNTopBar(
            title = "NCERT Biology Library",
            onSearchClick = onSearchClick
        )

        // Class 11 vs Class 12 Segmented Tabs
        TabRow(
            selectedTabIndex = if (selectedClass == 11) 0 else 1,
            containerColor = SurfaceCard,
            contentColor = PrimaryBlue,
            divider = { HorizontalDivider(color = BorderSubtle) }
        ) {
            Tab(
                selected = selectedClass == 11,
                onClick = { onSelectClass(11) },
                text = {
                    Text(
                        text = "Class 11 Biology",
                        fontWeight = if (selectedClass == 11) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.testTag("tab_class_11")
            )
            Tab(
                selected = selectedClass == 12,
                onClick = { onSelectClass(12) },
                text = {
                    Text(
                        text = "Class 12 Biology",
                        fontWeight = if (selectedClass == 12) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.testTag("tab_class_12")
            )
        }

        // Chapter List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUTHORIZED NCERT SYLLABUS",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryText,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${chapters.size} Chapters",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                }
            }

            items(chapters) { chapter ->
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenChapter(chapter.id) }
                        .testTag("chapter_item_${chapter.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VeryLightBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${chapter.number}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chapter.name,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (chapter.contentAvailability == "VERIFIED_LOCAL") "Pages ${chapter.startPage} - ${chapter.endPage} · ${chapter.totalPages} pages" else "Original PDF lesson · ${chapter.totalPages} pages",
                                    fontSize = 12.sp,
                                    color = SecondaryText
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SecondaryText
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = chapter.description,
                            fontSize = 12.sp,
                            color = PrimaryText.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (chapter.isHighYield) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "HIGH YIELD",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WarningOrange
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (chapter.contentAvailability == "VERIFIED_LOCAL") "${chapter.verifiedPYQCount} Verified PYQs" else "PDF bundled",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }
                            }

                            Text(
                                text = "Open chapter",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterDetailScreen(
    chapter: ChapterInfo,
    pages: List<NCERTPageData>,
    userProgress: UserProgress,
    onBackClick: () -> Unit,
    onOpenPage: (Int) -> Unit,
    onOpenPdfLesson: () -> Unit,
    onPracticeChapter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("chapter_detail_screen")
    ) {
        SSKNTopBar(
            title = "Chapter ${chapter.number}",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Chapter Summary Card
            item {
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CLASS ${chapter.classLevel} BIOLOGY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                letterSpacing = 1.sp
                            )
                            if (chapter.isHighYield) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFEF3C7))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "HIGH YIELD FOR NEET",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningOrange
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = chapter.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryText
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = chapter.description,
                            fontSize = 13.sp,
                            color = SecondaryText,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onOpenPdfLesson,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open original NCERT PDF lesson", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onPracticeChapter,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("practice_chapter_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Practice Full Chapter (15 Questions)",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Pages List Header
            item {
                Text(
                    text = "NCERT PAGES IN THIS CHAPTER",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText,
                    letterSpacing = 1.sp
                )
            }

            if (pages.isEmpty()) {
                item {
                    Surface(
                        color = Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Original PDF lesson available", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "The complete original textbook chapter is bundled in the app. Open the PDF lesson above to read every page and use the page-specific scanner and AI actions.",
                                fontSize = 13.sp, color = PrimaryText, lineHeight = 19.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(chapter.sourceUrl, fontSize = 11.sp, color = PrimaryBlue, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            // Verified local page items
            items(pages) { page ->
                val isRead = userProgress.readPages.contains(page.id)

                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPage(page.pageNumber) }
                        .testTag("ncert_page_row_${page.pageNumber}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isRead) VeryLightBlue else Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "PAGE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRead) PrimaryBlue else SecondaryText
                                )
                                Text(
                                    text = "${page.pageNumber}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isRead) PrimaryBlue else PrimaryText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = page.sectionTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${page.keyPoints.size} High-Yield Points",
                                    fontSize = 11.5.sp,
                                    color = SecondaryText
                                )
                                if (page.verifiedPYQCount > 0) {
                                    Text(
                                        text = " · ${page.verifiedPYQCount} PYQs",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { onOpenPage(page.pageNumber) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VeryLightBlue,
                                contentColor = PrimaryBlue
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Read",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
