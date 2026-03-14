package org.crepo.updated_user_manager

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import java.util.Locale

// Глобальный state для языка (временно — до перехода на CompositionLocal)
object AppLanguage {
    var current by mutableStateOf(Locale.getDefault())
}
data object MainScreen : Screen {
    private fun readResolve(): Any = MainScreen
    override val key: ScreenKey = AppLanguage.current.toString()  // ← ключ зависит от языка!

    @Composable
    override fun Content() {
        MainMenu(
            navigateBack = {}
        )
    }
}

data object CreateUserScreen : Screen {
    private fun readResolve(): Any = CreateUserScreen
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        UI2_1(navigateBack = { navigator.pop() })
    }
}

data object DeleteUserScreen : Screen {
    private fun readResolve(): Any = CreateUserScreen
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        UI2_2(navigateBack = { navigator.pop() })
    }
}

data object ChangePasswordScreen : Screen {
    private fun readResolve(): Any = ChangePasswordScreen
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        UI3_1(navigateBack = { navigator.pop() })
    }
}