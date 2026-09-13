package com.example.data.model

data class NCERTPageData(
    val id: String,
    val classLevel: Int, // 11 or 12
    val bookTitle: String,
    val chapterId: String,
    val chapterNumber: Int,
    val chapterName: String,
    val sectionId: String,
    val sectionTitle: String,
    val pageNumber: Int,
    val totalPagesInChapter: Int,
    val content: String,
    val keyPoints: List<ImportantPoint>,
    val diagramTitle: String? = null,
    val diagramDescription: String? = null,
    val diagramCaption: String? = null,
    val isHighYield: Boolean = false,
    val verifiedPYQCount: Int = 0,
    val contentVersion: String = "2024-25 Authorized Edition"
)

data class ImportantPoint(
    val id: String,
    val text: String,
    val importance: ImportanceRanking, // CRITICAL, HIGH, MEDIUM
    val reason: String,
    val questionType: String,
    val relatedConcepts: List<String> = emptyList(),
    val sourceLine: String? = null
)

enum class ImportanceRanking {
    CRITICAL,
    HIGH,
    MEDIUM
}

data class NEETLensAnalysis(
    val pageId: String,
    val mustRemember: List<String>,
    val neetFocus: String,
    val commonConfusions: List<String>,
    val keyNCERTFacts: List<String>,
    val questionPotential: String,
    val diagramFocus: String? = null,
    val highlights: List<ImportantPoint> = emptyList(),
    val keyTerms: List<String> = emptyList(),
    val definitions: List<String> = emptyList()
)

data class LearnFromThisResult(
    val whatYouMisunderstood: String,
    val correctConcept: String,
    val ncertConnection: String,
    val whyYourAnswerWasWrong: String,
    val whatToRemember: String
)

enum class PageStatus {
    NOT_STARTED,
    READ,
    PRACTICED,
    REVISITED
}

data class QuestionOption(
    val id: String,
    val text: String,
    val whyWrong: String? = null
)

enum class QuestionType {
    SINGLE_CORRECT,
    ASSERTION_REASON,
    STATEMENT_BASED,
    MULTIPLE_STATEMENT,
    MATCH_FOLLOWING,
    TRUE_FALSE,
    SEQUENCE,
    PROCESS,
    CONCEPTUAL
}

enum class Difficulty {
    EASY,
    MODERATE,
    NEET_LEVEL,
    CHALLENGING
}

enum class PracticeMode {
    NCERT_STRICT,
    NEET_STYLE
}

enum class SourceType {
    VERIFIED_PYQ,
    AI_GENERATED,
    DEMO_CONTENT
}

data class Question(
    val id: String,
    val questionText: String,
    val options: List<QuestionOption>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val whyOptionsAreWrong: List<String>,
    val sourceChapterId: String,
    val sourceChapterName: String,
    val sourcePageNumber: Int,
    val sourcePoint: String? = null,
    val questionType: QuestionType = QuestionType.SINGLE_CORRECT,
    val difficulty: Difficulty = Difficulty.NEET_LEVEL,
    val mode: PracticeMode = PracticeMode.NCERT_STRICT,
    val sourceType: SourceType = SourceType.AI_GENERATED,
    val pyqYear: Int? = null,
    val pyqExam: String? = null, // e.g. "NEET 2024", "NEET 2023"
    val pyqPaperCode: String? = null,
    val pyqQuestionNumber: Int? = null,
    val verificationStatus: String = "VERIFIED PYQ",
    val isAssertionReason: Boolean = false,
    val assertionText: String? = null,
    val reasonText: String? = null,
    val statement1: String? = null,
    val statement2: String? = null,
    val neetTip: String? = null
)

data class ChapterInfo(
    val id: String,
    val classLevel: Int,
    val number: Int,
    val name: String,
    val startPage: Int,
    val endPage: Int,
    val totalPages: Int,
    val isHighYield: Boolean,
    val verifiedPYQCount: Int,
    val description: String,
    val sourceUrl: String = "",
    val contentAvailability: String = "VERIFIED_LOCAL"
)

