package com.darsshannair.billsplitternav3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.darsshannair.billsplitternav3.models.ExpenseItem
import com.darsshannair.billsplitternav3.models.Person

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    participants: List<Person>,
    editingExpense: ExpenseItem?,
    onSaveExpense: (String, Double, Person, List<Person>) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var description by remember {
        mutableStateOf(editingExpense?.description ?: "")
    }
    var amountText by remember {
        mutableStateOf(editingExpense?.amount?.toString() ?: "")
    }
    var selectedPayer by remember {
        mutableStateOf(editingExpense?.paidBy ?: participants.firstOrNull())
    }
    var selectedParticipants by remember {
        mutableStateOf(editingExpense?.sharedBy?.toSet() ?: participants.toSet())
    }
    var showPayerDropdown by remember { mutableStateOf(false) }

    val isValid = description.isNotBlank() &&
            amountText.toDoubleOrNull() != null &&
            amountText.toDoubleOrNull()!! > 0 &&
            selectedPayer != null &&
            selectedParticipants.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (editingExpense != null) "Edit Expense" else "Add Expense")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Description
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("e.g., Dinner, Movie tickets") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null)
                        },
                        singleLine = true
                    )
                }

                // Amount
                item {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount") },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Build, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }

                // Paid by
                item {
                    Text(
                        text = "Paid by",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = showPayerDropdown,
                        onExpandedChange = { showPayerDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = selectedPayer?.name ?: "Select person",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPayerDropdown)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = showPayerDropdown,
                            onDismissRequest = { showPayerDropdown = false }
                        ) {
                            participants.forEach { person ->
                                DropdownMenuItem(
                                    text = { Text(person.name) },
                                    onClick = {
                                        selectedPayer = person
                                        showPayerDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Split between
                item {
                    Text(
                        text = "Split between",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedParticipants.size} of ${participants.size} selected",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row {
                            TextButton(
                                onClick = { selectedParticipants = emptySet() }
                            ) {
                                Text("Clear")
                            }
                            TextButton(
                                onClick = { selectedParticipants = participants.toSet() }
                            ) {
                                Text("Select All")
                            }
                        }
                    }
                }

                // Participant checkboxes
                items(participants, key = { it.id }) { person ->
                    ParticipantCheckbox(
                        person = person,
                        isSelected = selectedParticipants.contains(person),
                        onToggle = {
                            selectedParticipants = if (selectedParticipants.contains(person)) {
                                selectedParticipants - person
                            } else {
                                selectedParticipants + person
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (isValid && selectedPayer != null) {
                        onSaveExpense(
                            description,
                            amount,
                            selectedPayer!!,
                            selectedParticipants.toList()
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isValid
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (editingExpense != null) "Update Expense" else "Add Expense",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun ParticipantCheckbox(
    person: Person,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = person.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}