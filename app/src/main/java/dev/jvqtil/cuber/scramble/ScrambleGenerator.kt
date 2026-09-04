package dev.jvqtil.cuber.scramble

import org.worldcubeassociation.tnoodle.puzzle.CubePuzzle
import kotlin.random.Random
import kotlin.random.asJavaRandom

object ScrambleGenerator {
    private val puzzle = CubePuzzle(3)
    private val random = Random.Default

    fun generate(): Scramble {
        val text = puzzle.generateWcaScramble(random.asJavaRandom())
        val svg = puzzle.drawScramble(text, null).toString()

        return Scramble(
            text = text,
            svg = svg
        )
    }
}

data class Scramble(
    val text: String,
    val svg: String
)