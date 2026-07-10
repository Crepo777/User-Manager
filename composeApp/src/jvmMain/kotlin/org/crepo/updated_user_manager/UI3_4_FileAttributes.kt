package org.crepo.updateduser_manager

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
import org.crepo.updated_user_manager.Logger
import org.crepo.updated_user_manager.StringResources
import java.nio.charset.Charset

@Composable
fun FileAttributesScreenContent(navigateBack: () -> Unit) {
    var path by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var isReadOnly by remember { mutableStateOf(false) }
    var isHidden by remember { mutableStateOf(false) }
    var isSystem by remember { mutableStateOf(false) }
    var isArchive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Заголовок
        Text(
            text = StringResources.getString("ui_fileAttributes_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Text(
            text = StringResources.getString("ui_fileAttributes_subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        //Ввод пути
        OutlinedTextField(
            value = path,
            onValueChange = { path = it },
            label = { Text(StringResources.getString("ui_fileAttributes_path")) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (path.isNotBlank()) {
                        executeViewAttributes(path) { message, attributes ->
                            result = message
                            isReadOnly = attributes[0]
                            isHidden = attributes[1]
                            isSystem = attributes[2]
                            isArchive = attributes[3]
                        }
                    }
                }
            )
        )

        //Атрибуты файла
        if (path.isNotBlank()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isReadOnly,
                        onCheckedChange = { isReadOnly = it }
                    )
                    Text(StringResources.getString("ui_fileAttributes_readOnly"))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isHidden,
                        onCheckedChange = { isHidden = it }
                    )
                    Text(StringResources.getString("ui_fileAttributes_hidden"))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isSystem,
                        onCheckedChange = { isSystem = it }
                    )
                    Text(StringResources.getString("ui_fileAttributes_system"))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isArchive,
                        onCheckedChange = { isArchive = it }
                    )
                    Text(StringResources.getString("ui_fileAttributes_archive"))
                }
            }
        }

        //Результат
        if (result.isNotBlank()) {
            Text(
                text = result,
                color = if (result.startsWith("✅")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        //Кнопки
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
                Text(text = StringResources.getString("ui_fileAttributes_btn_back"), fontSize = 18.sp)
            }

            Button(
                onClick = {
                    if (path.isNotBlank()) {
                        executeChangeAttributes(
                            path,
                            listOf(isReadOnly, isHidden, isSystem, isArchive)
                        ) { message ->
                            result = message
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = StringResources.getString("ui_fileAttributes_btn_apply"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun executeViewAttributes(
    path: String,
    onResult: (String, List<Boolean>) -> Unit
) {
    try {
        val process = ProcessBuilder("attrib", path)
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        val outputBytes = process.inputStream.readBytes()
        val output = String(outputBytes, Charset.forName("CP866"))

        if (exitCode == 0) {
            val attributes = parseAttributes(output)

            val message = buildString {
                append("📄 $path\n\n")
                append(StringResources.getString("ui_fileAttributes_current_attributes"))
                append("\n• ")
                append(if (attributes[0]) "✓ " else "✗ ")
                append(StringResources.getString("ui_fileAttributes_readOnly"))
                append("\n• ")
                append(if (attributes[1]) "✓ " else "✗ ")
                append(StringResources.getString("ui_fileAttributes_hidden"))
                append("\n• ")
                append(if (attributes[2]) "✓ " else "✗ ")
                append(StringResources.getString("ui_fileAttributes_system"))
                append("\n• ")
                append(if (attributes[3]) "✓ " else "✗ ")
                append(StringResources.getString("ui_fileAttributes_archive"))
            }

            onResult(message, attributes)
            Logger.info("File attributes viewed for: $path")
        } else {
            onResult(StringResources.getString("ui_fileAttributes_error_view", output.take(100)), listOf(false, false, false, false))
            Logger.warning("Failed to view attributes for path '$path': $output")
        }
    } catch (e: Exception) {
        onResult(StringResources.getString("ui_fileAttributes_error_execution", e.message ?: "Unknown error"), listOf(false, false, false, false))
        Logger.error("Exception during attributes viewing", e)
    }
}

private fun parseAttributes(output: String): List<Boolean> {
    val attributes = output.lowercase()
    return listOf(
        attributes.contains("r"), //ReadOnly
        attributes.contains("h"), //Hidden
        attributes.contains("s"), //System
        attributes.contains("a")  //Archive
    )
}

private fun executeChangeAttributes(
    path: String,
    attributes: List<Boolean>,
    onResult: (String) -> Unit
) {
    try {
        val commands = mutableListOf<String>()

        if (attributes[0]) commands.add("+r") else commands.add("-r")
        if (attributes[1]) commands.add("+h") else commands.add("-h")
        if (attributes[2]) commands.add("+s") else commands.add("-s")
        if (attributes[3]) commands.add("+a") else commands.add("-a")

        val process = ProcessBuilder(
            "attrib", path, *commands.toTypedArray()
        ).redirectErrorStream(true).start()

        val exitCode = process.waitFor()
        if (exitCode == 0) {
            onResult(StringResources.getString("ui_fileAttributes_success", path))
            Logger.info("File attributes changed for: $path")
        } else {
            val output = process.inputStream.bufferedReader().readText()
            onResult(StringResources.getString("ui_fileAttributes_error_change", output.take(100)))
            Logger.warning("Failed to change attributes for path '$path': $output")
        }
    } catch (e: Exception) {
        onResult(StringResources.getString("ui_fileAttributes_error_execution", e.message ?: "Unknown error"))
        Logger.error("Exception during attributes change", e)
    }
}