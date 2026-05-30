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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.paisatracker.data.BankNotificationEntity
import com.example.paisatracker.data.Category
import com.example.paisatracker.data.Project
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsTransactionConfirmationSheet(
    transaction: BankNotificationEntity,
    viewModel: SmsTransactionViewModel,
    onDismiss: () -> Unit,
    onConfirm: (categoryId: Long?, projectId: Long?) -> Unit
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showProjectPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = "Confirm Transaction",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Review and edit transaction details before confirming",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Transaction Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Amount
                    DetailRow(
                        icon = Icons.Default.AttachMoney,
                        label = "Amount",
                        value = "₹${String.format("%.2f", transaction.amount ?: 0.0)}",
                        valueColor = MaterialTheme.colorScheme.error
                    )

                    HorizontalDivider()

                    // Merchant
                    DetailRow(
                        icon = Icons.Default.Store,
                        label = "Merchant",
                        value = transaction.merchant ?: "Unknown"
                    )

                    HorizontalDivider()

                    // Bank
                    DetailRow(
                        icon = Icons.Default.AccountBalance,
                        label = "Bank",
                        value = transaction.bankName ?: "Unknown"
                    )

                    HorizontalDivider()

                    // Date
                    DetailRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Date",
                        value = transaction.postedAt.format(
                            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        )
                    )

                    if (transaction.accountLast4 != null) {
                        HorizontalDivider()
                        DetailRow(
                            icon = Icons.Default.CreditCard,
                            label = "Account",
                            value = "****${transaction.accountLast4}"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category Selection
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SelectionCard(
                icon = Icons.Default.Category,
                label = selectedCategory?.name ?: "Select Category",
                emoji = selectedCategory?.emoji,
                onClick = { showCategoryPicker = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Project Selection
            Text(
                text = "Project (Optional)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SelectionCard(
                icon = Icons.Default.Folder,
                label = selectedProject?.name ?: "Select Project",
                emoji = selectedProject?.emoji,
                onClick = { showProjectPicker = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        onConfirm(selectedCategory?.id, selectedProject?.id)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = selectedCategory != null
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm")
                }
            }
        }
    }

    // Category Picker Dialog
    if (showCategoryPicker) {
        PickerDialog(
            title = "Select Category",
            items = categories,
            onDismiss = { showCategoryPicker = false },
            onSelect = { category ->
                selectedCategory = category
                showCategoryPicker = false
            },
            itemContent = { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedCategory = category
                            showCategoryPicker = false
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = category.emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (selectedCategory?.id == category.id) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )
    }

    // Project Picker Dialog
    if (showProjectPicker) {
        PickerDialog(
            title = "Select Project",
            items = projects,
            onDismiss = { showProjectPicker = false },
            onSelect = { project ->
                selectedProject = project
                showProjectPicker = false
            },
            itemContent = { project ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedProject = project
                            showProjectPicker = false
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = project.emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (selectedProject?.id == project.id) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun SelectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    emoji: String?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (emoji != null) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineMedium
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> PickerDialog(
    title: String,
    items: List<T>,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
    itemContent: @Composable (T) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(items) { item ->
                        itemContent(item)
                    }
                }
            }
        }
    }
}

// Made with Bob
