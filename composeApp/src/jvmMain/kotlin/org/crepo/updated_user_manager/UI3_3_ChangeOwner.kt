package org.crepo.updated_user_manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
fun ChangeOwnerScreenContent(navigateBack: () -> Unit) {
    var path by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
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
            text = StringResources.getString("ui_changeOwner_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Text(
            text = StringResources.getString("ui_changeOwner_subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Поля ввода
        OutlinedTextField(
            value = path,
            onValueChange = { path = it },
            label = { Text(StringResources.getString("ui_changeOwner_path")) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(StringResources.getString("ui_changeOwner_username")) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
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
                Text(text = StringResources.getString("ui_changeOwner_btn_back"), fontSize = 18.sp)
            }

            Button(
                onClick = {
                    if (path.isBlank() || username.isBlank()) {
                        result = StringResources.getString("ui_changeOwner_error_fieldsRequired")
                        return@Button
                    }

                    executeChangeOwner(path, username) { message ->
                        result = message
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = StringResources.getString("ui_changeOwner_btn_change"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun executeChangeOwner(path: String, username: String, onResult: (String) -> Unit) {
    val charset = if (Locale.getDefault().language == "ru")
        Charset.forName("CP866")
    else
        Charset.forName("UTF-8")

    try {

        // Сначала получаем владение
        val takeownProcess = ProcessBuilder(
            "takeown", "/f", path, "/r", "/d", "y"
        ).redirectErrorStream(true).start()

        val takeownExitCode = takeownProcess.waitFor()
        val process1 = ProcessBuilder("net", "user", username, "/delete")
            .redirectErrorStream(true)
            .start()
        if (takeownExitCode != 0) {
            val outputBytes = process1.inputStream.readBytes()
            val output = String(outputBytes, Charset.forName("CP866"))
            onResult(StringResources.getString("ui_changeOwner_error_takeown", output.take(100)))
            Logger.warning("Failed to take ownership for path '$path': $output")
            return
        }

        // Затем изменяем владельца
        val process = ProcessBuilder(
            "icacls", path, "/grant", "$username:(F)", "/t"
        ).redirectErrorStream(true).start()

        val exitCode = process.waitFor()
        if (exitCode == 0) {
            onResult(StringResources.getString("ui_changeOwner_success", username, path))
            Logger.info("Owner changed: $username for path $path")
        } else {
            val process2 = ProcessBuilder("net", "user", username, "/delete")
                .redirectErrorStream(true)
                .start()
            val outputBytes = process2.inputStream.readBytes()
            val output = String(outputBytes, Charset.forName("CP866"))
            onResult(StringResources.getString("ui_changeOwner_error_code", exitCode, output.take(100)))
            Logger.warning("Failed to change owner for path '$path': $output")
        }
    } catch (e: Exception) {
        onResult(StringResources.getString("ui_changeOwner_error_execution", e.message ?: "Unknown error"))
        Logger.error("Exception during owner change", e)
    }
}