package com.example.paisatracker.ui.expense

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.paisatracker.domain.models.groupByYearAndMonth
import com.example.paisatracker.domain.models.YearGroup
import com.example.paisatracker.domain.models.MonthGroup
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.paisatracker.PaisaTrackerViewModel
import com.example.paisatracker.ui.common.HeaderActionButton
import com.example.paisatracker.ui.common.ScreenHeader
import com.example.paisatracker.ui.common.DeleteConfirmationSheetContent
import com.example.paisatracker.ui.common.DatePickerSheet
import com.example.paisatracker.ui.common.ToastType
import com.example.paisatracker.R
import com.example.paisatracker.data.Expense
import com.example.paisatracker.ui.common.SortDropdown
import com.example.paisatracker.ui.common.SortOption
import com.example.paisatracker.util.formatCurrency
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun debugLog(context: Context, msg: String) {
    Log.d("PT_DEBUG", msg)
}

@DrawableRes
fun paymentIconRes(key: String?): Int? = when (key) {
    "UPI" -> R.drawable.ic_upi_payment_icon
    "PhonePe" -> R.drawable.ic_phonepe_icon
    "GPay" -> R.drawable.ic_google_pay_icon
    "Paytm" -> R.drawable.ic_paytm_icon
    "Cash" -> R.drawable.ic_cash_icon
    "Card" -> R.drawable.ic_card_icon
    else -> null
}

sealed class SheetState {
    object Add : SheetState()
    data class Edit(val expense: Expense) : SheetState()
    data class Delete(val expense: Expense) : SheetState()
}

