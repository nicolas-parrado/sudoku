package com.example.sudoku.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AdventureRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(record: AdventureRecordEntity)

    @Query("SELECT * FROM adventure_records WHERE id = 1")
    suspend fun getRecord(): AdventureRecordEntity?

    @Query("DELETE FROM adventure_records")
    suspend fun clearRecord()
}
