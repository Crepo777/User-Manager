package org.crepo.updated_user_manager

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.singleWindowApplication
import cafe.adriel.voyager.navigator.Navigator
import org.crepo.updated_user_manager.MainScreen
import androidx.compose.ui.window.singleWindowApplication
import org.crepo.updated_user_manager.LanguageManager
import org.crepo.updated_user_manager.InitialScreen


fun main() = singleWindowApplication {
    // Устанавливаем кодовую страницу 1251
    try {
        ProcessBuilder("cmd", "/c", "chcp", "1251 >nul").start()
    } catch (e: Exception) {
        println("Failed to set code page: ${e.message}")
    }
    val (locale, setLocale) = remember { mutableStateOf(LanguageManager.currentLocale) }


    // Обновляем глобальный язык при смене
    LaunchedEffect(locale) {
        LanguageManager.setLocale(locale)
    }

    CompositionLocalProvider(LocalLanguage provides locale) {

        Navigator(InitialScreen)
    }

    Navigator(InitialScreen)
}

fun checkCodePage(): Boolean {
    val process = ProcessBuilder("cmd", "/c", "chcp").start()
    val output = process.inputStream.bufferedReader().readText()
    //if (output.contains("1251")) {return true}
    //else {return false}
    println(output)
    return output.contains("1251")
}
