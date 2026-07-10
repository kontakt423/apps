package com.waffensachkunde.trainer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.waffensachkunde.trainer.data.db.dao.ExamAttemptDao
import com.waffensachkunde.trainer.data.db.dao.QuestionStatDao
import com.waffensachkunde.trainer.data.db.entity.ExamAttemptEntity
import com.waffensachkunde.trainer.data.db.entity.QuestionStatEntity

@Database(
    entities = [QuestionStatEntity::class, ExamAttemptEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionStatDao(): QuestionStatDao
    abstract fun examAttemptDao(): ExamAttemptDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "waffensachkunde.db"
                ).build().also { instance = it }
            }
        }
    }
}
