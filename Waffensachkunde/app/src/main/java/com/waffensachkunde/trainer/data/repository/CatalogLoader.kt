package com.waffensachkunde.trainer.data.repository

import android.content.Context
import com.waffensachkunde.trainer.data.model.Category
import com.waffensachkunde.trainer.data.model.Question
import com.waffensachkunde.trainer.data.model.QuestionCatalog
import com.waffensachkunde.trainer.data.model.QuestionType
import org.json.JSONObject

object CatalogLoader {

    fun loadFromAssets(context: Context, fileName: String = "questions.json"): QuestionCatalog {
        val text = context.assets.open(fileName).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(text)

        val categories = buildList {
            val array = root.getJSONArray("categories")
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    Category(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        section = obj.getString("section")
                    )
                )
            }
        }

        val questions = buildList {
            val array = root.getJSONArray("questions")
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val type = if (obj.getString("type") == "mc") QuestionType.MC else QuestionType.DIRECT

                val options = buildList {
                    val optionsArray = obj.optJSONArray("options")
                    if (optionsArray != null) {
                        for (j in 0 until optionsArray.length()) add(optionsArray.getString(j))
                    }
                }
                val correctIndices = buildList {
                    val correctArray = obj.optJSONArray("correctIndices")
                    if (correctArray != null) {
                        for (j in 0 until correctArray.length()) add(correctArray.getInt(j))
                    }
                }

                add(
                    Question(
                        id = obj.getString("id"),
                        categoryId = obj.getString("categoryId"),
                        type = type,
                        question = obj.getString("question"),
                        options = options,
                        correctIndices = correctIndices,
                        modelAnswer = obj.optString("modelAnswer", ""),
                        mnemonic = obj.optString("mnemonic", "")
                    )
                )
            }
        }

        return QuestionCatalog(
            catalogVersion = root.optString("catalogVersion", "unknown"),
            note = root.optString("note", ""),
            categories = categories,
            questions = questions
        )
    }
}
