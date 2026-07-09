package org.crepo.updated_user_manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.io.File
import java.nio.charset.Charset
import java.util.Locale

@Composable
fun ViewPermissionsScreenContent(navigateBack: () -> Unit) {
    var path by remember { mutableStateOf("") }
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
            text = StringResources.getString("ui_viewPermissions_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Text(
            text = StringResources.getString("ui_viewPermissions_subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Поле ввода пути
        OutlinedTextField(
            value = path,
            onValueChange = { path = it },
            label = { Text(StringResources.getString("ui_viewPermissions_path")) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (path.isNotBlank()) {
                        executeViewPermissions(path) { message ->
                            result = message
                        }
                    }
                }
            )
        )

        // Результат операции
        if (result.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = result,
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 24.sp
                )
            }
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
                Text(text = StringResources.getString("ui_viewPermissions_btn_back"), fontSize = 18.sp)
            }

            Button(
                onClick = {
                    if (path.isNotBlank()) {
                        executeViewPermissions(path) { message ->
                            result = message
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = StringResources.getString("ui_viewPermissions_btn_view"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun executeViewPermissions(path: String, onResult: (String) -> Unit) {
    val charset = if (Locale.getDefault().language == "ru")
        Charset.forName("CP866")
    else
        Charset.forName("UTF-8")

    try {
        // Проверяем существование пути
        val file = File(path)
        if (!file.exists()) {
            onResult(StringResources.getString("ui_viewPermissions_error_path_not_exist", path))
            Logger.warning("Path does not exist: $path")
            return
        }

        // Выполняем команду icacls для просмотра прав
        val process = ProcessBuilder("icacls", path)
            .redirectErrorStream(true)
            .start()

        // Читаем вывод в OEM-кодировке (CP866)
        val outputBytes = process.inputStream.readBytes()
        val output = String(outputBytes, Charset.forName("CP866"))

        val exitCode = process.waitFor()

        if (exitCode == 0) {
            // Форматируем вывод для лучшей читаемости
            val formattedOutput = "📋 $output".replace("\n", "\n• ")
            onResult(formattedOutput)
            Logger.info("Permissions viewed for path: $path")
        } else {
            val errorMessage = when {
                output.contains("Отказано в доступе") || output.contains("Access is denied") ->
                    StringResources.getString("ui_viewPermissions_error_access_denied")
                else ->
                    StringResources.getString("ui_viewPermissions_error_code", exitCode, output.take(100))
            }
            onResult(errorMessage)
            Logger.warning("Failed to view permissions for path '$path': $output")
        }
    } catch (e: Exception) {
        onResult(StringResources.getString("ui_viewPermissions_error_execution", e.message ?: "Unknown error"))
        Logger.error("Exception during permissions viewing", e)
    }
}

private fun formatPermissionsOutput(output: String): String {
    return "📋 $output".replace("\n", "\n• ")
}

