package com.h4rsh41.paisatracker.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.h4rsh41.paisatracker.data.AnimationPreferencesRepository
import com.h4rsh41.paisatracker.data.AnimationSpeed
import com.h4rsh41.paisatracker.data.AnimationSpeed.Companion.description
import com.h4rsh41.paisatracker.data.AnimationSpeed.Companion.displayName
import com.h4rsh41.paisatracker.data.AnimationSpeed.Companion.fromSliderValue
import com.h4rsh41.paisatracker.data.AnimationSpeed.Companion.toSliderValue
import com.h4rsh41.paisatracker.data.AnimationType
import com.h4rsh41.paisatracker.data.AnimationType.Companion.description
import com.h4rsh41.paisatracker.data.AnimationType.Companion.displayName
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationSettingsBottomSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val animationPrefsRepo = remember { AnimationPreferencesRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    // Collect current preferences
    val currentType by animationPrefsRepo.animationType.collectAsStateWithLifecycle(
        initialValue = AnimationType.default()
    )
    val currentSpeed by animationPrefsRepo.animationSpeed.collectAsStateWithLifecycle(
        initialValue = AnimationSpeed.default()
    )
    val animationsEnabled by animationPrefsRepo.animationsEnabled.collectAsStateWithLifecycle(
        initialValue = true
    )

    // Local state for preview
    var showPreview by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Page Transitions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customize navigation animations",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Enable/Disable Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (animationsEnabled) Icons.Default.Animation else Icons.Default.Block,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Enable Animations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (animationsEnabled) "Smooth transitions" else "Instant navigation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = animationsEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                animationPrefsRepo.setAnimationsEnabled(enabled)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animation Type Section
            AnimatedVisibility(visible = animationsEnabled) {
                Column {
                    Text(
                        text = "Animation Style",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(AnimationType.values().toList()) { type ->
                            AnimationTypeCard(
                                type = type,
                                isSelected = type == currentType,
                                onClick = {
                                    scope.launch {
                                        animationPrefsRepo.saveAnimationType(type)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Speed Slider
                    Text(
                        text = "Animation Speed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = currentSpeed.displayName(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${currentSpeed.durationMs}ms",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = currentSpeed.description(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Slider(
                                value = currentSpeed.toSliderValue().toFloat(),
                                onValueChange = { value ->
                                    scope.launch {
                                        animationPrefsRepo.saveAnimationSpeed(
                                            fromSliderValue(value.toInt())
                                        )
                                    }
                                },
                                valueRange = 0f..3f,
                                steps = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Instant",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Fast",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Normal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Slow",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Preview Button
                    Button(
                        onClick = { showPreview = !showPreview },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Preview Animation")
                    }

                    // Preview Demo
                    AnimatedVisibility(
                        visible = showPreview,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        PreviewDemo(
                            animationType = currentType,
                            animationSpeed = currentSpeed
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reset Button
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                animationPrefsRepo.resetToDefaults()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset to Defaults")
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimationTypeCard(
    type: AnimationType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (type) {
        AnimationType.NONE -> Icons.Default.Block
        AnimationType.SLIDE -> Icons.Default.SwipeRight
        AnimationType.FADE -> Icons.Default.Opacity
        AnimationType.SCALE -> Icons.Default.ZoomIn
        AnimationType.SLIDE_FADE -> Icons.Default.Animation
        AnimationType.ELEVATION -> Icons.Default.Layers
        AnimationType.SHARED_AXIS -> Icons.Default.SwapVert
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column {
                    Text(
                        text = type.displayName(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Text(
                        text = type.description(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PreviewDemo(
    animationType: AnimationType,
    animationSpeed: AnimationSpeed
) {
    var showCard by remember { mutableStateOf(true) }

    LaunchedEffect(animationType, animationSpeed) {
        showCard = false
        kotlinx.coroutines.delay(100)
        showCard = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Compute transition outside composable context
            val enterTransition = remember(animationType, animationSpeed) {
                when (animationType) {
                    AnimationType.NONE -> EnterTransition.None
                    AnimationType.SLIDE -> slideInHorizontally(
                        animationSpec = tween(animationSpeed.durationMs)
                    ) { it }
                    AnimationType.FADE -> fadeIn(
                        animationSpec = tween(animationSpeed.durationMs)
                    )
                    AnimationType.SCALE -> scaleIn(
                        animationSpec = tween(animationSpeed.durationMs)
                    ) + fadeIn(animationSpec = tween(animationSpeed.durationMs))
                    AnimationType.SLIDE_FADE -> slideInHorizontally(
                        animationSpec = tween(animationSpeed.durationMs)
                    ) { it } + fadeIn(animationSpec = tween(animationSpeed.durationMs))
                    AnimationType.ELEVATION -> fadeIn(
                        animationSpec = tween(animationSpeed.durationMs)
                    ) + scaleIn(
                        initialScale = 0.98f,
                        animationSpec = tween(animationSpeed.durationMs)
                    )
                    AnimationType.SHARED_AXIS -> slideInVertically(
                        animationSpec = tween(animationSpeed.durationMs)
                    ) { it / 10 } + fadeIn(
                        animationSpec = tween(animationSpeed.durationMs)
                    ) + scaleIn(
                        initialScale = 0.95f,
                        animationSpec = tween(animationSpeed.durationMs)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showCard,
                    enter = enterTransition,
                    exit = fadeOut(animationSpec = tween(100))
                ) {
                    Card(
                        modifier = Modifier
                            .width(200.dp)
                            .height(80.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sample Screen",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Made with Bob
