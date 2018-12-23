package com.splitbill.amit.splitbill.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.splitbill.amit.splitbill.MyApp
import com.splitbill.amit.splitbill.repo.*
import org.json.JSONObject
import java.lang.IllegalStateException
import kotlin.math.absoluteValue

class MainViewModel: ViewModel() {

    var users: LiveData<List<User>>
    var transactions: LiveData<List<Transaction>>

    private var db: AppDatabase = MyApp.dbInstance

    init {
        users = db.getDao().getUsers()
        transactions = db.getDao().getTransactions()
    }

    fun addTransaction(transaction: Transaction){
        db.getDao().addTransaction(transaction)
    }

    fun calculateSettlements() : List<Transfer>{
        println("Data ${transactions.value}")
        val transfers = transactions.value
            ?.map { it.paidBy-it.paidFor }
            ?.merge()
            ?.groupByTo(mutableMapOf()){if(it.money < 0) "owed" else "paid"}
            ?.calculateTransfers()

        println("Transfers $transfers")

        return transfers?.toList() ?: listOf()
    }

    fun generateSettlementJson():JSONObject{
        val json = transactions.value
            ?.map { it.paidBy-it.paidFor }
            ?.merge()
            ?.groupByTo(mutableMapOf()){if(it.money < 0) "owed" else "paid"}
            ?.getJsonObject()

        println("Json object $json")

        return json ?: JSONObject()
    }

    fun clearDb(){
        db.getDao().nukeTransaction()
        db.getDao().nukeUsers()
    }
}

operator fun Iterable<UserMoneyComposite>.minus(elements: Iterable<UserMoneyComposite>)
        :Iterable<UserMoneyComposite> {
    if(this.count()!=elements.count()) throw IllegalStateException("Different size of list not allowed")
    val list = mutableListOf<UserMoneyComposite>()
    for(i in 0 until this.count()){
        list.add(UserMoneyComposite(this.elementAt(i).userId, this.elementAt(i).money-elements.elementAt(i).money))
    }
    return list
}

operator fun Iterable<UserMoneyComposite>.plus(elements: Iterable<UserMoneyComposite>)
        :Iterable<UserMoneyComposite> {
    if(this.count()!=elements.count()) throw IllegalStateException("Different size of list not allowed")
    val list = mutableListOf<UserMoneyComposite>()
    for(i in 0 until this.count()){
        list.add(UserMoneyComposite(this.elementAt(i).userId, this.elementAt(i).money+elements.elementAt(i).money))
    }
    return list
}

fun Iterable<Iterable<UserMoneyComposite>>.merge()
        :Iterable<UserMoneyComposite>{
    println("just")
    if(count()==0) return listOf()
    if(count()==1) return elementAt(0)

    var finalList = elementAt(0)
    for(i in 1 until this.count()){
        finalList += this.elementAt(i)
    }

    //check if total turns out to be 0, if not throw the exception
    var total = 0L
    for(composite in finalList){
        total+=composite.money
    }
    if(total!=0L) throw IllegalStateException("Something bad happened, settlement cannot be created")

    return finalList
}

fun Map<String, MutableList<UserMoneyComposite>>.calculateTransfers()
        :Iterable<Transfer> {
    val transfers = mutableListOf<Transfer>()
    while (this["owed"]?.count() ?: 0 > 0 && this["paid"]?.count() ?: 0 > 0) {
        val sender = this["owed"]?.get(0)
        val receiver = this["paid"]?.get(0)

        val canGive = sender?.money?.absoluteValue ?: 0
        val demand = receiver?.money ?: 0
        val possibleToGive = Math.min(canGive, demand)

        sender?.money = canGive - possibleToGive
        if (sender?.money == 0L) this["owed"]?.remove(sender)

        receiver?.money = demand - possibleToGive
        if (receiver?.money == 0L) this["paid"]?.remove(receiver)

        transfers.add(Transfer(sender?.userId ?: "", receiver?.userId ?: "", possibleToGive))
    }
    return transfers
}

fun Map<String, MutableList<UserMoneyComposite>>.getJsonObject()
        :JSONObject {
    val json = JSONObject()
    //val transfers = mutableListOf<Transfer>()
    while (this["owed"]?.count() ?: 0 > 0 && this["paid"]?.count() ?: 0 > 0) {
        val sender = this["owed"]?.get(0)
        val receiver = this["paid"]?.get(0)

        val canGive = sender?.money?.absoluteValue ?: 0
        val demand = receiver?.money ?: 0
        val possibleToGive = Math.min(canGive, demand)

        sender?.money = canGive - possibleToGive
        if (sender?.money == 0L) this["owed"]?.remove(sender)

        receiver?.money = demand - possibleToGive
        if (receiver?.money == 0L) this["paid"]?.remove(receiver)

        //transfers.add(Transfer(sender?.userId ?: "", receiver?.userId ?: "", possibleToGive))

        if(json.has(sender?.userId)){
            json.getJSONObject(sender?.userId).put(receiver?.userId, possibleToGive*-1)
        }else {
            json.put(sender?.userId, JSONObject().put(receiver?.userId, possibleToGive*-1))
        }

        if(json.has(receiver?.userId)){
            json.getJSONObject(receiver?.userId).put(sender?.userId, possibleToGive)
        }else {
            json.put(receiver?.userId, JSONObject().put(sender?.userId, possibleToGive))
        }
    }
    return json
}