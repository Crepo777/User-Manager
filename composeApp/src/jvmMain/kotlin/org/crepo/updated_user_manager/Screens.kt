package org.crepo.updated_user_manager

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import androidx.compose.runtime.Composable
import org.crepo.updated_user_manager.MainMenu
import org.crepo.updated_user_manager.UI2_1

data object MainScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        MainMenu()
    }
}

data object CreateUserScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        UI2_1(navigateBack = { navigator.pop() })
    }
}