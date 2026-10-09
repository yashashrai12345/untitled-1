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
     * Endpoint URL for published patterns.
     * Can point to your free Vercel deployment (e.g. "https://your-app.vercel.app/api/public/patterns")
     * or directly to your Supabase REST endpoint.
     */
    var customLevelsApiUrl: String = "https://your-app.vercel.app/api/public/patterns"
    var supabaseApiKey: String? = null

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
                    val levelData = item.optJSONObject("level_data") ?: item
                    val numericId = item.optInt("numeric_id", 1000 + i + 1)

                    val parsedLevel = parseLevelFromJson(levelData, numericId)
                    if (parsedLevel != null) {
                        levels.add(parsedLevel)
                    }
                }
            }
        } catch (e: Exception) {
            // Offline or unreachable
        }
        return@withContext levels
    }

    fun parseLevelFromJson(json: JSONObject, fallbackNumericId: Int): Level? {
        return try {
            val rawId = json.optInt("id", fallbackNumericId)
            val id = if (rawId < 1000) 1000 + rawId else rawId
            val name = json.optString("name", "Custom Level $id")

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
                id = id,
                name = name,
                board = board,
                arrows = arrows,
                difficulty = difficulty,
                parMoves = parMoves,
                patternType = "CUSTOM",
                seed = id.toLong(),
                difficultyScore = difficulty * 1.0f
            )
        } catch (e: Exception) {
            null
        }
    }
}
