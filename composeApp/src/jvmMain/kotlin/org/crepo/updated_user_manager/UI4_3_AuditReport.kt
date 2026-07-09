package org.crepo.updated_user_manager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import cafe.adriel.voyager.navigator.LocalNavigator
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UI4_3_AuditReportScreenContent(navigateBack: () -> Unit) {
    var reportDate by remember { mutableStateOf(LocalDate.now()) }
    var reportType by remember { mutableStateOf("User") }
    var reportData by remember { mutableStateOf(emptyList<AuditRecord>()) }

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
            text = StringResources.getString("ui_auditReport_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Text(
            text = StringResources.getString("ui_auditReport_subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Настройки отчета
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Тип отчета
                Text(
                    text = StringResources.getString("ui_auditReport_type"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(StringResources.getString("ui_auditReport_type_desc"))
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        TextField(
                            readOnly = true,
                            value = reportType,
                            onValueChange = { /* Обработка выбора */ },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            // Список типов отчетов
                            Text("User", modifier = Modifier.clickable {
                                reportType = "User"
                                expanded = false
                            })
                            Text("Permissions", modifier = Modifier.clickable {
                                reportType = "Permissions"
                                expanded = false
                            })
                            Text("Security", modifier = Modifier.clickable {
                                reportType = "Security"
                                expanded = false
                            })
                        }
                    }
                }

                // Дата отчета
                Text(
                    text = StringResources.getString("ui_auditReport_date"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(StringResources.getString("ui_auditReport_date_desc"))
                    Button(
                        onClick = { /* Выбор даты */ },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(reportDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    }
                }

                // Кнопка генерации отчета
                Button(
                    onClick = {
                        generateAuditReport(reportType, reportDate) { records ->
                            reportData = records
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = StringResources.getString("ui_auditReport_generate"),
                        fontSize = 16.sp
                    )
                }

                // Отчет
                Text(
                    text = StringResources.getString("ui_auditReport_results"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reportData) { record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = record.timestamp,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = record.type,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = record.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = record.details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Кнопка "Назад"
        Button(
            onClick = navigateBack,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
        ) {
            Text(
                text = StringResources.getString("ui_auditReport_btn_back"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class AuditRecord(
    val timestamp: String,
    val type: String,
    val description: String,
    val details: String
)

private fun generateAuditReport(type: String, date: LocalDate, onResult: (List<AuditRecord>) -> Unit) {
    try {
        // Генерируем отчет на основе логов
        val logFile = File(System.getProperty("user.home"), ".usermanager/usermanager.log")
        if (!logFile.exists()) {
            onResult(emptyList())
            return
        }

        val logLines = logFile.readLines()
        val auditRecords = mutableListOf<AuditRecord>()

        logLines.forEach { line ->
            val timestamp = line.substring(1, 25)
            val level = line.substringAfter("[")
            val message = line.substringAfter("] ")

            when (type) {
                "User" -> {
                    if (message.contains("User created") ||
                        message.contains("User deleted") ||
                        message.contains("Password changed")) {
                        auditRecords.add(AuditRecord(
                            timestamp = timestamp,
                            type = "User Management",
                            description = message,
                            details = "User management activity"
                        ))
                    }
                }
                "Permissions" -> {
                    if (message.contains("Permissions set") ||
                        message.contains("Permissions viewed")) {
                        auditRecords.add(AuditRecord(
                            timestamp = timestamp,
                            type = "Permissions",
                            description = message,
                            details = "Permissions management activity"
                        ))
                    }
                }
                "Security" -> {
                    if (message.contains("Failed") ||
                        message.contains("Access denied") ||
                        message.contains("Security event")) {
                        auditRecords.add(AuditRecord(
                            timestamp = timestamp,
                            type = "Security",
                            description = message,
                            details = "Security-related activity"
                        ))
                    }
                }
            }
        }

        onResult(auditRecords)
    } catch (e: Exception) {
        Logger.error("Exception during audit report generation", e)
        onResult(emptyList())
    }
}