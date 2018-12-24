package com.splitbill.amit.splitbill.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.splitbill.amit.splitbill.R
import com.splitbill.amit.splitbill.viewModel.MainViewModel
import kotlinx.android.synthetic.main.activity_settlement.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SettlementActivity: AppCompatActivity() {

    private lateinit var model : MainViewModel
    private val handler: Handler = Handler(Looper.getMainLooper())

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settlement)
        title = "Settlements"
        model = ViewModelProviders.of(this).get(MainViewModel::class.java)

        model.transactions.observe(this, Observer {
            GlobalScope.launch {
                val transfers = model.calculateSettlements()

                val jsonString = model.generateSettlementJson().toString(4)

                handler.post {
                    if(transfers.isEmpty()){
                        settled.visibility = View.VISIBLE
                        json_text.visibility = View.GONE
                        return@post
                    }

                    json_text.text = jsonString

                    //not using recycler view just for change as there is no need for changing items dynamically over here
                    //and why write 100 lines of adapter when 4 lines does just fine!
                    for(transfer in transfers){
                        val view = layoutInflater.inflate(R.layout.item_settlement, null)
                        view.findViewById<TextView>(R.id.settlement_text).text = "${transfer.sender} owes ${transfer.receiver}"
                        view.findViewById<TextView>(R.id.amount_text).text = "${transfer.amount}"
                        settlementWrapper.addView(view)
                    }
                }
            }
        })
    }
}