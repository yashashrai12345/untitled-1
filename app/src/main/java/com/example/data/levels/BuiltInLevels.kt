package com.example.data.levels

import com.example.game.generator.pattern.HandcraftedLevelLibrary
import com.example.game.model.Level

object BuiltInLevels {

    val levels: List<Level> by lazy {
        (1..15).mapNotNull { HandcraftedLevelLibrary.getLevel(it) }
    }
}
