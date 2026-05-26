package com.example.paisatracker.ui.bankaccount

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.paisatracker.data.AccountType
import com.example.paisatracker.data.BankAccount

/**
 * Bottom sheet for adding a new bank account
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBankAccountSheet(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, bankName: String?, balance: Double, emoji: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.BANK) }
    var bankName by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("0") }
    var selectedEmoji by remember { mutableStateOf("🏦") }
    var selectedColor by remember { mutableStateOf("#2196F3") }

    val isValid = name.isNotBlank() && balance.toDoubleOrNull() != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Text(
                text = "Add Bank Account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Account Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Account Name") },
                placeholder = { Text("e.g., HDFC Savings") },
                leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Account Type Selection
            Text(
                text = "Account Type",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            AccountTypeSelector(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Bank Name (optional)
            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Bank Name (Optional)") },
                placeholder = { Text("e.g., HDFC Bank") },
                leadingIcon = { Icon(Icons.Default.Business, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Initial Balance
            OutlinedTextField(
                value = balance,
                onValueChange = { balance = it },
                label = { Text("Initial Balance") },
                leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Emoji Selector
            Text(
                text = "Icon",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            EmojiSelector(
                selectedEmoji = selectedEmoji,
                onEmojiSelected = { selectedEmoji = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Color Selector
            Text(
                text = "Color",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            ColorSelector(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
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
                        onConfirm(
                            name,
                            selectedType,
                            bankName.ifBlank { null },
                            balance.toDoubleOrNull() ?: 0.0,
                            selectedEmoji,
                            selectedColor
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isValid
                ) {
                    Text("Add Account")
                }
            }
        }
    }
}

/**
 * Bottom sheet for editing an existing bank account
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBankAccountSheet(
    account: BankAccount,
    onDismiss: () -> Unit,
    onConfirm: (BankAccount) -> Unit
) {
    var name by remember { mutableStateOf(account.name) }
    var selectedType by remember { mutableStateOf(account.accountType) }
    var bankName by remember { mutableStateOf(account.bankName ?: "") }
    var balance by remember { mutableStateOf(account.currentBalance.toString()) }
    var selectedEmoji by remember { mutableStateOf(account.emoji) }
    var selectedColor by remember { mutableStateOf(account.colorHex) }

    val isValid = name.isNotBlank() && balance.toDoubleOrNull() != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Text(
                text = "Edit Account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Account Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Account Name") },
                leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Account Type Selection
            Text(
                text = "Account Type",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            AccountTypeSelector(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Bank Name
            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Bank Name (Optional)") },
                leadingIcon = { Icon(Icons.Default.Business, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Current Balance
            OutlinedTextField(
                value = balance,
                onValueChange = { balance = it },
                label = { Text("Current Balance") },
                leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Emoji Selector
            Text(
                text = "Icon",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            EmojiSelector(
                selectedEmoji = selectedEmoji,
                onEmojiSelected = { selectedEmoji = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Color Selector
            Text(
                text = "Color",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            ColorSelector(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
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
                        onConfirm(
                            account.copy(
                                name = name,
                                accountType = selectedType,
                                bankName = bankName.ifBlank { null },
                                currentBalance = balance.toDoubleOrNull() ?: account.currentBalance,
                                emoji = selectedEmoji,
                                colorHex = selectedColor
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isValid
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoneySheet(
    account: BankAccount,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val parsedAmount = amount.toDoubleOrNull()
    val isValid = parsedAmount != null && parsedAmount > 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Add Money",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Credit money into ${account.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(android.graphics.Color.parseColor(account.colorHex)).copy(alpha = 0.2f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = account.emoji,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!account.bankName.isNullOrBlank()) {
                            Text(
                                text = account.bankName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Current balance: ₹${String.format("%.2f", account.currentBalance)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount to add") },
                leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                placeholder = { Text("Salary, refund, cashback, bonus...") },
                leadingIcon = { Icon(Icons.Default.NoteAlt, null) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Use this for salary, refunds, bonus, cashback, or any extra credit received in this account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                    onClick = { onConfirm(parsedAmount ?: 0.0, note.trim()) },
                    modifier = Modifier.weight(1f),
                    enabled = isValid
                ) {
                    Icon(Icons.Default.AddCard, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Money")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSalaryToAccountSheet(
    account: BankAccount,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String, isRecurring: Boolean) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }

    val parsedAmount = amount.toDoubleOrNull()
    val isValid = parsedAmount != null && parsedAmount > 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Salary",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Credit salary directly into ${account.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.18f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(android.graphics.Color.parseColor(account.colorHex)).copy(alpha = 0.2f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = account.emoji,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!account.bankName.isNullOrBlank()) {
                            Text(
                                text = account.bankName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Current balance: ₹${String.format("%.2f", account.currentBalance)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Salary amount") },
                leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") },
                placeholder = { Text("April salary, freelance payment, bonus...") },
                leadingIcon = { Icon(Icons.Default.NoteAlt, null) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isRecurring = !isRecurring },
                shape = RoundedCornerShape(18.dp),
                color = if (isRecurring) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                },
                border = BorderStroke(
                    1.dp,
                    if (isRecurring) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Recurring monthly salary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "When enabled, PaisaTracker will auto-create next month’s salary and credit it to this account on app open.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
                        Text(
                            if (isRecurring) "Recurring enabled for this account" else "One-time salary credit",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isRecurring) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Salary Usage Progress Bar
            val enteredAmount = parsedAmount ?: 0.0
            if (enteredAmount > 0) {
                val currentBalance = account.currentBalance
                val projectedBalance = currentBalance + enteredAmount
                val usagePercentage = if (projectedBalance > 0) {
                    (currentBalance / projectedBalance).toFloat().coerceIn(0f, 1f)
                } else {
                    0f
                }
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Account Balance Preview",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "+${String.format("%.0f", ((enteredAmount / projectedBalance) * 100))}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        
                        LinearProgressIndicator(
                            progress = { usagePercentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.tertiary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Current",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    "₹${String.format("%.2f", currentBalance)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "After Salary",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    "₹${String.format("%.2f", projectedBalance)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }

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
                    onClick = { onConfirm(parsedAmount ?: 0.0, note.trim(), isRecurring) },
                    modifier = Modifier.weight(1f),
                    enabled = isValid
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Salary")
                }
            }
        }
    }
}

/**
 * Bottom sheet for adding a new salary from the Salary Management Card
 * Allows user to select account, enter amount, choose source type, and set recurring
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddSalarySheet(
    accounts: List<BankAccount>,
    onDismiss: () -> Unit,
    onConfirm: (accountId: Long, amount: Double, sourceName: String, sourceType: String, note: String, isRecurring: Boolean) -> Unit
) {
    var selectedAccount by remember { mutableStateOf<BankAccount?>(null) }
    var amount by remember { mutableStateOf("") }
    var sourceName by remember { mutableStateOf("") }
    var selectedSourceType by remember { mutableStateOf(com.example.paisatracker.data.SalarySourceType.PRIMARY) }
    var note by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var showAccountPicker by remember { mutableStateOf(false) }

    val parsedAmount = amount.toDoubleOrNull()
    val isValid = selectedAccount != null && 
                  parsedAmount != null && 
                  parsedAmount > 0 && 
                  sourceName.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Add Salary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Account Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Credit to Account *",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAccountPicker = true },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (selectedAccount != null) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedAccount != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    selectedAccount!!.emoji,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Column {
                                    Text(
                                        selectedAccount!!.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        selectedAccount!!.accountType,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            Text(
                                "Select an account",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Source Name
            OutlinedTextField(
                value = sourceName,
                onValueChange = { sourceName = it },
                label = { Text("Source Name *") },
                placeholder = { Text("e.g., Monthly Salary, Freelance Project") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Work, contentDescription = null)
                }
            )

            // Source Type Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Source Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sourceTypes = listOf(
                        com.example.paisatracker.data.SalarySourceType.PRIMARY to "💼 Primary",
                        com.example.paisatracker.data.SalarySourceType.FREELANCE to "💻 Freelance",
                        com.example.paisatracker.data.SalarySourceType.BONUS to "🎁 Bonus",
                        com.example.paisatracker.data.SalarySourceType.PASSIVE to "📈 Passive",
                        com.example.paisatracker.data.SalarySourceType.OTHER to "📦 Other"
                    )
                    
                    sourceTypes.forEach { (type, label) ->
                        FilterChip(
                            selected = selectedSourceType == type,
                            onClick = { selectedSourceType = type },
                            label = { Text(label) },
                            leadingIcon = if (selectedSourceType == type) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }
            }

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount *") },
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Icon(Icons.Default.CurrencyRupee, contentDescription = null)
                },
                isError = amount.isNotBlank() && parsedAmount == null,
                supportingText = if (amount.isNotBlank() && parsedAmount == null) {
                    { Text("Please enter a valid amount") }
                } else null
            )

            // Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                placeholder = { Text("Add a note about this salary") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null)
                }
            )

            // Recurring Checkbox
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Recurring monthly salary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Auto-create this salary next month and credit to the selected account",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
                    }
                }
            }

            // Preview (if amount entered)
            if (parsedAmount != null && parsedAmount > 0 && selectedAccount != null) {
                val currentBalance = selectedAccount!!.currentBalance
                val projectedBalance = currentBalance + parsedAmount
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Account Balance Preview",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "+${String.format("%.0f", ((parsedAmount / projectedBalance) * 100))}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Current",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    "₹${String.format("%.2f", currentBalance)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "After Salary",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    "₹${String.format("%.2f", projectedBalance)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }

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
                        selectedAccount?.let { account ->
                            onConfirm(
                                account.id,
                                parsedAmount ?: 0.0,
                                sourceName.trim(),
                                selectedSourceType,
                                note.trim(),
                                isRecurring
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isValid
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Salary")
                }
            }
        }
    }

    // Account Picker Dialog
    if (showAccountPicker && accounts.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showAccountPicker = false },
            title = { Text("Select Account") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { account ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAccount = account
                                    showAccountPicker = false
                                },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (selectedAccount?.id == account.id)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    account.emoji,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        account.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "${account.accountType} • ₹${String.format("%.2f", account.currentBalance)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                if (selectedAccount?.id == account.id) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountPicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}

/**
 * Account type selector with chips
 * Uses FlowRow to prevent overflow issues
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val types = listOf(
        AccountType.BANK to "🏦",
        AccountType.CASH to "💵",
        AccountType.CREDIT_CARD to "💳",
        AccountType.DIGITAL_WALLET to "📱"
    )

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        types.forEach { (type, emoji) ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(emoji)
                        Text(
                            type.replace("_", " "),
                            maxLines = 1
                        )
                    }
                }
            )
        }
    }
}

/**
 * Emoji selector for account icon
 */
