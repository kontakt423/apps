package com.waffensachkunde.trainer.data.model

data class Category(
    val id: String,
    val name: String,
    val section: String
)

data class Question(
    val id: String,
    val categoryId: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val reference: String
)

data class QuestionCatalog(
    val catalogVersion: String,
    val note: String,
    val categories: List<Category>,
    val questions: List<Question>
)

/** A question with its answer options shuffled for display, tracking the new correct index. */
data class ShuffledQuestion(
    val question: Question,
    val displayOptions: List<String>,
    val correctDisplayIndex: Int
)

fun Question.shuffled(): ShuffledQuestion {
    val indices = options.indices.shuffled()
    val displayOptions = indices.map { options[it] }
    val correctDisplayIndex = indices.indexOf(correctIndex)
    return ShuffledQuestion(this, displayOptions, correctDisplayIndex)
}
