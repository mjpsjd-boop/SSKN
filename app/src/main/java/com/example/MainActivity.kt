package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.sample.NCERTSampleData
import com.example.ui.components.AppNavDestination
import com.example.ui.components.SSKNBottomNav
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SSKNViewModel
import com.example.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {

    private val viewModel: SSKNViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SSKNAppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SSKNAppRoot(viewModel: SSKNViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle Android system back button
    BackHandler(enabled = uiState.currentScreen != ScreenDestination.Main && uiState.currentScreen != ScreenDestination.Splash) {
        viewModel.navigateBack()
    }

    val isTopLevelRoute = uiState.currentScreen is ScreenDestination.Main ||
            uiState.currentScreen is ScreenDestination.GlobalSearch ||
            uiState.currentScreen is ScreenDestination.NEETGuidance

    val activeNavDestination = when (uiState.currentScreen) {
        is ScreenDestination.GlobalSearch -> AppNavDestination.SEARCH
        is ScreenDestination.Main -> uiState.currentNavTab
        else -> uiState.currentNavTab
    }

    Scaffold(
        bottomBar = {
            if (isTopLevelRoute && uiState.currentScreen != ScreenDestination.Splash) {
                SSKNBottomNav(
                    currentDestination = activeNavDestination,
                    onSelectDestination = { tab -> viewModel.selectNavTab(tab) }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isTopLevelRoute) scaffoldPadding else PaddingValues(0.dp))
        ) {
            when (val screen = uiState.currentScreen) {
        is ScreenDestination.Splash -> {
            SplashScreen(
                onEnterApp = { viewModel.dismissSplash() }
            )
        }

        is ScreenDestination.ChapterDetail -> {
            val chapter = uiState.activeChapter
            if (chapter != null) {
                val pages = NCERTSampleData.getPagesForChapter(chapter.id)
                ChapterDetailScreen(
                    chapter = chapter,
                    pages = pages,
                    userProgress = uiState.userProgress,
                    onBackClick = { viewModel.navigateBack() },
                    onOpenPage = { pageNum -> viewModel.openReader(chapter.id, pageNum) },
                    onPracticeChapter = { viewModel.startPracticeForChapter(chapter.id) }
                )
            } else {
                viewModel.navigateBack()
            }
        }

        is ScreenDestination.NCERTReader -> {
            val page = uiState.activePage
            if (page != null) {
                val isBookmarked = viewModel.isBookmarked("bm_page_${page.chapterId}_${page.pageNumber}")
                val pagePYQs = NCERTSampleData.getPYQsForPage(page.pageNumber)

                NCERTReaderScreen(
                    page = page,
                    neetLens = uiState.activeNEETLens,
                    isLensLoading = uiState.isAnalyzingLens,
                    isBookmarked = isBookmarked,
                    onToggleBookmark = {
                        viewModel.toggleBookmark(
                            com.example.data.model.BookmarkItem(
                                id = "bm_page_${page.chapterId}_${page.pageNumber}",
                                chapterId = page.chapterId,
                                targetPage = page.pageNumber,
                                title = "NCERT Page ${page.pageNumber}",
                                subtitle = "${page.chapterName}: ${page.sectionTitle}",
                                type = "PAGE"
                            )
                        )
                    },
                    onBackClick = { viewModel.navigateBack() },
                    onPreviousPage = { viewModel.navigateReaderPage(-1) },
                    onNextPage = { viewModel.navigateReaderPage(1) },
                    onStartPractice = { count -> viewModel.startPracticeForPage(page.pageNumber, count) },
                    onStartAIDrill = { viewModel.startAIMCQDrill(page.chapterId, page.pageNumber) },
                    onAskAI = { query -> viewModel.askThisPage(query) },
                    chatMessages = uiState.chatMessages,
                    isChatLoading = uiState.isChatLoading,
                    pagePYQs = pagePYQs,
                    onScanPage = { viewModel.scanCurrentPage() },
                    onQuestionFromPoint = { point -> viewModel.startTargetedPracticeForPoint(point) },
                    scanningStage = uiState.scanningStage,
                    aiMode = uiState.aiMode,
                    onToggleAIMode = { viewModel.toggleAIMode() },
                    diagramAnalysis = uiState.activeDiagramAnalysis,
                    isDiagramAnalysisLoading = uiState.isAnalyzingDiagram,
                    onAnalyzeDiagram = { viewModel.analyzeDiagram() },
                    conceptComparison = uiState.activeConceptComparison,
                    isComparingConcepts = uiState.isComparingConcepts,
                    onCompareConcepts = { a, b -> viewModel.compareConcepts(a, b) },
                    onClearComparison = { viewModel.clearConceptComparison() },
                    summary = uiState.active30SecondSummary,
                    isSummaryLoading = uiState.isGeneratingSummary,
                    onGenerateSummary = { viewModel.generate30SecondSummary(page) }
                )
            } else {
                viewModel.navigateBack()
            }
        }

        is ScreenDestination.Practice -> {
            MCQPracticeScreen(
                title = screen.sourceTitle,
                questions = uiState.practiceQuestions,
                currentIndex = uiState.currentQuestionIndex,
                selectedOptionIndex = uiState.selectedOptionIndex,
                isAnswerSubmitted = uiState.isAnswerSubmitted,
                score = uiState.practiceScore,
                isCompleted = uiState.practiceCompleted,
                onSelectOption = { viewModel.selectOption(it) },
                onSubmitAnswer = { viewModel.submitAnswer() },
                onNextQuestion = { viewModel.nextQuestion() },
                onViewInNCERT = { chapterId, pageNum ->
                    viewModel.openReader(chapterId, pageNum)
                },
                onReviewMistakes = { viewModel.openMistakesScreen() },
                onFinish = { viewModel.navigateBack() },
                onBackClick = { viewModel.navigateBack() }
            )
        }

        is ScreenDestination.MistakesList -> {
            MistakesScreen(
                mistakes = uiState.mistakes,
                activeDiagnosis = uiState.activeMistakeDiagnosis,
                isDiagnosing = uiState.isDiagnosingMistake,
                activeMistakeId = uiState.activeDiagnosedMistakeId,
                onDiagnoseMistake = { mistake -> viewModel.diagnoseMistake(mistake) },
                onClearDiagnosis = { viewModel.clearMistakeDiagnosis() },
                onTargetedPractice = { mistake -> viewModel.startTargetedPracticeForMistake(mistake) },
                onViewInNCERT = { pageNum ->
                    val page = NCERTSampleData.allSamplePages.find { it.pageNumber == pageNum }
                    if (page != null) {
                        viewModel.openReader(page.chapterId, page.pageNumber)
                    }
                },
                onPracticeConcept = { pageNum ->
                    viewModel.startPracticeForPage(pageNum, count = 5)
                },
                onDeleteMistake = { viewModel.deleteMistake(it) },
                onClearAll = { viewModel.clearAllMistakes() },
                onBackClick = { viewModel.navigateBack() }
            )
        }

        is ScreenDestination.MistakeDetail -> {
            // Handled inside MistakesScreen or direct view
            viewModel.navigateBack()
        }

        is ScreenDestination.Bookmarks -> {
            BookmarksScreen(
                bookmarks = uiState.bookmarks,
                onOpenBookmark = { chapterId, pageNum ->
                    viewModel.openReader(chapterId, pageNum)
                },
                onRemoveBookmark = { viewModel.toggleBookmark(it) },
                onBackClick = { viewModel.navigateBack() }
            )
        }

        is ScreenDestination.AdminPortal -> {
            AdminContentScreen(
                onBackClick = { viewModel.navigateBack() }
            )
        }

        is ScreenDestination.AIMCQInteraction -> {
            val isBookmarked = uiState.bookmarks.any {
                it.type == "MCQ" && it.id == "bm_mcq_${uiState.currentAIMCQ?.id}"
            }
            AIMCQInteractionScreen(
                currentQuestion = uiState.currentAIMCQ,
                isGenerating = uiState.isGeneratingAIMCQ,
                selectedOption = uiState.aiMCQSelectedOption,
                isAnswered = uiState.isAIMCQAnswered,
                score = uiState.aiMCQScore,
                attempted = uiState.aiMCQAttempted,
                correct = uiState.aiMCQCorrect,
                preferredType = uiState.aiMCQPreferredType,
                difficulty = uiState.aiMCQDifficulty,
                onAnswerOption = { viewModel.answerAIMCQ(it) },
                onGenerateNext = { chId, pNum, type, diff ->
                    viewModel.generateNextAIMCQ(chId, pNum, type, diff)
                },
                onViewInNCERT = { chId, pNum ->
                    viewModel.openReader(chId, pNum)
                },
                onAskSaleemSir = { q ->
                    viewModel.openReader(q.sourceChapterId, q.sourcePageNumber)
                },
                onBackClick = { viewModel.navigateBack() },
                onToggleBookmark = { q ->
                    viewModel.toggleBookmark(
                        com.example.data.model.BookmarkItem(
                            id = "bm_mcq_${q.id}",
                            type = "MCQ",
                            title = "AI MCQ: Page ${q.sourcePageNumber}",
                            subtitle = q.questionText.take(50),
                            targetPage = q.sourcePageNumber,
                            chapterId = q.sourceChapterId
                        )
                    )
                },
                isBookmarked = isBookmarked
            )
        }

        is ScreenDestination.NEETGuidance -> {
            NEETGuidanceScreen(
                items = uiState.guidanceItems,
                selectedCategory = uiState.selectedGuidanceCategory,
                onSelectCategory = { viewModel.filterGuidanceCategory(it) },
                onBackClick = { viewModel.navigateBack() },
                onStartPractice = { viewModel.startAIMCQDrill() }
            )
        }

        is ScreenDestination.GlobalSearch -> {
            SearchScreen(
                query = uiState.searchQuery,
                results = uiState.searchResults,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onClearQuery = { viewModel.clearSearch() },
                onOpenResult = { chId, pageNum -> viewModel.openReader(chId, pageNum) }
            )
        }

        is ScreenDestination.ReportProblem -> {
            AdminContentScreen(
                onBackClick = { viewModel.navigateBack() }
            )
        }

        is ScreenDestination.Main -> {
            when (uiState.currentNavTab) {
                AppNavDestination.HOME -> {
                    HomeScreen(
                        userProgress = uiState.userProgress,
                        chapters = viewModel.getChapters(uiState.selectedClass),
                        onContinueReading = { chId, pageNum ->
                            viewModel.openReader(chId, pageNum)
                        },
                        onSelectClass = { cls ->
                            viewModel.selectClass(cls)
                        },
                        onOpenChapter = { chId ->
                            viewModel.openChapter(chId)
                        },
                        onNavigateToNCERT = {
                            viewModel.selectNavTab(AppNavDestination.NCERT)
                        },
                        onNavigateToPYQs = {
                            viewModel.selectNavTab(AppNavDestination.PYQS)
                        },
                        onNavigateToSearch = {
                            viewModel.selectNavTab(AppNavDestination.SEARCH)
                        },
                        onNavigateToBookmarks = {
                            viewModel.openBookmarksScreen()
                        },
                        onNavigateToMistakes = {
                            viewModel.openMistakesScreen()
                        },
                        onStartAIDrill = {
                            viewModel.startAIMCQDrill()
                        },
                        onNavigateToGuidance = { cat ->
                            viewModel.openGuidance(cat)
                        }
                    )
                }

                AppNavDestination.NCERT -> {
                    NCERTBrowseScreen(
                        selectedClass = uiState.selectedClass,
                        onSelectClass = { viewModel.selectClass(it) },
                        chapters = viewModel.getChapters(uiState.selectedClass),
                        onOpenChapter = { chId -> viewModel.openChapter(chId) },
                        onSearchClick = { viewModel.selectNavTab(AppNavDestination.SEARCH) }
                    )
                }

                AppNavDestination.PYQS -> {
                    val pyqs = viewModel.getFilteredPYQs()
                    PYQVaultScreen(
                        pyqs = pyqs,
                        selectedYear = uiState.selectedPYQYear,
                        selectedType = uiState.selectedPYQType,
                        onFilterYear = { y -> viewModel.setPYQFilters(y, uiState.selectedPYQChapterId, uiState.selectedPYQType) },
                        onFilterType = { t -> viewModel.setPYQFilters(uiState.selectedPYQYear, uiState.selectedPYQChapterId, t) },
                        onViewInNCERT = { chId, pageNum -> viewModel.openReader(chId, pageNum) },
                        onPracticeAll = { qList ->
                            viewModel.startPracticeForPYQs(qList, "Verified NEET PYQs Quiz")
                        },
                        onSearchClick = { viewModel.selectNavTab(AppNavDestination.SEARCH) }
                    )
                }

                AppNavDestination.SEARCH -> {
                    SearchScreen(
                        query = uiState.searchQuery,
                        results = uiState.searchResults,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        onClearQuery = { viewModel.clearSearch() },
                        onOpenResult = { chId, pageNum -> viewModel.openReader(chId, pageNum) }
                    )
                }

                AppNavDestination.MORE -> {
                    MoreScreen(
                        selectedClass = uiState.selectedClass,
                        onSelectClass = { viewModel.selectClass(it) },
                        onOpenBookmarks = { viewModel.openBookmarksScreen() },
                        onOpenMistakes = { viewModel.openMistakesScreen() },
                        onOpenAdmin = { viewModel.openAdminPortal() },
                        onSearchClick = { viewModel.selectNavTab(AppNavDestination.SEARCH) }
                    )
                }
            }
        }
    }
}
}
}
