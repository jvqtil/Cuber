package dev.jvqtil.cuber

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jvqtil.cuber.database.PENALTY_DNF
import dev.jvqtil.cuber.database.PENALTY_OK
import dev.jvqtil.cuber.database.PENALTY_PLUS_TWO
import dev.jvqtil.cuber.database.SolveEntity
import dev.jvqtil.cuber.database.SolveRepository
import dev.jvqtil.cuber.scramble.Scramble
import dev.jvqtil.cuber.scramble.ScrambleGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class CuberUiState(
    val started: Boolean = false,
    val running: Boolean = false,
    val holding: Boolean = false,
    val elapsed: Long = 0L,
    val scramble: Scramble = ScrambleGenerator.generate()
)

class CuberViewModel(
    private val repository: SolveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CuberUiState()
    )

    val uiState: StateFlow<CuberUiState> =
        _uiState.asStateFlow()

    val solves: StateFlow<List<SolveEntity>> =
        repository.solves.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private var startedAt = 0L
    private var timerJob: Job? = null

    fun observeSolve(
        id: Long
    ): StateFlow<SolveEntity?> {
        return repository
            .observeSolve(id)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = solves.value.firstOrNull {
                    it.id == id
                }
            )
    }

    fun start() {
        if (
            _uiState.value.started ||
            _uiState.value.running
        ) {
            return
        }

        startedAt = SystemClock.elapsedRealtime()

        _uiState.update {
            it.copy(
                started = true,
                running = true,
                holding = false,
                elapsed = 0L
            )
        }

        startTimer()
    }

    fun stop() {
        if (!_uiState.value.running) {
            return
        }

        val finalTime =
            SystemClock.elapsedRealtime() - startedAt

        timerJob?.cancel()
        timerJob = null

        _uiState.update {
            it.copy(
                running = false,
                holding = false,
                elapsed = finalTime
            )
        }

        saveSolve(
            time = finalTime,
            scramble = _uiState.value.scramble.text
        )
    }

    fun reset() {
        timerJob?.cancel()
        timerJob = null

        startedAt = 0L

        _uiState.update {
            it.copy(
                started = false,
                running = false,
                holding = false,
                elapsed = 0L,
                scramble = ScrambleGenerator.generate()
            )
        }
    }

    fun setHolding(
        value: Boolean
    ) {
        if (
            value &&
            !_uiState.value.running
        ) {
            return
        }

        _uiState.update {
            it.copy(
                holding = value
            )
        }
    }

    fun setPenalty(
        solve: SolveEntity,
        penalty: String
    ) {
        if (
            penalty != PENALTY_OK &&
            penalty != PENALTY_PLUS_TWO &&
            penalty != PENALTY_DNF
        ) {
            return
        }

        viewModelScope.launch {
            repository.updatePenalty(
                solve = solve,
                penalty = penalty
            )
        }
    }

    fun setComment(
        solve: SolveEntity,
        comment: String
    ) {
        val normalized = comment
            .trim()
            .ifEmpty { null }

        viewModelScope.launch {
            repository.updateComment(
                solve = solve,
                comment = normalized
            )
        }
    }

    fun deleteSolve(
        solve: SolveEntity
    ) {
        viewModelScope.launch {
            repository.delete(solve)
        }
    }

    private fun saveSolve(
        time: Long,
        scramble: String
    ) {
        viewModelScope.launch {
            repository.save(
                time = time,
                scramble = scramble
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (_uiState.value.running) {
                _uiState.update {
                    it.copy(
                        elapsed =
                            SystemClock.elapsedRealtime() -
                                    startedAt
                    )
                }

                delay(10L.milliseconds)
            }
        }
    }

    @SuppressLint("EmptySuperCall")
    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}