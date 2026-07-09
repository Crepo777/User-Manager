
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
import java.util.Locale

@Composable
fun RenameUserScreenContent(navigateBack: () -> Unit) {
    var oldUsername by remember { mutableStateOf("") }
    var newUsername by remember { mutableStateOf("") }
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
            text = StringResources.getString("ui_renameUser_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Text(
            text = StringResources.getString("ui_renameUser_subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Поля ввода
        OutlinedTextField(
            value = oldUsername,
            onValueChange = { oldUsername = it },
            label = { Text(StringResources.getString("ui_renameUser_oldName")) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = newUsername,
            onValueChange = { newUsername = it },
            label = { Text(StringResources.getString("ui_renameUser_newName")) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // Здесь можно вызвать executeRename, если хочешь
                }
            )
        )

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
                Text(text = StringResources.getString("ui_renameUser_btn_back"), fontSize = 18.sp)
            }

            Button(
                onClick = {
                    if (oldUsername.isBlank() || newUsername.isBlank()) {
                        result = StringResources.getString("ui_renameUser_error_fieldsRequired")
                        return@Button
                    }

                    executeRename(oldUsername, newUsername) { message ->
                        result = message
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = StringResources.getString("ui_renameUser_btn_rename"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun executeRename(oldUsername: String, newUsername: String, onResult: (String) -> Unit) {
    val charset = if (Locale.getDefault().language == "ru")
        Charset.forName("CP866")
    else
        Charset.forName("UTF-8")

    try {
        // Используем PowerShell, так как wmic устаревает
        val process = ProcessBuilder(
            "powershell",
            "-Command",
            "Rename-LocalUser -Name '$oldUsername' -NewName '$newUsername'"
        ).redirectErrorStream(true).start()

        val exitCode = process.waitFor()
        val outputBytes = process.inputStream.readBytes()
        val output = String(outputBytes, Charset.forName("CP866"))

        if (exitCode == 0) {
            onResult(StringResources.getString("ui_renameUser_success", oldUsername, newUsername))
            Logger.info("User renamed: $oldUsername -> $newUsername")
        } else {
            val errorMessage = when {
                output.contains("не существует") || output.contains("cannot be found") ->
                    StringResources.getString("ui_renameUser_error_notExist", oldUsername)
                output.contains("уже существует") || output.contains("already exists") ->
                    StringResources.getString("ui_renameUser_error_alreadyExists", newUsername)
                output.contains("права") || output.contains("permission") || output.contains("access denied") ->
                    StringResources.getString("ui_renameUser_error_permissions")
                else ->
                    StringResources.getString("ui_renameUser_error_code", exitCode, output.take(100))
            }
            onResult(errorMessage)
            Logger.warning("Failed to rename user: $oldUsername -> $newUsername, error: $output")
        }
    } catch (e: Exception) {
        onResult(StringResources.getString("ui_renameUser_error_execution", e.message ?: "Unknown error"))
        Logger.error("Exception during user rename", e)
    }
}