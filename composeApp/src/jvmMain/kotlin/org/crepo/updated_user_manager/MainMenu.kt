package org.crepo.updated_user_manager

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontVariation.weight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import org.crepo.updated_user_manager.CreateUserScreen
import org.jetbrains.compose.resources.ExperimentalResourceApi
//import org.jetbrains.compose.resources.resource

@Composable
fun MainMenu() {
    val navigator = LocalNavigator.current ?: return

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
            text = "Система управления учётными записями Windows",
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
                title = "📋 Общее",
                icon = "ℹ️",
                items = listOf(
                    //MenuItem("Информация", onClick = { /* пока пусто */ })
                )
            )

            // Блок 2: Пользователи
            CategoryCard(
                title = "👤 Пользователи",
                icon = "👥",
                items = listOf(
                    MenuItem("Создать пользователя", onClick = { navigator.push(CreateUserScreen) }),
                    //MenuItem("Удалить пользователя", onClick = { /* TODO: navigate to delete */ }),
                    //MenuItem("Изменить пароль", onClick = { /* TODO: navigate to password */ })
                )
            )

            // Блок 3: Файлы и права
            CategoryCard(
                title = "📁 Файлы",
                icon = "🔒",
                items = listOf(
                    //MenuItem("Назначить права", onClick = { /* TODO: navigate to permissions */ }),
                    //MenuItem("Отозвать права", onClick = { /* TODO: navigate to revoke */ })
                )
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
            Button(
                onClick = { /* Открыть настройки */ },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = "⚙ Настройки", fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun CategoryCard(
    title: String,
    icon: String,
    items: List<MenuItem>
) {
    Card(
//        modifier = Modifier.run { weight(1) } as Modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
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

// --- Дополнительно: если хочешь иконки из ресурсов ---
@OptIn(ExperimentalResourceApi::class)
@Composable
private fun IconImage(resId: String) {
    Image(
        painter = painterResource(("drawable/$resId")),
        contentDescription = null,
        modifier = Modifier.size(24.dp)
    )
}