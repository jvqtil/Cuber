package dev.jvqtil.cuber.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SolveDao {

    @Insert
    suspend fun insert(solve: SolveEntity)

    @Update
    suspend fun update(solve: SolveEntity)

    @Delete
    suspend fun delete(solve: SolveEntity)

    @Query("DELETE FROM solves")
    suspend fun deleteAll()

    @Query("SELECT * FROM solves ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SolveEntity>>

    @Query("SELECT * FROM solves WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<SolveEntity?>

    @Query("UPDATE solves SET penalty = :penalty WHERE id = :id")
    suspend fun updatePenalty(
        id: Long,
        penalty: String
    )

    @Query("UPDATE solves SET comment = :comment WHERE id = :id")
    suspend fun updateComment(
        id: Long,
        comment: String?
    )
}