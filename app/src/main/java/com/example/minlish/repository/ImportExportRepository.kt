package com.example.minlish.repository

import android.content.Context
import android.net.Uri
import com.example.minlish.model.Vocabulary
import com.example.minlish.util.CsvUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class ImportExportRepository(private val context: Context) {

    private val vocabRepo = VocabularyRepository()

    suspend fun importFromUri(uri: Uri, deckId: String): ImportResult {
        val lines = mutableListOf<String>()
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).forEachLine { lines.add(it) }
        } ?: return ImportResult(0, 0, "Không thể mở file")

        if (lines.isEmpty()) return ImportResult(0, 0, "File rỗng")

        val header = lines.first().lowercase()
        val hasHeader = header.contains("word") || header.contains("meaning")
        val dataLines = if (hasHeader) lines.drop(1) else lines

        var imported = 0
        var skipped = 0

        dataLines.filter { it.isNotBlank() }.forEach { line ->
            val cols = line.split(",").map { CsvUtils.decode(it.trim()) }
            if (cols.size < 2 || cols[0].isBlank() || cols[1].isBlank()) {
                skipped++
                return@forEach
            }
            val vocab = Vocabulary(
                word = cols[0],
                meaning = cols[1],
                pronunciation = cols.getOrElse(2) { "" },
                example = cols.getOrElse(3) { "" },
                collocation = cols.getOrElse(4) { "" },
                relatedWords = cols.getOrElse(5) { "" },
                note = cols.getOrElse(6) { "" },
                deckId = deckId
            )
            vocabRepo.save(vocab)
            imported++
        }

        return ImportResult(imported, skipped, null)
    }

    suspend fun exportToUri(uri: Uri, deckId: String): Boolean {
        val vocabs = vocabRepo.getByDeck(deckId)
        return try {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                OutputStreamWriter(stream).use { writer ->
                    writer.write("word,meaning,pronunciation,example,collocation,relatedWords,note\n")
                    vocabs.forEach { v ->
                        writer.write(
                            "${CsvUtils.encodeCsv(v.word)},${CsvUtils.encodeCsv(v.meaning)}," +
                                    "${CsvUtils.encodeCsv(v.pronunciation)},${CsvUtils.encodeCsv(v.example)}," +
                                    "${CsvUtils.encodeCsv(v.collocation)},${CsvUtils.encodeCsv(v.relatedWords)}," +
                                    "${CsvUtils.encodeCsv(v.note)}\n"
                        )
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}

data class ImportResult(
    val imported: Int,
    val skipped: Int,
    val error: String?
)