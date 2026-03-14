package org.crepo.updated_user_manager

import org.jetbrains.compose.ui.tooling.preview.Preview


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import java.lang.ProcessBuilder

@Composable
fun UI2_1(navigateBack: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок
        Text(
            text = "Создание пользователя",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
        )

        // Поле: Имя
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Имя пользователя") },
            placeholder = { Text("user1") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = {
                Text("Введите имя нового пользователя")
            }
        )

        // Поле: Пароль
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            placeholder = { Text("••••••") },
            visualTransformation = if (password.isEmpty()) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = {
                Text("Оставьте пустым, если не хотите задавать пароль")
            }
        )

        // Примечание
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = "⚠️ ВНИМАНИЕ:\n" +
                        "• Приложение должно запускаться от имени администратора\n" +
                        "• Если пользователь уже существует — будет ошибка",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

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
                        return@Button                    }

                    try {
                        val command = mutableListOf("net", "user", username)
                        if (password.isNotBlank()) {
                            command.add(password)
                        }
                        command.add("/add")
                        val process = ProcessBuilder(command)
                            .redirectErrorStream(true)
                            .start()

                        val exitCode = process.waitFor()
                        val output = process.inputStream.bufferedReader().readText()

                        if (exitCode == 0) {
                            result = "✅ Пользователь '$username' успешно создан!"
                        } else {
                            result = if (output.contains("уже существует"))
                                "⚠️ Пользователь '$username' уже существует"
                            else
                                "❌ Ошибка: $exitCode\n$output.take(200)"
                        }
                    } catch (e: Exception) {
                        result = "❌ Ошибка выполнения: ${e.message}"
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Создать", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                        containerColor = MaterialTheme.colorScheme.errorContainer,                        contentColor = MaterialTheme.colorScheme.onErrorContainer
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
}

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun PreviewUI2_1() {
    MaterialTheme {
        UI2_1(navigateBack = {})
    }
}