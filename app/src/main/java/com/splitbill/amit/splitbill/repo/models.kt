package com.splitbill.amit.splitbill.repo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey var userId: String, //mobile number
    var name: String
)

@Entity
data class Transaction(
    @PrimaryKey var transactionId: String,
    var description: String,
    var totalAmount: Long,
    var paidBy: List<UserMoneyComposite>,
    var paidFor: List<UserMoneyComposite>
)

data class UserMoneyComposite(
    var userId: String, var money: Long
)

data class Transfer(
    val sender: String,
    val receiver: String,
    val amount: Long
)