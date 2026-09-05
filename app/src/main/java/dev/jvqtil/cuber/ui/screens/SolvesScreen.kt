package dev.jvqtil.cuber.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.jvqtil.cuber.CuberViewModel
import dev.jvqtil.cuber.database.PENALTY_DNF
import dev.jvqtil.cuber.database.PENALTY_PLUS_TWO
import dev.jvqtil.cuber.database.SolveEntity
import dev.jvqtil.cuber.ui.components.SolveRow
import dev.jvqtil.cuber.ui.components.SolveTimeChart
import dev.jvqtil.cuber.util.TimeUtils
import java.util.Calendar

@Composable
fun SolvesScreen(
    viewModel: CuberViewModel,
    onOpenSolve: (Long) -> Unit
) {
    val solves by viewModel.solves.collectAsStateWithLifecycle()
    val historyState = rememberLazyListState()

    val validSolves = remember(solves) {
        solves.filter { it.penalty != PENALTY_DNF }
    }

    val best = validSolves.minOfOrNull(::effectiveTime)

    val average = remember(validSolves) {
        validSolves
            .takeIf { it.isNotEmpty() }
            ?.map(::effectiveTime)
            ?.average()
            ?.toLong()
    }

    val averages = remember(solves) {
        listOf(
            "Ao5" to calculateAo(solves, 5),
            "Ao12" to calculateAo(solves, 12),
            "Ao25" to calculateAo(solves, 25),
            "Ao50" to calculateAo(solves, 50),
            "Ao100" to calculateAo(solves, 100)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    top = 12.dp,
                    end = 20.dp,
                    bottom = 8.dp
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Solves",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "${solves.size} solves",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PrimaryStats(
                best = best,
                average = average,
            )

            AverageStats(averages)

            Text(
                text = "Solve time chart",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SolveTimeChart(solves = solves)

            Text(
                text = "History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                state = historyState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = 20.dp,
                    vertical = 4.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (solves.isEmpty()) {
                    item {
                        Text(
                            text = "No solves yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                } else {
                    groupSolvesByDay(solves).forEach { group ->
                        item {
                            Text(
                                text = group.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    top = 12.dp,
                                    bottom = 2.dp
                                )
                            )
                        }

                        items(
                            items = group.solves,
                            key = SolveEntity::id
                        ) { solve ->
                            SolveRow(
                                solve = solve,
                                onClick = {
                                    onOpenSolve(solve.id)
                                }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                Color.Transparent
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun PrimaryStats(
    best: Long?,
    average: Long?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            title = "Best",
            value = TimeUtils.formatNullable(best),
            modifier = Modifier.weight(1f),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
            valueColor = MaterialTheme.colorScheme.onPrimaryContainer
        )

        StatCard(
            title = "Average",
            value = TimeUtils.formatNullable(average),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AverageStats(
    averages: List<Pair<String, Long?>>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        averages.forEach { (title, value) ->
            StatCard(
                title = title,
                value = TimeUtils.formatNullable(value),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    titleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 15.dp
                ),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = titleColor
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
    }
}

private data class DayGroup(
    val title: String,
    val solves: List<SolveEntity>
)

private fun groupSolvesByDay(
    solves: List<SolveEntity>
): List<DayGroup> {
    val today = Calendar.getInstance()

    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }

    return solves
        .sortedByDescending(SolveEntity::createdAt)
        .groupBy { solve ->
            Calendar.getInstance().apply {
                timeInMillis = solve.createdAt
            }.let {
                Triple(
                    it.get(Calendar.YEAR),
                    it.get(Calendar.MONTH),
                    it.get(Calendar.DAY_OF_MONTH)
                )
            }
        }
        .map { (_, daySolves) ->
            val first = daySolves.first()

            val calendar = Calendar.getInstance().apply {
                timeInMillis = first.createdAt
            }

            DayGroup(
                title = when {
                    TimeUtils.isSameDay(calendar, today) -> "Today"
                    TimeUtils.isSameDay(calendar, yesterday) -> "Yesterday"
                    else -> TimeUtils.formatDate(first.createdAt)
                },
                solves = daySolves.sortedByDescending(SolveEntity::createdAt)
            )
        }
}

private fun calculateAo(
    solves: List<SolveEntity>,
    count: Int
): Long? {
    if (solves.size < count) return null

    val window = solves.take(count)

    if (window.count { it.penalty == PENALTY_DNF } > 1) {
        return null
    }

    val times = window
        .map {
            if (it.penalty == PENALTY_DNF) {
                Long.MAX_VALUE
            } else {
                effectiveTime(it)
            }
        }
        .sorted()

    val trim = maxOf(1, count / 20)

    if (times.size <= trim * 2) return null

    val trimmed = times
        .drop(trim)
        .dropLast(trim)

    if (trimmed.any { it == Long.MAX_VALUE }) {
        return null
    }

    return trimmed.average().toLong()
}

private fun effectiveTime(
    solve: SolveEntity
): Long {
    return when (solve.penalty) {
        PENALTY_PLUS_TWO -> solve.time + 2_000
        else -> solve.time
    }
}