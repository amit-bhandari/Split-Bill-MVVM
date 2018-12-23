package com.splitbill.amit.splitbill.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.splitbill.amit.splitbill.viewModel.CreateGroupViewModel

class StartUpActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            val model = ViewModelProviders.of(this).get(CreateGroupViewModel::class.java)

            model.users.observe(this, Observer {
                model.users.removeObservers(this)
                if(it.count()>1) {
                    val intent = Intent(this@StartUpActivity, MainActivity::class.java)
                    startActivity(intent)
                }else{
                    val intent = Intent(this@StartUpActivity, CreateGroupActivity::class.java)
                    startActivity(intent)
                }
                finish()
            })
    }
}