package org.crepo.updated_user_manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.lang.ProcessBuilder

@Composable
fun UI2_1(navigateBack: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
        Text(
            text = "Создание пользователя",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
        )

        // Имя
        Text(
            text = "Введите имя нового пользователя:",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.Center).padding(bottom = 50.dp)
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Имя") },
            placeholder = { Text("user1") },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 275.dp),
            singleLine = true
        )

        // Пароль
        Text(
            text = "Введите пароль для этого пользователя",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 80.dp, bottom = 10.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            placeholder = { Text("••••••") },
            visualTransformation = if (password.isEmpty()) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 120.dp),
            singleLine = true
        )

        Text(
            text = "Примечание: если не хотите добавлять пароль, оставьте поле пустым",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 10.dp)
        )

        // Кнопка: Создать
        Button(
            modifier = Modifier
                .padding(top = 180.dp)
                .align(Alignment.Center),
            onClick = {
                if (username.isBlank()) {
                    result = "❗ Введите имя пользователя"
                    return@Button
                }

                val command = mutableListOf("net", "user", username)
                if (password.isNotBlank()) {
                    command.add(password)
                }
                command.add("/add")

                try {
                    val process = ProcessBuilder(command)
                        .redirectErrorStream(true)
                        .start()

                    val exitCode = process.waitFor()
                    if (exitCode == 0) {
                        result = "✅ Пользователь '$username' успешно создан!"
                    } else {
                        val output = process.inputStream.bufferedReader().readText()
                        result = if (output.contains("уже существует"))
                            "⚠️ Пользователь '$username' уже существует"
                        else
                            "❌ Ошибка: код $exitCode"
                    }
                } catch (e: Exception) {
                    result = "❌ Ошибка: ${e.message}"
                }
            }
        ) {
            Text(text = "Создать", fontSize = 30.sp)
        }

        // Кнопка: Назад
        Button(
            modifier = Modifier
                .padding(top = 310.dp)
                .align(Alignment.Center),
            onClick = navigateBack
        ) {
            Text(text = "Назад", fontSize = 30.sp)
        }

        // Результат
        if (result.isNotEmpty()) {
            Text(
                text = result,
                color = when {
                    result.startsWith("✅") -> Color.Green
                    result.startsWith("⚠️") -> Color.Yellow
                    result.startsWith("❌") || result.startsWith("❗") -> Color.Red
                    else -> Color.Black
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
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