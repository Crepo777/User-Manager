package org.crepo.updated_user_manager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import java.lang.ProcessBuilder

@Composable
fun UI2_2(navigateBack: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) } // Диалог подтверждения

    val navigator = LocalNavigator.current ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок
        Text(
            text = "Удаление пользователя",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
        )

        // Предупреждение
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚠️ ВНИМАНИЕ!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onError
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Это необратимое действие. Будут удалены:\n• Учётная запись\n• Папка пользователя\n• Все файлы в ней",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }

        // Поле ввода
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Имя пользователя") },
            placeholder = { Text("user1") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Кнопки
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = navigateBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.outlineVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(text = "Назад", fontSize = 18.sp)
            }

            Button(
                onClick = {
                    if (username.isBlank()) {
                        result = "❗ Введите имя пользователя"
                    } else {
                        showDeleteDialog = true // Показать диалог
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(text = "Удалить", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Результат
        if (result.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = when {
                    result.startsWith("✅") -> CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    result.startsWith("⚠️") -> CardDefaults.cardColors(
                        //containerColor = MaterialTheme.colorScheme.warningContainer,
                        //contentColor = MaterialTheme.colorScheme.onWarningContainer
                    )
                    else -> CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            ) {
                Text(
                    text = result,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // Диалог подтверждения
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Подтвердите удаление",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Вы действительно хотите удалить пользователя '$username'?\n" +
                            "Все его данные будут безвозвратно потеряны.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        executeDelete(username, onResult = { result = it })
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// Функция выполнения удаления
private fun executeDelete(username: String, onResult: (String) -> Unit) {
    try {
        // 1. Удаляем учётную запись
        val process1 = ProcessBuilder("net", "user", username, "/delete")
            .redirectErrorStream(true)
            .start()

        val exitCode1 = process1.waitFor()
        val output1 = process1.inputStream.bufferedReader().readText()

        if (exitCode1 != 0) {
            onResult(
                if (output1.contains("не существует"))
                    "❌ Пользователь '$username' не найден"
                else
                    "❌ Ошибка удаления: код $exitCode1"
            )
            return
        }

        // 2. Удаляем папку пользователя
        val userProfile = System.getProperty("user.home")
        val userFolder = "$userProfile\\..\\Users\\$username"
        val process2 = ProcessBuilder("cmd", "/c", "rmdir", "/s", "/q", userFolder)
            .redirectErrorStream(true)
            .start()

        val exitCode2 = process2.waitFor()

        if (exitCode2 == 0) {
            onResult("✅ Пользователь '$username' и его папка успешно удалены")
        } else {
            val output2 = process2.inputStream.bufferedReader().readText()
            onResult("⚠️ Учётная запись удалена, но папку удалить не удалось:\n$output2")
        }
    } catch (e: Exception) {
        onResult("❌ Ошибка: ${e.message}")
    }
}