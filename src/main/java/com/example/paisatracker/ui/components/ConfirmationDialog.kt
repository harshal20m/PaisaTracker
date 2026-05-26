package com.example.paisatracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Reusable confirmation dialog component
 * Shows a dialog with title, message, and confirm/cancel buttons
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    icon: ImageVector? = Icons.Default.Warning,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            }
        } else null,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = if (isDestructive) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(cancelText)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Convenience function for delete confirmation
 */
@Composable
fun DeleteConfirmationDialog(
    itemName: String,
    itemType: String = "item",
    additionalInfo: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmationDialog(
        title = "Delete $itemType?",
        message = buildString {
            append("Are you sure you want to delete \"$itemName\"?")
            if (additionalInfo != null) {
                append("\n\n$additionalInfo")
            }
            append("\n\nThis action cannot be undone.")
        },
        confirmText = "Delete",
        cancelText = "Cancel",
        isDestructive = true,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Preview
@Composable
private fun ConfirmationDialogPreview() {
    MaterialTheme {
        ConfirmationDialog(
            title = "Confirm Action",
            message = "Are you sure you want to proceed with this action?",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun DeleteConfirmationDialogPreview() {
    MaterialTheme {
        DeleteConfirmationDialog(
            itemName = "Groceries",
            itemType = "category",
            additionalInfo = "This will also delete 15 associated expenses.",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

// Made with Bob
