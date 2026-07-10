package com.waffensachkunde.trainer.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_stats")
data class QuestionStatEntity(
    @PrimaryKey val questionId: String,
    val timesShown: Int = 0,
    val timesCorrect: Int = 0,
    val timesWrong: Int = 0,
    val bookmarked: Boolean = false,
    val lastAnsweredCorrect: Boolean? = null,
    val lastAnsweredAt: Long? = null
)

@Entity(tableName = "exam_attempts")
data class ExamAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val finishedAt: Long,
    val totalQuestions: Int,
    val correctCount: Int,
    val passed: Boolean,
    val durationSeconds: Int,
    /** Comma-separated question IDs the user answered incorrectly, for review. */
    val wrongQuestionIds: String
)
