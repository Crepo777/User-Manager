package org.crepo.updated_user_manager

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UI_1_3_FuturePlansScreenContent(navigateBack: () -> Unit) {
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
            text = StringResources.getString("ui_future_plans_title"),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = StringResources.getString("ui_future_plans_subtitle"),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        //Основная информация
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //1
                PlanItem(
                    title = StringResources.getString("ui_future_plan1_title"),
                    description = StringResources.getString("ui_future_plan1_desc")
                )

                //2
                PlanItem(
                    title = StringResources.getString("ui_future_plan2_title"),
                    description = StringResources.getString("ui_future_plan2_desc")
                )

                //3
                PlanItem(
                    title = StringResources.getString("ui_future_plan3_title"),
                    description = StringResources.getString("ui_future_plan3_desc")
                )

                //4
                PlanItem(
                    title = StringResources.getString("ui_future_plan4_title"),
                    description = StringResources.getString("ui_future_plan4_desc")
                )

                //5
                PlanItem(
                    title = StringResources.getString("ui_future_plan5_title"),
                    description = StringResources.getString("ui_future_plan5_desc")
                )
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState)
            )
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
private fun PlanItem(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Start
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start
        )
    }
}