package com.waffensachkunde.trainer.data.model

enum class QuestionType { MC, DIRECT }

data class Category(
    val id: String,
    val name: String,
    val section: String
)

data class Question(
    val id: String,
    val categoryId: String,
    val type: QuestionType,
    val question: String,
    val options: List<String> = emptyList(),
    val correctIndices: List<Int> = emptyList(),
    val modelAnswer: String = "",
    val mnemonic: String = ""
)

data class QuestionCatalog(
    val catalogVersion: String,
    val note: String,
    val categories: List<Category>,
    val questions: List<Question>
)

/** A question with its MC answer options shuffled for display. Empty/unused for DIRECT questions. */
data class ShuffledQuestion(
    val question: Question,
    val displayOptions: List<String>,
    val correctDisplayIndices: Set<Int>
)

fun Question.shuffledForDisplay(): ShuffledQuestion {
    if (type != QuestionType.MC) {
        return ShuffledQuestion(this, emptyList(), emptySet())
    }
    val order = options.indices.shuffled()
    val displayOptions = order.map { options[it] }
    val correctSet = correctIndices.toSet()
    val correctDisplayIndices = order.withIndex()
        .filter { (_, originalIndex) -> originalIndex in correctSet }
        .map { (displayIndex, _) -> displayIndex }
        .toSet()
    return ShuffledQuestion(this, displayOptions, correctDisplayIndices)
}
