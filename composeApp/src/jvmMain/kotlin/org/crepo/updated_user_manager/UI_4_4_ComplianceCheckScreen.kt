package org.crepo.updated_user_manager

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import java.io.File
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UI4_4_ComplianceCheckScreenContent(navigateBack: () -> Unit) {
    var selectedStandard by remember { mutableStateOf("CIS") }
    var checkResults by remember { mutableStateOf(emptyList<ComplianceResult>()) }
    var isLoading by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

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
            text = StringResources.getString("ui_complianceCheck_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Text(
            text = StringResources.getString("ui_complianceCheck_subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = StringResources.getString("ui_complianceCheck_standard"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = StringResources.getString("ui_complianceCheck_standard_desc"),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    ExposedDropdownMenuBox(
                        expanded = isExpanded,
                        onExpandedChange = { isExpanded = !isExpanded }
                    ) {
                        TextField(
                            readOnly = true,
                            value = when (selectedStandard) {
                                "CIS" -> "CIS Benchmark"
                                "NIST" -> "NIST 800-53"
                                "PCI" -> "PCI DSS"
                                else -> selectedStandard
                            },
                            onValueChange = {},
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .width(150.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("CIS Benchmark") },
                                onClick = {
                                    selectedStandard = "CIS"
                                    isExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("NIST 800-53") },
                                onClick = {
                                    selectedStandard = "NIST"
                                    isExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("PCI DSS") },
                                onClick = {
                                    selectedStandard = "PCI"
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        isLoading = true
                        checkCompliance(selectedStandard) { results ->
                            checkResults = results
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = StringResources.getString("ui_complianceCheck_check"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        //Результаты проверки
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                //Заголовок результатов
                Text(
                    text = StringResources.getString("ui_complianceCheck_results"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                //Статистика
                if (!isLoading && checkResults.isNotEmpty()) {
                    val compliantCount = checkResults.count { it.status == "Compliant" }
                    val warningCount = checkResults.count { it.status == "Warning" }
                    val criticalCount = checkResults.count { it.status == "Critical" }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ComplianceStat(
                            count = compliantCount,
                            label = StringResources.getString("ui_complianceCheck_stat_compliant"),
                            color = MaterialTheme.colorScheme.primary
                        )
                        ComplianceStat(
                            count = warningCount,
                            label = StringResources.getString("ui_complianceCheck_stat_warning"),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        ComplianceStat(
                            count = criticalCount,
                            label = StringResources.getString("ui_complianceCheck_stat_critical"),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                //Содержимое результатов
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isLoading) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(StringResources.getString("ui_complianceCheck_loading"))
                        }
                    } else if (checkResults.isEmpty()) {
                        Text(
                            text = StringResources.getString("ui_complianceCheck_no_check"),
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        //Список результатов
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(checkResults) { result ->
                                ComplianceResultItem(result)
                            }
                        }
                    }
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

@Composable
private fun ComplianceStat(
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, RoundedCornerShape(50.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ComplianceResultItem(result: ComplianceResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (result.status) {
                "Critical" -> MaterialTheme.colorScheme.errorContainer
                "Warning" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = result.controlId,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (result.status) {
                        "Critical" -> MaterialTheme.colorScheme.error
                        "Warning" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = when (result.status) {
                        "Compliant" -> StringResources.getString("ui_complianceCheck_status_compliant")
                        "Warning" -> StringResources.getString("ui_complianceCheck_status_warning")
                        "Critical" -> StringResources.getString("ui_complianceCheck_status_critical")
                        else -> result.status
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (result.status) {
                        "Compliant" -> MaterialTheme.colorScheme.primary
                        "Warning" -> MaterialTheme.colorScheme.secondary
                        "Critical" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Text(
                text = result.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (result.recommendation.isNotBlank()) {
                Text(
                    text = StringResources.getString("ui_complianceCheck_recommendation") + ": " + result.recommendation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (result.currentValue.isNotBlank()) {
                Text(
                    text = StringResources.getString("ui_complianceCheck_current") + ": " + result.currentValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (result.requiredValue.isNotBlank()) {
                Text(
                    text = StringResources.getString("ui_complianceCheck_required") + ": " + result.requiredValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

data class ComplianceResult(
    val controlId: String,
    val description: String,
    val status: String,
    val recommendation: String,
    val currentValue: String,
    val requiredValue: String
)

//Проверка соответствия стандартам безопасности
private fun checkCompliance(
    standard: String,
    onResult: (List<ComplianceResult>) -> Unit
) {
    try {
        val policies = getCurrentSecurityPolicies()

        val results = when (standard) {
            "CIS" -> analyzeCISCompliance(policies)
            "NIST" -> analyzeNISTCompliance(policies)
            "PCI" -> analyzePCIDSSCompliance(policies)
            else -> emptyList()
        }

        onResult(results)
    } catch (e: Exception) {
        Logger.error("Exception during compliance check", e)
        onResult(listOf(
            ComplianceResult(
                "ERROR",
                StringResources.getString("ui_complianceCheck_error"),
                "Critical",
                StringResources.getString("ui_complianceCheck_error_desc"),
                "",
                ""
            )
        ))
    }
}



private fun getCurrentSecurityPolicies(): UserSecurityPolicies {
    try {
        if (!AppConfig.isRunningAsAdmin()) {
            Logger.warning("Admin rights required to get security policies")
            return UserSecurityPolicies(8, true, 5, 90, 5)
        }

        val seceditCheck = ProcessBuilder("where", "secedit")
            .redirectErrorStream(true)
            .start()

        if (seceditCheck.waitFor() != 0) {
            Logger.warning("secedit command not found - likely Home Edition of Windows")
            return UserSecurityPolicies(8, true, 5, 90, 5)
        }

        val tempFile = File.createTempFile("policies", ".inf")
        tempFile.deleteOnExit()

        val exportProcess = ProcessBuilder(
            "secedit",
            "/export",
            "/cfg",
            tempFile.absolutePath
        ).start()

        val completed = exportProcess.waitFor(10, TimeUnit.SECONDS)
        if (!completed) {
            Logger.warning("Timeout while exporting security policies")
            exportProcess.destroy()
            tempFile.delete()
            return UserSecurityPolicies(8, true, 5, 90, 5)
        }

        if (exportProcess.exitValue() != 0) {
            Logger.warning("Failed to export security policies. Exit code: ${exportProcess.exitValue()}")
            tempFile.delete()
            return UserSecurityPolicies(8, true, 5, 90, 5)
        }

        if (tempFile.length() == 0L) {
            Logger.warning("Exported security policies file is empty")
            tempFile.delete()
            return UserSecurityPolicies(8, true, 5, 90, 5)
        }

        val possibleCharsets = listOf(
            Charset.forName("CP866"),    //Русская кодировка
            Charset.forName("Windows-1251"), //Альтернативная русская кодировка
            Charset.forName("UTF-8"),    //Универсальная кодировка
            Charset.defaultCharset()     //Системная кодировка
        )

        var policiesContent = ""
        var charsetUsed: Charset? = null

        for (charset in possibleCharsets) {
            try {
                policiesContent = String(tempFile.readBytes(), charset)
                charsetUsed = charset
                break
            } catch (e: Exception) {
                continue
            }
        }

        if (charsetUsed == null) {
            Logger.warning("Failed to read security policies with any charset")
            tempFile.delete()
            return UserSecurityPolicies(8, true, 5, 90, 5)
        }

        Logger.info("Security policies read successfully with charset: ${charsetUsed.name()}")

        return parseSecurityPolicies(policiesContent)
    } catch (e: Exception) {
        Logger.error("Exception during security policy loading", e)
        return UserSecurityPolicies(8, true, 5, 90, 5)
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
            line.contains("MinimumPasswordLength", ignoreCase = true) -> {
                minPasswordLength = extractValue(line)
            }
            line.contains("PasswordComplexity", ignoreCase = true) -> {
                passwordComplexity = extractValue(line) == 1
            }
            line.contains("LockoutBadCount", ignoreCase = true) -> {
                accountLockoutThreshold = extractValue(line)
            }
            line.contains("MaximumPasswordAge", ignoreCase = true) -> {
                passwordExpiration = extractValue(line)
            }
            line.contains("PasswordHistorySize", ignoreCase = true) -> {
                historyCount = extractValue(line)
            }
        }
    }

    minPasswordLength = minPasswordLength.coerceIn(1, 128)
    accountLockoutThreshold = accountLockoutThreshold.coerceIn(0, 999)
    passwordExpiration = passwordExpiration.coerceIn(0, 999)
    historyCount = historyCount.coerceIn(0, 24)

    return UserSecurityPolicies(
        minPasswordLength = minPasswordLength,
        passwordComplexity = passwordComplexity,
        accountLockoutThreshold = accountLockoutThreshold,
        passwordExpiration = passwordExpiration,
        historyCount = historyCount
    )
}

private fun extractValue(line: String): Int {
    val valueStr = line.substringAfter("=").trim()
    return valueStr.toIntOrNull() ?: 0
}

//CIS Benchmark
private fun analyzeCISCompliance(policies: UserSecurityPolicies): List<ComplianceResult> {
    val results = mutableListOf<ComplianceResult>()

    //Минимальная длина пароля
    val minPasswordLength = 14
    val passwordLengthStatus = if (policies.minPasswordLength >= minPasswordLength) "Compliant" else "Critical"
    val passwordLengthRecommendation = if (policies.minPasswordLength < minPasswordLength) {
        StringResources.getString("ui_complianceCheck_cis_1_1_1_recommendation", minPasswordLength.toString())
    } else {
        ""
    }

    results.add(
        ComplianceResult(
            "1.1.1",
            StringResources.getString("ui_complianceCheck_cis_1_1_1_desc"),
            passwordLengthStatus,
            passwordLengthRecommendation,
            policies.minPasswordLength.toString(),
            minPasswordLength.toString()
        )
    )

    //Сложность пароля
    val passwordComplexityStatus = if (policies.passwordComplexity) "Compliant" else "Critical"
    val passwordComplexityRecommendation = if (!policies.passwordComplexity) {
        StringResources.getString("ui_complianceCheck_cis_1_1_2_recommendation")
    } else {
        ""
    }

    results.add(
        ComplianceResult(
            "1.1.2",
            StringResources.getString("ui_complianceCheck_cis_1_1_2_desc"),
            passwordComplexityStatus,
            passwordComplexityRecommendation,
            if (policies.passwordComplexity) "Enabled" else "Disabled",
            "Enabled"
        )
    )

    //Блокировка после неудачных попыток
    val lockoutThreshold = 5
    val lockoutStatus = if (policies.accountLockoutThreshold <= lockoutThreshold) "Compliant" else "Warning"
    val lockoutRecommendation = if (policies.accountLockoutThreshold > lockoutThreshold) {
        StringResources.getString("ui_complianceCheck_cis_1_1_3_recommendation", lockoutThreshold.toString())
    } else {
        ""
    }

    results.add(
        ComplianceResult(
            "1.1.3",
            StringResources.getString("ui_complianceCheck_cis_1_1_3_desc"),
            lockoutStatus,
            lockoutRecommendation,
            policies.accountLockoutThreshold.toString(),
            lockoutThreshold.toString()
        )
    )

    //Срок действия пароля
    val passwordExpiration = 60
    val expirationStatus = if (policies.passwordExpiration <= passwordExpiration && policies.passwordExpiration > 0) "Compliant" else "Warning"
    val expirationRecommendation = if (policies.passwordExpiration > passwordExpiration || policies.passwordExpiration == 0) {
        StringResources.getString("ui_complianceCheck_cis_1_1_4_recommendation", passwordExpiration.toString())
    } else {
        ""
    }

    results.add(
        ComplianceResult(
            "1.1.4",
            StringResources.getString("ui_complianceCheck_cis_1_1_4_desc"),
            expirationStatus,
            expirationRecommendation,
            policies.passwordExpiration.toString(),
            passwordExpiration.toString()
        )
    )

    //История паролей
    val historyCount = 24
    val historyStatus = if (policies.historyCount >= historyCount) "Compliant" else "Warning"
    val historyRecommendation = if (policies.historyCount < historyCount) {
        StringResources.getString("ui_complianceCheck_cis_1_1_5_recommendation", historyCount.toString())
    } else {
        ""
    }

    results.add(
        ComplianceResult(
            "1.1.5",
            StringResources.getString("ui_complianceCheck_cis_1_1_5_desc"),
            historyStatus,
            historyRecommendation,
            policies.historyCount.toString(),
            historyCount.toString()
        )
    )

    return results
}

//NIST 800-53
private fun analyzeNISTCompliance(policies: UserSecurityPolicies): List<ComplianceResult> {
    val results = mutableListOf<ComplianceResult>()

    //Управление аутентификаторами паролей
    val minPasswordLength = 8
    val passwordLengthStatus = if (policies.minPasswordLength >= minPasswordLength) "Compliant" else "Critical"
    val passwordLengthRecommendation = if (policies.minPasswordLength < minPasswordLength) {
        StringResources.getString("ui_complianceCheck_nist_ia5_recommendation", minPasswordLength.toString())
    } else {
        ""
    }

    results.add(
        ComplianceResult(
            "IA-5",
            StringResources.getString("ui_complianceCheck_nist_ia5_desc"),
            passwordLengthStatus,
            passwordLengthRecommendation,
            policies.minPasswordLength.toString(),
            minPasswordLength.toString()
        )
    )

    //Сложность пароля
    val passwordComplexityStatus = if (policies.passwordComplexity) "Compliant" else "Critical"
    val passwordComplexityRecommendation = if (!policies.passwordComplexity) {
        StringResources.getString("ui_complianceCheck_nist_ia5_1_recommendation")
    } else {
        ""
    }

    results.add(
        ComplianceResult(
            "IA-5(1)",
            StringResources.getString("ui_complianceCheck_nist_ia5_1_desc"),
            passwordComplexityStatus,
            passwordComplexityRecommendation,
            if (policies.passwordComplexity) "Enabled" else "Disabled",
            "Enabled"
        )
    )

    //Блокировка после неудачных попыток
    val lockoutThreshold = 3
    val lockoutStatus = if (policies.accountLockoutThreshold <= lockoutThreshold && policies.accountLockoutThreshold > 0) "Compliant" else "Critical"
    val lockoutRecommendation = if (policies.accountLockoutThreshold > lockoutThreshold || policies.accountLockoutThreshold == 0) {
        StringResources.getString("ui_complianceCheck_nist_ia5_2_recommendation", lockoutThreshold.toString())
    } else {
        ""
    }

    results.add(
        ComplianceResult(
            "IA-5(2)",
            StringResources.getString("ui_complianceCheck_nist_ia5_2_desc"),
            lockoutStatus,
            lockoutRecommendation,
            policies.accountLockoutThreshold.toString(),
            lockoutThreshold.toString()
        )
    )

    //Срок действия пароля
    val passwordExpiration = 60
    val expirationStatus = if (policies.passwordExpiration <= passwordExpiration && policies.passwordExpiration > 0) "Compliant" else "Warning"
    val expirationRecommendation = if (policies.passwordExpiration > passwordExpiration || policies.passwordExpiration == 0) {
        StringResources.getString("ui_complianceCheck_nist_ia5_3_recommendation", passwordExpiration.toString())
    } else {
        ""
    }

    results.add(
        ComplianceResult(
            "IA-5(1)(3)",
            StringResources.getString("ui_complianceCheck_nist_ia5_3_desc"),
            expirationStatus,
            expirationRecommendation,
            policies.passwordExpiration.toString(),
            passwordExpiration.toString()
        )
    )

    //История паролей
    val historyCount = 5
    val historyStatus = if (policies.historyCount >= historyCount) "Compliant" else "Warning"
    val historyRecommendation = if (policies.historyCount < historyCount) {
        StringResources.getString("ui_complianceCheck_nist_ia5_4_recommendation", historyCount.toString())
    } else {
        ""
    }

    results.add(
        ComplianceResult(
            "IA-5(1)(4)",
            StringResources.getString("ui_complianceCheck_nist_ia5_4_desc"),
            historyStatus,
            historyRecommendation,
            policies.historyCount.toString(),
            historyCount.toString()
        )
    )

    return results
}

//PCI DSS
private fun analyzePCIDSSCompliance(policies: UserSecurityPolicies): List<ComplianceResult> {
    val results = mutableListOf<ComplianceResult>()

    //Минимальная длина пароля
    val minPasswordLength = 7
    val passwordLengthStatus = if (policies.minPasswordLength >= minPasswordLength) "Compliant" else "Critical"
    val passwordLengthRecommendation = if (policies.minPasswordLength < minPasswordLength) {
        StringResources.getString("ui_complianceCheck_pci_8_2_3_recommendation", minPasswordLength.toString())
    } else {
        ""
    }

    results.add(
        ComplianceResult(
            "8.2.3",
            StringResources.getString("ui_complianceCheck_pci_8_2_3_desc"),
            passwordLengthStatus,
            passwordLengthRecommendation,
            policies.minPasswordLength.toString(),
            minPasswordLength.toString()
        )
    )

    //Сложность пароля
    val passwordComplexityStatus = if (policies.passwordComplexity) "Compliant" else "Critical"
    val passwordComplexityRecommendation = if (!policies.passwordComplexity) {
        StringResources.getString("ui_complianceCheck_pci_8_2_1_recommendation")
    } else {
        ""
    }

    results.add(
        ComplianceResult(
            "8.2.1",
            StringResources.getString("ui_complianceCheck_pci_8_2_1_desc"),
            passwordComplexityStatus,
            passwordComplexityRecommendation,
            if (policies.passwordComplexity) "Enabled" else "Disabled",
            "Enabled"
        )
    )

    //Блокировка после неудачных попыток
    val lockoutThreshold = 6
    val lockoutStatus = if (policies.accountLockoutThreshold <= lockoutThreshold && policies.accountLockoutThreshold > 0) "Compliant" else "Warning"
    val lockoutRecommendation = if (policies.accountLockoutThreshold > lockoutThreshold || policies.accountLockoutThreshold == 0) {
        StringResources.getString("ui_complianceCheck_pci_8_1_8_recommendation", lockoutThreshold.toString())
    } else {
        ""
    }

    results.add(
        ComplianceResult(
            "8.1.8",
            StringResources.getString("ui_complianceCheck_pci_8_1_8_desc"),
            lockoutStatus,
            lockoutRecommendation,
            policies.accountLockoutThreshold.toString(),
            lockoutThreshold.toString()
        )
    )

    //Срок действия пароля
    val passwordExpiration = 90
    val expirationStatus = if (policies.passwordExpiration <= passwordExpiration && policies.passwordExpiration > 0) "Compliant" else "Warning"
    val expirationRecommendation = if (policies.passwordExpiration > passwordExpiration || policies.passwordExpiration == 0) {
        StringResources.getString("ui_complianceCheck_pci_8_2_4_recommendation", passwordExpiration.toString())
    } else {
        ""
    }

    results.add(
        ComplianceResult(
            "8.2.4",
            StringResources.getString("ui_complianceCheck_pci_8_2_4_desc"),
            expirationStatus,
            expirationRecommendation,
            policies.passwordExpiration.toString(),
            passwordExpiration.toString()
        )
    )

    //История паролей
    val historyCount = 4
    val historyStatus = if (policies.historyCount >= historyCount) "Compliant" else "Warning"
    val historyRecommendation = if (policies.historyCount < historyCount) {
        StringResources.getString("ui_complianceCheck_pci_8_2_5_recommendation", historyCount.toString())
    } else {
        ""
    }

    results.add(
        ComplianceResult(
            "8.2.5",
            StringResources.getString("ui_complianceCheck_pci_8_2_5_desc"),
            historyStatus,
            historyRecommendation,
            policies.historyCount.toString(),
            historyCount.toString()
        )
    )

    return results
}