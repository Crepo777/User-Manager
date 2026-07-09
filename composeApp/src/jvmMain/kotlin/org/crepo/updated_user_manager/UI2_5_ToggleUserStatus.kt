package org.crepo.updated_user_manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.nio.charset.Charset

@Composable
fun ToggleUserStatusScreenContent(navigateBack: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var isBlocked by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок
        Text(
            text = StringResources.getString("ui_toggleUserStatus_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Text(
            text = StringResources.getString("ui_toggleUserStatus_subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Поле ввода имени
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(StringResources.getString("ui_toggleUserStatus_username")) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (username.isNotBlank()) {
                        checkUserStatus(username) { blocked ->
                            isBlocked = blocked
                        }
                    }
                }
            )
        )

        // Статус пользователя
        if (username.isNotBlank()) {
            val statusText = if (isBlocked)
                StringResources.getString("ui_toggleUserStatus_status_blocked")
            else
                StringResources.getString("ui_toggleUserStatus_status_active")

            val statusColor = if (isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

            Text(
                text = statusText,
                color = statusColor,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        // Результат операции
        if (result.isNotBlank()) {
            Text(
                text = result,
                color = if (result.startsWith("✅")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
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
                Text(text = StringResources.getString("ui_toggleUserStatus_btn_back"), fontSize = 18.sp)
            }

            Button(
                onClick = {
                    if (username.isNotBlank()) {
                        if (isBlocked) {
                            unblockUser(username) { message ->
                                result = message
                                isBlocked = false
                            }
                        } else {
                            blockUser(username) { message ->
                                result = message
                                isBlocked = true
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isBlocked)
                        StringResources.getString("ui_toggleUserStatus_btn_unblock")
                    else
                        StringResources.getString("ui_toggleUserStatus_btn_block"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun checkUserStatus(username: String, onResult: (Boolean) -> Unit) {
    try {
        val process = ProcessBuilder("net", "user", username)
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            onResult(false)
            return
        }

        // Читаем вывод с правильной кодировкой
        val outputBytes = process.inputStream.readBytes()
        val output = String(outputBytes, Charset.forName("CP866"))

        // Проверяем статус блокировки
        val isBlocked = output.contains("Аккаунт заблокирован") ||
                output.contains("Account disabled")

        onResult(isBlocked)
    } catch (e: Exception) {
        onResult(false)
        Logger.error("Exception during user status check", e)
    }
}

private fun blockUser(username: String, onResult: (String) -> Unit) {
    try {
        val process = ProcessBuilder("net", "user", username, "/active:no")
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        if (exitCode == 0) {
            onResult(StringResources.getString("ui_toggleUserStatus_success_block", username))
            Logger.info("User blocked: $username")
        } else {
            // Читаем вывод с правильной кодировкой
            val outputBytes = process.inputStream.readBytes()
            val output = String(outputBytes, Charset.forName("CP866"))

            val errorMessage = if (output.contains("не существует") || output.contains("cannot be found"))
                StringResources.getString("ui_toggleUserStatus_error_notExist", username)
            else
                StringResources.getString("ui_toggleUserStatus_error_block", exitCode)

            onResult(errorMessage)
            Logger.warning("Failed to block user: $username, error: $output")
        }
    } catch (e: Exception) {
        onResult(StringResources.getString("ui_toggleUserStatus_error_execution", e.message ?: "Unknown error"))
        Logger.error("Exception during user blocking", e)
    }
}

private fun unblockUser(username: String, onResult: (String) -> Unit) {
    try {
        val process = ProcessBuilder("net", "user", username, "/active:yes")
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        if (exitCode == 0) {
            onResult(StringResources.getString("ui_toggleUserStatus_success_unblock", username))
            Logger.info("User unblocked: $username")
        } else {
            // Читаем вывод с правильной кодировкой
            val outputBytes = process.inputStream.readBytes()
            val output = String(outputBytes, Charset.forName("CP866"))

            val errorMessage = if (output.contains("не существует") || output.contains("cannot be found"))
                StringResources.getString("ui_toggleUserStatus_error_notExist", username)
            else
                StringResources.getString("ui_toggleUserStatus_error_unblock", exitCode)

            onResult(errorMessage)
            Logger.warning("Failed to unblock user: $username, error: $output")
        }
    } catch (e: Exception) {
        onResult(StringResources.getString("ui_toggleUserStatus_error_execution", e.message ?: "Unknown error"))
        Logger.error("Exception during user unblocking", e)
    }
}