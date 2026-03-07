package org.crepo.updated_user_manager

import androidx.compose.ui.window.singleWindowApplication
import cafe.adriel.voyager.navigator.Navigator
import org.crepo.updated_user_manager.MainScreen

fun main() = singleWindowApplication {
    Navigator(MainScreen)
}