package com.example.paisatracker.ui.sms

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Compact card for HomeScreen that prompts users to scan SMS history
 * Shows only when user hasn't scanned recently or never scanned
 */
@Composable
fun HistoryScanCard(
    viewModel: SmsHistoryScanViewModel = viewModel(),
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lastScanDate by viewModel.lastScanDate.collectAsStateWithLifecycle()
    val shouldPrompt = viewModel.shouldPromptScan()
    
    // Only show if user should be prompted
    AnimatedVisibility(
        visible = shouldPrompt,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onScanClick),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Animated Icon
                    AnimatedScanIcon()
                    
                    // Content
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (lastScanDate == null) "📱 Import Old Expenses" else "🔄 Scan Again",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = if (lastScanDate == null) {
                                "Scan your SMS inbox to import past bank transactions"
                            } else {
                                val daysSince = ChronoUnit.DAYS.between(lastScanDate, LocalDate.now())
                                "Last scanned ${daysSince} days ago"
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                    
                    // Arrow Icon
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Scan",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Animated scanning icon with pulsing effect
 */
@Composable
private fun AnimatedScanIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    alpha = alpha
                ),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Minimal version of the card for smaller spaces
 */
@Composable
fun HistoryScanCompactCard(
    viewModel: SmsHistoryScanViewModel = viewModel(),
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldPrompt = viewModel.shouldPromptScan()
    
    AnimatedVisibility(
        visible = shouldPrompt,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        OutlinedCard(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onScanClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Scan Old Messages",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Import past transactions",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Banner version for prominent placement
 */
@Composable
fun HistoryScanBanner(
    viewModel: SmsHistoryScanViewModel = viewModel(),
    onScanClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lastScanDate by viewModel.lastScanDate.collectAsStateWithLifecycle()
    val shouldPrompt = viewModel.shouldPromptScan()
    
    AnimatedVisibility(
        visible = shouldPrompt,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "New Feature!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Import SMS History",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Text(
                    text = "Scan your SMS inbox to automatically import past bank transactions and track your historical expenses.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                
                Button(
                    onClick = onScanClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Scanning", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// Made with Bob