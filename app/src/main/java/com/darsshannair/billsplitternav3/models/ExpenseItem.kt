package com.darsshannair.billsplitternav3.models

import java.util.UUID

data class ExpenseItem(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val amount: Double,
    val paidBy: Person,
    val sharedBy: List<Person>
)