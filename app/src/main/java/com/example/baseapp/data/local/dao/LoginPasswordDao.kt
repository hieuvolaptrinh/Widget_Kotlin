package com.example.baseapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.baseapp.data.local.entity.LoginPassword

@Dao
interface LoginPasswordDao {
    @Query("SELECT * FROM logins")
    suspend fun getAll(): List<LoginPassword>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: LoginPassword)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<LoginPassword>)

    @Update
    suspend fun update(item: LoginPassword)

    @Update
    suspend fun updateAll(items: List<LoginPassword>)

    @Delete
    suspend fun delete(item: LoginPassword)

    @Query("SELECT * FROM logins WHERE name = :name")
    suspend fun getItemByName(name: String): List<LoginPassword>
}