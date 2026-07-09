package org.crepo.updated_user_manager

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.*
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.coroutines.coroutineContext

@Composable
fun UI4_2_SuspiciousActivityScreenContent(navigateBack: () -> Unit) {
    // Состояние экрана
    val (logType, setLogType) = remember { mutableStateOf("Security") }
    val (maxEvents, setMaxEvents) = remember { mutableStateOf(50) }
    val (batchSize, setBatchSize) = remember { mutableStateOf(5) }
    val (logs, setLogs) = remember { mutableStateOf(mutableStateListOf<SystemLogEntry>()) }
    val (isLoading, setIsLoading) = remember { mutableStateOf(false) }
    val (isExpanded, setIsExpanded) = remember { mutableStateOf(false) }
    val (isFilterExpanded, setIsFilterExpanded) = remember { mutableStateOf(false) }
    val (filterSeverity, setFilterSeverity) = remember { mutableStateOf("All") }
    val (currentEvent, setCurrentEvent) = remember { mutableStateOf(0) }
    val (isCancelled, setIsCancelled) = remember { mutableStateOf(false) }
    val (showSettings, setShowSettings) = remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val (loadJob, setLoadJob) = remember { mutableStateOf<Job?>(null) }


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
            text = StringResources.getString("ui_systemLogs_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Text(
            text = StringResources.getString("ui_systemLogs_subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Кнопка для скрытия/отображения настроек
        Button(
            onClick = { setShowSettings(!showSettings) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = if (showSettings) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
                    contentDescription = null
                )
                Text(
                    text = if (showSettings)
                        StringResources.getString("ui_systemLogs_hide_settings")
                    else
                        StringResources.getString("ui_systemLogs_show_settings"),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // Область настроек (условно отображаемая)
        if (showSettings) {
            SettingsArea(
                logType = logType,
                setLogType = setLogType,
                maxEvents = maxEvents,
                setMaxEvents = setMaxEvents,
                batchSize = batchSize,
                setBatchSize = setBatchSize,
                filterSeverity = filterSeverity,
                setFilterSeverity = setFilterSeverity,
                isLoading = isLoading,
                isCancelled = isCancelled,
                currentEvent = currentEvent,
                maxEventsTotal = maxEvents,
                onScan = {
                    loadJob?.cancel()

                    setIsCancelled(false)
                    setIsLoading(true)
                    setCurrentEvent(0)
                    logs.clear()

                    val newJob = coroutineScope.launch {
                        loadSystemLogsInBatches(
                            lifecycleOwner = lifecycleOwner,
                            logType = logType,
                            totalEvents = maxEvents,
                            batchSize = batchSize,
                            onBatchLoaded = { batch ->
                                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                                    logs.addAll(batch)
                                    setCurrentEvent(logs.size)
                                }
                            },
                            onFinished = {
                                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                                    setIsLoading(false)
                                    setLoadJob(null)
                                }
                            }
                        )
                    }
                    setLoadJob(newJob)
                },
                onCancel = {
                    loadJob?.cancel()
                    setLoadJob(null)
                    setIsLoading(false)
                    setIsCancelled(true)
                }
            )
        }

        // Список логов
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
                // Заголовок списка
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = StringResources.getString("ui_systemLogs_events"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (!isLoading && logs.isNotEmpty()) {
                        val filteredLogs = if (filterSeverity == "All") logs else logs.filter { it.severity == filterSeverity }
                        Text(
                            text = StringResources.getString("ui_systemLogs_found", filteredLogs.size.toString()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Содержимое списка
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isLoading && logs.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(StringResources.getString("ui_systemLogs_loading"))
                        }
                    } else if (logs.isEmpty()) {
                        Text(
                            text = StringResources.getString("ui_systemLogs_no_events"),
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val filteredLogs = if (filterSeverity == "All") logs else logs.filter { it.severity == filterSeverity }

                        if (filteredLogs.isEmpty()) {
                            Text(
                                text = StringResources.getString("ui_systemLogs_no_filtered_events"),
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredLogs) { entry ->
                                    LogEntryCard(entry)
                                }
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
                text = StringResources.getString("btn_back"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsArea(
    logType: String,
    setLogType: (String) -> Unit,
    maxEvents: Int,
    setMaxEvents: (Int) -> Unit,
    batchSize: Int,
    setBatchSize: (Int) -> Unit,
    filterSeverity: String,
    setFilterSeverity: (String) -> Unit,
    isLoading: Boolean,
    isCancelled: Boolean,
    currentEvent: Int,
    maxEventsTotal: Int,
    onScan: () -> Unit,
    onCancel: () -> Unit
) {
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
            // Тип логов
            Text(
                text = StringResources.getString("ui_systemLogs_logType"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = StringResources.getString("ui_systemLogs_logType_desc"),
                    style = MaterialTheme.typography.bodyMedium
                )

                var isExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { expanded ->
                        if (expanded) {
                            isExpanded = true
                        } else {
                            isExpanded = false
                        }
                    }
                ) {
                    TextField(
                        readOnly = true,
                        value = logType,
                        onValueChange = {},
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Security") },
                            onClick = {
                                setLogType("Security")
                                isExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("System") },
                            onClick = {
                                setLogType("System")
                                isExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Application") },
                            onClick = {
                                setLogType("Application")
                                isExpanded = false
                            }
                        )
                    }
                }
            }

            // Количество событий
            Text(
                text = StringResources.getString("ui_systemLogs_maxEvents"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = StringResources.getString("ui_systemLogs_maxEvents_desc"),
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = maxEvents.toString(),
                    onValueChange = {
                        val value = it.toIntOrNull() ?: 0
                        if (value in 1..1000) setMaxEvents(value)
                        else if (value > 1000) setMaxEvents(1000)
                        else if (value < 1) setMaxEvents(1)
                    },
                    label = { Text(StringResources.getString("ui_systemLogs_maxEvents_label")) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier
                        .width(120.dp)
                        .padding(vertical = 4.dp)
                )
            }

            // Размер пакета
            Text(
                text = StringResources.getString("ui_systemLogs_batchSize"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = StringResources.getString("ui_systemLogs_batchSize_desc"),
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = batchSize.toString(),
                    onValueChange = {
                        val value = it.toIntOrNull() ?: 0
                        if (value in 1..50) setBatchSize(value)
                        else if (value > 50) setBatchSize(50)
                        else if (value < 1) setBatchSize(1)
                    },
                    label = { Text(StringResources.getString("ui_systemLogs_batchSize_label")) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier
                        .width(120.dp)
                        .padding(vertical = 4.dp)
                )
            }

            // Фильтр по уровню важности
            Text(
                text = StringResources.getString("ui_systemLogs_filterSeverity"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = StringResources.getString("ui_systemLogs_filterSeverity_desc"),
                    style = MaterialTheme.typography.bodyMedium
                )

                var isFilterExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = isFilterExpanded,
                    onExpandedChange = { expanded ->
                        if (expanded) {
                            isFilterExpanded = true
                        } else {
                            isFilterExpanded = false
                        }
                    }
                ) {
                    TextField(
                        readOnly = true,
                        value = when (filterSeverity) {
                            "All" -> StringResources.getString("ui_systemLogs_filter_all")
                            "Critical" -> StringResources.getString("ui_systemLogs_filter_critical")
                            "Warning" -> StringResources.getString("ui_systemLogs_filter_warning")
                            "Info" -> StringResources.getString("ui_systemLogs_filter_info")
                            else -> filterSeverity
                        },
                        onValueChange = {},
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFilterExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isFilterExpanded,
                        onDismissRequest = { isFilterExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(StringResources.getString("ui_systemLogs_filter_all")) },
                            onClick = {
                                setFilterSeverity("All")
                                isFilterExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(StringResources.getString("ui_systemLogs_filter_critical")) },
                            onClick = {
                                setFilterSeverity("Critical")
                                isFilterExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(StringResources.getString("ui_systemLogs_filter_warning")) },
                            onClick = {
                                setFilterSeverity("Warning")
                                isFilterExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(StringResources.getString("ui_systemLogs_filter_info")) },
                            onClick = {
                                setFilterSeverity("Info")
                                isFilterExpanded = false
                            }
                        )
                    }
                }
            }

            // Кнопки управления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCancel,
                    enabled = isLoading && !isCancelled,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(StringResources.getString("ui_systemLogs_cancel"))
                }

                Button(
                    onClick = onScan,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = StringResources.getString("ui_systemLogs_scan"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Прогресс
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { currentEvent.toFloat() / maxEventsTotal.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                    Text(
                        text = StringResources.getString("ui_systemLogs_progress", currentEvent.toString(), maxEventsTotal.toString()),
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LogEntryCard(entry: SystemLogEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (entry.severity) {
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
                    text = entry.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (entry.severity) {
                        "Critical" -> MaterialTheme.colorScheme.error
                        "Warning" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = entry.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (entry.details.isNotBlank()) {
                Text(
                    text = entry.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .padding(start = 8.dp)
                )
            }
        }
    }
}

data class SystemLogEntry(
    var title: String,
    var description: String,
    val details: String,
    val timestamp: String,
    var severity: String
)

// Загрузка системных логов порциями
// Загрузка системных логов порциями
private suspend fun loadSystemLogsInBatches(
    lifecycleOwner: LifecycleOwner,
    logType: String,
    totalEvents: Int,
    batchSize: Int,
    onBatchLoaded: (List<SystemLogEntry>) -> Unit,
    onFinished: () -> Unit
) {
    var loadedEvents = 0

    try {
        while (loadedEvents < totalEvents) {
            // КРИТИЧЕСКИ ВАЖНО: Проверяем, не была ли корутина отменена
            coroutineContext.ensureActive()

            // Проверяем, не был ли уничтожен жизненный цикл
            if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                break
            }

            val eventsToLoad = minOf(batchSize, totalEvents - loadedEvents)

            // Загружаем пакет логов
            val batch = loadSingleBatch(logType, eventsToLoad, loadedEvents)

            // Проверяем, не был ли уничтожен жизненный цикл во время загрузки
            if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                break
            }

            // Проверяем, не была ли корутина отменена
            coroutineContext.ensureActive()

            onBatchLoaded(batch)
            loadedEvents += eventsToLoad

            // Даем время UI обновиться
            delay(50)
        }
    } catch (e: CancellationException) {
        // Корутина была отменена - это нормально
        Logger.info("Log loading was cancelled by user")
    } catch (e: Exception) {
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            Logger.error("Exception during system logs batch loading", e)
        }
    } finally {
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            onFinished()
        }
    }
}

// Загрузка одной порции логов
private fun loadSingleBatch(
    logType: String,
    maxEvents: Int,
    skipEvents: Int
): List<SystemLogEntry> {
    try {
        // Проверяем, что приложение запущено от имени администратора
        if (!AppConfig.isRunningAsAdmin()) {
            return listOf(
                SystemLogEntry(
                    StringResources.getString("ui_systemLogs_error_title"),
                    StringResources.getString("ui_systemLogs_error_admin_required"),
                    StringResources.getString("ui_systemLogs_error_admin_required_desc"),
                    "Critical",
                    ""
                )
            )
        }

        // Проверяем, доступен ли PowerShell
        val testPsProcess = ProcessBuilder("powershell", "-Command", "Get-Command Get-WinEvent -ErrorAction SilentlyContinue")
        val testPsExitCode = testPsProcess.start().waitFor()

        if (testPsExitCode != 0) {
            return listOf(
                SystemLogEntry(
                    StringResources.getString("ui_systemLogs_error_title"),
                    StringResources.getString("ui_systemLogs_error_ps_not_found"),
                    StringResources.getString("ui_systemLogs_error_ps_not_found_desc"),
                    "Critical",
                    ""
                )
            )
        }

        // Проверяем версию PowerShell
        val psVersionProcess = ProcessBuilder("powershell", "-Command", $$"""
    `$PSVersionTable.PSVersion.Major
""".trimIndent())
        val psVersion = try {
            val output = psVersionProcess.start().inputStream.bufferedReader().readText().trim()
            output.toIntOrNull() ?: 0
        } catch (e: Exception) {
            Logger.error("Failed to get PowerShell version", e)
            0
        }

        // Проверка наличия PowerShell
        val wherePsProcess = ProcessBuilder("where", "powershell")
        val wherePsExitCode = wherePsProcess.start().waitFor()

        if (wherePsExitCode != 0) {
            return listOf(
                SystemLogEntry(
                    StringResources.getString("ui_systemLogs_error_title"),
                    StringResources.getString("ui_systemLogs_error_ps_not_found"),
                    StringResources.getString("ui_systemLogs_error_ps_not_found_desc"),
                    "Critical",
                    ""
                )
            )
        }


        // Упрощенная команда без параметра -Skip (проблемный параметр)
        val command = if (psVersion >= 5) {
            // Для новых версий PowerShell
            "Get-WinEvent -LogName '$logType' -MaxEvents $maxEvents | " +
                    "Select-Object TimeCreated, Id, Message | " +
                    "ConvertTo-Csv -NoTypeInformation"
        } else {
            // Для старых версий PowerShell
            "Get-EventLog -LogName '$logType' -Newest $maxEvents | " +
                    "Select-Object TimeGenerated, EventID, Message | " +
                    "ConvertTo-Csv -NoTypeInformation"
        }

        val process = ProcessBuilder("powershell", "-Command", command)
            .redirectErrorStream(true)
            .start()

        // Добавляем таймаут для предотвращения зависания
        val timeoutMs = 10000 // 10 секунд
        val outputBytes = try {
            process.waitFor(timeoutMs.toLong(), TimeUnit.MILLISECONDS)
            process.inputStream.readBytes()
        } catch (e: TimeoutException) {
            process.destroy()
            return listOf(
                SystemLogEntry(
                    StringResources.getString("ui_systemLogs_error_title"),
                    StringResources.getString("ui_systemLogs_error_timeout"),
                    StringResources.getString("ui_systemLogs_error_timeout_desc"),
                    "Critical",
                    ""
                )
            )
        }

        val exitCode = process.exitValue()
        if (exitCode != 0) {
            // КРИТИЧЕСКИ ВАЖНО: Читаем реальное сообщение об ошибке
            val errorOutput = try {
                String(process.errorStream.readBytes(), Charset.forName("CP866"))
            } catch (e: Exception) {
                "Failed to read error stream"
            }

            return listOf(
                SystemLogEntry(
                    StringResources.getString("ui_systemLogs_error_title"),
                    StringResources.getString("ui_systemLogs_error_load_failed"),
                    "PowerShell error (code $exitCode): $errorOutput",
                    "Critical",
                    ""
                )
            )
        }

        // Читаем вывод с правильной кодировкой
        val output = String(outputBytes, Charset.forName("CP866"))

        // Парсим вывод в формате CSV
        return parseCsvLogs(output)
    } catch (e: Exception) {
        Logger.error("Exception during system logs loading", e)
        return listOf(
            SystemLogEntry(
                StringResources.getString("ui_systemLogs_error_title"),
                StringResources.getString("ui_systemLogs_error_load_failed"),
                StringResources.getString("ui_systemLogs_error_exception", e.message ?: "Unknown error"),
                "Critical",
                ""
            )
        )
    }
}

// Парсинг вывода в формате CSV
private fun parseCsvLogs(csv: String): List<SystemLogEntry> {
    val entries = mutableListOf<SystemLogEntry>()

    // Разбиваем на строки
    val lines = csv.split("\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }

    // Пропускаем заголовок CSV
    for (line in lines.drop(1)) {
        try {
            // Парсим CSV вручную (простой парсер)
            val parts = parseSimpleCsvLine(line)
            if (parts.size < 3) continue

            val timestamp = parts[0].trim('"')
            val idStr = parts[1].trim('"')
            val message = parts[2].trim('"')

            val id = idStr.toIntOrNull() ?: 0

            if (timestamp.isNotEmpty() && message.isNotEmpty()) {
                val severity = when {
                    id in listOf(4625, 4740, 4769, 4771) -> "Warning"
                    id in listOf(4726, 4724) -> "Critical"
                    else -> "Info"
                }

                val title = when (id) {
                    4624 -> "Успешный вход"
                    4625 -> "Неудачная попытка входа"
                    4627 -> "Учетная запись разблокирована"
                    4628 -> "Сессия пользователя заблокирована"
                    4720 -> "Создание учетной записи"
                    4722 -> "Учетная запись включена"
                    4723 -> "Попытка изменения пароля"
                    4724 -> "Попытка сброса пароля"
                    4725 -> "Учетная запись отключена"
                    4726 -> "Удаление учетной записи"
                    4732 -> "Добавление пользователя в группу"
                    4733 -> "Удаление пользователя из группы"
                    4740 -> "Учетная запись заблокирована"
                    4767 -> "Неудачная попытка входа в учетную запись"
                    4768 -> "Запрос билета Kerberos"
                    4769 -> "Неудачный запрос билета Kerberos"
                    4771 -> "Неудачная предварительная проверка Kerberos"
                    4776 -> "Попытка проверки учетных данных"
                    4778 -> "Сессия пользователя восстановлена"
                    else -> "Событие ID: $id"
                }

                entries.add(SystemLogEntry(
                    title = title,
                    description = message,
                    details = "",
                    timestamp = timestamp,
                    severity = severity
                ))
            }
        } catch (e: Exception) {
            // Пропускаем проблемные строки
            Logger.warning("Error parsing log line: $line. ${e.message}")
        }
    }

    return entries
}

// Простой парсер CSV (без поддержки сложных случаев)
private fun parseSimpleCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    var current = ""
    var inQuotes = false

    for (c in line) {
        when {
            c == '"' -> inQuotes = !inQuotes
            c == ',' && !inQuotes -> {
                result.add(current)
                current = ""
            }
            else -> current += c
        }
    }

    result.add(current)
    return result
}

// Парсинг вывода PowerShell (улучшенная версия)
private fun parseSystemLogs(output: String): List<SystemLogEntry> {
    val entries = mutableListOf<SystemLogEntry>()

    // Разбиваем на строки и фильтруем пустые
    val lines = output.split("\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    var currentEntry: SystemLogEntry? = null

    for (line in lines) {
        if (line.startsWith("Time:")) {
            if (currentEntry != null && currentEntry.timestamp.isNotEmpty() && currentEntry.description.isNotEmpty()) {
                entries.add(currentEntry)
            }
            currentEntry = SystemLogEntry(
                title = "",
                description = "",
                details = "",
                timestamp = line.substringAfter("Time:").trim(),
                severity = "Info"
            )
        } else if (line.startsWith("Id:")) {
            val id = line.substringAfter("Id:").trim().toIntOrNull() ?: 0
            currentEntry?.let { entry ->
                entry.title = when (id) {
                    4624 -> "Успешный вход"
                    4625 -> "Неудачная попытка входа"
                    else -> "Событие ID: $id"
                }
                entry.severity = when {
                    id in listOf(4625, 4740, 4769, 4771) -> "Warning"
                    else -> "Info"
                }
            }
        } else if (line.startsWith("Message:")) {
            currentEntry?.description = line.substringAfter("Message:").trim()
        }
    }

    if (currentEntry != null && currentEntry.timestamp.isNotEmpty() && currentEntry.description.isNotEmpty()) {
        entries.add(currentEntry)
    }

    return entries
}