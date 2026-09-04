package dev.jvqtil.cuber.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SolveEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CuberDatabase : RoomDatabase() {

    abstract fun solveDao(): SolveDao

    companion object {
        @Volatile
        private var INSTANCE: CuberDatabase? = null

        fun getInstance(context: Context): CuberDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CuberDatabase::class.java,
                    "cuber.db"
                ).build().also { INSTANCE = it }
            }
    }
}