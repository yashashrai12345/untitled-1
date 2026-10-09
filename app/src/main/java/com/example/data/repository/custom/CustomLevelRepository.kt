package com.example.data.repository.custom

import android.content.Context
import com.example.game.model.Level
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CustomLevelRepository {

    private val _customLevels = MutableStateFlow<List<Level>>(emptyList())
    val customLevels: StateFlow<List<Level>> = _customLevels.asStateFlow()

    fun initialize(context: Context) {
        val cached = CustomLevelCache.loadCustomLevels(context)
        _customLevels.value = cached
    }

    suspend fun syncRemoteCustomLevels(context: Context) {
        val remote = CustomLevelRemoteDataSource.fetchPublishedLevels()
        _customLevels.value = remote
        CustomLevelCache.saveCustomLevels(context, remote)
    }

    fun getCustomLevel(levelId: Int): Level? {
        val list = _customLevels.value
        if (list.isEmpty() || levelId < 1 || levelId > list.size) return null
        return list[levelId - 1].copy(id = levelId)
    }

    fun hasCustomLevels(): Boolean = _customLevels.value.isNotEmpty()
}
