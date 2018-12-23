package com.splitbill.amit.splitbill.repo

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DbDao {

    @Query("DELETE FROM user")
    fun nukeUsers()

    @Query("DELETE FROM `transaction`")
    fun nukeTransaction()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addUsers(users: List<User>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addUser(user: User)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addTransaction(transaction: Transaction)

    @Query("SELECT * FROM user")
    fun getUsers(): LiveData<List<User>>

    @Query("SELECT * FROM `transaction`")
    fun getTransactions(): LiveData<List<Transaction>>

}