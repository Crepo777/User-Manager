package org.crepo.updated_user_manager

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

object AppConfig {
    private val appDir = File(System.getProperty("user.home"), ".usermanager").apply {
        if (!exists()) mkdirs()
    }

    private val config = File(appDir, "config.properties")
    private val properties = Properties()

    init {
        if (config.exists()) {
            FileInputStream(config).use { properties.load(it) }
        }
    }

    fun isDisclaimerAccepted(): Boolean {
        return properties.getProperty("disclaimer_accepted", "false") == "true"
    }

    fun saveDisclaimerAccepted() {
        properties["disclaimer_accepted"] = "true"
        FileOutputStream(config).use { properties.store(it, "User Manager Configuration") }
    }

    fun resetDisclaimer() {
        properties.remove("disclaimer_accepted")
        FileOutputStream(config).use { properties.store(it, "User Manager Configuration") }
    }

    fun getSavedLanguage(): String? {
        return properties.getProperty("language")
    }

    fun saveLanguage(language: String) {
        properties["language"] = language
        FileOutputStream(config).use { properties.store(it, "User Manager Configuration") }
    }

    fun isRunningAsAdmin(): Boolean {
        return try {
            ProcessBuilder("net", "session").start().waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
}