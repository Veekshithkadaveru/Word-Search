package app.krafted.wordsearch.data

import android.content.Context
import com.google.gson.Gson

data class PuzzleCatalog(
    val categories: List<PuzzleCategory>
)

data class PuzzleCategory(
    val id: Int,
    val name: String,
    val symbol: String,
    val background: String,
    val accentColor: String,
    val jokerIntro: String,
    val jokerFound: List<String>,
    val jokerMiss: List<String>,
    val jokerComplete: List<String>,
    val jokerTimerWarning: List<String> = emptyList(),
    val puzzles: List<PuzzleDefinition>
)

data class PuzzleDefinition(
    val id: Int,
    val words: List<String>
)

enum class QuoteEvent {
    INTRO,
    FOUND,
    MISS,
    COMPLETE,
    TIMER_WARNING
}

class PuzzleRepository(
    context: Context,
    private val gson: Gson = Gson()
) {
    private val appContext = context.applicationContext

    private val catalog: PuzzleCatalog by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        appContext.assets.open(PUZZLES_ASSET_NAME).use { inputStream ->
            inputStream.reader(Charsets.UTF_8).use { reader ->
                gson.fromJson(reader, PuzzleCatalog::class.java)
            }
        }
    }

    fun getAllCategories(): List<PuzzleCategory> = catalog.categories

    fun getCategory(id: Int): PuzzleCategory? =
        catalog.categories.firstOrNull { it.id == id }

    fun getPuzzle(categoryId: Int, puzzleNumber: Int): PuzzleDefinition? =
        getCategory(categoryId)?.puzzles?.firstOrNull { it.id == puzzleNumber }

    fun getRandomQuote(categoryId: Int, event: QuoteEvent): String? {
        val category = getCategory(categoryId) ?: return null
        return when (event) {
            QuoteEvent.INTRO -> category.jokerIntro
            QuoteEvent.FOUND -> category.jokerFound.randomOrNull()
            QuoteEvent.MISS -> category.jokerMiss.randomOrNull()
            QuoteEvent.COMPLETE -> category.jokerComplete.randomOrNull()
            QuoteEvent.TIMER_WARNING -> category.jokerTimerWarning.randomOrNull()
        }
    }

    private companion object {
        const val PUZZLES_ASSET_NAME = "puzzles.json"
    }
}
