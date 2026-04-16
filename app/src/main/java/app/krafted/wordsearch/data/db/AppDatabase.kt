package app.krafted.wordsearch.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PuzzleProgress::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun puzzleDao(): PuzzleDao

    companion object {
        private const val DATABASE_NAME = "wordsearch.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                // NOTE: fallbackToDestructiveMigration() wipes all user progress if the schema
                // ever changes (e.g. adding a column). This is acceptable for v1.0 since no
                // users have data yet, but a proper Migration must replace this before any
                // schema change is shipped in a future version.
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
