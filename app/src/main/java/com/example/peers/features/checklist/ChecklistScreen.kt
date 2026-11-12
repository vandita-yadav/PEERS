package com.example.peers.features.checklist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.peers.features.dashboard.DashboardUser
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChecklistScreen(
    user: DashboardUser?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChecklistViewModel = viewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showAddSheet by remember { mutableStateOf(false) }
    var showRegenerateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initialize(user)
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    BackHandler { onBack() }

    val total = allItems.size
    val completed = allItems.count { it.isCompleted }
    val urgentRemaining = allItems.count { !it.isCompleted && it.priority == ChecklistPriority.HIGH }
    val percent = if (total > 0) (completed * 100 / total) else 0

    Scaffold(
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Column(Modifier.weight(1f).padding(start = 4.dp)) {
                    Text(
                        "Student Checklist",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        "Personalized for ${user?.university ?: "Campus"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { showRegenerateDialog = true }) {
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = "Regenerate",
                        tint = PeersPrimaryIndigo,
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = PeersPrimaryIndigo,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Task")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // ── Hero Progress Card ────────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                    ) {
                        // Circular progress ring
                        Box(contentAlignment = Alignment.Center) {
                            val animatedProgress by animateFloatAsState(
                                targetValue = percent / 100f,
                                animationSpec = tween(700),
                                label = "progressRing",
                            )
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.size(76.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                strokeWidth = 7.dp,
                            )
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(76.dp),
                                color = PeersPrimaryIndigo,
                                strokeWidth = 7.dp,
                                strokeCap = StrokeCap.Round,
                            )
                            Text(
                                "$percent%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        Spacer(Modifier.width(20.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                "$completed of $total tasks done",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (urgentRemaining > 0) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFFFDE8E8),
                                    modifier = Modifier.padding(top = 6.dp),
                                ) {
                                    Text(
                                        "⚠️ $urgentRemaining urgent remaining",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFC53030),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    )
                                }
                            } else {
                                Text(
                                    if (percent == 100) "🎉 All tasks finished! You're ready."
                                    else "Steady progress! Keep ticking off items.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
            }

            // ── Category Filter Ribbon ────────────────────────────────────────
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(ChecklistCategory.entries.toTypedArray()) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setCategory(cat) },
                            label = {
                                Text(
                                    "${cat.emoji} ${cat.label}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PeersPrimaryIndigo,
                                selectedLabelColor = Color.White,
                            ),
                            shape = RoundedCornerShape(50),
                        )
                    }
                }
            }

            // ── Tasks Count Header ────────────────────────────────────────────
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 6.dp),
                ) {
                    Text(
                        "${items.size} tasks in ${selectedCategory.label}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ── Tasks List ────────────────────────────────────────────────────
            if (items.isEmpty()) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 20.dp),
                    ) {
                        Text("✨", fontSize = 36.sp)
                        Text(
                            "No tasks in this category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Text(
                            "Tap + below to add a custom checklist item.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(items, key = { it.id }) { item ->
                    ChecklistTaskRow(
                        item = item,
                        onToggle = { viewModel.toggleTask(item) },
                        onDelete = { viewModel.deleteTask(item) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }

    // ── Add Custom Task Sheet ─────────────────────────────────────────────────
    if (showAddSheet) {
        AddChecklistTaskSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { title, desc, cat, prio ->
                viewModel.addCustomTask(title, desc, cat, prio)
                showAddSheet = false
            },
        )
    }

    // ── Regenerate Confirmation Dialog ────────────────────────────────────────
    if (showRegenerateDialog) {
        AlertDialog(
            onDismissRequest = { showRegenerateDialog = false },
            title = { Text("Reset & Regenerate Checklist?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will restore the personalized recommended checklist tasks based on your current university and course details.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.regenerate(user)
                        showRegenerateDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = PeersPrimaryIndigo),
                ) {
                    Text("Regenerate", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegenerateDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
        )
    }
}

@Composable
private fun ChecklistTaskRow(
    item: ChecklistItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isCompleted) 0.dp else 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(14.dp),
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = PeersPrimaryIndigo,
                    uncheckedColor = MaterialTheme.colorScheme.outline,
                ),
                modifier = Modifier.padding(end = 6.dp),
            )

            Column(Modifier.weight(1f)) {
                // Category + Priority badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 4.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = item.category.color.copy(alpha = 0.25f),
                    ) {
                        Text(
                            "${item.category.emoji} ${item.category.label}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = item.priority.color.copy(alpha = 0.15f),
                    ) {
                        Text(
                            item.priority.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = item.priority.color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }

                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                )

                if (item.description.isNotBlank()) {
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddChecklistTaskSheet(
    onDismiss: () -> Unit,
    onAdd: (title: String, desc: String, category: ChecklistCategory, priority: ChecklistPriority) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf(ChecklistCategory.CUSTOM) }
    var selectedPrio by remember { mutableStateOf(ChecklistPriority.MEDIUM) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
        ) {
            Text(
                "Add Custom Task",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title *") },
                placeholder = { Text("e.g. Buy winter boots & gloves") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes / Description (Optional)") },
                placeholder = { Text("Add specific reminders, links, or deadlines") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                minLines = 2,
            )

            Spacer(Modifier.height(14.dp))

            Text(
                "Category:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = PeersPrimaryIndigo,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 12.dp),
            ) {
                ChecklistCategory.entries.filter { it != ChecklistCategory.ALL }.forEach { cat ->
                    val isChosen = selectedCat == cat
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isChosen) cat.color else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedCat = cat },
                    ) {
                        Text(
                            "${cat.emoji} ${cat.label}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                            color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            Text(
                "Priority:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = PeersPrimaryIndigo,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            ) {
                ChecklistPriority.entries.forEach { prio ->
                    val isChosen = selectedPrio == prio
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isChosen) prio.color else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedPrio = prio },
                    ) {
                        Text(
                            prio.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                            color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            Button(
                onClick = { onAdd(title, description, selectedCat, selectedPrio) },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text("Add to Checklist", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
