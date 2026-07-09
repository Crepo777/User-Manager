package org.crepo.updated_user_manager

// Logger.kt

import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Logger {
    private val logFile: File

    init {
        val appDir = File(System.getProperty("user.home"), ".usermanager")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        logFile = File(appDir, "usermanager.log")

        // Создаем файл, если его нет
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
    }

    fun info(message: String) {
        log("INFO", message)
    }

    fun warning(message: String) {
        log("WARNING", message)
    }

    fun error(message: String, throwable: Throwable? = null) {
        var fullMessage = message
        if (throwable != null) {
            fullMessage += "\n${throwable.javaClass.name}: ${throwable.message}\n" +
                    throwable.stackTrace.joinToString("\n")
        }
        log("ERROR", fullMessage)
    }

    private fun log(level: String, message: String) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val logEntry = "[$timestamp] [$level] $message"

        try {
            OutputStreamWriter(FileOutputStream(logFile, true), StandardCharsets.UTF_8).use { writer ->
                writer.appendLine(logEntry)
            }
        } catch (e: IOException) {
            println("Failed to write to log file: $e")
        }
    }
    fun exportToCsv(): String {
        val logFile = File(System.getProperty("user.home"), ".usermanager/usermanager.log")
        if (!logFile.exists()) return ""

        return logFile.readLines().joinToString("\n") { line ->
            // Парсим строку лога
            val timestampRegex = "\\[(.*?)\\]".toRegex()
            val levelRegex = "\\[(INFO|WARNING|ERROR)\\]".toRegex()

            val timestamp = timestampRegex.find(line)?.groupValues?.get(1) ?: ""
            val level = levelRegex.find(line)?.groupValues?.get(1) ?: ""
            val message = line.substringAfter("] ", "")

            // Форматируем как CSV
            "\"$timestamp\",\"$level\",\"${message.replace("\"", "\"\"")}\""
        }
    }
}