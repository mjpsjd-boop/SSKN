package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import com.example.data.sample.NCERTSampleData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SSKNRepository(
    private val bookmarkDao: BookmarkDao,
    private val mistakeDao: MistakeDao,
    private val userProgressDao: UserProgressDao,
    private val aiAnalysisDao: AIAnalysisDao
) {
    // NCERT Chapters & Pages
    fun getChapters(classLevel: Int? = null): List<ChapterInfo> {
        return if (classLevel != null) {
            NCERTSampleData.chapters.filter { it.classLevel == classLevel }
        } else {
            NCERTSampleData.chapters
        }
    }

    fun getChapter(chapterId: String): ChapterInfo? {
        return NCERTSampleData.chapters.find { it.id == chapterId }
    }

    fun getPagesForChapter(chapterId: String): List<NCERTPageData> {
        return NCERTSampleData.pages.filter { it.chapterId == chapterId }
    }

    fun getPage(pageId: String): NCERTPageData? {
        return NCERTSampleData.pages.find { it.id == pageId }
    }

    fun getPageByNumber(chapterId: String, pageNumber: Int): NCERTPageData? {
        return NCERTSampleData.pages.find { it.chapterId == chapterId && it.pageNumber == pageNumber }
            ?: NCERTSampleData.pages.find { it.pageNumber == pageNumber }
    }

    // NEET Lens Analysis (Local Cache + Sample Fallback)
    suspend fun getPageAnalysis(pageId: String): NEETLensAnalysis? {
        // Check Room cache first
        val cached = aiAnalysisDao.getCachedAnalysis(pageId)
        if (cached != null) {
            return NEETLensAnalysis(
                pageId = cached.pageId,
                mustRemember = cached.mustRememberJson.split(";;;").filter { it.isNotBlank() },
                neetFocus = cached.neetFocus,
                commonConfusions = cached.commonConfusionsJson.split(";;;").filter { it.isNotBlank() },
                keyNCERTFacts = cached.keyNCERTFactsJson.split(";;;").filter { it.isNotBlank() },
                questionPotential = cached.questionPotential,
                diagramFocus = cached.diagramFocus
            )
        }
        // Fallback to precomputed sample analyses
        return NCERTSampleData.precomputedAnalyses[pageId]
    }

    suspend fun savePageAnalysis(analysis: NEETLensAnalysis) {
        val entity = AIAnalysisCacheEntity(
            pageId = analysis.pageId,
            mustRememberJson = analysis.mustRemember.joinToString(";;;"),
            neetFocus = analysis.neetFocus,
            commonConfusionsJson = analysis.commonConfusions.joinToString(";;;"),
            keyNCERTFactsJson = analysis.keyNCERTFacts.joinToString(";;;"),
            questionPotential = analysis.questionPotential,
            diagramFocus = analysis.diagramFocus,
            timestamp = System.currentTimeMillis()
        )
        aiAnalysisDao.cacheAnalysis(entity)
    }

    // PYQs Vault
    fun getAllPYQs(): List<Question> = NCERTSampleData.verifiedPYQs

    fun getPYQsForChapter(chapterId: String): List<Question> {
        return NCERTSampleData.verifiedPYQs.filter { it.sourceChapterId == chapterId }
    }

    fun getPYQsForPage(pageNumber: Int): List<Question> {
        return NCERTSampleData.verifiedPYQs.filter { it.sourcePageNumber == pageNumber }
    }

    fun filterPYQs(
        year: Int? = null,
        chapterId: String? = null,
        questionType: QuestionType? = null,
        exam: String? = null
    ): List<Question> {
        return NCERTSampleData.verifiedPYQs.filter { pyq ->
            val matchYear = year == null || pyq.pyqYear == year
            val matchChapter = chapterId == null || pyq.sourceChapterId == chapterId
            val matchType = questionType == null || pyq.questionType == questionType
            val matchExam = exam == null || pyq.pyqExam?.contains(exam, ignoreCase = true) == true
            matchYear && matchChapter && matchType && matchExam
        }
    }

    // Question Practice Sets
    fun getPracticeQuestionsForPage(
        pageNumber: Int,
        count: Int = 10,
        mode: PracticeMode = PracticeMode.NCERT_STRICT,
        filterType: QuestionType? = null
    ): List<Question> {
        val directPYQs = NCERTSampleData.verifiedPYQs.filter { it.sourcePageNumber == pageNumber }
        val generatedFromPoints = generateSyntheticQuestionsFromPage(pageNumber)

        val combined = (directPYQs + generatedFromPoints).distinctBy { it.questionText }
        val filtered = if (filterType != null) {
            combined.filter { it.questionType == filterType }
        } else {
            combined
        }
        return filtered.take(count)
    }

    fun getPracticeQuestionsForChapter(
        chapterId: String,
        count: Int = 15
    ): List<Question> {
        val pyqs = NCERTSampleData.verifiedPYQs.filter { it.sourceChapterId == chapterId }
        val pages = NCERTSampleData.pages.filter { it.chapterId == chapterId }
        val allPointsQuestions = pages.flatMap { generateSyntheticQuestionsFromPage(it.pageNumber) }
        return (pyqs + allPointsQuestions).distinctBy { it.questionText }.take(count)
    }

    private fun generateSyntheticQuestionsFromPage(pageNumber: Int): List<Question> {
        val page = NCERTSampleData.pages.find { it.pageNumber == pageNumber } ?: return emptyList()
        val questions = mutableListOf<Question>()

        page.keyPoints.forEachIndexed { index, kp ->
            val qId = "q_gen_${page.id}_$index"
            val question = Question(
                id = qId,
                questionText = "Consider the following statement from NCERT Page ${page.pageNumber}:\n\"${kp.text}\"\nWhich inference is biologically valid for NEET?",
                options = listOf(
                    QuestionOption("A", "This represents an authentic NCERT principle: ${kp.reason}", null),
                    QuestionOption("B", "This principle applies exclusively to non-cellular entities.", "Incorrect: Cell principles apply strictly to cellular life."),
                    QuestionOption("C", "This statement was invalidated in the revised 2024 NEET syllabus.", "Incorrect: This remains a core high-yield syllabus item."),
                    QuestionOption("D", "This was proposed by Hooke in 1665 without experimental proof.", "Incorrect historical attribution.")
                ),
                correctAnswerIndex = 0,
                explanation = "Direct reference from NCERT ${page.bookTitle}, Chapter: ${page.chapterName}, Page ${page.pageNumber}. Key point: ${kp.text}",
                whyOptionsAreWrong = listOf(
                    "Option A is correct according to NCERT.",
                    "Option B is incorrect as viruses and non-cellular particles are exceptions.",
                    "Option C is false as this concept is standard high-yield NEET material.",
                    "Option D is false as modern formulation is credited to ${kp.relatedConcepts.firstOrNull() ?: "NCERT"}."
                ),
                sourceChapterId = page.chapterId,
                sourceChapterName = page.chapterName,
                sourcePageNumber = page.pageNumber,
                sourcePoint = kp.text,
                questionType = if (index % 2 == 0) QuestionType.SINGLE_CORRECT else QuestionType.STATEMENT_BASED,
                difficulty = if (kp.importance == ImportanceRanking.CRITICAL) Difficulty.NEET_LEVEL else Difficulty.MODERATE,
                mode = PracticeMode.NCERT_STRICT,
                sourceType = SourceType.AI_GENERATED
            )
            questions.add(question)
        }
        return questions
    }

    // Global Search
    fun search(query: String): List<SearchResultItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()

        val results = mutableListOf<SearchResultItem>()

        // Search Pages
        NCERTSampleData.pages.forEach { page ->
            if (page.content.lowercase().contains(q) ||
                page.sectionTitle.lowercase().contains(q) ||
                page.chapterName.lowercase().contains(q)
            ) {
                val previewSnippet = extractSnippet(page.content, q)
                results.add(
                    SearchResultItem(
                        id = "search_p_${page.id}",
                        type = SearchResultType.NCERT_PAGE,
                        title = "${page.chapterName} · Page ${page.pageNumber}",
                        subtitle = page.sectionTitle,
                        snippet = previewSnippet,
                        targetPage = page.pageNumber,
                        chapterId = page.chapterId
                    )
                )
            }
        }

        // Search Key Points
        NCERTSampleData.pages.forEach { page ->
            page.keyPoints.forEach { kp ->
                if (kp.text.lowercase().contains(q) || kp.relatedConcepts.any { it.lowercase().contains(q) }) {
                    results.add(
                        SearchResultItem(
                            id = "search_kp_${kp.id}",
                            type = SearchResultType.CONCEPT,
                            title = "High-Yield Point (Page ${page.pageNumber})",
                            subtitle = page.chapterName,
                            snippet = kp.text,
                            targetPage = page.pageNumber,
                            chapterId = page.chapterId
                        )
                    )
                }
            }
        }

        // Search PYQs
        NCERTSampleData.verifiedPYQs.forEach { pyq ->
            if (pyq.questionText.lowercase().contains(q) ||
                pyq.explanation.lowercase().contains(q) ||
                (pyq.pyqExam?.lowercase()?.contains(q) == true)
            ) {
                results.add(
                    SearchResultItem(
                        id = "search_pyq_${pyq.id}",
                        type = SearchResultType.PYQ,
                        title = "${pyq.pyqExam ?: "NEET PYQ"} · Page ${pyq.sourcePageNumber}",
                        subtitle = pyq.sourceChapterName,
                        snippet = pyq.questionText.take(120) + "...",
                        targetPage = pyq.sourcePageNumber,
                        chapterId = pyq.sourceChapterId
                    )
                )
            }
        }

        return results
    }

    private fun extractSnippet(text: String, query: String): String {
        val index = text.lowercase().indexOf(query)
        if (index == -1) return text.take(100) + "..."
        val start = (index - 40).coerceAtLeast(0)
        val end = (index + query.length + 60).coerceAtMost(text.length)
        return (if (start > 0) "..." else "") + text.substring(start, end).replace("\n", " ") + (if (end < text.length) "..." else "")
    }

    // Bookmarks
    fun getBookmarks(): Flow<List<BookmarkItem>> {
        return bookmarkDao.getAllBookmarks().map { list ->
            list.map {
                BookmarkItem(
                    id = it.id,
                    type = it.type,
                    title = it.title,
                    subtitle = it.subtitle,
                    targetPage = it.targetPage,
                    chapterId = it.chapterId,
                    timestamp = it.timestamp
                )
            }
        }
    }

    suspend fun isBookmarked(id: String): Boolean = bookmarkDao.isBookmarked(id)

    suspend fun toggleBookmark(item: BookmarkItem) {
        if (bookmarkDao.isBookmarked(item.id)) {
            bookmarkDao.deleteBookmark(item.id)
        } else {
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    id = item.id,
                    type = item.type,
                    title = item.title,
                    subtitle = item.subtitle,
                    targetPage = item.targetPage,
                    chapterId = item.chapterId,
                    timestamp = item.timestamp
                )
            )
        }
    }

    // Mistakes & "Learn From This"
    fun getMistakes(): Flow<List<MistakeRecord>> {
        return mistakeDao.getAllMistakes().map { list ->
            list.map {
                MistakeRecord(
                    id = it.id,
                    questionId = it.questionId,
                    questionText = it.questionText,
                    selectedOptionIndex = it.selectedOptionIndex,
                    selectedOptionText = it.selectedOptionText,
                    correctOptionIndex = it.correctOptionIndex,
                    correctOptionText = it.correctOptionText,
                    chapterName = it.chapterName,
                    pageNumber = it.pageNumber,
                    misconceptionExplanation = it.misconceptionExplanation,
                    correctConceptSummary = it.correctConceptSummary,
                    ncertConnection = it.ncertConnection,
                    timestamp = it.timestamp
                )
            }
        }
    }

    suspend fun recordMistake(mistake: MistakeRecord) {
        val entity = MistakeEntity(
            id = mistake.id,
            questionId = mistake.questionId,
            questionText = mistake.questionText,
            selectedOptionIndex = mistake.selectedOptionIndex,
            selectedOptionText = mistake.selectedOptionText,
            correctOptionIndex = mistake.correctOptionIndex,
            correctOptionText = mistake.correctOptionText,
            chapterName = mistake.chapterName,
            pageNumber = mistake.pageNumber,
            misconceptionExplanation = mistake.misconceptionExplanation,
            correctConceptSummary = mistake.correctConceptSummary,
            ncertConnection = mistake.ncertConnection,
            timestamp = mistake.timestamp
        )
        mistakeDao.insertMistake(entity)
    }

    suspend fun deleteMistake(id: String) {
        mistakeDao.deleteMistake(id)
    }

    suspend fun clearAllMistakes() {
        mistakeDao.clearAllMistakes()
    }

    // User Progress
    fun getUserProgress(): Flow<UserProgress> {
        return userProgressDao.getProgress().map { entity ->
            if (entity == null) {
                UserProgress()
            } else {
                UserProgress(
                    lastClass = entity.lastClass,
                    lastChapterId = entity.lastChapterId,
                    lastChapterName = entity.lastChapterName,
                    lastPageNumber = entity.lastPageNumber,
                    readPages = entity.readPagesJson.split(",").filter { it.isNotBlank() }.toSet(),
                    practicedPages = entity.practicedPagesJson.split(",").filter { it.isNotBlank() }.toSet(),
                    completedQuestionsCount = entity.completedQuestionsCount,
                    correctQuestionsCount = entity.correctQuestionsCount
                )
            }
        }
    }

    suspend fun saveUserProgress(progress: UserProgress) {
        val entity = UserProgressEntity(
            id = 1,
            lastClass = progress.lastClass,
            lastChapterId = progress.lastChapterId,
            lastChapterName = progress.lastChapterName,
            lastPageNumber = progress.lastPageNumber,
            readPagesJson = progress.readPages.joinToString(","),
            practicedPagesJson = progress.practicedPages.joinToString(","),
            completedQuestionsCount = progress.completedQuestionsCount,
            correctQuestionsCount = progress.correctQuestionsCount
        )
        userProgressDao.saveProgress(entity)
    }

    // NEET Academic Guidance Repository
    private val guidanceItems = NCERTSampleData.neetGuidanceList.toMutableList()

    fun getAllGuidance(): List<NEETGuidanceItem> = guidanceItems.toList()

    fun getGuidanceByCategory(category: NEETGuidanceCategory): List<NEETGuidanceItem> {
        return guidanceItems.filter { it.category == category }
    }

    fun saveGuidanceItem(item: NEETGuidanceItem) {
        val idx = guidanceItems.indexOfFirst { it.id == item.id }
        if (idx >= 0) {
            guidanceItems[idx] = item
        } else {
            guidanceItems.add(0, item)
        }
    }

    // Problem Reports
    private val problemReports = NCERTSampleData.sampleProblemReports.toMutableList()

    fun getProblemReports(): List<ProblemReport> = problemReports.toList()

    fun submitProblemReport(report: ProblemReport) {
        problemReports.add(0, report)
    }

    fun updateProblemReportStatus(reportId: String, status: String) {
        val idx = problemReports.indexOfFirst { it.id == reportId }
        if (idx >= 0) {
            problemReports[idx] = problemReports[idx].copy(status = status)
        }
    }

    // AI Review Queue
    private val aiReviewItems = NCERTSampleData.sampleAIReviewQueue.toMutableList()

    fun getAIReviewQueue(): List<AIReviewItem> = aiReviewItems.toList()

    fun updateAIReviewStatus(id: String, status: AIContentStatus, notes: String? = null) {
        val idx = aiReviewItems.indexOfFirst { it.id == id }
        if (idx >= 0) {
            aiReviewItems[idx] = aiReviewItems[idx].copy(status = status, reviewerNotes = notes ?: aiReviewItems[idx].reviewerNotes)
        }
    }

    fun addAIReviewItem(question: Question) {
        aiReviewItems.add(
            0,
            AIReviewItem(
                id = "rev_${System.currentTimeMillis()}",
                question = question,
                status = AIContentStatus.GENERATED
            )
        )
    }

    // Page Mastery State (Informational only: NOT STARTED, READ, PRACTICED, REVISITED)
    fun getPageMasteryState(pageId: String, readPages: Set<String>, practicedPages: Set<String>): String {
        val isPracticed = practicedPages.contains(pageId)
        val isRead = readPages.contains(pageId)
        return when {
            isPracticed && isRead -> "REVISITED"
            isPracticed -> "PRACTICED"
            isRead -> "READ"
            else -> "NOT STARTED"
        }
    }
}

enum class SearchResultType {
    NCERT_PAGE,
    CONCEPT,
    PYQ,
    MCQ
}

data class SearchResultItem(
    val id: String,
    val type: SearchResultType,
    val title: String,
    val subtitle: String,
    val snippet: String,
    val targetPage: Int,
    val chapterId: String
)
