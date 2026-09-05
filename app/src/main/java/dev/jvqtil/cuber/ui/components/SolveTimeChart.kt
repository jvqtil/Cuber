package dev.jvqtil.cuber.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.jvqtil.cuber.database.PENALTY_PLUS_TWO
import dev.jvqtil.cuber.database.SolveEntity
import kotlin.math.max
import kotlin.math.min

@Composable
fun SolveTimeChart(
    solves: List<SolveEntity>,
) {
    if (solves.size < 2) return

    val lineColor = MaterialTheme.colorScheme.primary

    val values = remember(solves) {
        solves
            .sortedBy(SolveEntity::createdAt)
            .map {
                when (it.penalty) {
                    PENALTY_PLUS_TWO -> it.time + 2_000L
                    else -> it.time
                }.toFloat()
            }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Solve time chart",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val minValue = values.minOrNull() ?: return@Canvas
            val maxValue = values.maxOrNull() ?: return@Canvas
            val range = max(maxValue - minValue, 1f)

            val horizontalPadding = 4.dp.toPx()
            val verticalPadding = 12.dp.toPx()

            val width = size.width - horizontalPadding * 2
            val height = size.height - verticalPadding * 2

            val points = values.mapIndexed { index, value ->
                val x = horizontalPadding +
                        width * index / values.lastIndex.toFloat()

                val normalized = (value - minValue) / range

                Offset(
                    x = x,
                    y = verticalPadding + height * (1f - normalized)
                )
            }

            val path = Path().apply {
                moveTo(
                    points.first().x,
                    points.first().y
                )

                for (i in 0 until points.lastIndex) {
                    val p0 = points[max(0, i - 1)]
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    val p3 = points[min(points.lastIndex, i + 2)]

                    val control1 = Offset(
                        p1.x + (p2.x - p0.x) / 6f,
                        p1.y + (p2.y - p0.y) / 6f
                    )

                    val control2 = Offset(
                        p2.x - (p3.x - p1.x) / 6f,
                        p2.y - (p3.y - p1.y) / 6f
                    )

                    cubicTo(
                        control1.x,
                        control1.y,
                        control2.x,
                        control2.y,
                        p2.x,
                        p2.y
                    )
                }
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
