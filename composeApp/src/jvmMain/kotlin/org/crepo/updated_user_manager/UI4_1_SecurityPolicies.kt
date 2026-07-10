package org.crepo.updated_user_manager

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import java.io.File
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

@Composable
fun UI4_1_SecurityPolicyScreenContent(navigateBack: () -> Unit) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var minPasswordLength by remember { mutableStateOf(8) }
    var passwordComplexity by remember { mutableStateOf(true) }
    var accountLockoutThreshold by remember { mutableStateOf(5) }
    var passwordExpiration by remember { mutableStateOf(90) }
    var historyCount by remember { mutableStateOf(5) }

    LaunchedEffect(Unit) {
        loadSystemPolicies { policies, err ->
            if (err != null) {
                error = err
            } else {
                error = null
                minPasswordLength = policies.minPasswordLength
                passwordComplexity = policies.passwordComplexity
                accountLockoutThreshold = policies.accountLockoutThreshold
                passwordExpiration = policies.passwordExpiration
                historyCount = policies.historyCount
            }
            loading = false
        }
    }

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
            text = StringResources.getString("ui_securityPolicy_system_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Text(
            text = StringResources.getString("ui_securityPolicy_system_subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = StringResources.getString("ui_securityPolicy_explanation"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = StringResources.getString("ui_securityPolicy_explanation_detail"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

//Настройки политик
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = StringResources.getString("ui_securityPolicy_settings"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Divider()

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (loading) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = StringResources.getString("ui_securityPolicy_loading"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (error != null) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = StringResources.getString("ui_securityPolicy_error_title"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = error ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            //Поля настроек
                            SecurityPolicyField(
                                title = StringResources.getString("ui_securityPolicy_minPasswordLength"),
                                description = StringResources.getString("ui_securityPolicy_minPasswordLength_desc"),
                                value = minPasswordLength.toString(),
                                onValueChange = {
                                    val value = it.toIntOrNull() ?: 0
                                    if (value in 4..128) minPasswordLength = value
                                },
                                keyboardType = KeyboardType.Number
                            )

                            SecurityPolicyToggle(
                                title = StringResources.getString("ui_securityPolicy_passwordComplexity"),
                                description = StringResources.getString("ui_securityPolicy_passwordComplexity_desc"),
                                isChecked = passwordComplexity,
                                onCheckedChange = { passwordComplexity = it }
                            )

                            SecurityPolicyField(
                                title = StringResources.getString("ui_securityPolicy_accountLockoutThreshold"),
                                description = StringResources.getString("ui_securityPolicy_accountLockoutThreshold_desc"),
                                value = accountLockoutThreshold.toString(),
                                onValueChange = {
                                    val value = it.toIntOrNull() ?: 0
                                    if (value in 0..100) accountLockoutThreshold = value
                                },
                                keyboardType = KeyboardType.Number
                            )

                            SecurityPolicyField(
                                title = StringResources.getString("ui_securityPolicy_passwordExpiration"),
                                description = StringResources.getString("ui_securityPolicy_passwordExpiration_desc"),
                                value = passwordExpiration.toString(),
                                onValueChange = {
                                    val value = it.toIntOrNull() ?: 0
                                    if (value in 0..365) passwordExpiration = value
                                },
                                keyboardType = KeyboardType.Number
                            )

                            SecurityPolicyField(
                                title = StringResources.getString("ui_securityPolicy_historyCount"),
                                description = StringResources.getString("ui_securityPolicy_historyCount_desc"),
                                value = historyCount.toString(),
                                onValueChange = {
                                    val value = it.toIntOrNull() ?: 0
                                    if (value in 0..24) historyCount = value
                                },
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }

                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(scrollState)
                    )
                }
            }
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
                Text(text = StringResources.getString("btn_back"), fontSize = 18.sp)
            }

            Button(
                onClick = {
                    saveSystemPolicies(
                        minPasswordLength,
                        passwordComplexity,
                        accountLockoutThreshold,
                        passwordExpiration,
                        historyCount
                    ) { success, message ->
                        if (success) {
                            error = StringResources.getString("ui_securityPolicy_success")
                            loadSystemPolicies { policies, err ->
                                if (err == null) {
                                    minPasswordLength = policies.minPasswordLength
                                    passwordComplexity = policies.passwordComplexity
                                    accountLockoutThreshold = policies.accountLockoutThreshold
                                    passwordExpiration = policies.passwordExpiration
                                    historyCount = policies.historyCount
                                }
                            }
                        } else {
                            error = message
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = StringResources.getString("ui_securityPolicy_save"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SecurityPolicyField(
    title: String,
    description: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(title) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Done
            )
        )
    }
}

@Composable
private fun SecurityPolicyToggle(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isChecked)
                    StringResources.getString("ui_securityPolicy_enabled")
                else
                    StringResources.getString("ui_securityPolicy_disabled"),
                color = if (isChecked)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier
            )
        }
    }
}

//Данные политик безопасности
data class UserSecurityPolicies(
    val minPasswordLength: Int,
    val passwordComplexity: Boolean,
    val accountLockoutThreshold: Int,
    val passwordExpiration: Int,
    val historyCount: Int
)

