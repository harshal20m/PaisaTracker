package com.h4rsh41.paisatracker.ui.tour

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
import com.h4rsh41.paisatracker.data.AccountType
import com.h4rsh41.paisatracker.util.SupportedBanks

/**
 * Bank Account Setup Sheet for App Tour
 * Allows users to add their bank account during onboarding
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAccountSetupSheet(
    onSkip: () -> Unit,
    onAccountAdded: (
        name: String,
        bankName: String,
        accountNumberLast4: String,
        accountType: String,
        initialBalance: Double
    ) -> Unit
) {
    var accountName by remember { mutableStateOf("") }
    var bankSearchQuery by remember { mutableStateOf("") }
    var selectedBankName by remember { mutableStateOf("") }
    var accountLast4 by remember { mutableStateOf("") }
    var selectedAccountType by remember { mutableStateOf(AccountType.BANK) }
    var initialBalance by remember { mutableStateOf("") }
    var showSkipWarning by remember { mutableStateOf(false) }
    var showBankDropdown by remember { mutableStateOf(false) }
    var showAccountNameHelp by remember { mutableStateOf(false) }
    
    val allBanks = remember { SupportedBanks.getAllBankNames() }
    
    // Filter banks based on search query
    val filteredBanks = remember(bankSearchQuery) {
        if (bankSearchQuery.isBlank()) {
            allBanks.take(10) // Show only first 10 when no search
        } else {
            allBanks.filter {
                it.contains(bankSearchQuery, ignoreCase = true)
            }.take(20) // Show up to 20 matches
        }
    }
    
    // Determine final bank name: use selected if available, otherwise use search query for "Other"
    val finalBankName = selectedBankName.ifBlank { bankSearchQuery }
    
    val isValid = accountName.isNotBlank() &&
                  finalBankName.isNotBlank() &&
                  accountLast4.length == 4 &&
                  initialBalance.toDoubleOrNull() != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "🏦",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Set Up Your Bank Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Track your balance automatically when credits and debits occur",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
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
                supportingText = { Text("A friendly name to identify this account") }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bank Name Autocomplete/Searchable Field
        ExposedDropdownMenuBox(
            expanded = showBankDropdown,
            onExpandedChange = { showBankDropdown = it }
        ) {
            OutlinedTextField(
                value = if (selectedBankName.isNotBlank()) selectedBankName else bankSearchQuery,
                onValueChange = { query ->
                    bankSearchQuery = query
                    selectedBankName = "" // Clear selection when typing
                    showBankDropdown = query.isNotBlank()
                },
                label = { Text("Bank Name") },
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
                        Text("No matches found. Your input will be used as bank name.")
                    } else if (selectedBankName.isBlank() && bankSearchQuery.isBlank()) {
                        Text("Start typing to search from 100+ supported banks")
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
                    
                    // Show "Use custom name" option if there are matches but user wants something else
                    if (bankSearchQuery.isNotBlank() && !filteredBanks.any { it.equals(bankSearchQuery, ignoreCase = true) }) {
                        Divider()
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
        
        // Account Last 4 Digits
        OutlinedTextField(
            value = accountLast4,
            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) accountLast4 = it },
            label = { Text("Account Number (Last 4 digits)") },
            placeholder = { Text("1234") },
            leadingIcon = { Icon(Icons.Default.CreditCard, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            supportingText = { Text("Used to match SMS transactions") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Account Type
        Text(
            text = "Account Type",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedAccountType == AccountType.BANK,
                onClick = { selectedAccountType = AccountType.BANK },
                label = { Text("Savings") },
                leadingIcon = if (selectedAccountType == AccountType.BANK) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null,
                modifier = Modifier.weight(1f)
            )
            
            FilterChip(
                selected = selectedAccountType == AccountType.CREDIT_CARD,
                onClick = { selectedAccountType = AccountType.CREDIT_CARD },
                label = { Text("Credit Card") },
                leadingIcon = if (selectedAccountType == AccountType.CREDIT_CARD) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Initial Balance
        OutlinedTextField(
            value = initialBalance,
            onValueChange = { 
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                    initialBalance = it
                }
            },
            label = { Text("Current Balance") },
            placeholder = { Text("10000.00") },
            leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Add Button
        Button(
            onClick = {
                if (isValid) {
                    onAccountAdded(
                        accountName,
                        finalBankName,
                        accountLast4,
                        selectedAccountType,
                        initialBalance.toDouble()
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isValid,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            Icon(Icons.Default.Add, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add Bank Account", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Skip Button
        TextButton(
            onClick = { showSkipWarning = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Skip for Now")
        }
    }
    
    // Skip Warning Dialog
    if (showSkipWarning) {
        AlertDialog(
            onDismissRequest = { showSkipWarning = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Skip Bank Account Setup?") },
            text = {
                Text(
                    "Without a bank account, automatic credit and debit tracking won't work. " +
                    "You'll need to manually manage all transactions.\n\n" +
                    "You can add accounts later from Settings."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSkipWarning = false
                        onSkip()
                    }
                ) {
                    Text("Skip Anyway")
                }
            },
            dismissButton = {
                Button(onClick = { showSkipWarning = false }) {
                    Text("Go Back")
                }
            }
        )
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