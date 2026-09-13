package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

/**
 * Modern, accessible MCQ Interaction Component that renders AI-generated questions
 * from the Gemini API with immediate interactive feedback, visual scoring,
 * line-by-line NCERT explanations, and distractor misconception analysis.
 */
@Composable
fun GeminiMCQInteractionComponent(
    question: Question,
    selectedOptionIndex: Int?,
    isAnswered: Boolean,
    onSelectOption: (Int) -> Unit,
    onViewInNCERT: (chapterId: String, pageNumber: Int) -> Unit,
    modifier: Modifier = Modifier,
    onGenerateNext: (() -> Unit)? = null,
    onAskSaleemSir: ((Question) -> Unit)? = null,
    onToggleBookmark: (() -> Unit)? = null,
    isBookmarked: Boolean = false,
    isGeneratingNext: Boolean = false,
    showScoreBadge: Boolean = true
) {
    val wasCorrect = isAnswered && selectedOptionIndex == question.correctAnswerIndex

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(
            width = if (isAnswered) {
                if (wasCorrect) 1.5.dp else 1.5.dp
            } else 1.dp,
            color = if (isAnswered) {
                if (wasCorrect) SuccessGreen.copy(alpha = 0.6f) else ErrorRed.copy(alpha = 0.6f)
            } else BorderSubtle
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("gemini_mcq_card_${question.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Bar: AI Model Badge + Metadata + Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Gemini 3.5 AI Badge
                    Surface(
                        color = AILight,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, AIPurple.copy(alpha = 0.3f))
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
                                text = "GEMINI 3.5 FLASH",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AIPurple,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Question Type Badge
                    val typeLabel = when (question.questionType) {
                        QuestionType.ASSERTION_REASON -> "Assertion-Reason"
                        QuestionType.STATEMENT_BASED -> "Statement I & II"
                        QuestionType.MATCH_FOLLOWING -> "Match Following"
                        else -> "Single Choice"
                    }
                    Surface(
                        color = VeryLightBlue,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Difficulty Badge
                    DifficultyBadge(difficulty = question.difficulty)
                }

                if (onToggleBookmark != null) {
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("mcq_bookmark_btn")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark question",
                            tint = if (isBookmarked) WarningOrange else SecondaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Question Prompt
            Text(
                text = question.questionText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                lineHeight = 23.sp,
                modifier = Modifier.testTag("mcq_question_prompt")
            )

            // Special layout for Assertion-Reason questions
            if (question.isAssertionReason || question.assertionText != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row {
                        Text(
                            text = "Assertion (A): ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MidnightNavy
                        )
                        Text(
                            text = question.assertionText ?: "",
                            fontSize = 13.5.sp,
                            color = PrimaryText,
                            lineHeight = 19.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Text(
                            text = "Reason (R): ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MidnightNavy
                        )
                        Text(
                            text = question.reasonText ?: "",
                            fontSize = 13.5.sp,
                            color = PrimaryText,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            // Special layout for Statement-based questions
            if (question.statement1 != null && question.statement2 != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Statement I: ${question.statement1}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.5.sp,
                        color = PrimaryText,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Statement II: ${question.statement2}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.5.sp,
                        color = PrimaryText,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Interactive Options (A, B, C, D) with Immediate Feedback styling
            question.options.forEachIndexed { index, option ->
                val isSelected = selectedOptionIndex == index
                val isCorrectOption = index == question.correctAnswerIndex

                // Feedback colors:
                // Pre-answer: neutral or selected light blue
                // Post-answer: Correct option is green, wrong selected is red
                val (backgroundColor, borderColor, textHighlightColor) = when {
                    !isAnswered && isSelected -> Triple(VeryLightBlue, PrimaryBlue, PrimaryBlue)
                    !isAnswered && !isSelected -> Triple(SurfaceCard, BorderSubtle, PrimaryText)
                    isAnswered && isCorrectOption -> Triple(Color(0xFFDCFCE7), SuccessGreen, SuccessGreen)
                    isAnswered && isSelected && !isCorrectOption -> Triple(Color(0xFFFEE2E2), ErrorRed, ErrorRed)
                    else -> Triple(SurfaceCard, BorderSubtle.copy(alpha = 0.6f), SecondaryText)
                }

                Surface(
                    color = backgroundColor,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = if ((isAnswered && (isCorrectOption || isSelected)) || (!isAnswered && isSelected)) 1.5.dp else 1.dp,
                        color = borderColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isAnswered) {
                            onSelectOption(index)
                        }
                        .testTag("mcq_option_${index}_${question.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val optionLetter = when (index) {
                            0 -> "A"
                            1 -> "B"
                            2 -> "C"
                            else -> "D"
                        }

                        // Option Circle
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isAnswered && isCorrectOption -> SuccessGreen
                                        isAnswered && isSelected && !isCorrectOption -> ErrorRed
                                        !isAnswered && isSelected -> PrimaryBlue
                                        else -> Color(0xFFF1F5F9)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isAnswered && isCorrectOption) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Correct Answer",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else if (isAnswered && isSelected && !isCorrectOption) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Wrong Answer",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    text = optionLetter,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else SecondaryText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Option Text
                        Text(
                            text = option.text,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected || (isAnswered && isCorrectOption)) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isAnswered && !isCorrectOption && !isSelected) SecondaryText else PrimaryText,
                            modifier = Modifier.weight(1f),
                            lineHeight = 19.sp
                        )

                        // Status Icon on the right
                        if (isAnswered) {
                            Spacer(modifier = Modifier.width(8.dp))
                            if (isCorrectOption) {
                                Surface(
                                    color = SuccessGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "CORRECT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (isSelected) {
                                Surface(
                                    color = ErrorRed.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "YOU CHOSE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorRed,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Immediate Feedback & Explanations Panel
            AnimatedVisibility(
                visible = isAnswered,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .testTag("immediate_feedback_panel")
                ) {
                    // Immediate Score Callout
                    Surface(
                        color = if (wasCorrect) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (wasCorrect) SuccessGreen.copy(alpha = 0.4f) else ErrorRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (wasCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (wasCorrect) SuccessGreen else ErrorRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (wasCorrect) "Correct! +4 NEET Marks" else "Incorrect (-1 Mark) · Review NCERT Line",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (wasCorrect) SuccessGreen else ErrorRed
                                )
                                Text(
                                    text = if (wasCorrect) "Mastered NCERT Page ${question.sourcePageNumber} concept." else "Review why the distractor was a trap below.",
                                    fontSize = 12.sp,
                                    color = PrimaryText.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Comprehensive Explanation Section
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Section Header: NCERT Explanation
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "NCERT RATIONALE & EXPLANATION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue,
                                    letterSpacing = 0.6.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = question.explanation,
                                fontSize = 13.5.sp,
                                color = PrimaryText,
                                lineHeight = 20.sp
                            )

                            // Distractor Analysis (Why each option is incorrect)
                            if (question.whyOptionsAreWrong.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = BorderSubtle, thickness = 0.8.dp)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = null,
                                        tint = SecondaryText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "DISTRACTOR BREAKDOWN (WHY OTHER OPTIONS ARE WRONG)",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SecondaryText,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                question.whyOptionsAreWrong.forEachIndexed { optIdx, explanationItem ->
                                    if (optIdx != question.correctAnswerIndex) {
                                        val optLetter = when (optIdx) {
                                            0 -> "A"
                                            1 -> "B"
                                            2 -> "C"
                                            else -> "D"
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "• Option $optLetter: ",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedOptionIndex == optIdx) ErrorRed else PrimaryText
                                            )
                                            Text(
                                                text = explanationItem,
                                                fontSize = 12.5.sp,
                                                color = if (selectedOptionIndex == optIdx) PrimaryText else SecondaryText,
                                                lineHeight = 17.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            // Saleem Sir's NEET Tip
                            val tip = question.neetTip
                            if (!tip.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, WarningOrange.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            tint = WarningOrange,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "SALEEM SIR'S NEET EXAM TIP",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WarningOrange,
                                                letterSpacing = 0.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = tip,
                                                fontSize = 12.5.sp,
                                                color = Color(0xFF78350F),
                                                lineHeight = 17.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // View in NCERT Button
                        OutlinedButton(
                            onClick = {
                                onViewInNCERT(question.sourceChapterId, question.sourcePageNumber)
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, PrimaryBlue),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("mcq_view_in_ncert_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Page ${question.sourcePageNumber}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Ask AI clarification
                        if (onAskSaleemSir != null) {
                            IconButton(
                                onClick = { onAskSaleemSir(question) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AILight)
                                    .testTag("mcq_ask_ai_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "Ask Saleem Sir AI",
                                    tint = AIPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Next AI Question
                        if (onGenerateNext != null) {
                            Button(
                                onClick = onGenerateNext,
                                enabled = !isGeneratingNext,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryBlue,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(44.dp)
                                    .testTag("mcq_generate_next_btn")
                            ) {
                                if (isGeneratingNext) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Generating...", fontSize = 12.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Next AI MCQ →", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
