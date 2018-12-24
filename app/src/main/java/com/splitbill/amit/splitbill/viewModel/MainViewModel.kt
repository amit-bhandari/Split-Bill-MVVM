package com.splitbill.amit.splitbill.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.splitbill.amit.splitbill.MyApp
import com.splitbill.amit.splitbill.repo.*
import org.json.JSONObject
import java.lang.IllegalStateException
import kotlin.math.absoluteValue

/**
 * Heart of whatever application I have written
 * All the splitting logic goes here!
 */
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
        val transfers = transactions.value      //get all the transactions
            ?.map { it.paidBy-it.paidFor }      //subtract all 'paid by' from 'paid for' to get simplified debt
            ?.merge()                           //combine it
            ?.groupByTo(mutableMapOf()){if(it.money < 0) "owed" else "paid"}   //group entries into 'owed' and 'paid'
            ?.calculateTransfers()              //calculate who owes who what

        println("Transfers $transfers")

        return transfers?.toList() ?: listOf()
    }

    /**
     * Get Settlements in Json format and display below settlement data
     */
    fun generateSettlementJson():JSONObject{
        val json = transactions.value
            ?.map { it.paidBy-it.paidFor }
            ?.merge()
            ?.groupByTo(mutableMapOf()){if(it.money < 0) "owed" else "paid"}
            ?.getJsonObject()

        println("Json object $json")

        return json ?: JSONObject()
    }

    /**
     * apocalypse
     */
    fun clearDb(){
        db.getDao().nukeTransaction()
        db.getDao().nukeUsers()
    }
}

/**
 * Extensions for making code look little better in Model
 */

operator fun Iterable<UserMoneyComposite>.minus(elements: Iterable<UserMoneyComposite>)
        :Iterable<UserMoneyComposite> {
    if(this.count()!=elements.count()) throw IllegalStateException("Different size of list not allowed. Something horrendously went wrong!")
    val list = mutableListOf<UserMoneyComposite>()
    for(i in 0 until this.count()){
        list.add(UserMoneyComposite(this.elementAt(i).userName, this.elementAt(i).money-elements.elementAt(i).money))
    }
    return list
}

operator fun Iterable<UserMoneyComposite>.plus(elements: Iterable<UserMoneyComposite>)
        :Iterable<UserMoneyComposite> {
    if(this.count()!=elements.count()) throw IllegalStateException("Different size of list not allowed. Something horrendously went wrong!")
    val list = mutableListOf<UserMoneyComposite>()
    for(i in 0 until this.count()){
        list.add(UserMoneyComposite(this.elementAt(i).userName, this.elementAt(i).money+elements.elementAt(i).money))
    }
    return list
}

fun Iterable<Iterable<UserMoneyComposite>>.merge()
        :Iterable<UserMoneyComposite>{
    if(count()==0) return listOf()
    if(count()==1) return elementAt(0)

    var finalList = elementAt(0)
    for(i in 1 until this.count()){
        finalList += this.elementAt(i)
    }

    return finalList
}

fun Map<String, MutableList<UserMoneyComposite>>.calculateTransfers()
        :Iterable<Transfer> {
    val transfers = mutableListOf<Transfer>()
    while (this["owed"]?.count() ?: 0 > 0 && this["paid"]?.count() ?: 0 > 0) {
        val sender = this["owed"]?.get(0)
        val receiver = this["paid"]?.get(0)

        val canGive = sender?.money?.absoluteValue ?: 0f
        val demand = receiver?.money ?: 0f
        val possibleToGive = Math.min(canGive, demand)

        sender?.money = canGive - possibleToGive
        if (sender?.money == 0f) this["owed"]?.remove(sender)

        receiver?.money = demand - possibleToGive
        if (receiver?.money == 0f) this["paid"]?.remove(receiver)

        transfers.add(Transfer(sender?.userName ?: "", receiver?.userName ?: "", possibleToGive))
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

        val canGive = sender?.money?.absoluteValue ?: 0f
        val demand = receiver?.money ?: 0f
        val possibleToGive = Math.min(canGive, demand)

        sender?.money = canGive - possibleToGive
        if (sender?.money == 0f) this["owed"]?.remove(sender)

        receiver?.money = demand - possibleToGive
        if (receiver?.money == 0f) this["paid"]?.remove(receiver)

        //transfers.add(Transfer(sender?.userName ?: "", receiver?.userName ?: "", possibleToGive))

        if(json.has(sender?.userName)){
            json.getJSONObject(sender?.userName).put(receiver?.userName, possibleToGive*-1)
        }else {
            json.put(sender?.userName, JSONObject().put(receiver?.userName, possibleToGive*-1))
        }

        if(json.has(receiver?.userName)){
            json.getJSONObject(receiver?.userName).put(sender?.userName, possibleToGive)
        }else {
            json.put(receiver?.userName, JSONObject().put(sender?.userName, possibleToGive))
        }
    }
    return json
}