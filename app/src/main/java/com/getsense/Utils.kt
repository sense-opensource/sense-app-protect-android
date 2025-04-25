package com.getsense
import org.json.JSONObject

object Utils{
    private const val PREFS_NAME = "app_prefs"
    fun checkForStringOrTrue(jsonObject: JSONObject, key: String): Boolean {
        // Retrieve the value with optString to avoid exceptions if the key is missing
        val value = jsonObject.optString(key, "")

        // Check if the value is either "true" (case-insensitive) or a non-empty string
        return value.equals("true", ignoreCase = true)
    }
    fun checkKeyForStringOrTrue(key: String): Boolean {
        return key.equals("true", ignoreCase = true)
    }
}