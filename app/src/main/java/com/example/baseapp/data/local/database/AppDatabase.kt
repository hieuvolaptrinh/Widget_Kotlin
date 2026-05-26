package com.example.baseapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.baseapp.data.local.converter.Converters
import com.example.baseapp.data.local.dao.LoginPasswordDao
import com.example.baseapp.data.local.entity.LoginPassword

@Database(
    entities = [LoginPassword::class],
    version = 1,
    exportSchema = true,
    //    autoMigrations = [
//        AutoMigration(
//            from = 1,
//            to = 2
//        ),
//    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun loginDao(): LoginPasswordDao
}