package com.h4rsh41.paisatracker.ui.tour

import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.h4rsh41.paisatracker.PaisaTrackerApplication
import com.h4rsh41.paisatracker.data.AppTheme
import com.h4rsh41.paisatracker.ui.settings.getThemePreviewColors
import com.h4rsh41.paisatracker.ui.theme.PaisaTrackerTheme
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun AppTourSheetPreview() {
    PaisaTrackerTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AppTourSheet(onComplete = {})
        }
    }
}

data class TourPage(
    val emoji: String,
    val title: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppTourSheet(
    onComplete: () -> Unit,
    onBankAccountAdded: (String, String, String, String, Double) -> Unit = { _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val application = context.applicationContext as PaisaTrackerApplication
    val themePrefsRepo = remember { application.themePreferencesRepository }
    
    var showBankAccountSetup by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf<AppTheme?>(null) }
    
    val pages = listOf(
        TourPage(
            "🚀",
            "Welcome to PaisaTracker",
            "Your simple expense manager with automatic SMS tracking. Let's get started!"
        ),
        TourPage(
            "🎨",
            "Choose Your Style",
            "Pick a theme that matches your personality. You can always change it later in Settings."
        ),
        TourPage(
            "⚡",
            "Add Expenses in Seconds",
            "Tap the lightning button anytime to quickly log an expense. It's that simple!"
        ),
        TourPage(
            "📱",
            "Automatic SMS Detection",
            "We'll detect bank transactions from your SMS and help you track them automatically."
        ),
        TourPage(
            "🔒",
            "Your Data Stays Private",
            "Everything stays on your device. Enable PIN or Biometric lock in Settings for extra security."
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { onComplete() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (pagerState.currentPage == 1) 450.dp else 350.dp)
            ) { pageIndex ->
                val page = pages[pageIndex]
                
                if (pageIndex == 1) {
                    // Theme selection page
                    ThemeSelectionPage(
                        selectedTheme = selectedTheme,
                        onThemeSelected = { theme ->
                            selectedTheme = theme
                            scope.launch {
                                themePrefsRepo.saveTheme(theme)
                            }
                        }
                    )
                } else {
                    // Regular tour page
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.size(140.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = page.emoji,
                                    fontSize = 72.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            text = page.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = page.description,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Page Indicator
            Row(
                modifier = Modifier.height(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { i ->
                    val isSelected = pagerState.currentPage == i
                    val width by androidx.compose.animation.core.animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 10.dp,
                        label = "width"
                    )
                    Box(
                        modifier = Modifier
                            .size(width = width, height = 10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            // Show bank account setup before completing
                            showBankAccountSetup = true
                        }
                    },
                    modifier = Modifier.weight(2f),
                    shape = MaterialTheme.shapes.large,
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage < pages.size - 1) "Next" else "Get Started",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    
    // Bank Account Setup Sheet
    if (showBankAccountSetup) {
        ModalBottomSheet(
            onDismissRequest = { },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            BankAccountSetupSheet(
                onSkip = {
                    showBankAccountSetup = false
                    onComplete()
                },
                onAccountAdded = { name, bankName, last4, type, balance ->
                    onBankAccountAdded(name, bankName, last4, type, balance)
                    showBankAccountSetup = false
                    onComplete()
                }
            )
        }
    }
}


/**
 * Theme Selection Page for App Tour
 * Displays a grid of theme options for users to choose from
 */
@Composable
private fun ThemeSelectionPage(
    selectedTheme: AppTheme?,
    onThemeSelected: (AppTheme) -> Unit
) {
    val showDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val availableThemes = remember(showDynamic) {
        AppTheme.values().filter { it != AppTheme.WALLPAPER_ORIENTED || showDynamic }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Your Theme",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tap to preview and select",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Grid of theme swatches
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(availableThemes) { theme ->
                TourThemeSwatch(
                    theme = theme,
                    isSelected = theme == selectedTheme,
                    onSelect = { onThemeSelected(theme) }
                )
            }
        }
    }
}

/**
 * Compact theme swatch for tour page
 */
@Composable
private fun TourThemeSwatch(
    theme: AppTheme,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val previewColors = getThemePreviewColors(theme)
    
    Column(
        modifier = Modifier
            .width(70.dp)
            .clickable(onClick = onSelect),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Swatch circle with 4-quadrant color split
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(previewColors.getOrElse(0) { Color.Gray }))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(previewColors.getOrElse(2) { Color.LightGray }))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(previewColors.getOrElse(1) { Color.DarkGray }))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(previewColors.getOrElse(3) { Color.White }))
                }
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        
        Text(
            text = theme.themeName.split(" ").first(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
