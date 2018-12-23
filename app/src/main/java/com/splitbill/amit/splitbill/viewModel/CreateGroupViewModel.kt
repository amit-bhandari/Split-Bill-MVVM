package com.splitbill.amit.splitbill.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.splitbill.amit.splitbill.MyApp
import com.splitbill.amit.splitbill.repo.AppDatabase
import com.splitbill.amit.splitbill.repo.User

class CreateGroupViewModel: ViewModel() {
    var users: LiveData<List<User>>
    private var db: AppDatabase = MyApp.dbInstance

    init {
        users = db.getDao().getUsers()
    }

    //@todo error handling
    fun addUser(user: User){
        db.getDao().addUser(user)
    }
}