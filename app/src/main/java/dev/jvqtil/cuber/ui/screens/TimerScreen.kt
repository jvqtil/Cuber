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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val RESET_DELAY_MS = 500L

@Composable
fun TimerScreen(
    viewModel: CuberViewModel,
    onOpenSolves: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val view = LocalView.current
    val scope = rememberCoroutineScope()

    var dragDistance by remember {
        mutableFloatStateOf(0f)
    }

    var resetTriggered by remember {
        mutableStateOf(false)
    }

    var resetPressed by remember {
        mutableStateOf(false)
    }

    var resetJob by remember {
        mutableStateOf<Job?>(null)
    }

    var mouseResetGesture by remember {
        mutableStateOf(false)
    }

    fun cancelReset() {
        resetJob?.cancel()
        resetJob = null
    }

    fun beginResetHold() {
        if (
            state.running ||
            !state.started ||
            resetJob != null
        ) {
            return
        }

        resetTriggered = false
        resetPressed = true
        cancelReset()

        resetJob = scope.launch {
            delay(RESET_DELAY_MS)

            if (resetPressed) {
                resetTriggered = true
                viewModel.reset()

                haptic.performHapticFeedback(
                    HapticFeedbackType.LongPress
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cancelReset()
        }
    }

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

    val resetProgress by animateFloatAsState(
        targetValue = if (resetPressed) 1f else 0f,
        animationSpec = tween(
            durationMillis = RESET_DELAY_MS.toInt(),
            easing = FastOutSlowInEasing,
        ),
        label = "resetProgress",
    )

    val controlsAlpha by animateFloatAsState(
        targetValue = if (state.running) 0f else 1f,
        animationSpec = tween(
            durationMillis = 240,
            easing = FastOutSlowInEasing
        ),
        label = "controlsAlpha"
    )

    val controlsTranslation by androidx.compose.animation.core.animateFloatAsState(
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
                        val canReset =
                            state.started && !state.running

                        mouseResetGesture = canReset

                        if (canReset) {
                            beginResetHold()
                        }

                        try {
                            awaitRelease()
                        } finally {
                            cancelReset()
                            resetPressed = false
                        }
                    },
                    onTap = {
                        if (!mouseResetGesture) {
                            when {
                                !state.started -> viewModel.start()
                                state.running -> viewModel.stop()
                            }
                        }

                        mouseResetGesture = false
                        resetTriggered = false
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
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(x = timerOffset.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(360.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = TimeUtils.format(state.elapsed),
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontSize = 84.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-2.5).sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(190.dp)
                                    .height(7.dp)
                            ) {
                                ResetProgress(
                                    progress = resetProgress,
                                    visible = resetPressed
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .graphicsLayer {
                                alpha = controlsAlpha
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
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(360.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = TimeUtils.format(state.elapsed),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 84.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-2.5).sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Box(
                    modifier = Modifier
                        .width(190.dp)
                        .height(7.dp)
                ) {
                    ResetProgress(
                        progress = resetProgress,
                        visible = resetPressed
                    )
                }
            }

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
                    .graphicsLayer {
                        alpha = controlsAlpha
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

@Composable
private fun ResetProgress(
    progress: Float,
    visible: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = if (visible) 1f else 0f
            }
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                RoundedCornerShape(50),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(
                    progress.coerceIn(
                        0f,
                        1f,
                    ),
                )
                .fillMaxHeight()
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(50),
                ),
        )
    }
}
