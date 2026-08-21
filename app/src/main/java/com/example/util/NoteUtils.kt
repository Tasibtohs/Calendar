package com.example.util

import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class NoteChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isDone: Boolean = false
)

data class DrawingPoint(val x: Float, val y: Float)

data class DrawingStroke(
    val points: List<DrawingPoint>,
    val colorHex: String = "#0D47A1",
    val strokeWidth: Float = 6f
)

object NoteUtils {

    fun parseChecklist(json: String, fallbackContent: String = ""): List<NoteChecklistItem> {
        if (json.isNotBlank()) {
            try {
                val array = JSONArray(json)
                val list = mutableListOf<NoteChecklistItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        NoteChecklistItem(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            text = obj.optString("text", ""),
                            isDone = obj.optBoolean("isDone", false)
                        )
                    )
                }
                if (list.isNotEmpty()) return list
            } catch (e: Exception) {
                // fallback to parsing lines
            }
        }

        if (fallbackContent.isNotBlank()) {
            val lines = fallbackContent.lines().filter { it.isNotBlank() }
            if (lines.isNotEmpty()) {
                return lines.map { line ->
                    val isDone = line.startsWith("[x] ", ignoreCase = true) || line.startsWith("[X] ")
                    val cleanText = line
                        .removePrefix("[x] ")
                        .removePrefix("[X] ")
                        .removePrefix("[ ] ")
                        .removePrefix("• ")
                        .removePrefix("- ")
                        .trim()
                    NoteChecklistItem(text = cleanText, isDone = isDone)
                }
            }
        }

        return emptyList()
    }

    fun serializeChecklist(items: List<NoteChecklistItem>): String {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("text", item.text)
            obj.put("isDone", item.isDone)
            array.put(obj)
        }
        return array.toString()
    }

    fun parseDrawingStrokes(json: String?): List<DrawingStroke> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<DrawingStroke>()
            for (i in 0 until array.length()) {
                val strokeObj = array.getJSONObject(i)
                val colorHex = strokeObj.optString("colorHex", "#0D47A1")
                val width = strokeObj.optDouble("strokeWidth", 6.0).toFloat()
                val ptsArray = strokeObj.getJSONArray("points")
                val points = mutableListOf<DrawingPoint>()
                for (j in 0 until ptsArray.length()) {
                    val ptObj = ptsArray.getJSONObject(j)
                    points.add(DrawingPoint(ptObj.getDouble("x").toFloat(), ptObj.getDouble("y").toFloat()))
                }
                if (points.isNotEmpty()) {
                    list.add(DrawingStroke(points = points, colorHex = colorHex, strokeWidth = width))
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeDrawingStrokes(strokes: List<DrawingStroke>): String {
        val array = JSONArray()
        strokes.forEach { stroke ->
            val strokeObj = JSONObject()
            strokeObj.put("colorHex", stroke.colorHex)
            strokeObj.put("strokeWidth", stroke.strokeWidth)
            val ptsArray = JSONArray()
            stroke.points.forEach { pt ->
                val ptObj = JSONObject()
                ptObj.put("x", pt.x)
                ptObj.put("y", pt.y)
                ptsArray.put(ptObj)
            }
            strokeObj.put("points", ptsArray)
            array.put(strokeObj)
        }
        return array.toString()
    }

    fun parseTags(tagsString: String): List<String> {
        if (tagsString.isBlank()) return emptyList()
        return tagsString.split(",")
            .map { it.trim().removePrefix("#").trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    fun formatTags(tags: List<String>): String {
        return tags.map { it.trim().removePrefix("#").trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(",")
    }

    val SUGGESTED_TAGS = listOf(
        "ব্যক্তিগত", "অফিস", "আইডিয়া", "জরুরি", "শপিং", "ফাইন্যান্স", "পড়াশোনা", "প্রজেক্ট", "স্বাস্থ্য"
    )
}
