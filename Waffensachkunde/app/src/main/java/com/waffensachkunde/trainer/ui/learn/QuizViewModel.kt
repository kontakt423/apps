package com.waffensachkunde.trainer.ui.learn

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.waffensachkunde.trainer.data.model.Question
import com.waffensachkunde.trainer.data.model.ShuffledQuestion
import com.waffensachkunde.trainer.data.model.shuffled
import com.waffensachkunde.trainer.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class QuizMode { LEARN_CATEGORY, LEARN_ALL, BOOKMARKS, WRONG_ANSWERS }

data class QuizUiState(
    val loading: Boolean = true,
    val title: String = "",
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val current: ShuffledQuestion? = null,
    val selectedIndex: Int? = null,
    val answered: Boolean = false,
    val bookmarked: Boolean = false,
    val sessionCorrect: Int = 0,
    val finished: Boolean = false,
    val empty: Boolean = false
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository.getInstance(application)

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var started = false

    fun start(mode: QuizMode, categoryId: String?) {
        if (started) return
        started = true
        viewModelScope.launch {
            val questions = when (mode) {
                QuizMode.LEARN_CATEGORY -> repository.questionsForCategory(categoryId.orEmpty()).shuffled()
                QuizMode.LEARN_ALL -> repository.allQuestions.shuffled()
                QuizMode.BOOKMARKS -> repository.getBookmarkedQuestions().shuffled()
                QuizMode.WRONG_ANSWERS -> repository.getFrequentlyWrongQuestions().shuffled()
            }
            val title = when (mode) {
                QuizMode.LEARN_CATEGORY -> repository.categories.find { it.id == categoryId }?.name.orEmpty()
                QuizMode.LEARN_ALL -> "Alle Fragen"
                QuizMode.BOOKMARKS -> "Lesezeichen"
                QuizMode.WRONG_ANSWERS -> "Wiederholung: Fehler"
            }
            if (questions.isEmpty()) {
                _uiState.update { it.copy(loading = false, empty = true, title = title) }
                return@launch
            }
            _uiState.update {
                it.copy(
                    loading = false,
                    title = title,
                    questions = questions,
                    currentIndex = 0,
                    current = questions[0].shuffled(),
                    selectedIndex = null,
                    answered = false,
                    bookmarked = false
                )
            }
        }
    }

    fun selectAnswer(index: Int) {
        val state = _uiState.value
        if (state.answered || state.current == null) return
        val correct = index == state.current.correctDisplayIndex
        _uiState.update { it.copy(selectedIndex = index, answered = true, sessionCorrect = it.sessionCorrect + if (correct) 1 else 0) }
        viewModelScope.launch {
            repository.recordAnswer(state.current.question.id, correct)
        }
    }

    fun toggleBookmark() {
        val question = _uiState.value.current?.question ?: return
        _uiState.update { it.copy(bookmarked = !it.bookmarked) }
        viewModelScope.launch { repository.toggleBookmark(question.id) }
    }

    fun next() {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.questions.size) {
            _uiState.update { it.copy(finished = true) }
            return
        }
        _uiState.update {
            it.copy(
                currentIndex = nextIndex,
                current = it.questions[nextIndex].shuffled(),
                selectedIndex = null,
                answered = false,
                bookmarked = false
            )
        }
    }
}
