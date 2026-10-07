package com.example.data.levels

import com.example.data.repository.LevelRepository
import com.example.game.model.Level

object BuiltInLevels {

    val levels: List<Level> by lazy {
        (1..15).map { LevelRepository.getLevel(it) }
    }
}
