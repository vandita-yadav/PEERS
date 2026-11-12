package com.example.peers.features.budget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky

private val categoryColors = listOf(
    PeersLavender,
    PeersSky,
    PeersMint,
    Color(0xFFFFD9E2),
    Color(0xFFFFE8B8),
    Color(0xFFCDE7FF),
    Color(0xFFE5D4FF),
    Color(0xFFD6F5E3),
)

private fun categoryColor(category: BudgetCategory): Color =
    categoryColors[category.ordinal % categoryColors.size]

private fun money(amount: Int): String = "₹${"%,d".format(amount)}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetPlannerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showExpenseEditor by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Budget & Cost of Living",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Plan the month, track what you actually spend",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showExpenseEditor = true },
                    containerColor = PeersPrimaryIndigo,
                    contentColor = Color.White,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 18.dp),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Text(
                            "Add expense",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = innerPadding.calculateTopPadding() + 4.dp,
                    bottom = innerPadding.calculateBottomPadding() + 120.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    MonthSwitcher(
                        label = state.monthLabel,
                        isCurrentMonth = state.isCurrentMonth,
                        onPrevious = viewModel::previousMonth,
                        onNext = viewModel::nextMonth,
                        onToday = viewModel::thisMonth,
                    )
                }

                if (state.isLoading) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                        ) {}
                    }
                } else if (state.isEmptyMonth) {
                    item {
                        EmptyMonthCard(
                            onSetup = { showBudgetDialog = true },
                            onLoadSample = viewModel::loadSampleMonth,
                        )
                    }
                } else {
                    item {
                        BudgetHero(
                            state = state,
                            onEditBudget = { showBudgetDialog = true },
                        )
                    }

                    if (state.estimatedTotal > 0) {
                        item { EstimatedVsActualCard(state) }
                    }

                    if (state.savingsGoal > 0) {
                        item { SavingsRow(state) }
                    }

                    if (state.breakdown.isNotEmpty()) {
                        item { CategoryBreakdownCard(state.breakdown) }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                "Expenses",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                            if (state.expenses.isNotEmpty()) {
                                TextButton(onClick = { showClearDialog = true }) {
                                    Text("Clear month", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    if (state.expenses.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    "No expenses yet. Tap “Add expense” to start tracking.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(18.dp),
                                )
                            }
                        }
                    } else {
                        items(state.expenses, key = { it.id }) { expense ->
                            ExpenseRow(
                                expense = expense,
                                onClick = { editingExpense = expense },
                                onDelete = { viewModel.deleteExpense(expense.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showExpenseEditor) {
        ModalBottomSheet(
            onDismissRequest = { showExpenseEditor = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            ExpenseEditor(
                initial = null,
                onSave = { title, category, amount, estimated, note ->
                    viewModel.addExpense(title, category, amount, estimated, note)
                    showExpenseEditor = false
                },
                onDelete = null,
            )
        }
    }

    editingExpense?.let { expense ->
        ModalBottomSheet(
            onDismissRequest = { editingExpense = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            ExpenseEditor(
                initial = expense,
                onSave = { title, category, amount, estimated, note ->
                    viewModel.updateExpense(
                        expense.copy(
                            title = title.trim(),
                            category = category.label,
                            amount = amount,
                            estimated = estimated,
                            note = note.trim(),
                        )
                    )
                    editingExpense = null
                },
                onDelete = {
                    viewModel.deleteExpense(expense.id)
                    editingExpense = null
                },
            )
        }
    }

    if (showBudgetDialog) {
        BudgetDialog(
            initialBudget = state.budget,
            initialSavings = state.savingsGoal,
            onSave = { budget, savings ->
                viewModel.saveBudget(budget, savings)
                showBudgetDialog = false
            },
            onDismiss = { showBudgetDialog = false },
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear this month?") },
            text = {
                Text(
                    "This removes every expense for ${state.monthLabel} from this device. " +
                        "Your monthly budget stays."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearMonth()
                    showClearDialog = false
                }) { Text("Clear expenses", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun MonthSwitcher(
    label: String,
    isCurrentMonth: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous month")
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (isCurrentMonth) "This month" else "Tap to jump to today",
                    style = MaterialTheme.typography.labelSmall,
                    color = PeersPrimaryIndigo,
                    modifier = Modifier.clickable(onClick = onToday),
                )
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Rounded.ChevronRight, contentDescription = "Next month")
            }
        }
    }
}

@Composable
private fun BudgetHero(state: BudgetUiState, onEditBudget: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(PeersPrimaryIndigo, Color(0xFF8C7BF3), PeersLavender)
                )
            )
            .padding(20.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Left to spend",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                    Text(
                        if (state.overspent > 0) money(0) else money(state.remaining),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f),
                ) {
                    IconButton(onClick = onEditBudget) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = "Edit monthly budget",
                            tint = Color.White,
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 14.dp),
            ) {
                Text(
                    if (state.estimatedTotal > 0) {
                        "${money(state.actualTotal)} paid · ${money(state.estimatedTotal)} planned"
                    } else {
                        "${money(state.actualTotal)} of ${money(state.budget)}"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${(state.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.25f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(state.progress)
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White),
                )
            }

            if (state.overspent > 0) {
                Text(
                    "You're ${money(state.overspent)} over plan once planned costs are paid. " +
                        "Adjust the budget or trim a category.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFE1E1),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 10.dp),
                )
            } else {
                Text(
                    "Includes planned costs, so nothing sneaks up on you.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun EstimatedVsActualCard(state: BudgetUiState) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp)) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Already spent",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    money(state.actualTotal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                Modifier
                    .width(1.dp)
                    .height(38.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            )
            Column(Modifier.weight(1f).padding(start = 16.dp)) {
                Text(
                    "Still planned",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    money(state.estimatedTotal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PeersPrimaryIndigo,
                )
            }
            Icon(
                Icons.Rounded.TrendingUp,
                contentDescription = null,
                tint = PeersPrimaryIndigo,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }
    }
}

@Composable
private fun SavingsRow(state: BudgetUiState) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = PeersMint,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Icon(Icons.Rounded.Savings, contentDescription = null, tint = Color(0xFF1F6B3B))
            Text(
                "Savings goal",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F6B3B),
                modifier = Modifier.weight(1f).padding(start = 10.dp),
            )
            Text(
                money(state.savingsGoal),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F6B3B),
            )
        }
    }
}

@Composable
private fun CategoryBreakdownCard(breakdown: List<CategoryBreakdown>) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "Where it goes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Solid = paid · faded = planned",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp),
            )
            val max = breakdown.maxOf { it.actual + it.estimated }.coerceAtLeast(1)
            breakdown.forEach { row ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(categoryColor(row.category)),
                    ) {
                        Text(row.category.emoji, fontSize = 16.sp)
                    }
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                row.category.label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                money(row.actual),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            if (row.estimated > 0) {
                                Text(
                                    " + ${money(row.estimated)} planned",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 5.dp)
                                .height(7.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            if (row.actual > 0) {
                                Box(
                                    Modifier
                                        .weight(row.actual.toFloat())
                                        .fillMaxSize()
                                        .background(categoryColor(row.category))
                                )
                            }
                            if (row.estimated > 0) {
                                Box(
                                    Modifier
                                        .weight(row.estimated.toFloat())
                                        .fillMaxSize()
                                        .background(categoryColor(row.category).copy(alpha = 0.4f))
                                )
                            }
                            val rest = max - row.actual - row.estimated
                            if (rest > 0) Spacer(Modifier.weight(rest.toFloat()))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: ExpenseEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val category = BudgetCategory.fromLabel(expense.category)
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(categoryColor(category)),
            ) {
                Text(category.emoji, fontSize = 18.sp)
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        expense.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (expense.estimated) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = PeersPrimaryIndigo.copy(alpha = 0.12f),
                            modifier = Modifier.padding(start = 8.dp),
                        ) {
                            Text(
                                "Planned",
                                style = MaterialTheme.typography.labelSmall,
                                color = PeersPrimaryIndigo,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Text(
                    listOf(category.label, expense.note).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                money(expense.amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp),
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete ${expense.title}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptyMonthCard(onSetup: () -> Unit, onLoadSample: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        PeersPrimaryIndigo.copy(alpha = 0.14f),
                        PeersSky.copy(alpha = 0.35f),
                    )
                )
            )
            .padding(24.dp),
    ) {
        Text("🧮", fontSize = 44.sp)
        Text(
            "Let's plan this month",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            "Set a monthly budget, then track what you actually spend and what's still planned.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
        Button(
            onClick = onSetup,
            colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(50.dp),
        ) {
            Text("Set monthly budget", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onLoadSample, modifier = Modifier.padding(top = 4.dp)) {
            Text("Or load a sample month", color = PeersPrimaryIndigo)
        }
    }
}

@Composable
private fun BudgetDialog(
    initialBudget: Int,
    initialSavings: Int,
    onSave: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var budget by remember { mutableStateOf(if (initialBudget > 0) initialBudget.toString() else "") }
    var savings by remember { mutableStateOf(if (initialSavings > 0) initialSavings.toString() else "") }
    val budgetValue = budget.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialBudget > 0) "Edit monthly plan" else "Set monthly budget") },
        text = {
            Column {
                Text(
                    "Pick an amount that covers rent, food, transport and the fun bits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it.filter(Char::isDigit) },
                    label = { Text("Monthly budget (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
                OutlinedTextField(
                    value = savings,
                    onValueChange = { savings = it.filter(Char::isDigit) },
                    label = { Text("Savings goal (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(budgetValue, savings.toIntOrNull() ?: 0) },
                enabled = budgetValue > 0,
            ) { Text("Save plan", color = PeersPrimaryIndigo) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseEditor(
    initial: ExpenseEntity?,
    onSave: (String, BudgetCategory, Int, Boolean, String) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var amount by remember {
        mutableStateOf(initial?.amount?.toString().orEmpty())
    }
    var category by remember {
        mutableStateOf(initial?.category?.let(BudgetCategory::fromLabel) ?: BudgetCategory.FOOD)
    }
    var estimated by remember { mutableStateOf(initial?.estimated ?: false) }
    var note by remember { mutableStateOf(initial?.note.orEmpty()) }

    val valid = title.isNotBlank() && (amount.toIntOrNull() ?: 0) > 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(bottom = 28.dp),
    ) {
        Text(
            if (initial == null) "Add expense" else "Edit expense",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Track both paid and planned costs to see your real cost of living.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 16.dp),
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter(Char::isDigit) },
                label = { Text("Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("What was it?") },
                placeholder = { Text("Mess bill") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1.4f),
            )
        }

        Text(
            "Category",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 18.dp, bottom = 6.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(BudgetCategory.entries.toList()) { option ->
                FilterChip(
                    selected = category == option,
                    onClick = { category = option },
                    label = { Text("${option.emoji} ${option.label}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PeersPrimaryIndigo,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "This is a planned expense",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "Planned costs are tracked separately from what you've paid.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = estimated,
                onCheckedChange = { estimated = it },
                colors = SwitchDefaults.colors(checkedTrackColor = PeersPrimaryIndigo),
            )
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (optional)") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        )

        Button(
            onClick = { onSave(title, category, amount.toIntOrNull() ?: 0, estimated, note) },
            enabled = valid,
            colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(52.dp),
        ) {
            Text(if (initial == null) "Add expense" else "Save changes", fontWeight = FontWeight.Bold)
        }

        if (onDelete != null) {
            OutlinedButton(
                onClick = onDelete,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(48.dp),
            ) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text("  Delete expense")
            }
        }
    }
}
