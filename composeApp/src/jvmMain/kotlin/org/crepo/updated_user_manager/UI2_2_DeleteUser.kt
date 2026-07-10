package org.crepo.updated_user_manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import androidx.window.core.Logger
import cafe.adriel.voyager.navigator.LocalNavigator
import org.crepo.updated_user_manager.Logger
import java.lang.ProcessBuilder
import java.nio.charset.Charset
import java.util.Locale

@Composable
fun UI2_2(navigateBack: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val navigator = LocalNavigator.current ?: return

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
            text = StringResources.getString("ui_deleteUser_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
        )

        //Предупреждение
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = StringResources.getString("ui_deleteUser_warning_title"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onError
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = StringResources.getString("ui_deleteUser_warning_hint"),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }

        //Поле ввода
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(StringResources.getString("ui_deleteUser_userName_title")) },
            placeholder = { Text("user1") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

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
                Text(text = StringResources.getString("ui_deleteUser_back"), fontSize = 18.sp)
            }

            Button(
                onClick = {
                    if (username.isBlank()) {
                        result = StringResources.getString("ui_deleteUser_error_noName")
                    } else {
                        showDeleteDialog = true
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(text = StringResources.getString("ui_deleteUser_confirm"), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        //Результат
        if (result.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = when {
                    result.startsWith("✅") -> CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    result.startsWith("⚠\uFE0F") -> CardDefaults.cardColors(
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

    //Диалог подтверждения
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = StringResources.getString("ui_deleteUser_confirmWindow_title"),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = StringResources.getString("ui_deleteUser_confirmWindow_hint_1") +
                            StringResources.getString("ui_deleteUser_confirmWindow_hint_2"),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        executeDelete(username, onResult = { result = it })
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(StringResources.getString("ui_deleteUser_confirmWindow_deleteButton"))
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text(StringResources.getString("ui_deleteUser_confirmWindow_cancelButton"))
                }
            }
        )
    }
}

//Выполнение удаления
private fun executeDelete(username: String, onResult: (String) -> Unit) {
    val charset = if (Locale.getDefault().language == "ru")
        Charset.forName("CP866")
    else
        Charset.forName("UTF-8")

    try {
        //Удаление учётной записи
        val process1 = ProcessBuilder("net", "user", username, "/delete")
            .redirectErrorStream(true)
            .start()
        val exitCode1 = process1.waitFor()

        val outputBytes1 = process1.inputStream.readBytes()
        val output1 = String(outputBytes1, Charset.forName("CP866"))

        if (exitCode1 != 0) {
            val errorMessage = if (output1.contains(StringResources.getString("ui_deleteUser_error_notExist_0")))
                StringResources.getString("ui_deleteUser_error_notExist")
            else
                StringResources.getString("ui_deleteUser_error_code", exitCode1, output1.take(100)) + exitCode1 + "\n" + output1.take(100)

            Logger.warning("Failed to delete user: $username, error: $output1")
            onResult(errorMessage)
            return
        } else {
            Logger.info("User account deleted: $username")
        }

        //Удаление папки пользователя
        val userProfile = System.getProperty("user.home")
        val userFolder = "$userProfile\\..\\Users\\$username"
        val process2 = ProcessBuilder("cmd", "/c", "rmdir", "/s", "/q", userFolder)
            .redirectErrorStream(true)
            .start()
        val exitCode2 = process2.waitFor()

        val outputBytes2 = process2.inputStream.readBytes()
        val output2 = String(outputBytes2, Charset.forName("CP866"))

        if (exitCode2 == 0) {
            Logger.info("User folder deleted: $username")
            onResult(StringResources.getString("ui_deleteUser_fullSuccess_title"))
        } else {
            Logger.warning("Failed to delete user folder: $username, error: $output2")
            onResult(StringResources.getString("ui_deleteUser_halfSuccess_title"))
        }
    } catch (e: Exception) {
        Logger.error("Exception during user deletion", e)
        onResult(StringResources.getString("ui_deleteUser_error"))
    }
}