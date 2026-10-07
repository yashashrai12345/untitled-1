package com.example.game.model

/**
 * Data class holding a puzzle level definition.
 */
data class Level(
    val id: Int,
    val name: String,
    val board: Board,
    val arrows: List<Arrow>,
    val difficulty: Int = 1,
    val parMoves: Int = arrows.size,
    val patternType: String = "CUSTOM",
    val seed: Long = 0L,
    val difficultyScore: Float = 1.0f
) {
    init {
        require(arrows.isNotEmpty()) { "Level must contain at least one arrow" }
        val ids = arrows.map { it.id }
        require(ids.toSet().size == ids.size) { "Level has duplicate arrow IDs" }
    }
}
