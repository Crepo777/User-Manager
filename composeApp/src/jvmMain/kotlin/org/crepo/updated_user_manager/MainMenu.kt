package org.crepo.updated_user_manager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import java.util.*
import androidx.compose.ui.graphics.Color

@Composable
fun MainMenu(navigateBack: () -> Unit) {

    val (locale, setLocale) = remember { mutableStateOf(LanguageManager.currentLocale) }

    // При смене — обновляем глобальный язык
    SideEffect {
        LanguageManager.setLocale(locale)
    }

    val navigator = LocalNavigator.current ?: return

    // Состояние для диалога языка
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Заголовок
        Text(
            text = "User Manager",
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
                    MenuItem(StringResources.getString("ui_mainMenu_mainCategory_main"), onClick = navigateBack)
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
                    MenuItem(StringResources.getString("ui_mainMenu_usersCategory_changePassword")) { navigator.push(ChangePasswordScreen) }
                ),
                modifier = Modifier.weight(1f)
            )

            // Блок 3: Файлы и права
            CategoryCard(
                title = StringResources.getString("ui_mainMenu_filesCategory_title"),
                icon = "🔒",
                items = listOf(
                    MenuItem(StringResources.getString("ui_mainMenu_filesCategory_setPermissions"), onClick = navigateBack),
                    MenuItem(StringResources.getString("ui_mainMenu_filesCategory_backPermissions"), onClick = navigateBack)
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Нижняя панель
        Spacer(modifier = Modifier.weight(1f))
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

            // Кнопка Настройки
            Button(
                onClick = navigateBack,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = StringResources.getString("ui_mainMenu_options_title"), fontSize = 14.sp)
            }
        }
    }

    // Диалог выбора языка — должен быть за пределами Column, но внутри @Composable
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Выберите язык") },
            text = {
                Column {
                    LanguageItem(label = "🇷🇺 Русский", langCode = "ru") {
                        AppLanguage.current = Locale("ru")
                        showLanguageDialog = false
                    }
                    LanguageItem(label = "🇬🇧 English", langCode = "en") {
                        AppLanguage.current = Locale("en")
                        showLanguageDialog = false
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showLanguageDialog = false }) {
                    Text("Закрыть")
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = icon,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,   // ✅ Правильно — параметр Text(), не modifier
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            items.forEach { item ->
                MenuItemButton(item)
            }
        }
    }
}

@Composable
private fun LanguageItem(label: String, langCode: String, onClick: () -> Unit) {
    val isSelected = AppLanguage.current.language == langCode
    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.surfaceVariant
    else
        Color(0x00000000) // Полностью прозрачный цвет

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() },
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = label,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
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

data class MenuItem(
    val label: String,
    val onClick: () -> Unit
)