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


fun main() = singleWindowApplication {
    val (locale, setLocale) = remember { mutableStateOf(LanguageManager.currentLocale) }

    // Обновляем глобальный язык при смене
    LaunchedEffect(locale) {
        LanguageManager.setLocale(locale)
    }

    CompositionLocalProvider(LocalLanguage provides locale) {
        Navigator(MainScreen)
    }

    Navigator(MainScreen)
}