package com.darsshannair.billsplitternav3.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

class Navigator(
    initialBackStack: SnapshotStateList<BillSplitterRoute> =
        mutableListOf(BillSplitterRoute.Home).toMutableStateList()
) {
    var backStack: SnapshotStateList<BillSplitterRoute> by mutableStateOf(initialBackStack)
        private set

    fun navigateTo(route: BillSplitterRoute) {
        backStack.add(route)
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun popBackTo(route: BillSplitterRoute, inclusive: Boolean = false) {
        val index = backStack.indexOfLast { it == route }
        if (index != -1) {
            val targetIndex = if (inclusive) index else index + 1
            if (targetIndex < backStack.size) {
                backStack.subList(targetIndex, backStack.size).clear()
            }
        }
    }

    fun navigateAndClear(route: BillSplitterRoute) {
        backStack.clear()
        backStack.add(route)
    }

    fun currentRoute(): BillSplitterRoute? {
        return backStack.lastOrNull()
    }

    companion object {
        fun saver(): Saver<Navigator, List<String>> = Saver(
            save = { navigator ->
                // Convert routes to strings for saving
                navigator.backStack.map { route ->
                    when (route) {
                        is BillSplitterRoute.Home -> "Home"
                        is BillSplitterRoute.AddParticipants -> "AddParticipants"
                        is BillSplitterRoute.AddExpense ->
                            "AddExpense:${route.editingExpenseId ?: ""}"
                        is BillSplitterRoute.ViewExpenses -> "ViewExpenses"
                        is BillSplitterRoute.SplitResults -> "SplitResults"
                        is BillSplitterRoute.Settlements -> "Settlements"
                    }
                }
            },
            restore = { savedList ->
                // Convert strings back to routes
                val routes = savedList.mapNotNull { savedRoute ->
                    when {
                        savedRoute == "Home" -> BillSplitterRoute.Home
                        savedRoute == "AddParticipants" -> BillSplitterRoute.AddParticipants
                        savedRoute.startsWith("AddExpense:") -> {
                            val expenseId = savedRoute.substringAfter("AddExpense:")
                                .takeIf { it.isNotBlank() }
                            BillSplitterRoute.AddExpense(expenseId)
                        }
                        savedRoute == "ViewExpenses" -> BillSplitterRoute.ViewExpenses
                        savedRoute == "SplitResults" -> BillSplitterRoute.SplitResults
                        savedRoute == "Settlements" -> BillSplitterRoute.Settlements
                        else -> null
                    }
                }.toMutableStateList()

                Navigator(
                    if (routes.isEmpty()) {
                        mutableListOf(BillSplitterRoute.Home).toMutableStateList()
                    } else {
                        routes.toMutableStateList()
                    }
                )
            }
        )
    }
}

@Composable
fun rememberNavigator(): Navigator {
    return rememberSaveable(saver = Navigator.saver()) {
        Navigator()
    }
}