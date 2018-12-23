package com.splitbill.amit.splitbill.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.splitbill.amit.splitbill.R
import com.splitbill.amit.splitbill.viewModel.CreateGroupViewModel
import kotlinx.android.synthetic.main.activity_create_group.*
import android.provider.ContactsContract
import android.content.Intent
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.splitbill.amit.splitbill.helpers.getBackGradient
import com.splitbill.amit.splitbill.repo.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CreateGroupActivity: AppCompatActivity() {

    private lateinit var model: CreateGroupViewModel
    private val PICK_CONTACT = 1
    private val adapter = MemberAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        //root_view.setBackgroundDrawable(getBackGradient())
        model = ViewModelProviders.of(this).get(CreateGroupViewModel::class.java)
        model.users.observe(this, Observer {
            adapter.setData(it)
            button_create.isEnabled = it.count()>1
        })

        button_add.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            startActivityForResult(intent, PICK_CONTACT)
        }

        button_create.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)

        title = "Split Bills"
    }

    override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        when (reqCode) {
            PICK_CONTACT -> if (resultCode == Activity.RESULT_OK) {
                GlobalScope.launch(Dispatchers.IO){
                    createAndAddUser(data)
                }
            }
        }
    }

    private fun createAndAddUser(data: Intent?) {
        val contactData = data?.data
        val c = contentResolver.query(contactData!!, null, null, null, null)
        var name = ""
        var number = ""
        if (c.moveToFirst()) {
            val id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            val hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
            if (hasPhone.equals("1", ignoreCase = true)) {
                val phones = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null
                )
                phones!!.moveToFirst()
                number = phones.getString(phones.getColumnIndex("data1"))
                phones.close()
            }
            name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
        }
        c.close()
        model.addUser(User(number, name))
    }

    inner class MemberAdapter: RecyclerView.Adapter<MemberAdapter.MyViewHolder>(){
        private var users: List<User> = listOf()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(this@CreateGroupActivity).inflate(R.layout.item_member, parent, false))
        }

        override fun getItemCount(): Int {
            return users.count()
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.name.text = users[position].name
            holder.number.text = users[position].userId
        }

        fun setData(data: List<User>){
            this.users = data
            notifyDataSetChanged()
        }

        inner class MyViewHolder(view:View): RecyclerView.ViewHolder(view) {
            val name:TextView = view.findViewById(R.id.name)
            val number:TextView = view.findViewById(R.id.number)
        }
    }
}