package com.example.network.gemini

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = BuildConfig.GEMINI_API_KEY

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    suspend fun analyzePageWithNEETLens(
        chapterName: String,
        pageNumber: Int,
        pageContent: String
    ): NEETLensAnalysis = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Gemini AI is not configured")
        }

        val prompt = """
            You are SSKN (Saleem Sir Ki NCERT) AI Biology Master.
            Analyze the following NCERT Page for NEET UG aspirants.
            CHAPTER: $chapterName
            PAGE NUMBER: $pageNumber
            PAGE CONTENT:
            $pageContent

            Identify genuinely critical statements and lines that must be highlighted on the page.
            Return ONLY a valid JSON object strictly matching this schema:
            {
              "mustRemember": ["string", "string", "string"],
              "neetFocus": "string",
              "keyTerms": ["string", "string"],
              "definitions": ["string", "string"],
              "commonConfusions": ["string", "string"],
              "keyNCERTFacts": ["string", "string"],
              "questionPotential": "string",
              "diagramFocus": "string",
              "highlights": [
                {
                  "text": "Exact text quote from page content to highlight",
                  "importance": "CRITICAL",
                  "reason": "Why this line matters for NEET",
                  "questionPotential": "Single Correct / Assertion-Reason",
                  "concept": "Name of biological concept"
                }
              ]
            }
            Do not include markdown ticks, just raw JSON.
        """.trimIndent()

        try {
            val responseText = callGeminiRaw(prompt)
            val jsonStr = cleanJson(responseText)
            val json = JSONObject(jsonStr)

            val mustRemember = mutableListOf<String>()
            val mrArray = json.optJSONArray("mustRemember")
            if (mrArray != null) {
                for (i in 0 until mrArray.length()) mustRemember.add(mrArray.getString(i))
            }

            val commonConfusions = mutableListOf<String>()
            val ccArray = json.optJSONArray("commonConfusions")
            if (ccArray != null) {
                for (i in 0 until ccArray.length()) commonConfusions.add(ccArray.getString(i))
            }

            val keyFacts = mutableListOf<String>()
            val kfArray = json.optJSONArray("keyNCERTFacts")
            if (kfArray != null) {
                for (i in 0 until kfArray.length()) keyFacts.add(kfArray.getString(i))
            }

            val keyTerms = mutableListOf<String>()
            val ktArray = json.optJSONArray("keyTerms")
            if (ktArray != null) {
                for (i in 0 until ktArray.length()) keyTerms.add(ktArray.getString(i))
            }

            val definitions = mutableListOf<String>()
            val defArray = json.optJSONArray("definitions")
            if (defArray != null) {
                for (i in 0 until defArray.length()) definitions.add(defArray.getString(i))
            }

            val extractedHighlights = mutableListOf<ImportantPoint>()
            val hlArray = json.optJSONArray("highlights")
            if (hlArray != null) {
                for (i in 0 until hlArray.length()) {
                    val obj = hlArray.getJSONObject(i)
                    val text = obj.optString("text")
                    if (text.isNotBlank()) {
                        val impStr = obj.optString("importance", "HIGH")
                        val ranking = when (impStr.uppercase()) {
                            "CRITICAL" -> ImportanceRanking.CRITICAL
                            "MEDIUM" -> ImportanceRanking.MEDIUM
                            else -> ImportanceRanking.HIGH
                        }
                        extractedHighlights.add(
                            ImportantPoint(
                                id = "hl_${pageNumber}_$i",
                                text = text,
                                importance = ranking,
                                reason = obj.optString("reason", "High-yield NCERT concept"),
                                questionType = obj.optString("questionPotential", "Single Correct"),
                                relatedConcepts = listOf(obj.optString("concept", "Biology Core")),
                                sourceLine = text
                            )
                        )
                    }
                }
            }

            NEETLensAnalysis(
                pageId = "p_${chapterName}_$pageNumber",
                mustRemember = if (mustRemember.isNotEmpty()) mustRemember else listOf("High-yield NCERT facts on Page $pageNumber"),
                neetFocus = json.optString("neetFocus", "Focus on lines frequently tested in NEET UG."),
                commonConfusions = commonConfusions,
                keyNCERTFacts = keyFacts,
                questionPotential = json.optString("questionPotential", "High potential for Assertion-Reason & Statement questions."),
                diagramFocus = json.optString("diagramFocus", "Pay attention to diagram labels and captions on this page."),
                highlights = extractedHighlights,
                keyTerms = keyTerms,
                definitions = definitions
            )
        } catch (e: Exception) {
            Log.e("GeminiService", "Error analyzing page with Gemini: ${e.message}")
            throw IllegalStateException("AI analysis could not be completed", e)
        }
    }

    suspend fun askThisPage(
        chapterName: String,
        pageNumber: Int,
        pageContent: String,
        userQuery: String,
        history: List<ChatMessage>,
        mode: PracticeMode = PracticeMode.NCERT_STRICT
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext if (mode == PracticeMode.NCERT_STRICT) {
                "[NCERT STRICT MODE]\nAccording to NCERT Page $pageNumber ($chapterName):\n$userQuery relates directly to the core principles stated on this page. Review the highlighted statements in the NCERT text above for exact wording."
            } else {
                "[NEET MODE · HIGH YIELD]\nPage $pageNumber ($chapterName) analysis for NEET UG:\n$userQuery touches on high-yield exam concepts. Pay attention to trap wording and parenthetical qualifiers."
            }
        }

        val historyText = history.takeLast(4).joinToString("\n") {
            "${if (it.isUser) "Student" else "SSKN AI"}: ${it.text}"
        }

        val modeInstructions = if (mode == PracticeMode.NCERT_STRICT) {
            """
            MODE: NCERT STRICT MODE
            - Answers must strictly come from the provided NCERT text.
            - If student asks something NOT in this page/NCERT, say: 'Not directly stated in this NCERT page. I can give you a general Biology explanation if you want.'
            - No assumptions beyond the text.
            - Mark your response clearly with [NCERT BASED].
            """.trimIndent()
        } else {
            """
            MODE: NEET MODE (HIGH-YIELD EXAM FOCUS)
            - Evaluate question potential for NEET UG.
            - Flag high-probability exam points and trap wording (e.g. 'not true', 'except', 'only', 'all').
            - Predict Statement I / Statement II or Assertion-Reason patterns.
            - Never claim 'This will definitely come in NEET'. Say 'High-yield for NEET' or 'Frequently tested concept'.
            - Mark your response clearly with [NEET FOCUS].
            """.trimIndent()
        }

        val prompt = """
            You are SSKN (Saleem Sir Ki NCERT) AI Biology Master.
            Your role: Guide NEET UG aspirants to 360/360 in Biology with absolute precision.
            
            $modeInstructions

            PINNED CONTEXT:
            Chapter: $chapterName
            NCERT Page: $pageNumber
            Page Content:
            $pageContent

            CONVERSATION HISTORY:
            $historyText

            STUDENT ASKS:
            $userQuery

            Provide a clear, authoritative, beautifully structured response with bullet points and exact line references.
        """.trimIndent()

        try {
            callGeminiRaw(prompt)
        } catch (e: Exception) {
            Log.e("GeminiService", "Ask page error: ${e.message}")
            "Based on NCERT Page $pageNumber of $chapterName: Please focus on the highlighted definitions and statements as they form the basis for NEET questions."
        }
    }

    suspend fun compareConcepts(
        concept1: String,
        concept2: String,
        chapterName: String,
        pageNumber: Int,
        pageContent: String
    ): ConceptComparison = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackConceptComparison(concept1, concept2, chapterName, pageNumber)
        }

        val prompt = """
            You are SSKN AI Biology Master.
            Compare these two biological concepts strictly according to NCERT Biology:
            CONCEPT 1: $concept1
            CONCEPT 2: $concept2
            CONTEXT: Chapter $chapterName, Page $pageNumber
            PAGE CONTENT:
            $pageContent

            Return ONLY a valid JSON object matching this schema:
            {
              "concept1": "$concept1",
              "concept2": "$concept2",
              "definitionDifference": "Clear, direct contrast between the two definitions per NCERT",
              "processDifferences": ["Difference in occurrence/location", "Difference in mechanism", "Difference in outcome"],
              "keyDistinctions": ["Distinction point 1", "Distinction point 2", "Distinction point 3"],
              "ncertReference": "NCERT Biology Chapter $chapterName, Page $pageNumber",
              "neetExamTip": "High-Yield NEET Exam Tip: examiners frequently swap these two terms in Statement questions."
            }
            Do not include markdown ticks, just raw JSON.
        """.trimIndent()

        try {
            val res = callGeminiRaw(prompt)
            val json = JSONObject(cleanJson(res))

            val procList = mutableListOf<String>()
            val procArr = json.optJSONArray("processDifferences")
            if (procArr != null) {
                for (i in 0 until procArr.length()) procList.add(procArr.getString(i))
            }

            val distList = mutableListOf<String>()
            val distArr = json.optJSONArray("keyDistinctions")
            if (distArr != null) {
                for (i in 0 until distArr.length()) distList.add(distArr.getString(i))
            }

            ConceptComparison(
                concept1 = json.optString("concept1", concept1),
                concept2 = json.optString("concept2", concept2),
                definitionDifference = json.optString("definitionDifference", "Fundamental distinction stated in NCERT Page $pageNumber."),
                processDifferences = if (procList.isNotEmpty()) procList else listOf("Occurrence and cellular localization differences", "Structural and functional variations"),
                keyDistinctions = if (distList.isNotEmpty()) distList else listOf("Key distinction per NCERT text"),
                ncertReference = json.optString("ncertReference", "NCERT Biology Page $pageNumber"),
                neetExamTip = json.optString("neetExamTip", "Watch out for Statement I & II traps swapping these definitions in NEET.")
            )
        } catch (e: Exception) {
            Log.e("GeminiService", "Failed to compare concepts: ${e.message}")
            fallbackConceptComparison(concept1, concept2, chapterName, pageNumber)
        }
    }

    private fun fallbackConceptComparison(
        concept1: String,
        concept2: String,
        chapterName: String,
        pageNumber: Int
    ): ConceptComparison {
        return ConceptComparison(
            concept1 = concept1,
            concept2 = concept2,
            definitionDifference = "$concept1 and $concept2 represent distinct biological entities/processes described on NCERT Page $pageNumber.",
            processDifferences = listOf(
                "$concept1 operates under specific structural constraints defined in NCERT.",
                "$concept2 represents a complementary or contrasting mechanism.",
                "Examiners test the exact boundary conditions between both."
            ),
            keyDistinctions = listOf(
                "Distinct morphological or biochemical properties",
                "Explicitly categorized under separate headings in NCERT"
            ),
            ncertReference = "NCERT Biology $chapterName (Page $pageNumber)",
            neetExamTip = "NEET Trap Alert: NTA frequently inverts the characteristics of $concept1 and $concept2 in Statement-based MCQs!"
        )
    }

    suspend fun analyzeDiagramFull(
        chapterName: String,
        pageNumber: Int,
        diagramTitle: String,
        diagramDescription: String,
        diagramCaption: String?
    ): DiagramAnalysisResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext DiagramAnalysisResult(
                title = diagramTitle,
                representation = "Visual depiction of $diagramTitle from NCERT Page $pageNumber.",
                keyStructures = listOf("Key structural elements labeled in the textbook illustration", "Orientation and membrane polarity"),
                processOrSequence = "Follows the sequential pathway described in the accompanying text on Page $pageNumber.",
                potentialQuestions = listOf(
                    "Identify labels A, B, C, and D in the given diagram",
                    "Which labeled part is responsible for the stated cellular function?",
                    "Assertion-Reason based on diagram caption footnote"
                )
            )
        }

        val prompt = """
            You are SSKN AI Biology Master and Diagram Tutor.
            Analyze this NCERT Biology textbook diagram for NEET aspirants:
            CHAPTER: $chapterName, PAGE: $pageNumber
            DIAGRAM TITLE: $diagramTitle
            DESCRIPTION: $diagramDescription
            CAPTION: ${diagramCaption ?: "Standard NCERT Figure"}

            Return ONLY a valid JSON object matching this schema:
            {
              "title": "$diagramTitle",
              "representation": "What this diagram represents in 1-2 precise sentences",
              "keyStructures": ["Label/structure 1 and its function", "Label/structure 2", "Label/structure 3"],
              "processOrSequence": "Sequential flow or relationship depicted",
              "potentialQuestions": [
                "Label identification question pattern",
                "Function-matching question pattern",
                "Common diagram trap examiners exploit in NEET"
              ]
            }
            Do not include markdown ticks, just raw JSON.
        """.trimIndent()

        try {
            val res = callGeminiRaw(prompt)
            val json = JSONObject(cleanJson(res))

            val structures = mutableListOf<String>()
            val sArr = json.optJSONArray("keyStructures")
            if (sArr != null) {
                for (i in 0 until sArr.length()) structures.add(sArr.getString(i))
            }

            val questions = mutableListOf<String>()
            val qArr = json.optJSONArray("potentialQuestions")
            if (qArr != null) {
                for (i in 0 until qArr.length()) questions.add(qArr.getString(i))
            }

            DiagramAnalysisResult(
                title = json.optString("title", diagramTitle),
                representation = json.optString("representation", "Representation of $diagramTitle"),
                keyStructures = if (structures.isNotEmpty()) structures else listOf("Core labeled parts per NCERT figure"),
                processOrSequence = json.optString("processOrSequence", "Sequential relationship depicted on Page $pageNumber"),
                potentialQuestions = if (questions.isNotEmpty()) questions else listOf("Label identification MCQ", "Functional association question")
            )
        } catch (e: Exception) {
            Log.e("GeminiService", "Failed to analyze diagram: ${e.message}")
            DiagramAnalysisResult(
                title = diagramTitle,
                representation = "Depiction of $diagramTitle on NCERT Page $pageNumber.",
                keyStructures = listOf("Main labeled parts indicated in textbook figure"),
                processOrSequence = "Mechanistic sequence described in the text.",
                potentialQuestions = listOf("Label identification", "Structure-function relationship in NEET")
            )
        }
    }

    suspend fun searchBiologyAI(
        query: String,
        currentChapter: String,
        currentPage: Int
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Searching NCERT Biology for '$query': Relevant concepts appear in $currentChapter (around Page $currentPage) and related unit chapters. Review verbatim definitions and verified NEET PYQs."
        }

        val prompt = """
            You are SSKN AI Biology Search Engine.
            A student entered this natural-language query about NCERT Biology:
            "$query"
            
            Current student context: Chapter $currentChapter, Page $currentPage.

            Provide an instant, accurate academic result:
            1. NCERT Location (Class 11/12, Chapter name, Page/Section)
            2. Exact Verbatim NCERT Definition or Statement
            3. NEET Relevance (Has this appeared in NEET? How is it tested?)
            4. Common Trap or Confusion to avoid.
            Ground strictly in NCERT Biology.
        """.trimIndent()

        try {
            callGeminiRaw(prompt)
        } catch (e: Exception) {
            "Relevant NCERT result for '$query': Found in $currentChapter around Page $currentPage. Consult the highlighted text and verified PYQs."
        }
    }


    suspend fun analyzeMistake(
        questionText: String,
        selectedOption: String,
        correctOption: String,
        explanation: String,
        chapterName: String,
        pageNumber: Int
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "You selected '$selectedOption' instead of '$correctOption'. This is a common trap in NEET. NCERT Page $pageNumber clarifies that the correct concept is: $explanation"
        }

        val prompt = """
            You are SSKN 'Learn From This' analysis engine.
            A NEET aspirant got this question wrong:
            QUESTION: $questionText
            SELECTED (WRONG) OPTION: $selectedOption
            CORRECT OPTION: $correctOption
            OFFICIAL EXPLANATION: $explanation
            NCERT CHAPTER: $chapterName, PAGE: $pageNumber

            Explain in 3 short bullet points:
            1. Why the student likely chose the incorrect option (the misconception).
            2. The exact NCERT fact that refutes it.
            3. A 1-sentence mnemonic or rule to never get this wrong in NEET.
        """.trimIndent()

        try {
            callGeminiRaw(prompt)
        } catch (e: Exception) {
            "Misconception Analysis: You chose '$selectedOption' whereas NCERT Page $pageNumber specifies '$correctOption'. Keep the exact NCERT phrasing in mind."
        }
    }

    private fun callGeminiRaw(promptText: String): String {
        val url = "$baseUrl?key=$apiKey"
        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", promptText)
                        }
                        put(partObj)
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)
        }

        val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: ${response.message}")
            }
            val responseBody = response.body?.string() ?: throw Exception("Empty body")
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
            throw Exception("No text returned in Gemini response")
        }
    }

    private fun cleanJson(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json").trim()
        }
        if (text.startsWith("```")) {
            text = text.removePrefix("```").trim()
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```").trim()
        }
        return text
    }

    private fun fallbackAnalysis(chapterName: String, pageNumber: Int): NEETLensAnalysis {
        return NEETLensAnalysis(
            pageId = "p_${chapterName}_$pageNumber",
            mustRemember = listOf(
                "Core definitions and key terms stated on NCERT Page $pageNumber",
                "Distinctions between historical experiments and modern conclusions",
                "Specific exception cases explicitly mentioned in the text"
            ),
            neetFocus = "High-priority page for Assertion-Reason and direct factual multiple-choice questions.",
            commonConfusions = listOf(
                "Confusing similar terminologies and historical scientists",
                "Overlooking parenthetical conditions in NCERT statements"
            ),
            keyNCERTFacts = listOf(
                "All definitions on this page are verbatim testable in NEET UG",
                "Diagram captions contain crucial additional factual points"
            ),
            questionPotential = "Frequently tested in recent NEET papers (2018-2024).",
            diagramFocus = "Verify labels, polarity, and orientation in accompanying illustrations."
        )
    }

    suspend fun generateAIMCQ(
        chapterName: String,
        chapterId: String,
        pageNumber: Int,
        pageContent: String,
        preferredType: QuestionType? = null,
        difficulty: Difficulty = Difficulty.NEET_LEVEL,
        sourcePoint: String? = null
    ): Question = withContext(Dispatchers.IO) {
        val typeStr = when (preferredType) {
            QuestionType.ASSERTION_REASON -> "ASSERTION_REASON"
            QuestionType.STATEMENT_BASED -> "STATEMENT_BASED"
            QuestionType.MATCH_FOLLOWING -> "MATCH_FOLLOWING"
            else -> "SINGLE_CORRECT"
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Gemini AI is not configured")
        }

        val pointContext = if (!sourcePoint.isNullOrBlank()) {
            "\nGROUNDING REQUIREMENT: The question MUST be strictly generated from this specific highlighted NCERT point:\n\"$sourcePoint\"\n"
        } else ""

        val prompt = """
            You are SSKN (Saleem Sir Ki NCERT) AI Biology Master.
            Generate 1 NEET-UG level multiple-choice question strictly grounded in the following NCERT page content.
            Avoid outside syllabus claims. Ensure options are clear, plausible distractors reflecting common student misconceptions.
            $pointContext
            CHAPTER: $chapterName
            PAGE: $pageNumber
            PAGE CONTENT:
            $pageContent

            FORMAT PREFERENCE: $typeStr
            DIFFICULTY: ${difficulty.name}

            Return ONLY a valid JSON object matching this schema:
            {
              "questionText": "The main question statement",
              "questionType": "$typeStr",
              "statement1": "Text for Statement I if STATEMENT_BASED, else null",
              "statement2": "Text for Statement II if STATEMENT_BASED, else null",
              "assertionText": "Assertion text if ASSERTION_REASON, else null",
              "reasonText": "Reason text if ASSERTION_REASON, else null",
              "options": [
                {"id": "opt_A", "text": "Option A text", "whyWrong": "Detailed reason why Option A is incorrect or correct"},
                {"id": "opt_B", "text": "Option B text", "whyWrong": "Detailed reason why Option B is incorrect or correct"},
                {"id": "opt_C", "text": "Option C text", "whyWrong": "Detailed reason why Option C is incorrect or correct"},
                {"id": "opt_D", "text": "Option D text", "whyWrong": "Detailed reason why Option D is incorrect or correct"}
              ],
              "correctAnswerIndex": 0,
              "explanation": "Comprehensive explanation of why the correct option is true, citing the exact NCERT concept and wording.",
              "whyOptionsAreWrong": [
                "Detailed distractor breakdown for Option A",
                "Detailed distractor breakdown for Option B",
                "Detailed distractor breakdown for Option C",
                "Detailed distractor breakdown for Option D"
              ],
              "sourcePoint": "${sourcePoint ?: "NCERT Page $pageNumber specific paragraph or line"}",
              "neetTip": "High-Yield NEET Tip: Focus on precise wording and parenthetical qualifiers"
            }
            Do not include markdown ticks, just raw JSON.
        """.trimIndent()

        try {
            val responseText = callGeminiRaw(prompt)
            val jsonStr = cleanJson(responseText)
            val json = JSONObject(jsonStr)

            val qType = when (json.optString("questionType")) {
                "ASSERTION_REASON" -> QuestionType.ASSERTION_REASON
                "STATEMENT_BASED" -> QuestionType.STATEMENT_BASED
                "MATCH_FOLLOWING" -> QuestionType.MATCH_FOLLOWING
                else -> preferredType ?: QuestionType.SINGLE_CORRECT
            }

            val optionsList = mutableListOf<QuestionOption>()
            val optArray = json.optJSONArray("options")
            if (optArray != null && optArray.length() >= 4) {
                for (i in 0 until optArray.length()) {
                    val obj = optArray.getJSONObject(i)
                    optionsList.add(
                        QuestionOption(
                            id = obj.optString("id", "opt_$i"),
                            text = obj.optString("text", "Option ${('A' + i)}"),
                            whyWrong = obj.optString("whyWrong").takeIf { it != "null" }
                        )
                    )
                }
            } else {
                optionsList.addAll(
                    listOf(
                        QuestionOption("opt_0", "Option A"),
                        QuestionOption("opt_1", "Option B"),
                        QuestionOption("opt_2", "Option C"),
                        QuestionOption("opt_3", "Option D")
                    )
                )
            }

            val whyWrongList = mutableListOf<String>()
            val whyWrongArray = json.optJSONArray("whyOptionsAreWrong")
            if (whyWrongArray != null) {
                for (i in 0 until whyWrongArray.length()) {
                    whyWrongList.add(whyWrongArray.getString(i))
                }
            }
            while (whyWrongList.size < optionsList.size) {
                whyWrongList.add("Option is contrary to NCERT Page $pageNumber facts.")
            }

            val rawGenerated = Question(
                id = "ai_q_${System.currentTimeMillis()}_${(1000..9999).random()}",
                questionText = json.optString("questionText", "Identify the correct statement according to NCERT Page $pageNumber:"),
                options = optionsList,
                correctAnswerIndex = json.optInt("correctAnswerIndex", 0).coerceIn(0, optionsList.size - 1),
                explanation = json.optString("explanation", "As stated in NCERT Page $pageNumber: this concept is directly tested in NEET UG."),
                whyOptionsAreWrong = whyWrongList,
                sourceChapterId = chapterId,
                sourceChapterName = chapterName,
                sourcePageNumber = pageNumber,
                sourcePoint = sourcePoint ?: json.optString("sourcePoint", "NCERT Page $pageNumber"),
                questionType = qType,
                difficulty = difficulty,
                mode = PracticeMode.NCERT_STRICT,
                sourceType = SourceType.AI_GENERATED,
                verificationStatus = "AI GENERATED (VALIDATED)",
                isAssertionReason = qType == QuestionType.ASSERTION_REASON,
                assertionText = json.optString("assertionText").takeIf { it.isNotBlank() && it != "null" },
                reasonText = json.optString("reasonText").takeIf { it.isNotBlank() && it != "null" },
                statement1 = json.optString("statement1").takeIf { it.isNotBlank() && it != "null" },
                statement2 = json.optString("statement2").takeIf { it.isNotBlank() && it != "null" },
                neetTip = json.optString("neetTip").takeIf { it.isNotBlank() && it != "null" }
            )

            // Strict Question Validation Pipeline
            val validation = QuestionValidator.validate(rawGenerated, pageContent)
            if (validation is QuestionValidator.ValidationResult.Invalid) {
                throw IllegalStateException("Generated question failed validation: ${validation.reason}")
            } else {
                rawGenerated
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Failed to generate AI question via Gemini: ${e.message}")
            throw IllegalStateException("AI question generation could not be completed", e)
        }
    }

    suspend fun analyzeMistake(
        questionText: String,
        studentAnswer: String,
        correctAnswer: String,
        explanation: String,
        chapterName: String,
        pageNumber: Int,
        pageContent: String
    ): LearnFromThisResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext LearnFromThisResult(
                whatYouMisunderstood = "You selected '$studentAnswer' instead of '$correctAnswer'. NEET aspirants often conflate similar technical descriptors in NCERT.",
                correctConcept = "NCERT Page $pageNumber establishes the canonical benchmark: $correctAnswer.",
                ncertConnection = "Directly grounded in NCERT Biology ($chapterName, Page $pageNumber).",
                whyYourAnswerWasWrong = "The selected option conflicts with the precise factual conditions stated in NCERT.",
                whatToRemember = "NEET Revision Rule: Pay special attention to definitive qualifiers like 'strictly', 'only', and 'all' in NCERT questions."
            )
        }

        val prompt = """
            You are SSKN AI Biology Master.
            A NEET UG aspirant made a mistake on an NCERT-based question. Diagnose their misconception.

            QUESTION: $questionText
            STUDENT'S WRONG ANSWER: $studentAnswer
            CORRECT NCERT ANSWER: $correctAnswer
            EXPLANATION: $explanation
            NCERT CHAPTER: $chapterName, PAGE: $pageNumber
            PAGE CONTENT: $pageContent

            Return ONLY a valid JSON object strictly matching this schema:
            {
              "whatYouMisunderstood": "Detailed diagnosis of student's conceptual misconception",
              "correctConcept": "The precise NCERT concept explained clearly and authoritatively",
              "ncertConnection": "Exact reference to textbook context on Page $pageNumber",
              "whyYourAnswerWasWrong": "Why the student's selected option is scientifically/factually false per NCERT",
              "whatToRemember": "A memorable NEET memory hook or high-yield rule to prevent this mistake"
            }
            Do not include markdown ticks, just raw JSON.
        """.trimIndent()

        try {
            val responseText = callGeminiRaw(prompt)
            val json = JSONObject(cleanJson(responseText))
            LearnFromThisResult(
                whatYouMisunderstood = json.optString("whatYouMisunderstood", "Misinterpretation of core NCERT distinction."),
                correctConcept = json.optString("correctConcept", "Correct concept per NCERT Page $pageNumber."),
                ncertConnection = json.optString("ncertConnection", "Refer to Chapter $chapterName, Page $pageNumber."),
                whyYourAnswerWasWrong = json.optString("whyYourAnswerWasWrong", "Option contradicts stated textbook facts."),
                whatToRemember = json.optString("whatToRemember", "Review this line in the NCERT textbook.")
            )
        } catch (e: Exception) {
            Log.e("GeminiService", "Error analyzing mistake: ${e.message}")
            LearnFromThisResult(
                whatYouMisunderstood = "Confusion between similar NCERT biological terms.",
                correctConcept = "As stated on Page $pageNumber: $correctAnswer is the verified NCERT fact.",
                ncertConnection = "Direct NCERT line mapping: $chapterName Page $pageNumber.",
                whyYourAnswerWasWrong = "The chosen option contradicts the explicit textbook statement.",
                whatToRemember = "Mark this line with a high-priority highlight in your NCERT Reader."
            )
        }
    }

    suspend fun explainDiagram(
        chapterName: String,
        pageNumber: Int,
        diagramTitle: String,
        diagramDescription: String
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Diagram '$diagramTitle' is a high-yield visual asset in NCERT Biology Page $pageNumber. Focus on all labeled parts and their specific cellular functions as tested in NEET UG."
        }

        val prompt = """
            You are SSKN (Saleem Sir Ki NCERT) AI Biology Master.
            Explain this NCERT textbook diagram for NEET UG aspirants:
            CHAPTER: $chapterName, PAGE: $pageNumber
            DIAGRAM: $diagramTitle
            DETAILS: $diagramDescription

            Provide a concise, high-yield breakdown:
            1. Significance of the structure
            2. Critical labels examiners target in NEET
            3. Common diagram traps
        """.trimIndent()

        try {
            val response = callGeminiRaw(prompt)
            cleanJson(response).replace(Regex("^\"|\"$"), "").trim()
        } catch (e: Exception) {
            "Diagram '$diagramTitle' is a high-yield visual asset in NCERT Biology Page $pageNumber. Pay close attention to polarity, orientation, and labels."
        }
    }

    private fun fallbackQuestion(
        chapterName: String,
        chapterId: String,
        pageNumber: Int,
        preferredType: QuestionType?,
        difficulty: Difficulty,
        sourcePoint: String? = null
    ): Question {
        return when (pageNumber) {
            126 -> Question(
                id = "ai_fb_126_${System.currentTimeMillis()}",
                questionText = "Consider the following statements regarding the cell envelope of bacteria according to NCERT Page 126:",
                options = listOf(
                    QuestionOption("opt_0", "The cell envelope consists of a loosely bound two-layered structure.", "Incorrect: NCERT states it is a tightly bound three-layered structure."),
                    QuestionOption("opt_1", "The glycocalyx differs in composition and thickness among different bacteria.", "Correct: In some it is a loose sheath called slime layer, in others a thick and tough capsule."),
                    QuestionOption("opt_2", "Gram-positive bacteria do not take up the Gram stain because of the absence of peptidoglycan.", "Incorrect: Gram-positive bacteria take up Gram stain; Gram-negative do not retain it."),
                    QuestionOption("opt_3", "Mesosomes are extensions of the outer cell wall into the cytoplasm.", "Incorrect: Mesosomes are extensions of the plasma membrane into the cell, not the cell wall.")
                ),
                correctAnswerIndex = 1,
                explanation = "NCERT Page 126 verbatim: 'Glycocalyx differs in composition and thickness among different bacteria. It could be a loose sheath called the slime layer in some, while in others it may be thick and tough, called the capsule.'",
                whyOptionsAreWrong = listOf(
                    "Option A is false: The envelope consists of a tightly bound three-layered structure (glycocalyx, cell wall, plasma membrane), not two.",
                    "Option B is the correct answer according to NCERT Page 126.",
                    "Option C is false: Gram-positive bacteria take up and retain the Gram stain; Gram-negative do not.",
                    "Option D is false: Mesosomes are specialized membranous structures formed by the extension of the plasma membrane, not the cell wall."
                ),
                sourceChapterId = chapterId,
                sourceChapterName = chapterName,
                sourcePageNumber = pageNumber,
                sourcePoint = "NCERT Page 126, paragraph 2 & 3",
                questionType = QuestionType.SINGLE_CORRECT,
                difficulty = difficulty,
                mode = PracticeMode.NCERT_STRICT,
                sourceType = SourceType.AI_GENERATED,
                verificationStatus = "AI GENERATED (GEMINI 3.5)",
                neetTip = "NEET Lens Tip: Always note the sequence from outside to inside: Glycocalyx -> Cell Wall -> Plasma Membrane!"
            )
            132 -> Question(
                id = "ai_fb_132_${System.currentTimeMillis()}",
                questionText = "Given below are two statements regarding Singer and Nicolson's Fluid Mosaic Model (1972) on NCERT Page 132:",
                statement1 = "The quasi-fluid nature of lipid enables lateral movement of proteins within the overall bilayer.",
                statement2 = "This ability to move within the membrane is measured as its fluidity.",
                options = listOf(
                    QuestionOption("opt_0", "Both Statement I and Statement II are correct", "Correct: Both statements are verbatim sentences from NCERT Page 132."),
                    QuestionOption("opt_1", "Both Statement I and Statement II are incorrect", "Incorrect: Both statements accurately reflect the Fluid Mosaic Model in NCERT."),
                    QuestionOption("opt_2", "Statement I is correct but Statement II is incorrect", "Incorrect: Statement II is explicitly stated on NCERT Page 132."),
                    QuestionOption("opt_3", "Statement I is incorrect but Statement II is correct", "Incorrect: Statement I is explicitly true as per NCERT.")
                ),
                correctAnswerIndex = 0,
                explanation = "According to NCERT Page 132: 'The quasi-fluid nature of lipid enables lateral movement of proteins within the overall bilayer. This ability to move within the membrane is measured as its fluidity.' Both statements are entirely accurate.",
                whyOptionsAreWrong = listOf(
                    "Option A is the correct answer.",
                    "Option B is incorrect because neither statement is false.",
                    "Option C is incorrect because Statement II is true.",
                    "Option D is incorrect because Statement I is true."
                ),
                sourceChapterId = chapterId,
                sourceChapterName = chapterName,
                sourcePageNumber = pageNumber,
                sourcePoint = "NCERT Page 132, Plasma Membrane Fluidity",
                questionType = QuestionType.STATEMENT_BASED,
                difficulty = difficulty,
                mode = PracticeMode.NCERT_STRICT,
                sourceType = SourceType.AI_GENERATED,
                verificationStatus = "AI GENERATED (GEMINI 3.5)",
                neetTip = "NEET Lens Tip: NTA frequently tests the definition of fluidity—it is the ability of proteins to move laterally within the lipid bilayer!"
            )
            else -> Question(
                id = "ai_fb_gen_${System.currentTimeMillis()}",
                questionText = "Which of the following statements represents an exact factual principle from NCERT Page $pageNumber ($chapterName)?",
                options = listOf(
                    QuestionOption("opt_0", "Core definitions and terminologies presented on this page form verbatim testable statements in NEET UG.", "Correct: NCERT statements are directly quoted by NTA in NEET Biology."),
                    QuestionOption("opt_1", "Outside-syllabus theories supersede the direct observations described in NCERT text.", "Incorrect: NEET UG strictly restricts its answer key to NCERT textbooks."),
                    QuestionOption("opt_2", "Diagram captions and footnotes do not carry weight in competitive NEET evaluations.", "Incorrect: NTA regularly converts diagram footnotes into Assertion-Reason questions."),
                    QuestionOption("opt_3", "Exceptions cited in parentheses are ignored during NEET question drafting.", "Incorrect: Parenthetical exceptions are the #1 source of trap distractors in NEET.")
                ),
                correctAnswerIndex = 0,
                explanation = "NCERT Page $pageNumber establishes foundational concepts that are tested verbatim in NEET UG. Mastering each line and exception is critical for a 360/360 in NEET Biology.",
                whyOptionsAreWrong = listOf(
                    "Option A is the correct and foundational principle.",
                    "Option B is false: NTA adheres strictly to the NCERT textbook.",
                    "Option C is false: Diagram captions are heavily tested in NEET.",
                    "Option D is false: Exceptions in brackets are prime NEET question fodder."
                ),
                sourceChapterId = chapterId,
                sourceChapterName = chapterName,
                sourcePageNumber = pageNumber,
                sourcePoint = "NCERT Page $pageNumber",
                questionType = QuestionType.SINGLE_CORRECT,
                difficulty = difficulty,
                mode = PracticeMode.NCERT_STRICT,
                sourceType = SourceType.AI_GENERATED,
                verificationStatus = "AI GENERATED (GEMINI 3.5)",
                neetTip = "NEET Lens Tip: Pay special attention to words like 'all', 'none', 'except', and 'primarily' on Page $pageNumber."
            )
        }
    }

    suspend fun generate30SecondSummary(
        chapterName: String,
        pageNumber: Int,
        pageContent: String
    ): PageSummary = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackSummary(chapterName, pageNumber, pageContent)
        }

        val prompt = """
            You are SSKN AI Biology Master. Generate a 30-Second Page Summary for NEET UG aspirants.
            CHAPTER: $chapterName
            PAGE: $pageNumber
            CONTENT:
            $pageContent

            Return ONLY a valid JSON object strictly matching this schema:
            {
              "coreIdea": "1-2 sentence core idea of this page",
              "mustRememberFacts": ["fact 1", "fact 2", "fact 3"],
              "keyTerminology": ["term 1", "term 2", "term 3"],
              "trapOrConfusion": "The primary trap or confusion tested by NTA from this page"
            }
            Do not include markdown ticks, just raw JSON.
        """.trimIndent()

        try {
            val responseText = callGeminiRaw(prompt)
            val json = JSONObject(cleanJson(responseText))

            val facts = mutableListOf<String>()
            val factsArray = json.optJSONArray("mustRememberFacts")
            if (factsArray != null) {
                for (i in 0 until factsArray.length()) facts.add(factsArray.getString(i))
            }

            val terms = mutableListOf<String>()
            val termsArray = json.optJSONArray("keyTerminology")
            if (termsArray != null) {
                for (i in 0 until termsArray.length()) terms.add(termsArray.getString(i))
            }

            PageSummary(
                pageNumber = pageNumber,
                chapterName = chapterName,
                coreIdea = json.optString("coreIdea", "Core foundational concepts on NCERT Page $pageNumber."),
                mustRememberFacts = if (facts.isNotEmpty()) facts else listOf("Strict verbatim lines form direct NEET options."),
                keyTerminology = if (terms.isNotEmpty()) terms else listOf("NCERT terminology"),
                trapOrConfusion = json.optString("trapOrConfusion", "Watch for parenthetical caveats and exceptions.")
            )
        } catch (e: Exception) {
            Log.e("GeminiService", "Failed to generate 30-sec summary: ${e.message}")
            fallbackSummary(chapterName, pageNumber, pageContent)
        }
    }

    private fun fallbackSummary(
        chapterName: String,
        pageNumber: Int,
        pageContent: String
    ): PageSummary {
        return when (pageNumber) {
            125 -> PageSummary(
                pageNumber = 125,
                chapterName = chapterName,
                coreIdea = "Introduction of cell theory, historical contributions of Schleiden, Schwann, and Virchow.",
                mustRememberFacts = listOf(
                    "Schleiden (1838) was a German Botanist; Schwann (1839) was a British Zoologist.",
                    "Schwann concluded cell wall presence is a unique plant feature.",
                    "Rudolf Virchow (1855) completed cell theory: 'Omnis cellula-e cellula'."
                ),
                keyTerminology = listOf("Omnis cellula-e cellula", "Cell Theory", "Pre-existing cells"),
                trapOrConfusion = "Confusing Schwann's zoological background with Schleiden's botanical work."
            )
            128 -> PageSummary(
                pageNumber = 128,
                chapterName = chapterName,
                coreIdea = "Eukaryotic cell overview and the definition and scope of the Endomembrane System.",
                mustRememberFacts = listOf(
                    "Endomembrane system includes: ER, Golgi complex, Lysosomes, Vacuoles.",
                    "Mitochondria, Chloroplasts, and Peroxisomes are NOT part of the endomembrane system.",
                    "Plant cells have cell walls, plastids, and large central vacuole, absent in animals."
                ),
                keyTerminology = listOf("Endomembrane System", "Coordination", "Vacuoles", "Plastids"),
                trapOrConfusion = "Including Peroxisomes or Mitochondria in the endomembrane system."
            )
            else -> PageSummary(
                pageNumber = pageNumber,
                chapterName = chapterName,
                coreIdea = "High-yield NCERT concepts on Page $pageNumber forming potential 4-mark NEET questions.",
                mustRememberFacts = listOf(
                    "Pay attention to bold terms and numerical values on Page $pageNumber.",
                    "Diagram captions and footnotes carry equal testing weight.",
                    "Verbatim textbook lines are favored by NTA over external guides."
                ),
                keyTerminology = listOf("NCERT Strict", "NEET Grounded", "High Yield"),
                trapOrConfusion = "Misreading 'incorrect' or 'not true' in negative stem questions."
            )
        }
    }

    suspend fun explainWhyImportant(
        pointText: String,
        chapterName: String,
        pageNumber: Int
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackWhyImportant(pointText, pageNumber)
        }

        val prompt = """
            You are SSKN AI Biology Master.
            Explain WHY THIS POINT IS IMPORTANT for NEET UG:
            POINT: "$pointText"
            CHAPTER: $chapterName, PAGE: $pageNumber

            Provide a concise, grounded explanation covering:
            1. Biological Importance (mechanism/function)
            2. NCERT Importance (textbook emphasis/definitions)
            3. Question Potential (how NTA frames questions from this: e.g. Statement I & II, Assertion-Reason)
            Avoid claiming guaranteed appearance in NEET. Keep it concise, academic, and practical.
        """.trimIndent()

        try {
            val res = callGeminiRaw(prompt)
            cleanJson(res).replace(Regex("^\"|\"$"), "").trim()
        } catch (e: Exception) {
            fallbackWhyImportant(pointText, pageNumber)
        }
    }

    private fun fallbackWhyImportant(pointText: String, pageNumber: Int): String {
        return "• Biological Importance: Represents an essential physiological or structural mechanism.\n• NCERT Importance: Stated with precise scientific terminology on Page $pageNumber.\n• Question Potential: High probability for Statement-based or Assertion-Reason evaluations evaluating exact wording."
    }
}
