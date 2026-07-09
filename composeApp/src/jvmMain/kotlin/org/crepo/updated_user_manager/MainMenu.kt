package org.crepo.updated_user_manager

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import java.io.File
import java.util.*

@Composable
fun MainMenu() {
    val navigator = LocalNavigator.current ?: return
    //println(checkCodePage())

    // Состояние диалога
    var showLanguageDialog by remember { mutableStateOf(false) }
    // Состояние для диалога "Открыть..."
    var showOpenDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Заголовок

        // Проверка прав администратора
        val isRunningAsAdmin by remember {
            derivedStateOf { AppConfig.isRunningAsAdmin() }
        }

        // Баннер предупреждения о недостатке прав
        if (!isRunningAsAdmin) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = StringResources.getString("ui_admin_warning_banner_title"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = StringResources.getString("ui_admin_warning_banner_hint"),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Заголовок
        Text(
            text = StringResources.getString("app_name"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = StringResources.getString("ui_mainMenu_subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Разделитель
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Основные блоки (в виде Card)
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Блок 1: Общее
            CategoryCard(
                title = StringResources.getString("ui_mainMenu_mainCategory_title"),
                icon = "ℹ️",
                items = listOf(
                    MenuItem(StringResources.getString("ui_mainMenu_mainCategory_main"), onClick = {navigator.push(HelpScreen)})
                ),
                modifier = Modifier.weight(1f)
            )

            // Блок 2: Пользователи
            CategoryCard(
                title = StringResources.getString("ui_mainMenu_usersCategory_title"),
                icon = "👥",
                items = listOf(
                    MenuItem(StringResources.getString("ui_mainMenu_usersCategory_create")) { navigator.push(CreateUserScreen) },
                    MenuItem(StringResources.getString("ui_mainMenu_usersCategory_delete")) { navigator.push(DeleteUserScreen) },
                    MenuItem(StringResources.getString("ui_mainMenu_usersCategory_changePassword")) { navigator.push(ChangePasswordScreen) },
                    MenuItem(StringResources.getString("ui_mainMenu_usersCategory_rename")) { navigator.push(RenameUserScreen) },
                    MenuItem(StringResources.getString("ui_mainMenu_usersCategory_status")) { navigator.push(UserAccountScreen) },
                    MenuItem(StringResources.getString("ui_mainMenu_usersCategory_usersGroups")) { navigator.push(ManageUsersInGroupsScreen) }
                    //MenuItem(StringResources.getString("ui_mainMenu_usersCategory_managingGroups")) { navigator.push(ManageGroupsScreen) }
                ),
                modifier = Modifier.weight(1f)
            )

            // Блок 3: Файлы и права
            CategoryCard(
                title = StringResources.getString("ui_mainMenu_filesCategory_title"),
                icon = "🔒",
                items = listOf(
                    MenuItem(StringResources.getString("ui_mainMenu_filesCategory_setPermissions")) { navigator.push(SetPermissionsScreen) },
                    MenuItem(StringResources.getString("ui_mainMenu_filesCategory_viewPermissions")) { navigator.push(ViewPermissionsScreen) },
                    MenuItem(StringResources.getString("ui_mainMenu_filesCategory_changeOwner")) { navigator.push(ChangeOwnerScreen) },
                    MenuItem(StringResources.getString("ui_mainMenu_filesCategory_fileAttributes")) { navigator.push(FileAttributesScreen) }
                ),
                modifier = Modifier.weight(1f)
            )

            // Блок 4: Политика безопасности
            CategoryCard(
                title = StringResources.getString("ui_mainMenu_securityPolicies_title"),
                icon = "\uD83D\uDEE1\uFE0F",
                items = listOf(
                    MenuItem(StringResources.getString("ui_mainMenu_securityPolicies_main")) { navigator.push(SecurityPoliciesScreen) },
                    MenuItem(StringResources.getString("ui_mainMenu_securityPolicies_suspiciousActivity")) { navigator.push(SuspiciousActivityScreen) },
                    MenuItem(StringResources.getString("ui_mainMenu_securityPolicies_complianceCheck")) { navigator.push(ComplianceCheckScreen) }
                ),
                modifier = Modifier.weight(1f)
            )

            // Блок 4: Экспорт данных
            //CategoryCard(
            //    title = StringResources.getString("ui_mainMenu_exportData_title"),
            //    icon = "📊",
            //    items = listOf(
            //        MenuItem(StringResources.getString("ui_mainMenu_exportData_users_title")) {
            //            val csv = StringResources.getString("ui_mainMenu_exportData_users_text") +
            //                    UserManager.exportUsersToCsv()
            //            val exportFile = File(System.getProperty("user.home"), "usermanager_users.csv")
            //            exportFile.writeText(csv)
//
            //            // Открываем папку с экспортированным файлом
            //            ProcessBuilder("explorer", "/select,${exportFile.absolutePath}").start()
            //        },
            //        MenuItem(StringResources.getString("ui_mainMenu_exportData_logs_title")) {
            //            val csv = StringResources.getString("ui_mainMenu_exportData_logs_text") + Logger.exportToCsv()
            //            val exportFile = File(System.getProperty("user.home"), "usermanager_logs.csv")
            //            exportFile.writeText(csv)
//
            //            // Открываем папку с экспортированным файлом
            //            ProcessBuilder("explorer", "/select,${exportFile.absolutePath}").start()
            //        }
            //    ),
            //    modifier = Modifier.weight(1f)
            //)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Нижняя панель
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "v1.0.0 | © 2026 Крайнов Иван",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Кнопка выбора языка
            IconButton(
                onClick = { showLanguageDialog = true },
                modifier = Modifier.size(36.dp)
            ) {
                Text(
                    text = if (LanguageManager.currentLocale.language == "ru") "ru" else "en",
                    fontSize = 18.sp
                )
            }

            // В нижней панели, между кнопками выбора языка и "Открыть..."
            var showUmaiWarning by remember { mutableStateOf(false) }


            // Кнопка UMAI
            IconButton(
                onClick = { showUmaiWarning = true },
                modifier = Modifier.size(36.dp)
            ) {
                Text(
                    text = "🤖",
                    fontSize = 18.sp
                )
            }

            // Кнопка "Открыть..."
            IconButton(
                onClick = { showOpenDialog = true },
                modifier = Modifier.size(36.dp)
            ) {
                Text(
                    text = "📁",
                    fontSize = 18.sp
                )
            }

            // Диалог предупреждения о безопасности UMAI
            if (showUmaiWarning) {
                AlertDialog(
                    onDismissRequest = { showUmaiWarning = false },
                    title = { Text(StringResources.getString("ui_mainMenu_UMAI_warning_title")) },
                    text = {
                        Column {
                            Text(
                                text = StringResources.getString("ui_mainMenu_UMAI_warning_warning1"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = StringResources.getString("ui_mainMenu_UMAI_warning_warning2"),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = StringResources.getString("ui_mainMenu_UMAI_warning_warning3"),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            showUmaiWarning = false
                            //navigator.push(UMAIScreen)
                        }) {
                            Text(StringResources.getString("ui_mainMenu_UMAI_warning_close"))
                        }
                    },
                    //dismissButton = {
                    //    Button(onClick = { showUmaiWarning = false }) {
                    //        Text("Назад")
                    //    }
                    //}
                )
            }



            // Кнопка "Настройки"
            //Button(
            //    onClick = {
            //        // Временный сброс предупреждения (только для отладки)
            //        AppConfig.resetDisclaimer()
            //        println("Состояние предупреждения сброшено. При следующем запуске появится экран предупреждения.")
            //    },
            //    shape = RoundedCornerShape(8.dp),
            //    modifier = Modifier.height(36.dp)
            //) {
            //    Text(text = "Сброс предупреждения", fontSize = 14.sp)
            //}
                Button(
                onClick = { navigator.push(SettingsScreen) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = StringResources.getString("ui_mainMenu_options_title"), fontSize = 14.sp)
            }
        }
    }

    // Диалог выбора языка
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(StringResources.getString("ui_mainMenu_interfaceLanguage_title")) },
            text = {
                Column {
                    LanguageItem(label = "ru - Русский", langCode = "ru") {
                        LanguageManager.setLocale(Locale("ru"))
                        showLanguageDialog = false
                    }

                    LanguageItem(label = "en - English", langCode = "en") {
                        LanguageManager.setLocale(Locale("en"))
                        showLanguageDialog = false
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showLanguageDialog = false }) {
                    Text(StringResources.getString("ui_mainMenu_interfaceLanguage_back"))
                }
            }
        )
    }

    // Диалог выбора системных утилит
    if (showOpenDialog) {
        AlertDialog(
            onDismissRequest = { showOpenDialog = false },
            title = { Text(StringResources.getString("ui_mainMenu_utilities_title")) },
            text = {
                Column {
                    SystemToolItem(
                        icon = "📊",
                        title = StringResources.getString("ui_mainMenu_utilities_taskManager_title"),
                        description = StringResources.getString("ui_mainMenu_utilities_taskMenager_hint"),
                        onClick = {
                            openSystemTool("taskmgr")
                            showOpenDialog = false
                        }
                    )
                    SystemToolItem(
                        icon = "🔍",
                        title = StringResources.getString("ui_mainMenu_utilities_regedit_title"),
                        description = StringResources.getString("ui_mainMenu_utilities_regedit_hint"),
                        onClick = {
                            openSystemTool("regedit")
                            showOpenDialog = false
                        }
                    )
                    SystemToolItem(
                        icon = "🧹",
                        title = StringResources.getString("ui_mainMenu_utilities_cleanmgr_title"),
                        description = StringResources.getString("ui_mainMenu_utilities_cleanmgr_hint"),
                        onClick = {
                            openSystemTool("cleanmgr")
                            showOpenDialog = false
                        }
                    )
                    SystemToolItem(
                        icon = "💾",
                        title = StringResources.getString("ui_mainMenu_utilities_diskmgmt_title"),
                        description = StringResources.getString("ui_mainMenu_utilities_diskmgmt_hint"),
                        onClick = { openSystemTool("diskmgmt.msc") }
                    )
                    SystemToolItem(
                        icon = "⚙️",
                        title = StringResources.getString("ui_mainMenu_utilities_services_title"),
                        description = StringResources.getString("ui_mainMenu_utilities_services_hint"),
                        onClick = { openSystemTool("services.msc") }
                    )
                    SystemToolItem(
                        icon = "🛡️",
                        title = StringResources.getString("ui_mainMenu_utilities_gpedit_title"),
                        description = StringResources.getString("ui_mainMenu_utilities_gpedit_hint"),
                        onClick = { openSystemTool("gpedit.msc") }
                    )
                    SystemToolItem(
                        icon = "🖥️",
                        title = StringResources.getString("ui_mainMenu_utilities_compmgmt_title"),
                        description = StringResources.getString("ui_mainMenu_utilities_compmgmt_hint"),
                        onClick = { openSystemTool("compmgmt.msc") }
                    )
                    SystemToolItem(
                        icon = "⏰",
                        title = StringResources.getString("ui_mainMenu_utilities_taskschd_title"),
                        description = StringResources.getString("ui_mainMenu_utilities_taskschd_hint"),
                        onClick = { openSystemTool("taskschd.msc") }
                    )
                    SystemToolItem(
                        icon = "🔌",
                        title = StringResources.getString("ui_mainMenu_utilities_devmgmt_title"),
                        description = StringResources.getString("ui_mainMenu_utilities_devmgmt_hint"),
                        onClick = { openSystemTool("devmgmt.msc") }
                    )
                    SystemToolItem(
                        icon = "🔥",
                        title = StringResources.getString("ui_mainMenu_utilities_wf_title"),
                        description = StringResources.getString("ui_mainMenu_utilities_wf_hint"),
                        onClick = { openSystemTool("wf.msc") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showOpenDialog = false }) {
                    Text(StringResources.getString("ui_mainMenu_utilities_close"))
                }
            }
        )
    }
}

@Composable
private fun CategoryCard(
    title: String,
    icon: String,
    items: List<MenuItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = icon,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Spacer(Modifier.height(12.dp))
            items.forEach { MenuItemButton(it) }
        }
    }
}

@Composable
private fun LanguageItem(label: String, langCode: String, onClick: () -> Unit) {
    val isSelected = LanguageManager.currentLocale.language == langCode
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() },
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = label,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun MenuItemButton(item: MenuItem) {
    Button(
        onClick = item.onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        Text(
            text = item.label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// "Открыть..." выбор
@Composable
private fun SystemToolItem(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor by animateColorAsState(
        targetValue = if (isHovered) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        animationSpec = tween(150)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource
            ),
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = icon,
                fontSize = 18.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
fun openSystemTool(tool: String) {
    try {
        ProcessBuilder("cmd", "/c", "start", tool).start()
    } catch (e: Exception) {
        println(StringResources.getString("ui_mainMenu_utilities_error") + tool + ": " + {e.message})
    }
}

data class MenuItem(
    val label: String,
    val onClick: () -> Unit
)