private fun loadSystemPolicies(
    onResult: (UserSecurityPolicies, String?) -> Unit
) {
    try {
        if (!AppConfig.isRunningAsAdmin()) {
            onResult(
                UserSecurityPolicies(8, true, 5, 90, 5),
                StringResources.getString("ui_securityPolicy_error_admin_required")
            )
            return
        }

        val tempFile = File.createTempFile("policies", ".inf")
        tempFile.deleteOnExit()

        val exportProcess = ProcessBuilder(
            "secedit",
            "/export",
            "/cfg",
            tempFile.absolutePath
        ).start()

        val exitCode = exportProcess.waitFor()
        if (exitCode != 0) {
            onResult(
                UserSecurityPolicies(8, true, 5, 90, 5),
                StringResources.getString("ui_securityPolicy_error_load_failed")
            )
            return
        }

        val policiesContent = String(tempFile.readBytes(), Charset.forName("CP866"))

        val policies = parseSecurityPolicies(policiesContent)
        onResult(policies, null)
    } catch (e: Exception) {
        Logger.error("Exception during security policy loading", e)
        onResult(
            UserSecurityPolicies(8, true, 5, 90, 5),
            StringResources.getString("ui_securityPolicy_error_load_failed")
        )
    }
}

private fun parseSecurityPolicies(content: String): UserSecurityPolicies {
    val lines = content.lines()

    var minPasswordLength = 8
    var passwordComplexity = true
    var accountLockoutThreshold = 5
    var passwordExpiration = 90
    var historyCount = 5

    for (line in lines) {
        when {
            line.startsWith("MinimumPasswordLength") -> {
                minPasswordLength = line.substringAfter("=").trim().toIntOrNull() ?: 8
            }
            line.startsWith("PasswordComplexity") -> {
                passwordComplexity = line.substringAfter("=").trim().toIntOrNull() == 1
            }
            line.startsWith("LockoutBadCount") -> {
                accountLockoutThreshold = line.substringAfter("=").trim().toIntOrNull() ?: 5
            }
            line.startsWith("MaximumPasswordAge") -> {
                passwordExpiration = line.substringAfter("=").trim().toIntOrNull() ?: 90
            }
            line.startsWith("PasswordHistorySize") -> {
                historyCount = line.substringAfter("=").trim().toIntOrNull() ?: 5
            }
        }
    }

    return UserSecurityPolicies(
        minPasswordLength = minPasswordLength,
        passwordComplexity = passwordComplexity,
        accountLockoutThreshold = accountLockoutThreshold,
        passwordExpiration = passwordExpiration,
        historyCount = historyCount
    )
}

private fun saveSystemPolicies(
    minPasswordLength: Int,
    passwordComplexity: Boolean,
    accountLockoutThreshold: Int,
    passwordExpiration: Int,
    historyCount: Int,
    onResult: (Boolean, String) -> Unit
) {
    try {
        if (!AppConfig.isRunningAsAdmin()) {
            onResult(false, StringResources.getString("ui_securityPolicy_error_admin_required"))
            return
        }

        val tempFile = File.createTempFile("security_policy", ".inf")
        tempFile.deleteOnExit()

        val policyContent = $$"""
            [Unicode]
            Unicode=yes
            [Version]
            signature="`$CHICAGO$`"
            Revision=1
            [System Access]
            MinimumPasswordAge = 1
            MaximumPasswordAge = $$passwordExpiration
            MinimumPasswordLength = $$minPasswordLength
            PasswordComplexity = $${if (passwordComplexity) 1 else 0}
            PasswordHistorySize = $$historyCount
            LockoutBadCount = $$accountLockoutThreshold
            ResetLockoutCount = 30
            LockoutDuration = 30
            [Event Audit]
            ;
        """.trimIndent()

        tempFile.writeText(policyContent)

        Logger.info("Security policy content:\n$policyContent")

        val process = ProcessBuilder(
            "secedit",
            "/configure",
            "/db",
            "secedit.sdb",
            "/cfg",
            tempFile.absolutePath,
            "/overwrite",
            "/quiet"
        ).start()

        val exitCode = process.waitFor()
        if (exitCode == 0) {
            Logger.info("Security policies applied successfully")
            onResult(true, "")
        } else {
            val errorOutput = try {
                String(process.errorStream.readBytes(), Charset.forName("CP866"))
            } catch (e: Exception) {
                "Error reading error stream"
            }

            val stdoutOutput = try {
                String(process.inputStream.readBytes(), Charset.forName("CP866"))
            } catch (e: Exception) {
                "Error reading stdout stream"
            }

            Logger.warning("Failed to apply security policies. Exit code: $exitCode, Error: $errorOutput, Output: $stdoutOutput")
            onResult(false, StringResources.getString("ui_securityPolicy_error_apply_failed", exitCode, errorOutput))
        }
    } catch (e: Exception) {
        Logger.error("Exception during security policies configuration", e)
        onResult(false, StringResources.getString("ui_securityPolicy_error_execution", e.message ?: "Unknown error"))
    }
}