package com.example.game.generator.pattern

import com.example.game.model.Arrow
import com.example.game.model.Level

object PatternDeduplicator {

    private val registeredSignatures = mutableSetOf<String>()

    fun getCanonicalSignature(arrows: List<Arrow>): String {
        if (arrows.isEmpty()) return "empty"

        // Find bounding box minimum
        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        for (arrow in arrows) {
            for (p in arrow.points) {
                if (p.x < minX) minX = p.x
                if (p.y < minY) minY = p.y
            }
        }

        // Normalize points relative to (minX, minY)
        val normalizedArrows = arrows.map { arrow ->
            val normPoints = arrow.points.map { p -> "${p.x - minX},${p.y - minY}" }.joinToString(";")
            "${arrow.direction}:$normPoints"
        }.sorted()

        return normalizedArrows.joinToString("|")
    }

    fun isDuplicate(level: Level): Boolean {
        val sig = getCanonicalSignature(level.arrows)
        return registeredSignatures.contains(sig)
    }

    fun register(level: Level) {
        val sig = getCanonicalSignature(level.arrows)
        registeredSignatures.add(sig)
    }

    fun clear() {
        registeredSignatures.clear()
    }
}
