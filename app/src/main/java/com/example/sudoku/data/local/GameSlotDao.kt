package com.example.sudoku.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GameSlotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: GameSlotEntity)

    @Query("SELECT * FROM game_slots WHERE slotId = :slotId")
    suspend fun getSlot(slotId: String): GameSlotEntity?
}
