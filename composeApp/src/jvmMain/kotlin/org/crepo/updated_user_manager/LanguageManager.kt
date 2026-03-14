package org.crepo.updated_user_manager

import androidx.compose.runtime.*
import java.util.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

object LanguageManager {
    var currentLocale by mutableStateOf(Locale.getDefault())
        private set

    fun setLocale(locale: Locale) {
        currentLocale = locale
    }
}