package app.krafted.wordsearch.game

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.krafted.wordsearch.data.PuzzleRepository
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WordSearchEngineInstrumentedTest {
    private lateinit var repository: PuzzleRepository
    private val engine = WordSearchEngine()

    @Before
    fun setUp() {
        repository = PuzzleRepository(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test
    fun allPuzzlesGenerateWordsAtTheirRecordedCoordinates() {
        (1..7).forEach { categoryId ->
            val category = repository.getCategory(categoryId)
            assertNotNull(category)

            category!!.puzzles.forEach { puzzle ->
                val generated = engine.generateGrid(
                    words = puzzle.words,
                    seed = (category.id * 1_000L) + puzzle.id
                )

                assertEquals(12, generated.grid.size)
                assertTrue(generated.grid.all { it.size == 12 })
                assertEquals(puzzle.words.size, generated.placedWords.size)
                assertEquals(
                    puzzle.words.map { it.uppercase(Locale.ROOT) },
                    generated.placedWords.map { it.word }
                )

                generated.placedWords.forEach { placedWord ->
                    val (rowStep, colStep) = placedWord.direction

                    repeat(placedWord.word.length) { index ->
                        val row = placedWord.startRow + (rowStep * index)
                        val col = placedWord.startCol + (colStep * index)
                        assertTrue(row in 0 until 12)
                        assertTrue(col in 0 until 12)
                    }

                    assertEquals(placedWord.word, generated.grid.readWord(placedWord))
                }
            }
        }
    }

    private fun Array<CharArray>.readWord(placedWord: PlacedWord): String {
        val (rowStep, colStep) = placedWord.direction
        return buildString {
            repeat(placedWord.word.length) { index ->
                append(this@readWord[placedWord.startRow + (rowStep * index)][placedWord.startCol + (colStep * index)])
            }
        }
    }
}
