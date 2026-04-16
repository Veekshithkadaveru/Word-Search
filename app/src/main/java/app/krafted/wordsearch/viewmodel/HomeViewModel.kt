package app.krafted.wordsearch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.krafted.wordsearch.data.PuzzleRepository
import app.krafted.wordsearch.data.db.PuzzleDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryProgress(
    val categoryId: Int,
    val name: String,
    val symbol: String,
    val background: String,
    val accentColorHex: String,
    val totalPuzzles: Int,
    val completedCount: Int,
    val isFullyComplete: Boolean
)

data class PuzzleStatus(
    val puzzleNumber: Int,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val bestTimeSeconds: Int,
    val bestScore: Int
)

data class HomeUiState(
    val categories: List<CategoryProgress> = emptyList(),
    val isLoading: Boolean = true
)

data class PuzzleSelectUiState(
    val categoryId: Int = 0,
    val categoryName: String = "",
    val accentColorHex: String = "",
    val background: String = "",
    val puzzles: List<PuzzleStatus> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val repository: PuzzleRepository,
    private val dao: PuzzleDao
) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _puzzleSelectUiState = MutableStateFlow(PuzzleSelectUiState())
    val puzzleSelectUiState: StateFlow<PuzzleSelectUiState> = _puzzleSelectUiState.asStateFlow()

    fun loadHome() {
        viewModelScope.launch(Dispatchers.IO) {
            val categories = repository.getAllCategories().map { category ->
                var completed = 0
                category.puzzles.forEach { puzzle ->
                    if (dao.hasProgress(category.id, puzzle.id)) completed++
                }
                CategoryProgress(
                    categoryId = category.id,
                    name = category.name,
                    symbol = category.symbol,
                    background = category.background,
                    accentColorHex = category.accentColor,
                    totalPuzzles = category.puzzles.size,
                    completedCount = completed,
                    isFullyComplete = category.puzzles.isNotEmpty() && completed == category.puzzles.size
                )
            }
            _homeUiState.value = HomeUiState(categories = categories, isLoading = false)
        }
    }

    fun loadCategory(categoryId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val category = repository.getCategory(categoryId)
            if (category == null) {
                _puzzleSelectUiState.value = PuzzleSelectUiState(isLoading = false)
                return@launch
            }
            val statuses = category.puzzles.sortedBy { it.id }.map { puzzle ->
                val unlocked = dao.isPuzzleUnlocked(categoryId, puzzle.id)
                val completed = dao.hasProgress(categoryId, puzzle.id)
                val progress = dao.getProgress(categoryId, puzzle.id)
                PuzzleStatus(
                    puzzleNumber = puzzle.id,
                    isUnlocked = unlocked,
                    isCompleted = completed,
                    bestTimeSeconds = progress?.bestTimeSeconds ?: 0,
                    bestScore = progress?.bestScore ?: 0
                )
            }
            _puzzleSelectUiState.value = PuzzleSelectUiState(
                categoryId = categoryId,
                categoryName = category.name,
                accentColorHex = category.accentColor,
                background = category.background,
                puzzles = statuses,
                isLoading = false
            )
        }
    }

    fun refresh() {
        loadHome()
        val currentCategory = _puzzleSelectUiState.value.categoryId
        if (currentCategory != 0) loadCategory(currentCategory)
    }

    companion object {
        fun factory(repository: PuzzleRepository, dao: PuzzleDao): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { HomeViewModel(repository, dao) }
            }
    }
}
