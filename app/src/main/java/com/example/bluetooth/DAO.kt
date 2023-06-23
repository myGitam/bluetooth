//package com.example.bluetooth
//
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.Query
//import kotlinx.coroutines.flow.Flow
//
//@Dao
//interface DAO {
//    @Insert
//    fun insertItem(item: Item)
//    @Query("SELECT*FROM items")
//    fun getAllItem(): Flow<List<Item>> //Flow для отслеживания изменений
//}