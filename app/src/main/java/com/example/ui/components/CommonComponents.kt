package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Difficulty
import com.example.data.model.ImportanceRanking
import com.example.data.model.SourceType
import com.example.ui.theme.*

enum class AppNavDestination(val title: String, val icon: ImageVector, val outlinedIcon: ImageVector) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    NCERT("NCERT", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    PYQS("PYQ Vault", Icons.Filled.Verified, Icons.Outlined.Verified),
    SEARCH("Search", Icons.Filled.Search, Icons.Outlined.Search),
    MORE("More", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
}

@Composable
fun SSKNTopBar(
    title: String? = null,
    onBackClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    onBookmarkClick: (() -> Unit)? = null,
    isBookmarked: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        color = SurfaceCard,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .testTag("sskn_top_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryText
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            if (title != null) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    SSKNLogo(emblemSize = 32.dp, showTagline = false, showFullName = false)
                }
            }

            if (onSearchClick != null) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.testTag("top_bar_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = SecondaryText
                    )
                }
            }

            if (onBookmarkClick != null) {
                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier.testTag("top_bar_bookmark_button")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) PrimaryBlue else SecondaryText
                    )
                }
            }

            actions()
        }
    }
}

@Composable
fun SSKNBottomNav(
    currentDestination: AppNavDestination,
    onSelectDestination: (AppNavDestination) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceCard,
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("sskn_bottom_navigation")
    ) {
        AppNavDestination.values().forEach { destination ->
            val selected = currentDestination == destination
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectDestination(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.icon else destination.outlinedIcon,
                        contentDescription = destination.title
                    )
                },
                label = {
                    Text(
                        text = destination.title,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBlue,
                    unselectedIconColor = SecondaryText,
                    unselectedTextColor = SecondaryText,
                    indicatorColor = VeryLightBlue
                ),
                modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
            )
        }
    }
}

@Composable
fun ImportanceBadge(
    ranking: ImportanceRanking,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (ranking) {
        ImportanceRanking.CRITICAL -> Triple(Color(0xFFFEE2E2), ErrorRed, "CRITICAL")
        ImportanceRanking.HIGH -> Triple(Color(0xFFFEF3C7), WarningOrange, "HIGH YIELD")
        ImportanceRanking.MEDIUM -> Triple(VeryLightBlue, PrimaryBlue, "IMPORTANT")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 2.5.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun VerificationBadge(
    sourceType: SourceType,
    modifier: Modifier = Modifier,
    customText: String? = null
) {
    val (bgColor, textColor, icon, label) = when (sourceType) {
        SourceType.VERIFIED_PYQ -> Quad(
            Color(0xFFDCFCE7),
            SuccessGreen,
            Icons.Default.Verified,
            customText ?: "VERIFIED PYQ"
        )
        SourceType.AI_GENERATED -> Quad(
            AILight,
            AIPurple,
            Icons.Default.AutoAwesome,
            customText ?: "AI GENERATED"
        )
        SourceType.DEMO_CONTENT -> Quad(
            Color(0xFFF1F5F9),
            Color(0xFF475569),
            Icons.Default.CheckCircleOutline,
            customText ?: "DEMO CONTENT"
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
fun DifficultyBadge(
    difficulty: Difficulty,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (difficulty) {
        Difficulty.EASY -> "Easy" to SuccessGreen
        Difficulty.MODERATE -> "Moderate" to BrightBlue
        Difficulty.NEET_LEVEL -> "NEET Level" to WarningOrange
        Difficulty.CHALLENGING -> "Challenging" to ErrorRed
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
