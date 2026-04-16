package app.krafted.wordsearch.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PuzzleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(progress: PuzzleProgress)

    @Query(
        """
        SELECT * FROM puzzle_progress
        WHERE categoryId = :categoryId AND puzzleNumber = :puzzleNumber
        """
    )
    suspend fun getProgress(categoryId: Int, puzzleNumber: Int): PuzzleProgress?

    @Query(
        """
        SELECT * FROM puzzle_progress
        WHERE categoryId = :categoryId AND bestTimeSeconds > 0
        ORDER BY bestTimeSeconds ASC
        """
    )
    suspend fun getBestTimesByCategory(categoryId: Int): List<PuzzleProgress>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM puzzle_progress
            WHERE categoryId = :categoryId AND puzzleNumber = :puzzleNumber
        )
        """
    )
    suspend fun hasProgress(categoryId: Int, puzzleNumber: Int): Boolean

    @Transaction
    suspend fun insertOrUpdate(progress: PuzzleProgress) {
        val existing = getProgress(progress.categoryId, progress.puzzleNumber)
        if (existing == null) {
            insertOrReplace(progress)
            return
        }

        val mergedBestTime = when {
            existing.bestTimeSeconds <= 0 -> progress.bestTimeSeconds
            progress.bestTimeSeconds <= 0 -> existing.bestTimeSeconds
            else -> minOf(existing.bestTimeSeconds, progress.bestTimeSeconds)
        }

        insertOrReplace(
            progress.copy(
                bestTimeSeconds = mergedBestTime,
                bestScore = maxOf(existing.bestScore, progress.bestScore),
                completedAt = progress.completedAt,
                playerName = existing.playerName.ifBlank { progress.playerName }
            )
        )
    }

    @Query(
        """
        UPDATE puzzle_progress
        SET playerName = :playerName
        WHERE categoryId = :categoryId AND puzzleNumber = :puzzleNumber
        """
    )
    suspend fun updatePlayerName(categoryId: Int, puzzleNumber: Int, playerName: String)

    @Transaction
    suspend fun isPuzzleUnlocked(categoryId: Int, puzzleNumber: Int): Boolean {
        if (puzzleNumber <= 1) {
            return true
        }

        return hasProgress(categoryId, puzzleNumber - 1)
    }
}
