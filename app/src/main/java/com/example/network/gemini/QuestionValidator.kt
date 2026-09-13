package com.example.network.gemini

import com.example.data.model.Question
import com.example.data.model.QuestionType

object QuestionValidator {

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    /**
     * Validates every AI-generated question against strict NCERT grounding & exam standards:
     * - Exactly one correct option
     * - Minimum 4 options
     * - Non-empty, distinct options
     * - Adequate explanation referencing NCERT
     * - Assertion-Reason integrity
     * - Grounding check against source page content
     */
    fun validate(
        question: Question,
        pageContent: String,
        existingQuestions: List<Question> = emptyList()
    ): ValidationResult {
        // 1. Check options count
        if (question.options.size < 4) {
            return ValidationResult.Invalid("Question must have at least 4 options. Found ${question.options.size}")
        }

        // 2. Check correct answer index boundary
        if (question.correctAnswerIndex !in question.options.indices) {
            return ValidationResult.Invalid("Correct answer index ${question.correctAnswerIndex} is out of bounds for options size ${question.options.size}")
        }

        // 3. Check for distinct option texts
        val cleanedOptions = question.options.map { it.text.trim().lowercase() }
        if (cleanedOptions.distinct().size != cleanedOptions.size) {
            return ValidationResult.Invalid("Question has duplicate or identical options.")
        }

        // 4. Check for blank question text or options
        if (question.questionText.isBlank() || question.questionText.length < 15) {
            return ValidationResult.Invalid("Question text is too brief or empty.")
        }
        if (question.options.any { it.text.isBlank() }) {
            return ValidationResult.Invalid("One or more options are blank.")
        }

        // 5. Check explanation depth
        if (question.explanation.isBlank() || question.explanation.length < 15) {
            return ValidationResult.Invalid("Explanation is too short or missing NCERT grounding.")
        }

        // 6. Duplicate detection against current pool
        if (existingQuestions.any { it.questionText.trim().equals(question.questionText.trim(), ignoreCase = true) }) {
            return ValidationResult.Invalid("Duplicate question detected in the current session.")
        }

        // 7. Assertion-Reason specific validation
        if (question.questionType == QuestionType.ASSERTION_REASON) {
            val hasAssertion = !question.assertionText.isNullOrBlank() || question.questionText.contains("Assertion", ignoreCase = true)
            val hasReason = !question.reasonText.isNullOrBlank() || question.questionText.contains("Reason", ignoreCase = true)
            if (!hasAssertion || !hasReason) {
                return ValidationResult.Invalid("Assertion-Reason question must contain distinct Assertion (A) and Reason (R) statements.")
            }
        }

        // 8. Grounding check against provided NCERT page text
        if (pageContent.isNotBlank()) {
            val terms = question.questionText.split(Regex("[^a-zA-Z0-9]"))
                .filter { it.length > 5 && !it.equals("statement", ignoreCase = true) && !it.equals("following", ignoreCase = true) }
            val matchCount = terms.count { pageContent.contains(it, ignoreCase = true) }
            if (terms.isNotEmpty() && matchCount == 0) {
                // Warning/rejection: terms completely absent from page text
                return ValidationResult.Invalid("Question concepts do not match the authorized text of this NCERT page.")
            }
        }

        return ValidationResult.Valid
    }
}
