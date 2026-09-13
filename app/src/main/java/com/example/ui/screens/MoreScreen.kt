package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SSKNFullBrandCard
import com.example.ui.components.SSKNTopBar
import com.example.ui.theme.*

@Composable
fun MoreScreen(
    selectedClass: Int,
    onSelectClass: (Int) -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenMistakes: () -> Unit,
    onOpenAdmin: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("more_screen")
    ) {
        SSKNTopBar(
            title = "Study Hub & Settings",
            onSearchClick = onSearchClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SSKN Brand Card
            item {
                SSKNFullBrandCard()
            }

            // Study Tools Section
            item {
                Text(
                    text = "ACADEMIC UTILITIES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText,
                    letterSpacing = 0.8.sp
                )
            }

            item {
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        MoreMenuItem(
                            icon = Icons.Outlined.BookmarkBorder,
                            iconTint = PrimaryBlue,
                            title = "Saved Bookmarks",
                            subtitle = "Access bookmarked NCERT pages & high-yield points",
                            onClick = onOpenBookmarks
                        )
                        HorizontalDivider(color = BorderSubtle.copy(alpha = 0.6f))
                        MoreMenuItem(
                            icon = Icons.Outlined.ErrorOutline,
                            iconTint = ErrorRed,
                            title = "Mistakes & 'Learn From This'",
                            subtitle = "Review questions answered incorrectly & see AI diagnosis",
                            onClick = onOpenMistakes
                        )
                        HorizontalDivider(color = BorderSubtle.copy(alpha = 0.6f))
                        MoreMenuItem(
                            icon = Icons.Outlined.Security,
                            iconTint = SuccessGreen,
                            title = "Authorized Content Architecture",
                            subtitle = "Zero-scraping policy & NCERT ingestion schema status",
                            onClick = onOpenAdmin
                        )
                    }
                }
            }

            // Class Preference Section
            item {
                Text(
                    text = "DEFAULT CLASS LEVEL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText,
                    letterSpacing = 0.8.sp
                )
            }

            item {
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Active Syllabus Target",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                            Text(
                                text = "Class $selectedClass Biology Selected",
                                fontSize = 12.sp,
                                color = SecondaryText
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedClass == 11,
                                onClick = { onSelectClass(11) },
                                label = { Text("Class 11") }
                            )
                            FilterChip(
                                selected = selectedClass == 12,
                                onClick = { onSelectClass(12) },
                                label = { Text("Class 12") }
                            )
                        }
                    }
                }
            }

            // About App & Core Philosophy
            item {
                Text(
                    text = "CORE PHILOSOPHY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText,
                    letterSpacing = 0.8.sp
                )
            }

            item {
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "“EVERY PAGE OF NCERT SHOULD BECOME PRACTICE.”",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "SSKN is built on one simple truth: NEET UG Biology is 100% NCERT-based. We do not overwhelm you with social feeds, streaks, or bloated question banks. We take every line of NCERT, analyze its NEET potential, pair it with verified PYQs, and test your mastery.",
                            fontSize = 12.5.sp,
                            color = PrimaryText.copy(alpha = 0.85f),
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "SSKN Version 1.0.0 · Authorized 2024-25 NCERT Standard",
                            fontSize = 11.sp,
                            color = SecondaryText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = SecondaryText
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = SecondaryText
        )
    }
}
