package com.example.data.repository

import com.example.data.model.Question
import com.example.data.model.QuestionQualityResult
import com.example.data.model.QuestionType

object QuestionQualityEngine {

    fun validateQuestion(
        question: Question,
        existingQuestions: List<Question> = emptyList()
    ): QuestionQualityResult {
        val reasons = mutableListOf<String>()

        // 1. Validate Question Text
        if (question.questionText.isBlank()) {
            reasons.add("Question text is empty")
        } else if (question.questionText.trim().length < 15) {
            reasons.add("Question text is too short or ambiguous")
        }

        // 2. Validate Options
        if (question.options.size != 4) {
            reasons.add("Must have exactly 4 options, found ${question.options.size}")
        }
        val optionTexts = question.options.map { it.text.trim() }
        if (optionTexts.any { it.isBlank() }) {
            reasons.add("One or more options are blank")
        }
        if (optionTexts.toSet().size != optionTexts.size) {
            reasons.add("Duplicate options detected")
        }

        // 3. Validate Correct Answer
        if (question.correctAnswerIndex !in 0..3) {
            reasons.add("Correct answer index ${question.correctAnswerIndex} is out of bounds (0-3)")
        }

        // 4. Validate Explanation
        if (question.explanation.isBlank() || question.explanation.length < 20) {
            reasons.add("Explanation is too short or missing detailed NCERT reasoning")
        }

        // 5. Special Assertion-Reason Validation
        var assertionCheck: String? = null
        var reasonCheck: String? = null
        var explanationCheck: String? = null

        if (question.questionType == QuestionType.ASSERTION_REASON || question.isAssertionReason) {
            val aText = question.assertionText ?: ""
            val rText = question.reasonText ?: ""
            if (aText.isBlank()) {
                reasons.add("Assertion-Reason question is missing assertion text")
                assertionCheck = "Missing assertion"
            } else {
                assertionCheck = "Verified Assertion statement (${aText.length} chars)"
            }

            if (rText.isBlank()) {
                reasons.add("Assertion-Reason question is missing reason text")
                reasonCheck = "Missing reason"
            } else {
                reasonCheck = "Verified Reason statement (${rText.length} chars)"
            }

            explanationCheck = if (question.explanation.contains("Assertion", ignoreCase = true) &&
                question.explanation.contains("Reason", ignoreCase = true)
            ) {
                "Valid causal relationship explanation"
            } else {
                "Explanation must specifically address both Assertion and Reason"
            }
        }

        // 6. Duplicate Detection
        val isDuplicate = existingQuestions.any { existing ->
            calculateWordOverlap(question.questionText, existing.questionText) > 0.85
        }
        if (isDuplicate) {
            reasons.add("Question is a duplicate or highly similar to an existing question")
        }

        return QuestionQualityResult(
            isValid = reasons.isEmpty(),
            reasons = reasons,
            assertionCheck = assertionCheck,
            reasonCheck = reasonCheck,
            explanationCheck = explanationCheck
        )
    }

    private fun calculateWordOverlap(text1: String, text2: String): Double {
        val words1 = text1.lowercase().split("\\s+".toRegex()).filter { it.length > 3 }.toSet()
        val words2 = text2.lowercase().split("\\s+".toRegex()).filter { it.length > 3 }.toSet()
        if (words1.isEmpty() || words2.isEmpty()) return 0.0
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size
        return intersection.toDouble() / union.toDouble()
    }
}
