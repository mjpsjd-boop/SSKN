package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterInfo
import com.example.data.model.NEETGuidanceCategory
import com.example.data.model.UserProgress
import com.example.ui.components.SSKNEmblem
import com.example.ui.components.SSKNLogo
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    userProgress: UserProgress,
    chapters: List<ChapterInfo>,
    onContinueReading: (chapterId: String, pageNumber: Int) -> Unit,
    onSelectClass: (classLevel: Int) -> Unit,
    onOpenChapter: (chapterId: String) -> Unit,
    onNavigateToNCERT: () -> Unit,
    onNavigateToPYQs: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToMistakes: () -> Unit,
    onStartAIDrill: () -> Unit = {},
    onNavigateToGuidance: (NEETGuidanceCategory?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // App Header
        item {
            Surface(
                color = SurfaceCard,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SSKNLogo(
                        emblemSize = 36.dp,
                        showTagline = false,
                        showFullName = true
                    )
                    Row {
                        IconButton(
                            onClick = onNavigateToSearch,
                            modifier = Modifier.testTag("home_search_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = PrimaryText
                            )
                        }
                        IconButton(
                            onClick = onNavigateToBookmarks,
                            modifier = Modifier.testTag("home_bookmarks_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmarks",
                                tint = PrimaryText
                            )
                        }
                    }
                }
            }
        }

        // Hero: Continue Reading Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(MidnightNavy, SecondaryNavy, Color(0xFF1E293B))
                        )
                    )
                    .clickable {
                        onContinueReading(userProgress.lastChapterId, userProgress.lastPageNumber)
                    }
                    .padding(18.dp)
                    .testTag("continue_reading_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CURRENT PROGRESS",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftBlue,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Class ${userProgress.lastClass} Biology",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = userProgress.lastChapterName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Currently on NCERT Page ${userProgress.lastPageNumber}",
                        fontSize = 13.sp,
                        color = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                onContinueReading(userProgress.lastChapterId, userProgress.lastPageNumber)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Continue Reading",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${userProgress.readPages.size} pages read",
                            fontSize = 12.sp,
                            color = SoftBlue
                        )
                    }
                }
            }
        }

        // Class 11 & Class 12 Switcher Cards
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "NCERT BIOLOGY SYLLABUS",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryText,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Class 11 Card
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onSelectClass(11)
                            onNavigateToNCERT()
                        }
                        .testTag("home_class_11_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(VeryLightBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "11",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Class 11 Biology",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Cell, Photosynthesis & Division",
                            fontSize = 11.sp,
                            color = SecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Class 12 Card
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onSelectClass(12)
                            onNavigateToNCERT()
                        }
                        .testTag("home_class_12_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "12",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrightBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Class 12 Biology",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Genetics, Molecular & Repro",
                            fontSize = 11.sp,
                            color = SecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Quick Access Shortcuts Grid
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "QUICK STUDY TOOLS",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryText,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickToolItem(
                    icon = Icons.Default.MenuBook,
                    iconBg = VeryLightBlue,
                    iconTint = PrimaryBlue,
                    title = "NCERT\nReader",
                    onClick = onNavigateToNCERT,
                    modifier = Modifier.weight(1f)
                )
                QuickToolItem(
                    icon = Icons.Default.Verified,
                    iconBg = Color(0xFFDCFCE7),
                    iconTint = SuccessGreen,
                    title = "PYQ Vault\n2018-2026",
                    onClick = onNavigateToPYQs,
                    modifier = Modifier.weight(1f)
                )
                QuickToolItem(
                    icon = Icons.Default.ErrorOutline,
                    iconBg = Color(0xFFFEE2E2),
                    iconTint = ErrorRed,
                    title = "Mistakes\nLog",
                    onClick = onNavigateToMistakes,
                    modifier = Modifier.weight(1f)
                )
                QuickToolItem(
                    icon = Icons.Default.BookmarkBorder,
                    iconBg = Color(0xFFFEF3C7),
                    iconTint = WarningOrange,
                    title = "Saved\nBookmarks",
                    onClick = onNavigateToBookmarks,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Featured AI MCQ Practice Banner
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                color = MidnightNavy,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onStartAIDrill() }
                    .testTag("home_ai_mcq_featured_banner")
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(AIPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI MCQ Drill with Immediate Feedback",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Gemini 3.5 generates questions from NCERT lines with instant answers & distractor analysis.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = BrightBlue
                    )
                }
            }
        }

        // High-Yield NCERT Chapters List
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HIGH-YIELD CHAPTERS",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "See All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { onNavigateToNCERT() }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(chapters.filter { it.isHighYield }.take(4)) { chapter ->
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .clickable { onOpenChapter(chapter.id) }
                    .testTag("high_yield_chapter_${chapter.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(VeryLightBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${chapter.number}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Class ${chapter.classLevel}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SecondaryText
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "${chapter.verifiedPYQCount} PYQs",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = chapter.name,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = SecondaryText
                    )
                }
            }
        }

        // NEET Guidance & Philosophy Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("home_neet_guidance_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(VeryLightBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NEET GUIDANCE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Text(
                            text = "Explore All ↗",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.clickable { onNavigateToGuidance(null) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "“SSKN is: NCERT First. AI Second. Practice Third. Read each page like an exam question. Every single line can become a 4-mark question in NEET.”",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MidnightNavy,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "PREPARATION STRATEGY CATEGORIES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryText,
                        letterSpacing = 0.6.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(com.example.data.model.NEETGuidanceCategory.values()) { category ->
                            Surface(
                                color = VeryLightBlue,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, SoftBlue),
                                modifier = Modifier.clickable { onNavigateToGuidance(category) }
                            ) {
                                Text(
                                    text = category.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun QuickToolItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
            .clickable { onClick() }
            .testTag("quick_tool_${title.replace("\n", "_").lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText,
                lineHeight = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