@Composable
private fun EmojiSelector(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit
) {
    val emojis = listOf("🏦", "💳", "💰", "💵", "💴", "💶", "💷", "🪙", "📱", "🏧")

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(emojis) { emoji ->
            Surface(
                onClick = { onEmojiSelected(emoji) },
                shape = CircleShape,
                color = if (selectedEmoji == emoji) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    }
}

/**
 * Color selector for account theming
 */
@Composable
private fun ColorSelector(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#2196F3", "#4CAF50", "#FF9800", "#F44336",
        "#9C27B0", "#00BCD4", "#FFEB3B", "#795548"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(colors) { colorHex ->
            Surface(
                onClick = { onColorSelected(colorHex) },
                shape = CircleShape,
                color = Color(android.graphics.Color.parseColor(colorHex)),
                modifier = Modifier.size(48.dp),
                border = if (selectedColor == colorHex) 
                    androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                else null
            ) {
                if (selectedColor == colorHex) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Filter sheet for account types
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTypeFilterSheet(
    selectedType: String?,
    onDismiss: () -> Unit,
    onSelectType: (String?) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Filter by Account Type",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // All Accounts
            FilterOption(
                label = "All Accounts",
                icon = Icons.Default.AccountBalance,
                isSelected = selectedType == null,
                onClick = { onSelectType(null) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Account Types
            val types = listOf(
                Triple(AccountType.BANK, "Bank Accounts", Icons.Default.AccountBalance),
                Triple(AccountType.CASH, "Cash", Icons.Default.Money),
                Triple(AccountType.CREDIT_CARD, "Credit Cards", Icons.Default.CreditCard),
                Triple(AccountType.DIGITAL_WALLET, "Digital Wallets", Icons.Default.Wallet)
            )

            types.forEach { (type, label, icon) ->
                FilterOption(
                    label = label,
                    icon = icon,
                    isSelected = selectedType == type,
                    onClick = { onSelectType(type) }
                )
            }
        }
    }
}

@Composable
private fun FilterOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Made with Bob


/**
 * Bottom sheet for editing an existing salary record
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditSalarySheet(
    salary: com.example.paisatracker.data.SalaryRecord,
    accounts: List<BankAccount>,
    onDismiss: () -> Unit,
    onConfirm: (accountId: Long, amount: Double, sourceName: String, sourceType: String, note: String, isRecurring: Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var selectedAccount by remember { 
        mutableStateOf<BankAccount?>(accounts.firstOrNull { it.id == salary.linkedAccountId })
    }
    var amount by remember { mutableStateOf(salary.amount.toString()) }
    var sourceName by remember { mutableStateOf(salary.sourceName) }
    var selectedSourceType by remember { mutableStateOf(salary.sourceType) }
    var note by remember { mutableStateOf(salary.note) }
    var isRecurring by remember { mutableStateOf(salary.isRecurring) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val parsedAmount = amount.toDoubleOrNull()
    val isValid = selectedAccount != null && 
                  parsedAmount != null && 
                  parsedAmount > 0 && 
                  sourceName.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Edit Salary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            // Account Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Credit to Account *",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAccountPicker = true },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (selectedAccount != null) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedAccount != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    selectedAccount!!.emoji,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Column {
                                    Text(
                                        selectedAccount!!.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        selectedAccount!!.accountType,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            Text(
                                "Select an account",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                }
            }

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount *") },
                placeholder = { Text("Enter salary amount") },
                leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = amount.isNotBlank() && parsedAmount == null
            )

            // Source Name
            OutlinedTextField(
                value = sourceName,
                onValueChange = { sourceName = it },
                label = { Text("Source Name *") },
                placeholder = { Text("e.g., Monthly Salary") },
                leadingIcon = { Icon(Icons.Default.Work, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Source Type
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Source Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sourceTypes = listOf(
                        com.example.paisatracker.data.SalarySourceType.PRIMARY to "💼 Primary",
                        com.example.paisatracker.data.SalarySourceType.FREELANCE to "💻 Freelance",
                        com.example.paisatracker.data.SalarySourceType.BONUS to "🎁 Bonus",
                        com.example.paisatracker.data.SalarySourceType.PASSIVE to "📈 Passive",
                        com.example.paisatracker.data.SalarySourceType.OTHER to "📦 Other"
                    )
                    
                    sourceTypes.forEach { (type, label) ->
                        FilterChip(
                            selected = selectedSourceType == type,
                            onClick = { selectedSourceType = type },
                            label = { Text(label) },
                            leadingIcon = if (selectedSourceType == type) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }
            }

            // Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                placeholder = { Text("Add a note") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            // Recurring Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isRecurring = !isRecurring }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Repeat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            "Recurring Salary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Automatically add this salary every month",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it }
                )
            }

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
                        selectedAccount?.let { account ->
                            onConfirm(
                                account.id,
                                parsedAmount ?: 0.0,
                                sourceName.trim(),
                                selectedSourceType,
                                note.trim(),
                                isRecurring
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isValid
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Update")
                }
            }
        }
    }

    // Account Picker Dialog
    if (showAccountPicker && accounts.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showAccountPicker = false },
            title = { Text("Select Account") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { account ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAccount = account
                                    showAccountPicker = false
                                },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (selectedAccount?.id == account.id)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    account.emoji,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Column {
                                    Text(
                                        account.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        account.accountType,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountPicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Salary?") },
            text = { Text("Are you sure you want to delete this salary record? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