enum class ExpenseViewType {
    GRID, LIST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    viewModel: PaisaTrackerViewModel,
    categoryId: Long,
    navController: NavController
) {
    val category by viewModel.getCategoryById(categoryId).collectAsStateWithLifecycle(initialValue = null)
    val expenses by viewModel.getExpensesForCategory(categoryId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // sort state
    var expenseSortOption by remember { mutableStateOf(SortOption.DATE_NEW_OLD) }
    var currentViewType by remember { mutableStateOf(ExpenseViewType.GRID) }
    
    // Grouping state
    var enableGrouping by remember { mutableStateOf(true) }
    var loadedYears by remember { mutableStateOf(setOf<Int>()) }
    var isLoadingYear by remember { mutableStateOf(false) }

    var currentSheet by remember { mutableStateOf<SheetState?>(null) }

    var newExpenseAmount by remember { mutableStateOf("") }
    var newExpenseDescription by remember { mutableStateOf("") }
    var newExpenseDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var newPaymentMethod by remember { mutableStateOf<String?>(null) }

    var editedExpenseAmount by remember { mutableStateOf("") }
    var editedExpenseDescription by remember { mutableStateOf("") }
    var editedExpenseDate by remember { mutableStateOf(0L) }
    var editedPaymentMethod by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var newExpenseImageUri by remember { mutableStateOf<Uri?>(null) }
    var editedExpenseImageUri by remember { mutableStateOf<Uri?>(null) }

    // ======= Gallery =======
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        debugLog(context, "Gallery result uri=$uri, sheet=$currentSheet")
        uri ?: return@rememberLauncherForActivityResult
        when (currentSheet) {
            is SheetState.Add -> newExpenseImageUri = uri
            is SheetState.Edit -> editedExpenseImageUri = uri
            else -> {}
        }
    }

    // ======= Camera =======
    val cameraUri = remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        debugLog(context, "Camera callback success=$success, uri=${cameraUri.value}")
        if (success) {
            cameraUri.value?.let { uri ->
                when (currentSheet) {
                    is SheetState.Add -> {
                        newExpenseImageUri = uri
                        debugLog(context, "Camera ADD uri set: $uri")
                    }
                    is SheetState.Edit -> {
                        editedExpenseImageUri = uri
                        debugLog(context, "Camera EDIT uri set: $uri")
                    }
                    else -> {}
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val imagesDir = File(context.filesDir, "images")
                if (!imagesDir.exists()) imagesDir.mkdirs()

                val photoFile = File(
                    imagesDir,
                    "IMG_${System.currentTimeMillis()}.jpg"
                )

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                cameraUri.value = uri
                debugLog(context, "Camera uri created: $uri")
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                viewModel.showToast("Camera error: ${e.localizedMessage}", ToastType.ERROR)
            }
        } else {
            viewModel.showToast("Camera permission denied", ToastType.INFO)
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.18f),
            MaterialTheme.colorScheme.background
        )
    )

    val sortedExpenses = remember(expenses, expenseSortOption) {
        when (expenseSortOption) {
            SortOption.AMOUNT_LOW_HIGH -> expenses.sortedBy { it.amount }
            SortOption.AMOUNT_HIGH_LOW -> expenses.sortedByDescending { it.amount }
            SortOption.NAME_A_Z -> expenses.sortedBy { it.description.lowercase() }
            SortOption.NAME_Z_A -> expenses.sortedByDescending { it.description.lowercase() }
            SortOption.DATE_OLD_NEW -> expenses.sortedBy { it.date }
            SortOption.DATE_NEW_OLD -> expenses.sortedByDescending { it.date }
        }
    }
    
    // Group expenses by year and month
    val groupedExpenses = remember(sortedExpenses, enableGrouping) {
        if (enableGrouping && expenseSortOption == SortOption.DATE_NEW_OLD) {
            sortedExpenses.groupByYearAndMonth()
        } else {
            emptyList()
        }
    }
    
    // Get current year
    val currentYear = remember {
        java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    }
    
    // Initialize with current year loaded
    LaunchedEffect(Unit) {
        loadedYears = setOf(currentYear)
    }
    
    // Filter to show only loaded years
    val visibleYearGroups = remember(groupedExpenses, loadedYears) {
        groupedExpenses.filter { it.year in loadedYears }
    }
    
    // Get next year to load (oldest year - 1)
    val nextYearToLoad = remember(groupedExpenses, loadedYears) {
        val allYears = groupedExpenses.map { it.year }.toSet()
        val unloadedYears = allYears - loadedYears
        unloadedYears.minOrNull()
    }

    // Include a null item at the start to represent the "Add New" card
    val listItems = remember(sortedExpenses) {
        listOf<Expense?>(null) + sortedExpenses
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val onAddNewExpenseClick = {
        newExpenseDate = System.currentTimeMillis()
        newPaymentMethod = null
        newExpenseAmount = ""
        newExpenseDescription = ""
        newExpenseImageUri = null
        currentSheet = SheetState.Add
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            ScreenHeader(
                title = category?.name ?: "Expenses",
                subtitle = "Transaction list",
                onBackClick = { navController.popBackStack() },
                action = {
                    HeaderActionButton(
                        icon = Icons.Default.Add,
                        onClick = onAddNewExpenseClick,
                        contentDescription = "Add Expense"
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(paddingValues)
        ) {
            Column {
                ExpenseSummaryHeader(expenses = expenses)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExpenseViewTypeToggle(
                        currentViewType = currentViewType,
                        onViewTypeChange = { currentViewType = it }
                    )

                    SortDropdown(current = expenseSortOption, onChange = { expenseSortOption = it })
                }

                when (currentViewType) {
                    ExpenseViewType.LIST -> {
                        if (enableGrouping && expenseSortOption == SortOption.DATE_NEW_OLD && visibleYearGroups.isNotEmpty()) {
                            // Grouped view
                            GroupedExpenseListView(
                                yearGroups = visibleYearGroups,
                                nextYearToLoad = nextYearToLoad,
                                isLoadingYear = isLoadingYear,
                                currencySymbol = category?.emoji ?: "₹",
                                onAddExpenseClick = onAddNewExpenseClick,
                                onExpenseClick = { navController.navigate("expense_details/${it.id}") },
                                onEditClick = { expense ->
                                    expenseToEditPrep(
                                        expense,
                                        onSetAmount = { editedExpenseAmount = it },
                                        onSetDesc = { editedExpenseDescription = it },
                                        onSetDate = { editedExpenseDate = it },
                                        onSetMethod = { editedPaymentMethod = it }
                                    )
                                    editedExpenseImageUri = null
                                    currentSheet = SheetState.Edit(expense)
                                },
                                onDeleteClick = { currentSheet = SheetState.Delete(it) },
                                onLoadMoreClick = {
                                    nextYearToLoad?.let { year ->
                                        isLoadingYear = true
                                        scope.launch {
                                            loadedYears = loadedYears + year
                                            isLoadingYear = false
                                        }
                                    }
                                }
                            )
                        } else {
                            // Original flat list view
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(listItems, key = { it?.id ?: "ADD" }) { expense ->
                                    if (expense == null) {
                                        AddExpenseListItem(onClick = onAddNewExpenseClick)
                                    } else {
                                        ExpenseListItem(
                                            expense = expense,
                                            onClick = { navController.navigate("expense_details/${expense.id}") },
                                            onEditClick = {
                                                expenseToEditPrep(
                                                    expense,
                                                    onSetAmount = { editedExpenseAmount = it },
                                                    onSetDesc = { editedExpenseDescription = it },
                                                    onSetDate = { editedExpenseDate = it },
                                                    onSetMethod = { editedPaymentMethod = it }
                                                )
                                                editedExpenseImageUri = null
                                                currentSheet = SheetState.Edit(expense)
                                            },
                                            onDeleteClick = { currentSheet = SheetState.Delete(expense) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ExpenseViewType.GRID -> {
                        if (enableGrouping && expenseSortOption == SortOption.DATE_NEW_OLD && visibleYearGroups.isNotEmpty()) {
                            // Grouped grid view
                            GroupedExpenseGridView(
                                yearGroups = visibleYearGroups,
                                nextYearToLoad = nextYearToLoad,
                                isLoadingYear = isLoadingYear,
                                currencySymbol = category?.emoji ?: "₹",
                                onAddExpenseClick = onAddNewExpenseClick,
                                onExpenseClick = { navController.navigate("expense_details/${it.id}") },
                                onEditClick = { expense ->
                                    expenseToEditPrep(
                                        expense,
                                        onSetAmount = { editedExpenseAmount = it },
                                        onSetDesc = { editedExpenseDescription = it },
                                        onSetDate = { editedExpenseDate = it },
                                        onSetMethod = { editedPaymentMethod = it }
                                    )
                                    editedExpenseImageUri = null
                                    currentSheet = SheetState.Edit(expense)
                                },
                                onDeleteClick = { currentSheet = SheetState.Delete(it) },
                                onLoadMoreClick = {
                                    nextYearToLoad?.let { year ->
                                        isLoadingYear = true
                                        scope.launch {
                                            loadedYears = loadedYears + year
                                            isLoadingYear = false
                                        }
                                    }
                                }
                            )
                        } else {
                            // Original flat grid view
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 180.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = listItems.chunked(2)
                                ) { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowItems.forEach { expense ->
                                            if (expense == null) {
                                                AddExpenseGridItem(
                                                    onClick = onAddNewExpenseClick,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            } else {
                                                ExpenseGridItem(
                                                    expense = expense,
                                                    onClick = { navController.navigate("expense_details/${expense.id}") },
                                                    onEditClick = {
                                                        expenseToEditPrep(
                                                            expense,
                                                            onSetAmount = { editedExpenseAmount = it },
                                                            onSetDesc = { editedExpenseDescription = it },
                                                            onSetDate = { editedExpenseDate = it },
                                                            onSetMethod = { editedPaymentMethod = it }
                                                        )
                                                        editedExpenseImageUri = null
                                                        currentSheet = SheetState.Edit(expense)
                                                    },
                                                    onDeleteClick = { currentSheet = SheetState.Delete(expense) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }
                }

            // Show ModalBottomSheet when currentSheet != null
            currentSheet?.let { sheet ->
                ModalBottomSheet(onDismissRequest = { currentSheet = null }, sheetState = sheetState, tonalElevation = 10.dp) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp), contentAlignment = Alignment.TopCenter) {
                        Box(modifier = Modifier
                            .width(48.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)))
                    }

                    when (sheet) {
                        is SheetState.Add -> {
                            ExpenseBottomSheetContent(
                                title = "New Expense",
                                amount = newExpenseAmount,
                                onAmountChange = { newExpenseAmount = it },
                                description = newExpenseDescription,
                                onDescriptionChange = { newExpenseDescription = it },
                                date = newExpenseDate,
                                onDateChange = { newExpenseDate = it },
                                paymentMethod = newPaymentMethod,
                                onPaymentMethodChange = { newPaymentMethod = it },
                                onPickFromGallery = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                onPickFromCamera = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                previewImageUri = newExpenseImageUri,
                                onConfirm = {
                                    val amount = newExpenseAmount.toDoubleOrNull()
                                    if (newExpenseDescription.isNotBlank() && amount != null) {
                                        val method = newPaymentMethod?.takeIf { it.isNotBlank() }
                                        val expense = Expense(
                                            amount = amount,
                                            description = newExpenseDescription,
                                            date = newExpenseDate,
                                            categoryId = categoryId,
                                            paymentMethod = method,
                                            paymentIcon = method.toPaymentIconKey(),
                                            assetPath = null
                                        )

                                        val pickedUri = newExpenseImageUri
                                        val pickedTitle = newExpenseDescription

                                        viewModel.insertExpenseWithResult(expense) { newExpenseId ->
                                            pickedUri?.let { uri ->
                                                val assetTitle = pickedTitle.ifBlank { "Expense #$newExpenseId" }
                                                viewModel.addLinkedAsset(context = context, uri = uri, title = assetTitle, description = "", expenseId = newExpenseId)
                                            }
                                        }

                                        newExpenseAmount = ""
                                        newExpenseDescription = ""
                                        newPaymentMethod = null
                                        newExpenseImageUri = null
                                        currentSheet = null
                                    } else {
                                        viewModel.showToast("Enter valid description & amount", ToastType.ERROR)
                                    }
                                },
                                onDismiss = { currentSheet = null }
                            )
                        }

                        is SheetState.Edit -> {
                            ExpenseBottomSheetContent(
                                title = "Edit Expense",
                                amount = editedExpenseAmount,
                                onAmountChange = { editedExpenseAmount = it },
                                description = editedExpenseDescription,
                                onDescriptionChange = { editedExpenseDescription = it },
                                date = editedExpenseDate,
                                onDateChange = { editedExpenseDate = it },
                                paymentMethod = editedPaymentMethod,
                                onPaymentMethodChange = { editedPaymentMethod = it },
                                onPickFromGallery = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                onPickFromCamera = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                previewImageUri = editedExpenseImageUri,
                                onConfirm = {
                                    val amount = editedExpenseAmount.toDoubleOrNull()
                                    if (editedExpenseDescription.isNotBlank() && amount != null) {
                                        val method = editedPaymentMethod?.takeIf { it.isNotBlank() }
                                        val pickedUri = editedExpenseImageUri
                                        val pickedTitle = editedExpenseDescription

                                        val old = sheet.expense
                                        viewModel.updateExpense(old.copy(
                                            amount = amount,
                                            description = editedExpenseDescription,
                                            date = editedExpenseDate,
                                            paymentMethod = method,
                                            paymentIcon = method.toPaymentIconKey()
                                        ))

                                        pickedUri?.let { uri ->
                                            val assetTitle = pickedTitle.ifBlank { "Expense #${old.id}" }
                                            viewModel.addLinkedAsset(context = context, uri = uri, title = assetTitle, description = "", expenseId = old.id)
                                        }

                                        editedExpenseImageUri = null
                                        currentSheet = null
                                    } else {
                                        viewModel.showToast("Enter valid description & amount", ToastType.ERROR)
                                    }
                                },
                                onDismiss = { currentSheet = null }
                            )
                        }

                        is SheetState.Delete -> {
                            DeleteConfirmationSheetContent(
                                title = "Delete Expense",
                                message = "Are you sure you want to delete '${sheet.expense.description}'?",
                                onConfirm = {
                                    viewModel.deleteExpense(sheet.expense)
                                    currentSheet = null
                                },
                                onDismiss = { currentSheet = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddExpenseListItem(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dashColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .drawWithCache {
                val stroke = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
                )
                onDrawWithContent {
                    drawContent()
                    drawRoundRect(
                        color = dashColor,
                        style = stroke,
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                }
            }
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Expense",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Add New Expense",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AddExpenseGridItem(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dashColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .drawWithCache {
                val stroke = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
                )
                onDrawWithContent {
                    drawContent()
                    drawRoundRect(
                        color = dashColor,
                        style = stroke,
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                }
            }
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Expense",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Add Expense",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
@Composable
fun ExpenseViewTypeToggle(
    currentViewType: ExpenseViewType,
    onViewTypeChange: (ExpenseViewType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .height(40.dp)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExpenseViewType.values().forEach { viewType ->
                val isSelected = currentViewType == viewType
                val backgroundColor = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    Color.Transparent
                val iconColor = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant

                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onViewTypeChange(viewType) },
                    shape = RoundedCornerShape(8.dp),
                    color = backgroundColor
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (viewType == ExpenseViewType.GRID) "⊞" else "☰",
                            fontSize = 18.sp,
                            color = iconColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private fun expenseToEditPrep(
    expense: Expense,
    onSetAmount: (String) -> Unit,
    onSetDesc: (String) -> Unit,
    onSetDate: (Long) -> Unit,
    onSetMethod: (String?) -> Unit
) {
    onSetAmount(expense.amount.toString())
    onSetDesc(expense.description)
    onSetDate(expense.date)
    onSetMethod(expense.paymentMethod)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseBottomSheetContent(
    title: String,
    amount: String,
    onAmountChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    date: Long,
    onDateChange: (Long) -> Unit,
    paymentMethod: String?,
    onPaymentMethodChange: (String?) -> Unit,
    onPickFromGallery: () -> Unit,
    onPickFromCamera: () -> Unit,
    previewImageUri: Uri?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerSheet(
            initialSelectedDateMillis = date,
            onDateSelected = onDateChange,
            onDismiss = { showDatePicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss) { Text("Close") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Text("₹", style = MaterialTheme.typography.titleMedium) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            singleLine = false,
            maxLines = 3,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        val methods = listOf("UPI", "PhonePe", "GPay", "Paytm", "Cash", "Card")
        var expanded by remember { mutableStateOf(false) }

        Text(text = "Payment via (optional)", style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(6.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = paymentMethod ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Select method") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                methods.forEach { method ->
                    DropdownMenuItem(text = { Text(method) }, onClick = { onPaymentMethodChange(method); expanded = false })
                }
                DropdownMenuItem(text = { Text("None") }, onClick = { onPaymentMethodChange(null); expanded = false })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Attach asset (optional)", style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onPickFromGallery, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("From gallery") }
            OutlinedButton(onClick = onPickFromCamera, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("From camera") }
        }

        previewImageUri?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth().height(160.dp)) {
                AsyncImage(model = it, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = formatDate(date))
        }

        Spacer(modifier = Modifier.height(18.dp))

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onConfirm, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Text(text = if (title.startsWith("Edit")) "Save" else "Add")
            }
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Cancel") }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}


@Composable
fun ExpenseSummaryHeader(expenses: List<Expense>) {
    val totalExpenses = expenses.filter { it.amount > 0 }.sumOf { it.amount }
    val totalCredits = expenses.filter { it.amount < 0 }.sumOf { kotlin.math.abs(it.amount) }
    val expenseCount = expenses.count { it.amount > 0 }
    val creditCount = expenses.count { it.amount < 0 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Spending",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCurrency(totalExpenses),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                val countsText = buildString {
                    if (expenseCount > 0) append("$expenseCount expense${if (expenseCount != 1) "s" else ""}")
                    if (creditCount > 0) {
                        if (expenseCount > 0) append(" • ")
                        append("$creditCount credit${if (creditCount != 1) "s" else ""} (${formatCurrency(totalCredits)})")
                    }
                }
                Text(
                    text = countsText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${expenses.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseListItem(
    expense: Expense,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val iconRes = paymentIconRes(expense.paymentIcon)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Icon + Description + Date
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Payment Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (iconRes != null) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = expense.paymentMethod,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_expense_icon),
                            contentDescription = "Expense",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Description + Date
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatDate(expense.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            // Right: Amount + Menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Show credits in green with + prefix, debits in primary color
                val isCredit = expense.amount < 0
                val displayAmount = kotlin.math.abs(expense.amount)
                val prefix = if (isCredit) "+" else ""
                
                Text(
                    text = prefix + formatCurrency(displayAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) {
                        androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green for credits
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Edit", style = MaterialTheme.typography.bodyMedium)
                                }
                            },
                            onClick = {
                                onEditClick()
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Delete",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            },
                            onClick = {
                                onDeleteClick()
                                menuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseGridItem(
    expense: Expense,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val iconRes = paymentIconRes(expense.paymentIcon)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Icon + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (iconRes != null) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = expense.paymentMethod,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_expense_icon),
                            contentDescription = "Expense",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit", style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                onEditClick()
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                onDeleteClick()
                                menuExpanded = false
                            }
                        )
                    }
                }
            }

            // Middle: Description
            Text(
                text = expense.description,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            // Bottom: Amount + Date
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Show credits in green with + prefix, debits in primary color
                val isCredit = expense.amount < 0
                val displayAmount = kotlin.math.abs(expense.amount)
                val prefix = if (isCredit) "+" else ""
                
                Text(
                    text = prefix + formatCurrency(displayAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) {
                        androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green for credits
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatDate(expense.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun String?.toPaymentIconKey(): String? = when (this) {
    "UPI" -> "UPI"
    "PhonePe" -> "PhonePe"
    "GPay" -> "GPay"
    "Paytm" -> "Paytm"
    "Cash" -> "Cash"
    "Card" -> "Card"
    else -> null
}
// Grouped Expense List View
@Composable
private fun GroupedExpenseListView(
    yearGroups: List<YearGroup>,
    nextYearToLoad: Int?,
    isLoadingYear: Boolean,
    currencySymbol: String,
    onAddExpenseClick: () -> Unit,
    onExpenseClick: (Expense) -> Unit,
    onEditClick: (Expense) -> Unit,
    onDeleteClick: (Expense) -> Unit,
    onLoadMoreClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Add expense button at top
        item(key = "ADD_BUTTON") {
            AddExpenseListItem(onClick = onAddExpenseClick)
        }
        
        // Iterate through year groups
        yearGroups.forEachIndexed { yearIndex, yearGroup ->
            // Year separator (except for first year)
            if (yearIndex > 0) {
                item(key = "YEAR_SEP_${yearGroup.year}") {
                    YearSeparator(year = yearGroup.year)
                }
            }
            
            // Month groups within the year
            yearGroup.monthGroups.forEach { monthGroup ->
                // Month header
                item(key = "MONTH_${yearGroup.year}_${monthGroup.month}") {
                    CompactMonthHeader(
                        monthGroup = monthGroup,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                // Expenses in this month
                items(
                    items = monthGroup.expenses,
                    key = { expense -> "EXP_${expense.id}" }
                ) { expense ->
                    ExpenseListItem(
                        expense = expense,
                        onClick = { onExpenseClick(expense) },
                        onEditClick = { onEditClick(expense) },
                        onDeleteClick = { onDeleteClick(expense) }
                    )
                }
            }
        }
        
        // Load more button
        if (nextYearToLoad != null) {
            item(key = "LOAD_MORE") {
                LoadMoreYearButton(
                    year = nextYearToLoad,
                    isLoading = isLoadingYear,
                    onClick = onLoadMoreClick,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}


// Grouped Expense Grid View
@Composable
private fun GroupedExpenseGridView(
    yearGroups: List<YearGroup>,
    nextYearToLoad: Int?,
    isLoadingYear: Boolean,
    currencySymbol: String,
    onAddExpenseClick: () -> Unit,
    onExpenseClick: (Expense) -> Unit,
    onEditClick: (Expense) -> Unit,
    onDeleteClick: (Expense) -> Unit,
    onLoadMoreClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 180.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Add expense button at top (in grid format)
        item(key = "ADD_BUTTON") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AddExpenseGridItem(
                    onClick = onAddExpenseClick,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        
        // Iterate through year groups
        yearGroups.forEachIndexed { yearIndex, yearGroup ->
            // Year separator (except for first year)
            if (yearIndex > 0) {
                item(key = "YEAR_SEP_${yearGroup.year}") {
                    YearSeparator(year = yearGroup.year)
                }
            }
            
            // Month groups within the year
            yearGroup.monthGroups.forEach { monthGroup ->
                // Month header
                item(key = "MONTH_${yearGroup.year}_${monthGroup.month}") {
                    CompactMonthHeader(
                        monthGroup = monthGroup,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                // Expenses in this month (in grid format - 2 columns)
                val chunkedExpenses = monthGroup.expenses.chunked(2)
                items(
                    items = chunkedExpenses,
                    key = { rowExpenses -> "ROW_${rowExpenses.firstOrNull()?.id ?: 0}" }
                ) { rowExpenses ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowExpenses.forEach { expense ->
                            ExpenseGridItem(
                                expense = expense,
                                onClick = { onExpenseClick(expense) },
                                onEditClick = { onEditClick(expense) },
                                onDeleteClick = { onDeleteClick(expense) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Add spacer if only one item in row
                        if (rowExpenses.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        
        // Load more button
        if (nextYearToLoad != null) {
            item(key = "LOAD_MORE") {
                LoadMoreYearButton(
                    year = nextYearToLoad,
                    isLoading = isLoadingYear,
                    onClick = onLoadMoreClick,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}
