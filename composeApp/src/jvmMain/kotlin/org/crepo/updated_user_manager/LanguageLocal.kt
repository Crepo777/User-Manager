package org.crepo.updated_user_manager

// LanguageLocal.kt
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

val LocalLanguage = staticCompositionLocalOf<Locale> { Locale.getDefault() }