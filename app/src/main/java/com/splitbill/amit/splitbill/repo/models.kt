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
    var name: String,
    var totalAmount: Float,
    var paidBy: List<UserMoneyComposite>,
    var paidFor: List<UserMoneyComposite>
)

data class UserMoneyComposite(
    var userName: String, var money: Float
)

data class Transfer(
    val sender: String,
    val receiver: String,
    val amount: Float
)