package org.crepo.updated_user_manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import java.nio.charset.Charset

@Composable
fun UI2_6(navigateBack: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var availableGroups by remember { mutableStateOf(emptyList<String>()) }
    var userGroups by remember { mutableStateOf(emptyList<String>()) }
    var result by remember { mutableStateOf("") }

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
            text = StringResources.getString("ui_manageGroups_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Text(
            text = StringResources.getString("ui_manageGroups_subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        //Имя
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(StringResources.getString("ui_manageGroups_username")) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        //Кнопка загрузки групп
        Button(
            onClick = {
                if (username.isNotBlank()) {
                    loadUserGroups(username) { userGroupsList, availableGroupsList ->
                        userGroups = userGroupsList
                        availableGroups = availableGroupsList
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(StringResources.getString("ui_manageGroups_btn_load"))
        }

        //Результат
        if (result.isNotBlank()) {
            Text(
                text = result,
                color = if (result.startsWith("✅")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        if (username.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                //Группы пользователя
                GroupColumn(
                    modifier = Modifier.weight(1f), //Правильное применение weight
                    title = StringResources.getString("ui_manageGroups_userGroups"),
                    groups = userGroups,
                    onRemove = { group ->
                        removeUserFromGroup(username, group) { message ->
                            result = message
                            loadUserGroups(username) { uGroups, aGroups ->
                                userGroups = uGroups
                                availableGroups = aGroups
                            }
                        }
                    }
                )
                //Доступные группы
                GroupColumn(
                    modifier = Modifier.weight(1f), //Правильное применение weight
                    title = StringResources.getString("ui_manageGroups_availableGroups"),
                    groups = availableGroups,
                    onAdd = { group ->
                        addUserToGroup(username, group) { message ->
                            result = message
                            loadUserGroups(username) { uGroups, aGroups ->
                                userGroups = uGroups
                                availableGroups = aGroups
                            }
                        }
                    }
                )
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

@Composable
private fun GroupColumn(
    modifier: Modifier = Modifier,
    title: String,
    groups: List<String>,
    onRemove: ((String) -> Unit)? = null,
    onAdd: ((String) -> Unit)? = null
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(groups) { group ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(group)

                        if (onRemove != null) {
                            IconButton(
                                onClick = { onRemove(group) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Remove")
                            }
                        } else if (onAdd != null) {
                            IconButton(
                                onClick = { onAdd(group) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun loadUserGroups(
    username: String,
    onResult: (List<String>, List<String>) -> Unit
) {
    try {
        //Группы пользователя
        val process1 = ProcessBuilder("net", "user", username)
            .redirectErrorStream(true)
            .start()

        val exitCode1 = process1.waitFor()
        if (exitCode1 != 0) {
            onResult(emptyList(), emptyList())
            return
        }

        val outputBytes1 = process1.inputStream.readBytes()
        val output1 = String(outputBytes1, Charset.forName("CP866"))

        val userGroups = parseUserGroups(output1)

        val process2 = ProcessBuilder("net", "localgroup")
            .redirectErrorStream(true)
            .start()

        val exitCode2 = process2.waitFor()
        if (exitCode2 != 0) {
            onResult(userGroups, emptyList())
            return
        }

        val outputBytes2 = process2.inputStream.readBytes()
        val output2 = String(outputBytes2, Charset.forName("CP866"))

        val allGroups = parseAllGroups(output2)

        val availableGroups = allGroups.filter { !userGroups.contains(it) }

        onResult(userGroups, availableGroups)
    } catch (e: Exception) {
        onResult(emptyList(), emptyList())
        Logger.error("Exception during group loading", e)
    }
}

private fun parseUserGroups(output: String): List<String> {
    val groups = mutableListOf<String>()
    var isGroupSection = false

    output.lines().forEach { line ->
        if (line.contains("Локальные групповые членства") || line.contains("Local Group Memberships")) {
            isGroupSection = true
            return@forEach
        }

        if (isGroupSection && line.isNotBlank()) {
            if (line.startsWith("*")) {
                val group = line.trimStart('*').trim()
                if (group.isNotEmpty()) {
                    groups.add(group)
                }
            }
        }
    }

    return groups
}

private fun parseAllGroups(output: String): List<String> {
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

private fun addUserToGroup(username: String, group: String, onResult: (String) -> Unit) {
    try {
        val process = ProcessBuilder("net", "localgroup", group, username, "/add")
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        if (exitCode == 0) {
            onResult(StringResources.getString("ui_manageGroups_success_add", username, group))
            Logger.info("User '$username' added to group '$group'")
        } else {
            val outputBytes = process.inputStream.readBytes()
            val output = String(outputBytes, Charset.forName("CP866"))

            val errorMessage = when {
                output.contains("не существует") || output.contains("does not exist") ->
                    StringResources.getString("ui_manageGroups_error_user_not_exist", username)
                output.contains("уже является") || output.contains("already a member") ->
                    StringResources.getString("ui_manageGroups_error_already_member", username, group)
                else ->
                    StringResources.getString("ui_manageGroups_error_add", exitCode)
            }

            onResult(errorMessage)
            Logger.warning("Failed to add user '$username' to group '$group': $output")
        }
    } catch (e: Exception) {
        onResult(StringResources.getString("ui_manageGroups_error_execution", e.message ?: "Unknown error"))
        Logger.error("Exception during adding user to group", e)
    }
}

private fun removeUserFromGroup(username: String, group: String, onResult: (String) -> Unit) {
    try {
        val process = ProcessBuilder("net", "localgroup", group, username, "/delete")
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        if (exitCode == 0) {
            onResult(StringResources.getString("ui_manageGroups_success_remove", username, group))
            Logger.info("User '$username' removed from group '$group'")
        } else {
            val outputBytes = process.inputStream.readBytes()
            val output = String(outputBytes, Charset.forName("CP866"))

            val errorMessage = when {
                output.contains("не существует") || output.contains("does not exist") ->
                    StringResources.getString("ui_manageGroups_error_user_not_exist", username)
                else ->
                    StringResources.getString("ui_manageGroups_error_remove", exitCode)
            }

            onResult(errorMessage)
            Logger.warning("Failed to remove user '$username' from group '$group': $output")
        }
    } catch (e: Exception) {
        onResult(StringResources.getString("ui_manageGroups_error_execution", e.message ?: "Unknown error"))
        Logger.error("Exception during removing user from group", e)
    }
}