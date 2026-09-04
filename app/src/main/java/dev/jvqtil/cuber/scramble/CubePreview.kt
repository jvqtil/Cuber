package dev.jvqtil.cuber.scramble

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.caverock.androidsvg.SVG

private val PreviewWidth = 210.dp
private val PreviewHeight = 150.dp

@Composable
fun CubePreview(
    scramble: Scramble,
    modifier: Modifier = Modifier
) {
    val svg = remember(scramble.svg) {
        SVG.getFromString(scramble.svg)
    }

    Canvas(
        modifier = modifier.size(
            width = PreviewWidth,
            height = PreviewHeight
        )
    ) {
        val scale = minOf(
            size.width / svg.documentWidth,
            size.height / svg.documentHeight
        )

        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.save()
            canvas.nativeCanvas.scale(scale, scale)
            svg.renderToCanvas(canvas.nativeCanvas)
            canvas.nativeCanvas.restore()
        }
    }
}