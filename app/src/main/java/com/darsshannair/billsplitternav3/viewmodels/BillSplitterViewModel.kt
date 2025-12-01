package com.darsshannair.billsplitternav3.viewmodels

import androidx.lifecycle.ViewModel
import com.darsshannair.billsplitternav3.models.ExpenseItem
import com.darsshannair.billsplitternav3.models.Person
import com.darsshannair.billsplitternav3.models.Settlement
import com.darsshannair.billsplitternav3.models.SplitResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillSplitterViewModel : ViewModel() {

    private val _participants = MutableStateFlow<List<Person>>(emptyList())
    val participants: StateFlow<List<Person>> = _participants.asStateFlow()

    private val _expenses = MutableStateFlow<List<ExpenseItem>>(emptyList())
    val expenses: StateFlow<List<ExpenseItem>> = _expenses.asStateFlow()

    private val _splitResults = MutableStateFlow<List<SplitResult>>(emptyList())
    val splitResults: StateFlow<List<SplitResult>> = _splitResults.asStateFlow()

    private val _settlements = MutableStateFlow<List<Settlement>>(emptyList())
    val settlements: StateFlow<List<Settlement>> = _settlements.asStateFlow()

    fun addParticipant(name: String) {
        val newPerson = Person(name = name.trim())
        _participants.value = _participants.value + newPerson
    }

    fun removeParticipant(person: Person) {
        _participants.value = _participants.value.filter { it.id != person.id }
        // Also remove this person from all expenses
        _expenses.value = _expenses.value.map { expense ->
            expense.copy(
                sharedBy = expense.sharedBy.filter { it.id != person.id }
            )
        }.filter { it.sharedBy.isNotEmpty() } // Remove expenses with no participants
    }

    fun addOrUpdateExpense(
        description: String,
        amount: Double,
        paidBy: Person,
        sharedBy: List<Person>,
        editingExpenseId: String? = null
    ) {
        val expense = ExpenseItem(
            id = editingExpenseId ?: java.util.UUID.randomUUID().toString(),
            description = description.trim(),
            amount = amount,
            paidBy = paidBy,
            sharedBy = sharedBy
        )

        _expenses.value = if (editingExpenseId != null) {
            _expenses.value.map { if (it.id == editingExpenseId) expense else it }
        } else {
            _expenses.value + expense
        }
    }

    fun deleteExpense(expenseId: String) {
        _expenses.value = _expenses.value.filter { it.id != expenseId }
    }

    fun getExpenseById(id: String): ExpenseItem? {
        return _expenses.value.find { it.id == id }
    }

    fun calculateSplitResults() {
        val participants = _participants.value
        val expenses = _expenses.value

        if (participants.isEmpty() || expenses.isEmpty()) {
            _splitResults.value = emptyList()
            return
        }

        // Calculate how much each person paid
        val totalPaid = mutableMapOf<String, Double>()
        participants.forEach { totalPaid[it.id] = 0.0 }

        // Calculate how much each person owes
        val totalOwed = mutableMapOf<String, Double>()
        participants.forEach { totalOwed[it.id] = 0.0 }

        expenses.forEach { expense ->
            // Add to what the payer paid
            totalPaid[expense.paidBy.id] =
                (totalPaid[expense.paidBy.id] ?: 0.0) + expense.amount

            // Divide the expense among participants
            val perPersonShare = expense.amount / expense.sharedBy.size
            expense.sharedBy.forEach { person ->
                totalOwed[person.id] = (totalOwed[person.id] ?: 0.0) + perPersonShare
            }
        }

        // Create split results
        _splitResults.value = participants.map { person ->
            val paid = totalPaid[person.id] ?: 0.0
            val owed = totalOwed[person.id] ?: 0.0
            val balance = paid - owed

            SplitResult(
                person = person,
                totalPaid = paid,
                totalOwed = owed,
                balance = balance
            )
        }.sortedByDescending { it.balance }

        // Calculate settlements
        calculateSettlements()
    }

    private fun calculateSettlements() {
        val results = _splitResults.value

        // Separate creditors (people who should receive) and debtors (people who owe)
        val creditors = results.filter { it.balance > 0.01 }.toMutableList()
        val debtors = results.filter { it.balance < -0.01 }.toMutableList()

        val settlements = mutableListOf<Settlement>()

        while (creditors.isNotEmpty() && debtors.isNotEmpty()) {
            val creditor = creditors.first()
            val debtor = debtors.first()

            val amountToSettle = minOf(creditor.balance, -debtor.balance)

            settlements.add(
                Settlement(
                    from = debtor.person,
                    to = creditor.person,
                    amount = amountToSettle
                )
            )

            // Update balances
            val updatedCreditor = creditor.copy(balance = creditor.balance - amountToSettle)
            val updatedDebtor = debtor.copy(balance = debtor.balance + amountToSettle)

            creditors[0] = updatedCreditor
            debtors[0] = updatedDebtor

            // Remove if settled
            if (updatedCreditor.balance < 0.01) creditors.removeAt(0)
            if (updatedDebtor.balance > -0.01) debtors.removeAt(0)
        }

        _settlements.value = settlements
    }

    fun clearAll() {
        _participants.value = emptyList()
        _expenses.value = emptyList()
        _splitResults.value = emptyList()
        _settlements.value = emptyList()
    }

    fun getTotalExpenses(): Double {
        return _expenses.value.sumOf { it.amount }
    }
}