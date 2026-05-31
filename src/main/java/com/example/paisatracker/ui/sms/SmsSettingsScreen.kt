package com.example.paisatracker.ui.sms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun SmsSettingsScreen(
    navController : NavHostController,
    viewModel     : SmsTransactionViewModel = viewModel()
) {
    val autoCreate   by viewModel.autoCreateExpenses.collectAsStateWithLifecycle(initialValue = false)
    val showNotifs   by viewModel.showNotifications.collectAsStateWithLifecycle(initialValue = true)
    val vibrate      by viewModel.vibrateOnDetection.collectAsStateWithLifecycle(initialValue = false)
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle(initialValue = 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SettingsHeader(onBackClick = { navController.popBackStack() })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = "SMS Detection Active",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = "PaisaTracker reads bank SMSes, extracts amounts & merchants, and creates expenses automatically or queues them for review.",
                        fontSize   = 12.sp,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            // Pending alert (only when there are pending transactions)
            if (pendingCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                        .clickable { navController.navigate("pending_sms") }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.error,
                            modifier           = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text       = "$pendingCount Pending Transaction${if (pendingCount != 1) "s" else ""}",
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text     = "Tap to review now",
                                fontSize = 11.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Automation section
            SectionLabel("Automation")
            SettingsGroup {
                SettingToggleRow(
                    icon        = Icons.Default.AutoAwesome,
                    title       = "Auto-Create Expenses",
                    description = if (autoCreate)
                        "Expenses are created automatically from SMS"
                    else
                        "Review and confirm each transaction manually",
                    checked     = autoCreate,
                    onChecked   = { viewModel.setAutoCreateExpenses(it) }
                )
            }

            // Auto-Create Configuration section (only show when auto-create is enabled)
            if (autoCreate) {
                SectionLabel("Auto-Create Configuration")
                SettingsGroup {
                    SettingNavigationRow(
                        icon        = Icons.Default.Settings,
                        title       = "Default Category & Project",
                        description = "Configure defaults for auto-created expenses",
                        onClick     = { navController.navigate("auto_create_config") }
                    )
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 16.dp),
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.5.dp
                    )
                    SettingNavigationRow(
                        icon        = Icons.Default.Rule,
                        title       = "Merchant Rules",
                        description = "Set category rules for specific merchants",
                        onClick     = { navController.navigate("merchant_rules") }
                    )
                }
            }

            // Notifications section
            SectionLabel("Notifications")
            SettingsGroup {
                SettingToggleRow(
                    icon        = Icons.Default.Notifications,
                    title       = "Show Notifications",
                    description = "Alert when transactions are detected",
                    checked     = showNotifs,
                    onChecked   = { viewModel.setShowNotifications(it) }
                )
                HorizontalDivider(
                    modifier  = Modifier.padding(horizontal = 16.dp),
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.5.dp
                )
                SettingToggleRow(
                    icon        = Icons.Default.Vibration,
                    title       = "Vibrate on Detection",
                    description = "Haptic feedback on new transaction SMS",
                    checked     = vibrate,
                    onChecked   = { viewModel.setVibrateOnDetection(it) }
                )
            }

            // How it works card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Help,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(20.dp)
                        )
                        Text(
                            text       = "How it works",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    listOf(
                        "Reads bank transaction SMS in real time",
                        "Extracts amount, merchant, and bank details",
                        "Creates expenses automatically or queues for review",
                        "Supports 50+ banks across multiple countries"
                    ).forEach { step ->
                        Row(
                            verticalAlignment     = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(5.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text       = step,
                                fontSize   = 12.sp,
                                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        IconButton(
            onClick  = onBackClick,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text          = "CONFIGURE",
            fontSize      = 11.sp,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 1.6.sp,
            color         = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text       = "SMS Settings",
            fontSize   = 28.sp,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text     = "Auto expense detection",
            fontSize = 13.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text          = text.uppercase(),
        fontSize      = 11.sp,
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        color         = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingToggleRow(
    icon        : ImageVector,
    title       : String,
    description : String,
    checked     : Boolean,
    onChecked   : (Boolean) -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            modifier              = Modifier.weight(1f),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = title,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text       = description,
                    fontSize   = 11.sp,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked         = checked,
            onCheckedChange = onChecked,
            colors          = SwitchDefaults.colors(
                checkedThumbColor    = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor    = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor  = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
private fun SettingNavigationRow(
    icon        : ImageVector,
    title       : String,
    description : String,
    onClick     : () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            modifier              = Modifier.weight(1f),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = title,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text       = description,
                    fontSize   = 11.sp,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(20.dp)
        )
    }
}