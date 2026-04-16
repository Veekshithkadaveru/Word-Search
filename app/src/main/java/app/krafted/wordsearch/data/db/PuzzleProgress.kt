package app.krafted.wordsearch.data.db

import androidx.room.Entity

@Entity(
    tableName = "puzzle_progress",
    primaryKeys = ["categoryId", "puzzleNumber"]
)
data class PuzzleProgress(
    val categoryId: Int,
    val puzzleNumber: Int,
    val bestTimeSeconds: Int,
    val bestScore: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val playerName: String = ""
)
