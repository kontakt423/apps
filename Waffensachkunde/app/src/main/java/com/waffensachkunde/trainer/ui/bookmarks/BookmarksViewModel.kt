package com.waffensachkunde.trainer.ui.bookmarks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.waffensachkunde.trainer.data.model.Question
import com.waffensachkunde.trainer.data.repository.QuestionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BookmarksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository.getInstance(application)

    val bookmarks: StateFlow<List<Question>> = repository.bookmarkedQuestionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
