package com.duongame.db

import androidx.room.*
import com.duongame.adapter.ExplorerItem

@Dao
interface ExplorerItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(items: List<ExplorerItem>)

    @Query("DELETE FROM ExplorerItem")
    fun deleteAll()

    @Query("SELECT * FROM ExplorerItem")
    fun getItems(): List<ExplorerItem>
}