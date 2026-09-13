package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SSKNDatabase
import com.example.data.model.*
import com.example.data.repository.SSKNRepository
import com.example.data.repository.SearchResultItem
import com.example.network.gemini.GeminiService
import com.example.ui.components.AppNavDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class ScreenDestination {
    object Splash : ScreenDestination()
    object Main : ScreenDestination()
    data class ChapterDetail(val chapterId: String) : ScreenDestination()
    data class PdfLesson(val chapterId: String) : ScreenDestination()
    data class NCERTReader(val chapterId: String, val pageNumber: Int) : ScreenDestination()
    data class Practice(
        val sourceTitle: String,
        val questions: List<Question>,
        val returnDestination: ScreenDestination? = null
    ) : ScreenDestination()
    data class MistakeDetail(val mistake: MistakeRecord) : ScreenDestination()
    data class AIMCQInteraction(val chapterId: String? = null, val pageNumber: Int? = null) : ScreenDestination()
    object AdminPortal : ScreenDestination()
    object Bookmarks : ScreenDestination()
    object MistakesList : ScreenDestination()
    object NEETGuidance : ScreenDestination()
    object GlobalSearch : ScreenDestination()
    data class ReportProblem(val targetContent: String = "") : ScreenDestination()
}

data class SSKNUiState(
    val currentScreen: ScreenDestination = ScreenDestination.Splash,
    val currentNavTab: AppNavDestination = AppNavDestination.HOME,
    val selectedClass: Int = 11, // 11 or 12
    val activeChapter: ChapterInfo? = null,
    val activePage: NCERTPageData? = null,
    val userProgress: UserProgress = UserProgress(),
    val bookmarks: List<BookmarkItem> = emptyList(),
    val mistakes: List<MistakeRecord> = emptyList(),
    val activeNEETLens: NEETLensAnalysis? = null,
    val isAnalyzingLens: Boolean = false,
    val chatMessages: List<ChatMessage> = emptyList(),
    val isChatLoading: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<SearchResultItem> = emptyList(),
    // Practice state
    val practiceQuestions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val practiceScore: Int = 0,
    val practiceCompleted: Boolean = false,
    // AI MCQ Interaction State
    val currentAIMCQ: Question? = null,
    val isGeneratingAIMCQ: Boolean = false,
    val aiMCQSelectedOption: Int? = null,
    val isAIMCQAnswered: Boolean = false,
    val aiMCQScore: Int = 0,
    val aiMCQAttempted: Int = 0,
    val aiMCQCorrect: Int = 0,
    val aiMCQPreferredType: QuestionType? = null,
    val aiMCQDifficulty: Difficulty = Difficulty.NEET_LEVEL,
    val scanningStage: String? = null,
    val activeMistakeDiagnosis: LearnFromThisResult? = null,
    val isDiagnosingMistake: Boolean = false,
    val activeDiagnosedMistakeId: String? = null,
    // PYQ Filters
    val selectedPYQYear: Int? = null,
    val selectedPYQChapterId: String? = null,
    val selectedPYQType: QuestionType? = null,
    // NEET Guidance & Academic State
    val guidanceItems: List<NEETGuidanceItem> = emptyList(),
    val selectedGuidanceCategory: NEETGuidanceCategory? = null,
    val active30SecondSummary: PageSummary? = null,
    val isGeneratingSummary: Boolean = false,
    val problemReports: List<ProblemReport> = emptyList(),
    val aiReviewQueue: List<AIReviewItem> = emptyList(),
    val activeWhyImportantExplanation: String? = null,
    val isExplainingWhyImportant: Boolean = false,
    val aiMode: PracticeMode = PracticeMode.NCERT_STRICT,
    val activeConceptComparison: ConceptComparison? = null,
    val isComparingConcepts: Boolean = false,
    val activeDiagramAnalysis: DiagramAnalysisResult? = null,
    val isAnalyzingDiagram: Boolean = false,
    val aiGenerationHistory: List<AIGenerationHistoryItem> = emptyList(),
    val aiNaturalSearchResult: String? = null,
    val isAISearching: Boolean = false
)

class SSKNViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SSKNDatabase.getDatabase(application)
    private val repository = SSKNRepository(
        bookmarkDao = db.bookmarkDao(),
        mistakeDao = db.mistakeDao(),
        userProgressDao = db.userProgressDao(),
        aiAnalysisDao = db.aiAnalysisDao()
    )
    private val geminiService = GeminiService()

    private val _uiState = MutableStateFlow(SSKNUiState())
    val uiState: StateFlow<SSKNUiState> = _uiState.asStateFlow()

    init {
        // Observe User Progress
        viewModelScope.launch {
            repository.getUserProgress().collectLatest { progress ->
                _uiState.value = _uiState.value.copy(userProgress = progress)
            }
        }

        // Observe Bookmarks
        viewModelScope.launch {
            repository.getBookmarks().collectLatest { bookmarks ->
                _uiState.value = _uiState.value.copy(bookmarks = bookmarks)
            }
        }

        // Observe Mistakes
        viewModelScope.launch {
            repository.getMistakes().collectLatest { mistakes ->
                _uiState.value = _uiState.value.copy(mistakes = mistakes)
            }
        }

        // Initialize Guidance, Problem Reports, AI Review Queue
        _uiState.value = _uiState.value.copy(
            guidanceItems = repository.getAllGuidance(),
            problemReports = repository.getProblemReports(),
            aiReviewQueue = repository.getAIReviewQueue()
        )
    }

    fun dismissSplash() {
        _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.Main)
    }

    fun selectNavTab(tab: AppNavDestination) {
        _uiState.value = _uiState.value.copy(
            currentNavTab = tab,
            currentScreen = ScreenDestination.Main
        )
    }

    fun selectClass(classLevel: Int) {
        _uiState.value = _uiState.value.copy(selectedClass = classLevel)
    }

    fun getChapters(classLevel: Int? = null): List<ChapterInfo> {
        return repository.getChapters(classLevel ?: _uiState.value.selectedClass)
    }

    fun openChapter(chapterId: String) {
        val chapter = repository.getChapter(chapterId)
        _uiState.value = _uiState.value.copy(
            activeChapter = chapter,
            currentScreen = ScreenDestination.ChapterDetail(chapterId)
        )
    }

    fun openPdfLesson(chapterId: String) {
        _uiState.value = _uiState.value.copy(
            activeChapter = repository.getChapter(chapterId),
            currentScreen = ScreenDestination.PdfLesson(chapterId)
        )
    }

    fun openReader(chapterId: String, pageNumber: Int) {
        val page = repository.getPageByNumber(chapterId, pageNumber)
        val chapter = repository.getChapter(chapterId)
        _uiState.value = _uiState.value.copy(
            activeChapter = chapter,
            activePage = page,
            currentScreen = ScreenDestination.NCERTReader(chapterId, pageNumber),
            chatMessages = emptyList(), // reset chat for the new page
            activeNEETLens = null
        )

        // Mark as read in progress
        page?.let { p ->
            val updatedRead = _uiState.value.userProgress.readPages + p.id
            val updatedProgress = _uiState.value.userProgress.copy(
                lastClass = p.classLevel,
                lastChapterId = p.chapterId,
                lastChapterName = p.chapterName,
                lastPageNumber = p.pageNumber,
                readPages = updatedRead
            )
            viewModelScope.launch {
                repository.saveUserProgress(updatedProgress)
            }
            loadNEETLensForPage(p)
        }
    }

    fun navigateReaderPage(delta: Int) {
        val currentPage = _uiState.value.activePage ?: return
        val targetPageNumber = currentPage.pageNumber + delta
        val targetPage = repository.getPageByNumber(currentPage.chapterId, targetPageNumber)
        if (targetPage != null) {
            openReader(targetPage.chapterId, targetPage.pageNumber)
        }
    }

    fun loadNEETLensForPage(page: NCERTPageData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzingLens = true)
            val cached = repository.getPageAnalysis(page.id)
            if (cached != null) {
                _uiState.value = _uiState.value.copy(
                    activeNEETLens = cached,
                    isAnalyzingLens = false
                )
            } else {
                val live = geminiService.analyzePageWithNEETLens(
                    chapterName = page.chapterName,
                    pageNumber = page.pageNumber,
                    pageContent = page.content
                )
                repository.savePageAnalysis(live)
                _uiState.value = _uiState.value.copy(
                    activeNEETLens = live,
                    isAnalyzingLens = false
                )
            }
        }
    }

    fun scanCurrentPage() {
        val page = _uiState.value.activePage ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAnalyzingLens = true,
                scanningStage = "Analyzing your NCERT page..."
            )
            kotlinx.coroutines.delay(300)
            _uiState.value = _uiState.value.copy(
                scanningStage = "Finding important lines..."
            )
            kotlinx.coroutines.delay(300)
            _uiState.value = _uiState.value.copy(
                scanningStage = "Checking question potential..."
            )
            kotlinx.coroutines.delay(250)
            _uiState.value = _uiState.value.copy(
                scanningStage = "Preparing NEET Lens..."
            )

            val live = geminiService.analyzePageWithNEETLens(
                chapterName = page.chapterName,
                pageNumber = page.pageNumber,
                pageContent = page.content
            )
            repository.savePageAnalysis(live)

            _uiState.value = _uiState.value.copy(
                scanningStage = "Analysis ready",
                activeNEETLens = live,
                isAnalyzingLens = false
            )
        }
    }

    fun setAIMode(mode: PracticeMode) {
        _uiState.value = _uiState.value.copy(aiMode = mode)
    }

    fun toggleAIMode() {
        val nextMode = if (_uiState.value.aiMode == PracticeMode.NCERT_STRICT) {
            PracticeMode.NEET_STYLE
        } else {
            PracticeMode.NCERT_STRICT
        }
        _uiState.value = _uiState.value.copy(aiMode = nextMode)
    }

    fun askThisPage(query: String) {
        val page = _uiState.value.activePage ?: return
        if (query.isBlank()) return

        val userMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            isUser = true,
            text = query
        )
        val currentMsgs = _uiState.value.chatMessages + userMsg
        _uiState.value = _uiState.value.copy(chatMessages = currentMsgs, isChatLoading = true)

        viewModelScope.launch {
            val responseText = geminiService.askThisPage(
                chapterName = page.chapterName,
                pageNumber = page.pageNumber,
                pageContent = page.content,
                userQuery = query,
                history = currentMsgs,
                mode = _uiState.value.aiMode
            )
            val sourceTag = if (_uiState.value.aiMode == PracticeMode.NCERT_STRICT) "NCERT BASED" else "NEET FOCUS"
            val aiMsg = ChatMessage(
                id = "msg_ai_${System.currentTimeMillis()}",
                isUser = false,
                text = responseText,
                sourceContext = sourceTag
            )
            _uiState.value = _uiState.value.copy(
                chatMessages = _uiState.value.chatMessages + aiMsg,
                isChatLoading = false,
                aiGenerationHistory = listOf(
                    AIGenerationHistoryItem(
                        id = "hist_${System.currentTimeMillis()}",
                        type = "ASK_AI",
                        title = "Q: ${query.take(30)}...",
                        snippet = responseText.take(80) + "..."
                    )
                ) + _uiState.value.aiGenerationHistory
            )
        }
    }

    fun compareConcepts(c1: String, c2: String) {
        val page = _uiState.value.activePage ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isComparingConcepts = true, activeConceptComparison = null)
            val result = geminiService.compareConcepts(c1, c2, page.chapterName, page.pageNumber, page.content)
            _uiState.value = _uiState.value.copy(
                isComparingConcepts = false,
                activeConceptComparison = result,
                aiGenerationHistory = listOf(
                    AIGenerationHistoryItem(
                        id = "comp_${System.currentTimeMillis()}",
                        type = "COMPARISON",
                        title = "Compare: $c1 vs $c2",
                        snippet = result.definitionDifference
                    )
                ) + _uiState.value.aiGenerationHistory
            )
        }
    }

    fun clearConceptComparison() {
        _uiState.value = _uiState.value.copy(activeConceptComparison = null, isComparingConcepts = false)
    }

    fun analyzeDiagram() {
        val page = _uiState.value.activePage ?: return
        val diagramTitle = page.diagramTitle ?: "NCERT Diagram"
        val desc = page.diagramDescription ?: ""
        val cap = page.diagramCaption
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzingDiagram = true, activeDiagramAnalysis = null)
            val result = geminiService.analyzeDiagramFull(page.chapterName, page.pageNumber, diagramTitle, desc, cap)
            _uiState.value = _uiState.value.copy(
                isAnalyzingDiagram = false,
                activeDiagramAnalysis = result,
                aiGenerationHistory = listOf(
                    AIGenerationHistoryItem(
                        id = "diag_${System.currentTimeMillis()}",
                        type = "DIAGRAM",
                        title = "Diagram: $diagramTitle",
                        snippet = result.representation
                    )
                ) + _uiState.value.aiGenerationHistory
            )
        }
    }

    fun clearDiagramAnalysis() {
        _uiState.value = _uiState.value.copy(activeDiagramAnalysis = null, isAnalyzingDiagram = false)
    }

    fun searchBiologyWithAI(query: String) {
        if (query.isBlank()) return
        val chName = _uiState.value.activeChapter?.name ?: "Cell: The Unit of Life"
        val pgNum = _uiState.value.activePage?.pageNumber ?: 125
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAISearching = true, aiNaturalSearchResult = null)
            val result = geminiService.searchBiologyAI(query, chName, pgNum)
            _uiState.value = _uiState.value.copy(
                isAISearching = false,
                aiNaturalSearchResult = result
            )
        }
    }

    fun clearAISearch() {
        _uiState.value = _uiState.value.copy(aiNaturalSearchResult = null, isAISearching = false)
    }


    fun startPracticeForPage(
        pageNumber: Int,
        count: Int = 10,
        mode: PracticeMode = PracticeMode.NCERT_STRICT,
        questionType: QuestionType? = null
    ) {
        val questions = repository.getPracticeQuestionsForPage(pageNumber, count, mode, questionType)
        _uiState.value = _uiState.value.copy(
            practiceQuestions = questions,
            currentQuestionIndex = 0,
            selectedOptionIndex = null,
            isAnswerSubmitted = false,
            practiceScore = 0,
            practiceCompleted = false,
            currentScreen = ScreenDestination.Practice(
                sourceTitle = "NCERT Page $pageNumber Practice",
                questions = questions,
                returnDestination = _uiState.value.currentScreen
            )
        )
    }

    fun startPracticeForChapter(chapterId: String, count: Int = 15) {
        val chapter = repository.getChapter(chapterId)
        val questions = repository.getPracticeQuestionsForChapter(chapterId, count)
        _uiState.value = _uiState.value.copy(
            practiceQuestions = questions,
            currentQuestionIndex = 0,
            selectedOptionIndex = null,
            isAnswerSubmitted = false,
            practiceScore = 0,
            practiceCompleted = false,
            currentScreen = ScreenDestination.Practice(
                sourceTitle = "${chapter?.name ?: "Chapter"} Practice",
                questions = questions,
                returnDestination = _uiState.value.currentScreen
            )
        )
    }

    fun startPracticeForPYQs(questions: List<Question>, title: String) {
        _uiState.value = _uiState.value.copy(
            practiceQuestions = questions,
            currentQuestionIndex = 0,
            selectedOptionIndex = null,
            isAnswerSubmitted = false,
            practiceScore = 0,
            practiceCompleted = false,
            currentScreen = ScreenDestination.Practice(
                sourceTitle = title,
                questions = questions,
                returnDestination = _uiState.value.currentScreen
            )
        )
    }

    fun selectOption(optionIndex: Int) {
        if (_uiState.value.isAnswerSubmitted) return
        _uiState.value = _uiState.value.copy(selectedOptionIndex = optionIndex)
    }

    fun submitAnswer() {
        val selectedIndex = _uiState.value.selectedOptionIndex ?: return
        val currentQ = _uiState.value.practiceQuestions.getOrNull(_uiState.value.currentQuestionIndex) ?: return

        val isCorrect = selectedIndex == currentQ.correctAnswerIndex
        val newScore = if (isCorrect) _uiState.value.practiceScore + 1 else _uiState.value.practiceScore

        _uiState.value = _uiState.value.copy(
            isAnswerSubmitted = true,
            practiceScore = newScore
        )

        // If incorrect, log as a mistake
        if (!isCorrect) {
            val selectedOptionText = currentQ.options.getOrNull(selectedIndex)?.text ?: "Option $selectedIndex"
            val correctOptionText = currentQ.options.getOrNull(currentQ.correctAnswerIndex)?.text ?: "Option ${currentQ.correctAnswerIndex}"
            val misconception = currentQ.whyOptionsAreWrong.getOrNull(selectedIndex) ?: "Selected distractor contrary to NCERT principle."

            val mistake = MistakeRecord(
                id = "m_${currentQ.id}_${System.currentTimeMillis()}",
                questionId = currentQ.id,
                questionText = currentQ.questionText,
                selectedOptionIndex = selectedIndex,
                selectedOptionText = selectedOptionText,
                correctOptionIndex = currentQ.correctAnswerIndex,
                correctOptionText = correctOptionText,
                chapterName = currentQ.sourceChapterName,
                pageNumber = currentQ.sourcePageNumber,
                misconceptionExplanation = misconception,
                correctConceptSummary = currentQ.explanation,
                ncertConnection = "Class NCERT Biology, ${currentQ.sourceChapterName}, Page ${currentQ.sourcePageNumber}"
            )

            viewModelScope.launch {
                repository.recordMistake(mistake)
            }
        }

        // Update general question statistics
        viewModelScope.launch {
            val progress = _uiState.value.userProgress
            val updated = progress.copy(
                completedQuestionsCount = progress.completedQuestionsCount + 1,
                correctQuestionsCount = if (isCorrect) progress.correctQuestionsCount + 1 else progress.correctQuestionsCount
            )
            repository.saveUserProgress(updated)
        }
    }

    fun nextQuestion() {
        val nextIdx = _uiState.value.currentQuestionIndex + 1
        if (nextIdx < _uiState.value.practiceQuestions.size) {
            _uiState.value = _uiState.value.copy(
                currentQuestionIndex = nextIdx,
                selectedOptionIndex = null,
                isAnswerSubmitted = false
            )
        } else {
            _uiState.value = _uiState.value.copy(practiceCompleted = true)
        }
    }

    fun toggleBookmark(item: BookmarkItem) {
        viewModelScope.launch {
            repository.toggleBookmark(item)
        }
    }

    fun isBookmarked(id: String): Boolean {
        return _uiState.value.bookmarks.any { it.id == id }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            searchResults = repository.search(query)
        )
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList()
        )
    }

    fun setPYQFilters(year: Int?, chapterId: String?, type: QuestionType?) {
        _uiState.value = _uiState.value.copy(
            selectedPYQYear = year,
            selectedPYQChapterId = chapterId,
            selectedPYQType = type
        )
    }

    fun getFilteredPYQs(): List<Question> {
        return repository.filterPYQs(
            year = _uiState.value.selectedPYQYear,
            chapterId = _uiState.value.selectedPYQChapterId,
            questionType = _uiState.value.selectedPYQType
        )
    }

    fun navigateBack() {
        when (val current = _uiState.value.currentScreen) {
            is ScreenDestination.Splash -> {}
            is ScreenDestination.Main -> {}
            is ScreenDestination.ChapterDetail -> {
                _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.Main)
            }
            is ScreenDestination.PdfLesson -> {
                _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.ChapterDetail(current.chapterId))
            }
            is ScreenDestination.NCERTReader -> {
                val chapterId = current.chapterId
                _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.ChapterDetail(chapterId))
            }
            is ScreenDestination.Practice -> {
                val ret = current.returnDestination ?: ScreenDestination.Main
                _uiState.value = _uiState.value.copy(currentScreen = ret)
            }
            is ScreenDestination.MistakeDetail -> {
                _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.MistakesList)
            }
            is ScreenDestination.AdminPortal -> {
                _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.Main)
            }
            is ScreenDestination.Bookmarks -> {
                _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.Main)
            }
            is ScreenDestination.MistakesList -> {
                _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.Main)
            }
            is ScreenDestination.AIMCQInteraction -> {
                _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.Main)
            }
            is ScreenDestination.NEETGuidance -> {
                _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.Main)
            }
            is ScreenDestination.GlobalSearch -> {
                _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.Main)
            }
            is ScreenDestination.ReportProblem -> {
                _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.Main)
            }
        }
    }

    fun startAIMCQDrill(chapterId: String? = null, pageNumber: Int? = null) {
        val targetChapterId = chapterId ?: "c_cell_8"
        val targetPageNumber = pageNumber ?: 126
        _uiState.value = _uiState.value.copy(
            currentScreen = ScreenDestination.AIMCQInteraction(targetChapterId, targetPageNumber),
            aiMCQSelectedOption = null,
            isAIMCQAnswered = false
        )
        generateNextAIMCQ(targetChapterId, targetPageNumber)
    }

    fun generateNextAIMCQ(
        chapterId: String? = null,
        pageNumber: Int? = null,
        preferredType: QuestionType? = _uiState.value.aiMCQPreferredType,
        difficulty: Difficulty = _uiState.value.aiMCQDifficulty,
        sourcePoint: String? = null
    ) {
        val currentScreen = _uiState.value.currentScreen
        val targetChapterId = chapterId
            ?: if (currentScreen is ScreenDestination.AIMCQInteraction) currentScreen.chapterId ?: "c_cell_8"
            else "c_cell_8"
        val targetPageNumber = pageNumber
            ?: if (currentScreen is ScreenDestination.AIMCQInteraction) currentScreen.pageNumber ?: 126
            else 126

        val chapter = repository.getChapter(targetChapterId) ?: repository.getChapters().first()
        val page = repository.getPageByNumber(targetChapterId, targetPageNumber) ?: repository.getPagesForChapter(targetChapterId).first()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGeneratingAIMCQ = true,
                aiMCQSelectedOption = null,
                isAIMCQAnswered = false,
                aiMCQPreferredType = preferredType,
                aiMCQDifficulty = difficulty
            )

            try {
                val generatedQuestion = geminiService.generateAIMCQ(
                    chapterName = chapter.name,
                    chapterId = chapter.id,
                    pageNumber = page.pageNumber,
                    pageContent = page.content,
                    preferredType = preferredType,
                    difficulty = difficulty,
                    sourcePoint = sourcePoint
                )
                _uiState.value = _uiState.value.copy(
                    currentAIMCQ = generatedQuestion,
                    isGeneratingAIMCQ = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingAIMCQ = false
                )
            }
        }
    }

    fun startAIMCQDrillFromPoint(point: ImportantPoint) {
        val page = _uiState.value.activePage ?: return
        _uiState.value = _uiState.value.copy(
            currentScreen = ScreenDestination.AIMCQInteraction(page.chapterId, page.pageNumber),
            aiMCQSelectedOption = null,
            isAIMCQAnswered = false
        )
        generateNextAIMCQ(
            chapterId = page.chapterId,
            pageNumber = page.pageNumber,
            sourcePoint = point.text
        )
    }

    fun answerAIMCQ(selectedOption: Int) {
        val currentQ = _uiState.value.currentAIMCQ ?: return
        if (_uiState.value.isAIMCQAnswered) return

        val isCorrect = selectedOption == currentQ.correctAnswerIndex
        val newAttempted = _uiState.value.aiMCQAttempted + 1
        val newCorrect = if (isCorrect) _uiState.value.aiMCQCorrect + 1 else _uiState.value.aiMCQCorrect
        val newScore = _uiState.value.aiMCQScore + if (isCorrect) 4 else -1

        _uiState.value = _uiState.value.copy(
            aiMCQSelectedOption = selectedOption,
            isAIMCQAnswered = true,
            aiMCQAttempted = newAttempted,
            aiMCQCorrect = newCorrect,
            aiMCQScore = newScore
        )

        viewModelScope.launch {
            val progress = _uiState.value.userProgress
            val updated = progress.copy(
                completedQuestionsCount = progress.completedQuestionsCount + 1,
                correctQuestionsCount = if (isCorrect) progress.correctQuestionsCount + 1 else progress.correctQuestionsCount
            )
            repository.saveUserProgress(updated)

            if (!isCorrect) {
                val chosenOption = currentQ.options.getOrNull(selectedOption)
                val correctOption = currentQ.options.getOrNull(currentQ.correctAnswerIndex)
                val wrongExplanation = currentQ.whyOptionsAreWrong.getOrNull(selectedOption)
                    ?: chosenOption?.whyWrong
                    ?: "Selected distractor contrary to NCERT principle."

                repository.recordMistake(
                    MistakeRecord(
                        id = "m_${currentQ.id}_${System.currentTimeMillis()}",
                        questionId = currentQ.id,
                        questionText = currentQ.questionText,
                        selectedOptionIndex = selectedOption,
                        selectedOptionText = chosenOption?.text ?: "Option $selectedOption",
                        correctOptionIndex = currentQ.correctAnswerIndex,
                        correctOptionText = correctOption?.text ?: "Option ${currentQ.correctAnswerIndex}",
                        chapterName = currentQ.sourceChapterName,
                        pageNumber = currentQ.sourcePageNumber,
                        misconceptionExplanation = wrongExplanation,
                        correctConceptSummary = currentQ.explanation,
                        ncertConnection = "Class NCERT Biology, ${currentQ.sourceChapterName}, Page ${currentQ.sourcePageNumber}"
                    )
                )
            }
        }
    }

    fun resetAIMCQStats() {
        _uiState.value = _uiState.value.copy(
            aiMCQScore = 0,
            aiMCQAttempted = 0,
            aiMCQCorrect = 0
        )
    }

    fun diagnoseMistake(mistake: MistakeRecord) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDiagnosingMistake = true,
                activeDiagnosedMistakeId = mistake.id
            )
            val page = repository.getPageByNumber(mistake.chapterName, mistake.pageNumber)
                ?: repository.getPagesForChapter("c_cell_8").firstOrNull()
            val pageContent = page?.content ?: ""

            val diagnosis = geminiService.analyzeMistake(
                questionText = mistake.questionText,
                studentAnswer = mistake.selectedOptionText,
                correctAnswer = mistake.correctOptionText,
                explanation = mistake.correctConceptSummary,
                chapterName = mistake.chapterName,
                pageNumber = mistake.pageNumber,
                pageContent = pageContent
            )
            _uiState.value = _uiState.value.copy(
                activeMistakeDiagnosis = diagnosis,
                isDiagnosingMistake = false
            )
        }
    }

    fun clearMistakeDiagnosis() {
        _uiState.value = _uiState.value.copy(
            activeMistakeDiagnosis = null,
            isDiagnosingMistake = false,
            activeDiagnosedMistakeId = null
        )
    }

    fun startTargetedPracticeForMistake(mistake: MistakeRecord) {
        val page = repository.getPageByNumber(mistake.chapterName, mistake.pageNumber)
        val chapterId = page?.chapterId ?: "c_cell_8"
        _uiState.value = _uiState.value.copy(
            currentScreen = ScreenDestination.AIMCQInteraction(chapterId, mistake.pageNumber),
            aiMCQSelectedOption = null,
            isAIMCQAnswered = false,
            activeMistakeDiagnosis = null,
            activeDiagnosedMistakeId = null
        )
        generateNextAIMCQ(
            chapterId = chapterId,
            pageNumber = mistake.pageNumber,
            sourcePoint = mistake.correctConceptSummary
        )
    }

    fun startTargetedPracticeForPoint(point: ImportantPoint) {
        val page = _uiState.value.activePage ?: return
        _uiState.value = _uiState.value.copy(
            currentScreen = ScreenDestination.AIMCQInteraction(page.chapterId, page.pageNumber),
            aiMCQSelectedOption = null,
            isAIMCQAnswered = false,
            activeMistakeDiagnosis = null,
            activeDiagnosedMistakeId = null
        )
        generateNextAIMCQ(
            chapterId = page.chapterId,
            pageNumber = page.pageNumber,
            sourcePoint = point.text
        )
    }

    fun openMistakeDetail(mistake: MistakeRecord) {
        _uiState.value = _uiState.value.copy(
            currentScreen = ScreenDestination.MistakeDetail(mistake)
        )
    }

    fun openBookmarksScreen() {
        _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.Bookmarks)
    }

    fun openMistakesScreen() {
        _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.MistakesList)
    }

    fun openAdminPortal() {
        _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.AdminPortal)
    }

    fun clearAllMistakes() {
        viewModelScope.launch {
            repository.clearAllMistakes()
        }
    }

    fun deleteMistake(id: String) {
        viewModelScope.launch {
            repository.deleteMistake(id)
        }
    }

    // NEET Guidance Navigation & Actions
    fun openNEETGuidance() {
        _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.NEETGuidance)
    }

    fun openGuidance(category: NEETGuidanceCategory? = null) {
        _uiState.value = _uiState.value.copy(
            selectedGuidanceCategory = category,
            currentScreen = ScreenDestination.NEETGuidance
        )
    }

    fun filterGuidanceCategory(category: NEETGuidanceCategory?) {
        _uiState.value = _uiState.value.copy(selectedGuidanceCategory = category)
    }

    // Global Search Navigation
    fun openGlobalSearch() {
        _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.GlobalSearch)
    }

    // Problem Reporting
    fun openReportProblem(targetContent: String = "") {
        _uiState.value = _uiState.value.copy(currentScreen = ScreenDestination.ReportProblem(targetContent))
    }

    fun submitProblemReport(problemType: ProblemType, description: String, targetContent: String) {
        val report = ProblemReport(
            id = "rep_${System.currentTimeMillis()}",
            problemType = problemType,
            description = description,
            targetContent = targetContent
        )
        repository.submitProblemReport(report)
        _uiState.value = _uiState.value.copy(
            problemReports = repository.getProblemReports()
        )
    }

    fun updateReportStatus(reportId: String, status: String) {
        repository.updateProblemReportStatus(reportId, status)
        _uiState.value = _uiState.value.copy(
            problemReports = repository.getProblemReports()
        )
    }

    // 30-Second Summary
    fun generate30SecondSummary(page: NCERTPageData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingSummary = true)
            val summary = geminiService.generate30SecondSummary(
                chapterName = page.chapterName,
                pageNumber = page.pageNumber,
                pageContent = page.content
            )
            _uiState.value = _uiState.value.copy(
                active30SecondSummary = summary,
                isGeneratingSummary = false
            )
        }
    }

    fun clear30SecondSummary() {
        _uiState.value = _uiState.value.copy(active30SecondSummary = null)
    }

    // Why is this important?
    fun explainWhyImportant(point: ImportantPoint, page: NCERTPageData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExplainingWhyImportant = true)
            val explanation = geminiService.explainWhyImportant(
                pointText = point.text,
                chapterName = page.chapterName,
                pageNumber = page.pageNumber
            )
            _uiState.value = _uiState.value.copy(
                activeWhyImportantExplanation = explanation,
                isExplainingWhyImportant = false
            )
        }
    }

    fun clearWhyImportant() {
        _uiState.value = _uiState.value.copy(activeWhyImportantExplanation = null)
    }

    // AI Review Queue in Admin
    fun updateAIReviewStatus(id: String, status: AIContentStatus, notes: String? = null) {
        repository.updateAIReviewStatus(id, status, notes)
        _uiState.value = _uiState.value.copy(
            aiReviewQueue = repository.getAIReviewQueue()
        )
    }

    fun getPageMasteryState(pageId: String): String {
        val progress = _uiState.value.userProgress
        return repository.getPageMasteryState(pageId, progress.readPages, progress.practicedPages)
    }
}
