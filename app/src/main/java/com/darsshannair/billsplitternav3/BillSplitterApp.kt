package com.darsshannair.billsplitternav3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.darsshannair.billsplitternav3.navigation.BillSplitterRoute
import com.darsshannair.billsplitternav3.navigation.rememberNavigator
import com.darsshannair.billsplitternav3.screens.AddExpenseScreen
import com.darsshannair.billsplitternav3.screens.AddParticipantsScreen
import com.darsshannair.billsplitternav3.screens.HomeScreen
import com.darsshannair.billsplitternav3.screens.SettlementsScreen
import com.darsshannair.billsplitternav3.screens.SplitResultsScreen
import com.darsshannair.billsplitternav3.screens.ViewExpensesScreen
import com.darsshannair.billsplitternav3.viewmodels.BillSplitterViewModel

@Composable
fun BillSplitterApp(
    modifier: Modifier = Modifier,
    viewModel: BillSplitterViewModel = viewModel()
) {
    // Create navigator instance
    val navigator = rememberNavigator()

    // Collect state from ViewModel
    val participants by viewModel.participants.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val splitResults by viewModel.splitResults.collectAsState()
    val settlements by viewModel.settlements.collectAsState()

    // NavDisplay - the core Nav3 composable that displays screens based on the back stack
    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.navigateBack() },
        modifier = modifier,
        // Entry provider - maps routes to their composable content
        entryProvider = { route ->
            when (route) {
                is BillSplitterRoute.Home -> NavEntry(route) {
                    HomeScreen(
                        onStartNewBill = {
                            // Clear all data and navigate to add participants
                            viewModel.clearAll()
                            navigator.navigateTo(BillSplitterRoute.AddParticipants)
                        }
                    )
                }

                is BillSplitterRoute.AddParticipants -> NavEntry(route) {
                    AddParticipantsScreen(
                        participants = participants,
                        onAddParticipant = { name ->
                            viewModel.addParticipant(name)
                        },
                        onRemoveParticipant = { person ->
                            viewModel.removeParticipant(person)
                        },
                        onNavigateBack = {
                            navigator.navigateBack()
                        },
                        onContinue = {
                            navigator.navigateTo(BillSplitterRoute.ViewExpenses)
                        }
                    )
                }

                is BillSplitterRoute.AddExpense -> NavEntry(route) {
                    val editingExpense = route.editingExpenseId?.let {
                        viewModel.getExpenseById(it)
                    }

                    AddExpenseScreen(
                        participants = participants,
                        editingExpense = editingExpense,
                        onSaveExpense = { description, amount, paidBy, sharedBy ->
                            viewModel.addOrUpdateExpense(
                                description = description,
                                amount = amount,
                                paidBy = paidBy,
                                sharedBy = sharedBy,
                                editingExpenseId = route.editingExpenseId
                            )
                            navigator.navigateBack()
                        },
                        onNavigateBack = {
                            navigator.navigateBack()
                        }
                    )
                }

                is BillSplitterRoute.ViewExpenses -> NavEntry(route) {
                    ViewExpensesScreen(
                        expenses = expenses,
                        totalExpenses = viewModel.getTotalExpenses(),
                        onAddExpense = {
                            navigator.navigateTo(BillSplitterRoute.AddExpense())
                        },
                        onEditExpense = { expenseId ->
                            navigator.navigateTo(BillSplitterRoute.AddExpense(expenseId))
                        },
                        onDeleteExpense = { expenseId ->
                            viewModel.deleteExpense(expenseId)
                        },
                        onNavigateBack = {
                            navigator.navigateBack()
                        },
                        onCalculateSplit = {
                            viewModel.calculateSplitResults()
                            navigator.navigateTo(BillSplitterRoute.SplitResults)
                        }
                    )
                }

                is BillSplitterRoute.SplitResults -> NavEntry(route) {
                    SplitResultsScreen(
                        splitResults = splitResults,
                        onNavigateBack = {
                            navigator.navigateBack()
                        },
                        onViewSettlements = {
                            navigator.navigateTo(BillSplitterRoute.Settlements)
                        }
                    )
                }

                is BillSplitterRoute.Settlements -> NavEntry(route) {
                    SettlementsScreen(
                        settlements = settlements,
                        onNavigateBack = {
                            navigator.navigateBack()
                        },
                        onFinish = {
                            // Navigate back to home and clear the stack
                            navigator.navigateAndClear(BillSplitterRoute.Home)
                        }
                    )
                }
            }
        }
    )
}