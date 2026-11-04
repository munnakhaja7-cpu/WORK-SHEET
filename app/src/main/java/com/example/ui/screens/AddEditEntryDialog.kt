package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.GuestEntry
import com.example.viewmodel.LedgerViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEntryDialog(
    viewModel: LedgerViewModel,
    entryToEdit: GuestEntry? = null,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var date by remember { mutableStateOf(entryToEdit?.date ?: today) }
    var name by remember { mutableStateOf(entryToEdit?.name ?: "") }
    var phoneNumber by remember { mutableStateOf(entryToEdit?.phoneNumber ?: "") }
    var model by remember { mutableStateOf(entryToEdit?.model ?: "") }
    var rsNo by remember { mutableStateOf(entryToEdit?.rsNo ?: "") }

    var acStr by remember { mutableStateOf(entryToEdit?.ac?.toString() ?: "10.0") }
    var foodStr by remember { mutableStateOf(entryToEdit?.food?.toString() ?: "0.0") }
    var extraStr by remember { mutableStateOf(entryToEdit?.extra?.toString() ?: "0.0") }

    var entryCountToday by remember { mutableStateOf(0) }
    var totalToday by remember { mutableStateOf(0.0) }

    // Update entry count & default pricing when date changes (if adding new)
    LaunchedEffect(date) {
        val (count, tot) = viewModel.getEntryCountAndTotalsForDate(date)
        entryCountToday = count
        totalToday = tot

        if (entryToEdit == null) {
            val (defAc, defFood, defExtra) = viewModel.calculateDefaultPricingForDate(date)
            acStr = defAc.toString()
            foodStr = defFood.toString()
            extraStr = defExtra.toString()
        }
    }

    val calculatedTotal = (acStr.toDoubleOrNull() ?: 0.0) +
            (foodStr.toDoubleOrNull() ?: 0.0) +
            (extraStr.toDoubleOrNull() ?: 0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (entryToEdit == null) "New Guest Entry" else "Edit Guest Entry")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (entryToEdit == null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Entries made so far for $date: $entryCountToday",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Total received on this date: ₹$totalToday",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = if (entryCountToday < 2) "Rule applied: A/C = 10" else "Rule applied: Food = 32, Extra = 25 (A/C blank)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Guest Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model / Room Type") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = rsNo,
                    onValueChange = { rsNo = it },
                    label = { Text("RS.NO (Room/Sl No)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = acStr,
                        onValueChange = { acStr = it },
                        label = { Text("A/C") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = foodStr,
                        onValueChange = { foodStr = it },
                        label = { Text("Food") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = extraStr,
                        onValueChange = { extraStr = it },
                        label = { Text("Extra") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = calculatedTotal.toString(),
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Total") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val ac = acStr.toDoubleOrNull() ?: 0.0
                        val food = foodStr.toDoubleOrNull() ?: 0.0
                        val extra = extraStr.toDoubleOrNull() ?: 0.0

                        if (entryToEdit == null) {
                            viewModel.addEntry(
                                date = date,
                                name = name,
                                phoneNumber = phoneNumber,
                                model = model,
                                rsNo = rsNo,
                                ac = ac,
                                food = food,
                                extra = extra
                            )
                        } else {
                            viewModel.updateEntry(
                                id = entryToEdit.id,
                                date = date,
                                name = name,
                                phoneNumber = phoneNumber,
                                model = model,
                                rsNo = rsNo,
                                ac = ac,
                                food = food,
                                extra = extra,
                                createdAt = entryToEdit.createdAt
                            )
                        }
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
