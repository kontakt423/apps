package com.waffensachkunde.trainer.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.waffensachkunde.trainer.data.repository.QuestionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val totalQuestions: Int = 0,
    val answeredQuestions: Int = 0,
    val correctRate: Int = 0,
    val examAttemptCount: Int = 0,
    val lastExamPassed: Boolean? = null,
    val lastExamScore: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository.getInstance(application)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.statsFlow(),
        repository.examAttemptsFlow()
    ) { stats, attempts ->
        val answered = stats.values.count { it.timesShown > 0 }
        val totalCorrect = stats.values.sumOf { it.timesCorrect }
        val totalShown = stats.values.sumOf { it.timesShown }
        val rate = if (totalShown > 0) (totalCorrect * 100) / totalShown else 0
        val last = attempts.firstOrNull()
        HomeUiState(
            totalQuestions = repository.allQuestions.size,
            answeredQuestions = answered,
            correctRate = rate,
            examAttemptCount = attempts.size,
            lastExamPassed = last?.passed,
            lastExamScore = last?.let { "${it.correctCount}/${it.totalQuestions}" }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
