package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.GeminiMCQInteractionComponent
import com.example.ui.components.SSKNTopBar
import com.example.ui.theme.*

@Composable
fun AIMCQInteractionScreen(
    currentQuestion: Question?,
    isGenerating: Boolean,
    selectedOption: Int?,
    isAnswered: Boolean,
    score: Int,
    attempted: Int,
    correct: Int,
    preferredType: QuestionType?,
    difficulty: Difficulty,
    onAnswerOption: (Int) -> Unit,
    onGenerateNext: (chapterId: String?, pageNumber: Int?, type: QuestionType?, diff: Difficulty) -> Unit,
    onViewInNCERT: (chapterId: String, pageNumber: Int) -> Unit,
    onAskSaleemSir: (Question) -> Unit,
    onBackClick: () -> Unit,
    onToggleBookmark: (Question) -> Unit,
    isBookmarked: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var selectedChapterId by remember { mutableStateOf(currentQuestion?.sourceChapterId ?: "c_cell_8") }
    var selectedPageNum by remember { mutableStateOf(currentQuestion?.sourcePageNumber ?: 126) }
    var selectedType by remember { mutableStateOf(preferredType) }
    var selectedDiff by remember { mutableStateOf(difficulty) }

    Scaffold(
        topBar = {
            SSKNTopBar(
                title = "AI MCQ Drill (Gemini 3.5)",
                onBackClick = onBackClick,
                actions = {
                    Surface(
                        color = AILight,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AIPurple,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LIVE AI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AIPurple
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_mcq_interaction_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppBackground)
                .verticalScroll(scrollState)
        ) {
            // Live Session Stats Bar
            SessionStatsBar(
                score = score,
                attempted = attempted,
                correct = correct
            )

            // Filter Configuration Pill Bar
            ConfigFilterBar(
                selectedType = selectedType,
                selectedDiff = selectedDiff,
                onSelectType = {
                    selectedType = it
                    onGenerateNext(selectedChapterId, selectedPageNum, it, selectedDiff)
                },
                onSelectDiff = {
                    selectedDiff = it
                    onGenerateNext(selectedChapterId, selectedPageNum, selectedType, it)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Question Area / Loading State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (isGenerating) {
                    AIGeneratingCard(
                        pageNumber = selectedPageNum,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (currentQuestion != null) {
                    GeminiMCQInteractionComponent(
                        question = currentQuestion,
                        selectedOptionIndex = selectedOption,
                        isAnswered = isAnswered,
                        onSelectOption = onAnswerOption,
                        onViewInNCERT = onViewInNCERT,
                        onGenerateNext = {
                            onGenerateNext(selectedChapterId, selectedPageNum, selectedType, selectedDiff)
                        },
                        onAskSaleemSir = onAskSaleemSir,
                        onToggleBookmark = { onToggleBookmark(currentQuestion) },
                        isBookmarked = isBookmarked,
                        isGeneratingNext = isGenerating
                    )
                } else {
                    // Initial State: Prompt to generate
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(AILight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = AIPurple,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Ready to Master NCERT with AI?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Generate authentic NEET MCQs with instant feedback, line-by-line NCERT explanations, and trap alerts.",
                                fontSize = 13.sp,
                                color = SecondaryText,
                                modifier = Modifier.padding(horizontal = 12.dp),
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = {
                                    onGenerateNext(selectedChapterId, selectedPageNum, selectedType, selectedDiff)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_ai_drill_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Generate First Question",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SessionStatsBar(
    score: Int,
    attempted: Int,
    correct: Int
) {
    Surface(
        color = MidnightNavy,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Net Score
            Column {
                Text(
                    text = "NEET SCORE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (score >= 0) "+$score" else "$score",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (score >= 0) SuccessGreen else ErrorRed
                )
            }

            // Attempted
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ATTEMPTED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$attempted",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Accuracy %
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val acc = if (attempted > 0) (correct * 100) / attempted else 0
                Text(
                    text = "ACCURACY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$acc%",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (acc >= 75) SuccessGreen else if (acc >= 50) WarningOrange else Color.White
                )
            }

            // Correct Count (Academic metric, no gamification)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "CORRECT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$correct",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
private fun ConfigFilterBar(
    selectedType: QuestionType?,
    selectedDiff: Difficulty,
    onSelectType: (QuestionType?) -> Unit,
    onSelectDiff: (Difficulty) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Filter:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SecondaryText
        )

        // All Types Chip
        FilterChip(
            selected = selectedType == null,
            onClick = { onSelectType(null) },
            label = { Text("All Formats", fontSize = 12.sp) }
        )

        // Single Choice
        FilterChip(
            selected = selectedType == QuestionType.SINGLE_CORRECT,
            onClick = { onSelectType(QuestionType.SINGLE_CORRECT) },
            label = { Text("Single Choice", fontSize = 12.sp) }
        )

        // Assertion-Reason
        FilterChip(
            selected = selectedType == QuestionType.ASSERTION_REASON,
            onClick = { onSelectType(QuestionType.ASSERTION_REASON) },
            label = { Text("Assertion-Reason", fontSize = 12.sp) }
        )

        // Statement I & II
        FilterChip(
            selected = selectedType == QuestionType.STATEMENT_BASED,
            onClick = { onSelectType(QuestionType.STATEMENT_BASED) },
            label = { Text("Statement I & II", fontSize = 12.sp) }
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Difficulty toggles
        FilterChip(
            selected = selectedDiff == Difficulty.NEET_LEVEL,
            onClick = { onSelectDiff(Difficulty.NEET_LEVEL) },
            label = { Text("NEET Level", fontSize = 12.sp) }
        )

        FilterChip(
            selected = selectedDiff == Difficulty.CHALLENGING,
            onClick = { onSelectDiff(Difficulty.CHALLENGING) },
            label = { Text("Challenging", fontSize = 12.sp) }
        )
    }
}

@Composable
private fun AIGeneratingCard(
    pageNumber: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier.testTag("ai_generating_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = AIPurple,
                strokeWidth = 3.dp,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Gemini 3.5 Flash is Crafting Your Question",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Extracting high-yield NCERT Page $pageNumber concepts, designing plausible distractors, and writing line-referenced explanations...",
                fontSize = 13.sp,
                color = SecondaryText,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = VeryLightBlue,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Strictly Grounded in NCERT Biology",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
