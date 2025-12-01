package com.darsshannair.billsplitternav3.models

data class Settlement(
    val from: Person,
    val to: Person,
    val amount: Double
)