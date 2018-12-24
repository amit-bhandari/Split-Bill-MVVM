package com.splitbill.amit.splitbill.ui

import android.Manifest
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
import android.Manifest.permission
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.Manifest.permission.READ_PHONE_STATE
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.os.Build
import android.widget.Toast
import androidx.annotation.NonNull
import com.splitbill.amit.splitbill.MyApp


class CreateGroupActivity: AppCompatActivity() {

    private lateinit var model: CreateGroupViewModel
    private val PICK_CONTACT = 1
    private val MY_PERMISSIONS_REQUEST = 0
    private val PERMISSIONS =
        arrayOf<String>(Manifest.permission.READ_CONTACTS)

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
            if (!hasPermissions(this, PERMISSIONS)) {
                requestPermission()
            } else {
                val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                startActivityForResult(intent, PICK_CONTACT)
            }
        }

        button_create.setOnClickListener {
            var groupName = group_name.text.toString()
            if(groupName.isEmpty()) groupName = "Awesome Group"
            MyApp.pref.edit().putString("group_name",groupName).apply()

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


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST -> {
                if (grantResults.isEmpty()) {
                    return
                }
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                    startActivityForResult(intent, PICK_CONTACT)
                } else {
                    Toast.makeText(this, "Please enable contacts permission from settings to continue", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createAndAddUser(data: Intent?) {
        //stack overflow copy paste to get contact name and mobile number from picked contact
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

        //validate user name maybe
        model.addUser(User(number, name))
    }

    private fun requestPermission() {
        // Here, thisActivity is the current activity

        ActivityCompat.requestPermissions(
            this,
            PERMISSIONS,
            MY_PERMISSIONS_REQUEST
        )

    }

    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
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