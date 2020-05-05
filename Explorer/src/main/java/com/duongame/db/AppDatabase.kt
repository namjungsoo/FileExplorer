package com.duongame.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.duongame.adapter.ExplorerItem

@Database(entities = arrayOf(ExplorerItem::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun explorerItemDao(): ExplorerItemDao
}