//ЭТОТ ЭКРАН НЕ ИСПОЛЬЗУЕТСЯ
package org.crepo.updated_user_manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import java.util.Locale

@Composable
fun UMAIScreenContent() {
    val navigator = LocalNavigator.current ?: return
    var userInput by remember { mutableStateOf("") }
    var aiResponse by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    var commandToExecute by remember { mutableStateOf<CommandResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //Заголовок
        Text(
            text = "UMAI (User Manager AI)",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Безопасный помощник для управления пользователями",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        //Область чата
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Примеры команд:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("• Создай пользователя Ivan с паролем password123")
                Text("• Удали пользователя test")
                Text("• Назначь права на C:\\Folder для user1")

                //Ответ ИИ
                if (aiResponse.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "UMAI:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(aiResponse)
                }
            }
        }

        //Поле ввода
        OutlinedTextField(
            value = userInput,
            onValueChange = { userInput = it },
            label = { Text("Введите команду...") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    val result = processCommand(userInput)
                    aiResponse = result.description
                    commandToExecute = result
                    showConfirmation = result.action != "unknown"
                    userInput = ""
                }
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        val result = processCommand(userInput)
                        aiResponse = result.description
                        commandToExecute = result
                        showConfirmation = result.action != "unknown"
                        userInput = ""
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        )

        Button(
            onClick = { navigator.pop() },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Назад",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    //Диалог подтверждения
    if (showConfirmation && commandToExecute != null) {
        val cmd = commandToExecute!!
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Подтверждение команды") },
            text = {
                Column {
                    Text("Я понял ваш запрос как:", fontWeight = FontWeight.Bold)
                    Text(cmd.description)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Будут выполнены следующие действия:", fontWeight = FontWeight.Bold)
                    Text(cmd.command)

                    if (cmd.isDangerous) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "⚠\uFE0F Эта команда может быть опасной!",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    executeCommand(cmd)
                    showConfirmation = false
                }) {
                    Text("Подтвердить")
                }
            },
            dismissButton = {
                Button(onClick = { showConfirmation = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

//Результат обработки команды
data class CommandResult(
    val action: String,
    val description: String,
    val command: String,
    val params: Map<String, String>,
    val isDangerous: Boolean = false
)

//Обработка команд
fun processCommand(text: String): CommandResult {
    val normalized = text.lowercase().replace("[^a-zа-я0-9]", " ")

    return when {
        //Создание пользователя
        (normalized.contains("создать") || normalized.contains("сделать") || normalized.contains("добавить")) &&
                (normalized.contains("пользователь") || normalized.contains("юзер") || normalized.contains("user")) -> {
            val username = extractUsername(normalized) ?: "newuser"
            val password = extractPassword(normalized) ?: ""
            CommandResult(
                action = "create_user",
                description = "Создать пользователя $username",
                command = "net user $username ${password.ifEmpty { "*" }} /add",
                params = mapOf("username" to username, "password" to password),
                isDangerous = false
            )
        }

        //Удаление пользователя
        (normalized.contains("удалить") || normalized.contains("удали")) &&
                (normalized.contains("пользователь") || normalized.contains("юзер") || normalized.contains("user")) -> {
            val username = extractUsername(normalized) ?: "user"
            CommandResult(
                action = "delete_user",
                description = "Удалить пользователя $username",
                command = "net user $username /delete",
                params = mapOf("username" to username),
                isDangerous = true
            )
        }

        //Назначение прав
        (normalized.contains("назначить") || normalized.contains("дать") || normalized.contains("выдать")) &&
                (normalized.contains("права") || normalized.contains("разрешение")) -> {
            val username = extractUsername(normalized) ?: "user"
            val path = extractPath(normalized) ?: "C:\\"
            CommandResult(
                action = "set_permissions",
                description = "Назначить права для $username на $path",
                command = "icacls \"$path\" /grant \"$username\":(F)",
                params = mapOf("username" to username, "path" to path),
                isDangerous = path.contains("Windows") || path.contains("System32")
            )
        }

        else -> CommandResult(
            action = "unknown",
            description = "Не удалось распознать команду. Попробуйте:\n" +
                    "- Создать пользователя [имя] с паролем [пароль]\n" +
                    "- Удалить пользователя [имя]\n" +
                    "- Назначить права на [путь] для [имя]",
            command = "",
            params = emptyMap()
        )
    }
}

fun extractUsername(text: String): String? {
    val patterns = listOf(
        "пользователь\\s+(\\w+)",
        "юзер\\s+(\\w+)",
        "user\\s+(\\w+)",
        "\\b(\\w+)\\s+с\\s+паролем",
        "\\b(\\w+)\\s+с\\s+password"
    )

    for (pattern in patterns) {
        val regex = Regex(pattern)
        regex.find(text)?.groups?.get(1)?.value?.let { return it }
    }

    return null
}

fun extractPassword(text: String): String? {
    val patterns = listOf(
        "паролем\\s+(\\w+)",
        "password\\s+(\\w+)",
        "с\\s+паролем\\s+(\\w+)"
    )

    for (pattern in patterns) {
        val regex = Regex(pattern)
        regex.find(text)?.groups?.get(1)?.value?.let { return it }
    }

    return null
}

fun extractPath(text: String): String? {
    val patterns = listOf(
        "на\\s+([a-z]:\\\\[^\\s]+)",
        "для\\s+([a-z]:\\\\[^\\s]+)",
        "в\\s+([a-z]:\\\\[^\\s]+)"
    )

    for (pattern in patterns) {
        val regex = Regex(pattern)
        regex.find(text)?.groups?.get(1)?.value?.let { return it }
    }

    return null
}

//Выполнение команды
fun executeCommand(command: CommandResult) {
    when (command.action) {
        "create_user" -> {
            val username = command.params["username"] ?: return
            val password = command.params["password"] ?: ""

            try {
                val process = ProcessBuilder(
                    "net", "user", username, if (password.isEmpty()) "*" else password, "/add"
                ).redirectErrorStream(true).start()

                val exitCode = process.waitFor()
                val output = process.inputStream.bufferedReader().readText()

                if (exitCode == 0) {
                    Logger.info("User created via UMAI: $username")
                } else {
                    Logger.warning("Failed to create user via UMAI: $username, error: $output")
                }
            } catch (e: Exception) {
                Logger.error("Exception during user creation via UMAI", e)
            }
        }

        "delete_user" -> {
            val username = command.params["username"] ?: return

            try {
                val process = ProcessBuilder("net", "user", username, "/delete")
                    .redirectErrorStream(true).start()

                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    Logger.info("User deleted via UMAI: $username")
                } else {
                    Logger.warning("Failed to delete user via UMAI: $username")
                }
            } catch (e: Exception) {
                Logger.error("Exception during user deletion via UMAI", e)
            }
        }

        "set_permissions" -> {
            val username = command.params["username"] ?: return
            val path = command.params["path"] ?: return

            try {
                val process = ProcessBuilder(
                    "icacls", path, "/grant", "$username:(F)"
                ).redirectErrorStream(true).start()

                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    Logger.info("Permissions set via UMAI: user=$username, path=$path")
                } else {
                    Logger.warning("Failed to set permissions via UMAI: user=$username, path=$path")
                }
            } catch (e: Exception) {
                Logger.error("Exception during permissions setting via UMAI", e)
            }
        }
    }
}