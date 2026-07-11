package com.waffensachkunde.trainer.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.waffensachkunde.trainer.data.db.entity.ExamAttemptEntity
import com.waffensachkunde.trainer.data.repository.QuestionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryProgress(
    val categoryId: String,
    val name: String,
    val section: String,
    val totalCount: Int,
    val answeredCount: Int,
    val correctRate: Int
)

data class StatsUiState(
    val categoryProgress: List<CategoryProgress> = emptyList(),
    val examHistory: List<ExamAttemptEntity> = emptyList()
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository.getInstance(application)

    val uiState: StateFlow<StatsUiState> = combine(
        repository.statsFlow(),
        repository.examAttemptsFlow()
    ) { stats, attempts ->
        val progress = repository.categories.map { category ->
            val questions = repository.questionsForCategory(category.id)
            val relevantStats = questions.mapNotNull { stats[it.id] }
            val answered = relevantStats.count { it.timesShown > 0 }
            val correct = relevantStats.sumOf { it.timesCorrect }
            val shown = relevantStats.sumOf { it.timesShown }
            val rate = if (shown > 0) (correct * 100) / shown else 0
            CategoryProgress(
                categoryId = category.id,
                name = category.name,
                section = category.section,
                totalCount = questions.size,
                answeredCount = answered,
                correctRate = rate
            )
        }
        StatsUiState(categoryProgress = progress, examHistory = attempts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    fun resetProgress() {
        viewModelScope.launch { repository.resetAllProgress() }
    }
}
