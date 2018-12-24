package com.splitbill.amit.splitbill.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.splitbill.amit.splitbill.R
import com.splitbill.amit.splitbill.repo.Transaction
import com.splitbill.amit.splitbill.repo.UserMoneyComposite
import com.splitbill.amit.splitbill.viewModel.MainViewModel
import kotlinx.android.synthetic.main.activity_transaction_add.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class TransactionAddActivity: AppCompatActivity() {

    private lateinit var model: MainViewModel

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_add)

        model = ViewModelProviders.of(this).get(MainViewModel::class.java)

        //show 2 sections
        //1 for people who paid
        //2 for people who didn't have money xD
        //@todo give checkbox as "split equally" pressing which total in "who paid" will b divided equally among all people
        model.users.observe(this, Observer { users ->
            val categories = ArrayList<String>()
            for(user in users){
                categories.add(user.name)
            }
            categories.add("Multiple")
            val dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
            paid_by_spinner.adapter = dataAdapter
            paid_by_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    val item = p0?.getItemAtPosition(position).toString()
                    paidByWrapper.removeAllViews()
                    if(item=="Multiple"){
                        for(user in users){
                            val paidByView = layoutInflater.inflate(R.layout.item_amount_input,null)
                            paidByView.findViewById<TextView>(R.id.name).text = user.name
                            paidByWrapper.addView(paidByView)
                        }
                    }else{
                        val paidByView = layoutInflater.inflate(R.layout.item_amount_input,null)
                        paidByView.findViewById<TextView>(R.id.name).text = item
                        paidByWrapper.addView(paidByView)
                    }
                }

            }

            val cat = ArrayList<String>()
            cat.add("Split Equally")
            cat.add("Split Unequally")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cat)
            paid_for_spinner.adapter = adapter
            paid_for_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    val item = p0?.getItemAtPosition(position).toString()
                    paidForWrapper.removeAllViews()
                    if(item=="Split Unequally"){
                        for(user in users){
                            val paidForView = layoutInflater.inflate(R.layout.item_amount_input,null)
                            paidForView.findViewById<TextView>(R.id.name).text = user.name
                            paidForWrapper.addView(paidForView)
                        }
                    }
                }

            }

            button_add.setOnClickListener {
                //some basic validity of data before adding data to db
                if(transaction_name.text.isEmpty()) {
                    transaction_name.error = "Enter name"
                    return@setOnClickListener
                }

                var paidByTotal = 0f
                for(i in 0 until users.count()){
                    var amount = 0
                    val child = paidByWrapper.getChildAt(i)
                    var amountString = ""
                    if(child!=null) amountString = child.findViewById<EditText>(R.id.amount).text.toString()
                    if(amountString.isNotEmpty()) amount = amountString.toInt()
                    paidByTotal += amount
                }

                if(paidByTotal <= 0){
                    Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val item = paid_for_spinner?.selectedItem.toString()

                if(item=="Split Unequally") {
                    var paidForTotal = 0f
                    for (i in 0 until users.count()) {
                        var amount = 0f
                        val child = paidForWrapper.getChildAt(i)
                        var amountString = ""
                        if (child != null) amountString = child.findViewById<EditText>(R.id.amount).text.toString()
                        if (amountString.isNotEmpty()) amount = amountString.toFloat()
                        paidForTotal += amount
                    }

                    if (paidByTotal != paidForTotal) {
                        Toast.makeText(this, "Totals does not match", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }

                //prepare data for transaction model and add it to db
                val paidBy = mutableListOf<UserMoneyComposite>()
                for(i in 0 until users.count()){
                    var amount = 0f
                    val child = paidByWrapper.getChildAt(i)
                    var amountString = ""
                    if (child != null) amountString = child.findViewById<EditText>(R.id.amount).text.toString()
                    if (amountString.isNotEmpty()) amount = amountString.toFloat()

                    paidBy.add(UserMoneyComposite(users[i].name,amount))
                }
                val paidFor = mutableListOf<UserMoneyComposite>()
                if(item=="Split Unequally") {
                    for (i in 0 until users.count()) {
                        val view = paidForWrapper.getChildAt(i)
                        val amountString = view.findViewById<TextView>(R.id.amount).text.toString()
                        var amount = 0f
                        if (amountString.isNotEmpty()) amount = amountString.toFloat()

                        paidFor.add(UserMoneyComposite(view.findViewById<TextView>(R.id.name).text.toString(), amount))
                    }
                }else{
                    val divided = paidByTotal/users.count()
                    for (user in users) {
                        paidFor.add(UserMoneyComposite(user.name, divided))
                    }
                }

                GlobalScope.launch {
                    model.addTransaction(Transaction(UUID.randomUUID().toString(),transaction_name.text.toString(),paidByTotal, paidBy, paidFor))
                    finish()
                }
            }
        })
    }
}