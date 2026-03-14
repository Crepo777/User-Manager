package org.crepo.updated_user_manager

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
import java.awt.Color
import java.lang.ProcessBuilder

@Composable
fun UI3_1(navigateBack: () -> Unit) {
    //val warningContainer = Color(0x33FF6B6B) // полупрозрачный красный
    var username by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var useEmptyPassword by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }

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
            text = "Изменение пароля",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
        )

        // Поле: Имя пользователя
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Имя пользователя") },
            placeholder = { Text("user1") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Чекбокс: Пустой пароль
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = useEmptyPassword,
                onCheckedChange = { useEmptyPassword = it }
            )
            Text(
                text = "Установить пустой пароль",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Поле: Новый пароль (активно только если не выбран пустой пароль)
        val isPasswordFieldEnabled = !useEmptyPassword

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Новый пароль") },
            placeholder = { Text("••••••") },
            visualTransformation = if (newPassword.isEmpty()) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = isPasswordFieldEnabled,
            supportingText = {
                if (!isPasswordFieldEnabled) {
                    Text("Поле отключено", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )

        // Подтверждение пароля
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Подтвердите пароль") },
            placeholder = { Text("••••••") },
            visualTransformation = if (confirmPassword.isEmpty()) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = isPasswordFieldEnabled
        )

        // Примечание
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = "💡 Совет:\n" +
                        "• При установке пустого пароля вход будет без защиты\n" +
                        "• Убедитесь, что имя пользователя введено правильно",
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
                        return@Button
                    }

                    if (useEmptyPassword) {
                        // Устанавливаем пустой пароль
                        try {
                            val process = ProcessBuilder("net", "user", username, "")
                                .redirectErrorStream(true)
                                .start()

                            val exitCode = process.waitFor()
                            if (exitCode == 0) {
                                result = "✅ Пароль пользователя '$username' очищен (пустой)"
                            } else {
                                val output = process.inputStream.bufferedReader().readText()
                                result = "❌ Ошибка: $exitCode\n$output.take(200)"
                            }
                        } catch (e: Exception) {
                            result = "❌ Ошибка: ${e.message}"
                        }
                    } else {
                        // Проверка паролей
                        if (newPassword != confirmPassword) {
                            result = "❗ Пароли не совпадают"
                            return@Button
                        }
                        if (newPassword.isBlank()) {
                            result = "❗ Пароль не может быть пустым"
                            return@Button
                        }

                        try {
                            val process = ProcessBuilder("net", "user", username, newPassword)
                                .redirectErrorStream(true)
                                .start()

                            val exitCode = process.waitFor()
                            if (exitCode == 0) {
                                result = "✅ Пароль пользователя '$username' изменён"
                            } else {
                                val output = process.inputStream.bufferedReader().readText()
                                result = if (output.contains("не существует"))
                                    "❌ Пользователь '$username' не найден"
                                else
                                    "❌ Ошибка: $exitCode\n$output.take(200)"
                            }
                        } catch (e: Exception) {
                            result = "❌ Ошибка выполнения: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (useEmptyPassword) "Установить пустой" else "Изменить",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
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
}