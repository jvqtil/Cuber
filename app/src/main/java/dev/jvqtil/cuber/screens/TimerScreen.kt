package dev.jvqtil.cuber.screens

import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jvqtil.cuber.scramble.CubePreview
import dev.jvqtil.cuber.scramble.ScrambleGenerator
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val PreviewBottomPadding = 40.dp

@Composable
fun TimerScreen() {
    var started by remember { mutableStateOf(false) }
    var running by remember { mutableStateOf(false) }
    var holding by remember { mutableStateOf(false) }
    var elapsed by remember { mutableLongStateOf(0L) }
    var startedAt by remember { mutableLongStateOf(0L) }
    var scramble by remember { mutableStateOf(ScrambleGenerator.generate()) }

    val haptic = LocalHapticFeedback.current

    val timerColor by animateColorAsState(
        targetValue = if (holding) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onBackground
        },
        animationSpec = tween(
            durationMillis = 180,
            easing = FastOutSlowInEasing
        ),
        label = "timerColor"
    )

    LaunchedEffect(running) {
        while (running) {
            elapsed = SystemClock.elapsedRealtime() - startedAt
            delay(10.milliseconds)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        holding = true

                        try {
                            awaitRelease()
                        } finally {
                            holding = false
                        }
                    },
                    onTap = {
                        when {
                            !started -> {
                                started = true
                                running = true
                                startedAt = SystemClock.elapsedRealtime()
                            }

                            running -> {
                                running = false
                                elapsed = SystemClock.elapsedRealtime() - startedAt
                            }
                        }
                    },
                    onLongPress = {
                        started = false
                        running = false
                        elapsed = 0L
                        startedAt = 0L
                        scramble = ScrambleGenerator.generate()

                        haptic.performHapticFeedback(
                            HapticFeedbackType.LongPress
                        )
                    }
                )
            }
    ) {
        Text(
            text = formatTime(elapsed),
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 84.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2.5).sp
            ),
            color = timerColor
        )

        AnimatedVisibility(
            visible = !running,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = PreviewBottomPadding),
            enter = fadeIn(
                animationSpec = tween(220)
            ) + slideInVertically(
                animationSpec = tween(
                    durationMillis = 280,
                    easing = FastOutSlowInEasing
                ),
                initialOffsetY = { it / 5 }
            ),
            exit = fadeOut(
                animationSpec = tween(160)
            ) + slideOutVertically(
                animationSpec = tween(
                    durationMillis = 220,
                    easing = FastOutSlowInEasing
                ),
                targetOffsetY = { it / 5 }
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedContent(
                    targetState = scramble,
                    transitionSpec = {
                        fadeIn(
                            animationSpec = tween(220)
                        ) togetherWith fadeOut(
                            animationSpec = tween(140)
                        ) using SizeTransform(clip = false)
                    },
                    label = "scramblePreview"
                ) { currentScramble ->
                    CubePreview(currentScramble)
                }

                AnimatedContent(
                    targetState = scramble.text,
                    transitionSpec = {
                        fadeIn(
                            animationSpec = tween(220)
                        ) togetherWith fadeOut(
                            animationSpec = tween(140)
                        )
                    },
                    label = "scrambleText"
                ) { text ->
                    Text(
                        text = text,
                        modifier = Modifier.fillMaxWidth(0.8f),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val minutes = ms / 60_000
    val seconds = (ms / 1_000) % 60
    val centiseconds = (ms % 1_000) / 10

    return if (minutes > 0) {
        "$minutes:%02d.%02d".format(seconds, centiseconds)
    } else {
        "%d.%02d".format(seconds, centiseconds)
    }
}