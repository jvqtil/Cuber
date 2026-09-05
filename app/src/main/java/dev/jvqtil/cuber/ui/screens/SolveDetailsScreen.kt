package dev.jvqtil.cuber.ui.screens

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.jvqtil.cuber.CuberViewModel
import dev.jvqtil.cuber.database.PENALTY_DNF
import dev.jvqtil.cuber.database.PENALTY_OK
import dev.jvqtil.cuber.database.PENALTY_PLUS_TWO
import dev.jvqtil.cuber.scramble.CubePreview
import dev.jvqtil.cuber.scramble.ScrambleGenerator
import dev.jvqtil.cuber.util.TimeUtils
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val SWIPE_THRESHOLD = 120f

@Composable
fun SolveDetailsScreen(
    solveId: Long,
    viewModel: CuberViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val solve by remember(solveId) {
        viewModel.observeSolve(solveId)
    }.collectAsStateWithLifecycle()

    val currentSolve = solve ?: return

    var comment by remember(currentSolve.id) {
        mutableStateOf(currentSolve.comment.orEmpty())
    }

    var dragDistance by remember {
        mutableFloatStateOf(0f)
    }

    LaunchedEffect(currentSolve.id) {
        comment = currentSolve.comment.orEmpty()
    }

    LaunchedEffect(currentSolve.id, comment) {
        val normalized = comment.trim().ifEmpty { null }

        if (normalized != currentSolve.comment) {
            delay(250.milliseconds)

            viewModel.setComment(
                solve = currentSolve,
                comment = comment
            )
        }
    }

    val scramble = remember(currentSolve.scramble) {
        ScrambleGenerator.fromText(currentSolve.scramble)
    }

    val penalties = listOf(
        PENALTY_OK,
        PENALTY_PLUS_TWO,
        PENALTY_DNF
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        dragDistance = 0f
                    },
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 0f) {
                            dragDistance += dragAmount
                        }
                    },
                    onDragEnd = {
                        if (dragDistance >= SWIPE_THRESHOLD) {
                            onBack()
                        }

                        dragDistance = 0f
                    },
                    onDragCancel = {
                        dragDistance = 0f
                    }
                )
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp
            ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = TimeUtils.formatDateTime(currentSolve.createdAt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = TimeUtils.formatSolveTime(
                    currentSolve.time,
                    currentSolve.penalty
                ),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = when (currentSolve.penalty) {
                    PENALTY_DNF -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onBackground
                }
            )
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            penalties.forEachIndexed { index, penalty ->
                SegmentedButton(
                    selected = currentSolve.penalty == penalty,
                    onClick = {
                        viewModel.setPenalty(
                            solve = currentSolve,
                            penalty = penalty
                        )
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = penalties.size
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(penalty)
                }
            }
        }

        CubePreview(
            scramble = scramble,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )

        Text(
            text = currentSolve.scramble,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Comment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Add a comment",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Button(
            onClick = {
                viewModel.deleteSolve(currentSolve)
                onDeleted()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("Delete solve")
        }
    }
}