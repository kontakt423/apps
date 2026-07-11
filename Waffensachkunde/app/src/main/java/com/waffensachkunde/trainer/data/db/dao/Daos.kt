package com.waffensachkunde.trainer.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.waffensachkunde.trainer.data.db.entity.ExamAttemptEntity
import com.waffensachkunde.trainer.data.db.entity.QuestionStatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionStatDao {
    @Query("SELECT * FROM question_stats")
    fun observeAll(): Flow<List<QuestionStatEntity>>

    @Query("SELECT * FROM question_stats WHERE questionId = :questionId")
    suspend fun getById(questionId: String): QuestionStatEntity?

    @Query("SELECT * FROM question_stats WHERE bookmarked = 1")
    fun observeBookmarked(): Flow<List<QuestionStatEntity>>

    @Upsert
    suspend fun upsert(stat: QuestionStatEntity)

    @Query("DELETE FROM question_stats")
    suspend fun clearAll()
}

@Dao
interface ExamAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: ExamAttemptEntity): Long

    @Query("SELECT * FROM exam_attempts ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<ExamAttemptEntity>>

    @Query("DELETE FROM exam_attempts")
    suspend fun clearAll()
}
