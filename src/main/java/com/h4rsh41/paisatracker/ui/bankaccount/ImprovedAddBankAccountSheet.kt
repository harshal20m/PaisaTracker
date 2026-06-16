package com.h4rsh41.paisatracker.ui.bankaccount

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.h4rsh41.paisatracker.data.AccountPriority
import com.h4rsh41.paisatracker.data.AccountType
import com.h4rsh41.paisatracker.util.SupportedBanks

/**
 * Improved Add Bank Account Sheet matching AppTour design
 * Features:
 * - Bank name autocomplete with 100+ banks
 * - Primary/Secondary priority selector
 * - Clean Material 3 design
 * - Account type chips
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImprovedAddBankAccountSheet(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        type: String,
        bankName: String?,
        balance: Double,
        emoji: String,
        color: String,
        priority: String,
        accountLast4: String?
    ) -> Unit
) {
    var accountName by remember { mutableStateOf("") }
    var bankSearchQuery by remember { mutableStateOf("") }
    var selectedBankName by remember { mutableStateOf("") }
    var accountLast4 by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.BANK) }
    var balance by remember { mutableStateOf("0") }
    var selectedEmoji by remember { mutableStateOf("🏦") }
    var selectedColor by remember { mutableStateOf("#2196F3") }
    var selectedPriority by remember { mutableStateOf(AccountPriority.SECONDARY) }
    var showBankDropdown by remember { mutableStateOf(false) }
    var showAccountNameHelp by remember { mutableStateOf(false) }
    
    val allBanks = remember { SupportedBanks.getAllBankNames() }
    
    // Filter banks based on search query
    val filteredBanks = remember(bankSearchQuery) {
        if (bankSearchQuery.isBlank()) {
            allBanks.take(10)
        } else {
            allBanks.filter {
                it.contains(bankSearchQuery, ignoreCase = true)
            }.take(20)
        }
    }
    
    val finalBankName = selectedBankName.ifBlank { bankSearchQuery }
    val isValid = accountName.isNotBlank() && balance.toDoubleOrNull() != null

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
            // Header with emoji
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏦",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Text(
                    text = "Add Bank Account",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "Track your balance automatically",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Account Name with Help Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Account Name") },
                    placeholder = { Text("e.g., My Savings, Salary Account") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                    trailingIcon = {
                        IconButton(onClick = { showAccountNameHelp = true }) {
                            Icon(
                                Icons.Default.Help,
                                contentDescription = "Help",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("A friendly name to identify this account", fontSize = 11.sp) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bank Name Autocomplete
            ExposedDropdownMenuBox(
                expanded = showBankDropdown,
                onExpandedChange = { showBankDropdown = it }
            ) {
                OutlinedTextField(
                    value = if (selectedBankName.isNotBlank()) selectedBankName else bankSearchQuery,
                    onValueChange = { query ->
                        bankSearchQuery = query
                        selectedBankName = ""
                        showBankDropdown = query.isNotBlank()
                    },
                    label = { Text("Bank Name (Optional)") },
                    placeholder = { Text("Type to search (e.g., HDFC, Axis)") },
                    leadingIcon = { Icon(Icons.Default.Business, null) },
                    trailingIcon = {
                        Row {
                            if (selectedBankName.isNotBlank() || bankSearchQuery.isNotBlank()) {
                                IconButton(onClick = {
                                    bankSearchQuery = ""
                                    selectedBankName = ""
                                    showBankDropdown = false
                                }) {
                                    Icon(Icons.Default.Clear, "Clear", Modifier.size(20.dp))
                                }
                            }
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBankDropdown)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true,
                    supportingText = {
                        if (selectedBankName.isBlank() && bankSearchQuery.isNotBlank() && filteredBanks.isEmpty()) {
                            Text("No matches found. Your input will be used.", fontSize = 11.sp)
                        } else if (selectedBankName.isBlank() && bankSearchQuery.isBlank()) {
                            Text("Search from 100+ supported banks", fontSize = 11.sp)
                        }
                    }
                )
                
                if (filteredBanks.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = showBankDropdown,
                        onDismissRequest = { showBankDropdown = false },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        filteredBanks.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank) },
                                onClick = {
                                    selectedBankName = bank
                                    bankSearchQuery = bank
                                    showBankDropdown = false
                                }
                            )
                        }
                        
                        if (bankSearchQuery.isNotBlank() && !filteredBanks.any { it.equals(bankSearchQuery, ignoreCase = true) }) {
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("Use \"$bankSearchQuery\"", fontWeight = FontWeight.Bold)
                                        Text("Custom bank name", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    selectedBankName = bankSearchQuery
                                    showBankDropdown = false
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, null) }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Account Last 4 Digits (Optional)
            OutlinedTextField(
                value = accountLast4,
                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) accountLast4 = it },
                label = { Text("Account Number (Last 4 digits)") },
                placeholder = { Text("1234") },
                leadingIcon = { Icon(Icons.Default.CreditCard, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("Optional - Used to match SMS transactions", fontSize = 11.sp) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Account Type Selection
            Text(
                text = "Account Type",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == AccountType.BANK,
                    onClick = { selectedType = AccountType.BANK },
                    label = { Text("Savings/Current") },
                    leadingIcon = if (selectedType == AccountType.BANK) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == AccountType.CREDIT_CARD,
                    onClick = { selectedType = AccountType.CREDIT_CARD },
                    label = { Text("Credit Card") },
                    leadingIcon = if (selectedType == AccountType.CREDIT_CARD) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == AccountType.CASH,
                    onClick = { selectedType = AccountType.CASH },
                    label = { Text("Cash") },
                    leadingIcon = if (selectedType == AccountType.CASH) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == AccountType.DIGITAL_WALLET,
                    onClick = { selectedType = AccountType.DIGITAL_WALLET },
                    label = { Text("Digital Wallet") },
                    leadingIcon = if (selectedType == AccountType.DIGITAL_WALLET) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Priority Selection (NEW)
            Text(
                text = "Account Priority",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedPriority == AccountPriority.PRIMARY,
                    onClick = { selectedPriority = AccountPriority.PRIMARY },
                    label = { Text("Primary") },
                    leadingIcon = if (selectedPriority == AccountPriority.PRIMARY) {
                        { Icon(Icons.Default.Star, null, Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedPriority == AccountPriority.SECONDARY,
                    onClick = { selectedPriority = AccountPriority.SECONDARY },
                    label = { Text("Secondary") },
                    leadingIcon = if (selectedPriority == AccountPriority.SECONDARY) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = if (selectedPriority == AccountPriority.PRIMARY) 
                    "Primary accounts are shown first in lists" 
                else 
                    "Secondary accounts for backup or occasional use",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Current Balance
            OutlinedTextField(
                value = balance,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        balance = it
                    }
                },
                label = { Text("Current Balance") },
                placeholder = { Text("10000.00") },
                leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
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
                        if (isValid) {
                            onConfirm(
                                accountName,
                                selectedType,
                                finalBankName.ifBlank { null },
                                balance.toDouble(),
                                selectedEmoji,
                                selectedColor,
                                selectedPriority,
                                accountLast4.ifBlank { null }
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isValid,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add Account", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    
    // Account Name Help Dialog
    if (showAccountNameHelp) {
        AlertDialog(
            onDismissRequest = { showAccountNameHelp = false },
            icon = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("What is Account Name?") },
            text = {
                Column {
                    Text(
                        "Account Name is a friendly label to help you identify this account in the app.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Examples:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("• My Savings Account", style = MaterialTheme.typography.bodySmall)
                    Text("• Salary Account", style = MaterialTheme.typography.bodySmall)
                    Text("• Emergency Fund", style = MaterialTheme.typography.bodySmall)
                    Text("• HDFC Credit Card", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "This name is only for your reference and won't affect SMS matching.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAccountNameHelp = false }) {
                    Text("Got it!")
                }
            }
        )
    }
}

// Made with Bob