package org.crepo.updated_user_manager

import androidx.compose.foundation.layout.Box
import androidx.compose.material.CircularProgressIndicator
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.screen.ScreenKey
import java.util.Locale
import javax.swing.Box
import javax.swing.text.StyleConstants.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.crepo.updateduser_manager.FileAttributesScreenContent


//Глобальный state для языка (временно — до перехода на CompositionLocal)

data object MainScreen : Screen {
    override val key = LanguageManager.currentLocale.toString()


    @Composable
    override fun Content() {
        MainMenu()
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

data object UserAccountScreen : Screen {
    private fun readResolve(): Any = ChangePasswordScreen
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        ToggleUserStatusScreenContent(navigateBack = { navigator.pop() })
    }
}

data object ManageUsersInGroupsScreen : Screen {
    private fun readResolve(): Any = ChangePasswordScreen
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        UI2_6(navigateBack = { navigator.pop() })
    }
}

data object ManageGroupsScreen : Screen {
    private fun readResolve(): Any = ChangePasswordScreen
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        UI2_6(navigateBack = { navigator.pop() })
    }
}

data object SetPermissionsScreen : Screen {
    private fun readResolve(): Any = SetPermissionsScreen
    override val key = uniqueScreenKey
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        UI4_1(navigateBack = { navigator.pop() })
    }
}

data object ViewPermissionsScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        ViewPermissionsScreenContent(navigateBack = { navigator.pop() })
    }
}

data object ChangeOwnerScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        ChangeOwnerScreenContent(navigateBack = { navigator.pop() })
    }
}

data object FileAttributesScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        FileAttributesScreenContent(navigateBack = { navigator.pop() })
    }
}



//Disclaymer
data object InitialScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        val coroutineScope = rememberCoroutineScope()

        val disclaimerAccepted by remember { mutableStateOf(AppConfig.isDisclaimerAccepted()) }
        var showAdminWarning by remember { mutableStateOf(false) }
        var shouldNavigateToMain by remember { mutableStateOf(false) }

        //Обработка перехода после закрытия диалога
        LaunchedEffect(shouldNavigateToMain) {
            if (shouldNavigateToMain) {
                navigator.replace(MainScreen)
                shouldNavigateToMain = false
            }
        }

        LaunchedEffect(disclaimerAccepted) {
            if (disclaimerAccepted) {
                if (!AppConfig.isRunningAsAdmin()) {
                    navigator.replace(MainScreen)
                    //showAdminWarning = true
                } else {
                    navigator.replace(MainScreen)
                }
            } else {
                navigator.replace(DisclaimerScreen)
            }
        }

        if (showAdminWarning) {
            AlertDialog(
                onDismissRequest = { /* Только через кнопку "ОК" */ },
                title = { Text(StringResources.getString("admin_warning_title")) },
                text = { Text(StringResources.getString("admin_warning_text")) },
                confirmButton = {
                    Button(onClick = {
                        showAdminWarning = false
                        coroutineScope.launch {
                            delay(50)
                            showAdminWarning = false
                            navigator.replace(MainScreen)
                            showAdminWarning = false
                        }
                    }) {
                        Text(StringResources.getString("admin_warning_ok"))
                    }
                }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}


//Disclaymer
data object DisclaimerScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        DisclaimerScreenContent()
    }
}


data object SettingsScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        SettingsScreenContent()
    }
}


data object UMAIScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        UMAIScreenContent()
    }
}


data object RenameUserScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        RenameUserScreenContent(navigateBack = { navigator.pop() })
    }
}


data object HelpScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        HelpScreenContent(navigateBack = { navigator.pop() })
    }
}

data object SecurityPoliciesScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        UI4_1_SecurityPolicyScreenContent(navigateBack = { navigator.pop() })
    }
}

data object SuspiciousActivityScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        UI4_2_SuspiciousActivityScreenContent(navigateBack = { navigator.pop() })
    }
}

//No Usage
data object AuditReportScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        UI4_3_AuditReportScreenContent(navigateBack = { navigator.pop() })
    }
}

data object ComplianceCheckScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current ?: return
        UI4_4_ComplianceCheckScreenContent(navigateBack = { navigator.pop() })
    }
}