package com.example

import android.content.Context
import org.json.JSONObject

data class TiledArrow(
    val name: String,
    val direction: String,
    val points: List<Pair<Int, Int>>
)

object TiledLevelLoader {

    fun loadArrows(
        context: Context,
        fileName: String
    ): List<TiledArrow> {

        val jsonText = context.assets
            .open("levels/$fileName")
            .bufferedReader()
            .use { it.readText() }

        val map = JSONObject(jsonText)

        val layers = map.getJSONArray("layers")

        val arrows = mutableListOf<TiledArrow>()

        for (i in 0 until layers.length()) {

            val layer = layers.getJSONObject(i)

            // We only want the object layer
            if (layer.optString("type") != "objectgroup") {
                continue
            }

            val objects = layer.getJSONArray("objects")

            for (j in 0 until objects.length()) {

                val obj = objects.getJSONObject(j)

                val name = obj.optString("name", "Arrow")

                // Read direction
                var direction = "RIGHT"

                val properties = obj.optJSONArray("properties")

                if (properties != null) {
                    for (p in 0 until properties.length()) {

                        val property = properties.getJSONObject(p)

                        if (property.optString("name") == "direction") {
                            direction = property.optString(
                                "value",
                                "RIGHT"
                            )
                        }
                    }
                }

                // Read polyline
                val polyline = obj.optJSONArray("polyline")

                if (polyline == null) {
                    continue
                }

                val objectX = obj.optDouble("x", 0.0)
                val objectY = obj.optDouble("y", 0.0)

                val points = mutableListOf<Pair<Int, Int>>()

                for (p in 0 until polyline.length()) {

                    val point = polyline.getJSONObject(p)

                    val pixelX =
                        objectX + point.optDouble("x", 0.0)

                    val pixelY =
                        objectY + point.optDouble("y", 0.0)

                    // Convert pixels → grid coordinates
                    val gridX =
                        kotlin.math.round(pixelX / 32.0).toInt()

                    val gridY =
                        kotlin.math.round(pixelY / 32.0).toInt()

                    points.add(
                        Pair(gridX, gridY)
                    )
                }

                arrows.add(
                    TiledArrow(
                        name = name,
                        direction = direction,
                        points = points
                    )
                )
            }
        }
        return arrows
    }
}