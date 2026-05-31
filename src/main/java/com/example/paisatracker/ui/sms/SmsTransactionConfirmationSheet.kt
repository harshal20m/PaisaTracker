package com.example.paisatracker.ui.sms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.paisatracker.data.BankNotificationEntity
import com.example.paisatracker.data.Category
import com.example.paisatracker.data.Project
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsTransactionConfirmationSheet(
    transaction : BankNotificationEntity,
    viewModel   : SmsTransactionViewModel,
    onDismiss   : () -> Unit,
    onConfirm   : (categoryId: Long?, projectId: Long?) -> Unit
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val projects   by viewModel.projects.collectAsStateWithLifecycle()

    var selectedCategory   by remember { mutableStateOf<Category?>(null) }
    var selectedProject    by remember { mutableStateOf<Project?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showProjectPicker  by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.colorScheme.surface,
        dragHandle       = {
            Box(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text       = "Confirm Transaction",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text     = "Review details before adding to expenses",
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            // Transaction detail card
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SheetDetailRow(
                        icon       = Icons.Default.AttachMoney,
                        label      = "Amount",
                        value      = "₹${String.format("%.2f", transaction.amount ?: 0.0)}",
                        valueColor = MaterialTheme.colorScheme.error
                    )
                    RowDivider()
                    SheetDetailRow(
                        icon  = Icons.Default.Store,
                        label = "Merchant",
                        value = transaction.merchant ?: "Unknown"
                    )
                    RowDivider()
                    SheetDetailRow(
                        icon  = Icons.Default.AccountBalance,
                        label = "Bank",
                        value = transaction.bankName ?: "Unknown"
                    )
                    RowDivider()
                    SheetDetailRow(
                        icon  = Icons.Default.CalendarToday,
                        label = "Date",
                        value = transaction.postedAt.format(
                            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        )
                    )
                    if (transaction.accountLast4 != null) {
                        RowDivider()
                        SheetDetailRow(
                            icon  = Icons.Default.CreditCard,
                            label = "Account",
                            value = "••••${transaction.accountLast4}"
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Category
            PickerSectionLabel("Category")
            Spacer(Modifier.height(8.dp))
            PickerSelectorCard(
                icon     = Icons.Default.Category,
                label    = selectedCategory?.name ?: "Select Category",
                emoji    = selectedCategory?.emoji,
                selected = selectedCategory != null,
                onClick  = { showCategoryPicker = true }
            )

            Spacer(Modifier.height(14.dp))

            // Project
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PickerSectionLabel("Project")
                Text(
                    text     = "— optional",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.height(8.dp))
            PickerSelectorCard(
                icon     = Icons.Default.Folder,
                label    = selectedProject?.name ?: "Select Project",
                emoji    = selectedProject?.emoji,
                selected = selectedProject != null,
                onClick  = { showProjectPicker = true }
            )

            Spacer(Modifier.height(24.dp))

            // Actions
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick  = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick  = { onConfirm(selectedCategory?.id, selectedProject?.id) },
                    modifier = Modifier.weight(2f),
                    shape    = RoundedCornerShape(12.dp),
                    enabled  = selectedCategory != null,
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onPrimary,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Confirm",
                        color      = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showCategoryPicker) {
        DarkPickerDialog(
            title     = "Select Category",
            items     = categories,
            onDismiss = { showCategoryPicker = false },
            itemContent = { category ->
                PickerDialogRow(
                    emoji    = category.emoji,
                    name     = category.name,
                    selected = selectedCategory?.id == category.id,
                    onClick  = { selectedCategory = category; showCategoryPicker = false }
                )
            }
        )
    }

    if (showProjectPicker) {
        DarkPickerDialog(
            title     = "Select Project",
            items     = projects,
            onDismiss = { showProjectPicker = false },
            itemContent = { project ->
                PickerDialogRow(
                    emoji    = project.emoji,
                    name     = project.name,
                    selected = selectedProject?.id == project.id,
                    onClick  = { selectedProject = project; showProjectPicker = false }
                )
            }
        )
    }
}

@Composable
private fun SheetDetailRow(
    icon       : ImageVector,
    label      : String,
    value      : String,
    valueColor : androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(17.dp)
            )
            Text(
                text     = label,
                fontSize = 12.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text       = value,
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color      = valueColor
        )
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(horizontal = 14.dp),
        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 0.5.dp
    )
}

@Composable
private fun PickerSectionLabel(text: String) {
    Text(
        text          = text.uppercase(),
        fontSize      = 11.sp,
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        color         = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
private fun PickerSelectorCard(
    icon     : ImageVector,
    label    : String,
    emoji    : String?,
    selected : Boolean,
    onClick  : () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (emoji != null) {
            Text(emoji, fontSize = 26.sp)
        } else {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text     = label,
            fontSize = 14.sp,
            color    = if (selected) MaterialTheme.colorScheme.onSurface
                       else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DarkPickerDialog(
    title       : String,
    items       : List<T>,
    onDismiss   : () -> Unit,
    itemContent : @Composable (T) -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape  = RoundedCornerShape(20.dp),
            color  = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text       = title,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.5.dp
                )
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(items) { item -> itemContent(item) }
                }
            }
        }
    }
}

@Composable
private fun PickerDialogRow(
    emoji    : String,
    name     : String,
    selected : Boolean,
    onClick  : () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else androidx.compose.ui.graphics.Color.Transparent
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, fontSize = 24.sp)
        Text(
            text     = name,
            fontSize = 14.sp,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}