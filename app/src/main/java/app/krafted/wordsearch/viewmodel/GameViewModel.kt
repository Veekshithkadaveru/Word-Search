package app.krafted.wordsearch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.krafted.wordsearch.data.PuzzleRepository
import app.krafted.wordsearch.data.QuoteEvent
import app.krafted.wordsearch.data.db.PuzzleDao
import app.krafted.wordsearch.data.db.PuzzleProgress
import app.krafted.wordsearch.game.GridCell
import app.krafted.wordsearch.game.PlacedWord
import app.krafted.wordsearch.game.SelectionValidator
import app.krafted.wordsearch.game.WordSearchEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val GRID_SIZE = 12
private const val TIMER_WARNING_SECONDS = 30
private const val WRONG_FLASH_MS = 200L

data class GameUiState(
    val grid: Array<CharArray> = Array(GRID_SIZE) { CharArray(GRID_SIZE) { ' ' } },
    val placedWords: List<PlacedWord> = emptyList(),
    val foundCells: List<GridCell> = emptyList(),
    val selectedCells: List<GridCell> = emptyList(),
    val isTimedMode: Boolean = false,
    val timeElapsedSeconds: Int = 0,
    val jokerQuote: String = "",
    val isComplete: Boolean = false,
    val isError: Boolean = false,
    val score: Int = 0,
    val isNewBest: Boolean = false,
    val categoryId: Int = 0,
    val puzzleNumber: Int = 0,
    val categoryName: String = "",
    val accentColorHex: String = "#B71C1C",
    val backgroundDrawable: String = "",
    val isWrongFlash: Boolean = false,
    val timerWarningTriggered: Boolean = false
) {
    val remainingWords: List<PlacedWord> get() = placedWords.filter { !it.isFound }
    val foundWords: List<PlacedWord> get() = placedWords.filter { it.isFound }
}

class GameViewModel(
    private val repository: PuzzleRepository,
    private val dao: PuzzleDao,
    private val engine: WordSearchEngine = WordSearchEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var wrongFlashJob: Job? = null

    fun startPuzzle(categoryId: Int, puzzleNumber: Int, isTimedMode: Boolean) {
        timerJob?.cancel()
        wrongFlashJob?.cancel()
        val category = repository.getCategory(categoryId)
        val puzzle = repository.getPuzzle(categoryId, puzzleNumber)
        if (category == null || puzzle == null) {
            _uiState.value = GameUiState(isError = true)
            return
        }
        val seed = (categoryId.toLong() shl 16) or puzzleNumber.toLong()
        val generated = engine.generateGrid(words = puzzle.words, seed = seed)
        _uiState.value = GameUiState(
            grid = generated.grid,
            placedWords = generated.placedWords,
            isTimedMode = isTimedMode,
            jokerQuote = category.jokerIntro,
            categoryId = categoryId,
            puzzleNumber = puzzleNumber,
            categoryName = category.name,
            accentColorHex = category.accentColor,
            backgroundDrawable = category.background
        )
        if (isTimedMode) startTimer()
    }

    fun onDragStart(row: Int, col: Int) {
        _uiState.update {
            it.copy(selectedCells = listOf(GridCell(row, col)), isWrongFlash = false)
        }
    }

    fun onDragMove(row: Int, col: Int) {
        val current = _uiState.value.selectedCells
        if (current.isEmpty()) return
        val candidate = SelectionValidator.getCellsBetween(current.first(), GridCell(row, col))
        if (candidate.isNotEmpty()) {
            _uiState.update { it.copy(selectedCells = candidate) }
        }
    }

    fun onDragEnd() {
        val state = _uiState.value
        val selected = state.selectedCells
        if (selected.isEmpty()) return
        val selectedWord = buildString {
            selected.forEach { append(state.grid[it.row][it.col]) }
        }
        val matched = state.placedWords.firstOrNull {
            !it.isFound && (it.word == selectedWord || it.word == selectedWord.reversed())
        }
        if (matched != null) {
            val newPlaced = state.placedWords.map {
                if (it.word == matched.word && !it.isFound) it.copy(isFound = true) else it
            }
            val newFoundCells = (state.foundCells + selected).distinct()
            val quote =
                repository.getRandomQuote(state.categoryId, QuoteEvent.FOUND) ?: state.jokerQuote
            val allFound = newPlaced.all { it.isFound }
            _uiState.update {
                it.copy(
                    placedWords = newPlaced,
                    foundCells = newFoundCells,
                    selectedCells = emptyList(),
                    jokerQuote = quote
                )
            }
            if (allFound) viewModelScope.launch { onPuzzleComplete() }
        } else {
            val quote =
                repository.getRandomQuote(state.categoryId, QuoteEvent.MISS) ?: state.jokerQuote
            _uiState.update { it.copy(isWrongFlash = true, jokerQuote = quote) }
            wrongFlashJob?.cancel()
            wrongFlashJob = viewModelScope.launch {
                delay(WRONG_FLASH_MS)
                _uiState.update { it.copy(isWrongFlash = false, selectedCells = emptyList()) }
            }
        }
    }

    private suspend fun onPuzzleComplete() {
        timerJob?.cancel()
        val state = _uiState.value
        val base = state.placedWords.size * 100
        val timeBonus = if (state.isTimedMode) maxOf(0, 300 - state.timeElapsedSeconds) * 10 else 0
        val multiplier = 1.0 + (state.puzzleNumber * 0.1)
        val score = ((base + timeBonus) * multiplier).toInt()
        val existing = dao.getProgress(state.categoryId, state.puzzleNumber)
        val isNewBest = existing == null || score > existing.bestScore
        dao.insertOrUpdate(
            PuzzleProgress(
                categoryId = state.categoryId,
                puzzleNumber = state.puzzleNumber,
                bestTimeSeconds = if (state.isTimedMode) state.timeElapsedSeconds else 0,
                bestScore = score,
                completedAt = System.currentTimeMillis()
            )
        )
        val quote =
            repository.getRandomQuote(state.categoryId, QuoteEvent.COMPLETE) ?: state.jokerQuote
        _uiState.update {
            it.copy(
                isComplete = true,
                score = score,
                isNewBest = isNewBest,
                selectedCells = emptyList(),
                jokerQuote = quote
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && !_uiState.value.isComplete) {
                delay(1000)
                if (_uiState.value.isComplete) break
                _uiState.update { it.copy(timeElapsedSeconds = it.timeElapsedSeconds + 1) }
                val current = _uiState.value
                if (!current.timerWarningTriggered && current.timeElapsedSeconds == TIMER_WARNING_SECONDS) {
                    val quote =
                        repository.getRandomQuote(current.categoryId, QuoteEvent.TIMER_WARNING)
                            ?: current.jokerQuote
                    _uiState.update { it.copy(timerWarningTriggered = true, jokerQuote = quote) }
                }
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        wrongFlashJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun factory(repository: PuzzleRepository, dao: PuzzleDao): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { GameViewModel(repository, dao) }
            }
    }
}
