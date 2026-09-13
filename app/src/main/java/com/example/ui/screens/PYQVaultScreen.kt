package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.Question
import com.example.data.model.QuestionType
import com.example.data.model.SourceType
import com.example.ui.components.SSKNTopBar
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*

@Composable
fun PYQVaultScreen(
    pyqs: List<Question>,
    selectedYear: Int?,
    selectedType: QuestionType?,
    onFilterYear: (Int?) -> Unit,
    onFilterType: (QuestionType?) -> Unit,
    onViewInNCERT: (chapterId: String, pageNumber: Int) -> Unit,
    onPracticeAll: (List<Question>) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val years = listOf(null, 2024, 2023, 2022, 2021, 2020, 2019, 2018)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("pyq_vault_screen")
    ) {
        SSKNTopBar(
            title = "Verified PYQ Vault",
            onSearchClick = onSearchClick
        )

        // Year Filter Pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(years) { year ->
                val isSelected = selectedYear == year
                Surface(
                    color = if (isSelected) PrimaryBlue else SurfaceCard,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isSelected) PrimaryBlue else BorderSubtle),
                    modifier = Modifier
                        .clickable { onFilterYear(year) }
                        .testTag("pyq_filter_year_${year ?: "all"}")
                ) {
                    Text(
                        text = if (year == null) "All Years" else "NEET $year",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else PrimaryText,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Info Banner with Practice Button
        Surface(
            color = SurfaceCard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${pyqs.size} Verified Questions",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    Text(
                        text = "2018 - 2026 NTA/NEET Official",
                        fontSize = 11.sp,
                        color = SecondaryText
                    )
                }

                Button(
                    onClick = { onPracticeAll(pyqs) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("practice_filtered_pyqs_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Practice Quiz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        HorizontalDivider(color = BorderSubtle)

        // PYQ List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(pyqs) { question ->
                PYQCardItem(
                    question = question,
                    onViewInNCERT = { onViewInNCERT(question.sourceChapterId, question.sourcePageNumber) }
                )
            }
        }
    }
}

@Composable
private fun PYQCardItem(
    question: Question,
    onViewInNCERT: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Exam, Code, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VerificationBadge(
                    sourceType = SourceType.VERIFIED_PYQ,
                    customText = "${question.pyqExam ?: "NEET"} · ${question.pyqPaperCode ?: "Official"}"
                )
                Text(
                    text = "Q.${question.pyqQuestionNumber ?: 1}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Text
            Text(
                text = question.questionText,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Options Preview
            question.options.forEachIndexed { idx, opt ->
                val letter = when (idx) {
                    0 -> "A"
                    1 -> "B"
                    2 -> "C"
                    else -> "D"
                }
                val isCorrect = idx == question.correctAnswerIndex

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "($letter) ",
                        fontSize = 13.sp,
                        fontWeight = if (isExpanded && isCorrect) FontWeight.Bold else FontWeight.Normal,
                        color = if (isExpanded && isCorrect) SuccessGreen else SecondaryText
                    )
                    Text(
                        text = opt.text,
                        fontSize = 13.sp,
                        fontWeight = if (isExpanded && isCorrect) FontWeight.Bold else FontWeight.Normal,
                        color = if (isExpanded && isCorrect) SuccessGreen else PrimaryText,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expand Explanation or NCERT Source Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Hide Solution" else "Show Official Solution",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Button(
                    onClick = onViewInNCERT,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VeryLightBlue,
                        contentColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Page ${question.sourcePageNumber}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Expanded Explanation
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "OFFICIAL EXPLANATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = question.explanation,
                        fontSize = 12.5.sp,
                        color = PrimaryText,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Source: NCERT ${question.sourceChapterName}, Page ${question.sourcePageNumber}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SecondaryText
                    )
                }
            }
        }
    }
}
