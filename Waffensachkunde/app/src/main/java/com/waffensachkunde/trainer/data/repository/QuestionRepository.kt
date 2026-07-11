package com.waffensachkunde.trainer.data.repository

import android.content.Context
import com.waffensachkunde.trainer.data.db.AppDatabase
import com.waffensachkunde.trainer.data.db.entity.ExamAttemptEntity
import com.waffensachkunde.trainer.data.db.entity.QuestionStatEntity
import com.waffensachkunde.trainer.data.model.Category
import com.waffensachkunde.trainer.data.model.Question
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class QuestionRepository(context: Context) {

    private val appContext = context.applicationContext
    private val db = AppDatabase.getInstance(appContext)
    private val statDao = db.questionStatDao()
    private val examDao = db.examAttemptDao()

    private val catalog by lazy { CatalogLoader.loadFromAssets(appContext) }

    val catalogVersion: String get() = catalog.catalogVersion
    val catalogNote: String get() = catalog.note
    val categories: List<Category> get() = catalog.categories
    val allQuestions: List<Question> get() = catalog.questions

    private val questionsById: Map<String, Question> by lazy { catalog.questions.associateBy { it.id } }

    fun questionsForCategory(categoryId: String): List<Question> =
        catalog.questions.filter { it.categoryId == categoryId }

    fun statsFlow(): Flow<Map<String, QuestionStatEntity>> =
        statDao.observeAll().map { list -> list.associateBy { it.questionId } }

    fun bookmarkedQuestionsFlow(): Flow<List<Question>> =
        statDao.observeBookmarked().map { stats ->
            stats.mapNotNull { questionsById[it.questionId] }
        }

    fun examAttemptsFlow(): Flow<List<ExamAttemptEntity>> = examDao.observeAll()

    suspend fun recordAnswer(questionId: String, correct: Boolean) {
        val existing = statDao.getById(questionId)
        val updated = (existing ?: QuestionStatEntity(questionId = questionId)).let { stat ->
            stat.copy(
                timesShown = stat.timesShown + 1,
                timesCorrect = stat.timesCorrect + if (correct) 1 else 0,
                timesWrong = stat.timesWrong + if (correct) 0 else 1,
                lastAnsweredCorrect = correct,
                lastAnsweredAt = System.currentTimeMillis()
            )
        }
        statDao.upsert(updated)
    }

    suspend fun toggleBookmark(questionId: String) {
        val existing = statDao.getById(questionId) ?: QuestionStatEntity(questionId = questionId)
        statDao.upsert(existing.copy(bookmarked = !existing.bookmarked))
    }

    suspend fun saveExamAttempt(
        startedAt: Long,
        finishedAt: Long,
        totalQuestions: Int,
        correctCount: Int,
        passed: Boolean,
        durationSeconds: Int,
        wrongQuestionIds: List<String>
    ) {
        examDao.insert(
            ExamAttemptEntity(
                startedAt = startedAt,
                finishedAt = finishedAt,
                totalQuestions = totalQuestions,
                correctCount = correctCount,
                passed = passed,
                durationSeconds = durationSeconds,
                wrongQuestionIds = wrongQuestionIds.joinToString(",")
            )
        )
    }

    suspend fun resetAllProgress() {
        statDao.clearAll()
        examDao.clearAll()
    }

    fun questionById(id: String): Question? = questionsById[id]

    suspend fun getBookmarkedQuestions(): List<Question> =
        statDao.observeBookmarked().first().mapNotNull { questionsById[it.questionId] }

    /** Questions the user most recently got wrong, or has never answered correctly. */
    suspend fun getFrequentlyWrongQuestions(): List<Question> =
        statDao.observeAll().first()
            .filter { it.timesWrong > 0 }
            .sortedByDescending { it.timesWrong - it.timesCorrect }
            .mapNotNull { questionsById[it.questionId] }

    /** Draws up to [count] questions at random from the full pool for an exam simulation. */
    fun drawExamQuestions(count: Int = EXAM_QUESTION_COUNT): List<Question> =
        catalog.questions.shuffled().take(minOf(count, catalog.questions.size))

    /**
     * Draws a round of [count] learn-mode questions from [pool]: mostly fresh/unseen questions,
     * with a share of questions the user has repeatedly answered wrong mixed back in for repetition.
     */
    suspend fun drawLearnBatch(pool: List<Question>, count: Int = LEARN_BATCH_SIZE): List<Question> {
        if (pool.size <= count) return pool.shuffled()

        val stats = statDao.observeAll().first().associateBy { it.questionId }
        fun wrongScore(q: Question): Int {
            val stat = stats[q.id] ?: return 0
            return stat.timesWrong - stat.timesCorrect
        }

        val wrongPool = pool.filter { wrongScore(it) > 0 }.sortedByDescending { wrongScore(it) }
        val wrongSlots = ((count * LEARN_REPEAT_SHARE).toInt()).coerceAtMost(wrongPool.size)
        val wrongSelection = wrongPool.take((wrongSlots * 2).coerceAtLeast(wrongSlots)).shuffled().take(wrongSlots)

        val usedIds = wrongSelection.map { it.id }.toSet()
        val freshSelection = pool.filterNot { it.id in usedIds }.shuffled().take(count - wrongSelection.size)

        val combined = (wrongSelection + freshSelection).toMutableList()
        if (combined.size < count) {
            val stillUsed = combined.map { it.id }.toSet()
            combined += pool.filterNot { it.id in stillUsed }.shuffled().take(count - combined.size)
        }
        return combined.shuffled()
    }

    companion object {
        const val EXAM_QUESTION_COUNT = 100
        const val EXAM_PASS_THRESHOLD = 75
        const val LEARN_BATCH_SIZE = 10
        const val LEARN_REPEAT_SHARE = 0.4

        @Volatile
        private var instance: QuestionRepository? = null

        fun getInstance(context: Context): QuestionRepository {
            return instance ?: synchronized(this) {
                instance ?: QuestionRepository(context).also { instance = it }
            }
        }
    }
}
