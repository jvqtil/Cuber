package dev.jvqtil.cuber.ui.screens

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.jvqtil.cuber.CuberViewModel
import dev.jvqtil.cuber.scramble.CubePreview
import dev.jvqtil.cuber.util.TimeUtils

@Composable
fun TimerScreen(
    viewModel: CuberViewModel,
    onOpenSolves: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val view = LocalView.current

    DisposableEffect(state.running, isLandscape) {
        val controller = WindowCompat.getInsetsController(
            (view.context as Activity).window,
            view
        )

        if (isLandscape) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else if (state.running) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }

        onDispose { }
    }

    var dragDistance by remember {
        mutableFloatStateOf(0f)
    }

    val controlsAlpha by animateFloatAsState(
        targetValue = if (state.running) 0f else 1f,
        animationSpec = tween(
            durationMillis = 240,
            easing = FastOutSlowInEasing
        ),
        label = "controlsAlpha"
    )

    val controlsTranslation by animateFloatAsState(
        targetValue = if (state.running) 18f else 0f,
        animationSpec = spring(
            dampingRatio = 0.9f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "controlsTranslation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(state.started, state.running) {
                detectTapGestures(
                    onPress = {
                        if (state.running) {
                            viewModel.setHolding(true)

                            try {
                                awaitRelease()
                            } finally {
                                viewModel.setHolding(false)
                            }
                        }
                    },
                    onTap = {
                        when {
                            !state.started -> viewModel.start()
                            state.running -> viewModel.stop()
                        }
                    },
                    onLongPress = {
                        if (!state.running) {
                            viewModel.reset()

                            haptic.performHapticFeedback(
                                HapticFeedbackType.LongPress
                            )
                        }
                    }
                )
            }
            .pointerInput(state.running) {
                detectVerticalDragGestures(
                    onDragStart = {
                        dragDistance = 0f
                    },
                    onVerticalDrag = { _, dragAmount ->
                        dragDistance += dragAmount
                    },
                    onDragEnd = {
                        if (
                            !state.running &&
                            dragDistance < -120f
                        ) {
                            onOpenSolves()

                            haptic.performHapticFeedback(
                                HapticFeedbackType.TextHandleMove
                            )
                        }

                        dragDistance = 0f
                    },
                    onDragCancel = {
                        dragDistance = 0f
                    }
                )
            }
    ) {
        if (isLandscape) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val halfWidth = maxWidth / 2

                val timerOffset by animateFloatAsState(
                    targetValue = if (state.running) {
                        halfWidth.value / 2f
                    } else {
                        0f
                    },
                    animationSpec = spring(
                        dampingRatio = 0.9f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "timerOffset"
                )

                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = TimeUtils.format(state.elapsed),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(x = timerOffset.dp),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 84.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-2.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .alpha(controlsAlpha)
                            .graphicsLayer {
                                translationY = controlsTranslation
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CubePreview(
                                scramble = state.scramble
                            )

                            Text(
                                text = state.scramble.text,
                                modifier = Modifier.fillMaxWidth(0.82f),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text = TimeUtils.format(state.elapsed),
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 84.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-2.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 24.dp
                    )
                    .alpha(controlsAlpha)
                    .graphicsLayer {
                        translationY = controlsTranslation
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CubePreview(
                    scramble = state.scramble
                )

                Text(
                    text = state.scramble.text,
                    modifier = Modifier.fillMaxWidth(0.82f),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}