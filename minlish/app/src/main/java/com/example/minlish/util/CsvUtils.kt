package com.example.minlish.util

object CsvUtils {

    fun encode(value: String): String =
        value.replace("\n", "\\n").replace("|", "\\|")

    fun decode(value: String): String =
        value.replace("\\n", "\n").replace("\\|", "|")

    fun encodeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n"))
            "\"$escaped\""
        else
            escaped
    }
}