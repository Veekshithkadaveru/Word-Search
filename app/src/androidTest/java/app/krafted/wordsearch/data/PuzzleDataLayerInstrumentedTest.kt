package app.krafted.wordsearch.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.krafted.wordsearch.data.db.AppDatabase
import app.krafted.wordsearch.data.db.PuzzleProgress
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PuzzleDataLayerInstrumentedTest {
    private lateinit var repository: PuzzleRepository
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        repository = PuzzleRepository(context)
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun repositoryLoadsAllCategoriesAndPuzzles() {
        val categories = (1..7).mapNotNull(repository::getCategory)

        assertEquals(7, categories.size)
        assertEquals(70, categories.sumOf { it.puzzles.size })
    }

    @Test
    fun getCategoryAndPuzzleReturnExpectedData() {
        val category = repository.getCategory(1)
        val puzzle = repository.getPuzzle(1, 1)

        assertNotNull(category)
        assertNotNull(puzzle)
        assertEquals("Card Games", category?.name)
        assertEquals(8, puzzle?.words?.size)
        assertEquals(
            listOf("JOKER", "TRUMP", "SPADE", "HEART", "FLUSH", "BLUFF", "STACK", "DEALT"),
            puzzle?.words
        )
    }

    @Test
    fun getRandomQuoteReturnsOnlyFromRequestedBucket() {
        val category = repository.getCategory(1)!!

        assertEquals(category.jokerIntro, repository.getRandomQuote(1, QuoteEvent.INTRO))

        repeat(20) {
            val foundQuote = repository.getRandomQuote(1, QuoteEvent.FOUND)
            val missQuote = repository.getRandomQuote(1, QuoteEvent.MISS)
            val completeQuote = repository.getRandomQuote(1, QuoteEvent.COMPLETE)
            val timerWarningQuote = repository.getRandomQuote(1, QuoteEvent.TIMER_WARNING)

            assertTrue(foundQuote in category.jokerFound)
            assertTrue(missQuote in category.jokerMiss)
            assertTrue(completeQuote in category.jokerComplete)
            assertTrue(timerWarningQuote in category.jokerTimerWarning)
        }
    }

    @Test
    fun unknownCategoryOrPuzzleReturnsNull() {
        assertNull(repository.getCategory(99))
        assertNull(repository.getPuzzle(1, 99))
        assertNull(repository.getPuzzle(99, 1))
        assertNull(repository.getRandomQuote(99, QuoteEvent.FOUND))
    }

    @Test
    fun firstCompletionInsertsProgress() = runBlocking {
        val progress = PuzzleProgress(
            categoryId = 1,
            puzzleNumber = 1,
            bestTimeSeconds = 120,
            bestScore = 2400,
            completedAt = 1_000L
        )

        database.puzzleDao().insertOrUpdate(progress)

        assertEquals(progress, database.puzzleDao().getProgress(1, 1))
    }

    @Test
    fun repeatedCompletionsMergeWithoutLosingBestTimeOrScore() = runBlocking {
        val dao = database.puzzleDao()

        dao.insertOrUpdate(
            PuzzleProgress(
                categoryId = 1,
                puzzleNumber = 1,
                bestTimeSeconds = 120,
                bestScore = 1500,
                completedAt = 1_000L
            )
        )
        dao.insertOrUpdate(
            PuzzleProgress(
                categoryId = 1,
                puzzleNumber = 1,
                bestTimeSeconds = 150,
                bestScore = 1800,
                completedAt = 2_000L
            )
        )

        var merged = dao.getProgress(1, 1)
        assertEquals(120, merged?.bestTimeSeconds)
        assertEquals(1800, merged?.bestScore)
        assertEquals(2_000L, merged?.completedAt)

        dao.insertOrUpdate(
            PuzzleProgress(
                categoryId = 1,
                puzzleNumber = 1,
                bestTimeSeconds = 90,
                bestScore = 1700,
                completedAt = 3_000L
            )
        )

        merged = dao.getProgress(1, 1)
        assertEquals(90, merged?.bestTimeSeconds)
        assertEquals(1800, merged?.bestScore)
        assertEquals(3_000L, merged?.completedAt)
    }

    @Test
    fun bestTimesIgnoreZeroAndSortAscending() = runBlocking {
        val dao = database.puzzleDao()

        dao.insertOrUpdate(PuzzleProgress(1, 1, 0, 1000, 1_000L))
        dao.insertOrUpdate(PuzzleProgress(1, 2, 95, 1200, 2_000L))
        dao.insertOrUpdate(PuzzleProgress(1, 3, 60, 1500, 3_000L))

        val bestTimes = dao.getBestTimesByCategory(1)

        assertEquals(listOf(3, 2), bestTimes.map { it.puzzleNumber })
        assertEquals(listOf(60, 95), bestTimes.map { it.bestTimeSeconds })
    }

    @Test
    fun unlockLogicFollowsSequentialProgression() = runBlocking {
        val dao = database.puzzleDao()

        assertTrue(dao.isPuzzleUnlocked(1, 1))
        assertFalse(dao.isPuzzleUnlocked(1, 2))

        dao.insertOrUpdate(PuzzleProgress(1, 1, 120, 1500, 1_000L))

        assertTrue(dao.hasProgress(1, 1))
        assertTrue(dao.isPuzzleUnlocked(1, 2))
        assertFalse(dao.isPuzzleUnlocked(1, 3))
    }
}
