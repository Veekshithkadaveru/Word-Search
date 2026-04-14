package app.krafted.wordsearch.game

data class GridCell(
    val row: Int,
    val col: Int
)

data class PlacedWord(
    val word: String,
    val startRow: Int,
    val startCol: Int,
    val direction: Pair<Int, Int>,
    val isFound: Boolean = false
)

data class GeneratedGrid(
    val grid: Array<CharArray>,
    val placedWords: List<PlacedWord>
)
