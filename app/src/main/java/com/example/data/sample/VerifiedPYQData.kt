package com.example.data.sample

import com.example.data.model.Question

/** Complete extracted NEET Biology paper sets from verified source PDFs and answer keys, 2018–2025. */
object VerifiedPYQData {
    /** Split by year to keep each generated JVM class initializer below the bytecode limit. */
    val questions: List<Question> = listOf(
        VerifiedPYQ2018.questions,
        VerifiedPYQ2019.questions,
        VerifiedPYQ2020.questions,
        VerifiedPYQ2021.questions,
        VerifiedPYQ2022.questions,
        VerifiedPYQ2023.questions,
        VerifiedPYQ2024.questions,
        VerifiedPYQ2025.questions
    ).flatten()
}
