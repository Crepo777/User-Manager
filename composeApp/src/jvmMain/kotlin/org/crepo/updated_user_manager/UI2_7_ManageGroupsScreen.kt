//ЭТОТ ЭКРАН НЕ ИСПОЛЬЗУЕТСЯ
package org.crepo.updated_user_manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.nio.charset.Charset

@Composable
fun UI2_7(navigateBack: () -> Unit) {
    var groupName by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var groups by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(Unit) {
        loadGroups { groupsList ->
            groups = groupsList
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Заголовок
        Text(
            text = StringResources.getString("ui_manageGroups_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Text(
            text = StringResources.getString("ui_manageGroups_subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        //Имя группы
        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            label = { Text(StringResources.getString("ui_manageGroups_groupName")) },
            modifier = Modifier.fillMaxWidth()
        )

        //Кнопки создания и удаления
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (groupName.isNotBlank()) {
                        createGroup(groupName) { message ->
                            result = message
                            loadGroups { groupsList -> groups = groupsList }
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(StringResources.getString("ui_manageGroups_btn_create"))
            }

            Button(
                onClick = {
                    if (groupName.isNotBlank()) {
                        deleteGroup(groupName) { message ->
                            result = message
                            loadGroups { groupsList -> groups = groupsList }
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(StringResources.getString("ui_manageGroups_btn_delete"))
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

        //Список групп
        Text(
            text = StringResources.getString("ui_manageGroups_existingGroups"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groups) { group ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = group,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Button(
            onClick = navigateBack,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
        ) {
            Text(
                text = StringResources.getString("btn_back"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

//Загрузка списка групп
private fun loadGroups(onResult: (List<String>) -> Unit) {
    try {
        val process = ProcessBuilder("net", "localgroup")
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            onResult(emptyList())
            return
        }

        val outputBytes = process.inputStream.readBytes()
        val output = String(outputBytes, Charset.forName("CP866"))

        val groups = parseGroups(output)
        onResult(groups)
    } catch (e: Exception) {
        onResult(emptyList())
        Logger.error("Exception during groups loading", e)
    }
}

//Парсинг списка групп из вывода
private fun parseGroups(output: String): List<String> {
    val groups = mutableListOf<String>()
    var isGroupSection = false

    output.lines().forEach { line ->
        if (line.contains("----------")) {
            isGroupSection = true
            return@forEach
        }

        if (isGroupSection && line.isNotBlank() && !line.contains("----------")) {
            val group = line.trim()
            if (group.isNotEmpty()) {
                groups.add(group)
            }
        }
    }

    return groups
}

//Создание группы
private fun createGroup(groupName: String, onResult: (String) -> Unit) {
    try {
        val process = ProcessBuilder("net", "localgroup", groupName, "/add")
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        if (exitCode == 0) {
            onResult(StringResources.getString("ui_manageGroups_success_create", groupName))
            Logger.info("Group created: $groupName")
        } else {
            val outputBytes = process.inputStream.readBytes()
            val output = String(outputBytes, Charset.forName("CP866"))

            val errorMessage = when {
                output.contains("уже существует") || output.contains("already exists") ->
                    StringResources.getString("ui_manageGroups_error_exists", groupName)
                else ->
                    StringResources.getString("ui_manageGroups_error_code", exitCode)
            }

            onResult(errorMessage)
            Logger.warning("Failed to create group: $groupName, error: $output")
        }
    } catch (e: Exception) {
        onResult(StringResources.getString("ui_manageGroups_error_execution", e.message ?: "Unknown error"))
        Logger.error("Exception during group creation", e)
    }
}

//Удаление группы
private fun deleteGroup(groupName: String, onResult: (String) -> Unit) {
    try {
        val process = ProcessBuilder("net", "localgroup", groupName, "/delete")
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        if (exitCode == 0) {
            onResult(StringResources.getString("ui_manageGroups_success_delete", groupName))
            Logger.info("Group deleted: $groupName")
        } else {
            val outputBytes = process.inputStream.readBytes()
            val output = String(outputBytes, Charset.forName("CP866"))

            val errorMessage = when {
                output.contains("не существует") || output.contains("does not exist") ->
                    StringResources.getString("ui_manageGroups_error_notExists", groupName)
                output.contains("не может быть удалена") || output.contains("cannot be deleted") ->
                    StringResources.getString("ui_manageGroups_error_notEmpty", groupName)
                else ->
                    StringResources.getString("ui_manageGroups_error_code", exitCode)
            }

            onResult(errorMessage)
            Logger.warning("Failed to delete group: $groupName, error: $output")
        }
    } catch (e: Exception) {
        onResult(StringResources.getString("ui_manageGroups_error_execution", e.message ?: "Unknown error"))
        Logger.error("Exception during group deletion", e)
    }
}