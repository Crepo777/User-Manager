package org.crepo.updated_user_manager


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import java.util.Locale

@Composable
fun DisclaimerScreenContent() {
    val navigator = LocalNavigator.current ?: return

    val currentLocale by remember { derivedStateOf { LanguageManager.currentLocale } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Заголовок
        Text(
            text = StringResources.getString("ui_disclaimer_title"),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            ),
            textAlign = TextAlign.Center
        )

        //Основной текст
        Text(
            text = StringResources.getString("ui_disclaimer_text"),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        //Важное примечание
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text(
                text = StringResources.getString("ui_disclaimer_note"),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        //Кнопки выбора языка
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LanguageButton(
                label = "ru - Русский",
                isSelected = currentLocale.language == "ru",
                onClick = { LanguageManager.setLocale(Locale("ru")) }
            )

            LanguageButton(
                label = "en - English",
                isSelected = currentLocale.language == "en",
                onClick = { LanguageManager.setLocale(Locale("en")) }
            )
        }

        //Кнопка принятия
        Button(
            onClick = {
                AppConfig.saveDisclaimerAccepted()
                navigator.replace(MainScreen)
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
        ) {
            Text(
                text = StringResources.getString("ui_disclaimer_accept"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
    //Row(
    //   horizontalArrangement = Arrangement.SpaceBetween,
    //   modifier = Modifier.fillMaxWidth()
    //) {
    //   Text(
    //       text = "v1.0.0 | © 2026 Крайнов Иван",
    //       style = MaterialTheme.typography.labelSmall,
    //       color = MaterialTheme.colorScheme.onSurfaceVariant
    //   )}
}

@Composable
private fun LanguageButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .height(48.dp)
            //.weight(1f)
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}