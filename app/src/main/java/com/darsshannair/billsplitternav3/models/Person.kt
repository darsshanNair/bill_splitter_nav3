package com.darsshannair.billsplitternav3.models

import java.util.UUID

data class Person(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)