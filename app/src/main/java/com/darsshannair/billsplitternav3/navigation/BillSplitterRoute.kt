package com.darsshannair.billsplitternav3.navigation

import androidx.navigation3.runtime.NavKey

sealed interface BillSplitterRoute : NavKey {
    // Define entry point
    data object Home : BillSplitterRoute

    data object AddParticipants : BillSplitterRoute
    data class AddExpense(val editingExpenseId: String? = null) : BillSplitterRoute
    data object ViewExpenses : BillSplitterRoute
    data object SplitResults : BillSplitterRoute
    data object Settlements : BillSplitterRoute
}