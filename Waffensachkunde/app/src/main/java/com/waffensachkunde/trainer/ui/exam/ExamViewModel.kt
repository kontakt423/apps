package com.waffensachkunde.trainer.ui.exam

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.waffensachkunde.trainer.data.model.Question
import com.waffensachkunde.trainer.data.model.QuestionType
import com.waffensachkunde.trainer.data.model.ShuffledQuestion
import com.waffensachkunde.trainer.data.model.shuffledForDisplay
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

data class ExamAnswerState(
    val selectedIndices: Set<Int> = emptySet(),
    val submitted: Boolean = false,
    val selfCorrect: Boolean? = null,
    val mnemonicRevealed: Boolean = false
)

data class ExamUiState(
    val phase: ExamPhase = ExamPhase.INTRO,
    val questions: List<ShuffledQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val answers: Map<Int, ExamAnswerState> = emptyMap(),
    val timeRemainingSeconds: Int = ExamViewModel.EXAM_DURATION_SECONDS,
    val correctCount: Int = 0,
    val passThreshold: Int = 0,
    val passed: Boolean = false,
    val wrongQuestions: List<Question> = emptyList(),
    val unresolvedCount: Int = 0
)

class ExamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository.getInstance(application)

    private val _uiState = MutableStateFlow(ExamUiState())
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var startedAt: Long = 0L

    fun startExam() {
        val drawn = repository.drawExamQuestions(QuestionRepository.EXAM_QUESTION_COUNT).map { it.shuffledForDisplay() }
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

    private fun currentAnswer(): ExamAnswerState =
        _uiState.value.answers[_uiState.value.currentIndex] ?: ExamAnswerState()

    private fun updateCurrentAnswer(transform: (ExamAnswerState) -> ExamAnswerState) {
        val index = _uiState.value.currentIndex
        _uiState.update {
            val updated = transform(it.answers[index] ?: ExamAnswerState())
            it.copy(answers = it.answers + (index to updated))
        }
    }

    fun toggleMcOption(displayIndex: Int) {
        val current = currentAnswer()
        if (current.submitted) return
        updateCurrentAnswer {
            val newSelection = if (displayIndex in it.selectedIndices) it.selectedIndices - displayIndex else it.selectedIndices + displayIndex
            it.copy(selectedIndices = newSelection)
        }
    }

    fun submitMcCurrent() {
        val current = currentAnswer()
        if (current.submitted || current.selectedIndices.isEmpty()) return
        updateCurrentAnswer { it.copy(submitted = true) }
    }

    fun revealDirectCurrent() {
        val current = currentAnswer()
        if (current.submitted) return
        updateCurrentAnswer { it.copy(submitted = true) }
    }

    fun selfAssessCurrent(correct: Boolean) {
        val current = currentAnswer()
        if (!current.submitted || current.selfCorrect != null) return
        updateCurrentAnswer { it.copy(selfCorrect = correct) }
    }

    fun toggleMnemonicCurrent() {
        updateCurrentAnswer { it.copy(mnemonicRevealed = !it.mnemonicRevealed) }
    }

    fun goToQuestion(index: Int) {
        val size = _uiState.value.questions.size
        if (index in 0 until size) {
            _uiState.update { it.copy(currentIndex = index) }
        }
    }

    fun next() = goToQuestion(_uiState.value.currentIndex + 1)
    fun previous() = goToQuestion(_uiState.value.currentIndex - 1)

    fun unresolvedCount(): Int {
        val state = _uiState.value
        return state.questions.indices.count { idx ->
            val ans = state.answers[idx]
            val q = state.questions[idx].question
            when (q.type) {
                QuestionType.MC -> ans?.submitted != true
                QuestionType.DIRECT -> ans?.selfCorrect == null
            }
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.phase != ExamPhase.RUNNING) return
        timerJob?.cancel()

        var correct = 0
        val wrong = mutableListOf<Question>()
        var unresolved = 0
        state.questions.forEachIndexed { index, shuffledQuestion ->
            val ans = state.answers[index]
            val isCorrect = when (shuffledQuestion.question.type) {
                QuestionType.MC -> ans?.submitted == true && ans.selectedIndices == shuffledQuestion.correctDisplayIndices
                QuestionType.DIRECT -> ans?.selfCorrect == true
            }
            val resolved = when (shuffledQuestion.question.type) {
                QuestionType.MC -> ans?.submitted == true
                QuestionType.DIRECT -> ans?.selfCorrect != null
            }
            if (!resolved) unresolved++
            if (isCorrect) correct++ else wrong.add(shuffledQuestion.question)
        }
        val passed = correct >= state.passThreshold
        val finishedAt = System.currentTimeMillis()
        val durationSeconds = ((finishedAt - startedAt) / 1000).toInt()
        _uiState.update {
            it.copy(
                phase = ExamPhase.FINISHED,
                correctCount = correct,
                passed = passed,
                wrongQuestions = wrong,
                unresolvedCount = unresolved
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
