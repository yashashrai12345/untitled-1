package com.example.data.repository.custom

import com.example.game.model.Arrow
import com.example.game.model.Board
import com.example.game.model.Direction
import com.example.game.model.GridPoint
import com.example.game.model.Level
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object CustomLevelRemoteDataSource {

    /**
     * Endpoint URL for published patterns ordered by level_number ascending.
     */
    var customLevelsApiUrl: String = "https://wgqxhfpoqgzydepzwkls.supabase.co/rest/v1/custom_patterns?status=eq.published&select=*&order=level_number.asc,published_at.asc"
    var supabaseApiKey: String? = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndncXhoZnBvcWd6eWRlcHp3a2xzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTE0MzIsImV4cCI6MjA3MjgwN30.0c3gxTdJ8Xkp1ZPBf64PWWBWfxz0t4-UFFhicHRKSQ"

    suspend fun fetchPublishedLevels(): List<Level> = withContext(Dispatchers.IO) {
        val levels = mutableListOf<Level>()
        try {
            val url = URL(customLevelsApiUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                supabaseApiKey?.let { key ->
                    setRequestProperty("apikey", key)
                    setRequestProperty("Authorization", "Bearer $key")
                }
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonText)

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val status = item.optString("status", "published")
                    if (status.lowercase() != "published") continue

                    val levelData = item.optJSONObject("level_data") ?: item
                    val levelNumber = item.optInt("level_number", i + 1)

                    val parsedLevel = parseLevelFromJson(levelData, levelNumber, i + 1)
                    if (parsedLevel != null) {
                        levels.add(parsedLevel)
                    }
                }
            }
        } catch (e: Exception) {
            // Offline or network error
        }
        return@withContext levels
    }

    fun parseLevelFromJson(json: JSONObject, levelNumber: Int, displayIndex: Int): Level? {
        return try {
            val name = json.optString("name", "Level $displayIndex")

            val boardObj = json.getJSONObject("board")
            val width = boardObj.getInt("width")
            val height = boardObj.getInt("height")
            val board = Board(width, height)

            val arrowsArray = json.getJSONArray("arrows")
            val arrows = mutableListOf<Arrow>()

            for (j in 0 until arrowsArray.length()) {
                val arrowObj = arrowsArray.getJSONObject(j)
                val arrowId = arrowObj.getString("id")
                val dirStr = arrowObj.getString("direction")
                val direction = Direction.valueOf(dirStr.uppercase())

                val pointsArray = arrowObj.getJSONArray("points")
                val points = mutableListOf<GridPoint>()

                for (p in 0 until pointsArray.length()) {
                    val ptObj = pointsArray.getJSONObject(p)
                    points.add(GridPoint(ptObj.getInt("x"), ptObj.getInt("y")))
                }

                arrows.add(
                    Arrow(
                        id = arrowId,
                        direction = direction,
                        points = points
                    )
                )
            }

            val difficulty = json.optInt("difficulty", 2)
            val parMoves = json.optInt("parMoves", arrows.size)

            Level(
                id = displayIndex, // Always sequential 1..N display level number
                name = name,
                board = board,
                arrows = arrows,
                difficulty = difficulty,
                parMoves = parMoves,
                patternType = "CUSTOM",
                seed = displayIndex.toLong(),
                difficultyScore = difficulty * 1.0f,
                levelNumber = levelNumber
            )
        } catch (e: Exception) {
            null
        }
    }
}
