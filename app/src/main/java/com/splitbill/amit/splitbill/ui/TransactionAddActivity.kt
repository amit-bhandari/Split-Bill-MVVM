package com.splitbill.amit.splitbill.ui

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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

    lateinit var model: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_add)
        model = ViewModelProviders.of(this).get(MainViewModel::class.java)

        model.users.observe(this, Observer {
            for(user in it){
                val paidByView = layoutInflater.inflate(R.layout.item_amount_input,null)
                paidByView.findViewById<TextView>(R.id.name).text = user.name
                paidByWrapper.addView(paidByView)

                val paidForView = layoutInflater.inflate(R.layout.item_amount_input,null)
                paidForView.findViewById<TextView>(R.id.name).text = user.name
                paidForWrapper.addView(paidForView)
            }
        })

        button_add.setOnClickListener {
            if(transaction_name.text.isEmpty()) {
                transaction_name.error = "Enter name"
                return@setOnClickListener
            }

            var paidByTotal = 0L
            for(i in 0 until paidByWrapper.childCount){
                var amount = 0
                val amountString = paidByWrapper.getChildAt(i).findViewById<EditText>(R.id.amount).text.toString()
                if(amountString.isNotEmpty())
                    amount = amountString.toInt()
                paidByTotal += amount
            }

            if(paidByTotal <= 0){
                Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var paidForTotal = 0L
            for(i in 0 until paidForWrapper.childCount){
                var amount = 0
                val amountString = paidForWrapper.getChildAt(i).findViewById<EditText>(R.id.amount).text.toString()
                if(amountString.isNotEmpty())
                    amount = amountString.toInt()
                paidForTotal += amount
            }

            if(paidByTotal != paidForTotal){
                Toast.makeText(this, "Totals does not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //process
            val paidBy = mutableListOf<UserMoneyComposite>()
            for(i in 0 until paidByWrapper.childCount){
                val view = paidByWrapper.getChildAt(i)
                val amountString = view.findViewById<TextView>(R.id.amount).text.toString()
                var amount = 0L
                if(amountString.isNotEmpty()) amount = amountString.toLong()

                paidBy.add(UserMoneyComposite(view.findViewById<TextView>(R.id.name).text.toString(),amount))
            }

            val paidFor = mutableListOf<UserMoneyComposite>()
            for(i in 0 until paidForWrapper.childCount){
                val view = paidForWrapper.getChildAt(i)
                val amountString = view.findViewById<TextView>(R.id.amount).text.toString()
                var amount = 0L
                if(amountString.isNotEmpty()) amount = amountString.toLong()

                paidFor.add(UserMoneyComposite(view.findViewById<TextView>(R.id.name).text.toString(), amount))
            }

            GlobalScope.launch {
                model.addTransaction(Transaction(UUID.randomUUID().toString(),transaction_name.text.toString(),paidByTotal, paidBy, paidFor))
                finish()
            }
        }

    }
}