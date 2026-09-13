package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearnFromThisResult
import com.example.data.model.MistakeRecord
import com.example.ui.components.SSKNTopBar
import com.example.ui.theme.*

@Composable
fun MistakesScreen(
    mistakes: List<MistakeRecord>,
    activeDiagnosis: LearnFromThisResult? = null,
    isDiagnosing: Boolean = false,
    activeMistakeId: String? = null,
    onDiagnoseMistake: (MistakeRecord) -> Unit = {},
    onClearDiagnosis: () -> Unit = {},
    onTargetedPractice: (MistakeRecord) -> Unit = {},
    onViewInNCERT: (pageNumber: Int) -> Unit,
    onPracticeConcept: (pageNumber: Int) -> Unit,
    onDeleteMistake: (String) -> Unit,
    onClearAll: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("mistakes_screen")
    ) {
        SSKNTopBar(
            title = "Mistakes & Learn From This",
            onBackClick = onBackClick,
            actions = {
                if (mistakes.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text("Clear All", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )

        if (mistakes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Recorded Mistakes!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "When you solve MCQs in practice drills, any missed question is logged here for targeted conceptual diagnosis.",
                        fontSize = 13.sp,
                        color = SecondaryText,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${mistakes.size} concepts to strengthen. Focus on the underlying NCERT lines to eliminate traps in the actual exam.",
                                fontSize = 12.5.sp,
                                color = MidnightNavy,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                items(mistakes) { mistake ->
                    MistakeCard(
                        mistake = mistake,
                        isDiagnosingThis = isDiagnosing && activeMistakeId == mistake.id,
                        diagnosis = if (activeMistakeId == mistake.id) activeDiagnosis else null,
                        onDiagnose = { onDiagnoseMistake(mistake) },
                        onTargetedPractice = { onTargetedPractice(mistake) },
                        onViewInNCERT = { onViewInNCERT(mistake.pageNumber) },
                        onPracticeConcept = { onPracticeConcept(mistake.pageNumber) },
                        onDelete = { onDeleteMistake(mistake.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MistakeCard(
    mistake: MistakeRecord,
    isDiagnosingThis: Boolean,
    diagnosis: LearnFromThisResult?,
    onDiagnose: () -> Unit,
    onTargetedPractice: () -> Unit,
    onViewInNCERT: () -> Unit,
    onPracticeConcept: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${mistake.chapterName} · Page ${mistake.pageNumber}",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = SecondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Question Text
            Text(
                text = mistake.questionText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Options comparison (Student selection vs Correct answer)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "You chose: ${mistake.selectedOptionText}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ErrorRed
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Correct NCERT Answer: ${mistake.correctOptionText}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Learn From This Diagnosis Section
            if (isDiagnosingThis) {
                Surface(
                    color = AILight.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AIPurple.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = AIPurple,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Diagnosing misconception with Gemini 3.5...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = AIPurple
                        )
                    }
                }
            } else if (diagnosis != null) {
                Surface(
                    color = Color(0xFFFAF5FF),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, AIPurple.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AIPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI MISCONCEPTION DIAGNOSIS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AIPurple,
                                letterSpacing = 0.6.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "WHAT YOU MISUNDERSTOOD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText
                        )
                        Text(
                            text = diagnosis.whatYouMisunderstood,
                            fontSize = 12.5.sp,
                            color = MidnightNavy,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "CORRECT CONCEPT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText
                        )
                        Text(
                            text = diagnosis.correctConcept,
                            fontSize = 12.5.sp,
                            color = MidnightNavy,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "NCERT CONNECTION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText
                        )
                        Text(
                            text = diagnosis.ncertConnection,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = PrimaryBlue
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "WHAT TO REMEMBER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText
                        )
                        Text(
                            text = diagnosis.whatToRemember,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB45309)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onTargetedPractice,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AIPurple,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Practice This Concept", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Surface(
                    color = AILight.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AIPurple.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
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
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CONCEPT BREAKDOWN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AIPurple,
                                    letterSpacing = 0.6.sp
                                )
                            }
                            TextButton(
                                onClick = onDiagnose,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Learn From This", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AIPurple)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = mistake.misconceptionExplanation,
                            fontSize = 12.sp,
                            color = MidnightNavy,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewInNCERT,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Read NCERT Line", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onPracticeConcept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Re-Practice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
