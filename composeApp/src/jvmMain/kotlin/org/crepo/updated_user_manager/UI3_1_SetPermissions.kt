@file:Suppress("UNCHECKED_CAST")

package org.crepo.updated_user_manager

import java.lang.ProcessBuilder
import java.nio.charset.StandardCharsets
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.nio.charset.Charset
import java.nio.file.Paths
import java.util.Locale
import javax.swing.JFileChooser


@Composable
fun UI4_1(navigateBack: () -> Unit) {
    // Проверка прав администратора
    val isAdmin by remember {
        derivedStateOf {
            try {
                ProcessBuilder("net", "session").start().waitFor() == 0
            } catch (e: Exception) {
                false
            }
        }
    }

    if (!isAdmin) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text(
                text = StringResources.getString("ui_setPermissions_error_administrator"),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
    // --- Состояния ---
    var path by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var showUserDialog by remember { mutableStateOf(false) }
    var users by remember { mutableStateOf(emptyList<String>()) }

    var readAllowed by remember { mutableStateOf(false) }
    var writeAllowed by remember { mutableStateOf(false) }
    var executeAllowed by remember { mutableStateOf(false) }
    var fullControl by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }

    // --- Загрузка пользователей при первом открытии ---
    LaunchedEffect(Unit) {
        try {
            val process = ProcessBuilder("net", "user")
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader(StandardCharsets.UTF_8).readText()
            val lines = output.lines()

            // Ищем строку, содержащую "Пользователи" (с любым количеством пробелов)
            val userSectionStart = lines.indexOfFirst { it.contains(StringResources.getString("ui_setPermissions_button_users"), ignoreCase = true) }

            if (userSectionStart == -1) {
                println(StringResources.getString("ui_setPermissions_system_error_userSectionNotFound"))
                users = emptyList()
                return@LaunchedEffect
            }

            // Пропускаем заголовок и пустые строки
            val userLines = lines
                .subList(userSectionStart + 1, lines.size)
                .takeWhile { it.isNotBlank() && !it.contains(StringResources.getString("ui_setPermissions_commandCompleated")) }
                .map { it.trim().split(' ').firstOrNull() }
                .filterNotNull()
                .distinct()

            users = userLines
        } catch (e: Exception) {
            println(StringResources.getString("ui_setPermissions_system_error_usersLoading") + {e.message})
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок
        Text(
            text = StringResources.getString("ui_setPermissions_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
        )

        // --- Блок: Пользователь ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(StringResources.getString("ui_setPermissions_userName")) },
                placeholder = { Text("ivan") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showUserDialog = true },
                modifier = Modifier.height(56.dp)
            ) {
                Text(StringResources.getString("ui_setPermissions_userChoose_button"))
            }
        }

        // Диалог выбора пользователя
        if (showUserDialog) {
            AlertDialog(
                onDismissRequest = { showUserDialog = false },
                title = { Text(StringResources.getString("ui_setPermissions_userChoose_hint")) },
                text = {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(users) { user ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clickable { username = user; showUserDialog = false },
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 16.dp)
                                ) {
                                    Text(text = user, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showUserDialog = false }) {
                        Text(StringResources.getString("ui_setPermissions_userChoose_close"))
                    }
                }
            )
        }

        // --- Блок: Путь ---
        OutlinedTextField(
            value = path,
            onValueChange = { path = it },
            label = { Text(StringResources.getString("ui_setPermissions_fileField_title")) },
            placeholder = { Text("C:\\Users\\Public\\Documents") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Кнопка Обзор
        Button(
            onClick = { showFileChooser { selectedPath ->
                if (selectedPath != null) path = selectedPath
            } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(StringResources.getString("ui_setPermissions_fileField_buttonChoose"))
        }

        // Разделитель
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // --- Блок: Права ---
        Text(
            text = StringResources.getString("ui_setPermissions_choosePerms_title"),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Start)
        )

        PermissionItem(
            label = StringResources.getString("ui_setPermissions_choosePerms_read_title"),
            description = StringResources.getString("ui_setPermissions_choosePerms_read_hint"),
            isChecked = readAllowed,
            onCheckedChange = { checked ->
                readAllowed = checked
                if (checked) fullControl = false
            }
        )

        PermissionItem(
            label = StringResources.getString("ui_setPermissions_choosePerms_write_title"),
            description = StringResources.getString("ui_setPermissions_choosePerms_write_hint"),
            isChecked = writeAllowed,
            onCheckedChange = { checked ->
                writeAllowed = checked
                if (checked) fullControl = false
            }
        )

        PermissionItem(
            label = StringResources.getString("ui_setPermissions_choosePerms_execute_title"),
            description = StringResources.getString("ui_setPermissions_choosePerms_execute_hint"),
            isChecked = executeAllowed,
            onCheckedChange = { checked ->
                executeAllowed = checked
                if (checked) fullControl = false
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = fullControl,
                onCheckedChange = { checked ->
                    if (checked) {
                        readAllowed = false
                        writeAllowed = false
                        executeAllowed = false
                    }
                    fullControl = checked
                }
            )
            Column {
                Text(
                    text = StringResources.getString("ui_setPermissions_choosePerms_fullControl_title"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = StringResources.getString("ui_setPermissions_choosePerms_fullControl_hint"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Примечание
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = StringResources.getString("ui_setPermissions_choosePerms_hints_title") +
                        StringResources.getString("ui_setPermissions_choosePerms_hints_1") +
                        StringResources.getString("ui_setPermissions_choosePerms_hints_2"),
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
                Text(text = StringResources.getString("ui_setPermissions_choosePerms_back"), fontSize = 18.sp)
            }

            Button(
                onClick = {
                    // Сброс результата
                    result = ""

                    //
                    val charset = if (Locale.getDefault().language == "ru")
                        Charset.forName("CP866")
                    else
                        Charset.forName("UTF-8")

                    // Проверка ввода
                    if (username.isBlank()) {
                        result = StringResources.getString("ui_setPermissions_error_noName")
                        return@Button
                    }
                    if (path.isBlank()) {
                        result = StringResources.getString("ui_setPermissions_error_noPath")
                        return@Button
                    }

                    // Формирование команды
                    try {
                        val permissions = when {
                            fullControl -> "F"
                            else -> {
                                mutableListOf<String>().apply {
                                    if (readAllowed) add("R")
                                    if (writeAllowed) add("W")
                                    if (executeAllowed) add("X")
                                }.joinToString("")
                            }
                        }

                        if (permissions.isEmpty() && !fullControl) {
                            result = StringResources.getString("ui_setPermissions_error_noPermissions")
                            return@Button
                        }

                        val command = listOf("icacls", path, "/grant", "$username:($permissions)")
                        println(StringResources.getString("ui_setPermissions_system_executingInfo") + command.joinToString(" "))

                        // Логируем попытку изменения прав
                        Logger.info("Attempting to set permissions: user=$username, path=$path, permissions=$permissions")

                        val process = ProcessBuilder(command).redirectErrorStream(true).start()
                        val exitCode = process.waitFor()
                        val outputBytes = process.inputStream.readBytes()
                        val output = String(outputBytes, Charset.forName("CP866"))

                        if (exitCode == 0) {
                            result = StringResources.getString("ui_setPermissions_system_success_1") + username + StringResources.getString("ui_setPermissions_success_2") + path
                            // Логируем успешное применение прав
                            Logger.info("Permissions successfully set: user=$username, path=$path, permissions=$permissions")
                        } else {
                            result = when {
                                output.contains(StringResources.getString("ui_setPermissions_error_incorrectName")) -> StringResources.getString("ui_setPermissions_error_incorrectPath") + path
                                output.contains(StringResources.getString("ui_setPermissions_error_notExist")) -> StringResources.getString("ui_setPermissions_error_userNotFound_1") + username + StringResources.getString("ui_setPermissions_error_userNotFound_2")
                                output.contains(StringResources.getString("ui_setPermissions_error_accessDenied")) -> StringResources.getString("ui_setPermissions_error_notEnoughRights")
                                else -> StringResources.getString("ui_setPermissions_error_code") + exitCode
                            }
                            // Логируем неудачное применение прав
                            Logger.warning("Failed to set permissions: user=$username, path=$path, error=$output")
                        }
                    } catch (e: Exception) {
                        result = StringResources.getString("ui_setPermissions_error_message") + e.message
                        // Логируем исключение
                        Logger.error("Exception during permissions setting", e)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = StringResources.getString("ui_setPermissions_confirm"), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PermissionItem(
    label: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
        Column {
            Text(text = label, fontSize = 18.sp)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --- Диалог выбора файла (Swing) ---
@OptIn(ExperimentalComposeUiApi::class)
private fun showFileChooser(onResult: (String?) -> Unit) {
    val fileChooser = JFileChooser().apply {
        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    }
    val result = fileChooser.showOpenDialog(null)
    if (result == JFileChooser.APPROVE_OPTION) {
        val selected = Paths.get(fileChooser.selectedFile.absolutePath).toString()
        onResult(selected)
    } else {
        onResult(null)
    }
}