package com.splitbill.amit.splitbill

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import com.splitbill.amit.splitbill.repo.AppDatabase

class MyApp : Application(){

     companion object {
         lateinit var instance: Application
         lateinit var dbInstance: AppDatabase
     }

    override fun onCreate() {
        super.onCreate()
        dbInstance = Room.databaseBuilder(this, AppDatabase::class.java, "MyDB").build()
        instance = this
    }
}