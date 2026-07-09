package org.crepo.updated_user_manager

// SettingsScreen.kt

import org.crepo.updated_user_manager.Logger
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import java.io.File

@Composable
fun SettingsScreenContent() {
    val navigator = LocalNavigator.current ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок
        Text(
            text = StringResources.getString("ui_settings_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        // Описание
        Text(
            text = StringResources.getString("ui_settings_hint"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Карточка с логами
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = StringResources.getString("ui_settings_logs_title"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка открытия папки с логами
                Button(
                    onClick = {
                        val logDir = File(System.getProperty("user.home"), ".usermanager")
                        if (logDir.exists()) {
                            ProcessBuilder("explorer", logDir.absolutePath).start()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = StringResources.getString("ui_settings_logs_open"),
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Кнопка очистки логов
                Button(
                    onClick = {
                        val logFile = File(System.getProperty("user.home"), ".usermanager/usermanager.log")
                        if (logFile.exists()) {
                            logFile.writeText("") // Очищаем файл
                            Logger.info("Log file cleared by user")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(
                        text = StringResources.getString("ui_settings_logs_clear"),
                        fontSize = 16.sp
                    )
                }

                // Кнопка экспорта логов
                Button(
                    onClick = {
                        val csv = Logger.exportToCsv()
                        val exportFile = File(System.getProperty("user.home"), "usermanager_logs.csv")
                        exportFile.writeText("Timestamp,Level,Message\n$csv")

                        // Открываем папку с экспортированным файлом
                        ProcessBuilder("explorer", "/select,${exportFile.absolutePath}").start()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Экспортировать логи в CSV",
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Информация о логах
                val logFile = File(System.getProperty("user.home"), ".usermanager/usermanager.log")
                val logSize = if (logFile.exists()) logFile.length() / 1024 else 0

                Text(
                    text = StringResources.getString("ui_settings_logs_size_1") + logSize + StringResources.getString("ui_settings_logs_size_2"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Кнопка "Назад"
        Button(
            onClick = { navigator.pop() },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
        ) {
            Text(
                text = StringResources.getString("ui_settings_back"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}