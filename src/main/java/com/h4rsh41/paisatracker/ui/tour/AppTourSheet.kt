package com.h4rsh41.paisatracker.ui.tour

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var showBankAccountSetup by remember { mutableStateOf(false) }
    
    val pages = listOf(
        TourPage(
            "🚀",
            "Welcome to PaisaTracker",
            "Your simple expense manager with automatic SMS tracking. Let's get started!"
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
                    .height(350.dp)
            ) { pageIndex ->
                val page = pages[pageIndex]
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
