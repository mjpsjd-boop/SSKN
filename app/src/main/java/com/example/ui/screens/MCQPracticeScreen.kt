package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.DifficultyBadge
import com.example.ui.components.SSKNTopBar
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*

@Composable
fun MCQPracticeScreen(
    title: String,
    questions: List<Question>,
    currentIndex: Int,
    selectedOptionIndex: Int?,
    isAnswerSubmitted: Boolean,
    score: Int,
    isCompleted: Boolean,
    onSelectOption: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onViewInNCERT: (chapterId: String, pageNumber: Int) -> Unit,
    onReviewMistakes: () -> Unit,
    onFinish: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            SSKNTopBar(
                title = title,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            if (!isCompleted && questions.isNotEmpty()) {
                Surface(
                    color = SurfaceCard,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (!isAnswerSubmitted) {
                            Button(
                                onClick = onSubmitAnswer,
                                enabled = selectedOptionIndex != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryBlue,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("practice_submit_btn")
                            ) {
                                Text(
                                    text = "Submit Answer",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Button(
                                onClick = onNextQuestion,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryBlue,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("practice_next_btn")
                            ) {
                                Text(
                                    text = if (currentIndex < questions.size - 1) "Next Question →" else "View Results",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("mcq_practice_screen")
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppBackground)
        ) {
            if (questions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No questions available for this selection.",
                        color = SecondaryText,
                        fontSize = 14.sp
                    )
                }
            } else if (isCompleted) {
                // Practice Summary Screen
                PracticeSummaryView(
                    score = score,
                    total = questions.size,
                    onReviewMistakes = onReviewMistakes,
                    onFinish = onFinish
                )
            } else {
                val currentQuestion = questions.getOrNull(currentIndex) ?: return@Box

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Header Bar: Progress + Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUESTION ${currentIndex + 1} OF ${questions.size}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText,
                            letterSpacing = 1.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DifficultyBadge(difficulty = currentQuestion.difficulty)
                            VerificationBadge(
                                sourceType = currentQuestion.sourceType,
                                customText = currentQuestion.pyqExam
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / questions.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PrimaryBlue,
                        trackColor = VeryLightBlue,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Question Card
                    Surface(
                        color = SurfaceCard,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = currentQuestion.questionText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText,
                                lineHeight = 22.sp
                            )

                            if (currentQuestion.statement1 != null && currentQuestion.statement2 != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Statement I: ${currentQuestion.statement1}",
                                            fontSize = 13.sp,
                                            color = PrimaryText
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Statement II: ${currentQuestion.statement2}",
                                            fontSize = 13.sp,
                                            color = PrimaryText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Interactive Options (A, B, C, D)
                    currentQuestion.options.forEachIndexed { index, option ->
                        val isSelected = selectedOptionIndex == index
                        val isCorrectOption = index == currentQuestion.correctAnswerIndex

                        val (cardBg, borderColor, textTint) = when {
                            !isAnswerSubmitted && isSelected -> Triple(VeryLightBlue, PrimaryBlue, PrimaryBlue)
                            !isAnswerSubmitted && !isSelected -> Triple(SurfaceCard, BorderSubtle, PrimaryText)
                            isAnswerSubmitted && isCorrectOption -> Triple(Color(0xFFDCFCE7), SuccessGreen, SuccessGreen)
                            isAnswerSubmitted && isSelected && !isCorrectOption -> Triple(Color(0xFFFEE2E2), ErrorRed, ErrorRed)
                            else -> Triple(SurfaceCard, BorderSubtle, PrimaryText)
                        }

                        Surface(
                            color = cardBg,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(if (isSelected || (isAnswerSubmitted && isCorrectOption)) 1.5.dp else 1.dp, borderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clickable(enabled = !isAnswerSubmitted) {
                                    onSelectOption(index)
                                }
                                .testTag("option_${index}_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val optionLetter = when (index) {
                                    0 -> "A"
                                    1 -> "B"
                                    2 -> "C"
                                    else -> "D"
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected || (isAnswerSubmitted && isCorrectOption)) textTint.copy(alpha = 0.15f) else Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = optionLetter,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected || (isAnswerSubmitted && isCorrectOption)) textTint else SecondaryText
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = option.text,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = PrimaryText,
                                    modifier = Modifier.weight(1f),
                                    lineHeight = 18.sp
                                )

                                if (isAnswerSubmitted) {
                                    if (isCorrectOption) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Correct",
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = "Incorrect",
                                            tint = ErrorRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Post-submission Explanation Card
                    if (isAnswerSubmitted) {
                        Spacer(modifier = Modifier.height(16.dp))

                        val wasCorrect = selectedOptionIndex == currentQuestion.correctAnswerIndex

                        Surface(
                            color = SurfaceCard,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (wasCorrect) SuccessGreen.copy(alpha = 0.4f) else ErrorRed.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (wasCorrect) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (wasCorrect) SuccessGreen else ErrorRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (wasCorrect) "Correct! Well Done" else "Incorrect Answer",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (wasCorrect) SuccessGreen else ErrorRed
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = currentQuestion.explanation,
                                    fontSize = 13.sp,
                                    color = PrimaryText,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // NCERT Connection Banner & Jump Button
                                Surface(
                                    color = VeryLightBlue,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "EXACT NCERT SOURCE",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryBlue,
                                                letterSpacing = 0.6.sp
                                            )
                                            Text(
                                                text = "${currentQuestion.sourceChapterName} · Page ${currentQuestion.sourcePageNumber}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MidnightNavy
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                onViewInNCERT(
                                                    currentQuestion.sourceChapterId,
                                                    currentQuestion.sourcePageNumber
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PrimaryBlue,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                                            modifier = Modifier.testTag("view_in_ncert_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("View in NCERT", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun PracticeSummaryView(
    score: Int,
    total: Int,
    onReviewMistakes: () -> Unit,
    onFinish: () -> Unit
) {
    val percentage = if (total > 0) (score * 100) / total else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(if (percentage >= 70) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (percentage >= 70) Icons.Default.EmojiEvents else Icons.Default.School,
                contentDescription = null,
                tint = if (percentage >= 70) SuccessGreen else WarningOrange,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Practice Completed!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = PrimaryText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "NCERT Page Mastery Drill",
            fontSize = 13.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score / $total",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "Score",
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(BorderSubtle)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percentage%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = if (percentage >= 70) SuccessGreen else WarningOrange
                    )
                    Text(
                        text = "Accuracy",
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (score < total) {
            OutlinedButton(
                onClick = onReviewMistakes,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("summary_review_mistakes_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Review Mistakes & Learn (${total - score})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onFinish,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("summary_return_btn")
        ) {
            Text(
                text = "Back to Learning",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
