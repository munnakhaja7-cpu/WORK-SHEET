package com.example.ui.screens

import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.GuestEntry
import com.example.utils.PdfGenerator
import com.example.viewmodel.LedgerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlySummaryScreen(
    viewModel: LedgerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allEntries by viewModel.allEntries.collectAsState(initial = emptyList())

    // Extract available months from entries
    val availableMonths = remember(allEntries) {
        allEntries.map { it.date.take(7) }.distinct().sortedDescending()
    }

    var selectedMonth by remember(availableMonths) {
        mutableStateOf(availableMonths.firstOrNull() ?: SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().time))
    }

    val monthEntries = remember(allEntries, selectedMonth) {
        allEntries.filter { it.date.startsWith(selectedMonth) }
    }

    val totalAc = monthEntries.sumOf { it.ac }
    val totalFood = monthEntries.sumOf { it.food }
    val totalExtra = monthEntries.sumOf { it.extra }
    val grandTotal = monthEntries.sumOf { it.total }

    var expandedDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Statement Summary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (monthEntries.isNotEmpty()) {
                        IconButton(onClick = {
                            val file = PdfGenerator.generateMonthlyStatementPdf(
                                context = context,
                                yearMonth = selectedMonth,
                                entries = monthEntries,
                                businessName = viewModel.prefs.businessName,
                                location = viewModel.prefs.location
                            )
                            if (file != null) {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback or Toast if no PDF viewer
                                }
                            }
                        }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Month:", style = MaterialTheme.typography.titleMedium)

                Box {
                    OutlinedButton(onClick = { expandedDropdown = true }) {
                        Text(selectedMonth)
                    }
                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        val months = if (availableMonths.isEmpty()) listOf(selectedMonth) else availableMonths
                        for (m in months) {
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    selectedMonth = m
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Statement for ${viewModel.prefs.businessName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Location: ${viewModel.prefs.location}", style = MaterialTheme.typography.bodySmall)
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Entries: ${monthEntries.size}")
                        Text("A/C Total: ₹$totalAc")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Food Total: ₹$totalFood")
                        Text("Extra Total: ₹$totalExtra")
                    }
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Grand Total Received:", style = MaterialTheme.typography.titleMedium)
                        Text("₹$grandTotal", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Text("Entries Breakdown (${monthEntries.size})", style = MaterialTheme.typography.titleMedium)

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(monthEntries) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(entry.name, style = MaterialTheme.typography.titleSmall)
                                Text(entry.date, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Phone: ${entry.phoneNumber.ifEmpty { "N/A" }} | Model: ${entry.model.ifEmpty { "N/A" }} | RS: ${entry.rsNo.ifEmpty { "N/A" }}", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("A/C: ${if (entry.ac > 0) entry.ac else "-"} | Food: ${if (entry.food > 0) entry.food else "-"} | Extra: ${if (entry.extra > 0) entry.extra else "-"}", style = MaterialTheme.typography.bodySmall)
                                Text("Total: ₹${entry.total}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
