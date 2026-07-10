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
import java.lang.ProcessBuilder
import java.nio.charset.Charset
import java.util.Locale

@Composable
fun UI2_1(navigateBack: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    val charset = if (Locale.getDefault().language == "ru")
        Charset.forName("CP866")
    else
        Charset.forName("UTF-8")

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
            text = StringResources.getString("ui_createUser_title_create"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
        )

        //Имя
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(StringResources.getString("ui_createUser_hint_username")) },
            placeholder = { Text("user1") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = {
                Text(StringResources.getString("ui_createUser_enter_new_username"))
            }
        )

        //Пароль
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(StringResources.getString("ui_createUser_enter_new_username")) },
            placeholder = { Text("••••••") },
            visualTransformation = if (password.isEmpty()) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = {
                Text(StringResources.getString("ui_createUser_leave_empty_for_no_password"))
            }
        )

        //Примечание
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = StringResources.getString("ui_createUser_warning_admin_required") + "\n" +
                        StringResources.getString("ui_createUser_warning_user_exists"),
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
                Text(text = StringResources.getString("ui_createUser_btn_back"), fontSize = 18.sp)
            }

            Button(
                onClick = {
                    if (username.isBlank()) {
                        result = StringResources.getString("ui_createUser_error_username_required")
                        return@Button
                    }

                    try {
                        val command = mutableListOf("net", "user", username)
                        if (password.isNotBlank()) {
                            command.add(password)
                        }
                        command.add("/add")

                        val process = ProcessBuilder(command)
                            .redirectErrorStream(true)
                            .start()

                        val exitCode = process.waitFor()
                        //val systemCharset = Charset.defaultCharset()
                        val outputBytes = process.inputStream.readBytes()
                        val output = String(outputBytes, Charset.forName("CP866"))
                        //val output = EncodingUtils.getConsoleOutput(process)

                        if (exitCode == 0) {
                            result = StringResources.getString("ui_createUser_success_user_created", username)
                            Logger.info("User created: $username")
                        } else {
                            if (output.contains("уже существует") || output.contains("already exists")) {
                                result = StringResources.getString("ui_createUser_warning_user_exists_1", username)
                                Logger.warning("User already exists: $username")
                            } else {
                                result = StringResources.getString("ui_createUser_error_code", exitCode, output.take(100))
                                Logger.error("Error creating user '$username': exit code $exitCode, output: ${output.take(100)}")
                            }
                        }
                    } catch (e: Exception) {
                        result = StringResources.getString("ui_createUser_error_execution", e.message ?: "Unknown error")
                        Logger.error("Exception during user creation", e)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = StringResources.getString("ui_createUser_btn_create"),
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
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
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