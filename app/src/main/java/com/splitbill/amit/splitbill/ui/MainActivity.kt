package com.splitbill.amit.splitbill.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.splitbill.amit.splitbill.R
import com.splitbill.amit.splitbill.repo.Transaction
import com.splitbill.amit.splitbill.viewModel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import androidx.recyclerview.widget.DividerItemDecoration
import com.splitbill.amit.splitbill.MyApp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var model: MainViewModel
    private val adapter = TransactionAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = ViewModelProviders.of(this).get(MainViewModel::class.java)
        model.transactions.observe(this, Observer {
            adapter.setData(it)
            if(it.count()>0){
                rv.visibility = View.VISIBLE
                no_data.visibility = View.INVISIBLE
            }else{
                no_data.visibility = View.VISIBLE
                rv.visibility = View.INVISIBLE
            }
            progress.visibility = View.INVISIBLE
        })

        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)
        /*val dividerItemDecoration = DividerItemDecoration(
            rv.context,
            (rv.layoutManager as LinearLayoutManager).orientation
        )
        rv.addItemDecoration(dividerItemDecoration)*/

        fab.setOnClickListener {
            startActivity(Intent(this, TransactionAddActivity::class.java))
        }

        button_settle_up.setOnClickListener {
            startActivity(Intent(this, SettlementActivity::class.java))
        }

        title = MyApp.pref.getString("group_name", "Awesome Group")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.clear -> {
                GlobalScope.launch {
                    model.clearDb()
                    startActivity(Intent(this@MainActivity, StartUpActivity::class.java))
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class TransactionAdapter: RecyclerView.Adapter<TransactionAdapter.MyViewHolder>(){
        private var transactions: List<Transaction> = listOf()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(this@MainActivity).inflate(R.layout.item_transaction, parent, false))
        }

        override fun getItemCount(): Int {
            return transactions.count()
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.name.text = transactions[position].name
            holder.number.text = transactions[position].totalAmount.toString()
            holder.wrapper.removeAllViews()
            for(i in 0 until transactions[position].paidBy.count()){
                if(transactions[position].paidBy[i].money <= 0) continue
                val view = layoutInflater.inflate(R.layout.item_settlement_in_main, null)
                view.findViewById<TextView>(R.id.paid_by_name).text =
                        "${transactions[position].paidBy[i].userName} paid ${transactions[position].paidBy[i].money}"
                holder.wrapper.addView(view)
            }
        }

        fun setData(data: List<Transaction>){
            this.transactions = data
            notifyDataSetChanged()
        }

        inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.transaction_name)
            val number: TextView = view.findViewById(R.id.transaction_total)
            val wrapper: LinearLayout = view.findViewById(R.id.wrapper)
        }
    }
}
