package com.darsshannair.billsplitternav3.models

data class SplitResult(
    val person: Person,
    val totalPaid: Double,
    val totalOwed: Double,
    val balance: Double
)