package com.example.paisatracker.ui.applock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.paisatracker.data.AppLockPreferences
import com.example.paisatracker.PaisaTrackerViewModel
import com.example.paisatracker.ui.common.ToastType
import com.example.paisatracker.util.BiometricHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockSettingsSheet(
    viewModel: PaisaTrackerViewModel,
    appLockPrefs: AppLockPreferences,
    isAppLockEnabled: Boolean,
    isBiometricEnabled: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showChangePinSheet by remember { mutableStateOf(false) }
    var localAppLockEnabled by remember { mutableStateOf(isAppLockEnabled) }
    var localBiometricEnabled by remember { mutableStateOf(isBiometricEnabled) }

    // Check if biometric is available
    val isBiometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    "App Lock Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Enable/Disable App Lock
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "App Lock",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Require unlock on app launch",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = localAppLockEnabled,
                        onCheckedChange = {
                            localAppLockEnabled = it
                            scope.launch {
                                appLockPrefs.setAppLockEnabled(it)
                                if (!it) {
                                    // If app lock disabled, also disable biometric
                                    appLockPrefs.setBiometricEnabled(false)
                                    localBiometricEnabled = false
                                    viewModel.showToast("App Lock disabled", ToastType.INFO)
                                } else {
                                    viewModel.showToast("App Lock enabled", ToastType.SUCCESS)
                                }
                            }
                        }
                    )
                }
            }

            // Biometric Toggle (only if app lock enabled AND biometric available)
            if (localAppLockEnabled) {
                if (isBiometricAvailable) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        "Biometric Unlock",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Use fingerprint/face",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = localBiometricEnabled,
                                onCheckedChange = {
                                    localBiometricEnabled = it
                                    scope.launch {
                                        appLockPrefs.setBiometricEnabled(it)
                                        viewModel.showToast(
                                            if (it) "Biometric enabled" else "Biometric disabled",
                                            ToastType.INFO
                                        )
                                    }
                                }
                            )
                        }
                    }
                } else {
                    // Biometric not available message
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Biometric authentication not available on this device",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Change PIN Button
            if (localAppLockEnabled) {
                Button(
                    onClick = { showChangePinSheet = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        Icons.Default.VpnKey,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Change PIN", fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "💡 Tip: App lock will activate when you open the app",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Change PIN Sheet
    if (showChangePinSheet) {
        SetupPinSheet(
            onDismiss = { showChangePinSheet = false },
            onPinSet = { newPin ->
                scope.launch {
                    appLockPrefs.setPinCode(newPin)
                    viewModel.showToast("PIN changed successfully", ToastType.SUCCESS)
                    showChangePinSheet = false
                }
            }
        )
    }
}
