package com.splitbill.amit.splitbill

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.room.Room
import com.splitbill.amit.splitbill.repo.AppDatabase

class MyApp : Application(){

    //writing with dagger 2 will be overkill
     companion object {
         lateinit var instance: Application
         lateinit var dbInstance: AppDatabase
         lateinit var pref: SharedPreferences
     }

    override fun onCreate() {
        super.onCreate()
        dbInstance = Room.databaseBuilder(this, AppDatabase::class.java, "MyDB").build()
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        instance = this
    }
}