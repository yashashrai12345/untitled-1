package com.example.data.repository.custom

import android.content.Context
import com.example.game.model.Arrow
import com.example.game.model.Board
import com.example.game.model.Direction
import com.example.game.model.GridPoint
import com.example.game.model.Level
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object CustomLevelCache {

    private const val CACHE_FILE_NAME = "custom_levels_cache.json"

    fun saveCustomLevels(context: Context, levels: List<Level>) {
        try {
            val jsonArray = JSONArray()
            for (level in levels) {
                val lvlObj = JSONObject().apply {
                    put("id", level.id)
                    put("name", level.name)
                    put("difficulty", level.difficulty)
                    put("parMoves", level.parMoves)
                    put("board", JSONObject().apply {
                        put("width", level.board.width)
                        put("height", level.board.height)
                    })

                    val arrowsArray = JSONArray()
                    for (arrow in level.arrows) {
                        val arrowObj = JSONObject().apply {
                            put("id", arrow.id)
                            put("direction", arrow.direction.name)

                            val ptsArray = JSONArray()
                            for (p in arrow.points) {
                                ptsArray.put(JSONObject().apply {
                                    put("x", p.x)
                                    put("y", p.y)
                                })
                            }
                            put("points", ptsArray)
                        }
                        arrowsArray.put(arrowObj)
                    }
                    put("arrows", arrowsArray)
                }
                jsonArray.put(lvlObj)
            }

            val file = File(context.filesDir, CACHE_FILE_NAME)
            file.writeText(jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadCustomLevels(context: Context): List<Level> {
        val levels = mutableListOf<Level>()
        try {
            val file = File(context.filesDir, CACHE_FILE_NAME)
            if (!file.exists()) return emptyList()

            val jsonText = file.readText()
            val jsonArray = JSONArray(jsonText)

            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                val parsed = CustomLevelRemoteDataSource.parseLevelFromJson(json, 1001 + i)
                if (parsed != null) {
                    levels.add(parsed)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return levels
    }
}
