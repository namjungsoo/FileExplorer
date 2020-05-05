package com.duongame.db

import android.content.Context
import androidx.room.Room
import com.duongame.adapter.ExplorerItem

class ExplorerItemDB(context: Context) {
    val db: AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "explorer.db").build()

    companion object {
        lateinit var instance: ExplorerItemDB

        fun getInstance(context: Context): ExplorerItemDB {
            instance = ExplorerItemDB(context)
            return instance
        }
    }

    fun getItems(): List<ExplorerItem> {
        return db.explorerItemDao().getItems()
    }

    fun deleteAll() {
        db.explorerItemDao().deleteAll()
    }

    fun insertItems(items: List<ExplorerItem>) {
        db.explorerItemDao().insertItems(items)
    }
}