data class MistakeRecord(
    val id: String,
    val questionId: String,
    val questionText: String,
    val selectedOptionIndex: Int,
    val selectedOptionText: String,
    val correctOptionIndex: Int,
    val correctOptionText: String,
    val chapterName: String,
    val pageNumber: Int,
    val misconceptionExplanation: String,
    val correctConceptSummary: String,
    val ncertConnection: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class BookmarkItem(
    val id: String,
    val type: String, // "NCERT", "PYQ", "MCQ"
    val title: String,
    val subtitle: String,
    val targetPage: Int,
    val chapterId: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserProgress(
    val lastClass: Int = 11,
    val lastChapterId: String = "ch_cell_unit",
    val lastChapterName: String = "Cell: The Unit of Life",
    val lastPageNumber: Int = 125,
    val readPages: Set<String> = emptySet(),
    val practicedPages: Set<String> = emptySet(),
    val completedQuestionsCount: Int = 0,
    val correctQuestionsCount: Int = 0
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val sourceContext: String? = null // e.g. "NCERT BASED", "AI EXPLANATION"
)

enum class NEETGuidanceCategory(val displayName: String) {
    READ_NCERT_FOR_NEET("Read NCERT for NEET"),
    WHAT_TO_MEMORIZE("What to Memorize"),
    WHAT_TO_UNDERSTAND("What to Understand"),
    NCERT_DIAGRAMS("NCERT Diagrams"),
    TABLES_AND_EXAMPLES("Tables & Examples"),
    COMMON_NCERT_TRAPS("Common NCERT Traps"),
    STATEMENT_BASED_QUESTIONS("Statement-Based Questions"),
    ASSERTION_REASON_QUESTIONS("Assertion-Reason Questions"),
    HOW_TO_USE_PYQS("How to Use PYQs"),
    HOW_TO_REVISE_A_PAGE("How to Revise a Page"),
    HOW_TO_USE_SSKN("How to Use SSKN")
}

data class NEETGuidanceItem(
    val id: String,
    val title: String,
    val category: NEETGuidanceCategory,
    val shortExplanation: String,
    val keyPoints: List<String>,
    val relatedNCERTConcepts: List<String> = emptyList(),
    val relatedChapters: List<String> = emptyList(),
    val isPublished: Boolean = true
)

data class PageSummary(
    val pageNumber: Int,
    val chapterName: String,
    val coreIdea: String,
    val mustRememberFacts: List<String>,
    val keyTerminology: List<String>,
    val trapOrConfusion: String
)

enum class ProblemType(val displayName: String) {
    INCORRECT_ANSWER("Incorrect Answer"),
    AMBIGUOUS_QUESTION("Ambiguous Question"),
    INCORRECT_NCERT_MAPPING("Incorrect NCERT Mapping"),
    BROKEN_PAGE("Broken Page"),
    INCORRECT_HIGHLIGHT("Incorrect Highlight"),
    AI_HALLUCINATION("AI Hallucination"),
    PYQ_VERIFICATION_ISSUE("PYQ Verification Issue")
}

data class ProblemReport(
    val id: String,
    val problemType: ProblemType,
    val description: String,
    val targetContent: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "UNDER_REVIEW" // UNDER_REVIEW, RESOLVED
)

enum class AIContentStatus {
    GENERATED,
    VALIDATED,
    REVIEWED,
    PUBLISHED,
    REJECTED
}

data class AIReviewItem(
    val id: String,
    val question: Question,
    val status: AIContentStatus,
    val reviewerNotes: String? = null
)

data class QuestionQualityResult(
    val isValid: Boolean,
    val reasons: List<String> = emptyList(),
    val assertionCheck: String? = null,
    val reasonCheck: String? = null,
    val explanationCheck: String? = null
)

data class ConceptComparison(
    val concept1: String,
    val concept2: String,
    val definitionDifference: String,
    val processDifferences: List<String>,
    val keyDistinctions: List<String>,
    val ncertReference: String,
    val neetExamTip: String
)

data class DiagramAnalysisResult(
    val title: String,
    val representation: String,
    val keyStructures: List<String>,
    val processOrSequence: String,
    val potentialQuestions: List<String>,
    val verificationStatus: String = "NCERT DIAGRAM GROUNDED"
)

data class StructuredAIScanResult(
    val pageNumber: Int,
    val chapterName: String,
    val neetLens: NEETLensAnalysis,
    val highlights: List<ImportantPoint>,
    val summary: PageSummary,
    val pyqCount: Int
)

data class AIGenerationHistoryItem(
    val id: String,
    val type: String, // "SCAN", "QUESTION", "EXPLANATION", "COMPARISON"
    val title: String,
    val snippet: String,
    val timestamp: Long = System.currentTimeMillis()
)

