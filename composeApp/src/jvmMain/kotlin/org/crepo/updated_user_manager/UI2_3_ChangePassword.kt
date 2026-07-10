package org.crepo.updated_user_manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import java.awt.Color
import java.lang.ProcessBuilder
import java.nio.charset.Charset
import java.util.Locale

@Composable
fun UI3_1(navigateBack: () -> Unit) {
    //val warningContainer = Color(0x33FF6B6B)
    var username by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var useEmptyPassword by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    val charset = if (Locale.getDefault().language == "ru")
        Charset.forName("CP866")
    else
        Charset.forName("UTF-8")

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
            text = StringResources.getString("ui_changePassword_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
        )

        //Имя пользователя
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(StringResources.getString("ui_changePassword_userName_title")) },
            placeholder = { Text("user1") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        //Пустой пароль
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = useEmptyPassword,
                onCheckedChange = { useEmptyPassword = it }
            )
            Text(
                text = StringResources.getString("ui_changePassword_setBlackPasswordOption"),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        //Новый пароль
        val isPasswordFieldEnabled = !useEmptyPassword

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text(StringResources.getString("ui_changePassword_newPassword_title")) },
            placeholder = { Text("••••••") },
            visualTransformation = if (newPassword.isEmpty()) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = isPasswordFieldEnabled,
            supportingText = {
                if (!isPasswordFieldEnabled) {
                    Text(StringResources.getString("ui_changePassword_newPassword_disables"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )

        //Подтверждение пароля
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(StringResources.getString("ui_changePassword_confirmPassword_title")) },
            placeholder = { Text("••••••") },
            visualTransformation = if (confirmPassword.isEmpty()) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = isPasswordFieldEnabled
        )

        //Примечание
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = StringResources.getString("ui_changePassword_hints_title") +
                        StringResources.getString("ui_changePassword_hints_1") +
                        StringResources.getString("ui_changePassword_hints_2"),
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall
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
                Text(text = StringResources.getString("ui_changePassword_back"), fontSize = 18.sp)
            }

            Button(
                onClick = {
                    if (username.isBlank()) {
                        result = StringResources.getString("ui_changePassword_error_noName")
                        return@Button
                    }

                    if (useEmptyPassword) {
                        //Пустой пароль
                        try {
                            val process = ProcessBuilder("net", "user", username, "")
                                .redirectErrorStream(true)
                                .start()

                            val exitCode = process.waitFor()
                            if (exitCode == 0) {
                                result = StringResources.getString("ui_changePassword_success")
                                Logger.info("Empty password set for user: $username")
                            } else {
                                val outputBytes = process.inputStream.readBytes()
                                val output = String(outputBytes, Charset.forName("CP866"))

                                result = StringResources.getString("ui_changePassword_errorTake200", exitCode, output.take(200)) + exitCode + "\n" + output.take(200)
                                Logger.warning("Failed to set empty password for user '$username': $output")
                            }
                        } catch (e: Exception) {
                            result = StringResources.getString("ui_changePassword_error")
                            Logger.error("Exception during empty password setting", e)
                        }
                    } else {
                        //Проверка паролей
                        if (newPassword != confirmPassword) {
                            result = StringResources.getString("ui_changePassword_error_passwordsDoNotTheSame")
                            return@Button
                        }
                        if (newPassword.isBlank()) {
                            result = StringResources.getString("ui_changePassword_error_passwordsIsBlank")
                            return@Button
                        }

                        try {
                            val process = ProcessBuilder("net", "user", username, newPassword)
                                .redirectErrorStream(true)
                                .start()

                            val exitCode = process.waitFor()
                            if (exitCode == 0) {
                                result = StringResources.getString("ui_changePassword_success_changed")
                                Logger.info("Password changed for user: $username")
                            } else {
                                val output = process.inputStream.bufferedReader().readText()
                                result = if (output.contains(StringResources.getString("ui_changePassword_error_notExist")))
                                    StringResources.getString("ui_changePassword_error_userNotFound")
                                else
                                    StringResources.getString("ui_changePassword_errorTake200")

                                Logger.warning("Failed to change password for user '$username': $output")
                            }
                        } catch (e: Exception) {
                            result = StringResources.getString("ui_changePassword_error_execution")
                            Logger.error("Exception during password change", e)
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (useEmptyPassword) StringResources.getString("ui_changePassword_setBlank_title") else StringResources.getString("ui_changePassword_change_title"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
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
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
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
}