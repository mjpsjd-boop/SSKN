package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val type: String, // "NCERT", "PYQ", "MCQ"
    val title: String,
    val subtitle: String,
    val targetPage: Int,
    val chapterId: String,
    val timestamp: Long
)

@Entity(tableName = "mistakes")
data class MistakeEntity(
    @PrimaryKey val id: String,
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
    val timestamp: Long
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val lastClass: Int,
    val lastChapterId: String,
    val lastChapterName: String,
    val lastPageNumber: Int,
    val readPagesJson: String, // comma separated page IDs
    val practicedPagesJson: String,
    val completedQuestionsCount: Int,
    val correctQuestionsCount: Int
)

@Entity(tableName = "ai_cache")
data class AIAnalysisCacheEntity(
    @PrimaryKey val pageId: String,
    val mustRememberJson: String,
    val neetFocus: String,
    val commonConfusionsJson: String,
    val keyNCERTFactsJson: String,
    val questionPotential: String,
    val diagramFocus: String?,
    val timestamp: Long
)
