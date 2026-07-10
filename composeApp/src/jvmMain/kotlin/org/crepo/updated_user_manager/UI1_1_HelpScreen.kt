// UI1_1_HelpScreen.kt
package org.crepo.updated_user_manager

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun HelpScreenContent(navigateBack: () -> Unit) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var activeSection by remember { mutableStateOf(0) }

    // Обновляем активный раздел при прокрутке
    LaunchedEffect(scrollState.firstVisibleItemIndex) {
        activeSection = scrollState.firstVisibleItemIndex
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Заголовок
        Text(
            text = StringResources.getString("help_title"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = StringResources.getString("help_subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Основное содержимое
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Оглавление
                NavigationMenu(
                    activeSection = activeSection,
                    onSectionClick = { index ->
                        coroutineScope.launch {
                            scrollState.animateScrollToItem(index)
                        }
                        activeSection = index
                    }
                )

                // Содержание
                HelpContent(
                    scrollState = scrollState
                )
            }

            // Назад
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = navigateBack,
                    modifier = Modifier
                        .width(120.dp)
                        .height(48.dp)
                ) {
                    Text(
                        text = StringResources.getString("help_back"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationMenu(
    activeSection: Int,
    onSectionClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = StringResources.getString("help_toc_title"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Разделы оглавления
        val sections = listOf(
            StringResources.getString("help_section_users"),
            StringResources.getString("help_section_files"),
            StringResources.getString("help_section_tools"),
            StringResources.getString("help_section_logs"),
            StringResources.getString("help_subsection_security_policy")
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sections.size) { index ->
                val isActive = index == activeSection
                val backgroundColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent
                val contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSectionClick(index) }
                        .background(backgroundColor, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = sections[index],
                        color = contentColor,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpContent(
    scrollState: LazyListState
) {
    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(24.dp)
    ) {
        //Раздел 1: Пользователи
        item {
            HelpSection(
                title = StringResources.getString("help_section_users"),
                content = {
                    //Создание пользователя
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_create_user"),
                        content = {
                            Text(StringResources.getString("help_create_user_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_create_user_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_create_user_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_create_user_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_create_user_tips"))
                        }
                    )

                    //Удаление пользователя
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_delete_user"),
                        content = {
                            Text(StringResources.getString("help_delete_user_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_delete_user_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_delete_user_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_delete_user_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_delete_user_tips"))
                        }
                    )

                    //Изменение пароля
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_change_password"),
                        content = {
                            Text(StringResources.getString("help_change_password_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_change_password_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_change_password_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_change_password_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_change_password_tips"))
                        }
                    )

                    //Переименование пользователя
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_rename_user"),
                        content = {
                            Text(StringResources.getString("help_rename_user_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_rename_user_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_rename_user_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_rename_user_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_rename_user_tips"))
                        }
                    )

                    //Блокировка/Разблокировка пользователя
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_toggle_user_status"),
                        content = {
                            Text(StringResources.getString("help_toggle_user_status_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_toggle_user_status_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_toggle_user_status_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_toggle_user_status_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_toggle_user_status_tips"))
                        }
                    )

                    //Управление членством в группах
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_manage_group_membership"),
                        content = {
                            Text(StringResources.getString("help_manage_group_membership_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_manage_group_membership_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_manage_group_membership_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_manage_group_membership_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_manage_group_membership_tips"))
                        }
                    )

                    //Управление группами
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_manage_groups"),
                        content = {
                            Text(StringResources.getString("help_manage_groups_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_manage_groups_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_manage_groups_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_manage_groups_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_manage_groups_tips"))
                        }
                    )
                }
            )
        }

        //Раздел 2: Управление файлами и правами
        item {
            HelpSection(
                title = StringResources.getString("help_section_files"),
                content = {
                    //Назначение прав
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_set_permissions"),
                        content = {
                            Text(StringResources.getString("help_set_permissions_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_set_permissions_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_set_permissions_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_set_permissions_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_set_permissions_tips"))
                        }
                    )

                    //Просмотр прав доступа
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_view_permissions"),
                        content = {
                            Text(StringResources.getString("help_view_permissions_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_view_permissions_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_view_permissions_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_view_permissions_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_view_permissions_tips"))
                        }
                    )

                    //Изменить владельца
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_change_owner"),
                        content = {
                            Text(StringResources.getString("help_change_owner_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_change_owner_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_change_owner_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_change_owner_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_change_owner_tips"))
                        }
                    )

                    //Атрибуты файла
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_file_attributes"),
                        content = {
                            Text(StringResources.getString("help_file_attributes_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_file_attributes_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_file_attributes_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_file_attributes_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_file_attributes_tips"))
                        }
                    )
                }
            )
        }

        //Раздел 3: Системные утилиты
        item {
            HelpSection(
                title = StringResources.getString("help_section_tools"),
                content = {
                    //Системные инструменты
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_systemtools"),
                        content = {
                            Text(StringResources.getString("help_systemtools_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_systemtools_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_systemtools_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_systemtools_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_systemtools_tips"))
                        }
                    )

                    //Диспетчер задач
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_taskmgr"),
                        content = {
                            Text(StringResources.getString("help_taskmgr_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_taskmgr_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_taskmgr_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_taskmgr_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_taskmgr_tips"))
                        }
                    )

                    //Редактор реестра
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_regedit"),
                        content = {
                            Text(StringResources.getString("help_regedit_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_regedit_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_regedit_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_regedit_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_regedit_tips"))
                        }
                    )

                    //Очистка диска
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_cleanmgr"),
                        content = {
                            Text(StringResources.getString("help_cleanmgr_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_cleanmgr_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_cleanmgr_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_cleanmgr_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_cleanmgr_tips"))
                        }
                    )

                    //Управление дисками
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_diskmgmt"),
                        content = {
                            Text(StringResources.getString("help_diskmgmt_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_diskmgmt_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_diskmgmt_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_diskmgmt_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_diskmgmt_tips"))
                        }
                    )

                    //Службы
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_services"),
                        content = {
                            Text(StringResources.getString("help_services_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_services_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_services_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_services_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_services_tips"))
                        }
                    )

                    //Групповые политики
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_gpp"),
                        content = {
                            Text(StringResources.getString("help_gpp_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_gpp_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_gpp_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_gpp_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_gpp_tips"))
                        }
                    )

                    //Управление компьютером
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_computermanagement1"),
                        content = {
                            Text(StringResources.getString("help_computermanagement1_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_computermanagement1_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_computermanagement1_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_computermanagement1_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_computermanagement1_tips"))
                        }
                    )

                    //Планировщик задач
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_taskscheduler"),
                        content = {
                            Text(StringResources.getString("help_taskscheduler_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_taskscheduler_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_taskscheduler_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_taskscheduler_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_taskscheduler_tips"))
                        }
                    )

                    //Диспетчер устройств
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_devicemgr"),
                        content = {
                            Text(StringResources.getString("help_devicemgr_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_devicemgr_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_devicemgr_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_devicemgr_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_devicemgr_tips"))
                        }
                    )

                    //Брандмауэр
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_firewall"),
                        content = {
                            Text(StringResources.getString("help_firewall_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_firewall_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_firewall_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_firewall_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_firewall_tips"))
                        }
                    )


                    ////Просмотр событий
                    //HelpSubsection(
                    //    title = StringResources.getString("help_subsection_eventvwr"),
                    //    content = {
                    //        Text(StringResources.getString("help_eventvwr_desc"), lineHeight = 24.sp)
                    //        Spacer(modifier = Modifier.height(16.dp))
                    //        Text(
                    //            text = StringResources.getString("help_eventvwr_howto"),
                    //            fontWeight = FontWeight.Bold
                    //        )
                    //        Spacer(modifier = Modifier.height(8.dp))
                    //        Text(StringResources.getString("help_eventvwr_steps"))
                    //        Spacer(modifier = Modifier.height(16.dp))
                    //        Text(
                    //            text = StringResources.getString("help_eventvwr_tips_title"),
                    //            fontWeight = FontWeight.Bold
                    //        )
                    //        Spacer(modifier = Modifier.height(8.dp))
                    //        Text(StringResources.getString("help_eventvwr_tips"))
                    //    }
                    //)
                }
            )
        }

        //Раздел 4: Логирование
        item {
            HelpSection(
                title = StringResources.getString("help_section_logs"),
                content = {
                    //Просмотр логов
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_view_logs"),
                        content = {
                            Text(StringResources.getString("help_view_logs_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_view_logs_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_view_logs_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_view_logs_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_view_logs_tips"))
                        }
                    )

                    //Экспорт логов
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_export_logs"),
                        content = {
                            Text(StringResources.getString("help_export_logs_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_export_logs_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_export_logs_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_export_logs_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_export_logs_tips"))
                        }
                    )
                }
            )
        }

        //Раздел 5: Политика безопасности
        item {
            HelpSection(
                title = StringResources.getString("help_subsection_security_policy"),
                content = {
                    //Политика безопасности
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_security_policy"),
                        content = {
                            Text(StringResources.getString("help_security_policy_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_security_policy_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_security_policy_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_security_policy_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_security_policy_tips"))
                        }
                    )

                    //Анализ подозрительной активности
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_suspicious_activity"),
                        content = {
                            Text(StringResources.getString("help_suspicious_activity_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_suspicious_activity_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_suspicious_activity_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_suspicious_activity_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_suspicious_activity_tips"))
                        }
                    )
                    //Проверка соответствия стандартам
                    HelpSubsection(
                        title = StringResources.getString("help_subsection_compliance_check"),
                        content = {
                            Text(StringResources.getString("help_compliance_check_desc"), lineHeight = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_compliance_check_howto"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_compliance_check_steps"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_compliance_check_tips_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(StringResources.getString("help_compliance_check_tips"))

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = StringResources.getString("help_compliance_check_more_title"),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("CIS Benchmark: https://www.cisecurity.org/cis-benchmarks/")
                            Text("NIST 800-53: https://csrc.nist.gov/publications/detail/sp/800-53/rev-5/final")
                            Text("PCI DSS: https://www.pcisecuritystandards.org/standards/")
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun HelpSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(bottom = 32.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        content()
    }
}

@Composable
private fun HelpSubsection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        content()
    }
}