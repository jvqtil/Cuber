package dev.jvqtil.cuber.database

import kotlinx.coroutines.flow.Flow

class SolveRepository(
    private val dao: SolveDao
) {

    val solves: Flow<List<SolveEntity>> =
        dao.observeAll()

    fun observeSolve(id: Long): Flow<SolveEntity?> =
        dao.observeById(id)

    suspend fun save(
        time: Long,
        scramble: String
    ) {
        dao.insert(
            SolveEntity(
                time = time,
                scramble = scramble,
                createdAt = System.currentTimeMillis(),
                penalty = PENALTY_OK
            )
        )
    }

    suspend fun updatePenalty(
        solve: SolveEntity,
        penalty: String
    ) {
        dao.updatePenalty(
            id = solve.id,
            penalty = penalty
        )
    }

    suspend fun updateComment(
        solve: SolveEntity,
        comment: String?
    ) {
        dao.updateComment(
            id = solve.id,
            comment = comment
        )
    }

    suspend fun delete(
        solve: SolveEntity
    ) {
        dao.delete(solve)
    }
}