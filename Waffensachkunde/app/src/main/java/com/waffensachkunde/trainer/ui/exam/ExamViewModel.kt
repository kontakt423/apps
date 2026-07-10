package com.waffensachkunde.trainer.ui.exam

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.waffensachkunde.trainer.data.model.Question
import com.waffensachkunde.trainer.data.model.ShuffledQuestion
import com.waffensachkunde.trainer.data.model.shuffled
import com.waffensachkunde.trainer.data.repository.QuestionRepository
import kotlin.math.ceil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ExamPhase { INTRO, RUNNING, FINISHED }

data class ExamUiState(
    val phase: ExamPhase = ExamPhase.INTRO,
    val questions: List<ShuffledQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val answers: Map<Int, Int> = emptyMap(),
    val timeRemainingSeconds: Int = ExamViewModel.EXAM_DURATION_SECONDS,
    val correctCount: Int = 0,
    val passThreshold: Int = 0,
    val passed: Boolean = false,
    val wrongQuestions: List<Question> = emptyList(),
    val unansweredCount: Int = 0
)

class ExamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository.getInstance(application)

    private val _uiState = MutableStateFlow(ExamUiState())
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var startedAt: Long = 0L

    fun startExam() {
        val drawn = repository.drawExamQuestions(QuestionRepository.EXAM_QUESTION_COUNT).map { it.shuffled() }
        val threshold = ceil(drawn.size * 0.75).toInt()
        startedAt = System.currentTimeMillis()
        _uiState.value = ExamUiState(
            phase = ExamPhase.RUNNING,
            questions = drawn,
            currentIndex = 0,
            answers = emptyMap(),
            timeRemainingSeconds = EXAM_DURATION_SECONDS,
            passThreshold = threshold
        )
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemainingSeconds > 0 && _uiState.value.phase == ExamPhase.RUNNING) {
                delay(1000)
                _uiState.update { it.copy(timeRemainingSeconds = (it.timeRemainingSeconds - 1).coerceAtLeast(0)) }
            }
            if (_uiState.value.phase == ExamPhase.RUNNING && _uiState.value.timeRemainingSeconds <= 0) {
                submit()
            }
        }
    }

    fun selectAnswer(displayIndex: Int) {
        val index = _uiState.value.currentIndex
        _uiState.update { it.copy(answers = it.answers + (index to displayIndex)) }
    }

    fun goToQuestion(index: Int) {
        val size = _uiState.value.questions.size
        if (index in 0 until size) {
            _uiState.update { it.copy(currentIndex = index) }
        }
    }

    fun next() = goToQuestion(_uiState.value.currentIndex + 1)
    fun previous() = goToQuestion(_uiState.value.currentIndex - 1)

    fun submit() {
        val state = _uiState.value
        if (state.phase != ExamPhase.RUNNING) return
        timerJob?.cancel()
        var correct = 0
        val wrong = mutableListOf<Question>()
        state.questions.forEachIndexed { index, shuffledQuestion ->
            val selected = state.answers[index]
            if (selected != null && selected == shuffledQuestion.correctDisplayIndex) {
                correct++
            } else {
                wrong.add(shuffledQuestion.question)
            }
        }
        val unanswered = state.questions.size - state.answers.size
        val passed = correct >= state.passThreshold
        val finishedAt = System.currentTimeMillis()
        val durationSeconds = ((finishedAt - startedAt) / 1000).toInt()
        _uiState.update {
            it.copy(
                phase = ExamPhase.FINISHED,
                correctCount = correct,
                passed = passed,
                wrongQuestions = wrong,
                unansweredCount = unanswered
            )
        }
        viewModelScope.launch {
            repository.saveExamAttempt(
                startedAt = startedAt,
                finishedAt = finishedAt,
                totalQuestions = state.questions.size,
                correctCount = correct,
                passed = passed,
                durationSeconds = durationSeconds,
                wrongQuestionIds = wrong.map { it.id }
            )
        }
    }

    fun reset() {
        timerJob?.cancel()
        _uiState.value = ExamUiState()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        const val EXAM_DURATION_SECONDS = 60 * 60
    }
}
